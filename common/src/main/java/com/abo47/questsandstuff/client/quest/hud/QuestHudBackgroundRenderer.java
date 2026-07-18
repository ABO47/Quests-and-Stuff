package com.abo47.questsandstuff.client.quest.hud;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;

import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawRectOutline;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

final class QuestHudBackgroundRenderer {
    private QuestHudBackgroundRenderer() {
    }

    static void draw(GuiGraphics graphics, QuestHudLayoutManager.Element element, int x, int y, int width, int height, boolean selected) {
        draw(graphics, element, x, y, width, height, selected, "");
    }

    static void draw(GuiGraphics graphics, QuestHudLayoutManager.Element element, int x, int y, int width, int height, boolean selected, String backgroundOverride) {
        draw(graphics, element, x, y, width, height, selected, backgroundOverride, 255);
    }

    static void draw(GuiGraphics graphics, QuestHudLayoutManager.Element element, int x, int y, int width, int height, boolean selected, String backgroundOverride, int alpha) {
        int opacity = QuestHudLayoutManager.opacityPercent(element);
        if (opacity <= 0 || alpha <= 0) {
            return;
        }
        float animationAlpha = Math.max(0, Math.min(255, alpha)) / 255.0f;
        float effectiveOpacity = opacity / 100.0f * animationAlpha;
        if (effectiveOpacity <= 0.0f) {
            return;
        }
        String override = normalizeBackground(backgroundOverride);
        String background = override.isBlank() ? normalizeBackground(QuestHudLayoutManager.background(element)) : override;
        if (!background.isBlank()) {
            IGuiTexture texture = TabletUiFactory.chapterBackgroundTexture(background);
            if (texture != null && effectiveOpacity > 0.0f) {
                resetTextureState(graphics);
                graphics.setColor(1.0f, 1.0f, 1.0f, effectiveOpacity);
                texture.draw(graphics, 0, 0, x, y, width, height);
                resetTextureState(graphics);
                SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, Math.round(44.0f * effectiveOpacity))).draw(graphics, 0, 0, x, y, width, height);
            }
        } else {
            int panelAlpha = Math.round(224.0f * effectiveOpacity);
            if (panelAlpha > 0) {
                SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_PANEL, panelAlpha)).draw(graphics, 0, 0, x, y, width, height);
            }
        }
        if (selected || QuestHudLayoutManager.showBorders(element)) {
            int borderAlpha = Math.round((selected ? 240.0f : 150.0f) * effectiveOpacity);
            if (borderAlpha > 0) {
                drawRectOutline(graphics, x, y, width, height, withAlpha(selected ? TabletColors.INTERACTIVE : TabletColors.BORDER_BASE, borderAlpha));
            }
        }
    }

    private static String normalizeBackground(String background) {
        if (background == null || background.isBlank()) {
            return "";
        }
        String value = background.trim();
        return "default".equals(value) ? "" : value;
    }

    private static void resetTextureState(GuiGraphics graphics) {
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
    }

    static void drawThumbnail(GuiGraphics graphics, String background, int x, int y, int width, int height) {
        if (background == null || background.isBlank()) {
            SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, 180)).draw(graphics, 0, 0, x, y, width, height);
            drawRectOutline(graphics, x, y, width, height, withAlpha(TabletColors.BORDER_BASE, 160));
            return;
        }
        IGuiTexture texture = TabletUiFactory.assetThumbnailTexture(background);
        if (texture == null) {
            texture = TabletUiFactory.chapterBackgroundTexture(background);
        }
        if (texture != null) {
            texture.draw(graphics, 0, 0, x, y, width, height);
        } else {
            SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, 180)).draw(graphics, 0, 0, x, y, width, height);
        }
        drawRectOutline(graphics, x, y, width, height, withAlpha(TabletColors.BORDER_BASE, 160));
    }
}
