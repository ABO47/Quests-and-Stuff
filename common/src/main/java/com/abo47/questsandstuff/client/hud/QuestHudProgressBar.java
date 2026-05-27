package com.abo47.questsandstuff.client.hud;

import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import net.minecraft.client.gui.GuiGraphics;

final class QuestHudProgressBar {
    private QuestHudProgressBar() {
    }

    static void draw(GuiGraphics graphics, int x, int y, int width, int height, float progress, int fillColor, int alpha) {
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        int fillW = Math.round((w - 2) * Math.max(0.0f, Math.min(1.0f, progress)));
        graphics.fill(x, y, x + w, y + h, TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, Math.min(178, safeAlpha)));
        graphics.renderOutline(x, y, w, h, TabletUiFactory.withAlpha(ModColors.BORDER_BASE, Math.min(130, safeAlpha)));
        if (fillW > 0) {
            graphics.fill(x + 1, y + 1, x + 1 + Math.max(1, fillW), y + h - 1, TabletUiFactory.withAlpha(fillColor, Math.min(180, safeAlpha)));
        }
    }
}
