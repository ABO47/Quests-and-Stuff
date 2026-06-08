package com.abo47.questsandstuff.client.tablet.input;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import net.minecraft.client.Minecraft;

public final class TabletModifierKeys {
    private TabletModifierKeys() {
    }

    public static boolean shiftDown() {
        return Minecraft.getInstance() != null && Widget.isShiftDown();
    }
}
