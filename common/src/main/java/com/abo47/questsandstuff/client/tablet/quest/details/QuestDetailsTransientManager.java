package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuAnimationBridge;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuAnimation;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class QuestDetailsTransientManager {
    private QuestDetailsTransientManager() {
    }

    public static void openContext(TabletUiState state, String kind, String id, int x, int y) {
        state.questDetails.questDetailsContextOpen = true;
        ContextMenuAnimationBridge.start(state, ContextMenuAnimationBridge.DEFAULT_KEY);
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
        ContextMenuController.setScrollDragging(state, false);
        closeTypePicker(state);
        closeItemSourcePicker(state);
        closeXpPicker(state);
        closeCommandRewardEditor(state);
        closeTaskRename(state);
        ContextMenuController.clearDeleteConfirm(state);
    }

    public static void closeContext(TabletUiState state) {
        state.questDetails.questDetailsContextOpen = false;
        state.questDetails.questDetailsContextKind = "";
        state.questDetails.questDetailsContextId = "";
        state.questDetails.questDetailsContextScroll = 0;
        state.questDetails.questDetailsContextScrollMax = 0;
        ContextMenuController.setScrollDragging(state, false);
        ContextMenuController.clearDeleteConfirm(state);
    }

    public static void openTypePicker(TabletUiState state, String kind, String targetId) {
        ContextMenuAnimationBridge.start(state, ContextMenuAnimationBridge.DEFAULT_KEY);
        state.questDetails.questDetailsPickerSession = QuestDetailsPickerSession.type(kind, targetId, state.questDetails.questDetailsContextX, state.questDetails.questDetailsContextY);
    }

    public static void closeTypePicker(TabletUiState state) {
        if (state.questDetails.questDetailsPickerSession.typePicker()) {
            state.questDetails.questDetailsPickerSession = QuestDetailsPickerSession.none();
        }
    }

    public static void openItemSourcePicker(TabletUiState state, String target) {
        ContextMenuAnimationBridge.start(state, ContextMenuAnimationBridge.DEFAULT_KEY);
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
        ContextMenuAnimationBridge.start(state, ContextMenuAnimationBridge.DEFAULT_KEY);
        QuestDetailsPickerSession active = state.questDetails.questDetailsPickerSession;
        int x = active.typePicker() ? active.x() : state.questDetails.questDetailsContextX;
        int y = active.typePicker() ? active.y() : state.questDetails.questDetailsContextY;
        state.questDetails.questDetailsPickerSession = QuestDetailsPickerSession.xp(questId, id, task, x, y);
        closeCommandRewardEditor(state);
        closeTaskRename(state);
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

    public static void openTaskRename(TabletUiState state, String questId, String id, boolean task, String draft) {
        state.questDetails.questDetailsTaskRenameOpen = true;
        state.questDetails.questDetailsTaskRenameTask = task;
        state.questDetails.questDetailsTaskRenameQuestId = questId == null ? "" : questId;
        state.questDetails.questDetailsTaskRenameId = id == null ? "" : id;
        state.questDetails.questDetailsTaskRenameDraft = draft == null ? "" : draft;
        state.questDetails.questDetailsTaskRenameFocusPending = true;
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

    public static void closeTaskRename(TabletUiState state) {
        state.questDetails.questDetailsTaskRenameOpen = false;
        state.questDetails.questDetailsTaskRenameTask = false;
        state.questDetails.questDetailsTaskRenameQuestId = "";
        state.questDetails.questDetailsTaskRenameId = "";
        state.questDetails.questDetailsTaskRenameDraft = "";
        state.questDetails.questDetailsTaskRenameFocusPending = false;
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
        if (state.questDetails.questDetailsTaskRenameOpen) {
            closeTaskRename(state);
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
