package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;

import java.util.ArrayList;
import java.util.List;

final class QuestTaskCreateMenuActions {
    private QuestTaskCreateMenuActions() {
    }

    static List<ContextAction> actions(TabletUiState state, String kind) {
        List<ContextAction> actions = new ArrayList<>();
        if (kind.startsWith("task")) {
            actions.add(ContextActionFactory.add(TabletTranslationKeys.text(QuestTranslationKeys.ADD_TASK), () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestDetailsTransientManager.openTypePicker(state, "task", "");
            }));
        }
        if (kind.startsWith("reward")) {
            actions.add(ContextActionFactory.add(TabletTranslationKeys.text(QuestTranslationKeys.ADD_REWARD), () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestDetailsTransientManager.openTypePicker(state, "reward", "");
            }));
        }
        return actions;
    }
}
