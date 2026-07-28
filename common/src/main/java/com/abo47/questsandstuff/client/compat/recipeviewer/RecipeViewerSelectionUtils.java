package com.abo47.questsandstuff.client.compat.recipeviewer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes.RecipeView;

public final class RecipeViewerSelectionUtils {
    private RecipeViewerSelectionUtils() {
    }

    public static Set<String> recipeIds(List<RecipeView> recipes) {
        Set<String> ids = new LinkedHashSet<>();
        if (recipes == null) {
            return ids;
        }
        for (RecipeView recipe : recipes) {
            String id = normalizeRecipeId(recipe == null ? "" : recipe.id());
            if (!id.isBlank()) {
                ids.add(id);
            }
        }
        return ids;
    }

    public static boolean canPickRecipe(Selection selection, String recipeId, RecipeView recipe) {
        String normalized = normalizeRecipeId(recipeId);
        if (selection == null || normalized.isBlank() || recipe == null) {
            return false;
        }
        if (selection.recipeIds().contains(normalized)) {
            return true;
        }
        return selection.mode().matches(selection.target(), recipe);
    }

    public static boolean canPickVisibleRecipe(Selection selection, String recipeId, RecipeView recipe, String visibleOutputTarget) {
        String normalized = normalizeRecipeId(recipeId);
        if (selection == null || normalized.isBlank()) {
            return false;
        }
        if (recipe != null && canPickRecipe(selection, normalized, recipe)) {
            return true;
        }
        return selection.mode().visibleFallbackTarget(selection.target(), visibleOutputTarget) != null;
    }

    public static String pickTarget(Selection selection, String recipeId, RecipeView recipe, boolean allowVisibleFallback, String visibleOutputTarget) {
        String normalized = normalizeRecipeId(recipeId);
        if (selection == null || normalized.isBlank()) {
            return "";
        }
        boolean knownRecipe = recipe != null && canPickRecipe(selection, normalized, recipe);
        if (knownRecipe) {
            return selection.mode().assetTarget(selection.target(), recipe);
        }
        String fallbackTarget = selection.mode().visibleFallbackTarget(selection.target(), visibleOutputTarget);
        return allowVisibleFallback && fallbackTarget != null ? fallbackTarget : "";
    }

    public static String normalizeRecipeId(String recipeId) {
        ResourceLocation id = ResourceLocation.tryParse(recipeId == null ? "" : recipeId.trim());
        return id == null ? "" : id.toString();
    }

    public record Selection(String target, Set<String> recipeIds, RecipeViewerSelectionMode mode) {
        public Selection {
            target = target == null ? "" : target;
            recipeIds = recipeIds == null ? Set.of() : Set.copyOf(recipeIds);
            mode = mode == null ? RecipeViewerSelectionMode.OUTPUT : mode;
        }
    }
}
