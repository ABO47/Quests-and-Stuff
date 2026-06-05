package com.abo47.questsandstuff.client.tablet.quest.canvas.recipe;

import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes.RecipeView;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerIntegrations;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public final class CanvasRecipeCardRenderer {
    private CanvasRecipeCardRenderer() {
    }

    public static boolean render(GuiGraphics graphics, String asset, int width, int height, int pivotX, int pivotY) {
        String target = CanvasRecipeCardAsset.target(asset);
        if (target.isBlank() || width <= 0 || height <= 0) {
            return false;
        }
        List<RecipeView> recipes = CanvasRecipeCardRecipes.recipesForAsset(asset);
        if (recipes.isEmpty()) {
            return true;
        }
        RecipeView recipe = recipes.get(0);
        if (!recipe.id().isBlank()) {
            RecipeViewerIntegrations.renderRecipeSnapshot(graphics, recipe, width, height, pivotX, pivotY);
        }
        return true;
    }
}
