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
        JeiRecipePickOverlay.drawForScreen(screen, graphics, mouseX, mouseY);
        EmiRecipePickOverlay.drawForScreen(screen, graphics, mouseX, mouseY);
        ReiRecipePickOverlay.drawForScreen(screen, graphics, mouseX, mouseY);
    }

    public static boolean pickFromScreen(Object screen, double mouseX, double mouseY, int mouseButton) {
        return RecipeViewerSelectionBridge.hasPendingSelection()
                && (JeiRecipePickOverlay.pickFromScreen(screen, mouseX, mouseY, mouseButton)
                || EmiRecipePickOverlay.pickFromScreen(screen, mouseX, mouseY, mouseButton)
                || ReiRecipePickOverlay.pickFromScreen(screen, mouseX, mouseY, mouseButton));
    }
}
