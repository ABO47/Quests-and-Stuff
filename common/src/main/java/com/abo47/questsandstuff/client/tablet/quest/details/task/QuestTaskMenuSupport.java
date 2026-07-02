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

final class QuestTaskMenuSupport {
    private QuestTaskMenuSupport() {
    }

    static ContextAction editSubmenu(List<ContextAction> editActions) {
        return ContextActionFactory.submenu(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_EDIT), "rename", TabletColors.INTERACTIVE, editActions);
    }

    static void addMoveActions(List<ContextAction> actions, Runnable moveUp, Runnable moveDown) {
        actions.add(ContextActionFactory.moveUp(moveUp));
        actions.add(ContextActionFactory.moveDown(moveDown));
    }

    static ContextAction visualsSubmenu(TabletUiState state, String questId, String taskId, boolean task) {
        List<ContextAction> visualActions = new ArrayList<>();
        visualActions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_ICON), "icon", TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsWindow.openIconPicker(state, task ? ModalTargets.taskIcon(questId, taskId) : ModalTargets.rewardIcon(questId, taskId));
        }));
        addEntityIconActions(visualActions, state, questId, taskId, task);
        return ContextActionFactory.submenu(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_VISUALS), "style", TabletColors.INTERACTIVE, visualActions);
    }

    static JsonObject parseTaskJson(String value) {
        TaskJsonFactory.ParseResult result = TaskJsonFactory.readResult(value);
        if (!result.valid()) {
            QuestsAndStuffMod.debugLog(
                    "[QnS:UI] task menu json fallback diagnostic={}",
                    result.diagnostic()
            );
        }
        return result.value();
    }

    private static void addEntityIconActions(List<ContextAction> actions, TabletUiState state, String questId, String taskId, boolean task) {
        String icon = QuestTaskEditActions.taskIcon(questId, taskId, task);
        EntityIconControls.addEntityVariantAndMotionActions(
                actions,
                state,
                icon,
                task ? ModalTargets.taskTask(questId, taskId) : ModalTargets.taskReward(questId, taskId),
                () -> QuestDetailsTransientManager.closeContext(state),
                () -> EntityMotionEditor.openTaskIcon(state, questId, taskId, task, state.questDetails.questDetailsContextX, state.questDetails.questDetailsContextY),
                () -> {
                }
        );
    }
}
