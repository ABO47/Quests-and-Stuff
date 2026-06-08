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
            CanvasTextLayer textHit
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
        } else if (edgeHit != null && edgeAboveHits(state, edgeHit, hit, imageHit, textHit)) {
            ContextMenuState.targetEdge(state, edgeHit.sourceQuestId(), edgeHit.targetQuestId());
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=edge source={} target={}", state.contextEdgeSource, state.contextEdgeTarget);
        } else if (textHit != null) {
            ContextMenuState.targetText(state, textHit.id());
            state.canvasSelection.setPrimaryTextId(textHit.id());
            state.canvasSelection.textIds().clear();
            state.canvasSelection.textIds().add(textHit.id());
            state.canvasSelection.setPrimaryImageId("");
            state.canvasSelection.imageIds().clear();
            state.canvasSelection.questIds().clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=text id={}", textHit.id());
        } else if (imageHit != null) {
            ContextMenuState.targetImage(state, imageHit.id());
            state.canvasSelection.setPrimaryImageId(imageHit.id());
            state.canvasSelection.imageIds().clear();
            state.canvasSelection.imageIds().add(imageHit.id());
            state.canvasSelection.setPrimaryTextId("");
            state.canvasSelection.textIds().clear();
            state.canvasSelection.questIds().clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=image id={}", imageHit.id());
        } else if (hit != null) {
            ContextMenuState.targetQuest(state, hit.questId());
            if (!state.canvasSelection.questIds().contains(hit.questId())) {
                state.canvasSelection.questIds().clear();
                state.canvasSelection.questIds().add(hit.questId());
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=quest quest={}", state.contextQuestId);
        } else if (edgeHit != null) {
            ContextMenuState.targetEdge(state, edgeHit.sourceQuestId(), edgeHit.targetQuestId());
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=edge source={} target={}", state.contextEdgeSource, state.contextEdgeTarget);
        } else {
            ContextMenuState.targetCanvas(state);
            CanvasSelectionActions.clearCanvasSelection(state);
            state.connectSourceQuestId = "";
            state.connectSourceQuestIds.clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=canvas logicalX={} logicalY={}", logicalX, logicalY);
        }
        refresher.run();
    }

    private static boolean edgeAboveHits(TabletUiState state, EdgeHit edgeHit, QuestCardLayout questHit, CanvasImageLayer imageHit, CanvasTextLayer textHit) {
        if (state == null || edgeHit == null) {
            return false;
        }
        String group = TabletStateQueries.selectedGroupName(state);
        List<String> order = state.canvasLayerOrderByGroup.getOrDefault(group, List.of());
        String edgeKey = CanvasLayerOrdering.connectionKey(CanvasRenderer.edgeKey(edgeHit.sourceQuestId(), edgeHit.targetQuestId()));
        int edgeIndex = order.indexOf(edgeKey);
        if (edgeIndex < 0) {
            return false;
        }
        return above(order, edgeIndex, questHit == null ? "" : CanvasLayerOrdering.questKey(questHit.questId()))
                && above(order, edgeIndex, imageHit == null ? "" : CanvasLayerOrdering.imageKey(imageHit.id()))
                && above(order, edgeIndex, textHit == null ? "" : CanvasLayerOrdering.textKey(textHit.id()));
    }

    private static boolean above(List<String> order, int edgeIndex, String otherKey) {
        if (otherKey == null || otherKey.isBlank()) {
            return true;
        }
        int otherIndex = order.indexOf(otherKey);
        return otherIndex < 0 || edgeIndex > otherIndex;
    }
}
