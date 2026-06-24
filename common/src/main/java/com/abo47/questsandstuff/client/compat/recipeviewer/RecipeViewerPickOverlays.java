package com.abo47.questsandstuff.client.compat.recipeviewer;

import com.abo47.questsandstuff.client.compat.recipeviewer.emi.EmiRecipePickOverlay;
import com.abo47.questsandstuff.client.compat.recipeviewer.jei.JeiRecipePickOverlay;
import com.abo47.questsandstuff.client.compat.recipeviewer.rei.ReiRecipePickOverlay;
import net.minecraft.client.gui.GuiGraphics;

public final class RecipeViewerPickOverlays {
    private RecipeViewerPickOverlays() {
    }

    public static void drawForScreen(Object screen, GuiGraphics graphics, int mouseX, int mouseY) {
        if (!RecipeViewerSelectionBridge.hasPendingSelection()) {
            return;
        }
        if (RecipeViewerIntegrations.providerSupports("JEI", RecipeViewerCapability.VISIBLE_RECIPE_PICK)) {
            JeiRecipePickOverlay.drawForScreen(screen, graphics, mouseX, mouseY);
        }
        if (RecipeViewerIntegrations.providerSupports("EMI", RecipeViewerCapability.VISIBLE_RECIPE_PICK)) {
            EmiRecipePickOverlay.drawForScreen(screen, graphics, mouseX, mouseY);
        }
        if (RecipeViewerIntegrations.providerSupports("REI", RecipeViewerCapability.VISIBLE_RECIPE_PICK)) {
            ReiRecipePickOverlay.drawForScreen(screen, graphics, mouseX, mouseY);
        }
    }

    public static boolean pickFromScreen(Object screen, double mouseX, double mouseY, int mouseButton) {
        if (!RecipeViewerSelectionBridge.hasPendingSelection()) {
            return false;
        }
        if (RecipeViewerIntegrations.providerSupports("JEI", RecipeViewerCapability.VISIBLE_RECIPE_PICK)
                && JeiRecipePickOverlay.pickFromScreen(screen, mouseX, mouseY, mouseButton)) {
            return true;
        }
        if (RecipeViewerIntegrations.providerSupports("EMI", RecipeViewerCapability.VISIBLE_RECIPE_PICK)
                && EmiRecipePickOverlay.pickFromScreen(screen, mouseX, mouseY, mouseButton)) {
            return true;
        }
        return RecipeViewerIntegrations.providerSupports("REI", RecipeViewerCapability.VISIBLE_RECIPE_PICK)
                && ReiRecipePickOverlay.pickFromScreen(screen, mouseX, mouseY, mouseButton);
    }
}
