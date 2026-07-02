package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;

import com.abo47.questsandstuff.client.tablet.bootstrap.TabletLifecycle;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformSessions;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuAnimation;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class TabletRootWindowController {
    private TabletRootWindowController() {
    }

    public static boolean closeFrontmostWindow(TabletUiState state) {
        if (isAnyModalOpen(state)) {
            ModalCloseActions.closeAll(state);
            return true;
        }
        if (state.questDetails.questDetailsClosing) {
            return true;
        }
        if (state.questDetails.questDetailsOpen) {
            if (closeQuestDetailsFrontState(state)) {
                return true;
            }
            QuestDetailsWindow.close(state);
            TabletLifecycle.rememberMainWindow();
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details close via escape");
            return true;
        }
        if (state.pickers.assetContextOpen || state.pickers.assetRenameOpen) {
            state.pickers.assetContextOpen = false;
            state.pickers.assetRenameOpen = false;
            ContextMenuController.clearDeleteConfirm(state);
            return true;
        }
        if (state.pickers.colorPaletteContextOpen) {
            state.pickers.colorPaletteContextOpen = false;
            state.pickers.colorPaletteContextValue = Integer.MIN_VALUE;
            return true;
        }
        if (state.chapterPanel.chapterTextMenuOpen) {
            state.chapterPanel.chapterTextMenuOpen = false;
            state.chapterPanel.chapterTextMenuTarget = "";
            state.chapterPanel.chapterTextFontSizeDraftTarget = "";
            state.chapterPanel.chapterTextFontSizeFieldTarget = "";
            return true;
        }
        if (state.contextMenu.contextMenuOpen) {
            ContextMenuController.close(state);
            return true;
        }
        if (EntityMotionEditor.isMainCanvasOpen(state)) {
            EntityMotionEditor.close(state);
            return true;
        }
        if (state.chapterPanel.chapterMenuOpen) {
            state.chapterPanel.chapterMenuOpen = false;
            state.chapterPanel.chapterMenuTarget = "";
            ContextMenuController.clearDeleteConfirm(state);
            return true;
        }
        if (state.canvas.toolsMenuOpen || state.canvas.toolsMenuClosing || state.canvas.toolsGridSizeMenuOpen || state.canvas.toolsGridOpacityMenuOpen) {
            ToolMenuAnimation.closeMain(state);
            return true;
        }
        if (!state.questDetails.pendingQuestTitleChangeId.isBlank()) {
            EditorQuestCommandClient.cancelQuestTitleChange(state);
            return true;
        }
        if (!state.canvas.pendingChapterRename.isBlank()) {
            state.canvas.pendingChapterRename = "";
            state.chapterPanel.chapterDraftName = state.root.selectedChapter;
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

    private static boolean closeQuestDetailsFrontState(TabletUiState state) {
        boolean changed = QuestDetailsTransientManager.closeFloatingPopups(state);
        if (EntityMotionEditor.isQuestDetailsOpen(state)) {
            EntityMotionEditor.close(state);
            changed = true;
        }
        if (TextStyleSession.questDetailsOpenOrEditingFont(state)) {
            TextStyleSession.closeQuestDetails(state);
            changed = true;
        }
        if (TextEditSession.isAnyEditing(state)) {
            TextEditSession.closeAny(state, true);
            changed = true;
        }
        if (state.questDetails.questDetailsTitleFocused || state.questDetails.questDetailsQuestId.equals(state.questDetails.pendingQuestTitleChangeId)) {
            state.questDetails.questDetailsTitleFocused = false;
            if (state.questDetails.questDetailsQuestId.equals(state.questDetails.pendingQuestTitleChangeId)) {
                state.questDetails.pendingQuestTitleChangeId = "";
                state.questDetails.questTitleDraft = "";
            }
            changed = true;
        }
        if (!state.questDetails.questDetailsTransformKind.isBlank() || !state.questDetails.questDetailsTransformId.isBlank()) {
            CanvasTransformSessions.clearQuestDetailsSession(state);
            changed = true;
        }
        if (state.questDetails.questDetailsBoxSelecting) {
            state.questDetails.questDetailsBoxSelecting = false;
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
