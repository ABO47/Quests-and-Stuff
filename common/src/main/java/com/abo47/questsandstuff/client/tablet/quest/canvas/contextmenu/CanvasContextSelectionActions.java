package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmoMenus;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionSet;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class CanvasContextSelectionActions {
    private CanvasContextSelectionActions() {
    }

    static void addSelectionActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedGroup) {
        if (state.contextMenu.contextMenuTarget != ContextMenuTarget.SELECTION || selectedGroup.isBlank()) {
            return;
        }
        if (CanvasSelectionActions.totalCanvasSelectionCount(state) > 0) {
            actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.save_as_blueprint"), "scroll", ModColors.INTERACTIVE, () -> {
                boolean saved = CanvasBlueprintController.saveSelectionWithNotice(canvasViewport, state, state.contextMenu.contextLastClickX, state.contextMenu.contextLastClickY);
                ContextMenuState.close(state);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] context save_as_blueprint count={} saved={}", CanvasSelectionActions.totalCanvasSelectionCount(state), saved);
                canvasViewport.refresh();
            }));
        }
        List<CanvasContextMenuController.EdgeRef> connectedEdges = CanvasContextMenuController.selectedConnectedEdges(state, selectedGroup);
        if (!connectedEdges.isEmpty()) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.selection_connection_color"), "style_color", ModColors.INTERACTIVE, () -> {
                CanvasContextMenuController.EdgeRef first = connectedEdges.get(0);
                int color = CanvasRenderer.connectionColor(state, selectedGroup, first.prerequisiteId(), first.questId());
                ModalOpenActions.openColorPicker(state, ModalTargets.connectionSelection(selectedGroup), color);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=patch_connection_colors group={} edges={}", selectedGroup, connectedEdges.size());
                canvasViewport.refresh();
            }));
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.selection_connection_texture"), "background", ModColors.INTERACTIVE, () -> {
                ModalOpenActions.openConnectionTexturePicker(state, ModalTargets.connectionSelection(selectedGroup));
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=patch_connection_textures group={} edges={}", selectedGroup, connectedEdges.size());
                canvasViewport.refresh();
            }));
        }
        if (CanvasSelectionActions.totalCanvasSelectionCount(state) > 1) {
            ContextMenuState.closeExclusiveSubmenus(state);
            addBatchQuestActions(actions, canvasViewport, state, player);
            addSelectionAlignmentActions(actions, canvasViewport, state, player);
            addSelectionLayerActions(actions, canvasViewport, state, selectedGroup);
            if (CanvasGridFitController.canFitSelectionToGrid(state, selectedGroup, canvasViewport.cardLookup())) {
                actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "fit_grid", ModColors.INTERACTIVE, () -> {
                    boolean changed = CanvasGridFitController.fitSelectionToGrid(player, state, selectedGroup, canvasViewport.cardLookup());
                    ContextMenuState.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=selection count={} changed={}", CanvasSelectionActions.totalCanvasSelectionCount(state), changed);
                    canvasViewport.refresh();
                }));
            }
            if (selectionSupportsGizmo(state, selectedGroup)) {
                CanvasTransformGizmoMenus.addModeActions(actions, state, canvasViewport::refresh);
            }
        }
        addSelectionCopyAndDeleteActions(actions, canvasViewport, state, player);
    }

    private static boolean selectionSupportsGizmo(TabletUiState state, String selectedGroup) {
        for (CanvasImageLayer image : state.canvas.canvasImagesByGroup.getOrDefault(selectedGroup, List.of())) {
            if (CanvasSelectionActions.isImageSelected(state, image.id()) && CanvasTransformGizmo.supports(image.asset())) {
                return true;
            }
        }
        return false;
    }

    private static void addBatchQuestActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player) {
        Set<String> questIds = state.canvas.canvasSelection.questIds();
        if (questIds.size() <= 1) {
            return;
        }
        List<String> targets = new ArrayList<>(questIds);
        CompoundTag first = firstQuest(targets);
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
        actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.batch_quest_background"), "background", ModColors.INTERACTIVE, () -> {
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
        actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.batch_completion_hud_background"), "completion_hud_background", ModColors.INTERACTIVE, () -> {
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
        if (selectionHasCompletionHudBackground(targets)) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.remove_completion_hud_background"), "delete", ModColors.WARNING, () -> {
                EditorCommandClient.setQuestCompletionHudBackground(player, new java.util.LinkedHashSet<>(targets), QuestDisplay.DEFAULT_COMPLETION_HUD_BACKGROUND);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_remove_completion_hud_background quests={}", targets.size());
                canvasViewport.refresh();
            }));
        }
        addBatchMiscActions(actions, canvasViewport, state, player, targets, first);
    }

    private static void addBatchMiscActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, List<String> targets, CompoundTag first) {
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
                        EditorCommandClient.setQuestRepeatable(player, questId, !repeatable);
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
                        EditorCommandClient.setQuestHiddenMode(player, questId, lockUntilUnlocked ? prerequisitesVisible : locked);
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
                        EditorCommandClient.setQuestVisualHidden(player, questId, !hidden);
                    }
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_behavior_hide quests={}", targets.size());
                    canvasViewport.refresh();
                }));
        actions.add(ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_BEHAVIOR), "wrench", ModColors.INTERACTIVE, behaviorActions));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.reset_quest"), "reset_quest", ModColors.WARNING, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            for (String questId : targets) {
                EditorCommandClient.resetQuestProgress(player, questId);
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_reset_quest quests={}", targets.size());
            canvasViewport.refresh();
        }));
        if (selectionHasQuestBackground(targets)) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.remove_background"), "delete", ModColors.WARNING, () -> {
                ContextMenuState.clearDeleteConfirm(state);
                for (String questId : targets) {
                    EditorCommandClient.setQuestBackground(player, questId, QuestDisplay.DEFAULT_QUEST_BACKGROUND, false);
                }
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_remove_quest_background quests={}", targets.size());
                canvasViewport.refresh();
            }));
        }
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
