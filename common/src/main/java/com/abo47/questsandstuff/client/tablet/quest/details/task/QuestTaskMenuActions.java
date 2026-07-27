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

final class QuestTaskMenuActions {
    private QuestTaskMenuActions() {
    }

    static void addSections(ContextMenuSections sections, TabletUiState state, Player player, String questId, String contextId) {
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(TabletTranslationKeys.text(QuestTranslationKeys.CHANGE_TASK), "square_pen", TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsTransientManager.openTypePicker(state, "task_change", contextId);
        }));
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promotedRename(TabletTranslationKeys.text(QuestTranslationKeys.RENAME_TASK), () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestTaskEditActions.openTaskRenameEditor(state, questId, contextId, true);
        }));
        CompoundTag taskTag = ClientQuestStateFacade.quest(questId)
                .getCompound("tasks")
                .getCompound(contextId);
        JsonObject taskJson = QuestTaskMenuSupport.parseTaskJson(taskTag.getString("json"));
        if (QuestTaskXpEditor.isXp(taskJson)) {
            sections.add(ContextMenuSection.PRIMARY, QuestTaskMenuSupport.editSubmenu(List.of(ContextActionFactory.rename(TabletTranslationKeys.text(QuestTranslationKeys.EDIT_XP), () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestDetailsTransientManager.openXpPicker(state, questId, contextId, true);
            }))));
        }
        List<ContextAction> moveActions = new ArrayList<>();
        QuestTaskMenuSupport.addMoveActions(moveActions, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            EditorQuestCommandClient.moveQuestTask(player, questId, contextId, -1);
        }, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            EditorQuestCommandClient.moveQuestTask(player, questId, contextId, 1);
        });
        sections.addAll(ContextMenuSection.ARRANGE, moveActions);
        List<ContextAction> visualActions = new ArrayList<>();
        QuestTaskMenuSupport.addVisualActions(visualActions, state, questId, contextId, true);
        sections.addAll(ContextMenuSection.APPEARANCE, visualActions);
        String deleteKey = "quest_details_task:" + questId + ":" + contextId;
        sections.add(ContextMenuSection.DANGER, ContextActionFactory.delete(state, deleteKey, TabletTranslationKeys.text(TabletTranslationKeys.COMMON_REMOVE), () -> {
            EditorQuestCommandClient.removeQuestTask(player, questId, contextId);
        }));
    }
}
