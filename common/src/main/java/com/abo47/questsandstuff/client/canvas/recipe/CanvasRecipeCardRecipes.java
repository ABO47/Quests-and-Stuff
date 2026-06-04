package com.abo47.questsandstuff.client.canvas.recipe;

import com.abo47.questsandstuff.client.tablet.text.DisplayNameFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CanvasRecipeCardRecipes {
    private static final Map<String, List<RecipeView>> CACHED_BY_TARGET = new HashMap<>();
    private static final Map<String, List<RecipeView>> CACHED_USES_BY_TARGET = new HashMap<>();
    private static final Map<String, RecipeView> CACHED_BY_ID = new HashMap<>();
    private static RecipeManager cachedManager;
    private static RegistryAccess cachedRegistryAccess;

    private CanvasRecipeCardRecipes() {
    }

    public static List<RecipeView> recipesForAsset(String asset) {
        String target = CanvasRecipeCardAsset.target(asset);
        if (target.isBlank()) {
            return List.of();
        }
        String recipeId = CanvasRecipeCardAsset.recipeId(asset);
        if (!recipeId.isBlank()) {
            String viewerTypeId = CanvasRecipeCardAsset.viewerTypeId(asset);
            RecipeView exact = recipeById(recipeId);
            if (exact != null && CanvasRecipeCardAsset.matchesOutput(target, exact.output())) {
                return List.of(withViewerType(exact, viewerTypeId));
            }
            if (!viewerTypeId.isBlank()) {
                RecipeView external = externalView(recipeId, viewerTypeId, CanvasRecipeCardAsset.outputStack(asset));
                if (external != null) {
                    return List.of(external);
                }
            }
        }
        return recipesForTarget(target);
    }

    public static List<RecipeView> recipesForTarget(String target) {
        String normalized = CanvasRecipeCardAsset.target(CanvasRecipeCardAsset.assetForPick(target));
        if (normalized.isBlank()) {
            return List.of();
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        RecipeManager manager = connection == null ? null : connection.getRecipeManager();
        RegistryAccess registryAccess = connection == null ? null : connection.registryAccess();
        if (manager == null || registryAccess == null) {
            return List.of();
        }
        synchronized (CanvasRecipeCardRecipes.class) {
            resetIfNeeded(manager, registryAccess);
            return CACHED_BY_TARGET.computeIfAbsent(normalized, ignored -> buildRecipes(normalized, manager, registryAccess));
        }
    }

    public static List<RecipeView> usesForTarget(String target) {
        String normalized = CanvasRecipeCardAsset.target(CanvasRecipeCardAsset.assetForPick(target));
        if (normalized.isBlank()) {
            return List.of();
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        RecipeManager manager = connection == null ? null : connection.getRecipeManager();
        RegistryAccess registryAccess = connection == null ? null : connection.registryAccess();
        if (manager == null || registryAccess == null) {
            return List.of();
        }
        synchronized (CanvasRecipeCardRecipes.class) {
            resetIfNeeded(manager, registryAccess);
            return CACHED_USES_BY_TARGET.computeIfAbsent(normalized, ignored -> buildUses(normalized, manager, registryAccess));
        }
    }

    public static boolean recipeUsesTarget(String target, RecipeView recipe) {
        String normalized = CanvasRecipeCardAsset.target(CanvasRecipeCardAsset.assetForPick(target));
        if (normalized.isBlank() || recipe == null || recipe.ingredients() == null) {
            return false;
        }
        for (Ingredient ingredient : recipe.ingredients()) {
            if (ingredientUsesTarget(normalized, ingredient)) {
                return true;
            }
        }
        return false;
    }

    public static RecipeView recipeById(String recipeId) {
        ResourceLocation id = ResourceLocation.tryParse(recipeId == null ? "" : recipeId.trim());
        if (id == null) {
            return null;
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        RecipeManager manager = connection == null ? null : connection.getRecipeManager();
        RegistryAccess registryAccess = connection == null ? null : connection.registryAccess();
        if (manager == null || registryAccess == null) {
            return null;
        }
        synchronized (CanvasRecipeCardRecipes.class) {
            resetIfNeeded(manager, registryAccess);
            return CACHED_BY_ID.computeIfAbsent(id.toString(), ignored -> buildRecipeById(id, manager, registryAccess));
        }
    }

    public static RecipeView emptyView(ItemStack output) {
        return new RecipeView(
                "",
                "",
                "Recipe",
                output == null ? ItemStack.EMPTY : output.copy(),
                List.of(),
                false,
                3,
                3,
                LayoutKind.GENERIC,
                new ItemStack(Items.CRAFTING_TABLE)
        );
    }

    private static RecipeView withViewerType(RecipeView recipe, String viewerTypeId) {
        ResourceLocation id = ResourceLocation.tryParse(viewerTypeId == null ? "" : viewerTypeId.trim());
        if (recipe == null || id == null || id.toString().equals(recipe.typeId())) {
            return recipe;
        }
        return new RecipeView(
                recipe.id(),
                id.toString(),
                DisplayNameFormatter.resourceLeaf(id.toString()),
                recipe.output(),
                recipe.ingredients(),
                recipe.shaped(),
                recipe.shapedWidth(),
                recipe.shapedHeight(),
                recipe.layoutKind(),
                recipe.stationIcon()
        );
    }

    private static RecipeView externalView(String recipeId, String viewerTypeId, ItemStack output) {
        ResourceLocation recipe = ResourceLocation.tryParse(recipeId == null ? "" : recipeId.trim());
        ResourceLocation type = ResourceLocation.tryParse(viewerTypeId == null ? "" : viewerTypeId.trim());
        if (recipe == null || type == null) {
            return null;
        }
        return new RecipeView(
                recipe.toString(),
                type.toString(),
                DisplayNameFormatter.resourceLeaf(type.toString()),
                output == null ? ItemStack.EMPTY : output.copy(),
                List.of(),
                false,
                1,
                1,
                LayoutKind.GENERIC,
                new ItemStack(Items.CRAFTING_TABLE)
        );
    }

    private static void resetIfNeeded(RecipeManager manager, RegistryAccess registryAccess) {
        if (manager == cachedManager && registryAccess == cachedRegistryAccess) {
            return;
        }
        cachedManager = manager;
        cachedRegistryAccess = registryAccess;
        CACHED_BY_TARGET.clear();
        CACHED_USES_BY_TARGET.clear();
        CACHED_BY_ID.clear();
    }

    private static List<RecipeView> buildRecipes(String target, RecipeManager manager, RegistryAccess registryAccess) {
        List<RecipeView> found = new ArrayList<>();
        for (Recipe<?> recipe : manager.getRecipes()) {
            ItemStack output = resultItem(recipe, registryAccess);
            if (!CanvasRecipeCardAsset.matchesOutput(target, output)) {
                continue;
            }
            found.add(of(recipe, output));
        }
        found.sort(Comparator.comparing(RecipeView::typeLabel, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(RecipeView::id));
        return List.copyOf(found);
    }

    private static List<RecipeView> buildUses(String target, RecipeManager manager, RegistryAccess registryAccess) {
        List<RecipeView> found = new ArrayList<>();
        for (Recipe<?> recipe : manager.getRecipes()) {
            RecipeView view = of(recipe, resultItem(recipe, registryAccess));
            if (recipeUsesTarget(target, view)) {
                found.add(view);
            }
        }
        found.sort(Comparator.comparing(RecipeView::typeLabel, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(RecipeView::id));
        return List.copyOf(found);
    }

    private static boolean ingredientUsesTarget(String target, Ingredient ingredient) {
        if (ingredient == null || ingredient.isEmpty()) {
            return false;
        }
        for (ItemStack stack : ingredient.getItems()) {
            if (CanvasRecipeCardAsset.matchesOutput(target, stack)) {
                return true;
            }
        }
        return false;
    }

    private static RecipeView buildRecipeById(ResourceLocation id, RecipeManager manager, RegistryAccess registryAccess) {
        return manager.byKey(id)
                .map(recipe -> of(recipe, resultItem(recipe, registryAccess)))
                .orElse(null);
    }

    private static ItemStack resultItem(Recipe<?> recipe, RegistryAccess registryAccess) {
        try {
            return recipe.getResultItem(registryAccess);
        } catch (RuntimeException ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static RecipeView of(Recipe<?> recipe, ItemStack output) {
        NonNullList<Ingredient> source = recipe.getIngredients();
        List<Ingredient> ingredients = new ArrayList<>(source);
        int width = recipe instanceof ShapedRecipe shapedRecipe ? shapedRecipe.getWidth() : 3;
        int height = recipe instanceof ShapedRecipe shapedRecipe ? shapedRecipe.getHeight() : 3;
        RecipeType<?> type = recipe.getType();
        ResourceLocation typeId = BuiltInRegistries.RECIPE_TYPE.getKey(type);
        return new RecipeView(
                recipe.getId().toString(),
                typeId == null ? "" : typeId.toString(),
                typeLabel(type, typeId),
                output == null ? ItemStack.EMPTY : output.copy(),
                ingredients,
                recipe instanceof ShapedRecipe,
                Math.max(1, width),
                Math.max(1, height),
                layoutKind(type),
                stationIcon(type)
        );
    }

    private static LayoutKind layoutKind(RecipeType<?> type) {
        if (type == RecipeType.SMELTING || type == RecipeType.BLASTING || type == RecipeType.SMOKING || type == RecipeType.CAMPFIRE_COOKING) {
            return LayoutKind.COOKING;
        }
        if (type == RecipeType.STONECUTTING) {
            return LayoutKind.STONECUTTING;
        }
        if (type == RecipeType.CRAFTING) {
            return LayoutKind.CRAFTING;
        }
        return LayoutKind.GENERIC;
    }

    private static ItemStack stationIcon(RecipeType<?> type) {
        if (type == RecipeType.BLASTING) {
            return new ItemStack(Items.BLAST_FURNACE);
        }
        if (type == RecipeType.SMOKING) {
            return new ItemStack(Items.SMOKER);
        }
        if (type == RecipeType.CAMPFIRE_COOKING) {
            return new ItemStack(Items.CAMPFIRE);
        }
        if (type == RecipeType.SMELTING) {
            return new ItemStack(Items.FURNACE);
        }
        if (type == RecipeType.STONECUTTING) {
            return new ItemStack(Items.STONECUTTER);
        }
        if (type == RecipeType.SMITHING) {
            return new ItemStack(Items.SMITHING_TABLE);
        }
        return new ItemStack(Items.CRAFTING_TABLE);
    }

    private static String typeLabel(RecipeType<?> type, ResourceLocation typeId) {
        if (type == RecipeType.BLASTING) {
            return "Blasting";
        }
        if (type == RecipeType.SMOKING) {
            return "Smoking";
        }
        if (type == RecipeType.CAMPFIRE_COOKING) {
            return "Campfire";
        }
        if (type == RecipeType.SMELTING) {
            return "Smelting";
        }
        if (type == RecipeType.STONECUTTING) {
            return "Stonecutting";
        }
        if (type == RecipeType.SMITHING) {
            return "Smithing";
        }
        if (type == RecipeType.CRAFTING) {
            return "Crafting";
        }
        return typeId == null ? "Recipe" : DisplayNameFormatter.resourceLeaf(typeId.toString());
    }

    public enum LayoutKind {
        CRAFTING,
        COOKING,
        STONECUTTING,
        GENERIC
    }

    public record RecipeView(
            String id,
            String typeId,
            String typeLabel,
            ItemStack output,
            List<Ingredient> ingredients,
            boolean shaped,
            int shapedWidth,
            int shapedHeight,
            LayoutKind layoutKind,
            ItemStack stationIcon
    ) {
    }
}
