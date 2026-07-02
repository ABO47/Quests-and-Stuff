package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.controls.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

final class QuestObjectiveMenuSupport {
    private QuestObjectiveMenuSupport() {
    }

    static ContextAction editSubmenu(List<ContextAction> editActions) {
        return ContextActionFactory.submenu(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_EDIT), "rename", TabletColors.INTERACTIVE, editActions);
    }

    static void addMoveActions(List<ContextAction> actions, Runnable moveUp, Runnable moveDown) {
        actions.add(ContextActionFactory.moveUp(moveUp));
        actions.add(ContextActionFactory.moveDown(moveDown));
    }

    static ContextAction visualsSubmenu(TabletUiState state, String questId, String objectiveId, boolean task) {
        List<ContextAction> visualActions = new ArrayList<>();
        visualActions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_ICON), "icon", TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsWindow.openIconPicker(state, task ? ModalTargets.taskIcon(questId, objectiveId) : ModalTargets.rewardIcon(questId, objectiveId));
        }));
        addEntityIconActions(visualActions, state, questId, objectiveId, task);
        return ContextActionFactory.submenu(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_VISUALS), "style", TabletColors.INTERACTIVE, visualActions);
    }

    static JsonObject parseObjectiveJson(String value) {
        TaskJsonFactory.ParseResult result = TaskJsonFactory.readResult(value);
        if (!result.valid()) {
            QuestsAndStuffMod.debugLog(
                    "[QnS:UI] objective menu json fallback diagnostic={}",
                    result.diagnostic()
            );
        }
        return result.value();
    }

    private static void addEntityIconActions(List<ContextAction> actions, TabletUiState state, String questId, String objectiveId, boolean task) {
        String icon = QuestObjectiveEditActions.objectiveIcon(questId, objectiveId, task);
        EntityIconControls.addEntityVariantAndMotionActions(
                actions,
                state,
                icon,
                task ? ModalTargets.objectiveTask(questId, objectiveId) : ModalTargets.objectiveReward(questId, objectiveId),
                () -> QuestDetailsTransientManager.closeContext(state),
                () -> EntityMotionEditor.openObjectiveIcon(state, questId, objectiveId, task, state.questDetails.questDetailsContextX, state.questDetails.questDetailsContextY),
                () -> {
                }
        );
    }
}
