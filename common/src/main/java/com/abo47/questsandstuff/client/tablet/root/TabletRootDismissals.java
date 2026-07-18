package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.client.tablet.ui.widget.TabletWidgetCoordinates;

final class TabletRootDismissals {
    private TabletRootDismissals() {
    }

    static ClickDismissState capture(TabletRootWidget root, TabletUiState state, double mouseX, double mouseY) {
        int rootX = TabletWidgetCoordinates.rootX(root);
        int rootY = TabletWidgetCoordinates.rootY(root);
        boolean chapterMenuWasOpen = state.chapterPanel.chapterMenuOpen;
        boolean contextMenuWasOpen = state.contextMenu.contextMenuOpen;
        boolean questDetailsWasOpen = state.questDetails.questDetailsOpen;
        boolean chapterTextMenuWasOpen = state.chapterPanel.chapterTextMenuOpen;
        boolean canvasTextMenuWasOpen = state.canvas.canvasTextMenuOpen;
        boolean chapterMenuHit = chapterMenuWasOpen && TabletRootHitTest.isChapterMenuHit(state, rootX, rootY, mouseX, mouseY);
        boolean contextMenuHit = contextMenuWasOpen && TabletRootHitTest.isCanvasContextMenuHit(state, rootX, rootY, mouseX, mouseY);
        boolean chapterTextMenuHit = chapterTextMenuWasOpen && TabletRootHitTest.isChapterTextMenuHit(state, rootX, rootY, mouseX, mouseY);
        boolean canvasTextMenuHit = canvasTextMenuWasOpen && TabletRootHitTest.isCanvasTextMenuHit(state, rootX, rootY, mouseX, mouseY);
        boolean assetContextHit = state.pickers.assetContextOpen && TabletRootHitTest.isAssetContextHit(state, rootX, rootY, mouseX, mouseY);
        return new ClickDismissState(chapterMenuWasOpen, contextMenuWasOpen, questDetailsWasOpen, chapterTextMenuWasOpen, canvasTextMenuWasOpen, chapterMenuHit, contextMenuHit, chapterTextMenuHit, canvasTextMenuHit, assetContextHit);
    }

    static boolean handleAfterClick(TabletRootWidget root, TabletUiState state, Runnable refresher, ClickDismissState clickState, double mouseX, double mouseY, int button, boolean handled) {
        int rootX = TabletWidgetCoordinates.rootX(root);
        int rootY = TabletWidgetCoordinates.rootY(root);
        if (clickState.questDetailsWasOpen()) {
            return true;
        }
        boolean chapterMenuOpenedByThisClick = state.chapterPanel.chapterMenuOpenedByClick;
        state.chapterPanel.chapterSelectionJustChanged = false;
        state.chapterPanel.chapterMenuOpenedByClick = false;

        if (button != 0 && button != 1) {
            return handled;
        }
        boolean changed = false;
        if (TabletUiFactory.DRAFT_CHAPTER.equals(state.canvas.pendingChapterRename)
                && !TabletRootHitTest.isInsideChapterPanel(state, rootX, rootY, mouseX, mouseY)) {
            state.canvas.pendingChapterRename = "";
            changed = true;
        }
        if (!state.questDetails.pendingQuestTitleChangeId.isBlank()
                && !QuestDetailsWindow.isInside(state, mouseX, mouseY)
                && !TabletRootHitTest.isInsideCanvasViewport(state, rootX, rootY, mouseX, mouseY)) {
            EditorQuestCommandClient.cancelQuestTitleChange(state);
            changed = true;
        }
        if (clickState.chapterMenuWasOpen() && state.chapterPanel.chapterMenuOpen && !chapterMenuOpenedByThisClick && !clickState.chapterMenuHit()) {
            state.chapterPanel.chapterMenuOpen = false;
            ContextMenuController.clearDeleteConfirm(state);
            changed = true;
        }
        if (clickState.contextMenuWasOpen() && state.contextMenu.contextMenuOpen && !clickState.contextMenuHit()) {
            ContextMenuController.close(state);
            changed = true;
        }
        if (clickState.chapterTextMenuWasOpen() && state.chapterPanel.chapterTextMenuOpen && !clickState.chapterTextMenuHit()) {
            state.chapterPanel.chapterTextMenuOpen = false;
            state.chapterPanel.chapterTextMenuTarget = "";
            state.chapterPanel.chapterTextFontSizeFieldTarget = "";
            changed = true;
        }
        if (clickState.canvasTextMenuWasOpen() && state.canvas.canvasTextMenuOpen && !clickState.canvasTextMenuHit()) {
            TextStyleSession.closeMainCanvas(state);
            changed = true;
        }
        if (state.pickers.assetContextOpen && !clickState.assetContextHit()) {
            state.pickers.assetContextOpen = false;
            state.pickers.assetRenameOpen = false;
            ContextMenuController.clearDeleteConfirm(state);
            changed = true;
        }
        boolean insideCanvasViewport = TabletRootHitTest.isInsideCanvasViewport(state, rootX, rootY, mouseX, mouseY);
        boolean insideChapterPanel = TabletRootHitTest.isInsideChapterPanel(state, rootX, rootY, mouseX, mouseY);
        if (!insideCanvasViewport
                && !TabletRootHitTest.isToolsMenuHit(state, rootX, rootY, mouseX, mouseY)
                && !state.canvas.canvasSelection.questIds().isEmpty()) {
            state.canvas.canvasSelection.questIds().clear();
            if (!insideChapterPanel) {
                state.canvas.connectSourceQuestId = "";
                state.canvas.connectSourceQuestIds.clear();
            }
            state.canvas.selectionBoundsVisible = false;
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
