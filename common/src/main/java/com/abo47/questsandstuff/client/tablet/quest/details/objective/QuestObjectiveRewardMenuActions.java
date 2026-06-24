package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
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
        if ("command".equals(QuestObjectiveJsons.typePath(rewardJson.has("type") ? rewardJson.get("type").getAsString() : ""))) {
            editActions.add(ContextActions.rename(TabletVocabulary.text(QuestVocabulary.EDIT_COMMAND_REWARD), () -> {
                ContextMenuState.clearDeleteConfirm(state);
                QuestObjectiveEditActions.openExistingCommandRewardEditor(state, questId, contextId);
            }));
        }
        if (QuestObjectiveXpEditor.isXp(rewardJson)) {
            editActions.add(ContextActions.rename(TabletVocabulary.text(QuestVocabulary.EDIT_XP), () -> {
                ContextMenuState.clearDeleteConfirm(state);
                QuestDetailsTransientState.openXpPicker(state, questId, contextId, false);
            }));
        }
        if (!selectable) {
            editActions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.MAKE_SELECTABLE_REWARD), "selectable", ModColors.INTERACTIVE, () -> {
                ContextMenuState.clearDeleteConfirm(state);
                QuestObjectiveSelectableRewards.makeSelectable(player, questId, contextId);
            }));
        }
        List<ContextAction> actions = new ArrayList<>();
        actions.add(ContextActions.promoted(TabletVocabulary.text(QuestVocabulary.CHANGE_REWARD), "rename", ModColors.INTERACTIVE, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsTransientState.openTypePicker(state, "reward_change", contextId);
        }));
        actions.add(ContextActions.promotedRename(TabletVocabulary.text(QuestVocabulary.RENAME_REWARD), () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestObjectiveEditActions.openObjectiveRenameEditor(state, questId, contextId, false);
        }));
        if (!editActions.isEmpty()) {
            actions.add(QuestObjectiveMenuSupport.editSubmenu(editActions));
        }
        QuestObjectiveMenuSupport.addMoveActions(actions, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            EditorCommandClient.moveQuestReward(player, questId, contextId, -1);
        }, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            EditorCommandClient.moveQuestReward(player, questId, contextId, 1);
        });
        actions.add(QuestObjectiveMenuSupport.visualsSubmenu(state, questId, contextId, false));
        String deleteKey = "quest_details_reward:" + questId + ":" + contextId;
        actions.add(ContextActions.delete(state, deleteKey, TabletVocabulary.text(TabletVocabulary.COMMON_DELETE), () -> {
            EditorCommandClient.removeQuestReward(player, questId, contextId);
        }));
        return actions;
    }
}
