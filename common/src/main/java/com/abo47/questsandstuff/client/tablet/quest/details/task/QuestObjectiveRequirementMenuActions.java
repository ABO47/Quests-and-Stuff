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

final class QuestObjectiveRequirementMenuActions {
    private QuestObjectiveRequirementMenuActions() {
    }

    static List<ContextAction> actions(TabletUiState state, Player player, String questId, String contextId) {
        List<ContextAction> actions = new ArrayList<>();
        actions.add(ContextActionFactory.promoted(TabletTranslationKeys.text(QuestTranslationKeys.CHANGE_REQUIREMENT), "rename", TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsTransientManager.openTypePicker(state, "requirement_change", contextId);
        }));
        actions.add(ContextActionFactory.promotedRename(TabletTranslationKeys.text(QuestTranslationKeys.RENAME_REQUIREMENT), () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestObjectiveEditActions.openObjectiveRenameEditor(state, questId, contextId, true);
        }));
        CompoundTag requirementTag = ClientQuestStateFacade.quest(questId)
                .getCompound("tasks")
                .getCompound(contextId);
        JsonObject requirementJson = QuestObjectiveMenuSupport.parseObjectiveJson(requirementTag.getString("json"));
        if (QuestObjectiveXpEditor.isXp(requirementJson)) {
            actions.add(QuestObjectiveMenuSupport.editSubmenu(List.of(ContextActionFactory.rename(TabletTranslationKeys.text(QuestTranslationKeys.EDIT_XP), () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestDetailsTransientManager.openXpPicker(state, questId, contextId, true);
            }))));
        }
        QuestObjectiveMenuSupport.addMoveActions(actions, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            EditorQuestCommandClient.moveQuestTask(player, questId, contextId, -1);
        }, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            EditorQuestCommandClient.moveQuestTask(player, questId, contextId, 1);
        });
        actions.add(QuestObjectiveMenuSupport.visualsSubmenu(state, questId, contextId, true));
        String deleteKey = "quest_details_requirement:" + questId + ":" + contextId;
        actions.add(ContextActionFactory.delete(state, deleteKey, TabletTranslationKeys.text(TabletTranslationKeys.COMMON_DELETE), () -> {
            EditorQuestCommandClient.removeQuestTask(player, questId, contextId);
        }));
        return actions;
    }
}
