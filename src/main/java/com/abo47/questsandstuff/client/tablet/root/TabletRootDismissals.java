package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;

final class TabletRootDismissals {
    private TabletRootDismissals() {
    }

    static ClickDismissState capture(TabletRootWidget root, TabletUiState state, double mouseX, double mouseY) {
        boolean chapterMenuWasOpen = state.chapterMenuOpen;
        boolean contextMenuWasOpen = state.contextMenuOpen;
        boolean chapterMenuHit = chapterMenuWasOpen && TabletRootHitTest.isChapterMenuHit(state, root.getPositionX(), root.getPositionY(), mouseX, mouseY);
        boolean contextMenuHit = contextMenuWasOpen && TabletRootHitTest.isCanvasContextMenuHit(state, root.getPositionX(), root.getPositionY(), mouseX, mouseY);
        boolean chapterTextMenuHit = state.chapterTextMenuOpen && TabletRootHitTest.isChapterTextMenuHit(state, root.getPositionX(), root.getPositionY(), mouseX, mouseY);
        boolean assetContextHit = state.assetContextOpen && TabletRootHitTest.isAssetContextHit(state, root.getPositionX(), root.getPositionY(), mouseX, mouseY);
        return new ClickDismissState(chapterMenuWasOpen, contextMenuWasOpen, chapterMenuHit, contextMenuHit, chapterTextMenuHit, assetContextHit);
    }

    static boolean handleAfterClick(TabletRootWidget root, TabletUiState state, Runnable refresher, ClickDismissState clickState, double mouseX, double mouseY, int button, boolean handled) {
        if (state.questDetailsOpen) {
            return true;
        }
        boolean chapterMenuOpenedByThisClick = state.chapterMenuOpenedByClick;
        state.chapterSelectionJustChanged = false;
        state.chapterMenuOpenedByClick = false;

        if (button != 0 && button != 1) {
            return handled;
        }
        boolean changed = false;
        if (TabletUiFactory.DRAFT_CHAPTER.equals(state.pendingChapterRename)
                && !TabletRootHitTest.isInsideChapterPanel(state, root.getPositionX(), root.getPositionY(), mouseX, mouseY)) {
            state.pendingChapterRename = "";
            changed = true;
        }
        if (!state.pendingQuestRenameId.isBlank()
                && !QuestDetailsWindow.isInside(state, mouseX, mouseY)
                && !TabletRootHitTest.isInsideCanvasViewport(state, root.getPositionX(), root.getPositionY(), mouseX, mouseY)) {
            EditorCommandClient.cancelQuestRename(state);
            changed = true;
        }
        if (clickState.chapterMenuWasOpen() && state.chapterMenuOpen && !chapterMenuOpenedByThisClick && !clickState.chapterMenuHit()) {
            state.chapterMenuOpen = false;
            state.contextDeleteConfirmKey = "";
            changed = true;
        }
        if (clickState.contextMenuWasOpen() && state.contextMenuOpen && !clickState.contextMenuHit()) {
            state.contextMenuOpen = false;
            state.contextMenuRows = 0;
            state.contextDeleteConfirmKey = "";
            changed = true;
        }
        if (state.chapterTextMenuOpen && !clickState.chapterTextMenuHit() && !handled) {
            state.chapterTextMenuOpen = false;
            state.chapterTextMenuTarget = "";
            state.chapterTextFontSizeSliderTarget = "";
            changed = true;
        }
        if (state.assetContextOpen && !clickState.assetContextHit()) {
            state.assetContextOpen = false;
            state.assetRenameOpen = false;
            state.contextDeleteConfirmKey = "";
            changed = true;
        }
        if (!TabletRootHitTest.isInsideCanvasViewport(state, root.getPositionX(), root.getPositionY(), mouseX, mouseY)
                && !TabletRootHitTest.isToolsMenuHit(state, root.getPositionX(), root.getPositionY(), mouseX, mouseY)
                && !state.selectedQuestIds.isEmpty()) {
            state.selectedQuestIds.clear();
            state.connectSourceQuestId = "";
            state.connectSourceQuestIds.clear();
            state.selectionBoundsVisible = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas selection cleared reason=outside_canvas x={} y={}", Math.round(mouseX - root.getPositionX()), Math.round(mouseY - root.getPositionY()));
            changed = true;
        }
        if (changed) {
            refresher.run();
            return true;
        }
        return handled;
    }

    record ClickDismissState(
            boolean chapterMenuWasOpen,
            boolean contextMenuWasOpen,
            boolean chapterMenuHit,
            boolean contextMenuHit,
            boolean chapterTextMenuHit,
            boolean assetContextHit
    ) {
    }
}
