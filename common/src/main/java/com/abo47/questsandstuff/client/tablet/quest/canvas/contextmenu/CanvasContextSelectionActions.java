package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardAsset;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmoMenus;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionSet;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCanvasCommandClient;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.entity.variant.EntityVariantCatalog;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.model.ModelAssetPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class CanvasContextSelectionActions {
    private CanvasContextSelectionActions() {
    }

    static void addSelectionActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedGroup) {
        if (state.contextMenu.contextMenuTarget != ContextMenuTarget.SELECTION || selectedGroup.isBlank()) {
            return;
        }
        int totalCount = CanvasSelectionActions.totalCanvasSelectionCount(state);
        List<CanvasContextMenuController.EdgeRef> connectedEdges = CanvasContextMenuController.selectedConnectedEdges(state, selectedGroup);
        boolean hasEdges = !connectedEdges.isEmpty();

        if (totalCount > 0) {
            actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.save_as_blueprint"), "scroll", ModColors.INTERACTIVE, () -> {
                boolean saved = CanvasBlueprintController.saveSelectionWithNotice(canvasViewport, state, state.contextMenu.contextLastClickX, state.contextMenu.contextLastClickY);
                ContextMenuState.close(state);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] context save_as_blueprint count={} saved={}", totalCount, saved);
                canvasViewport.refresh();
            }));
            Set<String> entityImageIds = selectedEntityImageIds(state, selectedGroup);
            if (totalCount > 1
                    && state.canvas.canvasSelection.questIds().isEmpty()
                    && state.canvas.canvasSelection.textIds().isEmpty()
                    && state.canvas.canvasSelection.ecIds().isEmpty()
                    && !entityImageIds.isEmpty()
                    && entityImageIds.size() == totalCount) {
                String batchTarget = ModalTargets.canvasEntityChangeBatch(selectedGroup, entityImageIds.toArray(new String[0]));
                actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.change_entity"), "entity", ModColors.INTERACTIVE, () -> {
                    int x = state.canvas.canvasImageLogicalX;
                    int y = state.canvas.canvasImageLogicalY;
                    ModalOpenActions.openCanvasEntityPicker(state, batchTarget, x, y);
                    ContextMenuState.close(state);
                    ContextMenuState.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_change_entity images={}", entityImageIds.size());
                    canvasViewport.refresh();
                }));
            }
            Set<String> questIds = state.canvas.canvasSelection.questIds();
            if (totalCount > 1 && !questIds.isEmpty()) {
                List<String> targets = new ArrayList<>(questIds);
                CompoundTag first = firstQuest(targets);
                actions.add(ContextActions.promoted(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_CHANGE_QUEST_BACKGROUND), "background", ModColors.INTERACTIVE, () -> {
                    ContextMenuState.closeExclusiveSubmenus(state);
                    ModalOpenActions.openBatchQuestBackgroundPicker(
                            state,
                            targets,
                            first.getString("quest_background"),
                            first.getBoolean("quest_background_grayscale")
                    );
                    ContextMenuState.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_quest_background quests={}", targets.size());
                    canvasViewport.refresh();
                }));
                actions.add(ContextActions.promoted(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_CHANGE_COMPLETION_HUD_BACKGROUND), "completion_hud_background", ModColors.INTERACTIVE, () -> {
                    ContextMenuState.closeExclusiveSubmenus(state);
                    ModalOpenActions.openBatchQuestCompletionHudBackgroundPicker(
                            state,
                            targets,
                            first.getString("completion_hud_background")
                    );
                    ContextMenuState.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_completion_hud_background quests={}", targets.size());
                    canvasViewport.refresh();
                }));
            }
        }
        if (totalCount > 1) {
            ContextMenuState.closeExclusiveSubmenus(state);
            if (hasEdges) {
                actions.add(new ContextAction(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_CONNECTION_COLOR), "style_color", ModColors.INTERACTIVE, () -> {
                    CanvasContextMenuController.EdgeRef first = connectedEdges.get(0);
                    int color = CanvasRenderer.connectionColor(state, selectedGroup, first.prerequisiteId(), first.questId());
                    ModalOpenActions.openColorPicker(state, ModalTargets.connectionSelection(selectedGroup), color);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=patch_connection_colors group={} edges={}", selectedGroup, connectedEdges.size());
                    canvasViewport.refresh();
                }));
                actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_connection_texture"), "connect", ModColors.INTERACTIVE, () -> {
                    ModalOpenActions.openConnectionTexturePicker(state, ModalTargets.connectionSelection(selectedGroup));
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=patch_connection_textures group={} edges={}", selectedGroup, connectedEdges.size());
                    canvasViewport.refresh();
                }));
                if (selectionHasConnectionTexture(state, selectedGroup, connectedEdges)) {
                    actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.remove_connection_texture"), "delete", ModColors.WARNING, () -> {
                        ContextMenuState.clearDeleteConfirm(state);
                        for (var edge : connectedEdges) {
                            String prereq = edge.prerequisiteId();
                            String quest = edge.questId();
                            boolean isEc = ConnectionRenderer.isEcId(state, selectedGroup, prereq) || ConnectionRenderer.isEcId(state, selectedGroup, quest);
                            if (isEc) {
                                EditorCanvasCommandClient.runEcConnectionTextureAction(state, prereq, quest, "");
                            } else {
                                EditorCanvasCommandClient.runConnectionTextureAction(player, quest, prereq, "");
                                ConnectionRenderer.setConnectionTexture(state, selectedGroup, prereq, quest, "");
                            }
                        }
                        QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_connection_textures group={} edges={}", selectedGroup, connectedEdges.size());
                        canvasViewport.refresh();
                    }));
                }
            }
            addBatchElementActions(actions, canvasViewport, state, selectedGroup);
            if (selectionSupportsGizmo(state, selectedGroup)) {
                CanvasTransformGizmoMenus.addModeActions(actions, state, canvasViewport::refresh);
            }
            addBatchRemainingVisualActions(actions, canvasViewport, state, player);
            addBatchBehaviorActions(actions, canvasViewport, state, player);
            if (CanvasGridFitController.canFitSelectionToGrid(state, selectedGroup, canvasViewport.cardLookup())) {
                actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "fit_grid", ModColors.INTERACTIVE, () -> {
                    boolean changed = CanvasGridFitController.fitSelectionToGrid(player, state, selectedGroup, canvasViewport.cardLookup());
                    ContextMenuState.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=selection count={} changed={}", totalCount, changed);
                    canvasViewport.refresh();
                }));
            }
            addSelectionAlignmentActions(actions, canvasViewport, state, player);
            addSelectionLayerActions(actions, canvasViewport, state, selectedGroup);
            addBatchResetQuest(actions, canvasViewport, state, player);
        }
        addSelectionCopyAndDeleteActions(actions, canvasViewport, state, player);
    }

    private static boolean selectionSupportsGizmo(TabletUiState state, String selectedGroup) {
        if (!state.canvas.canvasSelection.questIds().isEmpty()) return false;
        if (!state.canvas.canvasSelection.textIds().isEmpty()) return false;
        if (!state.canvas.canvasSelection.ecIds().isEmpty()) return false;
        boolean hasGizmoImage = false;
        for (CanvasImageLayer image : state.canvas.canvasImagesByGroup.getOrDefault(selectedGroup, List.of())) {
            if (CanvasSelectionActions.isImageSelected(state, image.id())) {
                if (!CanvasTransformGizmo.supports(image.asset())) {
                    return false;
                }
                hasGizmoImage = true;
            }
        }
        return hasGizmoImage;
    }

    private static void addBatchRemainingVisualActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player) {
        Set<String> questIds = state.canvas.canvasSelection.questIds();
        if (questIds.size() <= 1) {
            return;
        }
        List<String> targets = new ArrayList<>(questIds);
        CompoundTag first = firstQuest(targets);
        if (selectionHasQuestBackground(targets)) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.remove_background"), "delete", ModColors.WARNING, () -> {
                ContextMenuState.clearDeleteConfirm(state);
                for (String questId : targets) {
                    EditorQuestCommandClient.setQuestBackground(player, questId, QuestDisplay.DEFAULT_QUEST_BACKGROUND, false);
                }
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_remove_quest_background quests={}", targets.size());
                canvasViewport.refresh();
            }));
        }
        if (selectionHasCompletionHudBackground(targets)) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.remove_completion_hud_background"), "delete", ModColors.WARNING, () -> {
                EditorQuestCommandClient.setQuestCompletionHudBackground(player, new java.util.LinkedHashSet<>(targets), QuestDisplay.DEFAULT_COMPLETION_HUD_BACKGROUND);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_remove_completion_hud_background quests={}", targets.size());
                canvasViewport.refresh();
            }));
        }
        actions.add(ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_COMPLETION_SOUND), "audio-lines", ModColors.INTERACTIVE, List.of(
                ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.use_game_sound"), "audio-lines", ModColors.INTERACTIVE, () -> {
                    ModalOpenActions.openBatchQuestGameSoundPicker(state, targets, first.getString("completion_sound"));
                    ContextMenuState.closeExclusiveSubmenus(state);
                    ContextMenuState.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_completion_sound_game quests={}", targets.size());
                    canvasViewport.refresh();
                }),
                ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.use_custom_sound"), "audio-lines", ModColors.INTERACTIVE, () -> {
                    ModalOpenActions.openBatchQuestCustomCompletionSoundPicker(state, targets, first.getString("completion_sound"));
                    ContextMenuState.closeExclusiveSubmenus(state);
                    ContextMenuState.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_completion_sound_custom quests={}", targets.size());
                    canvasViewport.refresh();
                })
        )));
    }

    private static void addBatchBehaviorActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player) {
        Set<String> questIds = state.canvas.canvasSelection.questIds();
        if (questIds.size() <= 1) {
            return;
        }
        List<String> targets = new ArrayList<>(questIds);
        CompoundTag first = firstQuest(targets);
        boolean repeatable = first.getBoolean("repeatable");
        String hiddenMode = first.getString("hidden_mode");
        String locked = QuestVisibilityMode.LOCKED.serializedName();
        String prerequisitesVisible = QuestVisibilityMode.PREREQUISITES_VISIBLE.serializedName();
        boolean lockUntilUnlocked = locked.equals(hiddenMode);
        boolean hidden = first.getBoolean("visual_hidden");
        List<ContextAction> behaviorActions = new ArrayList<>();
        behaviorActions.add(new ContextAction(
                CanvasContextMenuController.tr(repeatable ? QuestVocabulary.CONTEXT_MAKE_QUEST_NOT_REPEATABLE : QuestVocabulary.CONTEXT_MAKE_QUEST_REPEATABLE),
                repeatable ? "repeat-off" : "repeat",
                repeatable ? ModColors.SUCCESS : ModColors.INTERACTIVE,
                () -> {
                    ContextMenuState.clearDeleteConfirm(state);
                    for (String questId : targets) {
                        EditorQuestCommandClient.setQuestRepeatable(player, questId, !repeatable);
                    }
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_behavior_repeatable quests={}", targets.size());
                    canvasViewport.refresh();
                }));
        behaviorActions.add(new ContextAction(
                CanvasContextMenuController.tr(lockUntilUnlocked ? QuestVocabulary.CONTEXT_SHOW_QUEST_BEFORE_UNLOCKED : QuestVocabulary.CONTEXT_LOCK_QUEST_UNTIL_UNLOCKED),
                lockUntilUnlocked ? "unlock_quest" : "lock_quest",
                lockUntilUnlocked ? ModColors.SUCCESS : ModColors.INTERACTIVE,
                () -> {
                    ContextMenuState.clearDeleteConfirm(state);
                    for (String questId : targets) {
                        EditorQuestCommandClient.setQuestHiddenMode(player, questId, lockUntilUnlocked ? prerequisitesVisible : locked);
                    }
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_behavior_lock quests={}", targets.size());
                    canvasViewport.refresh();
                }));
        behaviorActions.add(new ContextAction(
                CanvasContextMenuController.tr(hidden ? QuestVocabulary.CONTEXT_REVEAL_QUEST : QuestVocabulary.CONTEXT_HIDE_QUEST_UNTIL_UNLOCKED),
                hidden ? "eye" : "eye-off",
                hidden ? ModColors.SUCCESS : ModColors.WARNING,
                () -> {
                    ContextMenuState.clearDeleteConfirm(state);
                    for (String questId : targets) {
                        EditorQuestCommandClient.setQuestVisualHidden(player, questId, !hidden);
                    }
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_behavior_hide quests={}", targets.size());
                    canvasViewport.refresh();
                }));
        actions.add(ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_BEHAVIOR), "wrench", ModColors.INTERACTIVE, behaviorActions));
    }

    private static void addBatchResetQuest(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player) {
        Set<String> questIds = state.canvas.canvasSelection.questIds();
        if (questIds.size() <= 1) {
            return;
        }
        List<String> targets = new ArrayList<>(questIds);
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.reset_quest"), "reset_quest", ModColors.WARNING, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            for (String questId : targets) {
                EditorQuestCommandClient.resetQuestProgress(player, questId);
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_reset_quest quests={}", targets.size());
            canvasViewport.refresh();
        }));
    }

    private static boolean selectionHasQuestBackground(List<String> questIds) {
        for (String questId : questIds) {
            CompoundTag quest = ClientQuestCache.quest(questId);
            if (quest != null && !QuestDisplay.DEFAULT_QUEST_BACKGROUND.equals(QuestDisplay.normalizeQuestBackground(quest.getString("quest_background")))) {
                return true;
            }
        }
        return false;
    }

    private static CompoundTag firstQuest(List<String> questIds) {
        for (String questId : questIds) {
            CompoundTag quest = ClientQuestCache.quest(questId);
            if (quest != null && !quest.isEmpty()) {
                return quest;
            }
        }
        return new CompoundTag();
    }

    private static boolean selectionHasCompletionHudBackground(List<String> questIds) {
        for (String questId : questIds) {
            CompoundTag quest = ClientQuestCache.quest(questId);
            if (quest != null && !QuestDisplay.DEFAULT_COMPLETION_HUD_BACKGROUND.equals(QuestDisplay.normalizeCompletionHudBackground(quest.getString("completion_hud_background")))) {
                return true;
            }
        }
        return false;
    }

    private static boolean selectionHasConnectionTexture(TabletUiState state, String group, List<CanvasContextMenuController.EdgeRef> edges) {
        for (var edge : edges) {
            String prereq = edge.prerequisiteId();
            String quest = edge.questId();
            boolean isEc = ConnectionRenderer.isEcId(state, group, prereq) || ConnectionRenderer.isEcId(state, group, quest);
            String texture = isEc
                    ? ConnectionRenderer.ecConnectionTexture(state, group, prereq, quest)
                    : ConnectionRenderer.connectionTexture(state, group, prereq, quest);
            if (!texture.isBlank()) return true;
        }
        return false;
    }

    private static void addSelectionLayerActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup) {
        CanvasSelectionSet selection = CanvasSelectionSet.current(state);
        if (selection.layerKeys().isEmpty()) {
            return;
        }
        actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.bring_to_front"), "up", ModColors.INTERACTIVE, () -> {
            CanvasLayerMutations.moveCanvasLayers(state, selectedGroup, selection.layerKeys(), true);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=bring_to_front target=selection count={}", selection.size());
            canvasViewport.refresh();
        }));
        actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.send_to_back"), "down", ModColors.TEXT_MUTED, () -> {
            CanvasLayerMutations.moveCanvasLayers(state, selectedGroup, selection.layerKeys(), false);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=send_to_back target=selection count={}", selection.size());
            canvasViewport.refresh();
        }));
    }

    private static void addSelectionCopyAndDeleteActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player) {
        if (CanvasContextMenuSupport.canCopyContext(canvasViewport, state)) {
            actions.add(ContextActions.copy(() -> {
                CanvasContextMenuSupport.copyContextToClipboard(canvasViewport, state);
                ContextMenuState.clearDeleteConfirm(state);
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextDeleteController.canDeleteContext(state)) {
            String deleteKey = CanvasContextDeleteController.deleteConfirmKey(state);
            actions.add(ContextActions.delete(state, deleteKey, TabletVocabulary.text(TabletVocabulary.COMMON_DELETE), () -> {
                CanvasContextDeleteController.runDeleteAction(player, state);
                canvasViewport.refresh();
            }));
        }
    }

    private static Set<String> selectedEntityImageIds(TabletUiState state, String selectedGroup) {
        Set<String> ids = new LinkedHashSet<>();
        String primary = state.canvas.canvasSelection.primaryImageId();
        if (!primary.isBlank()) ids.add(primary);
        ids.addAll(state.canvas.canvasSelection.imageIds());
        if (ids.isEmpty()) return ids;
        List<CanvasImageLayer> images = state.canvas.canvasImagesByGroup.getOrDefault(selectedGroup, List.of());
        Set<String> entityIds = new LinkedHashSet<>();
        for (CanvasImageLayer image : images) {
            if (ids.contains(image.id()) && EntityPreviewRenderer.isEntityAsset(image.asset())) {
                entityIds.add(image.id());
            }
        }
        return entityIds;
    }

    private static void addBatchElementActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup) {
        Set<String> questIds = state.canvas.canvasSelection.questIds();
        Set<String> imageIds = state.canvas.canvasSelection.imageIds();
        Set<String> textIds = state.canvas.canvasSelection.textIds();
        Set<String> ecIds = state.canvas.canvasSelection.ecIds();

        boolean hasQuests = !questIds.isEmpty();
        boolean hasImages = !imageIds.isEmpty();
        boolean hasTexts = !textIds.isEmpty();
        boolean hasEcs = !ecIds.isEmpty();

        if (hasQuests) return;

        if (hasImages) {
            addBatchImageOnlyActions(actions, canvasViewport, state, selectedGroup, imageIds);
        }
        if (hasTexts) {
            addBatchTextOnlyActions(actions, canvasViewport, state, selectedGroup, textIds);
        }
        if (hasEcs && (!hasImages || selectedEntityImageIds(state, selectedGroup).isEmpty())) {
            addBatchEcOnlyActions(actions, canvasViewport, state, selectedGroup, ecIds);
        }
    }

    private static void addBatchImageOnlyActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup, Set<String> imageIds) {
        String primaryId = state.canvas.canvasSelection.primaryImageId();
        if (primaryId.isBlank()) {
            primaryId = imageIds.iterator().next();
        }
        String finalPrimaryId = primaryId;
        CanvasImageLayer primary = CanvasLayerMutations.findCanvasImage(state, selectedGroup, finalPrimaryId);
        if (primary == null) return;

        boolean isRecipeCard = CanvasRecipeCardAsset.isRecipeCardAsset(primary.asset());
        boolean isEntity = EntityPreviewRenderer.isEntityAsset(primary.asset());
        boolean isItem = ModelAssetPreviewRenderer.isItemAsset(primary.asset());
        boolean isBlock = ModelAssetPreviewRenderer.isBlockModelAsset(primary.asset());
        boolean allEntitySelection = state.canvas.canvasSelection.questIds().isEmpty()
                && state.canvas.canvasSelection.textIds().isEmpty()
                && state.canvas.canvasSelection.ecIds().isEmpty()
                && !imageIds.isEmpty()
                && selectedEntityImageIds(state, selectedGroup).size() == imageIds.size();

        if (isRecipeCard) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_recipe"), "recipe", ModColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasRecipePicker(state, ModalTargets.canvasRecipeChange(selectedGroup, finalPrimaryId), primary.x(), primary.y());
                ContextMenuState.close(state);
                canvasViewport.refresh();
            }));
        } else if (isEntity) {
            if (!allEntitySelection) {
                actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_entity"), "entity", ModColors.INTERACTIVE, () -> {
                    ModalOpenActions.openCanvasEntityPicker(state, ModalTargets.canvasEntityChange(selectedGroup, finalPrimaryId), primary.x(), primary.y());
                    ContextMenuState.close(state);
                    canvasViewport.refresh();
                }));
            }
            Set<String> batchEntityIds = selectedEntityImageIds(state, selectedGroup);
            batchEntityIds.remove(finalPrimaryId);
            state.questDetails.entityMotionEditorBatchImageIds = String.join(",", batchEntityIds);
            String entityId = EntityPreviewRenderer.entityId(primary.asset());
            String variantTarget = ModalTargets.canvasImage(selectedGroup, finalPrimaryId);
            if (EntityVariantCatalog.hasVariants(entityId)) {
                actions.add(ContextActions.changeVariant(() -> {
                    ModalOpenActions.openEntityVariantPicker(state, variantTarget, primary.asset());
                    ContextMenuState.clearDeleteConfirm(state);
                    canvasViewport.refresh();
                }));
            }
            actions.add(ContextActions.promoted(
                    TabletVocabulary.text(QuestVocabulary.CONTEXT_EDIT_MOTION),
                    "motion", ModColors.INTERACTIVE, () -> {
                        ContextMenuState.clearDeleteConfirm(state);
                        EntityMotionEditor.openMainCanvas(state, selectedGroup, finalPrimaryId, state.contextMenu.contextMenuX, state.contextMenu.contextMenuY);
                        canvasViewport.refresh();
                    }));
        } else if (isItem) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_item"), "icon", ModColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasItemPicker(state, ModalTargets.canvasItemChange(selectedGroup, finalPrimaryId), primary.x(), primary.y());
                ContextMenuState.close(state);
                canvasViewport.refresh();
            }));
        } else if (isBlock) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_block"), "box", ModColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasBlockPicker(state, ModalTargets.canvasBlockChange(selectedGroup, finalPrimaryId), primary.x(), primary.y());
                ContextMenuState.close(state);
                canvasViewport.refresh();
            }));
        }
    }

    private static void addBatchEcOnlyActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup, Set<String> ecIds) {
        String primaryId = state.canvas.canvasSelection.primaryEcId();
        if (primaryId.isBlank()) {
            primaryId = ecIds.iterator().next();
        }
        String finalPrimaryId = primaryId;
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, selectedGroup, finalPrimaryId);
        if (ec == null) return;

        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_ec_background"), "background", ModColors.INTERACTIVE, () -> {
            ModalOpenActions.openEcBackgroundPicker(state, selectedGroup, finalPrimaryId, ec.background());
            ContextMenuState.close(state);
            canvasViewport.refresh();
        }));
        if (!ec.background().isBlank()) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.remove_background"), "delete", ModColors.WARNING, () -> {
                for (String batchEcId : ecIds) {
                    CanvasExclusiveChoice batchEc = CanvasLayerMutations.findCanvasExclusiveChoice(state, selectedGroup, batchEcId);
                    if (batchEc != null && !batchEc.background().isBlank()) {
                        CanvasLayerMutations.putCanvasExclusiveChoice(state, selectedGroup, batchEc.withBackground(""));
                        CanvasLayerMutations.persistCanvasExclusiveChoice(state, selectedGroup, batchEcId);
                    }
                }
                ContextMenuState.close(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_remove_ec_background ecs={}", ecIds);
                canvasViewport.refresh();
            }));
        }
    }

    private static void addBatchTextOnlyActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup, Set<String> textIds) {
        String primaryId = state.canvas.canvasSelection.primaryTextId();
        if (primaryId.isBlank()) {
            primaryId = textIds.iterator().next();
        }
        String finalPrimaryId = primaryId;
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.menu.text_style"), "style", ModColors.INTERACTIVE, false, () -> {
            TextStyleSession.openMainCanvas(state, finalPrimaryId);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_text_style id={}", finalPrimaryId);
            canvasViewport.refresh();
        }));
    }

    private static void addSelectionAlignmentActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player) {
        actions.add(ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_ALIGN), "align-center-horizontal", ModColors.INTERACTIVE, List.of(
                ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.align_horizontal_center"), "align-center-horizontal", ModColors.INTERACTIVE, () -> {
                    boolean changed = CanvasSelectionActions.alignSelectedToCanvasCenter(player, state, false);
                    ContextMenuState.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=align_horizontal_center target=selection count={} changed={}", CanvasSelectionActions.totalCanvasSelectionCount(state), changed);
                    canvasViewport.refresh();
                }),
                ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.align_vertical_center"), "align-center-vertical", ModColors.INTERACTIVE, () -> {
                    boolean changed = CanvasSelectionActions.alignSelectedToCanvasCenter(player, state, true);
                    ContextMenuState.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=align_vertical_center target=selection count={} changed={}", CanvasSelectionActions.totalCanvasSelectionCount(state), changed);
                    canvasViewport.refresh();
                })
        )));
    }
}
