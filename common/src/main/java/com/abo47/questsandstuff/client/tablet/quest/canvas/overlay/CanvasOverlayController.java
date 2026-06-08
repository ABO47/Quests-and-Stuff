package com.abo47.questsandstuff.client.tablet.quest.canvas.overlay;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu.CanvasContextMenuController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu.CanvasContextMenuController.EdgeRef;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
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
    }

    public static boolean isContextMenuHit(TabletUiState state, int x, int y) {
        return CanvasContextMenuController.isContextMenuHit(state, x, y);
    }
    public static java.util.List<EdgeRef> selectedConnectedEdges(TabletUiState state, String group) {
        return CanvasContextMenuController.selectedConnectedEdges(state, group);
    }
}
