package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.EdgeHit;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;
import java.util.Map;

final class CanvasViewportContextRouter {
    private CanvasViewportContextRouter() {
    }

    static void closeContextMenu(TabletUiState state) {
        ContextMenuState.close(state);
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
        EdgeHit edgeHit = TabletUiFactory.hitTestEdge(state, cards, byQuestId, localX, localY);
        CanvasPoint anchor = CanvasGeometry.anchorForScreenVisualCenter(state, localX, localY, 1.0f);
        int logicalX = anchor.x;
        int logicalY = anchor.y;
        int pointerLogicalX = CanvasGeometry.screenToNearestLogicalX(state, localX);
        int pointerLogicalY = CanvasGeometry.screenToNearestLogicalY(state, localY);
        ContextMenuState.openCanvas(state, localX, localY, logicalX, logicalY, pointerLogicalX, pointerLogicalY);
        EntityMotionEditor.close(state);
        if (CanvasSelectionActions.totalCanvasSelectionCount(state) > 1 && CanvasRenderer.isSelectionBoundsHit(state, localX, localY)) {
            ContextMenuState.targetSelection(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=selection count={}", CanvasSelectionActions.totalCanvasSelectionCount(state));
        } else if (edgeHit != null && edgeAboveHits(state, edgeHit, hit, imageHit, textHit, ecHit)) {
            ContextMenuState.targetEdge(state, edgeHit.sourceQuestId(), edgeHit.targetQuestId());
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=edge source={} target={}", state.contextMenu.contextEdgeSource, state.contextMenu.contextEdgeTarget);
        } else if (ecHit != null) {
            ContextMenuState.targetExclusiveChoice(state, ecHit.id());
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
            ContextMenuState.targetText(state, textHit.id());
            state.canvas.canvasSelection.setPrimaryTextId(textHit.id());
            state.canvas.canvasSelection.textIds().clear();
            state.canvas.canvasSelection.textIds().add(textHit.id());
            state.canvas.canvasSelection.setPrimaryImageId("");
            state.canvas.canvasSelection.imageIds().clear();
            state.canvas.canvasSelection.questIds().clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=text id={}", textHit.id());
        } else if (imageHit != null) {
            ContextMenuState.targetImage(state, imageHit.id());
            state.canvas.canvasSelection.setPrimaryImageId(imageHit.id());
            state.canvas.canvasSelection.imageIds().clear();
            state.canvas.canvasSelection.imageIds().add(imageHit.id());
            state.canvas.canvasSelection.setPrimaryTextId("");
            state.canvas.canvasSelection.textIds().clear();
            state.canvas.canvasSelection.questIds().clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=image id={}", imageHit.id());
        } else if (hit != null) {
            ContextMenuState.targetQuest(state, hit.questId());
            if (!state.canvas.canvasSelection.questIds().contains(hit.questId())) {
                state.canvas.canvasSelection.questIds().clear();
                state.canvas.canvasSelection.questIds().add(hit.questId());
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=quest quest={}", state.contextMenu.contextQuestId);
        } else if (edgeHit != null) {
            ContextMenuState.targetEdge(state, edgeHit.sourceQuestId(), edgeHit.targetQuestId());
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=edge source={} target={}", state.contextMenu.contextEdgeSource, state.contextMenu.contextEdgeTarget);
        } else {
            ContextMenuState.targetCanvas(state);
            CanvasSelectionActions.clearCanvasSelection(state);
            state.canvas.connectSourceQuestId = "";
            state.canvas.connectSourceQuestIds.clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=canvas logicalX={} logicalY={}", logicalX, logicalY);
        }
        refresher.run();
    }

    private static boolean edgeAboveHits(TabletUiState state, EdgeHit edgeHit, QuestCardLayout questHit, CanvasImageLayer imageHit, CanvasTextLayer textHit) {
        return edgeAboveHits(state, edgeHit, questHit, imageHit, textHit, null);
    }

    private static boolean edgeAboveHits(TabletUiState state, EdgeHit edgeHit, QuestCardLayout questHit, CanvasImageLayer imageHit, CanvasTextLayer textHit, CanvasExclusiveChoice ecHit) {
        if (state == null || edgeHit == null) {
            return false;
        }
        String group = TabletStateQueries.selectedGroupName(state);
        List<String> order = state.canvas.canvasLayerOrderByGroup.getOrDefault(group, List.of());
        String edgeKey = CanvasLayerOrdering.connectionKey(CanvasRenderer.edgeKey(edgeHit.sourceQuestId(), edgeHit.targetQuestId()));
        int edgeIndex = order.indexOf(edgeKey);
        if (edgeIndex < 0) {
            return false;
        }
        return above(order, edgeIndex, questHit == null ? "" : CanvasLayerOrdering.questKey(questHit.questId()))
                && above(order, edgeIndex, imageHit == null ? "" : CanvasLayerOrdering.imageKey(imageHit.id()))
                && above(order, edgeIndex, textHit == null ? "" : CanvasLayerOrdering.textKey(textHit.id()))
                && above(order, edgeIndex, ecHit == null ? "" : CanvasLayerOrdering.exclusiveChoiceKey(ecHit.id()));
    }

    private static boolean above(List<String> order, int edgeIndex, String otherKey) {
        if (otherKey == null || otherKey.isBlank()) {
            return true;
        }
        int otherIndex = order.indexOf(otherKey);
        return otherIndex < 0 || edgeIndex > otherIndex;
    }
}
