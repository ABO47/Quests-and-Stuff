package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuAnimation;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuAnimation;

public final class QuestDetailsTransientState {
    private QuestDetailsTransientState() {
    }

    public static void openContext(TabletUiState state, String kind, String id, int x, int y) {
        state.questDetails.questDetailsContextOpen = true;
        ContextMenuAnimation.start(state, ContextMenuAnimation.DEFAULT_KEY);
        state.questDetails.questDetailsContextKind = kind == null ? "" : kind;
        state.questDetails.questDetailsContextId = id == null ? "" : id;
        state.questDetails.questDetailsContextX = x;
        state.questDetails.questDetailsContextY = y;
        state.questDetails.questDetailsContextAnchorX = x;
        state.questDetails.questDetailsContextAnchorY = y;
        state.questDetails.questDetailsContextW = 0;
        state.questDetails.questDetailsContextH = 0;
        state.questDetails.questDetailsContextScroll = 0;
        state.questDetails.questDetailsContextScrollMax = 0;
        ContextMenuState.setScrollDragging(state, false);
        closeTypePicker(state);
        closeItemSourcePicker(state);
        closeXpPicker(state);
        closeCommandRewardEditor(state);
        closeObjectiveRename(state);
        ContextMenuState.clearDeleteConfirm(state);
    }

    public static void closeContext(TabletUiState state) {
        state.questDetails.questDetailsContextOpen = false;
        state.questDetails.questDetailsContextKind = "";
        state.questDetails.questDetailsContextId = "";
        state.questDetails.questDetailsContextScroll = 0;
        state.questDetails.questDetailsContextScrollMax = 0;
        ContextMenuState.setScrollDragging(state, false);
        ContextMenuState.clearDeleteConfirm(state);
    }

    public static void openTypePicker(TabletUiState state, String kind, String targetId) {
        ContextMenuAnimation.start(state, ContextMenuAnimation.DEFAULT_KEY);
        state.questDetails.questDetailsPickerSession = QuestDetailsPickerSession.type(kind, targetId, state.questDetails.questDetailsContextX, state.questDetails.questDetailsContextY);
    }

    public static void closeTypePicker(TabletUiState state) {
        if (state.questDetails.questDetailsPickerSession.typePicker()) {
            state.questDetails.questDetailsPickerSession = QuestDetailsPickerSession.none();
        }
    }

    public static void openItemSourcePicker(TabletUiState state, String target) {
        ContextMenuAnimation.start(state, ContextMenuAnimation.DEFAULT_KEY);
        QuestDetailsPickerSession active = state.questDetails.questDetailsPickerSession;
        int x = active.typePicker() ? active.x() : state.questDetails.questDetailsContextX;
        int y = active.typePicker() ? active.y() : state.questDetails.questDetailsContextY;
        state.questDetails.questDetailsPickerSession = QuestDetailsPickerSession.itemSource(target, x, y);
    }

    public static void closeItemSourcePicker(TabletUiState state) {
        if (state.questDetails.questDetailsPickerSession.itemSourcePicker()) {
            state.questDetails.questDetailsPickerSession = QuestDetailsPickerSession.none();
        }
    }

    public static void openXpPicker(TabletUiState state, String questId, String id, boolean task) {
        ContextMenuAnimation.start(state, ContextMenuAnimation.DEFAULT_KEY);
        QuestDetailsPickerSession active = state.questDetails.questDetailsPickerSession;
        int x = active.typePicker() ? active.x() : state.questDetails.questDetailsContextX;
        int y = active.typePicker() ? active.y() : state.questDetails.questDetailsContextY;
        state.questDetails.questDetailsPickerSession = QuestDetailsPickerSession.xp(questId, id, task, x, y);
        closeCommandRewardEditor(state);
        closeObjectiveRename(state);
        closeContext(state);
    }

    public static void closeXpPicker(TabletUiState state) {
        if (state.questDetails.questDetailsPickerSession.xpPicker()) {
            state.questDetails.questDetailsPickerSession = QuestDetailsPickerSession.none();
        }
    }

    public static void openCommandRewardEditor(TabletUiState state, String questId, String id, String command, String title, String icon) {
        state.questDetails.questDetailsCommandRewardEditorOpen = true;
        state.questDetails.questDetailsCommandRewardQuestId = questId == null ? "" : questId;
        state.questDetails.questDetailsCommandRewardId = id == null ? "" : id;
        state.questDetails.questDetailsCommandRewardCommand = command == null ? "" : command;
        state.questDetails.questDetailsCommandRewardTitle = title == null || title.isBlank() ? "Command" : title;
        state.questDetails.questDetailsCommandRewardIcon = icon == null || icon.isBlank() ? "minecraft:command_block" : icon;
        closeTypePicker(state);
        closeItemSourcePicker(state);
        closeXpPicker(state);
        closeContext(state);
    }

    public static void openObjectiveRename(TabletUiState state, String questId, String id, boolean task, String draft) {
        state.questDetails.questDetailsObjectiveRenameOpen = true;
        state.questDetails.questDetailsObjectiveRenameTask = task;
        state.questDetails.questDetailsObjectiveRenameQuestId = questId == null ? "" : questId;
        state.questDetails.questDetailsObjectiveRenameId = id == null ? "" : id;
        state.questDetails.questDetailsObjectiveRenameDraft = draft == null ? "" : draft;
        state.questDetails.questDetailsObjectiveRenameFocusPending = true;
        closeTypePicker(state);
        closeItemSourcePicker(state);
        closeXpPicker(state);
        closeContext(state);
    }

    public static void closeCommandRewardEditor(TabletUiState state) {
        state.questDetails.questDetailsCommandRewardEditorOpen = false;
        state.questDetails.questDetailsCommandRewardQuestId = "";
        state.questDetails.questDetailsCommandRewardId = "";
        state.questDetails.questDetailsCommandRewardCommand = "";
        state.questDetails.questDetailsCommandRewardTitle = "";
        state.questDetails.questDetailsCommandRewardIcon = "";
    }

    public static void closeObjectiveRename(TabletUiState state) {
        state.questDetails.questDetailsObjectiveRenameOpen = false;
        state.questDetails.questDetailsObjectiveRenameTask = false;
        state.questDetails.questDetailsObjectiveRenameQuestId = "";
        state.questDetails.questDetailsObjectiveRenameId = "";
        state.questDetails.questDetailsObjectiveRenameDraft = "";
        state.questDetails.questDetailsObjectiveRenameFocusPending = false;
    }

    public static boolean closeFloatingPopups(TabletUiState state) {
        boolean changed = false;
        if (state.questDetails.questDetailsPickerSession.active()) {
            state.questDetails.questDetailsPickerSession = QuestDetailsPickerSession.none();
            changed = true;
        }
        if (state.questDetails.questDetailsCommandRewardEditorOpen) {
            closeCommandRewardEditor(state);
            changed = true;
        }
        if (state.questDetails.questDetailsObjectiveRenameOpen) {
            closeObjectiveRename(state);
            changed = true;
        }
        if (state.questDetails.questDetailsContextOpen) {
            closeContext(state);
            changed = true;
        }
        if (state.questDetails.questDetailsToolsOpen || state.questDetails.questDetailsToolsClosing) {
            ToolMenuAnimation.closeQuestDetails(state);
            changed = true;
        }
        return changed;
    }
}
