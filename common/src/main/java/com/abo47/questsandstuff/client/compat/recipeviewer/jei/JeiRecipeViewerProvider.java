package com.abo47.questsandstuff.client.compat.recipeviewer.jei;

import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerCapabilityProbe;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerProvider;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerProviderCapabilities;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerReflectionUtils;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerSnapshotRenderer;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerCapability;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes.RecipeView;
import com.abo47.questsandstuff.client.tablet.icons.FluidIconCodec;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public final class JeiRecipeViewerProvider implements RecipeViewerProvider {
    private static final String INTERNAL = "mezz.jei.common.Internal";
    private static final String RECIPE_ROLE = "mezz.jei.api.recipe.RecipeIngredientRole";
    private static final String VANILLA_TYPES = "mezz.jei.api.constants.VanillaTypes";
    private static final String RECIPES_GUI = "mezz.jei.gui.recipes.RecipesGui";
    private static final String[] RECIPE_KEYS = {"key.jei.showRecipe", "key.jei.showRecipe2"};
    private static final String[] USES_KEYS = {"key.jei.showUses", "key.jei.showUses2"};
    private static final RecipeViewerCapabilityProbe CAPABILITIES = RecipeViewerCapabilityProbe.provider("JEI")
            .requires(RecipeViewerCapability.AVAILABLE, INTERNAL, RECIPE_ROLE, VANILLA_TYPES)
            .requires(RecipeViewerCapability.SHOW_RECIPES, INTERNAL, RECIPE_ROLE, VANILLA_TYPES)
            .requires(RecipeViewerCapability.SHOW_USES, INTERNAL, RECIPE_ROLE, VANILLA_TYPES)
            .requires(RecipeViewerCapability.NATIVE_SELECTION, INTERNAL, RECIPE_ROLE, VANILLA_TYPES)
            .requires(RecipeViewerCapability.RECIPE_KEYBIND, INTERNAL)
            .requires(RecipeViewerCapability.USES_KEYBIND, INTERNAL)
            .requires(RecipeViewerCapability.SNAPSHOT_RENDERING, INTERNAL, RECIPE_ROLE)
            .requires(RecipeViewerCapability.VISIBLE_RECIPE_PICK, RECIPES_GUI)
            .requires(RecipeViewerCapability.FLUID_ENTRIES, INTERNAL)
            .build();

    @Override
    public String name() {
        return "JEI";
    }

    @Override
    public RecipeViewerProviderCapabilities capabilities() {
        return CAPABILITIES.evaluate();
    }

    @Override
    public boolean showRecipes(ItemStack stack) {
        return show(stack, "OUTPUT");
    }

    @Override
    public boolean showUses(ItemStack stack) {
        return show(stack, "INPUT");
    }

    @Override
    public boolean showRecipes(String target) {
        return show(target, "OUTPUT");
    }

    @Override
    public boolean showUses(String target) {
        return show(target, "INPUT");
    }

    @Override
    public boolean renderRecipeSnapshot(GuiGraphics graphics, RecipeView recipe, int width, int height, int pivotX, int pivotY) {
        if (recipe == null || recipe.id().isBlank() || recipe.typeId().isBlank()) {
            return false;
        }
        ResourceLocation recipeTypeId = jeiRecipeTypeId(recipe.typeId());
        if (recipeTypeId == null) {
            return false;
        }
        String key = "jei-live:" + recipeTypeId + ":" + recipe.id();
        return RecipeViewerSnapshotRenderer.renderLive(graphics, key, () -> createSnapshotPlan(recipe), width, height, pivotX, pivotY);
    }

    @Override
    public List<String> fluidEntries() {
        if (!isAvailable()) {
            return List.of();
        }
        try {
            Object runtime = Class.forName(INTERNAL).getMethod("getJeiRuntime").invoke(null);
            Object helpers = runtime == null ? null : runtime.getClass().getMethod("getJeiHelpers").invoke(runtime);
            Object fluidHelper = helpers == null ? null : helpers.getClass().getMethod("getPlatformFluidHelper").invoke(helpers);
            Object fluidType = fluidHelper == null ? null : fluidHelper.getClass().getMethod("getFluidIngredientType").invoke(fluidHelper);
            Object ingredientManager = runtime == null ? null : runtime.getClass().getMethod("getIngredientManager").invoke(runtime);
            if (fluidType == null || ingredientManager == null) {
                return List.of();
            }
            Object ingredients = firstCompatibleMethod(ingredientManager.getClass(), "getAllIngredients", 1).invoke(ingredientManager, fluidType);
            if (!(ingredients instanceof Iterable<?> iterable)) {
                return List.of();
            }
            List<String> entries = new ArrayList<>();
            for (Object ingredient : iterable) {
                Object base = firstCompatibleMethod(fluidType.getClass(), "getBase", 1).invoke(fluidType, ingredient);
                if (base instanceof Fluid fluid) {
                    addFluidEntry(entries, fluid);
                }
            }
            return entries;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return List.of();
        }
    }

    @Override
    public boolean matchesRecipeKey(int keyCode, int scanCode) {
        return RecipeViewerReflectionUtils.matchesMinecraftKey(RECIPE_KEYS, keyCode, scanCode);
    }

    @Override
    public boolean matchesUsesKey(int keyCode, int scanCode) {
        return RecipeViewerReflectionUtils.matchesMinecraftKey(USES_KEYS, keyCode, scanCode);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean show(ItemStack stack, String roleName) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        try {
            Class<?> internalClass = Class.forName(INTERNAL);
            Object runtime = internalClass.getMethod("getJeiRuntime").invoke(null);
            if (runtime == null) {
                return false;
            }
            Object helpers = runtime.getClass().getMethod("getJeiHelpers").invoke(runtime);
            Object focusFactory = helpers.getClass().getMethod("getFocusFactory").invoke(helpers);
            Class<?> roleClass = Class.forName(RECIPE_ROLE);
            Object role = Enum.valueOf((Class<Enum>) roleClass.asSubclass(Enum.class), roleName);
            Object itemStackType = Class.forName(VANILLA_TYPES).getField("ITEM_STACK").get(null);
            Method createFocus = RecipeViewerReflectionUtils.firstMethod(focusFactory.getClass(), "createFocus", 3);
            Object focus = createFocus.invoke(focusFactory, role, itemStackType, stack.copy());
            Object recipesGui = runtime.getClass().getMethod("getRecipesGui").invoke(runtime);
            recipesGui.getClass().getMethod("show", List.class).invoke(recipesGui, List.of(focus));
            return true;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return false;
        }
    }

    private RecipeViewerSnapshotRenderer.SnapshotPlan createSnapshotPlan(RecipeView view) throws ReflectiveOperationException {
        ResourceLocation recipeId = ResourceLocation.tryParse(view.id());
        ResourceLocation recipeTypeId = jeiRecipeTypeId(view.typeId());
        if (recipeId == null || recipeTypeId == null) {
            return null;
        }
        Object runtime = Class.forName(INTERNAL).getMethod("getJeiRuntime").invoke(null);
        if (runtime == null) {
            return null;
        }
        Object recipeManager = runtime.getClass().getMethod("getRecipeManager").invoke(runtime);
        Object focusGroup = emptyFocusGroup(runtime);
        RecipeMatch match = findJeiRecipeMatch(recipeManager, recipeTypeId, recipeId);
        if (match == null) {
            return null;
        }
        Object layout = createLayout(recipeManager, match.category(), match.recipe(), focusGroup);
        if (layout == null) {
            return null;
        }
        Rect2i original = (Rect2i) layout.getClass().getMethod("getRect").invoke(layout);
        Rect2i border = (Rect2i) layout.getClass().getMethod("getRectWithBorder").invoke(layout);
        layout.getClass().getMethod("setPosition", int.class, int.class)
                .invoke(layout, original.getX() - border.getX(), original.getY() - border.getY());
        Rect2i placedBorder = (Rect2i) layout.getClass().getMethod("getRectWithBorder").invoke(layout);
        int snapshotWidth = Math.max(1, placedBorder.getWidth());
        int snapshotHeight = Math.max(1, placedBorder.getHeight());
        return new RecipeViewerSnapshotRenderer.SnapshotPlan(snapshotWidth, snapshotHeight, snapshotGraphics -> {
            try {
                layout.getClass().getMethod("tick").invoke(layout);
                layout.getClass().getMethod("drawRecipe", GuiGraphics.class, int.class, int.class)
                        .invoke(layout, snapshotGraphics, -10_000, -10_000);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(exception);
            }
        });
    }

    private RecipeMatch findJeiRecipeMatch(Object recipeManager, ResourceLocation preferredRecipeTypeId, ResourceLocation recipeId) throws ReflectiveOperationException {
        try {
            RecipeMatch preferred = findJeiRecipeMatchByTypeId(recipeManager, preferredRecipeTypeId, recipeId);
            if (preferred != null) {
                return preferred;
            }
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
        }
        Object lookup;
        try {
            lookup = RecipeViewerReflectionUtils.firstMethod(recipeManager.getClass(), "createRecipeCategoryLookup", 0).invoke(recipeManager);
        } catch (NoSuchMethodException exception) {
            return null;
        }
        try {
            RecipeViewerReflectionUtils.firstMethod(lookup.getClass(), "includeHidden", 0).invoke(lookup);
        } catch (NoSuchMethodException ignored) {
        }
        Object value = lookup.getClass().getMethod("get").invoke(lookup);
        if (!(value instanceof Stream<?> stream)) {
            return null;
        }
        try (stream) {
            List<?> categories = stream.toList();
            for (Object category : categories) {
                RecipeMatch match = findJeiRecipeMatchByCategory(recipeManager, category, recipeId);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    private RecipeMatch findJeiRecipeMatchByTypeId(Object recipeManager, ResourceLocation recipeTypeId, ResourceLocation recipeId) throws ReflectiveOperationException {
        if (recipeTypeId == null) {
            return null;
        }
        Object recipeType = optionalValue(invokeFirst(recipeManager, "getRecipeType", recipeTypeId));
        if (recipeType == null) {
            return null;
        }
        Object category = invokeFirst(recipeManager, "getRecipeCategory", recipeType);
        return findJeiRecipeMatchByCategory(recipeManager, category, recipeId);
    }

    private RecipeMatch findJeiRecipeMatchByCategory(Object recipeManager, Object category, ResourceLocation recipeId) {
        if (category == null) {
            return null;
        }
        try {
            Object recipeType = RecipeViewerReflectionUtils.firstMethod(category.getClass(), "getRecipeType", 0).invoke(category);
            Object recipe = findJeiRecipe(recipeManager, recipeType, category, recipeId);
            return recipe == null ? null : new RecipeMatch(category, recipe);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return null;
        }
    }

    private boolean show(String target, String roleName) {
        if (!FluidIconCodec.isFluidIcon(target)) {
            return "OUTPUT".equals(roleName) ? RecipeViewerProvider.super.showRecipes(target) : RecipeViewerProvider.super.showUses(target);
        }
        Fluid fluid = FluidIconCodec.fluidFromIcon(target);
        return fluid != Fluids.EMPTY && showFluid(fluid, roleName);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean showFluid(Fluid fluid, String roleName) {
        try {
            Class<?> internalClass = Class.forName(INTERNAL);
            Object runtime = internalClass.getMethod("getJeiRuntime").invoke(null);
            if (runtime == null) {
                return false;
            }
            Object helpers = runtime.getClass().getMethod("getJeiHelpers").invoke(runtime);
            Object focusFactory = helpers.getClass().getMethod("getFocusFactory").invoke(helpers);
            Object fluidHelper = helpers.getClass().getMethod("getPlatformFluidHelper").invoke(helpers);
            Object fluidType = fluidHelper.getClass().getMethod("getFluidIngredientType").invoke(fluidHelper);
            long amount = longMethodOrDefault(fluidHelper, "bucketVolume", 1000L);
            Object fluidIngredient = firstCompatibleMethod(fluidHelper.getClass(), "create", 2).invoke(fluidHelper, fluid, amount);
            Class<?> roleClass = Class.forName(RECIPE_ROLE);
            Object role = Enum.valueOf((Class<Enum>) roleClass.asSubclass(Enum.class), roleName);
            Method createFocus = RecipeViewerReflectionUtils.firstMethod(focusFactory.getClass(), "createFocus", 3);
            Object focus = createFocus.invoke(focusFactory, role, fluidType, fluidIngredient);
            Object recipesGui = runtime.getClass().getMethod("getRecipesGui").invoke(runtime);
            recipesGui.getClass().getMethod("show", List.class).invoke(recipesGui, List.of(focus));
            return true;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return false;
        }
    }

    private Object findJeiRecipe(Object recipeManager, Object recipeType, Object category, ResourceLocation recipeId) throws ReflectiveOperationException {
        Object lookup = firstCompatibleMethod(recipeManager.getClass(), "createRecipeLookup", 1).invoke(recipeManager, recipeType);
        try {
            RecipeViewerReflectionUtils.firstMethod(lookup.getClass(), "includeHidden", 0).invoke(lookup);
        } catch (NoSuchMethodException ignored) {
        }
        Object value = lookup.getClass().getMethod("get").invoke(lookup);
        if (!(value instanceof Stream<?> stream)) {
            return null;
        }
        try (stream) {
            return stream
                    .filter(candidate -> Objects.equals(registryName(category, candidate), recipeId))
                    .findFirst()
                    .orElse(null);
        }
    }

    private Object createLayout(Object recipeManager, Object category, Object recipe, Object focusGroup) throws ReflectiveOperationException {
        Object previewBackground = recipePreviewBackground();
        if (previewBackground != null) {
            Object layout = optionalValue(RecipeViewerReflectionUtils.firstMethod(recipeManager.getClass(), "createRecipeLayoutDrawable", 5)
                    .invoke(recipeManager, category, recipe, focusGroup, previewBackground, 4));
            if (layout != null) {
                return layout;
            }
        }
        return optionalValue(RecipeViewerReflectionUtils.firstMethod(recipeManager.getClass(), "createRecipeLayoutDrawable", 3)
                .invoke(recipeManager, category, recipe, focusGroup));
    }

    private static Object recipePreviewBackground() {
        try {
            Object textures = Class.forName(INTERNAL).getMethod("getTextures").invoke(null);
            return textures.getClass().getMethod("getRecipePreviewBackground").invoke(textures);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return null;
        }
    }

    private static ResourceLocation registryName(Object category, Object recipe) {
        try {
            Object id = RecipeViewerReflectionUtils.firstMethod(category.getClass(), "getRegistryName", 1).invoke(category, recipe);
            return id instanceof ResourceLocation resourceLocation ? resourceLocation : null;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return null;
        }
    }

    private static Object emptyFocusGroup(Object runtime) throws ReflectiveOperationException {
        Object helpers = runtime.getClass().getMethod("getJeiHelpers").invoke(runtime);
        Object focusFactory = helpers.getClass().getMethod("getFocusFactory").invoke(helpers);
        return RecipeViewerReflectionUtils.firstMethod(focusFactory.getClass(), "createFocusGroup", 1).invoke(focusFactory, List.of());
    }

    private static ResourceLocation jeiRecipeTypeId(String vanillaTypeId) {
        ResourceLocation id = ResourceLocation.tryParse(vanillaTypeId == null ? "" : vanillaTypeId.trim());
        if (id == null || !"minecraft".equals(id.getNamespace())) {
            return id;
        }
        return switch (id.getPath()) {
            case "smelting" -> new ResourceLocation("minecraft", "furnace");
            case "campfire_cooking" -> new ResourceLocation("minecraft", "campfire");
            default -> id;
        };
    }

    private static Object optionalValue(Object value) {
        if (value instanceof Optional<?> optional) {
            return optional.orElse(null);
        }
        return value;
    }

    private static Object invokeFirst(Object owner, String name, Object... args) throws ReflectiveOperationException {
        Method method = firstCompatibleMethod(owner.getClass(), name, args.length);
        try {
            return method.invoke(owner, args);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw exception;
        }
    }

    private static long longMethodOrDefault(Object owner, String name, long fallback) {
        try {
            Object value = firstCompatibleMethod(owner.getClass(), name, 0).invoke(owner);
            return value instanceof Number number ? number.longValue() : fallback;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return fallback;
        }
    }

    private static Method firstCompatibleMethod(Class<?> owner, String name, int parameterCount) throws NoSuchMethodException {
        for (Method method : owner.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                return method;
            }
        }
        throw new NoSuchMethodException(owner.getName() + "#" + name + "/" + parameterCount);
    }

    private static void addFluidEntry(List<String> entries, Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return;
        }
        String icon = FluidIconCodec.iconFromFluid(fluid);
        if (!icon.isBlank() && !entries.contains(icon)) {
            entries.add(icon);
        }
    }

    private record RecipeMatch(Object category, Object recipe) {
    }
}
