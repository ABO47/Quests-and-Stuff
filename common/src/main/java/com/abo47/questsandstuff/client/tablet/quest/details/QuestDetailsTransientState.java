package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuAnimation;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuAnimation;

public final class QuestDetailsTransientState {
    private QuestDetailsTransientState() {
    }

    public static void openContext(TabletUiState state, String kind, String id, int x, int y) {
        state.questDetailsContextOpen = true;
        ContextMenuAnimation.start(state, ContextMenuAnimation.DEFAULT_KEY);
        state.questDetailsContextKind = kind == null ? "" : kind;
        state.questDetailsContextId = id == null ? "" : id;
        state.questDetailsContextX = x;
        state.questDetailsContextY = y;
        state.questDetailsContextAnchorX = x;
        state.questDetailsContextAnchorY = y;
        state.questDetailsContextW = 0;
        state.questDetailsContextH = 0;
        state.questDetailsContextScroll = 0;
        state.questDetailsContextScrollMax = 0;
        ContextMenuState.setScrollDragging(state, false);
        closeTypePicker(state);
        closeItemSourcePicker(state);
        closeXpPicker(state);
        closeCommandRewardEditor(state);
        closeObjectiveRename(state);
        ContextMenuState.clearDeleteConfirm(state);
    }

    public static void closeContext(TabletUiState state) {
        state.questDetailsContextOpen = false;
        state.questDetailsContextKind = "";
        state.questDetailsContextId = "";
        state.questDetailsContextScroll = 0;
        state.questDetailsContextScrollMax = 0;
        ContextMenuState.setScrollDragging(state, false);
        ContextMenuState.clearDeleteConfirm(state);
    }

    public static void openTypePicker(TabletUiState state, String kind, String targetId) {
        ContextMenuAnimation.start(state, ContextMenuAnimation.DEFAULT_KEY);
        state.questDetailsPickerSession = QuestDetailsPickerSession.type(kind, targetId, state.questDetailsContextX, state.questDetailsContextY);
    }

    public static void closeTypePicker(TabletUiState state) {
        if (state.questDetailsPickerSession.typePicker()) {
            state.questDetailsPickerSession = QuestDetailsPickerSession.none();
        }
    }

    public static void openItemSourcePicker(TabletUiState state, String target) {
        ContextMenuAnimation.start(state, ContextMenuAnimation.DEFAULT_KEY);
        QuestDetailsPickerSession active = state.questDetailsPickerSession;
        int x = active.typePicker() ? active.x() : state.questDetailsContextX;
        int y = active.typePicker() ? active.y() : state.questDetailsContextY;
        state.questDetailsPickerSession = QuestDetailsPickerSession.itemSource(target, x, y);
    }

    public static void closeItemSourcePicker(TabletUiState state) {
        if (state.questDetailsPickerSession.itemSourcePicker()) {
            state.questDetailsPickerSession = QuestDetailsPickerSession.none();
        }
    }

    public static void openXpPicker(TabletUiState state, String questId, String id, boolean task) {
        ContextMenuAnimation.start(state, ContextMenuAnimation.DEFAULT_KEY);
        QuestDetailsPickerSession active = state.questDetailsPickerSession;
        int x = active.typePicker() ? active.x() : state.questDetailsContextX;
        int y = active.typePicker() ? active.y() : state.questDetailsContextY;
        state.questDetailsPickerSession = QuestDetailsPickerSession.xp(questId, id, task, x, y);
        closeCommandRewardEditor(state);
        closeObjectiveRename(state);
        closeContext(state);
    }

    public static void closeXpPicker(TabletUiState state) {
        if (state.questDetailsPickerSession.xpPicker()) {
            state.questDetailsPickerSession = QuestDetailsPickerSession.none();
        }
    }

    public static void openCommandRewardEditor(TabletUiState state, String questId, String id, String command, String title, String icon) {
        state.questDetailsCommandRewardEditorOpen = true;
        state.questDetailsCommandRewardQuestId = questId == null ? "" : questId;
        state.questDetailsCommandRewardId = id == null ? "" : id;
        state.questDetailsCommandRewardCommand = command == null ? "" : command;
        state.questDetailsCommandRewardTitle = title == null || title.isBlank() ? "Command" : title;
        state.questDetailsCommandRewardIcon = icon == null || icon.isBlank() ? "minecraft:command_block" : icon;
        closeTypePicker(state);
        closeItemSourcePicker(state);
        closeXpPicker(state);
        closeContext(state);
    }

    public static void openObjectiveRename(TabletUiState state, String questId, String id, boolean task, String draft) {
        state.questDetailsObjectiveRenameOpen = true;
        state.questDetailsObjectiveRenameTask = task;
        state.questDetailsObjectiveRenameQuestId = questId == null ? "" : questId;
        state.questDetailsObjectiveRenameId = id == null ? "" : id;
        state.questDetailsObjectiveRenameDraft = draft == null ? "" : draft;
        state.questDetailsObjectiveRenameFocusPending = true;
        closeTypePicker(state);
        closeItemSourcePicker(state);
        closeXpPicker(state);
        closeContext(state);
    }

    public static void closeCommandRewardEditor(TabletUiState state) {
        state.questDetailsCommandRewardEditorOpen = false;
        state.questDetailsCommandRewardQuestId = "";
        state.questDetailsCommandRewardId = "";
        state.questDetailsCommandRewardCommand = "";
        state.questDetailsCommandRewardTitle = "";
        state.questDetailsCommandRewardIcon = "";
    }

    public static void closeObjectiveRename(TabletUiState state) {
        state.questDetailsObjectiveRenameOpen = false;
        state.questDetailsObjectiveRenameTask = false;
        state.questDetailsObjectiveRenameQuestId = "";
        state.questDetailsObjectiveRenameId = "";
        state.questDetailsObjectiveRenameDraft = "";
        state.questDetailsObjectiveRenameFocusPending = false;
    }

    public static boolean closeFloatingPopups(TabletUiState state) {
        boolean changed = false;
        if (state.questDetailsPickerSession.active()) {
            state.questDetailsPickerSession = QuestDetailsPickerSession.none();
            changed = true;
        }
        if (state.questDetailsCommandRewardEditorOpen) {
            closeCommandRewardEditor(state);
            changed = true;
        }
        if (state.questDetailsObjectiveRenameOpen) {
            closeObjectiveRename(state);
            changed = true;
        }
        if (state.questDetailsContextOpen) {
            closeContext(state);
            changed = true;
        }
        if (state.questDetailsToolsOpen || state.questDetailsToolsClosing) {
            ToolMenuAnimation.closeQuestDetails(state);
            changed = true;
        }
        return changed;
    }
}
