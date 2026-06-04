package com.abo47.questsandstuff.client.compat.recipeviewer;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.recipe.CanvasRecipeCardRecipes.RecipeView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class RecipeViewerIntegrations {
    private static final List<RecipeViewerProvider> PROVIDERS = List.of(
            new JeiRecipeViewerProvider(),
            new EmiRecipeViewerProvider(),
            new ReiRecipeViewerProvider()
    );

    private RecipeViewerIntegrations() {
    }

    public static boolean hasAvailableViewer() {
        return activeProvider() != null;
    }

    public static boolean showRecipesForSelection(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        for (RecipeViewerProvider provider : PROVIDERS) {
            if (provider.isAvailable() && provider.supportsNativeRecipeSelection()) {
                return showWithProvider(provider, stack, true);
            }
        }
        return false;
    }

    public static boolean handleKeybind(ItemStack stack, int keyCode, int scanCode) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        for (RecipeViewerProvider provider : PROVIDERS) {
            if (!provider.isAvailable()) {
                continue;
            }
            if (provider.matchesRecipeKey(keyCode, scanCode)) {
                return showWithProvider(provider, stack, true);
            }
            if (provider.matchesUsesKey(keyCode, scanCode)) {
                return showWithProvider(provider, stack, false);
            }
        }
        return false;
    }

    public static boolean renderRecipeSnapshot(GuiGraphics graphics, RecipeView recipe, int width, int height, int pivotX, int pivotY) {
        if (recipe == null || recipe.id() == null || recipe.id().isBlank()) {
            return false;
        }
        RecipeViewerProvider first = activeProvider();
        if (first != null && first.renderRecipeSnapshot(graphics, recipe, width, height, pivotX, pivotY)) {
            return true;
        }
        for (RecipeViewerProvider provider : PROVIDERS) {
            if (provider == first || !provider.isAvailable()) {
                continue;
            }
            if (provider.renderRecipeSnapshot(graphics, recipe, width, height, pivotX, pivotY)) {
                return true;
            }
        }
        return false;
    }

    private static boolean showWithProvider(RecipeViewerProvider provider, ItemStack stack, boolean recipes) {
        boolean opened = recipes ? provider.showRecipes(stack.copy()) : provider.showUses(stack.copy());
        if (opened) {
            QuestsAndStuffMod.debugLog("[QnS:Compat] opened {} in {}", recipes ? "recipes" : "uses", provider.name());
        }
        return opened;
    }

    private static RecipeViewerProvider activeProvider() {
        for (RecipeViewerProvider provider : PROVIDERS) {
            if (provider.isAvailable()) {
                return provider;
            }
        }
        return null;
    }
}
