package com.abo47.questsandstuff.client.quest.hud;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawRectOutline;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import net.minecraft.client.gui.GuiGraphics;

final class QuestHudProgressBar {
    private QuestHudProgressBar() {
    }

    static void draw(GuiGraphics graphics, int x, int y, int width, int height, float progress, int fillColor, int alpha) {
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        if (safeAlpha <= 0) {
            return;
        }
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        float safeProgress = Math.max(0.0f, Math.min(1.0f, progress));
        SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, Math.min(178, safeAlpha))).draw(graphics, 0, 0, x, y, w, h);
        drawRectOutline(graphics, x, y, w, h, withAlpha(TabletColors.BORDER_BASE, Math.min(130, safeAlpha)));
        ProgressTexture texture = new ProgressTexture(
                IGuiTexture.EMPTY,
                SurfaceFactory.fill(withAlpha(fillColor, Math.min(180, safeAlpha)))
        ).setFillDirection(ProgressTexture.FillDirection.LEFT_TO_RIGHT);
        texture.setProgress(safeProgress);
        texture.draw(graphics, 0, 0, x + 1, y + 1, Math.max(1, w - 2), Math.max(1, h - 2));
    }
}
