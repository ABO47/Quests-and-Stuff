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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class CanvasContextSelectionActions {
    private CanvasContextSelectionActions() {
    }

    static void addSelectionActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedGroup) {
        if (state.contextMenuTarget != ContextMenuTarget.SELECTION || selectedGroup.isBlank()) {
            return;
        }
        if (CanvasSelectionActions.totalCanvasSelectionCount(state) > 0) {
            actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.save_as_blueprint"), "scroll", ModColors.INTERACTIVE, () -> {
                boolean saved = CanvasBlueprintController.saveSelectionWithNotice(canvasViewport, state, state.contextLastClickX, state.contextLastClickY);
                state.contextMenuOpen = false;
                state.contextDeleteConfirmKey = "";
                QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] context save_as_blueprint count={} saved={}", CanvasSelectionActions.totalCanvasSelectionCount(state), saved);
                canvasViewport.refresh();
            }));
        }
        if (CanvasSelectionActions.totalCanvasSelectionCount(state) > 1) {
            state.contextQuestCompletionSoundMenuOpen = false;
            if (selectionSupportsGizmo(state, selectedGroup)) {
                CanvasTransformGizmoMenus.addModeActions(actions, state, canvasViewport::refresh);
            }
            addBatchQuestActions(actions, canvasViewport, state, player);
            addSelectionAlignmentActions(actions, canvasViewport, state, player);
            addSelectionLayerActions(actions, canvasViewport, state, selectedGroup);
            if (CanvasGridFitController.canFitSelectionToGrid(state, selectedGroup, canvasViewport.cardLookup())) {
                actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "fit_grid", ModColors.INTERACTIVE, () -> {
                    boolean changed = CanvasGridFitController.fitSelectionToGrid(player, state, selectedGroup, canvasViewport.cardLookup());
                    state.contextDeleteConfirmKey = "";
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=selection count={} changed={}", CanvasSelectionActions.totalCanvasSelectionCount(state), changed);
                    canvasViewport.refresh();
                }));
            }
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
        }
    }

    private static boolean selectionSupportsGizmo(TabletUiState state, String selectedGroup) {
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(selectedGroup, List.of())) {
            if (CanvasSelectionActions.isImageSelected(state, image.id()) && CanvasTransformGizmo.supports(image.asset())) {
                return true;
            }
        }
        return false;
    }

    private static void addBatchQuestActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player) {
        Set<String> questIds = state.selectedQuestIds;
        if (questIds.size() <= 1) {
            return;
        }
        List<String> targets = new ArrayList<>(questIds);
        CompoundTag first = firstQuest(targets);
        actions.add(ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_COMPLETION_SOUND), "audio-lines", ModColors.INTERACTIVE, List.of(
                ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.use_game_sound"), "audio-lines", ModColors.INTERACTIVE, () -> {
                    ModalOpenActions.openBatchQuestGameSoundPicker(state, targets, first.getString("completion_sound"));
                    state.contextQuestCompletionSoundMenuOpen = false;
                    state.contextDeleteConfirmKey = "";
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_completion_sound_game quests={}", targets.size());
                    canvasViewport.refresh();
                }),
                ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.use_custom_sound"), "audio-lines", ModColors.INTERACTIVE, () -> {
                    ModalOpenActions.openBatchQuestCustomCompletionSoundPicker(state, targets, first.getString("completion_sound"));
                    state.contextQuestCompletionSoundMenuOpen = false;
                    state.contextDeleteConfirmKey = "";
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_completion_sound_custom quests={}", targets.size());
                    canvasViewport.refresh();
                })
        )));
        actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.batch_quest_background"), "background", ModColors.INTERACTIVE, () -> {
            state.contextQuestCompletionSoundMenuOpen = false;
            ModalOpenActions.openBatchQuestBackgroundPicker(
                    state,
                    targets,
                    first.getString("quest_background"),
                    first.getBoolean("quest_background_grayscale")
            );
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_quest_background quests={}", targets.size());
            canvasViewport.refresh();
        }));
        actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.batch_completion_hud_background"), "completion_hud_background", ModColors.INTERACTIVE, () -> {
            state.contextQuestCompletionSoundMenuOpen = false;
            ModalOpenActions.openBatchQuestCompletionHudBackgroundPicker(
                    state,
                    targets,
                    first.getString("completion_hud_background")
            );
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_completion_hud_background quests={}", targets.size());
            canvasViewport.refresh();
        }));
        if (selectionHasCompletionHudBackground(targets)) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.remove_completion_hud_background"), "delete", ModColors.WARNING, () -> {
                EditorCommandClient.setQuestCompletionHudBackground(player, new java.util.LinkedHashSet<>(targets), QuestDisplay.DEFAULT_COMPLETION_HUD_BACKGROUND);
                state.contextDeleteConfirmKey = "";
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=batch_remove_completion_hud_background quests={}", targets.size());
                canvasViewport.refresh();
            }));
        }
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
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=bring_to_front target=selection count={}", selection.size());
            canvasViewport.refresh();
        }));
        actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.send_to_back"), "down", ModColors.TEXT_MUTED, () -> {
            CanvasLayerMutations.moveCanvasLayers(state, selectedGroup, selection.layerKeys(), false);
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=send_to_back target=selection count={}", selection.size());
            canvasViewport.refresh();
        }));
    }

    private static void addSelectionAlignmentActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player) {
        actions.add(ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_ALIGN), "align-center-horizontal", ModColors.INTERACTIVE, List.of(
                ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.align_horizontal_center"), "align-center-horizontal", ModColors.INTERACTIVE, () -> {
                    boolean changed = CanvasSelectionActions.alignSelectedToCanvasCenter(player, state, false);
                    state.contextDeleteConfirmKey = "";
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=align_horizontal_center target=selection count={} changed={}", CanvasSelectionActions.totalCanvasSelectionCount(state), changed);
                    canvasViewport.refresh();
                }),
                ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.align_vertical_center"), "align-center-vertical", ModColors.INTERACTIVE, () -> {
                    boolean changed = CanvasSelectionActions.alignSelectedToCanvasCenter(player, state, true);
                    state.contextDeleteConfirmKey = "";
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=align_vertical_center target=selection count={} changed={}", CanvasSelectionActions.totalCanvasSelectionCount(state), changed);
                    canvasViewport.refresh();
                })
        )));
    }
}
