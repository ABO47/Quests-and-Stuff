package com.abo47.questsandstuff.client.tablet.quest.details.task;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
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

    static void addSections(ContextMenuSections sections, TabletUiState state, Player player, String questId, String contextId) {
        CompoundTag rewardTag = ClientQuestStateFacade.quest(questId)
                .getCompound("rewards")
                .getCompound(contextId);
        JsonObject rewardJson = QuestTaskMenuSupport.parseTaskJson(rewardTag.getString("json"));
        boolean selectable = QuestTaskSelectableRewards.isSelectable(rewardJson);
        if ("command".equals(TaskJsonFactory.typePath(rewardJson.has("type") ? rewardJson.get("type").getAsString() : ""))) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.rename(TabletTranslationKeys.text(QuestTranslationKeys.EDIT_COMMAND_REWARD), () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestTaskEditActions.openExistingCommandRewardEditor(state, questId, contextId);
            }));
        }
        if (QuestTaskXpEditor.isXp(rewardJson)) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.rename(TabletTranslationKeys.text(QuestTranslationKeys.EDIT_XP), () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestDetailsTransientManager.openXpPicker(state, questId, contextId, false);
            }));
        }
        if (!selectable) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.MAKE_SELECTABLE_REWARD), "selectable", TabletColors.INTERACTIVE, () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestTaskSelectableRewards.makeSelectable(player, questId, contextId);
            }));
        }
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(TabletTranslationKeys.text(QuestTranslationKeys.CHANGE_REWARD), "square_pen", TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsTransientManager.openTypePicker(state, "reward_change", contextId);
        }));
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promotedRename(TabletTranslationKeys.text(QuestTranslationKeys.RENAME_REWARD), () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestTaskEditActions.openTaskRenameEditor(state, questId, contextId, false);
        }));
        List<ContextAction> moveActions = new ArrayList<>();
        QuestTaskMenuSupport.addMoveActions(moveActions, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            EditorQuestCommandClient.moveQuestReward(player, questId, contextId, -1);
        }, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            EditorQuestCommandClient.moveQuestReward(player, questId, contextId, 1);
        });
        sections.addAll(ContextMenuSection.ARRANGE, moveActions);
        List<ContextAction> visualActions = new ArrayList<>();
        QuestTaskMenuSupport.addVisualActions(visualActions, state, questId, contextId, false);
        sections.addAll(ContextMenuSection.APPEARANCE, visualActions);
        String deleteKey = "quest_details_reward:" + questId + ":" + contextId;
        sections.add(ContextMenuSection.DANGER, ContextActionFactory.delete(state, deleteKey, TabletTranslationKeys.text(TabletTranslationKeys.COMMON_REMOVE), () -> {
            EditorQuestCommandClient.removeQuestReward(player, questId, contextId);
        }));
    }
}
