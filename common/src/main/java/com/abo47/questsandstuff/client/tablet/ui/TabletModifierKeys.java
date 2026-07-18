package com.abo47.questsandstuff.client.tablet.ui;

import net.minecraft.client.Minecraft;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

public final class TabletModifierKeys {
    private TabletModifierKeys() {
    }

    public static boolean shiftDown() {
        return Minecraft.getInstance() != null && Widget.isShiftDown();
    }

    public static boolean ctrlDown() {
        return Minecraft.getInstance() != null && Widget.isCtrlDown();
    }

    public static boolean shiftOrCtrlDown() {
        return shiftDown() || ctrlDown();
    }
}
