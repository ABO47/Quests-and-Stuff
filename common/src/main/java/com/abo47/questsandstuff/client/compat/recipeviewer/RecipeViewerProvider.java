package com.abo47.questsandstuff.client.compat.recipeviewer;

import com.abo47.questsandstuff.client.canvas.recipe.CanvasRecipeCardRecipes.RecipeView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

interface RecipeViewerProvider {
    String name();

    boolean isAvailable();

    boolean showRecipes(ItemStack stack);

    boolean showUses(ItemStack stack);

    default boolean supportsNativeRecipeSelection() {
        return false;
    }

    default boolean renderRecipeSnapshot(GuiGraphics graphics, RecipeView recipe, int width, int height, int pivotX, int pivotY) {
        return false;
    }

    default boolean matchesRecipeKey(int keyCode, int scanCode) {
        return false;
    }

    default boolean matchesUsesKey(int keyCode, int scanCode) {
        return false;
    }
}
