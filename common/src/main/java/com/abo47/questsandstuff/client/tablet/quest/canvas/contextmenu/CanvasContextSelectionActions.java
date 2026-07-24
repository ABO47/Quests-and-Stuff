package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.entity.variant.EntityVariantCatalog;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.preview.ModelAssetPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardAsset;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmoMenus;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionSet;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCanvasCommandClient;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;

final class CanvasContextSelectionActions {
    private CanvasContextSelectionActions() {
    }

    static void addSelectionActions(ContextMenuSections sections, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedChapter) {
        if (state.contextMenu.contextMenuTarget != ContextMenuTarget.SELECTION || selectedChapter.isBlank()) {
            return;
        }
        int totalCount = CanvasSelectionActions.totalCanvasSelectionCount(state);
        List<CanvasContextMenuController.ConnectionRef> connectedConnections = CanvasContextMenuController.selectedConnections(state, selectedChapter);
        boolean hasConnections = !connectedConnections.isEmpty();

        if (totalCount > 0) {
            sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.save_as_blueprint"), "scroll", TabletColors.INTERACTIVE, () -> {
                boolean saved = CanvasBlueprintController.saveSelectionWithNotice(canvasViewport, state, state.contextMenu.contextLastClickX, state.contextMenu.contextLastClickY);
                ContextMenuController.close(state);
                ContextMenuController.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] context save_as_blueprint count={} saved={}", totalCount, saved);
                canvasViewport.refresh();
            }));
            Set<String> entityImageIds = selectedEntityImageIds(state, selectedChapter);
            if (totalCount > 1
                    && state.canvas.canvasSelection.questIds().isEmpty()
                    && state.canvas.canvasSelection.textIds().isEmpty()
                    && state.canvas.canvasSelection.ecIds().isEmpty()
                    && !entityImageIds.isEmpty()
                    && entityImageIds.size() == totalCount) {
                String batchTarget = ModalTargets.canvasEntityChangeBatch(selectedChapter, entityImageIds.toArray(new String[0]));
                sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.change_entity"), "entity", TabletColors.INTERACTIVE, () -> {
                    int x = state.canvas.canvasImageLogicalX;
                    int y = state.canvas.canvasImageLogicalY;
                    ModalOpenActions.openCanvasEntityPicker(state, batchTarget, x, y);
                    ContextMenuController.close(state);
                    ContextMenuController.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_change_entity images={}", entityImageIds.size());
                    canvasViewport.refresh();
                }));
            }
            Set<String> questIds = state.canvas.canvasSelection.questIds();
            if (totalCount > 1 && !questIds.isEmpty()) {
                List<String> targets = new ArrayList<>(questIds);
                CompoundTag first = firstQuest(targets);
                sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_CHANGE_QUEST_BACKGROUND), "background", TabletColors.INTERACTIVE, () -> {
                    ContextMenuController.closeExclusiveSubmenus(state);
                    ModalOpenActions.openBatchQuestBackgroundPicker(
                            state,
                            targets,
                            first.getString("quest_background"),
                            first.getBoolean("quest_background_grayscale")
                    );
                    ContextMenuController.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_quest_background quests={}", targets.size());
                    canvasViewport.refresh();
                }));
                sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_CHANGE_COMPLETION_HUD_BACKGROUND), "completion_hud_background", TabletColors.INTERACTIVE, () -> {
                    ContextMenuController.closeExclusiveSubmenus(state);
                    ModalOpenActions.openBatchQuestCompletionHudBackgroundPicker(
                            state,
                            targets,
                            first.getString("completion_hud_background")
                    );
                    ContextMenuController.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_completion_hud_background quests={}", targets.size());
                    canvasViewport.refresh();
                }));
            }
        }
        if (totalCount > 1) {
            ContextMenuController.closeExclusiveSubmenus(state);
            if (hasConnections) {
                sections.add(ContextMenuSection.APPEARANCE, new ContextAction(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_CONNECTION_COLOR), "style_color", TabletColors.INTERACTIVE, () -> {
                    CanvasContextMenuController.ConnectionRef first = connectedConnections.get(0);
                    int color = CanvasRenderer.connectionColor(state, selectedChapter, first.prerequisiteId(), first.questId());
                    ModalOpenActions.openColorPicker(state, ModalTargets.connectionSelection(selectedChapter), color);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=patch_connection_colors chapter={} connections={}", selectedChapter, connectedConnections.size());
                    canvasViewport.refresh();
                }));
                sections.add(ContextMenuSection.APPEARANCE, new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_connection_texture"), "connect", TabletColors.INTERACTIVE, () -> {
                    ModalOpenActions.openConnectionTexturePicker(state, ModalTargets.connectionSelection(selectedChapter));
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=patch_connection_textures chapter={} connections={}", selectedChapter, connectedConnections.size());
                    canvasViewport.refresh();
                }));
                if (selectionHasConnectionTexture(state, selectedChapter, connectedConnections)) {
                    String selConnTexKey = "sel_remove_conn_tex:" + selectedChapter;
                    sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.warningDelete(state, selConnTexKey, CanvasContextMenuController.tr("ui.questsandstuff.context.remove_connection_texture"), () -> {
                        ContextMenuController.clearDeleteConfirm(state);
                        for (var connection : connectedConnections) {
                            String prereq = connection.prerequisiteId();
                            String quest = connection.questId();
                            boolean isEc = ConnectionRenderer.isEcId(state, selectedChapter, prereq) || ConnectionRenderer.isEcId(state, selectedChapter, quest);
                            if (isEc) {
                                EditorCanvasCommandClient.runEcConnectionTextureAction(state, prereq, quest, "");
                            } else {
                                EditorCanvasCommandClient.runConnectionTextureAction(player, quest, prereq, "");
                                ConnectionRenderer.setConnectionTexture(state, selectedChapter, prereq, quest, "");
                            }
                        }
                        QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_connection_textures chapter={} connections={}", selectedChapter, connectedConnections.size());
                        canvasViewport.refresh();
                    }));
                }
            }
            addBatchElementActions(sections, canvasViewport, state, selectedChapter);
            if (selectionSupportsGizmo(state, selectedChapter)) {
                CanvasTransformGizmoMenus.addModeActions(sections, ContextMenuSection.ARRANGE, state, canvasViewport::refresh);
            }
            addBatchRemainingVisualActions(sections, canvasViewport, state, player);
            addBatchBehaviorActions(sections, canvasViewport, state, player);
            if (CanvasGridFitController.canFitSelectionToGrid(state, selectedChapter, canvasViewport.cardLookup())) {
                sections.add(ContextMenuSection.ARRANGE, new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "fit_grid", TabletColors.INTERACTIVE, () -> {
                    boolean changed = CanvasGridFitController.fitSelectionToGrid(player, state, selectedChapter, canvasViewport.cardLookup());
                    ContextMenuController.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=selection count={} changed={}", totalCount, changed);
                    canvasViewport.refresh();
                }));
            }
            addSelectionAlignmentActions(sections, canvasViewport, state, player);
            addSelectionLayerActions(sections, canvasViewport, state, selectedChapter);
            addBatchResetQuest(sections, canvasViewport, state, player);
        }
        addSelectionCopyAndDeleteActions(sections, canvasViewport, state, player);
    }

    private static boolean selectionSupportsGizmo(TabletUiState state, String selectedChapter) {
        if (!state.canvas.canvasSelection.questIds().isEmpty()) return false;
        if (!state.canvas.canvasSelection.textIds().isEmpty()) return false;
        if (!state.canvas.canvasSelection.ecIds().isEmpty()) return false;
        boolean hasGizmoImage = false;
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(selectedChapter, List.of())) {
            if (CanvasSelectionActions.isImageSelected(state, image.id())) {
                if (!CanvasTransformGizmo.supports(image.asset())) {
                    return false;
                }
                hasGizmoImage = true;
            }
        }
        return hasGizmoImage;
    }

    private static void addBatchRemainingVisualActions(ContextMenuSections sections, CanvasViewport canvasViewport, TabletUiState state, Player player) {
        Set<String> questIds = state.canvas.canvasSelection.questIds();
        if (questIds.size() <= 1) {
            return;
        }
        List<String> targets = new ArrayList<>(questIds);
        if (selectionHasQuestBackground(targets)) {
            String selQuestBgKey = "sel_remove_quest_bg";
            sections.add(ContextMenuSection.DANGER, ContextActionFactory.warningDelete(state, selQuestBgKey, CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_REMOVE_QUEST_TEXTURE), () -> {
                ContextMenuController.clearDeleteConfirm(state);
                for (String questId : targets) {
                    EditorQuestCommandClient.setQuestBackground(player, questId, QuestDisplay.DEFAULT_QUEST_BACKGROUND, false);
                }
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_remove_quest_background quests={}", targets.size());
                canvasViewport.refresh();
            }));
        }
        if (selectionHasCompletionHudBackground(targets)) {
            String selHudBgKey = "sel_remove_hud_bg";
            sections.add(ContextMenuSection.DANGER, ContextActionFactory.warningDelete(state, selHudBgKey, CanvasContextMenuController.tr("ui.questsandstuff.context.remove_completion_hud_background"), () -> {
                EditorQuestCommandClient.setQuestCompletionHudBackground(player, new java.util.LinkedHashSet<>(targets), QuestDisplay.DEFAULT_COMPLETION_HUD_BACKGROUND);
                ContextMenuController.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_remove_completion_hud_background quests={}", targets.size());
                canvasViewport.refresh();
            }));
        }
    }

    private static void addBatchBehaviorActions(ContextMenuSections sections, CanvasViewport canvasViewport, TabletUiState state, Player player) {
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
                CanvasContextMenuController.tr(repeatable ? QuestTranslationKeys.CONTEXT_MAKE_QUEST_NOT_REPEATABLE : QuestTranslationKeys.CONTEXT_MAKE_QUEST_REPEATABLE),
                repeatable ? "repeat-off" : "repeat",
                repeatable ? TabletColors.SUCCESS : TabletColors.INTERACTIVE,
                () -> {
                    ContextMenuController.clearDeleteConfirm(state);
                    for (String questId : targets) {
                        EditorQuestCommandClient.setQuestRepeatable(player, questId, !repeatable);
                    }
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_behavior_repeatable quests={}", targets.size());
                    canvasViewport.refresh();
                }));
        behaviorActions.add(new ContextAction(
                CanvasContextMenuController.tr(lockUntilUnlocked ? QuestTranslationKeys.CONTEXT_SHOW_QUEST_BEFORE_UNLOCKED : QuestTranslationKeys.CONTEXT_LOCK_QUEST_UNTIL_UNLOCKED),
                lockUntilUnlocked ? "unlock_quest" : "lock_quest",
                lockUntilUnlocked ? TabletColors.SUCCESS : TabletColors.INTERACTIVE,
                () -> {
                    ContextMenuController.clearDeleteConfirm(state);
                    for (String questId : targets) {
                        EditorQuestCommandClient.setQuestHiddenMode(player, questId, lockUntilUnlocked ? prerequisitesVisible : locked);
                    }
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_behavior_lock quests={}", targets.size());
                    canvasViewport.refresh();
                }));
        behaviorActions.add(new ContextAction(
                CanvasContextMenuController.tr(hidden ? QuestTranslationKeys.CONTEXT_REVEAL_QUEST : QuestTranslationKeys.CONTEXT_HIDE_QUEST_UNTIL_UNLOCKED),
                hidden ? "eye" : "eye-off",
                hidden ? TabletColors.SUCCESS : TabletColors.WARNING,
                () -> {
                    ContextMenuController.clearDeleteConfirm(state);
                    for (String questId : targets) {
                        EditorQuestCommandClient.setQuestVisualHidden(player, questId, !hidden);
                    }
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_behavior_hide quests={}", targets.size());
                    canvasViewport.refresh();
                }));
        sections.add(ContextMenuSection.BEHAVIOR, ContextActionFactory.submenu(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_BEHAVIOR), "wrench", TabletColors.INTERACTIVE, behaviorActions));

        sections.add(ContextMenuSection.BEHAVIOR, ContextActionFactory.submenu(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_COMPLETION_SOUND), "audio-lines", TabletColors.INTERACTIVE, List.of(
                ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.use_game_sound"), "audio-lines", TabletColors.INTERACTIVE, () -> {
                    ModalOpenActions.openBatchQuestGameSoundPicker(state, targets, first.getString("completion_sound"));
                    ContextMenuController.closeExclusiveSubmenus(state);
                    ContextMenuController.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_completion_sound_game quests={}", targets.size());
                    canvasViewport.refresh();
                }),
                ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.use_custom_sound"), "audio-lines", TabletColors.INTERACTIVE, () -> {
                    ModalOpenActions.openBatchQuestCustomCompletionSoundPicker(state, targets, first.getString("completion_sound"));
                    ContextMenuController.closeExclusiveSubmenus(state);
                    ContextMenuController.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_completion_sound_custom quests={}", targets.size());
                    canvasViewport.refresh();
                })
        )));
    }

    private static void addBatchResetQuest(ContextMenuSections sections, CanvasViewport canvasViewport, TabletUiState state, Player player) {
        Set<String> questIds = state.canvas.canvasSelection.questIds();
        if (questIds.size() <= 1) {
            return;
        }
        List<String> targets = new ArrayList<>();
        for (String questId : questIds) {
            CompoundTag tag = ClientQuestStateFacade.quest(questId);
            if (tag != null && (tag.getBoolean("completed") || tag.getBoolean("claimed") || tag.getFloat("progress") > 0.0f)) {
                targets.add(questId);
            }
        }
        if (targets.size() <= 1) {
            return;
        }
        String selResetKey = "sel_reset_quests";
        sections.add(ContextMenuSection.DANGER, ContextActionFactory.warningDelete(state, selResetKey, CanvasContextMenuController.tr("ui.questsandstuff.context.reset_quest"), () -> {
            ContextMenuController.clearDeleteConfirm(state);
            for (String questId : targets) {
                EditorQuestCommandClient.resetQuestProgress(player, questId);
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_reset_quest quests={}", targets.size());
            canvasViewport.refresh();
        }));
    }

    private static boolean selectionHasQuestBackground(List<String> questIds) {
        for (String questId : questIds) {
            CompoundTag quest = ClientQuestStateFacade.quest(questId);
            if (quest != null && !QuestDisplay.DEFAULT_QUEST_BACKGROUND.equals(QuestDisplay.normalizeQuestBackground(quest.getString("quest_background")))) {
                return true;
            }
        }
        return false;
    }

    private static CompoundTag firstQuest(List<String> questIds) {
        for (String questId : questIds) {
            CompoundTag quest = ClientQuestStateFacade.quest(questId);
            if (quest != null && !quest.isEmpty()) {
                return quest;
            }
        }
        return new CompoundTag();
    }

    private static boolean selectionHasCompletionHudBackground(List<String> questIds) {
        for (String questId : questIds) {
            CompoundTag quest = ClientQuestStateFacade.quest(questId);
            if (quest != null && !QuestDisplay.DEFAULT_COMPLETION_HUD_BACKGROUND.equals(QuestDisplay.normalizeCompletionHudBackground(quest.getString("completion_hud_background")))) {
                return true;
            }
        }
        return false;
    }

    private static boolean selectionHasConnectionTexture(TabletUiState state, String chapter, List<CanvasContextMenuController.ConnectionRef> connections) {
        for (var connection : connections) {
            String prereq = connection.prerequisiteId();
            String quest = connection.questId();
            boolean isEc = ConnectionRenderer.isEcId(state, chapter, prereq) || ConnectionRenderer.isEcId(state, chapter, quest);
            String texture = isEc
                    ? ConnectionRenderer.ecConnectionTexture(state, chapter, prereq, quest)
                    : ConnectionRenderer.connectionTexture(state, chapter, prereq, quest);
            if (!texture.isBlank()) return true;
        }
        return false;
    }

    private static void addSelectionLayerActions(ContextMenuSections sections, CanvasViewport canvasViewport, TabletUiState state, String selectedChapter) {
        CanvasSelectionSet selection = CanvasSelectionSet.current(state);
        if (selection.layerKeys().isEmpty()) {
            return;
        }
        sections.add(ContextMenuSection.ARRANGE, ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.bring_to_front"), "up", TabletColors.INTERACTIVE, () -> {
            CanvasLayerMutations.moveCanvasLayers(state, selectedChapter, selection.layerKeys(), true);
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=bring_to_front target=selection count={}", selection.size());
            canvasViewport.refresh();
        }));
        sections.add(ContextMenuSection.ARRANGE, ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.send_to_back"), "down", TabletColors.TEXT_MUTED, () -> {
            CanvasLayerMutations.moveCanvasLayers(state, selectedChapter, selection.layerKeys(), false);
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=send_to_back target=selection count={}", selection.size());
            canvasViewport.refresh();
        }));
    }

    private static void addSelectionCopyAndDeleteActions(ContextMenuSections sections, CanvasViewport canvasViewport, TabletUiState state, Player player) {
        if (CanvasContextMenuSupport.canCopyContext(canvasViewport, state)) {
            sections.add(ContextMenuSection.CLIPBOARD, ContextActionFactory.copy(() -> {
                CanvasContextMenuSupport.copyContextToClipboard(canvasViewport, state);
                ContextMenuController.clearDeleteConfirm(state);
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextDeleteController.canDeleteContext(state)) {
            String deleteKey = CanvasContextDeleteController.deleteConfirmKey(state);
            sections.add(ContextMenuSection.DANGER, ContextActionFactory.delete(state, deleteKey, TabletTranslationKeys.text(TabletTranslationKeys.COMMON_DELETE), () -> {
                CanvasContextDeleteController.runDeleteAction(player, state);
                canvasViewport.refresh();
            }));
        }
    }

    private static Set<String> selectedEntityImageIds(TabletUiState state, String selectedChapter) {
        Set<String> ids = new LinkedHashSet<>();
        String primary = state.canvas.canvasSelection.primaryImageId();
        if (!primary.isBlank()) ids.add(primary);
        ids.addAll(state.canvas.canvasSelection.imageIds());
        if (ids.isEmpty()) return ids;
        List<CanvasImageLayer> images = state.canvas.canvasImagesByChapter.getOrDefault(selectedChapter, List.of());
        Set<String> entityIds = new LinkedHashSet<>();
        for (CanvasImageLayer image : images) {
            if (ids.contains(image.id()) && EntityPreviewRenderer.isEntityAsset(image.asset())) {
                entityIds.add(image.id());
            }
        }
        return entityIds;
    }

    private static void addBatchElementActions(ContextMenuSections sections, CanvasViewport canvasViewport, TabletUiState state, String selectedChapter) {
        Set<String> imageIds = state.canvas.canvasSelection.imageIds();
        Set<String> textIds = state.canvas.canvasSelection.textIds();
        Set<String> ecIds = state.canvas.canvasSelection.ecIds();

        boolean hasImages = !imageIds.isEmpty();
        boolean hasTexts = !textIds.isEmpty();
        boolean hasEcs = !ecIds.isEmpty();

        if (hasImages) {
            addBatchImageOnlyActions(sections, canvasViewport, state, selectedChapter, imageIds);
        }
        if (hasTexts) {
            addBatchTextOnlyActions(sections, canvasViewport, state, selectedChapter, textIds);
        }
        if (hasEcs) {
            addBatchEcOnlyActions(sections, canvasViewport, state, selectedChapter, ecIds);
        }
    }

    private static void addBatchImageOnlyActions(ContextMenuSections sections, CanvasViewport canvasViewport, TabletUiState state, String selectedChapter, Set<String> imageIds) {
        String primaryId = state.canvas.canvasSelection.primaryImageId();
        if (primaryId.isBlank()) {
            primaryId = imageIds.iterator().next();
        }
        String finalPrimaryId = primaryId;
        CanvasImageLayer primary = CanvasLayerMutations.findCanvasImage(state, selectedChapter, finalPrimaryId);
        if (primary == null) return;

        boolean isRecipeCard = CanvasRecipeCardAsset.isRecipeCardAsset(primary.asset());
        boolean isEntity = EntityPreviewRenderer.isEntityAsset(primary.asset());
        boolean isItem = ModelAssetPreviewRenderer.isItemAsset(primary.asset());
        boolean isBlock = ModelAssetPreviewRenderer.isBlockModelAsset(primary.asset());
        boolean allEntitySelection = state.canvas.canvasSelection.questIds().isEmpty()
                && state.canvas.canvasSelection.textIds().isEmpty()
                && state.canvas.canvasSelection.ecIds().isEmpty()
                && !imageIds.isEmpty()
                && selectedEntityImageIds(state, selectedChapter).size() == imageIds.size();

        if (isRecipeCard) {
            sections.add(ContextMenuSection.PRIMARY, new ContextAction(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_CHANGE_RECIPE_CARD), "recipe", TabletColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasRecipePicker(state, ModalTargets.canvasRecipeChange(selectedChapter, finalPrimaryId), primary.x(), primary.y());
                ContextMenuController.close(state);
                canvasViewport.refresh();
            }));
        } else if (isEntity) {
            if (!allEntitySelection) {
                sections.add(ContextMenuSection.PRIMARY, new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_entity"), "entity", TabletColors.INTERACTIVE, () -> {
                    ModalOpenActions.openCanvasEntityPicker(state, ModalTargets.canvasEntityChange(selectedChapter, finalPrimaryId), primary.x(), primary.y());
                    ContextMenuController.close(state);
                    canvasViewport.refresh();
                }));
            }
            Set<String> batchEntityIds = selectedEntityImageIds(state, selectedChapter);
            batchEntityIds.remove(finalPrimaryId);
            state.questDetails.entityMotionEditorBatchImageIds = String.join(",", batchEntityIds);
            String entityId = EntityPreviewRenderer.entityId(primary.asset());
            String variantTarget = ModalTargets.canvasImage(selectedChapter, finalPrimaryId);
            if (EntityVariantCatalog.hasVariants(entityId)) {
                sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.changeVariant(() -> {
                    ModalOpenActions.openEntityVariantPicker(state, variantTarget, primary.asset());
                    ContextMenuController.clearDeleteConfirm(state);
                    canvasViewport.refresh();
                }));
            }
            sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.promoted(
                    TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_EDIT_MOTION),
                    "motion", TabletColors.INTERACTIVE, () -> {
                        ContextMenuController.clearDeleteConfirm(state);
                        EntityMotionEditor.openMainCanvas(state, selectedChapter, finalPrimaryId, state.contextMenu.contextMenuX, state.contextMenu.contextMenuY);
                        canvasViewport.refresh();
                    }));
        } else if (isItem) {
            sections.add(ContextMenuSection.PRIMARY, new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_item"), "icon", TabletColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasItemPicker(state, ModalTargets.canvasItemChange(selectedChapter, finalPrimaryId), primary.x(), primary.y());
                ContextMenuController.close(state);
                canvasViewport.refresh();
            }));
        } else if (isBlock) {
            sections.add(ContextMenuSection.PRIMARY, new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_block"), "box", TabletColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasBlockPicker(state, ModalTargets.canvasBlockChange(selectedChapter, finalPrimaryId), primary.x(), primary.y());
                ContextMenuController.close(state);
                canvasViewport.refresh();
            }));
        }
    }

    private static void addBatchEcOnlyActions(ContextMenuSections sections, CanvasViewport canvasViewport, TabletUiState state, String selectedChapter, Set<String> ecIds) {
        String primaryId = state.canvas.canvasSelection.primaryEcId();
        if (primaryId.isBlank()) {
            primaryId = ecIds.iterator().next();
        }
        String finalPrimaryId = primaryId;
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, selectedChapter, finalPrimaryId);
        if (ec == null) return;

        sections.add(ContextMenuSection.APPEARANCE, new ContextAction(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_CHANGE_EXCLUSIVE_CHOICE_TEXTURE), "background", TabletColors.INTERACTIVE, () -> {
            ModalOpenActions.openEcBackgroundPicker(state, selectedChapter, finalPrimaryId, ec.background());
            ContextMenuController.close(state);
            canvasViewport.refresh();
        }));
        if (!ec.background().isBlank()) {
            String selEcBgKey = "sel_remove_ec_bg:" + selectedChapter;
            sections.add(ContextMenuSection.APPEARANCE, ContextActionFactory.warningDelete(state, selEcBgKey, CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_REMOVE_EXCLUSIVE_CHOICE_TEXTURE), () -> {
                for (String batchEcId : ecIds) {
                    CanvasExclusiveChoice batchEc = CanvasLayerMutations.findCanvasExclusiveChoice(state, selectedChapter, batchEcId);
                    if (batchEc != null && !batchEc.background().isBlank()) {
                        CanvasLayerMutations.putCanvasExclusiveChoice(state, selectedChapter, batchEc.withBackground(""));
                        CanvasLayerMutations.persistCanvasExclusiveChoice(state, selectedChapter, batchEcId);
                    }
                }
                ContextMenuController.close(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_remove_ec_background ecs={}", ecIds);
                canvasViewport.refresh();
            }));
        }
    }

    private static void addBatchTextOnlyActions(ContextMenuSections sections, CanvasViewport canvasViewport, TabletUiState state, String selectedChapter, Set<String> textIds) {
        String primaryId = state.canvas.canvasSelection.primaryTextId();
        if (primaryId.isBlank()) {
            primaryId = textIds.iterator().next();
        }
        String finalPrimaryId = primaryId;
        sections.add(ContextMenuSection.APPEARANCE, new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.menu.text_style"), "style", TabletColors.INTERACTIVE, false, () -> {
            TextStyleSession.openMainCanvas(state, finalPrimaryId);
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_text_style id={}", finalPrimaryId);
            canvasViewport.refresh();
        }));
    }

    private static void addSelectionAlignmentActions(ContextMenuSections sections, CanvasViewport canvasViewport, TabletUiState state, Player player) {
        sections.add(ContextMenuSection.ARRANGE, ContextActionFactory.submenu(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_ALIGN), "align-center-horizontal", TabletColors.INTERACTIVE, List.of(
                ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.align_horizontal_center"), "align-center-horizontal", TabletColors.INTERACTIVE, () -> {
                    boolean changed = CanvasSelectionActions.alignSelectedToCanvasCenter(player, state, false);
                    ContextMenuController.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=align_horizontal_center target=selection count={} changed={}", CanvasSelectionActions.totalCanvasSelectionCount(state), changed);
                    canvasViewport.refresh();
                }),
                ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.align_vertical_center"), "align-center-vertical", TabletColors.INTERACTIVE, () -> {
                    boolean changed = CanvasSelectionActions.alignSelectedToCanvasCenter(player, state, true);
                    ContextMenuController.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=align_vertical_center target=selection count={} changed={}", CanvasSelectionActions.totalCanvasSelectionCount(state), changed);
                    canvasViewport.refresh();
                })
        )));
    }
}
