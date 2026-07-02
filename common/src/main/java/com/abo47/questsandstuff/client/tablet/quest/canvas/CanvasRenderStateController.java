package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;

import java.util.Set;

final class CanvasRenderStateController {
    private CanvasRenderStateController() {
    }

    static String prepareRebuild(TabletUiState state) {
        String selectedGroup = TabletStateQueries.selectedGroupName(state);
        state.canvas.canvasZoom = CanvasRenderer.clampZoom(state.canvas.canvasZoom);
        return selectedGroup;
    }

    static void setContentBounds(TabletUiState state, int contentX, int contentY, int contentW, int contentH) {
        state.canvas.canvasContentX = contentX;
        state.canvas.canvasContentY = contentY;
        state.canvas.canvasContentW = contentW;
        state.canvas.canvasContentH = contentH;
    }

    static void pruneStaleInteractiveState(TabletUiState state, Set<String> visibleQuestIds) {
        state.canvas.canvasSelection.questIds().retainAll(visibleQuestIds);
        if (!state.canvas.connectSourceQuestId.isBlank() && !ClientQuestCache.containsQuest(state.canvas.connectSourceQuestId)) {
            state.canvas.connectSourceQuestId = "";
        }
        state.canvas.connectSourceQuestIds.removeIf(questId -> !ClientQuestCache.containsQuest(questId));
    }

    static void closeEditOnlyStateWhenReadOnly(TabletUiState state) {
        if (state.root.canEdit) {
            return;
        }
        ContextMenuState.close(state);
        state.canvas.boxSelecting = false;
    }
}
