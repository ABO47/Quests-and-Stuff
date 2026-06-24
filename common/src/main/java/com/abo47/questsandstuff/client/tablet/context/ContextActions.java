package com.abo47.questsandstuff.client.tablet.context;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.confirmDeleteClick;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.pendingDeleteLabel;

public final class ContextActions {
    private ContextActions() {
    }

    public static ContextAction add(String label, Runnable action) {
        return action(label, "add", ActionTone.SUCCESS, action);
    }

    public static ContextAction rename(String label, Runnable action) {
        return action(label, "rename", ActionTone.PRIMARY, action);
    }

    public static ContextAction promotedRename(String label, Runnable action) {
        return promoted(label, "rename", ActionTone.PRIMARY, action);
    }

    public static ContextAction changeIcon(Runnable action) {
        return promoted(TabletVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_ICON), "icon", ActionTone.PRIMARY, action);
    }

    public static ContextAction changeVariant(Runnable action) {
        return action(TabletVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_VARIANT), "variant", ActionTone.PRIMARY, action);
    }

    public static ContextAction editMotion(Runnable action) {
        return action(TabletVocabulary.text(QuestVocabulary.CONTEXT_EDIT_MOTION), "motion", ActionTone.PRIMARY, action);
    }

    public static ContextAction moveUp(Runnable action) {
        return action(TabletVocabulary.text(QuestVocabulary.CONTEXT_MOVE_UP), "up", ActionTone.PRIMARY, action);
    }

    public static ContextAction moveDown(Runnable action) {
        return action(TabletVocabulary.text(QuestVocabulary.CONTEXT_MOVE_DOWN), "down", ActionTone.PRIMARY, action);
    }

    public static ContextAction copy(Runnable action) {
        return promoted(TabletVocabulary.text(QuestVocabulary.CONTEXT_COPY), "copy", ActionTone.PRIMARY, action);
    }

    public static ContextAction delete(TabletUiState state, String key, String label, Runnable deleteAction) {
        boolean confirming = ContextMenuState.isDeleteConfirming(state, key);
        return new ContextAction(pendingDeleteLabel(state, key, label), "delete", ActionTone.DANGER, confirming, true, () -> {
            if (confirmDeleteClick(state, key)) {
                deleteAction.run();
            }
        });
    }

    public static ContextAction warningDelete(TabletUiState state, String key, String label, Runnable deleteAction) {
        boolean confirming = ContextMenuState.isDeleteConfirming(state, key);
        return new ContextAction(pendingDeleteLabel(state, key, label), "delete", ActionTone.WARNING, confirming, () -> {
            if (confirmDeleteClick(state, key)) {
                deleteAction.run();
            }
        });
    }

    public static ContextAction action(String label, String icon, int color, Runnable action) {
        return new ContextAction(label, icon, color, action);
    }

    public static ContextAction action(String label, String icon, ActionTone tone, Runnable action) {
        return new ContextAction(label, icon, tone, action);
    }

    public static ContextAction promoted(String label, String icon, int color, Runnable action) {
        return new ContextAction(label, icon, color, true, true, action);
    }

    public static ContextAction promoted(String label, String icon, ActionTone tone, Runnable action) {
        return new ContextAction(label, icon, tone, true, true, action);
    }

    public static ContextAction stayOpen(String label, String icon, int color, Runnable action) {
        return new ContextAction(label, icon, color, false, action);
    }

    public static ContextAction stayOpen(String label, String icon, ActionTone tone, Runnable action) {
        return new ContextAction(label, icon, tone, false, action);
    }

    public static ContextAction submenu(String label, String icon, int color, List<ContextAction> children) {
        return new ContextAction(label, icon, color, false, false, () -> {
        }, children);
    }

    public static ContextAction submenu(String label, String icon, ActionTone tone, List<ContextAction> children) {
        return new ContextAction(label, icon, tone, false, false, () -> {
        }, children);
    }
}
