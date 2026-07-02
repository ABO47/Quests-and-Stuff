package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import java.util.List;

public final class CanvasTransformGizmoMenus {
    private CanvasTransformGizmoMenus() {
    }

    public static void addModeActions(List<ContextAction> actions, TabletUiState state, Runnable refresh) {
        addModeAction(actions, state, CanvasTransformMode.MOVE, QuestTranslationKeys.CONTEXT_GIZMO_MOVE, refresh);
        addModeAction(actions, state, CanvasTransformMode.RESIZE, QuestTranslationKeys.CONTEXT_GIZMO_RESIZE, refresh);
        addModeAction(actions, state, CanvasTransformMode.ROTATE, QuestTranslationKeys.CONTEXT_GIZMO_ROTATE, refresh);
    }

    public static void addCenterPivotAction(List<ContextAction> actions, TabletUiState state, Runnable centerPivot, Runnable refresh) {
        actions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_CENTER_PIVOT), "align-center-horizontal", TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            centerPivot.run();
            QuestsAndStuffMod.debugLog("[QnS:UI] transform gizmo center_pivot");
            refresh.run();
        }));
    }

    private static void addModeAction(List<ContextAction> actions, TabletUiState state, CanvasTransformMode mode, String labelKey, Runnable refresh) {
        boolean active = CanvasTransformGizmo.activeMode(state) == mode;
        actions.add(ContextActionFactory.stayOpen(TabletTranslationKeys.text(labelKey), mode.icon, active ? TabletColors.SUCCESS : TabletColors.INTERACTIVE, () -> {
            CanvasTransformGizmo.setMode(state, mode);
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] transform gizmo mode={}", mode.id);
            refresh.run();
        }));
    }
}
