package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.ConnectionHit;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;
import java.util.Map;

final class CanvasViewportContextRouter {
    private CanvasViewportContextRouter() {
    }

    static void closeContextMenu(TabletUiState state) {
        ContextMenuController.close(state);
    }

    static void openContextMenu(
            TabletUiState state,
            Runnable refresher,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int localX,
            int localY,
            QuestCardLayout hit,
            CanvasImageLayer imageHit,
            CanvasTextLayer textHit,
            CanvasExclusiveChoice ecHit
    ) {
        ConnectionHit connectionHit = TabletUiFactory.hitTestEdge(state, cards, byQuestId, localX, localY);
        CanvasPoint anchor = CanvasGeometry.anchorForScreenVisualCenter(state, localX, localY, 1.0f);
        int logicalX = anchor.x;
        int logicalY = anchor.y;
        int pointerLogicalX = CanvasGeometry.screenToNearestLogicalX(state, localX);
        int pointerLogicalY = CanvasGeometry.screenToNearestLogicalY(state, localY);
        ContextMenuController.openCanvas(state, localX, localY, logicalX, logicalY, pointerLogicalX, pointerLogicalY);
        EntityMotionEditor.close(state);
        if (CanvasSelectionActions.totalCanvasSelectionCount(state) > 1 && CanvasRenderer.isSelectionBoundsHit(state, localX, localY)) {
            ContextMenuController.targetSelection(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=selection count={}", CanvasSelectionActions.totalCanvasSelectionCount(state));
        } else if (connectionHit != null && connectionAboveHits(state, connectionHit, hit, imageHit, textHit, ecHit)) {
            ContextMenuController.targetConnection(state, connectionHit.sourceQuestId(), connectionHit.targetQuestId());
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=connection source={} target={}", state.contextMenu.contextConnectionSource, state.contextMenu.contextConnectionTarget);
        } else if (ecHit != null) {
            ContextMenuController.targetExclusiveChoice(state, ecHit.id());
            state.canvas.canvasSelection.setPrimaryEcId(ecHit.id());
            state.canvas.canvasSelection.ecIds().clear();
            state.canvas.canvasSelection.ecIds().add(ecHit.id());
            state.canvas.canvasSelection.setPrimaryImageId("");
            state.canvas.canvasSelection.imageIds().clear();
            state.canvas.canvasSelection.setPrimaryTextId("");
            state.canvas.canvasSelection.textIds().clear();
            state.canvas.canvasSelection.questIds().clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=exclusive_choice id={}", ecHit.id());
        } else if (textHit != null) {
            ContextMenuController.targetText(state, textHit.id());
            state.canvas.canvasSelection.setPrimaryTextId(textHit.id());
            state.canvas.canvasSelection.textIds().clear();
            state.canvas.canvasSelection.textIds().add(textHit.id());
            state.canvas.canvasSelection.setPrimaryImageId("");
            state.canvas.canvasSelection.imageIds().clear();
            state.canvas.canvasSelection.setPrimaryEcId("");
            state.canvas.canvasSelection.ecIds().clear();
            state.canvas.canvasSelection.questIds().clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=text id={}", textHit.id());
        } else if (imageHit != null) {
            ContextMenuController.targetImage(state, imageHit.id());
            state.canvas.canvasSelection.setPrimaryImageId(imageHit.id());
            state.canvas.canvasSelection.imageIds().clear();
            state.canvas.canvasSelection.imageIds().add(imageHit.id());
            state.canvas.canvasSelection.setPrimaryTextId("");
            state.canvas.canvasSelection.textIds().clear();
            state.canvas.canvasSelection.setPrimaryEcId("");
            state.canvas.canvasSelection.ecIds().clear();
            state.canvas.canvasSelection.questIds().clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=image id={}", imageHit.id());
        } else if (hit != null) {
            ContextMenuController.targetQuest(state, hit.questId());
            if (!state.canvas.canvasSelection.questIds().contains(hit.questId())) {
                state.canvas.canvasSelection.questIds().clear();
                state.canvas.canvasSelection.questIds().add(hit.questId());
            }
            state.canvas.canvasSelection.setPrimaryEcId("");
            state.canvas.canvasSelection.ecIds().clear();
            state.canvas.canvasSelection.setPrimaryImageId("");
            state.canvas.canvasSelection.imageIds().clear();
            state.canvas.canvasSelection.setPrimaryTextId("");
            state.canvas.canvasSelection.textIds().clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=quest quest={}", state.contextMenu.contextQuestId);
        } else if (connectionHit != null) {
            ContextMenuController.targetConnection(state, connectionHit.sourceQuestId(), connectionHit.targetQuestId());
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=connection source={} target={}", state.contextMenu.contextConnectionSource, state.contextMenu.contextConnectionTarget);
        } else {
            ContextMenuController.targetCanvas(state);
            CanvasSelectionActions.clearCanvasSelection(state);
            state.canvas.connectSourceQuestId = "";
            state.canvas.connectSourceQuestIds.clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=canvas logicalX={} logicalY={}", logicalX, logicalY);
        }
        refresher.run();
    }

    private static boolean connectionAboveHits(TabletUiState state, ConnectionHit connectionHit, QuestCardLayout questHit, CanvasImageLayer imageHit, CanvasTextLayer textHit, CanvasExclusiveChoice ecHit) {
        if (state == null || connectionHit == null) {
            return false;
        }
        String group = TabletStateQueries.selectedChapterName(state);
        List<String> order = state.canvas.canvasLayerOrderByChapter.getOrDefault(group, List.of());
        String connectionLayerKey = CanvasLayerOrdering.connectionKey(CanvasRenderer.connectionKey(connectionHit.sourceQuestId(), connectionHit.targetQuestId()));
        int connectionIndex = order.indexOf(connectionLayerKey);
        if (connectionIndex < 0) {
            return false;
        }
        return above(order, connectionIndex, questHit == null ? "" : CanvasLayerOrdering.questKey(questHit.questId()))
                && above(order, connectionIndex, imageHit == null ? "" : CanvasLayerOrdering.imageKey(imageHit.id()))
                && above(order, connectionIndex, textHit == null ? "" : CanvasLayerOrdering.textKey(textHit.id()))
                && above(order, connectionIndex, ecHit == null ? "" : CanvasLayerOrdering.exclusiveChoiceKey(ecHit.id()));
    }

    private static boolean above(List<String> order, int connectionIndex, String otherKey) {
        if (otherKey == null || otherKey.isBlank()) {
            return true;
        }
        int otherIndex = order.indexOf(otherKey);
            return otherIndex < 0 || connectionIndex > otherIndex;
    }
}
