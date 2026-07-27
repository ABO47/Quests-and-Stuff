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
            String hoverKey = SkinEditTargetResolver.findTargetKeyAt(root, mouseX, mouseY);
            if (hoverKey != null) {
                drawGlowForKey(graphics, root, state, hoverKey, GLOW_COLOR, mouseX, mouseY);
            }
        }

        if (root.isContextMenuOpen()) {
            root.getContextMenuRoot().drawInBackground(graphics, mouseX, mouseY, mc.getFrameTime());
        }

        String tooltipKey = !selectedKey.isBlank() ? selectedKey :
                SkinEditTargetResolver.findTargetKeyAt(root, mouseX, mouseY);
        if (tooltipKey != null) {
            Widget tw = SkinEditTargetResolver.widgetForKey(root, tooltipKey);
            if (tw != null) {
                int dw = tw.getSizeWidth() + 1;
                int dh = tw.getSizeHeight() + 1;
                graphics.renderTooltip(mc.font, Component.literal(dw + " x " + dh), mouseX, mouseY);
            }
        }
    }

    private static void drawGlowForKey(GuiGraphics graphics, WidgetGroup root, TabletUiState state, String key, int glowColor, int mouseX, int mouseY) {
        Widget widget = SkinEditTargetResolver.widgetForKey(root, key);
        if (widget != null) {
            drawGlow(graphics, root, widget, glowColor, mouseX, mouseY);
            return;
        }
        int gx = 0, gy = 0, gw = 0, gh = 0;
        if ("quests_minimap_body".equals(key)) {
            gx = state.canvas.minimapPanelX;
            gy = state.canvas.minimapPanelY;
            gw = state.canvas.minimapPanelW;
            gh = state.canvas.minimapPanelH;
        } else if ("quests_minimap_toggle".equals(key)) {
            gx = state.canvas.minimapToggleX;
            gy = state.canvas.minimapToggleY;
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
