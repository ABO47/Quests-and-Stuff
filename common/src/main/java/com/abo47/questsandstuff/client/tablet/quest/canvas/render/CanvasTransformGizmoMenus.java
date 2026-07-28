package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

public final class CanvasTransformGizmoMenus {
    private CanvasTransformGizmoMenus() {
    }

    public static void addModeActions(ContextMenuSections sections, ContextMenuSection section, TabletUiState state, Runnable refresh) {
        addModeAction(sections, section, state, CanvasTransformMode.MOVE, QuestTranslationKeys.CONTEXT_GIZMO_MOVE, refresh);
        addModeAction(sections, section, state, CanvasTransformMode.RESIZE, QuestTranslationKeys.CONTEXT_GIZMO_RESIZE, refresh);
        addModeAction(sections, section, state, CanvasTransformMode.ROTATE, QuestTranslationKeys.CONTEXT_GIZMO_ROTATE, refresh);
    }

    public static void addCenterPivotAction(ContextMenuSections sections, ContextMenuSection section, TabletUiState state, Runnable centerPivot, Runnable refresh) {
        sections.add(section, ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_CENTER_PIVOT), "align-center-horizontal", TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            centerPivot.run();
            QuestsAndStuffMod.debugLog("[QnS:UI] transform gizmo center_pivot");
            refresh.run();
        }));
    }

    private static void addModeAction(ContextMenuSections sections, ContextMenuSection section, TabletUiState state, CanvasTransformMode mode, String labelKey, Runnable refresh) {
        boolean active = CanvasTransformGizmo.activeMode(state) == mode;
        sections.add(section, ContextActionFactory.stayOpen(TabletTranslationKeys.text(labelKey), mode.icon, active ? TabletColors.SUCCESS : TabletColors.INTERACTIVE, () -> {
            CanvasTransformGizmo.setMode(state, mode);
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] transform gizmo mode={}", mode.id);
            refresh.run();
        }));
    }
}
