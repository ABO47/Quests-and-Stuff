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

final class QuestTaskRewardMenuActions {
    private QuestTaskRewardMenuActions() {
    }

    static List<ContextAction> actions(TabletUiState state, Player player, String questId, String contextId) {
        CompoundTag rewardTag = ClientQuestStateFacade.quest(questId)
                .getCompound("rewards")
                .getCompound(contextId);
        JsonObject rewardJson = QuestTaskMenuSupport.parseTaskJson(rewardTag.getString("json"));
        boolean selectable = QuestTaskSelectableRewards.isSelectable(rewardJson);
        List<ContextAction> editActions = new ArrayList<>();
        if ("command".equals(TaskJsonFactory.typePath(rewardJson.has("type") ? rewardJson.get("type").getAsString() : ""))) {
            editActions.add(ContextActionFactory.rename(TabletTranslationKeys.text(QuestTranslationKeys.EDIT_COMMAND_REWARD), () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestTaskEditActions.openExistingCommandRewardEditor(state, questId, contextId);
            }));
        }
        if (QuestTaskXpEditor.isXp(rewardJson)) {
            editActions.add(ContextActionFactory.rename(TabletTranslationKeys.text(QuestTranslationKeys.EDIT_XP), () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestDetailsTransientManager.openXpPicker(state, questId, contextId, false);
            }));
        }
        if (!selectable) {
            editActions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.MAKE_SELECTABLE_REWARD), "selectable", TabletColors.INTERACTIVE, () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestTaskSelectableRewards.makeSelectable(player, questId, contextId);
            }));
        }
        List<ContextAction> actions = new ArrayList<>();
        actions.add(ContextActionFactory.promoted(TabletTranslationKeys.text(QuestTranslationKeys.CHANGE_REWARD), "rename", TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsTransientManager.openTypePicker(state, "reward_change", contextId);
        }));
        actions.add(ContextActionFactory.promotedRename(TabletTranslationKeys.text(QuestTranslationKeys.RENAME_REWARD), () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestTaskEditActions.openTaskRenameEditor(state, questId, contextId, false);
        }));
        if (!editActions.isEmpty()) {
            actions.add(QuestTaskMenuSupport.editSubmenu(editActions));
        }
        QuestTaskMenuSupport.addMoveActions(actions, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            EditorQuestCommandClient.moveQuestReward(player, questId, contextId, -1);
        }, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            EditorQuestCommandClient.moveQuestReward(player, questId, contextId, 1);
        });
        QuestTaskMenuSupport.addVisualActions(actions, state, questId, contextId, false);
        String deleteKey = "quest_details_reward:" + questId + ":" + contextId;
        actions.add(ContextActionFactory.delete(state, deleteKey, TabletTranslationKeys.text(TabletTranslationKeys.COMMON_REMOVE), () -> {
            EditorQuestCommandClient.removeQuestReward(player, questId, contextId);
        }));
        return actions;
    }
}
