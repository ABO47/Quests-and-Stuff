package com.abo47.questsandstuff.client.tablet.quest.canvas.overlay;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu.CanvasContextMenuController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu.CanvasContextMenuController.ConnectionRef;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class CanvasOverlayController {
    private CanvasOverlayController() {
    }

    public static void renderCanvasMetaPanels(
            CanvasViewport canvasViewport,
            TabletUiState state,
            int contentX,
            int contentY,
            int contentW,
            int contentH
    ) {
        CanvasMinimapOverlay.render(canvasViewport, state);
        CanvasContextMenuController.renderCanvasContextMenu(canvasViewport, state);
        CanvasTextStyleMenu.render(canvasViewport, state, canvasViewport::refresh);
        EntityMotionEditor.renderMainCanvas(canvasViewport, state, canvasViewport.player(), canvasViewport::refresh);
    }

    public static boolean isContextMenuHit(TabletUiState state, int x, int y) {
        return CanvasContextMenuController.isContextMenuHit(state, x, y);
    }
    public static java.util.List<ConnectionRef> selectedConnections(TabletUiState state, String chapter) {
        return CanvasContextMenuController.selectedConnections(state, chapter);
    }
}
