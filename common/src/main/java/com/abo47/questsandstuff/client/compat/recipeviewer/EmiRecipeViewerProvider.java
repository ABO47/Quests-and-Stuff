package com.abo47.questsandstuff.client.compat.recipeviewer;

import com.abo47.questsandstuff.client.canvas.recipe.CanvasRecipeCardRecipes.RecipeView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

final class EmiRecipeViewerProvider implements RecipeViewerProvider {
    private static final String EMI_API = "dev.emi.emi.api.EmiApi";
    private static final String EMI_CONFIG = "dev.emi.emi.config.EmiConfig";
    private static final String EMI_DRAW_CONTEXT = "dev.emi.emi.runtime.EmiDrawContext";
    private static final String EMI_PORT = "dev.emi.emi.EmiPort";
    private static final String EMI_RENDER_HELPER = "dev.emi.emi.EmiRenderHelper";
    private static final String EMI_INGREDIENT = "dev.emi.emi.api.stack.EmiIngredient";
    private static final String EMI_STACK = "dev.emi.emi.api.stack.EmiStack";

    @Override
    public String name() {
        return "EMI";
    }

    @Override
    public boolean isAvailable() {
        return RecipeViewerReflection.classPresent(EMI_API)
                && RecipeViewerReflection.classPresent(EMI_INGREDIENT)
                && RecipeViewerReflection.classPresent(EMI_STACK);
    }

    @Override
    public boolean showRecipes(ItemStack stack) {
        return show(stack, "displayRecipes");
    }

    @Override
    public boolean showUses(ItemStack stack) {
        return show(stack, "displayUses");
    }

    @Override
    public boolean supportsNativeRecipeSelection() {
        return true;
    }

    @Override
    public boolean matchesRecipeKey(int keyCode, int scanCode) {
        return RecipeViewerReflection.matchesPublicStaticBind(EMI_CONFIG, "viewRecipes", keyCode, scanCode);
    }

    @Override
    public boolean matchesUsesKey(int keyCode, int scanCode) {
        return RecipeViewerReflection.matchesPublicStaticBind(EMI_CONFIG, "viewUses", keyCode, scanCode);
    }

    @Override
    public boolean renderRecipeSnapshot(GuiGraphics graphics, RecipeView recipe, int width, int height, int pivotX, int pivotY) {
        if (recipe == null || recipe.id().isBlank()) {
            return false;
        }
        String key = "emi-live:" + recipe.typeId() + ":" + recipe.id();
        return RecipeViewerSnapshotRenderer.renderLive(graphics, key, () -> createSnapshotPlan(recipe), width, height, pivotX, pivotY);
    }

    private boolean show(ItemStack stack, String methodName) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        try {
            Class<?> emiStackClass = Class.forName(EMI_STACK);
            Object ingredient = emiStackClass.getMethod("of", ItemStack.class).invoke(null, stack.copy());
            Class<?> ingredientClass = Class.forName(EMI_INGREDIENT);
            Class.forName(EMI_API).getMethod(methodName, ingredientClass).invoke(null, ingredient);
            return true;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return false;
        }
    }

    private RecipeViewerSnapshotRenderer.SnapshotPlan createSnapshotPlan(RecipeView view) throws ReflectiveOperationException {
        Object recipeId = emiId(view.id());
        if (recipeId == null) {
            return null;
        }
        Class<?> apiClass = Class.forName(EMI_API);
        Object recipeManager = apiClass.getMethod("getRecipeManager").invoke(null);
        Object recipe = findRecipe(recipeManager, recipeId, view.typeId());
        if (recipe == null) {
            return null;
        }
        int displayWidth = intValue(RecipeViewerReflection.firstMethod(recipe.getClass(), "getDisplayWidth", 0).invoke(recipe));
        int displayHeight = intValue(RecipeViewerReflection.firstMethod(recipe.getClass(), "getDisplayHeight", 0).invoke(recipe));
        if (displayWidth <= 0 || displayHeight <= 0) {
            return null;
        }
        int snapshotWidth = displayWidth + 8;
        int snapshotHeight = displayHeight + 8;
        Class<?> drawContextClass = Class.forName(EMI_DRAW_CONTEXT);
        Class<?> renderHelperClass = Class.forName(EMI_RENDER_HELPER);
        Method wrap = RecipeViewerReflection.firstMethod(drawContextClass, "wrap", 1);
        Method renderRecipe = RecipeViewerReflection.firstMethod(renderHelperClass, "renderRecipe", 6);
        return new RecipeViewerSnapshotRenderer.SnapshotPlan(snapshotWidth, snapshotHeight, snapshotGraphics -> {
            try {
                Object context = wrap.invoke(null, snapshotGraphics);
                renderRecipe.invoke(null, recipe, context, 0, 0, false, -1);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(exception);
            }
        });
    }

    private static Object findRecipe(Object recipeManager, Object recipeId, String typeId) throws ReflectiveOperationException {
        Object recipe = findRecipeByCategory(recipeManager, recipeId, typeId);
        if (recipe != null) {
            return recipe;
        }
        return RecipeViewerReflection.firstMethod(recipeManager.getClass(), "getRecipe", 1).invoke(recipeManager, recipeId);
    }

    private static Object findRecipeByCategory(Object recipeManager, Object recipeId, String typeId) throws ReflectiveOperationException {
        Object categoryId = emiId(typeId);
        if (categoryId == null) {
            return null;
        }
        Object categories = RecipeViewerReflection.firstMethod(recipeManager.getClass(), "getCategories", 0).invoke(recipeManager);
        if (!(categories instanceof List<?> list)) {
            return null;
        }
        for (Object category : list) {
            Object id = RecipeViewerReflection.firstMethod(category.getClass(), "getId", 0).invoke(category);
            if (!Objects.equals(id, categoryId)) {
                continue;
            }
            Object recipes = RecipeViewerReflection.firstMethod(recipeManager.getClass(), "getRecipes", 1).invoke(recipeManager, category);
            if (!(recipes instanceof List<?> recipeList)) {
                return null;
            }
            for (Object recipe : recipeList) {
                Object idValue = RecipeViewerReflection.firstMethod(recipe.getClass(), "getId", 0).invoke(recipe);
                if (Objects.equals(idValue, recipeId)) {
                    return recipe;
                }
            }
            return null;
        }
        return null;
    }

    private static int intValue(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private static Object emiId(String value) throws ReflectiveOperationException {
        ResourceLocation id = ResourceLocation.tryParse(value == null ? "" : value.trim());
        if (id == null) {
            return null;
        }
        Class<?> portClass = Class.forName(EMI_PORT);
        return RecipeViewerReflection.firstMethod(portClass, "id", 1).invoke(null, id.toString());
    }
}
