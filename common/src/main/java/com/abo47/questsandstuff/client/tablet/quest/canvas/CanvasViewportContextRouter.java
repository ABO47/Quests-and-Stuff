package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.EdgeHit;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuAnimation;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;
import java.util.Map;

final class CanvasViewportContextRouter {
    private CanvasViewportContextRouter() {
    }

    static void closeContextMenu(TabletUiState state) {
        state.contextMenuOpen = false;
        state.contextMenuRows = 0;
        state.contextMenuScroll = 0;
        state.contextMenuScrollMax = 0;
        state.contextDeleteConfirmKey = "";
        state.contextQuestCompletionSoundMenuOpen = false;
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
        state.contextPointerLogicalX = CanvasGeometry.screenToNearestLogicalX(state, localX);
        state.contextPointerLogicalY = CanvasGeometry.screenToNearestLogicalY(state, localY);
        state.contextMenuOpen = true;
        ContextMenuAnimation.start(state, ContextMenuAnimation.DEFAULT_KEY);
        EntityMotionEditor.close(state);
        state.contextMenuX = localX;
        state.contextMenuY = localY;
        state.contextMenuAnchorX = localX;
        state.contextMenuAnchorY = localY;
        state.contextMenuScroll = 0;
        state.contextMenuScrollMax = 0;
        state.contextQuestCompletionSoundMenuOpen = false;
        state.createQuestModalOpen = false;
        state.contextLogicalX = logicalX;
        state.contextLogicalY = logicalY;
        state.contextQuestId = "";
        state.contextEdgeSource = "";
        state.contextEdgeTarget = "";
        state.contextCanvasImageId = "";
        state.contextCanvasTextId = "";
        if (CanvasRenderer.totalCanvasSelectionCount(state) > 1 && CanvasRenderer.isSelectionBoundsHit(state, localX, localY)) {
            state.contextMenuTarget = ContextMenuTarget.SELECTION;
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=selection count={}", CanvasRenderer.totalCanvasSelectionCount(state));
        } else if (edgeHit != null && edgeAboveHits(state, edgeHit, hit, imageHit, textHit)) {
            state.contextMenuTarget = ContextMenuTarget.EDGE;
            state.contextEdgeSource = edgeHit.sourceQuestId();
            state.contextEdgeTarget = edgeHit.targetQuestId();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=edge source={} target={}", state.contextEdgeSource, state.contextEdgeTarget);
        } else if (textHit != null) {
            state.contextMenuTarget = ContextMenuTarget.TEXT;
            state.contextCanvasTextId = textHit.id();
            state.selectedCanvasTextId = textHit.id();
            state.selectedCanvasTextIds.clear();
            state.selectedCanvasTextIds.add(textHit.id());
            state.selectedCanvasImageId = "";
            state.selectedCanvasImageIds.clear();
            state.selectedQuestIds.clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=text id={}", textHit.id());
        } else if (imageHit != null) {
            state.contextMenuTarget = ContextMenuTarget.IMAGE;
            state.contextCanvasImageId = imageHit.id();
            state.selectedCanvasImageId = imageHit.id();
            state.selectedCanvasImageIds.clear();
            state.selectedCanvasImageIds.add(imageHit.id());
            state.selectedCanvasTextId = "";
            state.selectedCanvasTextIds.clear();
            state.selectedQuestIds.clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=image id={}", imageHit.id());
        } else if (hit != null) {
            state.contextMenuTarget = ContextMenuTarget.QUEST;
            state.contextQuestId = hit.questId();
            if (!state.selectedQuestIds.contains(hit.questId())) {
                state.selectedQuestIds.clear();
                state.selectedQuestIds.add(hit.questId());
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=quest quest={}", state.contextQuestId);
        } else if (edgeHit != null) {
            state.contextMenuTarget = ContextMenuTarget.EDGE;
            state.contextEdgeSource = edgeHit.sourceQuestId();
            state.contextEdgeTarget = edgeHit.targetQuestId();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context menu open target=edge source={} target={}", state.contextEdgeSource, state.contextEdgeTarget);
        } else {
            state.contextMenuTarget = ContextMenuTarget.CANVAS;
            CanvasRenderer.clearCanvasSelection(state);
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
        String group = TabletUiFactory.selectedGroupName(state);
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
