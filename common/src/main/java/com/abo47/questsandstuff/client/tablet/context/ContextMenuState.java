package com.abo47.questsandstuff.client.tablet.context;

import com.abo47.questsandstuff.client.tablet.controls.ScrollController;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class ContextMenuState {
    private ContextMenuState() {
    }

    public static boolean isOpen(TabletUiState state) {
        return state != null && state.contextMenu.contextMenuOpen;
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
        state.contextMenu.contextMenuOpen = true;
        ContextMenuAnimation.start(state, ContextMenuAnimation.DEFAULT_KEY);
        state.contextMenu.contextMenuX = localX;
        state.contextMenu.contextMenuY = localY;
        state.contextMenu.contextMenuAnchorX = localX;
        state.contextMenu.contextMenuAnchorY = localY;
        state.contextMenu.contextLogicalX = logicalX;
        state.contextMenu.contextLogicalY = logicalY;
        state.contextMenu.contextPointerLogicalX = pointerLogicalX;
        state.contextMenu.contextPointerLogicalY = pointerLogicalY;
        clearTarget(state);
        resetMenuMetrics(state);
        clearDeleteConfirm(state);
        closeExclusiveSubmenus(state);
    }

    public static void targetCanvas(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.contextMenu.contextMenuTarget = ContextMenuTarget.CANVAS;
        clearTarget(state);
    }

    public static void targetSelection(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.contextMenu.contextMenuTarget = ContextMenuTarget.SELECTION;
        clearTarget(state);
    }

    public static void targetQuest(TabletUiState state, String questId) {
        if (state == null) {
            return;
        }
        clearTarget(state);
        state.contextMenu.contextMenuTarget = ContextMenuTarget.QUEST;
        state.contextMenu.contextQuestId = clean(questId);
    }

    public static void targetEdge(TabletUiState state, String sourceQuestId, String targetQuestId) {
        if (state == null) {
            return;
        }
        clearTarget(state);
        state.contextMenu.contextMenuTarget = ContextMenuTarget.EDGE;
        state.contextMenu.contextEdgeSource = clean(sourceQuestId);
        state.contextMenu.contextEdgeTarget = clean(targetQuestId);
    }

    public static void targetImage(TabletUiState state, String imageId) {
        if (state == null) {
            return;
        }
        clearTarget(state);
        state.contextMenu.contextMenuTarget = ContextMenuTarget.IMAGE;
        state.contextMenu.contextCanvasImageId = clean(imageId);
    }

    public static void targetText(TabletUiState state, String textId) {
        if (state == null) {
            return;
        }
        clearTarget(state);
        state.contextMenu.contextMenuTarget = ContextMenuTarget.TEXT;
        state.contextMenu.contextCanvasTextId = clean(textId);
    }

    public static void targetExclusiveChoice(TabletUiState state, String ecId) {
        if (state == null) {
            return;
        }
        clearTarget(state);
        state.contextMenu.contextMenuTarget = ContextMenuTarget.EXCLUSIVE_CHOICE;
        state.contextMenu.contextCanvasExclusiveChoiceId = clean(ecId);
    }

    public static void close(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.contextMenu.contextMenuOpen = false;
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
        state.contextMenu.contextMenuX = x;
        state.contextMenu.contextMenuY = y;
        state.contextMenu.contextMenuWidthPx = Math.max(0, width);
        state.contextMenu.contextMenuHeightPx = Math.max(0, height);
        state.contextMenu.contextMenuRows = Math.max(0, rowCount);
        state.contextMenu.contextMenuScrollMax = Math.max(0, scrollMax);
        state.contextMenu.contextMenuScroll = ScrollController.clamp(state.contextMenu.contextMenuScroll, state.contextMenu.contextMenuScrollMax);
        if (state.contextMenu.contextMenuScrollMax <= 0) {
            state.contextMenu.contextMenuScrollDragging = false;
        }
    }

    public static void setLastClick(TabletUiState state, int x, int y) {
        if (state == null) {
            return;
        }
        state.contextMenu.contextLastClickX = x;
        state.contextMenu.contextLastClickY = y;
    }

    public static void scrollByWheel(TabletUiState state, double wheelDelta) {
        if (!isOpen(state) || state.contextMenu.contextMenuScrollMax <= 0 || wheelDelta == 0) {
            return;
        }
        int step = wheelDelta > 0 ? -1 : 1;
        setScroll(state, state.contextMenu.contextMenuScroll + step);
    }

    public static void setScroll(TabletUiState state, int value) {
        if (state == null) {
            return;
        }
        state.contextMenu.contextMenuScroll = ScrollController.clamp(value, state.contextMenu.contextMenuScrollMax);
    }

    public static void setScrollDragging(TabletUiState state, boolean dragging) {
        if (state == null) {
            return;
        }
        state.contextMenu.contextMenuScrollDragging = dragging;
    }

    public static ScrollState scrollState(TabletUiState state) {
        return ScrollState.bind(
                () -> state.contextMenu.contextMenuScroll,
                value -> setScroll(state, value),
                () -> state.contextMenu.contextMenuScrollDragging,
                dragging -> setScrollDragging(state, dragging)
        );
    }

    public static void clearDeleteConfirm(TabletUiState state) {
        if (state != null) {
            state.contextMenu.contextDeleteConfirmKey = "";
        }
    }

    public static boolean isDeleteConfirming(TabletUiState state, String key) {
        return state != null && key != null && key.equals(state.contextMenu.contextDeleteConfirmKey);
    }

    public static String pendingDeleteLabel(TabletUiState state, String key, String fallback) {
        return isDeleteConfirming(state, key) ? "Sure?" : fallback;
    }

    public static boolean confirmDeleteClick(TabletUiState state, String key) {
        if (state == null) {
            return false;
        }
        String safeKey = clean(key);
        if (safeKey.equals(state.contextMenu.contextDeleteConfirmKey)) {
            clearDeleteConfirm(state);
            return true;
        }
        state.contextMenu.contextDeleteConfirmKey = safeKey;
        return false;
    }

    public static void closeExclusiveSubmenus(TabletUiState state) {
        if (state != null) {
            state.contextMenu.contextQuestCompletionSoundMenuOpen = false;
        }
    }

    public static void clearTarget(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.contextMenu.contextQuestId = "";
        state.contextMenu.contextEdgeSource = "";
        state.contextMenu.contextEdgeTarget = "";
        state.contextMenu.contextCanvasImageId = "";
        state.contextMenu.contextCanvasTextId = "";
        state.contextMenu.contextCanvasExclusiveChoiceId = "";
    }

    private static void resetMenuMetrics(TabletUiState state) {
        state.contextMenu.contextMenuRows = 0;
        state.contextMenu.contextMenuScroll = 0;
        state.contextMenu.contextMenuScrollMax = 0;
        state.contextMenu.contextMenuScrollDragging = false;
        state.contextMenu.contextMenuWidthPx = 0;
        state.contextMenu.contextMenuHeightPx = 0;
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }
}
