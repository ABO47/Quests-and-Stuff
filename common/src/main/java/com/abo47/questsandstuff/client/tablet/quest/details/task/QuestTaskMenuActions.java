package com.abo47.questsandstuff.client.tablet.quest.details.task;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import com.google.gson.JsonObject;

final class QuestTaskMenuActions {
    private QuestTaskMenuActions() {
    }

    static List<ContextAction> actions(TabletUiState state, Player player, String questId, String contextId) {
        List<ContextAction> actions = new ArrayList<>();
        actions.add(ContextActionFactory.promoted(TabletTranslationKeys.text(QuestTranslationKeys.CHANGE_TASK), "rename", TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsTransientManager.openTypePicker(state, "task_change", contextId);
        }));
        actions.add(ContextActionFactory.promotedRename(TabletTranslationKeys.text(QuestTranslationKeys.RENAME_TASK), () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestTaskEditActions.openTaskRenameEditor(state, questId, contextId, true);
        }));
        CompoundTag taskTag = ClientQuestStateFacade.quest(questId)
                .getCompound("tasks")
                .getCompound(contextId);
        JsonObject taskJson = QuestTaskMenuSupport.parseTaskJson(taskTag.getString("json"));
        if (QuestTaskXpEditor.isXp(taskJson)) {
            actions.add(QuestTaskMenuSupport.editSubmenu(List.of(ContextActionFactory.rename(TabletTranslationKeys.text(QuestTranslationKeys.EDIT_XP), () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestDetailsTransientManager.openXpPicker(state, questId, contextId, true);
            }))));
        }
        QuestTaskMenuSupport.addMoveActions(actions, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            EditorQuestCommandClient.moveQuestTask(player, questId, contextId, -1);
        }, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            EditorQuestCommandClient.moveQuestTask(player, questId, contextId, 1);
        });
        QuestTaskMenuSupport.addVisualActions(actions, state, questId, contextId, true);
        String deleteKey = "quest_details_task:" + questId + ":" + contextId;
        actions.add(ContextActionFactory.delete(state, deleteKey, TabletTranslationKeys.text(TabletTranslationKeys.COMMON_REMOVE), () -> {
            EditorQuestCommandClient.removeQuestTask(player, questId, contextId);
        }));
        return actions;
    }
}
