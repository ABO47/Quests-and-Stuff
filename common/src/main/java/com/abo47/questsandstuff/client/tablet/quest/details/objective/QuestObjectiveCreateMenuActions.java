package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;

import java.util.ArrayList;
import java.util.List;

final class QuestObjectiveCreateMenuActions {
    private QuestObjectiveCreateMenuActions() {
    }

    static List<ContextAction> actions(TabletUiState state, String kind) {
        List<ContextAction> actions = new ArrayList<>();
        if (kind.startsWith("requirement")) {
            actions.add(ContextActions.add(TabletVocabulary.text(QuestVocabulary.ADD_REQUIREMENT), () -> {
                ContextMenuState.clearDeleteConfirm(state);
                QuestDetailsTransientState.openTypePicker(state, "requirement", "");
            }));
        }
        if (kind.startsWith("reward")) {
            actions.add(ContextActions.add(TabletVocabulary.text(QuestVocabulary.ADD_REWARD), () -> {
                ContextMenuState.clearDeleteConfirm(state);
                QuestDetailsTransientState.openTypePicker(state, "reward", "");
            }));
        }
        return actions;
    }
}
