package com.abo47.questsandstuff.client.tablet.details;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuAnimation;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.tools.ToolMenuAnimation;

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
        closeItemSourcePicker(state);
        closeXpPicker(state);
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
        closeItemSourcePicker(state);
        closeXpPicker(state);
    }

    public static void closeTypePicker(TabletUiState state) {
        state.questDetailsTypePickerOpen = false;
        state.questDetailsTypePickerKind = "";
        state.questDetailsTypePickerTargetId = "";
    }

    public static void openItemSourcePicker(TabletUiState state, String target) {
        state.questDetailsItemSourcePickerOpen = true;
        ContextMenuAnimation.start(state, ContextMenuAnimation.DEFAULT_KEY);
        state.questDetailsItemSourcePickerTarget = target == null ? "" : target;
        state.questDetailsItemSourcePickerX = state.questDetailsTypePickerX;
        state.questDetailsItemSourcePickerY = state.questDetailsTypePickerY;
    }

    public static void closeItemSourcePicker(TabletUiState state) {
        state.questDetailsItemSourcePickerOpen = false;
        state.questDetailsItemSourcePickerTarget = "";
    }

    public static void openXpPicker(TabletUiState state, String questId, String id, boolean task) {
        state.questDetailsXpPickerOpen = true;
        ContextMenuAnimation.start(state, ContextMenuAnimation.DEFAULT_KEY);
        state.questDetailsXpPickerQuestId = questId == null ? "" : questId;
        state.questDetailsXpPickerEntryId = id == null ? "" : id;
        state.questDetailsXpPickerTask = task;
        state.questDetailsXpPickerX = state.questDetailsTypePickerOpen ? state.questDetailsTypePickerX : state.questDetailsContextX;
        state.questDetailsXpPickerY = state.questDetailsTypePickerOpen ? state.questDetailsTypePickerY : state.questDetailsContextY;
        closeTypePicker(state);
        closeItemSourcePicker(state);
        closeCommandRewardEditor(state);
        closeObjectiveRename(state);
        closeContext(state);
    }

    public static void closeXpPicker(TabletUiState state) {
        state.questDetailsXpPickerOpen = false;
        state.questDetailsXpPickerTask = false;
        state.questDetailsXpPickerQuestId = "";
        state.questDetailsXpPickerEntryId = "";
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
        if (state.questDetailsTypePickerOpen) {
            closeTypePicker(state);
            changed = true;
        }
        if (state.questDetailsItemSourcePickerOpen) {
            closeItemSourcePicker(state);
            changed = true;
        }
        if (state.questDetailsXpPickerOpen) {
            closeXpPicker(state);
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
