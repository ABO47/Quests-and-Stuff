package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;

import java.util.List;

public final class CanvasTransformGizmoMenus {
    private CanvasTransformGizmoMenus() {
    }

    public static void addModeActions(List<ContextAction> actions, TabletUiState state, Runnable refresh) {
        addModeAction(actions, state, CanvasTransformMode.MOVE, QuestVocabulary.CONTEXT_GIZMO_MOVE, refresh);
        addModeAction(actions, state, CanvasTransformMode.RESIZE, QuestVocabulary.CONTEXT_GIZMO_RESIZE, refresh);
        addModeAction(actions, state, CanvasTransformMode.ROTATE, QuestVocabulary.CONTEXT_GIZMO_ROTATE, refresh);
    }

    public static void addCenterPivotAction(List<ContextAction> actions, TabletUiState state, Runnable centerPivot, Runnable refresh) {
        actions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_CENTER_PIVOT), "align-center-horizontal", ModColors.INTERACTIVE, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            centerPivot.run();
            QuestsAndStuffMod.debugLog("[QnS:UI] transform gizmo center_pivot");
            refresh.run();
        }));
    }

    private static void addModeAction(List<ContextAction> actions, TabletUiState state, CanvasTransformMode mode, String labelKey, Runnable refresh) {
        boolean active = CanvasTransformGizmo.activeMode(state) == mode;
        actions.add(ContextActions.stayOpen(TabletVocabulary.text(labelKey), mode.icon, active ? ModColors.SUCCESS : ModColors.INTERACTIVE, () -> {
            CanvasTransformGizmo.setMode(state, mode);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] transform gizmo mode={}", mode.id);
            refresh.run();
        }));
    }
}
