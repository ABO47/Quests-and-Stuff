package com.abo47.questsandstuff.client.tablet.root;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.bootstrap.TabletLifecycle;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformSessions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuAnimation;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class TabletRootWindowController {
    private TabletRootWindowController() {
    }

    public static boolean closeFrontmostWindow(TabletUiState state) {
        if (isAnyModalOpen(state)) {
            ModalCloseActions.closeAll(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: closed modals");
            return true;
        }
        if (state.teams.inviteCodeModalOpen) {
            state.teams.inviteCodeModalOpen = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: closed teams invite modal");
            return true;
        }
        if (state.teams.confirmModalOpen) {
            state.teams.confirmModalOpen = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: closed teams confirm modal");
            return true;
        }
        if (state.canvas.canvasTextMenuOpen) {
            TextEditSession.closeMainCanvas(state, true);
            TextStyleSession.closeMainCanvas(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: closed canvas text menu");
            return true;
        }
        if (state.questDetails.questDetailsClosing) {
            QuestDetailsWindow.finishCloseAnimation(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: finished quest details close animation");
            return true;
        }
        if (state.questDetails.questDetailsOpen) {
            boolean hadVisibleSubState = hasVisibleQuestDetailsSubState(state);
            if (closeQuestDetailsFrontState(state)) {
                if (!hadVisibleSubState) {
                    QuestDetailsWindow.close(state);
                    TabletLifecycle.rememberMainWindow();
                    QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: force-close quest details stale sub-state");
                    return true;
                }
                QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: closed quest details sub-state");
                return true;
            }
            QuestDetailsWindow.close(state);
            TabletLifecycle.rememberMainWindow();
            QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: closed quest details");
            return true;
        }
        if (state.pickers.assetContextOpen || state.pickers.assetRenameOpen) {
            state.pickers.assetContextOpen = false;
            state.pickers.assetRenameOpen = false;
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: closed asset context/rename");
            return true;
        }
        if (state.pickers.colorPaletteContextOpen) {
            state.pickers.colorPaletteContextOpen = false;
            state.pickers.colorPaletteContextValue = Integer.MIN_VALUE;
            QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: closed color palette context");
            return true;
        }
        if (state.chapterPanel.chapterTextMenuOpen) {
            state.chapterPanel.chapterTextMenuOpen = false;
            state.chapterPanel.chapterTextMenuTarget = "";
            state.chapterPanel.chapterTextFontSizeDraftTarget = "";
            state.chapterPanel.chapterTextFontSizeFieldTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: closed chapter text menu");
            return true;
        }
        if (state.contextMenu.contextMenuOpen) {
            ContextMenuController.close(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: closed context menu");
            return true;
        }
        if (EntityMotionEditor.isMainCanvasOpen(state)) {
            EntityMotionEditor.close(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: closed entity motion editor");
            return true;
        }
        if (state.chapterPanel.chapterMenuOpen) {
            state.chapterPanel.chapterMenuOpen = false;
            state.chapterPanel.chapterMenuTarget = "";
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: closed chapter menu");
            return true;
        }
        if (state.canvas.toolsMenuOpen || state.canvas.toolsGridSizeMenuOpen || state.canvas.toolsGridOpacityMenuOpen) {
            ToolMenuAnimation.closeMain(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: closed tools menu");
            return true;
        }
        if (!state.questDetails.pendingQuestTitleChangeId.isBlank()) {
            EditorQuestCommandClient.cancelQuestTitleChange(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: cancelled quest title change");
            return true;
        }
        if (!state.canvas.pendingChapterRename.isBlank()) {
            state.canvas.pendingChapterRename = "";
            state.chapterPanel.chapterDraftName = state.root.selectedChapter;
            QuestsAndStuffMod.debugLog("[QnS:UI] closeFrontmostWindow: cancelled chapter rename");
            return true;
        }
        return false;
    }

    public static boolean isTextInputActive(TabletUiState state, WidgetGroup root) {
        return state.root.searchFocused
                || state.chapterPanel.chapterSearchFocused
                || state.pickers.iconSearchFocused
                || state.pickers.assetSearchFocused
                || state.pickers.biomeSearchFocused
                || state.pickers.advancementSearchFocused
                || state.pickers.structureSearchFocused
                || state.pickers.blockSearchFocused
                || state.pickers.dimensionSearchFocused
                || state.pickers.lootTableSearchFocused
                || state.pickers.soundSearchFocused
                || state.canvas.toolsSearchFocused
                || state.questDetails.questDetailsTitleFocused
                || state.pickers.assetRenameOpen
                || state.questDetails.questDetailsCommandRewardEditorOpen
                || state.questDetails.questDetailsTaskRenameOpen
                || isFontSizeFieldOpen(state)
                || TextEditSession.isAnyEditing(state)
                || !state.questDetails.pendingQuestTitleChangeId.isBlank()
                || !state.canvas.pendingChapterRename.isBlank()
                || root != null && hasFocusedTextField(root);
    }

    public static boolean isFontSizeFieldOpen(TabletUiState state) {
        return TextStyleSession.isAnyFontSizeFieldOpen(state);
    }

    private static boolean hasVisibleQuestDetailsSubState(TabletUiState state) {
        boolean visible = state.questDetails.questDetailsPickerSession.active()
                || state.questDetails.questDetailsCommandRewardEditorOpen
                || state.questDetails.questDetailsTaskRenameOpen
                || state.questDetails.questDetailsContextOpen
                || state.questDetails.questDetailsToolsOpen
                || state.questDetails.questDetailsToolsClosing
                || EntityMotionEditor.isQuestDetailsOpen(state)
                || TextStyleSession.questDetailsOpenOrEditingFont(state)
                || TextEditSession.isAnyEditing(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] hasVisibleQuestDetailsSubState={}", visible);
        return visible;
    }

    private static boolean closeQuestDetailsFrontState(TabletUiState state) {
        boolean changed = QuestDetailsTransientManager.closeFloatingPopups(state);
        if (EntityMotionEditor.isQuestDetailsOpen(state)) {
            EntityMotionEditor.close(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] closeQuestDetailsFrontState: closed entity editor");
            changed = true;
        }
        if (TextStyleSession.questDetailsOpenOrEditingFont(state)) {
            TextStyleSession.closeQuestDetails(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] closeQuestDetailsFrontState: closed text style session");
            changed = true;
        }
        if (TextEditSession.isAnyEditing(state)) {
            TextEditSession.closeAny(state, true);
            QuestsAndStuffMod.debugLog("[QnS:UI] closeQuestDetailsFrontState: closed text edit session");
            changed = true;
        }
        if (state.questDetails.questDetailsTitleFocused || state.questDetails.questDetailsQuestId.equals(state.questDetails.pendingQuestTitleChangeId)) {
            state.questDetails.questDetailsTitleFocused = false;
            if (state.questDetails.questDetailsQuestId.equals(state.questDetails.pendingQuestTitleChangeId)) {
                state.questDetails.pendingQuestTitleChangeId = "";
                state.questDetails.questTitleDraft = "";
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] closeQuestDetailsFrontState: closed title focused/change");
            changed = true;
        }
        if (!state.questDetails.questDetailsTransformKind.isBlank() || !state.questDetails.questDetailsTransformId.isBlank()) {
            CanvasTransformSessions.clearQuestDetailsSession(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] closeQuestDetailsFrontState: cleared transform session");
            changed = true;
        }
        if (state.questDetails.questDetailsBoxSelecting) {
            state.questDetails.questDetailsBoxSelecting = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] closeQuestDetailsFrontState: cleared box selecting");
            changed = true;
        }
        return changed;
    }

    private static boolean isAnyModalOpen(TabletUiState state) {
        return ModalStateQueries.anyOpen(state);
    }

    private static boolean hasFocusedTextField(WidgetGroup group) {
        for (Widget widget : group.widgets) {
            if (widget instanceof TextFieldWidget && widget.isFocus()) {
                return true;
            }
            if (widget instanceof WidgetGroup child && hasFocusedTextField(child)) {
                return true;
            }
        }
        return false;
    }
}
