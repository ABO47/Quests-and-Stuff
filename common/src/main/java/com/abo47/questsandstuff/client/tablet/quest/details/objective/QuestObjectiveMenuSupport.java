package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;
import com.abo47.questsandstuff.client.tablet.entity.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

final class QuestObjectiveMenuSupport {
    private QuestObjectiveMenuSupport() {
    }

    static ContextAction editSubmenu(List<ContextAction> editActions) {
        return ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_EDIT), "rename", ModColors.INTERACTIVE, editActions);
    }

    static void addMoveActions(List<ContextAction> actions, Runnable moveUp, Runnable moveDown) {
        actions.add(ContextActions.moveUp(moveUp));
        actions.add(ContextActions.moveDown(moveDown));
    }

    static ContextAction visualsSubmenu(TabletUiState state, String questId, String objectiveId, boolean task) {
        List<ContextAction> visualActions = new ArrayList<>();
        visualActions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_ICON), "icon", ModColors.INTERACTIVE, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsWindow.openIconPicker(state, task ? ModalTargets.taskIcon(questId, objectiveId) : ModalTargets.rewardIcon(questId, objectiveId));
        }));
        addEntityIconActions(visualActions, state, questId, objectiveId, task);
        return ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_VISUALS), "style", ModColors.INTERACTIVE, visualActions);
    }

    static JsonObject parseObjectiveJson(String value) {
        QuestObjectiveJsons.ParseResult result = QuestObjectiveJsons.readResult(value);
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
                () -> QuestDetailsTransientState.closeContext(state),
                () -> EntityMotionEditor.openObjectiveIcon(state, questId, objectiveId, task, state.questDetails.questDetailsContextX, state.questDetails.questDetailsContextY),
                () -> {
                }
        );
    }
}
