package com.abo47.questsandstuff.client.tablet.ui.widget;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

public final class TabletWidgetCoordinates {
    private TabletWidgetCoordinates() {
    }

    public static int rootX(Widget widget) {
        return screenX(widget, 0);
    }

    public static int rootY(Widget widget) {
        return screenY(widget, 0);
    }

    public static int localX(Widget widget, int rootLocalX, double mouseX) {
        return (int) Math.round(mouseX - screenX(widget, rootLocalX));
    }

    public static int localY(Widget widget, int rootLocalY, double mouseY) {
        return (int) Math.round(mouseY - screenY(widget, rootLocalY));
    }

    public static int screenX(Widget widget, int rootLocalX) {
        ModularUI gui = widget == null ? null : widget.getGui();
        if (gui != null && gui.getScreenWidth() > 0) {
            return gui.getGuiLeft() + rootLocalX;
        }
        return widget == null ? rootLocalX : widget.getPositionX();
    }

    public static int screenY(Widget widget, int rootLocalY) {
        ModularUI gui = widget == null ? null : widget.getGui();
        if (gui != null && gui.getScreenHeight() > 0) {
            return gui.getGuiTop() + rootLocalY;
        }
        return widget == null ? rootLocalY : widget.getPositionY();
    }
}
