package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.root.TabletRootHitTest;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.widget.TabletWidgetCoordinates;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

public final class CanvasInteractionGate {
    private CanvasInteractionGate() {
    }

    public static boolean hoverAllowed(TabletUiState state, Widget view, double mouseX, double mouseY) {
        if (state == null || view == null) {
            return true;
        }
        if (ContextMenuController.isOpen(state)) {
            return false;
        }
        return !TabletRootHitTest.isInsideChapterPanel(state, TabletWidgetCoordinates.rootX(view),
                TabletWidgetCoordinates.rootY(view), mouseX, mouseY);
    }
}
