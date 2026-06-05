package com.abo47.questsandstuff.client.tablet.entity.motion;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionModel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

public final class EntityMotionEditor {
    private EntityMotionEditor() {
    }

    public static void openMainCanvas(TabletUiState state, String group, String imageId, int x, int y) {
        CanvasImageLayer image = CanvasRenderer.findCanvasImage(state, group, imageId);
        if (!EntityMotionTargets.isEditableEntity(image)) {
            return;
        }
        EntityMotionTargets.openImage(state, EntityMotionTargets.SCOPE_CANVAS, group, "", imageId, x, y, image);
        QuestsAndStuffMod.debugLog("[QnS:UI] entity motion editor open scope=canvas group={} image={} spin={}", group, imageId, image.entitySpinSpeed());
    }

    public static void openQuestDetails(TabletUiState state, String questId, String imageId, int x, int y) {
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
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

    public static void openObjectiveIcon(TabletUiState state, String questId, String objectiveId, boolean task, int x, int y) {
        EntityIconMotion icon = EntityMotionTargets.currentObjectiveIconMotion(questId, objectiveId, task);
        if (!icon.editable()) {
            return;
        }
        EntityMotionTargets.openIcon(state, EntityMotionTargets.SCOPE_OBJECTIVE_ICON, EntityMotionTargets.objectiveGroup(task), questId, objectiveId, x, y, icon);
        QuestsAndStuffMod.debugLog("[QnS:UI] entity motion editor open scope=objective_icon quest={} objective={} task={} spin={}", questId, objectiveId, task, icon.spin());
    }

    public static void close(TabletUiState state) {
        if (state == null) {
            return;
        }
        boolean wasOpen = state.entityMotionEditorOpen;
        String scope = state.entityMotionEditorScope;
        String imageId = state.entityMotionEditorImageId;
        state.entityMotionEditorOpen = false;
        state.entityMotionEditorScope = "";
        state.entityMotionEditorGroup = "";
        state.entityMotionEditorQuestId = "";
        state.entityMotionEditorImageId = "";
        state.entityMotionEditorX = 0;
        state.entityMotionEditorY = 0;
        state.entityMotionEditorW = 0;
        state.entityMotionEditorH = 0;
        state.entityMotionYawDraft = "";
        state.entityMotionSpinDraft = "";
        state.entityMotionFocusedField = "";
        state.entityMotionYawSliderDragging = false;
        state.entityMotionSpinSliderDragging = false;
        if (wasOpen) {
            QuestsAndStuffMod.debugLog("[QnS:UI] entity motion editor close scope={} image={}", scope, imageId);
        }
    }

    public static boolean isMainCanvasOpen(TabletUiState state) {
        return state != null
                && state.entityMotionEditorOpen
                && (EntityMotionTargets.SCOPE_CANVAS.equals(state.entityMotionEditorScope)
                || EntityMotionTargets.SCOPE_QUEST_ICON.equals(state.entityMotionEditorScope));
    }

    public static boolean isChapterPanelOpen(TabletUiState state) {
        return state != null && state.entityMotionEditorOpen && EntityMotionTargets.SCOPE_CHAPTER_ICON.equals(state.entityMotionEditorScope);
    }

    public static boolean isQuestDetailsOpen(TabletUiState state) {
        return state != null
                && state.entityMotionEditorOpen
                && (EntityMotionTargets.SCOPE_QUEST_DETAILS.equals(state.entityMotionEditorScope)
                || EntityMotionTargets.SCOPE_OBJECTIVE_ICON.equals(state.entityMotionEditorScope));
    }

    public static boolean isDragging(TabletUiState state) {
        return state != null && (state.entityMotionYawSliderDragging || state.entityMotionSpinSliderDragging);
    }

    public static boolean isMainCanvasHit(TabletUiState state, double localX, double localY) {
        return isMainCanvasOpen(state) && inside(localX, localY, state.entityMotionEditorX, state.entityMotionEditorY, state.entityMotionEditorW, state.entityMotionEditorH);
    }

    public static boolean isQuestDetailsHit(TabletUiState state, double mouseX, double mouseY) {
        if (!isQuestDetailsOpen(state)) {
            return false;
        }
        int x = state.questDetailsScreenX + state.entityMotionEditorX;
        int y = state.questDetailsScreenY + state.entityMotionEditorY;
        return inside(mouseX, mouseY, x, y, state.entityMotionEditorW, state.entityMotionEditorH);
    }

    public static boolean isChapterPanelHit(TabletUiState state, double localX, double localY) {
        return isChapterPanelOpen(state) && inside(localX, localY, state.entityMotionEditorX, state.entityMotionEditorY, state.entityMotionEditorW, state.entityMotionEditorH);
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
        EntityMotionPopup.render(modal, state, player, refresh, motion, state.questDetailsW, state.questDetailsH);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
