package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries;

import java.util.Set;

final class CanvasRenderStateController {
    private CanvasRenderStateController() {
    }

    static String prepareRebuild(TabletUiState state) {
        String selectedGroup = TabletStateQueries.selectedGroupName(state);
        state.canvasZoom = CanvasRenderer.clampZoom(state.canvasZoom);
        return selectedGroup;
    }

    static void setContentBounds(TabletUiState state, int contentX, int contentY, int contentW, int contentH) {
        state.canvasContentX = contentX;
        state.canvasContentY = contentY;
        state.canvasContentW = contentW;
        state.canvasContentH = contentH;
    }

    static void pruneStaleInteractiveState(TabletUiState state, Set<String> visibleQuestIds) {
        state.selectedQuestIds.retainAll(visibleQuestIds);
        if (!state.connectSourceQuestId.isBlank() && !ClientQuestCache.containsQuest(state.connectSourceQuestId)) {
            state.connectSourceQuestId = "";
        }
        state.connectSourceQuestIds.removeIf(questId -> !ClientQuestCache.containsQuest(questId));
    }

    static void closeEditOnlyStateWhenReadOnly(TabletUiState state) {
        if (state.canEdit) {
            return;
        }
        ContextMenuState.close(state);
        state.createQuestModalOpen = false;
        state.boxSelecting = false;
    }
}
