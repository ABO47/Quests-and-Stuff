package com.abo47.questsandstuff.client.tablet.tools;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

final class ToolMenuRows {
    private final WidgetGroup menu;
    private final int x;
    private final int slot;
    private final int gap;
    private final int border;
    private int y;

    private ToolMenuRows(WidgetGroup menu, int x, int y, int slot, int gap, int border) {
        this.menu = menu;
        this.x = x;
        this.y = y;
        this.slot = slot;
        this.gap = gap;
        this.border = border;
    }

    static ToolMenuRows at(WidgetGroup menu, int x, int y, int slot, int gap, int border) {
        return new ToolMenuRows(menu, x, y, slot, gap, border);
    }

    int y() {
        return y;
    }

    void toggle(String iconName, int accentColor, boolean active, Component[] tooltip, Runnable action) {
        TabletToolButtons.addToggle(menu, x, y, slot, border, iconName, accentColor, active, tooltip, action);
        advance();
    }

    void action(String iconName, int accentColor, Component[] tooltip, Runnable action) {
        TabletToolButtons.addAction(menu, x, y, slot, border, iconName, accentColor, tooltip, action);
        advance();
    }

    void opacity(String iconName, Component[] tooltip, Consumer<Boolean> action) {
        TabletToolButtons.addOpacity(menu, x, y, slot, border, iconName, tooltip, action);
        advance();
    }

    void advancePastCustomRow() {
        advance();
    }

    private void advance() {
        y += slot + gap;
    }
}
