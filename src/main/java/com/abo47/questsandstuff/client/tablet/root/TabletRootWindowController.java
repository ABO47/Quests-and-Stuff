package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.client.tablet.screen.TabletClientHooks;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
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
        if (state.questDetailsOpen) {
            if (closeQuestDetailsFrontState(state)) {
                return true;
            }
            QuestDetailsWindow.close(state);
            TabletClientHooks.rememberMainWindow();
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details close via escape");
            return true;
        }
        if (state.createQuestModalOpen) {
            state.createQuestModalOpen = false;
            state.createQuestTitle = "";
            return true;
        }
        if (state.assetContextOpen || state.assetRenameOpen) {
            state.assetContextOpen = false;
            state.assetRenameOpen = false;
            state.contextDeleteConfirmKey = "";
            return true;
        }
        if (state.colorPaletteContextOpen) {
            state.colorPaletteContextOpen = false;
            state.colorPaletteContextValue = Integer.MIN_VALUE;
            return true;
        }
        if (state.chapterTextMenuOpen) {
            state.chapterTextMenuOpen = false;
            state.chapterTextMenuTarget = "";
            state.chapterTextFontSizeDraftTarget = "";
            state.chapterTextFontSizeSliderTarget = "";
            return true;
        }
        if (state.contextMenuOpen) {
            state.contextMenuOpen = false;
            state.contextMenuRows = 0;
            state.contextDeleteConfirmKey = "";
            return true;
        }
        if (EntityMotionEditor.isMainCanvasOpen(state)) {
            EntityMotionEditor.close(state);
            return true;
        }
        if (state.chapterMenuOpen) {
            state.chapterMenuOpen = false;
            state.chapterMenuTarget = "";
            state.contextDeleteConfirmKey = "";
            return true;
        }
        if (state.toolsMenuOpen || state.toolsGridSizeMenuOpen || state.toolsGridOpacityMenuOpen) {
            state.toolsMenuOpen = false;
            state.toolsGridSizeMenuOpen = false;
            state.toolsGridOpacityMenuOpen = false;
            return true;
        }
        if (!state.pendingQuestRenameId.isBlank()) {
            EditorCommandClient.cancelQuestRename(state);
            return true;
        }
        if (!state.pendingChapterRename.isBlank()) {
            state.pendingChapterRename = "";
            state.chapterDraftName = state.selectedGroup;
            return true;
        }
        return false;
    }

    public static boolean isTextInputActive(TabletUiState state, WidgetGroup root) {
        return state.searchFocused
                || state.chapterSearchFocused
                || state.iconSearchFocused
                || state.assetSearchFocused
                || state.biomeSearchFocused
                || state.lootTableSearchFocused
                || state.toolsSearchFocused
                || state.questDetailsTitleFocused
                || state.assetRenameOpen
                || state.questDetailsCommandRewardEditorOpen
                || state.questDetailsObjectiveRenameOpen
                || state.canvasTextEditOpen
                || !state.questDetailsTextEditTarget.isBlank()
                || !state.pendingQuestRenameId.isBlank()
                || !state.pendingChapterRename.isBlank()
                || state.createQuestModalOpen
                || root != null && hasFocusedTextField(root);
    }

    private static boolean closeQuestDetailsFrontState(TabletUiState state) {
        boolean changed = QuestDetailsTransientState.closeFloatingPopups(state);
        if (EntityMotionEditor.isQuestDetailsOpen(state)) {
            EntityMotionEditor.close(state);
            changed = true;
        }
        if (state.questDetailsTextStyleOpen || !state.questDetailsTextFontSizeSliderTarget.isBlank()) {
            state.questDetailsTextStyleOpen = false;
            state.questDetailsTextStyleTarget = "";
            state.questDetailsTextStyleMenuX = 0;
            state.questDetailsTextStyleMenuY = 0;
            state.questDetailsTextStyleMenuW = 0;
            state.questDetailsTextStyleMenuH = 0;
            state.questDetailsTextFontSizeSliderTarget = "";
            state.questDetailsTextFontSizeSliderDragging = false;
            state.questDetailsTextFontSizeSliderDragTarget = "";
            changed = true;
        }
        if (state.canvasTextEditOpen || !state.questDetailsTextEditTarget.isBlank()) {
            state.canvasTextEditOpen = false;
            state.canvasTextEditTarget = "";
            state.canvasTextEditDraft = "";
            state.questDetailsTextEditTarget = "";
            state.questDetailsTextEditDraft = "";
            changed = true;
        }
        if (state.questDetailsTitleFocused || state.questDetailsQuestId.equals(state.pendingQuestRenameId)) {
            state.questDetailsTitleFocused = false;
            if (state.questDetailsQuestId.equals(state.pendingQuestRenameId)) {
                state.pendingQuestRenameId = "";
                state.questTitleDraft = "";
            }
            changed = true;
        }
        if (!state.questDetailsTransformKind.isBlank() || !state.questDetailsTransformId.isBlank()) {
            state.questDetailsTransformKind = "";
            state.questDetailsTransformId = "";
            state.questDetailsTransformMode = "";
            changed = true;
        }
        if (state.questDetailsBoxSelecting) {
            state.questDetailsBoxSelecting = false;
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
