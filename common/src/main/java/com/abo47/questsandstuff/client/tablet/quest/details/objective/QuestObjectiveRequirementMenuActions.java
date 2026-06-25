package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
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
        actions.add(ContextActions.promoted(TabletVocabulary.text(QuestVocabulary.CHANGE_REQUIREMENT), "rename", ModColors.INTERACTIVE, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsTransientState.openTypePicker(state, "requirement_change", contextId);
        }));
        actions.add(ContextActions.promotedRename(TabletVocabulary.text(QuestVocabulary.RENAME_REQUIREMENT), () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestObjectiveEditActions.openObjectiveRenameEditor(state, questId, contextId, true);
        }));
        CompoundTag requirementTag = ClientQuestCache.quest(questId)
                .getCompound("tasks")
                .getCompound(contextId);
        JsonObject requirementJson = QuestObjectiveMenuSupport.parseObjectiveJson(requirementTag.getString("json"));
        if (QuestObjectiveXpEditor.isXp(requirementJson)) {
            actions.add(QuestObjectiveMenuSupport.editSubmenu(List.of(ContextActions.rename(TabletVocabulary.text(QuestVocabulary.EDIT_XP), () -> {
                ContextMenuState.clearDeleteConfirm(state);
                QuestDetailsTransientState.openXpPicker(state, questId, contextId, true);
            }))));
        }
        QuestObjectiveMenuSupport.addMoveActions(actions, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            EditorQuestCommandClient.moveQuestTask(player, questId, contextId, -1);
        }, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            EditorQuestCommandClient.moveQuestTask(player, questId, contextId, 1);
        });
        actions.add(QuestObjectiveMenuSupport.visualsSubmenu(state, questId, contextId, true));
        String deleteKey = "quest_details_requirement:" + questId + ":" + contextId;
        actions.add(ContextActions.delete(state, deleteKey, TabletVocabulary.text(TabletVocabulary.COMMON_DELETE), () -> {
            EditorQuestCommandClient.removeQuestTask(player, questId, contextId);
        }));
        return actions;
    }
}
