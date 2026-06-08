package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.tablet.shell.TabletClientHooks;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
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
        if (state.questDetailsClosing) {
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
        if (state.assetContextOpen || state.assetRenameOpen) {
            state.assetContextOpen = false;
            state.assetRenameOpen = false;
            ContextMenuState.clearDeleteConfirm(state);
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
            state.chapterTextFontSizeFieldTarget = "";
            return true;
        }
        if (state.contextMenuOpen) {
            ContextMenuState.close(state);
            return true;
        }
        if (EntityMotionEditor.isMainCanvasOpen(state)) {
            EntityMotionEditor.close(state);
            return true;
        }
        if (state.chapterMenuOpen) {
            state.chapterMenuOpen = false;
            state.chapterMenuTarget = "";
            ContextMenuState.clearDeleteConfirm(state);
            return true;
        }
        if (state.toolsMenuOpen || state.toolsMenuClosing || state.toolsGridSizeMenuOpen || state.toolsGridOpacityMenuOpen) {
            ToolMenuAnimation.closeMain(state);
            return true;
        }
        if (!state.pendingQuestTitleChangeId.isBlank()) {
            EditorCommandClient.cancelQuestTitleChange(state);
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
                || state.advancementSearchFocused
                || state.structureSearchFocused
                || state.blockSearchFocused
                || state.dimensionSearchFocused
                || state.lootTableSearchFocused
                || state.soundSearchFocused
                || state.toolsSearchFocused
                || state.questDetailsTitleFocused
                || state.assetRenameOpen
                || state.questDetailsCommandRewardEditorOpen
                || state.questDetailsObjectiveRenameOpen
                || isFontSizeFieldOpen(state)
                || TextEditSession.isAnyEditing(state)
                || !state.pendingQuestTitleChangeId.isBlank()
                || !state.pendingChapterRename.isBlank()
                || root != null && hasFocusedTextField(root);
    }

    public static boolean isFontSizeFieldOpen(TabletUiState state) {
        return TextStyleSession.isAnyFontSizeFieldOpen(state);
    }

    private static boolean closeQuestDetailsFrontState(TabletUiState state) {
        boolean changed = QuestDetailsTransientState.closeFloatingPopups(state);
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
        if (state.questDetailsTitleFocused || state.questDetailsQuestId.equals(state.pendingQuestTitleChangeId)) {
            state.questDetailsTitleFocused = false;
            if (state.questDetailsQuestId.equals(state.pendingQuestTitleChangeId)) {
                state.pendingQuestTitleChangeId = "";
                state.questTitleDraft = "";
            }
            changed = true;
        }
        if (!state.questDetailsTransformKind.isBlank() || !state.questDetailsTransformId.isBlank()) {
            CanvasTransformSessions.clearQuestDetailsSession(state);
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
