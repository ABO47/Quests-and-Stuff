package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestDetailsObjectivesPanel;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuAnimation;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class QuestDetailsLayerWidget extends WidgetGroup {
    private final TabletUiState state;
    private final Runnable refresh;

    public QuestDetailsLayerWidget(int x, int y, int width, int height, TabletUiState state, Runnable refresh) {
        super(x, y, width, height);
        this.state = state;
        this.refresh = refresh == null ? () -> {
        } : refresh;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (QuestDetailsWindow.finishCloseIfDone(state)) {
            refresh.run();
        }
        if (ToolMenuAnimation.finishClosingIfDone(state)) {
            refresh.run();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!QuestDetailsWindow.isVisible(state)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        QuestDetailsWindow.syncScreenOrigin(this, state);
        if (!QuestDetailsWindow.isInteractive(state)) {
            return true;
        }
        boolean detailsContextWasOpen = state.questDetails.questDetailsContextOpen;
        boolean detailsContextHit = detailsContextWasOpen && QuestDetailsWindow.isContextMenuHit(state, mouseX, mouseY);
        boolean textStyleWasOpen = TextStyleSession.questDetailsOpenOrEditingFont(state);
        boolean textStyleHit = textStyleWasOpen && QuestDetailsWindow.isTextStyleMenuHit(state, mouseX, mouseY);
        boolean textOwnerHit = textStyleWasOpen && QuestDetailsWindow.isTextStyleOwnerHit(state, mouseX, mouseY);
        boolean motionEditorWasOpen = EntityMotionEditor.isQuestDetailsOpen(state);
        boolean motionEditorHit = motionEditorWasOpen && EntityMotionEditor.isQuestDetailsHit(state, mouseX, mouseY);
        String selectedObjectiveKindBefore = state.questDetails.questDetailsSelectedObjectiveKind;
        String selectedObjectiveIdBefore = state.questDetails.questDetailsSelectedObjectiveId;
        boolean dragPendingBefore = state.questDetails.questDetailsObjectiveDragPending;
        boolean clearObjectiveSelection = (button == 0 || button == 1)
                && !detailsContextHit
                && !textStyleHit
                && !textOwnerHit
                && !motionEditorHit
                && !QuestDetailsObjectivesPanel.isCardHit(state, mouseX, mouseY);
        if (textStyleHit) {
            TextStyleSession.markQuestDetailsInteraction(state);
        }
        super.mouseClicked(mouseX, mouseY, button);
        if (clearObjectiveSelection && objectiveInteractionStarted(
                state,
                selectedObjectiveKindBefore,
                selectedObjectiveIdBefore,
                dragPendingBefore,
                detailsContextWasOpen
        )) {
            clearObjectiveSelection = false;
        }
        if (motionEditorWasOpen && (motionEditorHit || EntityMotionEditor.isDragging(state))) {
            return true;
        }
        if (motionEditorWasOpen && (button == 0 || button == 1)) {
            if (clearObjectiveSelection) {
                QuestDetailsObjectivesPanel.clearSelection(state, "outside_card_click");
            }
            EntityMotionEditor.close(state);
            refresh.run();
            return true;
        }
        boolean textStyleStillHit = textStyleWasOpen && QuestDetailsWindow.isTextStyleMenuHit(state, mouseX, mouseY);
        boolean textOwnerStillHit = textStyleWasOpen && QuestDetailsWindow.isTextStyleOwnerHit(state, mouseX, mouseY);
        if (textStyleHit || textOwnerHit || textStyleStillHit || textOwnerStillHit || recentlyHandledTextStyleClick()) {
            return true;
        }
        boolean selectionCleared = clearObjectiveSelection
                && QuestDetailsObjectivesPanel.clearSelection(state, "outside_card_click");
        if (!QuestDetailsWindow.isInside(state, mouseX, mouseY)) {
            closeFloatingDetailsState();
            refresh.run();
        } else if ((button == 0 || button == 1) && detailsContextWasOpen && state.questDetails.questDetailsContextOpen && !detailsContextHit) {
            QuestDetailsTransientManager.closeContext(state);
            ContextMenuState.clearDeleteConfirm(state);
            refresh.run();
        } else if ((button == 0 || button == 1) && textStyleWasOpen && !textStyleHit && state.questDetails.questDetailsTextStyleOpen) {
            closeTextStyle("outside_click");
            refresh.run();
        } else if (selectionCleared) {
            refresh.run();
        }
        return true;
    }

    private static boolean objectiveInteractionStarted(
            TabletUiState state,
            String selectedKindBefore,
            String selectedIdBefore,
            boolean dragPendingBefore,
            boolean detailsContextWasOpen
    ) {
        if (!selectedKindBefore.equals(state.questDetails.questDetailsSelectedObjectiveKind)
                || !selectedIdBefore.equals(state.questDetails.questDetailsSelectedObjectiveId)) {
            return true;
        }
        if (!dragPendingBefore && state.questDetails.questDetailsObjectiveDragPending && !state.questDetails.questDetailsObjectiveDragId.isBlank()) {
            return true;
        }
        if (!detailsContextWasOpen && state.questDetails.questDetailsContextOpen) {
            return "requirement".equals(state.questDetails.questDetailsContextKind) || "reward".equals(state.questDetails.questDetailsContextKind);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!QuestDetailsWindow.isVisible(state)) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        QuestDetailsWindow.syncScreenOrigin(this, state);
        if (!QuestDetailsWindow.isInteractive(state)) {
            return true;
        }
        super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!QuestDetailsWindow.isVisible(state)) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        QuestDetailsWindow.syncScreenOrigin(this, state);
        if (!QuestDetailsWindow.isInteractive(state)) {
            return true;
        }
        super.mouseReleased(mouseX, mouseY, button);
        return true;
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!QuestDetailsWindow.isVisible(state)) {
            return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        }
        QuestDetailsWindow.syncScreenOrigin(this, state);
        if (!QuestDetailsWindow.isInteractive(state)) {
            return true;
        }
        super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!QuestDetailsWindow.isVisible(state)) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (!QuestDetailsWindow.isInteractive(state)) {
            return true;
        }
        super.keyPressed(keyCode, scanCode, modifiers);
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!QuestDetailsWindow.isVisible(state)) {
            return super.charTyped(codePoint, modifiers);
        }
        if (!QuestDetailsWindow.isInteractive(state)) {
            return true;
        }
        super.charTyped(codePoint, modifiers);
        return true;
    }

    private void closeFloatingDetailsState() {
        QuestDetailsTransientManager.closeFloatingPopups(state);
        EntityMotionEditor.close(state);
        closeTextStyle("details_outside");
    }

    private void closeTextStyle(String reason) {
        boolean wasOpen = TextStyleSession.questDetailsOpenOrEditingFont(state) || !state.questDetails.questDetailsTextStyleTarget.isBlank();
        String target = state.questDetails.questDetailsTextStyleTarget == null ? "" : state.questDetails.questDetailsTextStyleTarget;
        TextStyleSession.closeQuestDetails(state);
        if (wasOpen) {
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details text style close target={} reason={}", target, reason);
        }
    }

    private boolean recentlyHandledTextStyleClick() {
        return TextStyleSession.recentlyHandledQuestDetailsClick(state, System.currentTimeMillis(), 350L);
    }
}
