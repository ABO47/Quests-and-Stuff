package com.abo47.questsandstuff.client.tablet.entity.motion;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionModel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

public final class EntityMotionEditor {
    private EntityMotionEditor() {
    }

    public static void openMainCanvas(TabletUiState state, String chapter, String imageId, int x, int y) {
        CanvasImageLayer image = CanvasLayerMutations.findCanvasImage(state, group, imageId);
        if (!EntityMotionTargets.isEditableEntity(image)) {
            return;
        }
        EntityMotionTargets.openImage(state, EntityMotionTargets.SCOPE_CANVAS, group, "", imageId, x, y, image);
        QuestsAndStuffMod.debugLog("[QnS:UI] entity motion editor open scope=canvas chapter={} image={} spin={}", group, imageId, image.entitySpinSpeed());
    }

    public static void openQuestDetails(TabletUiState state, String questId, String imageId, int x, int y) {
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestStateFacade.quest(questId));
        CanvasImageLayer image = model.image(imageId);
        if (!EntityMotionTargets.isEditableEntity(image)) {
            return;
        }
        EntityMotionTargets.openImage(state, EntityMotionTargets.SCOPE_QUEST_DETAILS, "", questId, imageId, x, y, image);
        QuestsAndStuffMod.debugLog("[QnS:UI] entity motion editor open scope=quest_details quest={} image={} spin={}", questId, imageId, image.entitySpinSpeed());
    }

    public static void openQuestIcon(TabletUiState state, String questId, int x, int y) {
        EntityIconMotion icon = EntityMotionTargets.currentQuestIconMotion(questId);
        if (!icon.editable()) {
            return;
        }
        EntityMotionTargets.openIcon(state, EntityMotionTargets.SCOPE_QUEST_ICON, "", questId, "", x, y, icon);
        QuestsAndStuffMod.debugLog("[QnS:UI] entity motion editor open scope=quest_icon quest={} spin={}", questId, icon.spin());
    }

    public static void openChapterIcon(TabletUiState state, String chapter, int x, int y) {
        EntityIconMotion icon = EntityMotionTargets.currentChapterIconMotion(chapter);
        if (!icon.editable()) {
            return;
        }
        EntityMotionTargets.openIcon(state, EntityMotionTargets.SCOPE_CHAPTER_ICON, chapter, "", "", x, y, icon);
        QuestsAndStuffMod.debugLog("[QnS:UI] entity motion editor open scope=chapter_icon chapter={} spin={}", chapter, icon.spin());
    }

    public static void openTaskIcon(TabletUiState state, String questId, String taskId, boolean task, int x, int y) {
        EntityIconMotion icon = EntityMotionTargets.currentTaskIconMotion(questId, taskId, task);
        if (!icon.editable()) {
            return;
        }
        EntityMotionTargets.openIcon(state, EntityMotionTargets.SCOPE_TASK_ICON, EntityMotionTargets.taskGroup(task), questId, taskId, x, y, icon);
        QuestsAndStuffMod.debugLog("[QnS:UI] entity motion editor open scope=task_icon quest={} task={} task={} spin={}", questId, taskId, task, icon.spin());
    }

    public static void close(TabletUiState state) {
        if (state == null) {
            return;
        }
        boolean wasOpen = state.questDetails.entityMotionEditorOpen;
        String scope = state.questDetails.entityMotionEditorScope;
        String imageId = state.questDetails.entityMotionEditorImageId;
        state.questDetails.entityMotionEditorOpen = false;
        state.questDetails.entityMotionEditorScope = "";
        state.questDetails.entityMotionEditorGroup = "";
        state.questDetails.entityMotionEditorQuestId = "";
        state.questDetails.entityMotionEditorImageId = "";
        state.questDetails.entityMotionEditorBatchImageIds = "";
        state.questDetails.entityMotionEditorX = 0;
        state.questDetails.entityMotionEditorY = 0;
        state.questDetails.entityMotionEditorW = 0;
        state.questDetails.entityMotionEditorH = 0;
        state.questDetails.entityMotionYawDraft = "";
        state.questDetails.entityMotionSpinDraft = "";
        state.questDetails.entityMotionFocusedField = "";
        state.questDetails.entityMotionYawSliderDragging = false;
        state.questDetails.entityMotionSpinSliderDragging = false;
        if (wasOpen) {
            QuestsAndStuffMod.debugLog("[QnS:UI] entity motion editor close scope={} image={}", scope, imageId);
        }
    }

    public static boolean isMainCanvasOpen(TabletUiState state) {
        return state != null
                && state.questDetails.entityMotionEditorOpen
                && (EntityMotionTargets.SCOPE_CANVAS.equals(state.questDetails.entityMotionEditorScope)
                || EntityMotionTargets.SCOPE_QUEST_ICON.equals(state.questDetails.entityMotionEditorScope));
    }

    public static boolean isChapterPanelOpen(TabletUiState state) {
        return state != null && state.questDetails.entityMotionEditorOpen && EntityMotionTargets.SCOPE_CHAPTER_ICON.equals(state.questDetails.entityMotionEditorScope);
    }

    public static boolean isQuestDetailsOpen(TabletUiState state) {
        return state != null
                && state.questDetails.entityMotionEditorOpen
                && (EntityMotionTargets.SCOPE_QUEST_DETAILS.equals(state.questDetails.entityMotionEditorScope)
                || EntityMotionTargets.SCOPE_TASK_ICON.equals(state.questDetails.entityMotionEditorScope));
    }

    public static boolean isDragging(TabletUiState state) {
        return state != null && (state.questDetails.entityMotionYawSliderDragging || state.questDetails.entityMotionSpinSliderDragging);
    }

    public static boolean isMainCanvasHit(TabletUiState state, double localX, double localY) {
        return isMainCanvasOpen(state) && inside(localX, localY, state.questDetails.entityMotionEditorX, state.questDetails.entityMotionEditorY, state.questDetails.entityMotionEditorW, state.questDetails.entityMotionEditorH);
    }

    public static boolean isQuestDetailsHit(TabletUiState state, double mouseX, double mouseY) {
        if (!isQuestDetailsOpen(state)) {
            return false;
        }
        int x = state.questDetails.questDetailsScreenX + state.questDetails.entityMotionEditorX;
        int y = state.questDetails.questDetailsScreenY + state.questDetails.entityMotionEditorY;
        return inside(mouseX, mouseY, x, y, state.questDetails.entityMotionEditorW, state.questDetails.entityMotionEditorH);
    }

    public static boolean isChapterPanelHit(TabletUiState state, double localX, double localY) {
        return isChapterPanelOpen(state) && inside(localX, localY, state.questDetails.entityMotionEditorX, state.questDetails.entityMotionEditorY, state.questDetails.entityMotionEditorW, state.questDetails.entityMotionEditorH);
    }

    public static void renderMainCanvas(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh) {
        if (!isMainCanvasOpen(state)) {
            return;
        }
        EntityMotionValues motion = EntityMotionTargets.mainCanvasMotion(state);
        if (motion == null) {
            close(state);
            return;
        }
        EntityMotionPopup.render(parent, state, player, refresh, motion, parent.getSizeWidth(), parent.getSizeHeight());
    }

    public static void renderChapterPanel(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh) {
        if (!isChapterPanelOpen(state)) {
            return;
        }
        EntityMotionValues motion = EntityMotionTargets.chapterMotion(state);
        if (motion == null) {
            close(state);
            return;
        }
        EntityMotionPopup.render(parent, state, player, refresh, motion, parent.getSizeWidth(), parent.getSizeHeight());
    }

    public static void renderQuestDetails(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh) {
        if (!isQuestDetailsOpen(state)) {
            return;
        }
        EntityMotionValues motion = EntityMotionTargets.questDetailsMotion(state);
        if (motion == null) {
            close(state);
            return;
        }
        EntityMotionPopup.render(modal, state, player, refresh, motion, state.questDetails.questDetailsW, state.questDetails.questDetailsH);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
