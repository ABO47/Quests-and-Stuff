package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.client.tablet.ui.TabletWidgetCoordinates;

final class TabletRootDismissals {
    private TabletRootDismissals() {
    }

    static ClickDismissState capture(TabletRootWidget root, TabletUiState state, double mouseX, double mouseY) {
        int rootX = TabletWidgetCoordinates.rootX(root);
        int rootY = TabletWidgetCoordinates.rootY(root);
        boolean chapterMenuWasOpen = state.chapterMenuOpen;
        boolean contextMenuWasOpen = state.contextMenuOpen;
        boolean questDetailsWasOpen = state.questDetailsOpen;
        boolean chapterMenuHit = chapterMenuWasOpen && TabletRootHitTest.isChapterMenuHit(state, rootX, rootY, mouseX, mouseY);
        boolean contextMenuHit = contextMenuWasOpen && TabletRootHitTest.isCanvasContextMenuHit(state, rootX, rootY, mouseX, mouseY);
        boolean chapterTextMenuHit = state.chapterTextMenuOpen && TabletRootHitTest.isChapterTextMenuHit(state, rootX, rootY, mouseX, mouseY);
        boolean canvasTextMenuHit = state.canvasTextMenuOpen && TabletRootHitTest.isCanvasTextMenuHit(state, rootX, rootY, mouseX, mouseY);
        boolean assetContextHit = state.assetContextOpen && TabletRootHitTest.isAssetContextHit(state, rootX, rootY, mouseX, mouseY);
        return new ClickDismissState(chapterMenuWasOpen, contextMenuWasOpen, questDetailsWasOpen, chapterMenuHit, contextMenuHit, chapterTextMenuHit, canvasTextMenuHit, assetContextHit);
    }

    static boolean handleAfterClick(TabletRootWidget root, TabletUiState state, Runnable refresher, ClickDismissState clickState, double mouseX, double mouseY, int button, boolean handled) {
        int rootX = TabletWidgetCoordinates.rootX(root);
        int rootY = TabletWidgetCoordinates.rootY(root);
        if (clickState.questDetailsWasOpen()) {
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
                && !TabletRootHitTest.isInsideChapterPanel(state, rootX, rootY, mouseX, mouseY)) {
            state.pendingChapterRename = "";
            changed = true;
        }
        if (!state.pendingQuestTitleChangeId.isBlank()
                && !QuestDetailsWindow.isInside(state, mouseX, mouseY)
                && !TabletRootHitTest.isInsideCanvasViewport(state, rootX, rootY, mouseX, mouseY)) {
            EditorCommandClient.cancelQuestTitleChange(state);
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
            state.contextQuestCompletionSoundMenuOpen = false;
            changed = true;
        }
        if (state.chapterTextMenuOpen && !clickState.chapterTextMenuHit()) {
            state.chapterTextMenuOpen = false;
            state.chapterTextMenuTarget = "";
            state.chapterTextFontSizeSliderTarget = "";
            changed = true;
        }
        if (state.canvasTextMenuOpen && !clickState.canvasTextMenuHit()) {
            state.canvasTextMenuOpen = false;
            state.canvasTextMenuTarget = "";
            state.canvasTextFontSizeSliderTarget = "";
            state.canvasTextFontSizeSliderDragging = false;
            state.canvasTextFontSizeSliderDragTarget = "";
            changed = true;
        }
        if (state.assetContextOpen && !clickState.assetContextHit()) {
            state.assetContextOpen = false;
            state.assetRenameOpen = false;
            state.contextDeleteConfirmKey = "";
            changed = true;
        }
        boolean insideCanvasViewport = TabletRootHitTest.isInsideCanvasViewport(state, rootX, rootY, mouseX, mouseY);
        boolean insideChapterPanel = TabletRootHitTest.isInsideChapterPanel(state, rootX, rootY, mouseX, mouseY);
        if (!insideCanvasViewport
                && !TabletRootHitTest.isToolsMenuHit(state, rootX, rootY, mouseX, mouseY)
                && !state.selectedQuestIds.isEmpty()) {
            state.selectedQuestIds.clear();
            if (!insideChapterPanel) {
                state.connectSourceQuestId = "";
                state.connectSourceQuestIds.clear();
            }
            state.selectionBoundsVisible = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas selection cleared reason=outside_canvas x={} y={}", Math.round(mouseX - rootX), Math.round(mouseY - rootY));
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
            boolean questDetailsWasOpen,
            boolean chapterMenuHit,
            boolean contextMenuHit,
            boolean chapterTextMenuHit,
            boolean canvasTextMenuHit,
            boolean assetContextHit
    ) {
    }
}
