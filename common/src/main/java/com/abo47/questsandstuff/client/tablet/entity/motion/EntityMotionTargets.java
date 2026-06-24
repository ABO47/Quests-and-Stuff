package com.abo47.questsandstuff.client.tablet.entity.motion;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionModel;
import com.abo47.questsandstuff.client.tablet.quest.details.objective.QuestObjectiveEditActions;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;

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
        state.questDetails.entityMotionEditorOpen = true;
        state.questDetails.entityMotionEditorScope = scope;
        state.questDetails.entityMotionEditorGroup = group == null ? "" : group;
        state.questDetails.entityMotionEditorQuestId = questId == null ? "" : questId;
        state.questDetails.entityMotionEditorImageId = imageId == null ? "" : imageId;
        state.questDetails.entityMotionEditorX = x;
        state.questDetails.entityMotionEditorY = y;
        state.questDetails.entityMotionYawDraft = Integer.toString(image.entityYaw());
        state.questDetails.entityMotionSpinDraft = Integer.toString(image.entitySpinSpeed());
        resetTransientState(state);
    }

    static void openIcon(TabletUiState state, String scope, String group, String questId, String imageId, int x, int y, EntityIconMotion icon) {
        state.questDetails.entityMotionEditorOpen = true;
        state.questDetails.entityMotionEditorScope = scope;
        state.questDetails.entityMotionEditorGroup = group == null ? "" : group;
        state.questDetails.entityMotionEditorQuestId = questId == null ? "" : questId;
        state.questDetails.entityMotionEditorImageId = imageId == null ? "" : imageId;
        state.questDetails.entityMotionEditorX = x;
        state.questDetails.entityMotionEditorY = y;
        state.questDetails.entityMotionYawDraft = Integer.toString(icon.yaw());
        state.questDetails.entityMotionSpinDraft = Integer.toString(icon.spin());
        resetTransientState(state);
    }

    static EntityMotionValues mainCanvasMotion(TabletUiState state) {
        if (SCOPE_QUEST_ICON.equals(state.questDetails.entityMotionEditorScope)) {
            EntityIconMotion icon = currentQuestIconMotion(state.questDetails.entityMotionEditorQuestId);
            return icon.editable() ? new EntityMotionValues(icon.yaw(), icon.spin()) : null;
        }
        CanvasImageLayer image = CanvasLayerMutations.findCanvasImage(state, state.questDetails.entityMotionEditorGroup, state.questDetails.entityMotionEditorImageId);
        return isEditableEntity(image) ? new EntityMotionValues(image.entityYaw(), image.entitySpinSpeed()) : null;
    }

    static EntityMotionValues chapterMotion(TabletUiState state) {
        EntityIconMotion icon = currentChapterIconMotion(state.questDetails.entityMotionEditorGroup);
        return icon.editable() ? new EntityMotionValues(icon.yaw(), icon.spin()) : null;
    }

    static EntityMotionValues questDetailsMotion(TabletUiState state) {
        if (SCOPE_OBJECTIVE_ICON.equals(state.questDetails.entityMotionEditorScope)) {
            EntityIconMotion icon = currentObjectiveIconMotion(state.questDetails.entityMotionEditorQuestId, state.questDetails.entityMotionEditorImageId, objectiveMotionTask(state));
            return icon.editable() ? new EntityMotionValues(icon.yaw(), icon.spin()) : null;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(state.questDetails.entityMotionEditorQuestId));
        CanvasImageLayer image = model.image(state.questDetails.entityMotionEditorImageId);
        return isEditableEntity(image) ? new EntityMotionValues(image.entityYaw(), image.entitySpinSpeed()) : null;
    }

    static EntityMotionValues currentMotionValues(TabletUiState state) {
        if (state == null || !state.questDetails.entityMotionEditorOpen) {
            return null;
        }
        return switch (state.questDetails.entityMotionEditorScope) {
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
        int yaw = motion.yaw();
        int spin = parseDraft(state.questDetails.entityMotionSpinDraft, motion.spin(), CanvasImageLayer.MAX_ENTITY_SPIN_SPEED);
        switch (state.questDetails.entityMotionEditorScope) {
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
        state.questDetails.entityMotionYawDraft = Integer.toString(motion.yaw());
        state.questDetails.entityMotionSpinDraft = Integer.toString(motion.spin());
    }

    static void setDraft(TabletUiState state, boolean yaw, int value) {
        if (yaw) {
            state.questDetails.entityMotionYawDraft = Integer.toString(CanvasImageLayer.normalizeDegrees(value));
        } else {
            state.questDetails.entityMotionSpinDraft = Integer.toString(CanvasImageLayer.clampEntitySpinSpeed(value));
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
        return state != null && OBJECTIVE_TASK.equals(state.questDetails.entityMotionEditorGroup);
    }

    static String objectiveGroup(boolean task) {
        return task ? OBJECTIVE_TASK : OBJECTIVE_REWARD;
    }

    private static void resetTransientState(TabletUiState state) {
        state.questDetails.entityMotionFocusedField = "";
        state.questDetails.entityMotionYawSliderDragging = false;
        state.questDetails.entityMotionSpinSliderDragging = false;
        ContextMenuState.clearDeleteConfirm(state);
    }

    private static void applyCanvasMotion(TabletUiState state, int yaw, int spin, boolean sync) {
        CanvasImageLayer primary = CanvasLayerMutations.findCanvasImage(state, state.questDetails.entityMotionEditorGroup, state.questDetails.entityMotionEditorImageId);
        if (primary == null) {
            EntityMotionEditor.close(state);
            return;
        }
        CanvasLayerMutations.putCanvasImage(state, state.questDetails.entityMotionEditorGroup, primary.withEntityMotion(yaw, spin), sync);
        String batchRaw = state.questDetails.entityMotionEditorBatchImageIds;
        if (!batchRaw.isBlank()) {
            Set<String> applied = new HashSet<>();
            applied.add(state.questDetails.entityMotionEditorImageId);
            for (String id : batchRaw.split(",")) {
                id = id.trim();
                if (id.isBlank() || applied.contains(id)) continue;
                applied.add(id);
                CanvasImageLayer batch = CanvasLayerMutations.findCanvasImage(state, state.questDetails.entityMotionEditorGroup, id);
                if (batch != null) {
                    CanvasLayerMutations.putCanvasImage(state, state.questDetails.entityMotionEditorGroup, batch.withEntityMotion(yaw, spin), sync);
                }
            }
        }
    }

    private static void applyQuestIconMotion(Player player, TabletUiState state, int yaw, int spin, boolean sync) {
        EntityIconMotion icon = currentQuestIconMotion(state.questDetails.entityMotionEditorQuestId);
        if (!icon.editable()) {
            EntityMotionEditor.close(state);
            return;
        }
        String nextIcon = EntityPreviewRenderer.withEntityMotion(icon.icon(), yaw, spin);
        ClientQuestCache.setQuestIconLocal(state.questDetails.entityMotionEditorQuestId, nextIcon);
        if (sync) {
            EditorCommandClient.runQuestIconAction(player, state.questDetails.entityMotionEditorQuestId, nextIcon);
        }
    }

    private static void applyChapterIconMotion(Player player, TabletUiState state, int yaw, int spin, boolean sync) {
        EntityIconMotion icon = currentChapterIconMotion(state.questDetails.entityMotionEditorGroup);
        if (!icon.editable()) {
            EntityMotionEditor.close(state);
            return;
        }
        String nextIcon = EntityPreviewRenderer.withEntityMotion(icon.icon(), yaw, spin);
        ClientQuestCache.setGroupIconLocal(state.questDetails.entityMotionEditorGroup, nextIcon);
        if (sync) {
            EditorCommandClient.runGroupAction(player, state, "set_icon", state.questDetails.entityMotionEditorGroup, nextIcon, 0);
        }
    }

    private static void applyObjectiveIconMotion(Player player, TabletUiState state, int yaw, int spin, boolean sync) {
        EntityIconMotion icon = currentObjectiveIconMotion(state.questDetails.entityMotionEditorQuestId, state.questDetails.entityMotionEditorImageId, objectiveMotionTask(state));
        if (!icon.editable()) {
            EntityMotionEditor.close(state);
            return;
        }
        String nextIcon = EntityPreviewRenderer.withEntityMotion(icon.icon(), yaw, spin);
        QuestObjectiveEditActions.putObjectiveIcon(player, state.questDetails.entityMotionEditorQuestId, state.questDetails.entityMotionEditorImageId, nextIcon, objectiveMotionTask(state), sync);
    }

    private static void applyQuestDetailsMotion(Player player, TabletUiState state, int yaw, int spin, boolean sync) {
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(state.questDetails.entityMotionEditorQuestId));
        CanvasImageLayer detailsImage = model.image(state.questDetails.entityMotionEditorImageId);
        if (detailsImage == null) {
            EntityMotionEditor.close(state);
            return;
        }
        model.putImage(detailsImage.withEntityMotion(yaw, spin));
        if (sync) {
            QuestDetailsDescriptionModel.save(player, state.questDetails.entityMotionEditorQuestId, model);
        } else {
            QuestDetailsDescriptionModel.preview(state.questDetails.entityMotionEditorQuestId, model);
        }
    }
}
