package com.abo47.questsandstuff.client.hud;

import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import net.minecraft.client.gui.GuiGraphics;

final class QuestHudBackgroundRenderer {
    private QuestHudBackgroundRenderer() {
    }

    static void draw(GuiGraphics graphics, QuestHudLayout.Element element, int x, int y, int width, int height, boolean selected) {
        draw(graphics, element, x, y, width, height, selected, "");
    }

    static void draw(GuiGraphics graphics, QuestHudLayout.Element element, int x, int y, int width, int height, boolean selected, String backgroundOverride) {
        int opacity = QuestHudLayout.opacityPercent(element);
        String background = backgroundOverride == null || backgroundOverride.isBlank() ? QuestHudLayout.background(element) : backgroundOverride.trim();
        if (!background.isBlank()) {
            IGuiTexture texture = TabletUiFactory.chapterBackgroundTexture(background);
            if (texture != null && opacity > 0) {
                float alpha = opacity / 100.0f;
                graphics.setColor(1.0f, 1.0f, 1.0f, alpha);
                texture.draw(graphics, 0, 0, x, y, width, height);
                graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                graphics.fill(x, y, x + width, y + height, TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, Math.round(44.0f * alpha)));
            }
        } else {
            int panelAlpha = Math.round(224.0f * opacity / 100.0f);
            if (panelAlpha > 0) {
                graphics.fill(x, y, x + width, y + height, TabletUiFactory.withAlpha(ModColors.SURFACE_PANEL, panelAlpha));
            }
        }
        int borderAlpha = selected ? 240 : Math.round(150.0f * opacity / 100.0f);
        if (borderAlpha > 0) {
            graphics.renderOutline(x, y, width, height, TabletUiFactory.withAlpha(selected ? ModColors.INTERACTIVE : ModColors.BORDER_BASE, borderAlpha));
        }
    }

    static void drawThumbnail(GuiGraphics graphics, String background, int x, int y, int width, int height) {
        if (background == null || background.isBlank()) {
            graphics.fill(x, y, x + width, y + height, TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, 180));
            graphics.renderOutline(x, y, width, height, TabletUiFactory.withAlpha(ModColors.BORDER_BASE, 160));
            return;
        }
        IGuiTexture texture = TabletUiFactory.assetThumbnailTexture(background);
        if (texture == null) {
            texture = TabletUiFactory.chapterBackgroundTexture(background);
        }
        if (texture != null) {
            texture.draw(graphics, 0, 0, x, y, width, height);
        } else {
            graphics.fill(x, y, x + width, y + height, TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, 180));
        }
        graphics.renderOutline(x, y, width, height, TabletUiFactory.withAlpha(ModColors.BORDER_BASE, 160));
    }
}
