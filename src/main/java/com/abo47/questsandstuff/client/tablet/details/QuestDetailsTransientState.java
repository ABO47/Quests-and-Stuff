package com.abo47.questsandstuff.client.tablet.details;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuAnimation;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

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
        state.questDetailsContextW = 0;
        state.questDetailsContextH = 0;
        closeTypePicker(state);
        closeCommandRewardEditor(state);
        closeObjectiveRename(state);
        state.contextDeleteConfirmKey = "";
    }

    public static void closeContext(TabletUiState state) {
        state.questDetailsContextOpen = false;
        state.questDetailsContextKind = "";
        state.questDetailsContextId = "";
        state.contextDeleteConfirmKey = "";
    }

    public static void openTypePicker(TabletUiState state, String kind, String targetId) {
        state.questDetailsTypePickerOpen = true;
        ContextMenuAnimation.start(state, ContextMenuAnimation.DEFAULT_KEY);
        state.questDetailsTypePickerKind = kind == null ? "" : kind;
        state.questDetailsTypePickerTargetId = targetId == null ? "" : targetId;
        state.questDetailsTypePickerX = state.questDetailsContextX;
        state.questDetailsTypePickerY = state.questDetailsContextY;
    }

    public static void closeTypePicker(TabletUiState state) {
        state.questDetailsTypePickerOpen = false;
        state.questDetailsTypePickerKind = "";
        state.questDetailsTypePickerTargetId = "";
    }

    public static void openCommandRewardEditor(TabletUiState state, String questId, String id, String command, String title, String icon) {
        state.questDetailsCommandRewardEditorOpen = true;
        state.questDetailsCommandRewardQuestId = questId == null ? "" : questId;
        state.questDetailsCommandRewardId = id == null ? "" : id;
        state.questDetailsCommandRewardCommand = command == null ? "" : command;
        state.questDetailsCommandRewardTitle = title == null || title.isBlank() ? "Command" : title;
        state.questDetailsCommandRewardIcon = icon == null || icon.isBlank() ? "minecraft:command_block" : icon;
        closeTypePicker(state);
        closeContext(state);
    }

    public static void openObjectiveRename(TabletUiState state, String questId, String id, boolean task, String draft) {
        state.questDetailsObjectiveRenameOpen = true;
        state.questDetailsObjectiveRenameTask = task;
        state.questDetailsObjectiveRenameQuestId = questId == null ? "" : questId;
        state.questDetailsObjectiveRenameId = id == null ? "" : id;
        state.questDetailsObjectiveRenameDraft = draft == null ? "" : draft;
        closeTypePicker(state);
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
    }

    public static boolean closeFloatingPopups(TabletUiState state) {
        boolean changed = false;
        if (state.questDetailsTypePickerOpen) {
            closeTypePicker(state);
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
        if (state.questDetailsToolsOpen) {
            state.questDetailsToolsOpen = false;
            changed = true;
        }
        return changed;
    }
}
