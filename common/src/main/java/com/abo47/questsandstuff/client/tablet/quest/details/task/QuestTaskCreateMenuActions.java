package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;

final class QuestTaskCreateMenuActions {
    private QuestTaskCreateMenuActions() {
    }

    static void addSections(ContextMenuSections sections, TabletUiState state, String kind) {
        if (kind.startsWith("task")) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.add(TabletTranslationKeys.text(QuestTranslationKeys.ADD_TASK), () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestDetailsTransientManager.openTypePicker(state, "task", "");
            }));
        }
        if (kind.startsWith("reward")) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.add(TabletTranslationKeys.text(QuestTranslationKeys.ADD_REWARD), () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestDetailsTransientManager.openTypePicker(state, "reward", "");
            }));
        }
    }
}
