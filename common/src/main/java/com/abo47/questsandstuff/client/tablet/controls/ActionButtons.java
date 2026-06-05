package com.abo47.questsandstuff.client.tablet.controls;

import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ACTION_ICON_SIZE;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class ActionButtons {
    private ActionButtons() {
    }

    public static void iconAction(WidgetGroup parent, int x, int y, int width, String icon, String text, int color, Consumer<ClickData> callback) {
        iconAction(parent, x, y, width, 16, icon, text, color, null, callback);
    }

    public static void iconAction(WidgetGroup parent, int x, int y, int width, int height, String icon, String text, int color, Component[] tooltips, Consumer<ClickData> callback) {
        parent.addWidget(Surfaces.panel(x, y, width, height, withAlpha(ModColors.elevatedSurface(), 210), ModColors.subtleBorder()));
        int iconSize = Math.min(ACTION_ICON_SIZE, Math.max(8, height - 4));
        String safeText = text == null ? "" : text;
        int textWidth = Minecraft.getInstance().font.width(safeText);
        int textHeight = Minecraft.getInstance().font.lineHeight;
        int textGap = textWidth > 0 ? 4 : 0;
        int contentWidth = iconSize + textGap + textWidth;
        int contentX = x + Math.max(3, (width - contentWidth) / 2);
        int iconY = y + Math.max(0, (height - iconSize) / 2);
        int textY = y + Math.max(1, (height - textHeight) / 2);
        parent.addWidget(IconOnlyButton.icon(contentX, iconY, iconSize, icon, color));
        parent.addWidget(label(contentX + iconSize + textGap, textY, safeText, color));
        var hit = flatHitButton(x, y, width, height, callback);
        hit.setHoverTexture(Surfaces.controlHover(color));
        hit.setClickedTexture(Surfaces.controlPressed(color));
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
