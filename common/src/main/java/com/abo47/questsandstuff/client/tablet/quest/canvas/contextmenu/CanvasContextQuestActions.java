package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.entity.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletWidgetCoordinates;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

final class CanvasContextQuestActions {
    private CanvasContextQuestActions() {
    }

    static void addQuestActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedGroup) {
        if (state.contextMenu.contextMenuTarget != ContextMenuTarget.QUEST || state.contextMenu.contextQuestId.isBlank()) {
            return;
        }
        CompoundTag questTag = ClientQuestCache.quest(state.contextMenu.contextQuestId);
        ContextMenuState.closeExclusiveSubmenus(state);
        if (CanvasContextMenuSupport.hasOtherQuest(canvasViewport, state.contextMenu.contextQuestId)) {
            actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.connect_to"), "connect", ModColors.SUCCESS, () -> {
                state.canvas.connectSourceQuestId = state.contextMenu.contextQuestId;
                state.canvas.connectSourceQuestIds.clear();
                if (state.canvas.canvasSelection.questIds().contains(state.contextMenu.contextQuestId) && state.canvas.canvasSelection.questIds().size() > 1) {
                    state.canvas.connectSourceQuestIds.addAll(state.canvas.canvasSelection.questIds());
                } else {
                    state.canvas.canvasSelection.questIds().clear();
                    state.canvas.canvasSelection.questIds().add(state.contextMenu.contextQuestId);
                    state.canvas.connectSourceQuestIds.add(state.contextMenu.contextQuestId);
                }
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=connect_to sources={}", state.canvas.connectSourceQuestIds);
                canvasViewport.refresh();
            }));
        }
        actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.open_quest"), "open", ModColors.INTERACTIVE, () -> {
            openQuestDetails(canvasViewport, state);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=open_quest quest={}", state.contextMenu.contextQuestId);
            canvasViewport.refresh();
        }));
        actions.add(ContextActions.promoted(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_CHANGE_TITLE), "rename", ModColors.INTERACTIVE, () -> {
            EditorCommandClient.beginQuestTitleChange(state, state.contextMenu.contextQuestId);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_title quest={}", state.contextMenu.contextQuestId);
            canvasViewport.refresh();
        }));
        QuestCardLayout contextQuest = canvasViewport.cardLookup().get(state.contextMenu.contextQuestId);
        if (CanvasGridFitController.canFitQuestToGrid(state, contextQuest)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "fit_grid", ModColors.INTERACTIVE, () -> {
                boolean changed = CanvasGridFitController.fitQuestToGrid(player, state, contextQuest);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=quest id={} changed={}", state.contextMenu.contextQuestId, changed);
                canvasViewport.refresh();
            }));
        }
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.reset_quest"), "reset_quest", ModColors.WARNING, () -> {
            EditorCommandClient.resetQuestProgress(player, state.contextMenu.contextQuestId);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=reset_quest quest={}", state.contextMenu.contextQuestId);
            canvasViewport.refresh();
        }));
        addQuestRepeatableAction(actions, canvasViewport, state, player, questTag);
        addQuestPrerequisiteActions(actions, canvasViewport, state, questTag);
        addQuestVisibilityActions(actions, canvasViewport, state, player, questTag);
        addCompletionSoundActions(actions, canvasViewport, state, questTag);
        actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.menu.change_icon"), "icon", ModColors.INTERACTIVE, () -> {
            EntityIconControls.openIconPicker(state, EntityIconControls.IconPickerTarget.quest(state.contextMenu.contextQuestId));
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_icon quest={}", state.contextMenu.contextQuestId);
            canvasViewport.refresh();
        }));
        addQuestBackgroundActions(actions, canvasViewport, state, player, questTag);
        EntityIconControls.addEntityVariantAndMotionActions(
                actions,
                state,
                questTag.getString("icon"),
                ModalTargets.questIcon(state.contextMenu.contextQuestId),
                () -> ContextMenuState.close(state),
                () -> {
                    EntityMotionEditor.openQuestIcon(state, state.contextMenu.contextQuestId, state.contextMenu.contextMenuX, state.contextMenu.contextMenuY);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=edit_entity_icon_motion quest={}", state.contextMenu.contextQuestId);
                },
                canvasViewport::refresh
        );
        addQuestLayerActions(actions, canvasViewport, state, selectedGroup);
    }

    private static void addQuestBackgroundActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        actions.add(ContextActions.action(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_CHANGE_QUEST_BACKGROUND), "background", ModColors.INTERACTIVE, () -> {
            ModalOpenActions.openQuestBackgroundPicker(
                    state,
                    state.contextMenu.contextQuestId,
                    questTag.getString("quest_background"),
                    questTag.getBoolean("quest_background_grayscale")
            );
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_quest_background quest={}", state.contextMenu.contextQuestId);
            canvasViewport.refresh();
        }));
        if (!QuestDisplay.DEFAULT_QUEST_BACKGROUND.equals(QuestDisplay.normalizeQuestBackground(questTag.getString("quest_background")))) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_REMOVE_BACKGROUND), "delete", ModColors.WARNING, () -> {
                EditorCommandClient.setQuestBackground(player, state.contextMenu.contextQuestId, QuestDisplay.DEFAULT_QUEST_BACKGROUND, false);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_quest_background quest={}", state.contextMenu.contextQuestId);
                canvasViewport.refresh();
            }));
        }
        addCompletionHudBackgroundActions(actions, canvasViewport, state, player, questTag);
    }

    private static void addCompletionSoundActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, CompoundTag questTag) {
        actions.add(ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_COMPLETION_SOUND), "audio-lines", ModColors.INTERACTIVE, List.of(
                ContextActions.action(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_USE_GAME_SOUND), "audio-lines", ModColors.INTERACTIVE, () -> {
                    String sound = questTag.getString("completion_sound");
                    ModalOpenActions.openQuestGameSoundPicker(state, state.contextMenu.contextQuestId, sound);
                    ContextMenuState.closeExclusiveSubmenus(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=completion_sound_game quest={} sound={}", state.contextMenu.contextQuestId, sound);
                    canvasViewport.refresh();
                }),
                ContextActions.action(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_USE_CUSTOM_SOUND), "audio-lines", ModColors.INTERACTIVE, () -> {
                    String sound = questTag.getString("completion_sound");
                    ModalOpenActions.openQuestCustomCompletionSoundPicker(state, state.contextMenu.contextQuestId, sound);
                    ContextMenuState.closeExclusiveSubmenus(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=completion_sound_custom quest={} sound={}", state.contextMenu.contextQuestId, sound);
                    canvasViewport.refresh();
                })
        )));
    }

    private static void addCompletionHudBackgroundActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        String currentBackground = questTag == null ? "" : questTag.getString("completion_hud_background");
        actions.add(new ContextAction(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_CHANGE_COMPLETION_HUD_BACKGROUND), "completion_hud_background", ModColors.INTERACTIVE, () -> {
            ModalOpenActions.openQuestCompletionHudBackgroundPicker(state, state.contextMenu.contextQuestId, currentBackground);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_completion_hud_background quest={}", state.contextMenu.contextQuestId);
            canvasViewport.refresh();
        }));
        if (!QuestDisplay.DEFAULT_COMPLETION_HUD_BACKGROUND.equals(QuestDisplay.normalizeCompletionHudBackground(currentBackground))) {
            actions.add(new ContextAction(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_REMOVE_COMPLETION_HUD_BACKGROUND), "delete", ModColors.WARNING, () -> {
                EditorCommandClient.setQuestCompletionHudBackground(player, state.contextMenu.contextQuestId, QuestDisplay.DEFAULT_COMPLETION_HUD_BACKGROUND);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_completion_hud_background quest={}", state.contextMenu.contextQuestId);
                canvasViewport.refresh();
            }));
        }
    }

    private static void openQuestDetails(CanvasViewport canvasViewport, TabletUiState state) {
        QuestCardLayout card = canvasViewport.cardLookup().get(state.contextMenu.contextQuestId);
        int viewportScreenX = TabletWidgetCoordinates.screenX(canvasViewport, state.canvas.canvasPanelX + state.canvas.canvasViewportX);
        int viewportScreenY = TabletWidgetCoordinates.screenY(canvasViewport, state.canvas.canvasPanelY + state.canvas.canvasViewportY);
        if (card == null) {
            QuestDetailsWindow.openAtSource(
                    state,
                    state.contextMenu.contextQuestId,
                    viewportScreenX + state.contextMenu.contextMenuX,
                    viewportScreenY + state.contextMenu.contextMenuY,
                    1,
                    1
            );
            return;
        }
        QuestDetailsWindow.openAtSource(
                state,
                state.contextMenu.contextQuestId,
                viewportScreenX + card.x(),
                viewportScreenY + card.y(),
                card.width(),
                card.height()
        );
    }

    private static void addQuestPrerequisiteActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, CompoundTag questTag) {
        actions.add(ContextActions.action(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_PREREQUISITES_MANAGER), "share-2", ModColors.INTERACTIVE, () -> {
            ModalOpenActions.openPrerequisitesManager(state, state.contextMenu.contextQuestId);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=prerequisites_manager quest={}", state.contextMenu.contextQuestId);
            canvasViewport.refresh();
        }));
    }

    private static void addQuestVisibilityActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        List<ContextAction> visibilityActions = new ArrayList<>();
        addQuestVisibilityAction(visibilityActions, canvasViewport, state, player, questTag);
        addQuestVisualHiddenAction(visibilityActions, canvasViewport, state, player, questTag);
        if (!visibilityActions.isEmpty()) {
            actions.add(ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_VISIBILITY), "eye", ModColors.INTERACTIVE, visibilityActions));
        }
    }

    private static void addQuestRepeatableAction(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        boolean repeatable = questTag.getBoolean("repeatable");
        actions.add(new ContextAction(
                CanvasContextMenuController.tr(repeatable ? QuestVocabulary.CONTEXT_MAKE_QUEST_NOT_REPEATABLE : QuestVocabulary.CONTEXT_MAKE_QUEST_REPEATABLE),
                repeatable ? "repeat-off" : "repeat",
                repeatable ? ModColors.SUCCESS : ModColors.INTERACTIVE,
                () -> {
                    EditorCommandClient.setQuestRepeatable(player, state.contextMenu.contextQuestId, !repeatable);
                    ContextMenuState.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=quest_repeatable quest={} enabled={}", state.contextMenu.contextQuestId, !repeatable);
                    canvasViewport.refresh();
                }));
    }

    private static void addQuestVisibilityAction(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        String hiddenMode = questTag.getString("hidden_mode");
        String locked = QuestVisibilityMode.LOCKED.serializedName();
        String prerequisitesVisible = QuestVisibilityMode.PREREQUISITES_VISIBLE.serializedName();
        boolean lockUntilUnlocked = locked.equals(hiddenMode);
        actions.add(new ContextAction(
                CanvasContextMenuController.tr(lockUntilUnlocked ? QuestVocabulary.CONTEXT_SHOW_QUEST_BEFORE_UNLOCKED : QuestVocabulary.CONTEXT_LOCK_QUEST_UNTIL_UNLOCKED),
                lockUntilUnlocked ? "unlock_quest" : "lock_quest",
                lockUntilUnlocked ? ModColors.SUCCESS : ModColors.INTERACTIVE,
                () -> {
                    EditorCommandClient.setQuestHiddenMode(player, state.contextMenu.contextQuestId, lockUntilUnlocked ? prerequisitesVisible : locked);
                    ContextMenuState.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=quest_lock_until_unlocked quest={} enabled={}", state.contextMenu.contextQuestId, !lockUntilUnlocked);
                    canvasViewport.refresh();
                }));
    }

    private static void addQuestVisualHiddenAction(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        boolean hidden = questTag.getBoolean("visual_hidden");
        actions.add(new ContextAction(
                CanvasContextMenuController.tr(hidden ? QuestVocabulary.CONTEXT_REVEAL_QUEST : QuestVocabulary.CONTEXT_HIDE_QUEST_UNTIL_UNLOCKED),
                hidden ? "eye" : "eye-off",
                hidden ? ModColors.SUCCESS : ModColors.WARNING,
                () -> {
                    EditorCommandClient.setQuestVisualHidden(player, state.contextMenu.contextQuestId, !hidden);
                    ContextMenuState.clearDeleteConfirm(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=quest_hide_until_unlocked quest={} enabled={}", state.contextMenu.contextQuestId, !hidden);
                    canvasViewport.refresh();
                }));
    }

    private static void addQuestLayerActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup) {
        String layerKey = CanvasLayerOrdering.questKey(state.contextMenu.contextQuestId);
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedGroup, layerKey, true)) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.bring_to_front"), "up", ModColors.INTERACTIVE, () -> {
                CanvasLayerMutations.moveQuestLayer(state, selectedGroup, state.contextMenu.contextQuestId, true);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=bring_to_front target=quest id={}", state.contextMenu.contextQuestId);
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedGroup, layerKey, false)) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.send_to_back"), "down", ModColors.TEXT_MUTED, () -> {
                CanvasLayerMutations.moveQuestLayer(state, selectedGroup, state.contextMenu.contextQuestId, false);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=send_to_back target=quest id={}", state.contextMenu.contextQuestId);
                canvasViewport.refresh();
            }));
        }
    }
}
