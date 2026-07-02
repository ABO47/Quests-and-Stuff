package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.controls.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.widget.TabletWidgetCoordinates;
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
        CompoundTag questTag = ClientQuestStateFacade.quest(state.contextMenu.contextQuestId);
        ContextMenuController.closeExclusiveSubmenus(state);
        QuestCardLayout contextQuest = canvasViewport.cardLookup().get(state.contextMenu.contextQuestId);

        actions.add(ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.open_quest"), "open", TabletColors.INTERACTIVE, withCleanup(canvasViewport, state, () -> {
            openQuestDetails(canvasViewport, state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=open_quest quest={}", state.contextMenu.contextQuestId);
        })));
        if (CanvasContextMenuSupport.hasOtherQuest(canvasViewport, state.contextMenu.contextQuestId)) {
            actions.add(ContextActionFactory.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.connect_to"), "connect", TabletColors.SUCCESS, withCleanup(canvasViewport, state, () -> {
                state.canvas.connectSourceQuestId = state.contextMenu.contextQuestId;
                state.canvas.connectSourceQuestIds.clear();
                if (state.canvas.canvasSelection.questIds().contains(state.contextMenu.contextQuestId) && state.canvas.canvasSelection.questIds().size() > 1) {
                    state.canvas.connectSourceQuestIds.addAll(state.canvas.canvasSelection.questIds());
                } else {
                    state.canvas.canvasSelection.questIds().clear();
                    state.canvas.canvasSelection.questIds().add(state.contextMenu.contextQuestId);
                    state.canvas.connectSourceQuestIds.add(state.contextMenu.contextQuestId);
                }
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=connect_to sources={}", state.canvas.connectSourceQuestIds);
            })));
        }
        actions.add(ContextActionFactory.promoted(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_CHANGE_TITLE), "rename", TabletColors.INTERACTIVE, withCleanup(canvasViewport, state, () -> {
            EditorQuestCommandClient.beginQuestTitleChange(state, state.contextMenu.contextQuestId);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_title quest={}", state.contextMenu.contextQuestId);
        })));
        actions.add(ContextActionFactory.promoted(CanvasContextMenuController.tr("ui.questsandstuff.menu.change_icon"), "icon", TabletColors.INTERACTIVE, withCleanup(canvasViewport, state, () -> {
            EntityIconControls.openIconPicker(state, EntityIconControls.IconPickerTarget.quest(state.contextMenu.contextQuestId));
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_icon quest={}", state.contextMenu.contextQuestId);
        })));

        addQuestPrerequisiteActions(actions, canvasViewport, state, questTag);
        addQuestBehaviorActions(actions, canvasViewport, state, player, questTag);
        addCompletionSoundActions(actions, canvasViewport, state, questTag);

        addQuestBackgroundActions(actions, canvasViewport, state, player, questTag);

        if (CanvasGridFitController.canFitQuestToGrid(state, contextQuest)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "fit_grid", TabletColors.INTERACTIVE, withCleanup(canvasViewport, state, () -> {
                boolean changed = CanvasGridFitController.fitQuestToGrid(player, state, contextQuest);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=quest id={} changed={}", state.contextMenu.contextQuestId, changed);
            })));
        }
        addQuestArrangeActions(actions, canvasViewport, state, selectedGroup);

        EntityIconControls.addEntityVariantAndMotionActions(
                actions,
                state,
                questTag.getString("icon"),
                ModalTargets.questIcon(state.contextMenu.contextQuestId),
                () -> ContextMenuController.close(state),
                () -> {
                    EntityMotionEditor.openQuestIcon(state, state.contextMenu.contextQuestId, state.contextMenu.contextMenuX, state.contextMenu.contextMenuY);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=edit_entity_icon_motion quest={}", state.contextMenu.contextQuestId);
                },
                canvasViewport::refresh
        );
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.reset_quest"), "reset_quest", TabletColors.WARNING, withCleanup(canvasViewport, state, () -> {
            EditorQuestCommandClient.resetQuestProgress(player, state.contextMenu.contextQuestId);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=reset_quest quest={}", state.contextMenu.contextQuestId);
        })));
        addQuestCopyAndDeleteActions(actions, canvasViewport, state, player);
    }

    private static void addQuestBackgroundActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        actions.add(ContextActionFactory.action(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_CHANGE_QUEST_BACKGROUND), "background", TabletColors.INTERACTIVE, withCleanup(canvasViewport, state, () -> {
            ModalOpenActions.openQuestBackgroundPicker(
                    state,
                    state.contextMenu.contextQuestId,
                    questTag.getString("quest_background"),
                    questTag.getBoolean("quest_background_grayscale")
            );
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_quest_background quest={}", state.contextMenu.contextQuestId);
        })));
        if (!QuestDisplay.DEFAULT_QUEST_BACKGROUND.equals(QuestDisplay.normalizeQuestBackground(questTag.getString("quest_background")))) {
            actions.add(ContextActionFactory.action(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_REMOVE_BACKGROUND), "delete", TabletColors.WARNING, withCleanup(canvasViewport, state, () -> {
                EditorQuestCommandClient.setQuestBackground(player, state.contextMenu.contextQuestId, QuestDisplay.DEFAULT_QUEST_BACKGROUND, false);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_quest_background quest={}", state.contextMenu.contextQuestId);
            })));
        }
        addCompletionHudBackgroundActions(actions, canvasViewport, state, player, questTag);
    }

    private static void addCompletionSoundActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, CompoundTag questTag) {
        actions.add(ContextActionFactory.submenu(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_COMPLETION_SOUND), "audio-lines", TabletColors.INTERACTIVE, List.of(
                ContextActionFactory.action(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_USE_GAME_SOUND), "audio-lines", TabletColors.INTERACTIVE, () -> {
                    String sound = questTag.getString("completion_sound");
                    ModalOpenActions.openQuestGameSoundPicker(state, state.contextMenu.contextQuestId, sound);
                    ContextMenuController.closeExclusiveSubmenus(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=completion_sound_game quest={} sound={}", state.contextMenu.contextQuestId, sound);
                    canvasViewport.refresh();
                }),
                ContextActionFactory.action(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_USE_CUSTOM_SOUND), "audio-lines", TabletColors.INTERACTIVE, () -> {
                    String sound = questTag.getString("completion_sound");
                    ModalOpenActions.openQuestCustomCompletionSoundPicker(state, state.contextMenu.contextQuestId, sound);
                    ContextMenuController.closeExclusiveSubmenus(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=completion_sound_custom quest={} sound={}", state.contextMenu.contextQuestId, sound);
                    canvasViewport.refresh();
                })
        )));
    }

    private static void addCompletionHudBackgroundActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        String currentBackground = questTag == null ? "" : questTag.getString("completion_hud_background");
        actions.add(new ContextAction(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_CHANGE_COMPLETION_HUD_BACKGROUND), "completion_hud_background", TabletColors.INTERACTIVE, withCleanup(canvasViewport, state, () -> {
            ModalOpenActions.openQuestCompletionHudBackgroundPicker(state, state.contextMenu.contextQuestId, currentBackground);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_completion_hud_background quest={}", state.contextMenu.contextQuestId);
        })));
        if (!QuestDisplay.DEFAULT_COMPLETION_HUD_BACKGROUND.equals(QuestDisplay.normalizeCompletionHudBackground(currentBackground))) {
            actions.add(new ContextAction(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_REMOVE_COMPLETION_HUD_BACKGROUND), "delete", TabletColors.WARNING, withCleanup(canvasViewport, state, () -> {
                EditorQuestCommandClient.setQuestCompletionHudBackground(player, state.contextMenu.contextQuestId, QuestDisplay.DEFAULT_COMPLETION_HUD_BACKGROUND);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_completion_hud_background quest={}", state.contextMenu.contextQuestId);
            })));
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
        actions.add(ContextActionFactory.action(CanvasContextMenuController.tr(QuestTranslationKeys.CONTEXT_PREREQUISITES_MANAGER), "share-2", TabletColors.INTERACTIVE, withCleanup(canvasViewport, state, () -> {
            ModalOpenActions.openPrerequisitesManager(state, state.contextMenu.contextQuestId);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=prerequisites_manager quest={}", state.contextMenu.contextQuestId);
        })));
    }

    private static void addQuestBehaviorActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        List<ContextAction> behaviorActions = new ArrayList<>();
        addQuestRepeatableAction(behaviorActions, canvasViewport, state, player, questTag);
        addQuestLockAction(behaviorActions, canvasViewport, state, player, questTag);
        addQuestHiddenAction(behaviorActions, canvasViewport, state, player, questTag);
        actions.add(ContextActionFactory.submenu(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_BEHAVIOR), "wrench", TabletColors.INTERACTIVE, behaviorActions));
    }

    private static void addQuestRepeatableAction(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        boolean repeatable = questTag.getBoolean("repeatable");
        actions.add(new ContextAction(
                CanvasContextMenuController.tr(repeatable ? QuestTranslationKeys.CONTEXT_MAKE_QUEST_NOT_REPEATABLE : QuestTranslationKeys.CONTEXT_MAKE_QUEST_REPEATABLE),
                repeatable ? "repeat-off" : "repeat",
                repeatable ? TabletColors.SUCCESS : TabletColors.INTERACTIVE,
                withCleanup(canvasViewport, state, () -> {
                    EditorQuestCommandClient.setQuestRepeatable(player, state.contextMenu.contextQuestId, !repeatable);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=quest_repeatable quest={} enabled={}", state.contextMenu.contextQuestId, !repeatable);
                })));
    }

    private static void addQuestLockAction(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        String hiddenMode = questTag.getString("hidden_mode");
        String locked = QuestVisibilityMode.LOCKED.serializedName();
        String prerequisitesVisible = QuestVisibilityMode.PREREQUISITES_VISIBLE.serializedName();
        boolean lockUntilUnlocked = locked.equals(hiddenMode);
        actions.add(new ContextAction(
                CanvasContextMenuController.tr(lockUntilUnlocked ? QuestTranslationKeys.CONTEXT_SHOW_QUEST_BEFORE_UNLOCKED : QuestTranslationKeys.CONTEXT_LOCK_QUEST_UNTIL_UNLOCKED),
                lockUntilUnlocked ? "unlock_quest" : "lock_quest",
                lockUntilUnlocked ? TabletColors.SUCCESS : TabletColors.INTERACTIVE,
                withCleanup(canvasViewport, state, () -> {
                    EditorQuestCommandClient.setQuestHiddenMode(player, state.contextMenu.contextQuestId, lockUntilUnlocked ? prerequisitesVisible : locked);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=quest_lock_until_unlocked quest={} enabled={}", state.contextMenu.contextQuestId, !lockUntilUnlocked);
                })));
    }

    private static void addQuestHiddenAction(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        boolean hidden = questTag.getBoolean("visual_hidden");
        actions.add(new ContextAction(
                CanvasContextMenuController.tr(hidden ? QuestTranslationKeys.CONTEXT_REVEAL_QUEST : QuestTranslationKeys.CONTEXT_HIDE_QUEST_UNTIL_UNLOCKED),
                hidden ? "eye" : "eye-off",
                hidden ? TabletColors.SUCCESS : TabletColors.WARNING,
                withCleanup(canvasViewport, state, () -> {
                    EditorQuestCommandClient.setQuestVisualHidden(player, state.contextMenu.contextQuestId, !hidden);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=quest_hide_until_unlocked quest={} enabled={}", state.contextMenu.contextQuestId, !hidden);
                })));
    }

    private static void addQuestArrangeActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup) {
        List<ContextAction> arrangeActions = new ArrayList<>();
        String layerKey = CanvasLayerOrdering.questKey(state.contextMenu.contextQuestId);
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedGroup, layerKey, true)) {
            arrangeActions.add(ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.bring_to_front"), "up", TabletColors.INTERACTIVE, withCleanup(canvasViewport, state, () -> {
                CanvasLayerMutations.moveQuestLayer(state, selectedGroup, state.contextMenu.contextQuestId, true);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=bring_to_front target=quest id={}", state.contextMenu.contextQuestId);
            })));
        }
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedGroup, layerKey, false)) {
            arrangeActions.add(ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.send_to_back"), "down", TabletColors.TEXT_MUTED, withCleanup(canvasViewport, state, () -> {
                CanvasLayerMutations.moveQuestLayer(state, selectedGroup, state.contextMenu.contextQuestId, false);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=send_to_back target=quest id={}", state.contextMenu.contextQuestId);
            })));
        }
        if (!arrangeActions.isEmpty()) {
            actions.add(ContextActionFactory.submenu(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_ORDER), "arrow-up-down", TabletColors.INTERACTIVE, arrangeActions));
        }
    }

    private static void addQuestCopyAndDeleteActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player) {
        if (CanvasContextMenuSupport.canCopyContext(canvasViewport, state)) {
            actions.add(ContextActionFactory.copy(() -> {
                CanvasContextMenuSupport.copyContextToClipboard(canvasViewport, state);
                ContextMenuController.clearDeleteConfirm(state);
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextDeleteController.canDeleteContext(state)) {
            String deleteKey = CanvasContextDeleteController.deleteConfirmKey(state);
            actions.add(ContextActionFactory.delete(state, deleteKey, TabletTranslationKeys.text(TabletTranslationKeys.COMMON_DELETE), () -> {
                CanvasContextDeleteController.runDeleteAction(player, state);
                canvasViewport.refresh();
            }));
        }
    }

    private static Runnable withCleanup(CanvasViewport canvasViewport, TabletUiState state, Runnable action) {
        return () -> {
            action.run();
            ContextMenuController.clearDeleteConfirm(state);
            canvasViewport.refresh();
        };
    }
}
