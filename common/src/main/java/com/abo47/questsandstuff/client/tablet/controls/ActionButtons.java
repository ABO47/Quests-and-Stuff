package com.abo47.questsandstuff.client.tablet.controls;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ACTION_ICON_SIZE;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

public final class ActionButtons {
    private ActionButtons() {
    }

    public static void iconAction(WidgetGroup parent, int x, int y, int width, String icon, String text, int color, Consumer<ClickData> callback) {
        iconAction(parent, x, y, width, GRID_16, icon, text, color, null, callback);
    }

    public static void iconAction(WidgetGroup parent, int x, int y, int width, int height, String icon, String text, int color, Component[] tooltips, Consumer<ClickData> callback) {
        parent.addWidget(SurfaceFactory.panel(x, y, width, height, withAlpha(TabletColors.elevatedSurface(), 210), TabletColors.subtleBorder()));
        int iconSize = Math.min(ACTION_ICON_SIZE, Math.max(8, height - 4));
        String safeText = text == null ? "" : text;
        int textWidth = Minecraft.getInstance().font.width(safeText);
        int textHeight = Minecraft.getInstance().font.lineHeight;
        int textGap = textWidth > 0 ? 4 : 0;
        int contentWidth = iconSize + textGap + textWidth;
        int contentX = x + Math.max(3, (width - contentWidth) / 2);
        int centerY = y + height / 2;
        int iconY = centerY - iconSize / 2;
        int textY = centerY - textHeight / 2;
        parent.addWidget(IconOnlyButton.icon(contentX, iconY, iconSize, icon, color));
        parent.addWidget(label(contentX + iconSize + textGap, textY, safeText, color));
        var hit = flatHitButton(x, y, width, height, callback);
        hit.setHoverTexture(GlowShaderHelper.hoverGlow(color));
        hit.setClickedTexture(SurfaceFactory.controlPressed(color));
        if (tooltips != null) {
            hit.setHoverTooltips(tooltips);
        }
        parent.addWidget(hit);
    }

    public static void iconRow(WidgetGroup parent, int x, int y, int height, int gap, IconAction... actions) {
        int cursorX = x;
        for (IconAction action : actions) {
            iconAction(parent, cursorX, y, action.width(), height, action.icon(), action.text(), action.color(), action.tooltips(), action.callback());
            cursorX += action.width() + gap;
        }
    }

    public record IconAction(String icon, String text, int width, int color, Component[] tooltips, Consumer<ClickData> callback) {
        public IconAction(String icon, String text, int width, int color, Consumer<ClickData> callback) {
            this(icon, text, width, color, null, callback);
        }
    }
}
