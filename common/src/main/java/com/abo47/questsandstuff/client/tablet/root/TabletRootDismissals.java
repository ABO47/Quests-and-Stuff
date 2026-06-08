package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
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
        boolean chapterTextMenuWasOpen = state.chapterTextMenuOpen;
        boolean canvasTextMenuWasOpen = state.canvasTextMenuOpen;
        boolean chapterMenuHit = chapterMenuWasOpen && TabletRootHitTest.isChapterMenuHit(state, rootX, rootY, mouseX, mouseY);
        boolean contextMenuHit = contextMenuWasOpen && TabletRootHitTest.isCanvasContextMenuHit(state, rootX, rootY, mouseX, mouseY);
        boolean chapterTextMenuHit = chapterTextMenuWasOpen && TabletRootHitTest.isChapterTextMenuHit(state, rootX, rootY, mouseX, mouseY);
        boolean canvasTextMenuHit = canvasTextMenuWasOpen && TabletRootHitTest.isCanvasTextMenuHit(state, rootX, rootY, mouseX, mouseY);
        boolean assetContextHit = state.assetContextOpen && TabletRootHitTest.isAssetContextHit(state, rootX, rootY, mouseX, mouseY);
        return new ClickDismissState(chapterMenuWasOpen, contextMenuWasOpen, questDetailsWasOpen, chapterTextMenuWasOpen, canvasTextMenuWasOpen, chapterMenuHit, contextMenuHit, chapterTextMenuHit, canvasTextMenuHit, assetContextHit);
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
            ContextMenuState.clearDeleteConfirm(state);
            changed = true;
        }
        if (clickState.contextMenuWasOpen() && state.contextMenuOpen && !clickState.contextMenuHit()) {
            ContextMenuState.close(state);
            changed = true;
        }
        if (clickState.chapterTextMenuWasOpen() && state.chapterTextMenuOpen && !clickState.chapterTextMenuHit()) {
            state.chapterTextMenuOpen = false;
            state.chapterTextMenuTarget = "";
            state.chapterTextFontSizeFieldTarget = "";
            changed = true;
        }
        if (clickState.canvasTextMenuWasOpen() && state.canvasTextMenuOpen && !clickState.canvasTextMenuHit()) {
            TextStyleSession.closeMainCanvas(state);
            changed = true;
        }
        if (state.assetContextOpen && !clickState.assetContextHit()) {
            state.assetContextOpen = false;
            state.assetRenameOpen = false;
            ContextMenuState.clearDeleteConfirm(state);
            changed = true;
        }
        boolean insideCanvasViewport = TabletRootHitTest.isInsideCanvasViewport(state, rootX, rootY, mouseX, mouseY);
        boolean insideChapterPanel = TabletRootHitTest.isInsideChapterPanel(state, rootX, rootY, mouseX, mouseY);
        if (!insideCanvasViewport
                && !TabletRootHitTest.isToolsMenuHit(state, rootX, rootY, mouseX, mouseY)
                && !state.canvasSelection.questIds().isEmpty()) {
            state.canvasSelection.questIds().clear();
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
            boolean chapterTextMenuWasOpen,
            boolean canvasTextMenuWasOpen,
            boolean chapterMenuHit,
            boolean contextMenuHit,
            boolean chapterTextMenuHit,
            boolean canvasTextMenuHit,
            boolean assetContextHit
    ) {
    }
}
