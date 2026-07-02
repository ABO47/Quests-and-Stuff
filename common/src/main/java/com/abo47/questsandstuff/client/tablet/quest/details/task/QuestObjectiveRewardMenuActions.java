package com.abo47.questsandstuff.client.tablet.quest.details.task;

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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

final class QuestObjectiveRewardMenuActions {
    private QuestObjectiveRewardMenuActions() {
    }

    static List<ContextAction> actions(TabletUiState state, Player player, String questId, String contextId) {
        CompoundTag rewardTag = ClientQuestCache.quest(questId)
                .getCompound("rewards")
                .getCompound(contextId);
        JsonObject rewardJson = QuestObjectiveMenuSupport.parseObjectiveJson(rewardTag.getString("json"));
        boolean selectable = QuestObjectiveSelectableRewards.isSelectable(rewardJson);
        List<ContextAction> editActions = new ArrayList<>();
        if ("command".equals(TaskJsonFactory.typePath(rewardJson.has("type") ? rewardJson.get("type").getAsString() : ""))) {
            editActions.add(ContextActions.rename(TabletTranslationKeys.text(QuestTranslationKeys.EDIT_COMMAND_REWARD), () -> {
                ContextMenuState.clearDeleteConfirm(state);
                QuestObjectiveEditActions.openExistingCommandRewardEditor(state, questId, contextId);
            }));
        }
        if (QuestObjectiveXpEditor.isXp(rewardJson)) {
            editActions.add(ContextActions.rename(TabletTranslationKeys.text(QuestTranslationKeys.EDIT_XP), () -> {
                ContextMenuState.clearDeleteConfirm(state);
                QuestDetailsTransientManager.openXpPicker(state, questId, contextId, false);
            }));
        }
        if (!selectable) {
            editActions.add(ContextActions.action(TabletTranslationKeys.text(QuestTranslationKeys.MAKE_SELECTABLE_REWARD), "selectable", TabletColors.INTERACTIVE, () -> {
                ContextMenuState.clearDeleteConfirm(state);
                QuestObjectiveSelectableRewards.makeSelectable(player, questId, contextId);
            }));
        }
        List<ContextAction> actions = new ArrayList<>();
        actions.add(ContextActions.promoted(TabletTranslationKeys.text(QuestTranslationKeys.CHANGE_REWARD), "rename", TabletColors.INTERACTIVE, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsTransientManager.openTypePicker(state, "reward_change", contextId);
        }));
        actions.add(ContextActions.promotedRename(TabletTranslationKeys.text(QuestTranslationKeys.RENAME_REWARD), () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestObjectiveEditActions.openObjectiveRenameEditor(state, questId, contextId, false);
        }));
        if (!editActions.isEmpty()) {
            actions.add(QuestObjectiveMenuSupport.editSubmenu(editActions));
        }
        QuestObjectiveMenuSupport.addMoveActions(actions, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            EditorQuestCommandClient.moveQuestReward(player, questId, contextId, -1);
        }, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            EditorQuestCommandClient.moveQuestReward(player, questId, contextId, 1);
        });
        actions.add(QuestObjectiveMenuSupport.visualsSubmenu(state, questId, contextId, false));
        String deleteKey = "quest_details_reward:" + questId + ":" + contextId;
        actions.add(ContextActions.delete(state, deleteKey, TabletTranslationKeys.text(TabletTranslationKeys.COMMON_DELETE), () -> {
            EditorQuestCommandClient.removeQuestReward(player, questId, contextId);
        }));
        return actions;
    }
}
