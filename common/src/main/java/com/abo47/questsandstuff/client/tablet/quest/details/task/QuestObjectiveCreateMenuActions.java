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

final class QuestObjectiveCreateMenuActions {
    private QuestObjectiveCreateMenuActions() {
    }

    static List<ContextAction> actions(TabletUiState state, String kind) {
        List<ContextAction> actions = new ArrayList<>();
        if (kind.startsWith("requirement")) {
            actions.add(ContextActions.add(TabletTranslationKeys.text(QuestTranslationKeys.ADD_REQUIREMENT), () -> {
                ContextMenuState.clearDeleteConfirm(state);
                QuestDetailsTransientManager.openTypePicker(state, "requirement", "");
            }));
        }
        if (kind.startsWith("reward")) {
            actions.add(ContextActions.add(TabletTranslationKeys.text(QuestTranslationKeys.ADD_REWARD), () -> {
                ContextMenuState.clearDeleteConfirm(state);
                QuestDetailsTransientManager.openTypePicker(state, "reward", "");
            }));
        }
        return actions;
    }
}
