package com.abo47.questsandstuff.client.tablet.context;

import com.abo47.questsandstuff.client.tablet.controls.ScrollController;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class ContextMenuState {
    private ContextMenuState() {
    }

    public static boolean isOpen(TabletUiState state) {
        return state != null && state.contextMenuOpen;
    }

    public static void openCanvas(
            TabletUiState state,
            int localX,
            int localY,
            int logicalX,
            int logicalY,
            int pointerLogicalX,
            int pointerLogicalY
    ) {
        if (state == null) {
            return;
        }
        state.contextMenuOpen = true;
        ContextMenuAnimation.start(state, ContextMenuAnimation.DEFAULT_KEY);
        state.contextMenuX = localX;
        state.contextMenuY = localY;
        state.contextMenuAnchorX = localX;
        state.contextMenuAnchorY = localY;
        state.contextLogicalX = logicalX;
        state.contextLogicalY = logicalY;
        state.contextPointerLogicalX = pointerLogicalX;
        state.contextPointerLogicalY = pointerLogicalY;
        clearTarget(state);
        resetMenuMetrics(state);
        clearDeleteConfirm(state);
        closeExclusiveSubmenus(state);
    }

    public static void targetCanvas(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.contextMenuTarget = ContextMenuTarget.CANVAS;
        clearTarget(state);
    }

    public static void targetSelection(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.contextMenuTarget = ContextMenuTarget.SELECTION;
        clearTarget(state);
    }

    public static void targetQuest(TabletUiState state, String questId) {
        if (state == null) {
            return;
        }
        clearTarget(state);
        state.contextMenuTarget = ContextMenuTarget.QUEST;
        state.contextQuestId = clean(questId);
    }

    public static void targetEdge(TabletUiState state, String sourceQuestId, String targetQuestId) {
        if (state == null) {
            return;
        }
        clearTarget(state);
        state.contextMenuTarget = ContextMenuTarget.EDGE;
        state.contextEdgeSource = clean(sourceQuestId);
        state.contextEdgeTarget = clean(targetQuestId);
    }

    public static void targetImage(TabletUiState state, String imageId) {
        if (state == null) {
            return;
        }
        clearTarget(state);
        state.contextMenuTarget = ContextMenuTarget.IMAGE;
        state.contextCanvasImageId = clean(imageId);
    }

    public static void targetText(TabletUiState state, String textId) {
        if (state == null) {
            return;
        }
        clearTarget(state);
        state.contextMenuTarget = ContextMenuTarget.TEXT;
        state.contextCanvasTextId = clean(textId);
    }

    public static void close(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.contextMenuOpen = false;
        resetMenuMetrics(state);
        clearDeleteConfirm(state);
        closeExclusiveSubmenus(state);
    }

    public static void resetClosedMetrics(TabletUiState state) {
        if (state == null) {
            return;
        }
        resetMenuMetrics(state);
        closeExclusiveSubmenus(state);
    }

    public static void setLayout(TabletUiState state, int x, int y, int width, int height, int rowCount, int scrollMax) {
        if (state == null) {
            return;
        }
        state.contextMenuX = x;
        state.contextMenuY = y;
        state.contextMenuWidthPx = Math.max(0, width);
        state.contextMenuHeightPx = Math.max(0, height);
        state.contextMenuRows = Math.max(0, rowCount);
        state.contextMenuScrollMax = Math.max(0, scrollMax);
        state.contextMenuScroll = ScrollController.clamp(state.contextMenuScroll, state.contextMenuScrollMax);
        if (state.contextMenuScrollMax <= 0) {
            state.contextMenuScrollDragging = false;
        }
    }

    public static void setLastClick(TabletUiState state, int x, int y) {
        if (state == null) {
            return;
        }
        state.contextLastClickX = x;
        state.contextLastClickY = y;
    }

    public static void scrollByWheel(TabletUiState state, double wheelDelta) {
        if (!isOpen(state) || state.contextMenuScrollMax <= 0 || wheelDelta == 0) {
            return;
        }
        int step = wheelDelta > 0 ? -1 : 1;
        setScroll(state, state.contextMenuScroll + step);
    }

    public static void setScroll(TabletUiState state, int value) {
        if (state == null) {
            return;
        }
        state.contextMenuScroll = ScrollController.clamp(value, state.contextMenuScrollMax);
    }

    public static void setScrollDragging(TabletUiState state, boolean dragging) {
        if (state == null) {
            return;
        }
        state.contextMenuScrollDragging = dragging;
    }

    public static ScrollState scrollState(TabletUiState state) {
        return ScrollState.bind(
                () -> state.contextMenuScroll,
                value -> setScroll(state, value),
                () -> state.contextMenuScrollDragging,
                dragging -> setScrollDragging(state, dragging)
        );
    }

    public static void clearDeleteConfirm(TabletUiState state) {
        if (state != null) {
            state.contextDeleteConfirmKey = "";
        }
    }

    public static boolean isDeleteConfirming(TabletUiState state, String key) {
        return state != null && key != null && key.equals(state.contextDeleteConfirmKey);
    }

    public static String pendingDeleteLabel(TabletUiState state, String key, String fallback) {
        return isDeleteConfirming(state, key) ? "Sure?" : fallback;
    }

    public static boolean confirmDeleteClick(TabletUiState state, String key) {
        if (state == null) {
            return false;
        }
        String safeKey = clean(key);
        if (safeKey.equals(state.contextDeleteConfirmKey)) {
            clearDeleteConfirm(state);
            return true;
        }
        state.contextDeleteConfirmKey = safeKey;
        return false;
    }

    public static void closeExclusiveSubmenus(TabletUiState state) {
        if (state != null) {
            state.contextQuestCompletionSoundMenuOpen = false;
        }
    }

    public static void clearTarget(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.contextQuestId = "";
        state.contextEdgeSource = "";
        state.contextEdgeTarget = "";
        state.contextCanvasImageId = "";
        state.contextCanvasTextId = "";
    }

    private static void resetMenuMetrics(TabletUiState state) {
        state.contextMenuRows = 0;
        state.contextMenuScroll = 0;
        state.contextMenuScrollMax = 0;
        state.contextMenuScrollDragging = false;
        state.contextMenuWidthPx = 0;
        state.contextMenuHeightPx = 0;
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }
}
