package com.abo47.questsandstuff.client.tablet.theme.skin;

import java.awt.Rectangle;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

public final class SkinEditRenderer {
    private static final int DIM_COLOR = TabletColors.DIM_OVERLAY;
    private static final ColorRectTexture DIM_BG = new ColorRectTexture(DIM_COLOR);
    private static final int GLOW_COLOR = TabletColors.INTERACTIVE;
    private static final int SELECTED_GLOW_COLOR = TabletColors.SUCCESS;

    private SkinEditRenderer() {
    }

    public static void draw(GuiGraphics graphics, TabletRootWidget root, TabletUiState state, int mouseX, int mouseY) {
        if (ModalStateQueries.anyOpen(state)) return;
        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        DIM_BG.draw(graphics, 0, 0, 0, 0, screenW, screenH);

        String selectedKey = state.root.skinEditSelectedTarget;
        if (!selectedKey.isBlank()) {
            drawGlowForKey(graphics, root, state, selectedKey, SELECTED_GLOW_COLOR, mouseX, mouseY);
        } else {
            String hoverKey = hoverKeyAt(root, state, mouseX, mouseY);
            if (hoverKey != null) {
                drawGlowForKey(graphics, root, state, hoverKey, GLOW_COLOR, mouseX, mouseY);
            }
        }

        if (root.isContextMenuOpen()) {
            root.getContextMenuRoot().drawInBackground(graphics, mouseX, mouseY, mc.getFrameTime());
        }

        String tooltipKey = !selectedKey.isBlank() ? selectedKey : hoverKeyAt(root, state, mouseX, mouseY);
        if (tooltipKey != null) {
            int dw = 0, dh = 0;
            if ("quests_minimap_body".equals(tooltipKey)) {
                dw = state.canvas.minimapPanelW + 1;
                dh = state.canvas.minimapPanelH + 1;
            } else if ("quests_minimap_toggle".equals(tooltipKey)) {
                dw = state.canvas.minimapToggleW + 1;
                dh = state.canvas.minimapToggleH + 1;
            } else {
                Widget tw = SkinEditTargetResolver.widgetForKey(root, tooltipKey);
                if (tw != null) {
                    dw = tw.getSizeWidth() + 1;
                    dh = tw.getSizeHeight() + 1;
                }
            }
            if (dw > 0 && dh > 0) {
                graphics.renderTooltip(mc.font, Component.literal(dw + " x " + dh), mouseX, mouseY);
            }
        }
    }

    private static String hoverKeyAt(TabletRootWidget root, TabletUiState state, int mouseX, int mouseY) {
        if (SkinEditManager.isMinimapHit(state, root, mouseX, mouseY)) {
            return SkinEditManager.minimapHitKey(state, root, mouseX, mouseY);
        }
        return SkinEditTargetResolver.findTargetKeyAt(root, mouseX, mouseY);
    }

    private static void drawGlowForKey(GuiGraphics graphics, WidgetGroup root, TabletUiState state, String key, int glowColor, int mouseX, int mouseY) {
        Widget widget = SkinEditTargetResolver.widgetForKey(root, key);
        if (widget != null) {
            drawGlow(graphics, root, widget, glowColor, mouseX, mouseY);
            return;
        }
        int gx = 0, gy = 0, gw = 0, gh = 0;
        if ("quests_minimap_body".equals(key)) {
            CanvasViewport vp = root instanceof TabletRootWidget trw ? trw.getCanvasViewport() : null;
            if (vp != null) {
                gx = vp.getPositionX() + state.canvas.minimapPanelX;
                gy = vp.getPositionY() + state.canvas.minimapPanelY;
            }
            gw = state.canvas.minimapPanelW;
            gh = state.canvas.minimapPanelH;
        } else if ("quests_minimap_toggle".equals(key)) {
            CanvasViewport vp = root instanceof TabletRootWidget trw ? trw.getCanvasViewport() : null;
            if (vp != null) {
                gx = vp.getPositionX() + state.canvas.minimapToggleX;
                gy = vp.getPositionY() + state.canvas.minimapToggleY;
            }
            gw = state.canvas.minimapToggleW;
            gh = state.canvas.minimapToggleH;
        }
        if (gw > 0 && gh > 0) {
            GlowShaderHelper.drawGlow(graphics, mouseX, mouseY, gx, gy, gw, gh);
        }
    }

    private static void drawGlow(GuiGraphics graphics, WidgetGroup root, Widget widget, int glowColor, int mouseX, int mouseY) {
        List<Rectangle> ancestors = SkinEditTargetResolver.ancestorBounds(widget, root);
        List<Rectangle> nestedTargets = SkinEditTargetResolver.nestedTargetBounds(widget, root);
        GlowShaderHelper.drawGlowOccluded(graphics, mouseX, mouseY,
                widget.getPositionX(), widget.getPositionY(),
                widget.getSizeWidth(), widget.getSizeHeight(),
                glowColor, ancestors, nestedTargets);
    }
}
