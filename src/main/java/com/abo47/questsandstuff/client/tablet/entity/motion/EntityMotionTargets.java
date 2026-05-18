package com.abo47.questsandstuff.client.tablet.entity.motion;

import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.details.description.QuestDetailsDescriptionModel;
import com.abo47.questsandstuff.client.tablet.details.objective.QuestObjectiveEditActions;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

final class EntityMotionTargets {
    static final String SCOPE_CANVAS = "canvas";
    static final String SCOPE_QUEST_DETAILS = "quest_details";
    static final String SCOPE_CHAPTER_ICON = "chapter_icon";
    static final String SCOPE_QUEST_ICON = "quest_icon";
    static final String SCOPE_OBJECTIVE_ICON = "objective_icon";

    private static final String OBJECTIVE_TASK = "task";
    private static final String OBJECTIVE_REWARD = "reward";

    private EntityMotionTargets() {
    }

    static void openImage(TabletUiState state, String scope, String group, String questId, String imageId, int x, int y, CanvasImageLayer image) {
        state.entityMotionEditorOpen = true;
        state.entityMotionEditorScope = scope;
        state.entityMotionEditorGroup = group == null ? "" : group;
        state.entityMotionEditorQuestId = questId == null ? "" : questId;
        state.entityMotionEditorImageId = imageId == null ? "" : imageId;
        state.entityMotionEditorX = x;
        state.entityMotionEditorY = y;
        state.entityMotionYawDraft = Integer.toString(image.entityYaw());
        state.entityMotionSpinDraft = Integer.toString(image.entitySpinSpeed());
        resetTransientState(state);
    }

    static void openIcon(TabletUiState state, String scope, String group, String questId, String imageId, int x, int y, EntityIconMotion icon) {
        state.entityMotionEditorOpen = true;
        state.entityMotionEditorScope = scope;
        state.entityMotionEditorGroup = group == null ? "" : group;
        state.entityMotionEditorQuestId = questId == null ? "" : questId;
        state.entityMotionEditorImageId = imageId == null ? "" : imageId;
        state.entityMotionEditorX = x;
        state.entityMotionEditorY = y;
        state.entityMotionYawDraft = Integer.toString(icon.yaw());
        state.entityMotionSpinDraft = Integer.toString(icon.spin());
        resetTransientState(state);
    }

    static EntityMotionValues mainCanvasMotion(TabletUiState state) {
        if (SCOPE_QUEST_ICON.equals(state.entityMotionEditorScope)) {
            EntityIconMotion icon = currentQuestIconMotion(state.entityMotionEditorQuestId);
            return icon.editable() ? new EntityMotionValues(icon.yaw(), icon.spin()) : null;
        }
        CanvasImageLayer image = CanvasRenderer.findCanvasImage(state, state.entityMotionEditorGroup, state.entityMotionEditorImageId);
        return isEditableEntity(image) ? new EntityMotionValues(image.entityYaw(), image.entitySpinSpeed()) : null;
    }

    static EntityMotionValues chapterMotion(TabletUiState state) {
        EntityIconMotion icon = currentChapterIconMotion(state.entityMotionEditorGroup);
        return icon.editable() ? new EntityMotionValues(icon.yaw(), icon.spin()) : null;
    }

    static EntityMotionValues questDetailsMotion(TabletUiState state) {
        if (SCOPE_OBJECTIVE_ICON.equals(state.entityMotionEditorScope)) {
            EntityIconMotion icon = currentObjectiveIconMotion(state.entityMotionEditorQuestId, state.entityMotionEditorImageId, objectiveMotionTask(state));
            return icon.editable() ? new EntityMotionValues(icon.yaw(), icon.spin()) : null;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(state.entityMotionEditorQuestId));
        CanvasImageLayer image = model.image(state.entityMotionEditorImageId);
        return isEditableEntity(image) ? new EntityMotionValues(image.entityYaw(), image.entitySpinSpeed()) : null;
    }

    static EntityMotionValues currentMotionValues(TabletUiState state) {
        if (state == null || !state.entityMotionEditorOpen) {
            return null;
        }
        return switch (state.entityMotionEditorScope) {
            case SCOPE_CANVAS, SCOPE_QUEST_ICON -> mainCanvasMotion(state);
            case SCOPE_CHAPTER_ICON -> chapterMotion(state);
            case SCOPE_QUEST_DETAILS, SCOPE_OBJECTIVE_ICON -> questDetailsMotion(state);
            default -> null;
        };
    }

    static void applyMotion(Player player, TabletUiState state, boolean sync) {
        EntityMotionValues motion = currentMotionValues(state);
        if (motion == null) {
            EntityMotionEditor.close(state);
            return;
        }
        int yaw = parseDraft(state.entityMotionYawDraft, motion.yaw(), 359);
        int spin = parseDraft(state.entityMotionSpinDraft, motion.spin(), CanvasImageLayer.MAX_ENTITY_SPIN_SPEED);
        switch (state.entityMotionEditorScope) {
            case SCOPE_CANVAS -> applyCanvasMotion(state, yaw, spin, sync);
            case SCOPE_QUEST_ICON -> applyQuestIconMotion(player, state, yaw, spin, sync);
            case SCOPE_CHAPTER_ICON -> applyChapterIconMotion(player, state, yaw, spin, sync);
            case SCOPE_OBJECTIVE_ICON -> applyObjectiveIconMotion(player, state, yaw, spin, sync);
            default -> applyQuestDetailsMotion(player, state, yaw, spin, sync);
        }
    }

    static void resetDrafts(TabletUiState state, EntityMotionValues motion) {
        if (state == null || motion == null) {
            return;
        }
        state.entityMotionYawDraft = Integer.toString(motion.yaw());
        state.entityMotionSpinDraft = Integer.toString(motion.spin());
    }

    static void setDraft(TabletUiState state, boolean yaw, int value) {
        if (yaw) {
            state.entityMotionYawDraft = Integer.toString(CanvasImageLayer.normalizeDegrees(value));
        } else {
            state.entityMotionSpinDraft = Integer.toString(CanvasImageLayer.clampEntitySpinSpeed(value));
        }
    }

    static int parseDraft(String value, int fallback, int max) {
        try {
            return Math.max(0, Math.min(max, Integer.parseInt(value == null ? "" : value.trim())));
        } catch (NumberFormatException ignored) {
            return Math.max(0, Math.min(max, fallback));
        }
    }

    static boolean isEditableEntity(CanvasImageLayer image) {
        return image != null && EntityPreviewRenderer.isEntityAsset(image.asset());
    }

    static EntityIconMotion currentQuestIconMotion(String questId) {
        CompoundTag quest = ClientQuestCache.quest(questId);
        String icon = quest == null ? "" : quest.getString("icon");
        if (!EntityPreviewRenderer.isEntityAsset(icon)) {
            return new EntityIconMotion(icon, 0, 0, false);
        }
        return new EntityIconMotion(icon, EntityPreviewRenderer.entityYaw(icon), EntityPreviewRenderer.entitySpinSpeed(icon), true);
    }

    static EntityIconMotion currentChapterIconMotion(String chapter) {
        String icon = ClientQuestCache.groupIcon(chapter);
        if (!EntityPreviewRenderer.isEntityAsset(icon)) {
            return new EntityIconMotion(icon, 0, 0, false);
        }
        return new EntityIconMotion(icon, EntityPreviewRenderer.entityYaw(icon), EntityPreviewRenderer.entitySpinSpeed(icon), true);
    }

    static EntityIconMotion currentObjectiveIconMotion(String questId, String objectiveId, boolean task) {
        String icon = QuestObjectiveEditActions.objectiveIcon(questId, objectiveId, task);
        if (!EntityPreviewRenderer.isEntityAsset(icon)) {
            return new EntityIconMotion(icon, 0, 0, false);
        }
        return new EntityIconMotion(icon, EntityPreviewRenderer.entityYaw(icon), EntityPreviewRenderer.entitySpinSpeed(icon), true);
    }

    static boolean objectiveMotionTask(TabletUiState state) {
        return state != null && OBJECTIVE_TASK.equals(state.entityMotionEditorGroup);
    }

    static String objectiveGroup(boolean task) {
        return task ? OBJECTIVE_TASK : OBJECTIVE_REWARD;
    }

    private static void resetTransientState(TabletUiState state) {
        state.entityMotionFocusedField = "";
        state.entityMotionYawSliderDragging = false;
        state.entityMotionSpinSliderDragging = false;
        state.contextDeleteConfirmKey = "";
    }

    private static void applyCanvasMotion(TabletUiState state, int yaw, int spin, boolean sync) {
        CanvasImageLayer image = CanvasRenderer.findCanvasImage(state, state.entityMotionEditorGroup, state.entityMotionEditorImageId);
        if (image == null) {
            EntityMotionEditor.close(state);
            return;
        }
        CanvasRenderer.putCanvasImage(state, state.entityMotionEditorGroup, image.withEntityMotion(yaw, spin), sync);
    }

    private static void applyQuestIconMotion(Player player, TabletUiState state, int yaw, int spin, boolean sync) {
        EntityIconMotion icon = currentQuestIconMotion(state.entityMotionEditorQuestId);
        if (!icon.editable()) {
            EntityMotionEditor.close(state);
            return;
        }
        String nextIcon = EntityPreviewRenderer.withEntityMotion(icon.icon(), yaw, spin);
        ClientQuestCache.setQuestIconLocal(state.entityMotionEditorQuestId, nextIcon);
        if (sync) {
            EditorCommandClient.runQuestIconAction(player, state.entityMotionEditorQuestId, nextIcon);
        }
    }

    private static void applyChapterIconMotion(Player player, TabletUiState state, int yaw, int spin, boolean sync) {
        EntityIconMotion icon = currentChapterIconMotion(state.entityMotionEditorGroup);
        if (!icon.editable()) {
            EntityMotionEditor.close(state);
            return;
        }
        String nextIcon = EntityPreviewRenderer.withEntityMotion(icon.icon(), yaw, spin);
        ClientQuestCache.setGroupIconLocal(state.entityMotionEditorGroup, nextIcon);
        if (sync) {
            EditorCommandClient.runGroupAction(player, state, "set_icon", state.entityMotionEditorGroup, nextIcon, 0);
        }
    }

    private static void applyObjectiveIconMotion(Player player, TabletUiState state, int yaw, int spin, boolean sync) {
        EntityIconMotion icon = currentObjectiveIconMotion(state.entityMotionEditorQuestId, state.entityMotionEditorImageId, objectiveMotionTask(state));
        if (!icon.editable()) {
            EntityMotionEditor.close(state);
            return;
        }
        String nextIcon = EntityPreviewRenderer.withEntityMotion(icon.icon(), yaw, spin);
        QuestObjectiveEditActions.putObjectiveIcon(player, state.entityMotionEditorQuestId, state.entityMotionEditorImageId, nextIcon, objectiveMotionTask(state), sync);
    }

    private static void applyQuestDetailsMotion(Player player, TabletUiState state, int yaw, int spin, boolean sync) {
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(state.entityMotionEditorQuestId));
        CanvasImageLayer detailsImage = model.image(state.entityMotionEditorImageId);
        if (detailsImage == null) {
            EntityMotionEditor.close(state);
            return;
        }
        model.putImage(detailsImage.withEntityMotion(yaw, spin));
        if (sync) {
            QuestDetailsDescriptionModel.save(player, state.entityMotionEditorQuestId, model);
        } else {
            QuestDetailsDescriptionModel.preview(state.entityMotionEditorQuestId, model);
        }
    }
}
