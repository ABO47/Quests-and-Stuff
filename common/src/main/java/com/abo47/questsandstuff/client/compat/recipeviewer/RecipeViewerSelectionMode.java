package com.abo47.questsandstuff.client.compat.recipeviewer;

import com.abo47.questsandstuff.client.tablet.icons.ItemStackIconCodec;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardAsset;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes.RecipeView;
import net.minecraft.world.item.ItemStack;

enum RecipeViewerSelectionMode {
    OUTPUT {
        @Override
        boolean matches(String target, RecipeView recipe) {
            return CanvasRecipeCardAsset.matchesOutput(target, recipe.output());
        }

        @Override
        String assetTarget(String target, RecipeView recipe) {
            return target;
        }

        @Override
        String logName() {
            return "recipe";
        }

        @Override
        String visibleFallbackTarget(String target, String visibleOutputTarget) {
            String normalized = CanvasRecipeCardAsset.target(CanvasRecipeCardAsset.assetForPick(target));
            return normalized.isBlank() ? null : normalized;
        }
    },
    INPUT {
        @Override
        boolean matches(String target, RecipeView recipe) {
            return CanvasRecipeCardRecipes.recipeUsesTarget(target, recipe);
        }

        @Override
        String assetTarget(String target, RecipeView recipe) {
            return outputTarget(recipe);
        }

        @Override
        String logName() {
            return "uses";
        }

        @Override
        String visibleFallbackTarget(String target, String visibleOutputTarget) {
            String normalized = CanvasRecipeCardAsset.target(CanvasRecipeCardAsset.assetForPick(visibleOutputTarget));
            return normalized.isBlank() ? null : normalized;
        }
    };

    abstract boolean matches(String target, RecipeView recipe);

    abstract String assetTarget(String target, RecipeView recipe);

    abstract String logName();

    abstract String visibleFallbackTarget(String target, String visibleOutputTarget);

    private static String outputTarget(RecipeView recipe) {
        if (recipe == null || recipe.output() == null || recipe.output().isEmpty()) {
            return "";
        }
        ItemStack output = recipe.output().copy();
        output.setCount(1);
        return ItemStackIconCodec.iconFromStack(output);
    }
}
