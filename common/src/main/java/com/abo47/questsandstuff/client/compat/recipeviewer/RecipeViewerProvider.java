package com.abo47.questsandstuff.client.compat.recipeviewer;

import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes.RecipeView;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardAsset;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.List;

interface RecipeViewerProvider {
    String name();

    RecipeViewerProviderCapabilities capabilities();

    default boolean isAvailable() {
        return supports(RecipeViewerCapability.AVAILABLE);
    }

    default boolean supports(RecipeViewerCapability capability) {
        return capabilities().supports(capability);
    }

    boolean showRecipes(ItemStack stack);

    boolean showUses(ItemStack stack);

    default boolean showRecipes(String target) {
        return showRecipes(stackForTarget(target));
    }

    default boolean showUses(String target) {
        return showUses(stackForTarget(target));
    }

    default boolean supportsNativeRecipeSelection() {
        return supports(RecipeViewerCapability.NATIVE_SELECTION);
    }

    default boolean renderRecipeSnapshot(GuiGraphics graphics, RecipeView recipe, int width, int height, int pivotX, int pivotY) {
        return false;
    }

    default List<String> fluidEntries() {
        return List.of();
    }

    default boolean matchesRecipeKey(int keyCode, int scanCode) {
        return false;
    }

    default boolean matchesUsesKey(int keyCode, int scanCode) {
        return false;
    }

    private static ItemStack stackForTarget(String target) {
        return CanvasRecipeCardAsset.outputStack(CanvasRecipeCardAsset.assetForPick(target));
    }
}
