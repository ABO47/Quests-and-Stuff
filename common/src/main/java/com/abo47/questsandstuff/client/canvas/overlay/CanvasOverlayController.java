package com.abo47.questsandstuff.client.canvas.overlay;

import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.canvas.contextmenu.CanvasContextMenuController;
import com.abo47.questsandstuff.client.canvas.contextmenu.CanvasCreateQuestModal;
import com.abo47.questsandstuff.client.canvas.contextmenu.CanvasContextMenuController.EdgeRef;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import java.util.List;
import java.util.Map;

public final class CanvasOverlayController {
    private CanvasOverlayController() {
    }

    public static void renderCanvasMetaPanels(
            CanvasViewport canvasViewport,
            TabletUiState state,
            List<QuestCardLayout> visibleCards,
            Map<String, QuestCardLayout> byQuestId,
            int contentX,
            int contentY,
            int contentW,
            int contentH
    ) {
        CanvasMinimapOverlay.render(canvasViewport, state, visibleCards, byQuestId);
        CanvasContextMenuController.renderCanvasContextMenu(canvasViewport, state);
        CanvasTextStyleMenu.render(canvasViewport, state, canvasViewport::refresh);
        EntityMotionEditor.renderMainCanvas(canvasViewport, state, canvasViewport.player(), canvasViewport::refresh);
        CanvasCreateQuestModal.render(canvasViewport, state);
    }

    public static boolean isContextMenuHit(TabletUiState state, int x, int y) {
        return CanvasContextMenuController.isContextMenuHit(state, x, y);
    }
    public static java.util.List<EdgeRef> selectedConnectedEdges(TabletUiState state, String group) {
        return CanvasContextMenuController.selectedConnectedEdges(state, group);
    }
}
