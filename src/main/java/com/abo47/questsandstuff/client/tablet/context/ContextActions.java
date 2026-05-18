package com.abo47.questsandstuff.client.tablet.context;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.confirmDeleteClick;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.pendingDeleteLabel;

public final class ContextActions {
    private ContextActions() {
    }

    public static ContextAction add(String label, Runnable action) {
        return action(label, "add", ModColors.SUCCESS, action);
    }

    public static ContextAction rename(String label, Runnable action) {
        return action(label, "rename", ModColors.INTERACTIVE, action);
    }

    public static ContextAction changeIcon(Runnable action) {
        return action(QuestVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_ICON), "icon", ModColors.INTERACTIVE, action);
    }

    public static ContextAction changeVariant(Runnable action) {
        return action(QuestVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_VARIANT), "variant", ModColors.INTERACTIVE, action);
    }

    public static ContextAction editMotion(Runnable action) {
        return action(QuestVocabulary.text(QuestVocabulary.CONTEXT_EDIT_MOTION), "motion", ModColors.INTERACTIVE, action);
    }

    public static ContextAction moveUp(Runnable action) {
        return action(QuestVocabulary.text(QuestVocabulary.CONTEXT_MOVE_UP), "up", ModColors.INTERACTIVE, action);
    }

    public static ContextAction moveDown(Runnable action) {
        return action(QuestVocabulary.text(QuestVocabulary.CONTEXT_MOVE_DOWN), "down", ModColors.INTERACTIVE, action);
    }

    public static ContextAction copy(Runnable action) {
        return action(QuestVocabulary.text(QuestVocabulary.CONTEXT_COPY), "copy", ModColors.INTERACTIVE, action);
    }

    public static ContextAction delete(TabletUiState state, String key, String label, Runnable deleteAction) {
        boolean confirming = key != null && key.equals(state.contextDeleteConfirmKey);
        return new ContextAction(pendingDeleteLabel(state, key, label), "delete", ModColors.ERROR, confirming, () -> {
            if (confirmDeleteClick(state, key)) {
                deleteAction.run();
            }
        });
    }

    public static ContextAction warningDelete(TabletUiState state, String key, String label, Runnable deleteAction) {
        boolean confirming = key != null && key.equals(state.contextDeleteConfirmKey);
        return new ContextAction(pendingDeleteLabel(state, key, label), "delete", ModColors.WARNING, confirming, () -> {
            if (confirmDeleteClick(state, key)) {
                deleteAction.run();
            }
        });
    }

    public static ContextAction action(String label, String icon, int color, Runnable action) {
        return new ContextAction(label, icon, color, action);
    }

    public static ContextAction stayOpen(String label, String icon, int color, Runnable action) {
        return new ContextAction(label, icon, color, false, action);
    }
}
