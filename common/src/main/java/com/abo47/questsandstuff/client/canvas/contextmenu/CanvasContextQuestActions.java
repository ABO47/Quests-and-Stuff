package com.abo47.questsandstuff.client.canvas.contextmenu;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.entity.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletWidgetCoordinates;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.List;

final class CanvasContextQuestActions {
    private CanvasContextQuestActions() {
    }

    static void addQuestActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedGroup) {
        if (state.contextMenuTarget != ContextMenuTarget.QUEST || state.contextQuestId.isBlank()) {
            return;
        }
        CompoundTag questTag = ClientQuestCache.quest(state.contextQuestId);
        if (state.contextQuestCompletionSoundMenuOpen) {
            addCompletionSoundActions(actions, canvasViewport, state, questTag);
            return;
        }
        if (CanvasContextMenuSupport.hasOtherQuest(canvasViewport, state.contextQuestId)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.connect_to"), "connect", ModColors.SUCCESS, () -> {
                state.connectSourceQuestId = state.contextQuestId;
                state.connectSourceQuestIds.clear();
                if (state.selectedQuestIds.contains(state.contextQuestId) && state.selectedQuestIds.size() > 1) {
                    state.connectSourceQuestIds.addAll(state.selectedQuestIds);
                } else {
                    state.selectedQuestIds.clear();
                    state.selectedQuestIds.add(state.contextQuestId);
                    state.connectSourceQuestIds.add(state.contextQuestId);
                }
                state.contextDeleteConfirmKey = "";
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=connect_to sources={}", state.connectSourceQuestIds);
                canvasViewport.refresh();
            }));
        }
        actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.open_quest"), "open", ModColors.INTERACTIVE, () -> {
            openQuestDetails(canvasViewport, state);
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=open_quest quest={}", state.contextQuestId);
            canvasViewport.refresh();
        }));
        actions.add(ContextActions.promoted(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_CHANGE_TITLE), "rename", ModColors.INTERACTIVE, () -> {
            EditorCommandClient.beginQuestTitleChange(state, state.contextQuestId);
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_title quest={}", state.contextQuestId);
            canvasViewport.refresh();
        }));
        QuestCardLayout contextQuest = canvasViewport.cardLookup().get(state.contextQuestId);
        if (CanvasGridFitController.canFitQuestToGrid(state, contextQuest)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "grid", ModColors.INTERACTIVE, () -> {
                boolean changed = CanvasGridFitController.fitQuestToGrid(player, state, contextQuest);
                state.contextDeleteConfirmKey = "";
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=quest id={} changed={}", state.contextQuestId, changed);
                canvasViewport.refresh();
            }));
        }
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.reset_quest"), "reset_quest", ModColors.WARNING, () -> {
            EditorCommandClient.resetQuestProgress(player, state.contextQuestId);
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=reset_quest quest={}", state.contextQuestId);
            canvasViewport.refresh();
        }));
        addQuestRepeatableAction(actions, canvasViewport, state, player, questTag);
        addQuestPrerequisiteActions(actions, canvasViewport, state, player, questTag);
        addQuestVisibilityAction(actions, canvasViewport, state, player, questTag);
        addCompletionSoundActions(actions, canvasViewport, state, questTag);
        actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.menu.change_icon"), "icon", ModColors.INTERACTIVE, () -> {
            EntityIconControls.openIconPicker(state, EntityIconControls.IconPickerTarget.quest(state.contextQuestId));
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_icon quest={}", state.contextQuestId);
            canvasViewport.refresh();
        }));
        EntityIconControls.addEntityVariantAndMotionActions(
                actions,
                state,
                questTag.getString("icon"),
                ModalTargets.questIcon(state.contextQuestId),
                () -> state.contextMenuOpen = false,
                () -> {
                    EntityMotionEditor.openQuestIcon(state, state.contextQuestId, state.contextMenuX, state.contextMenuY);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=edit_entity_icon_motion quest={}", state.contextQuestId);
                },
                canvasViewport::refresh
        );
        addQuestLayerActions(actions, canvasViewport, state, selectedGroup);
    }

    private static void addCompletionSoundActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, CompoundTag questTag) {
        if (!state.contextQuestCompletionSoundMenuOpen) {
            actions.add(new ContextAction(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_CHANGE_COMPLETION_SOUND), "audio-lines", ModColors.INTERACTIVE, false, () -> {
                state.contextQuestCompletionSoundMenuOpen = true;
                state.contextMenuScroll = 0;
                state.contextDeleteConfirmKey = "";
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=completion_sound_menu quest={}", state.contextQuestId);
                canvasViewport.refresh();
            }));
            return;
        }
        actions.add(new ContextAction(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_USE_GAME_SOUND), "audio-lines", ModColors.INTERACTIVE, () -> {
            String sound = questTag.getString("completion_sound");
            ModalOpenActions.openQuestGameSoundPicker(state, state.contextQuestId, sound);
            state.contextQuestCompletionSoundMenuOpen = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=completion_sound_game quest={} sound={}", state.contextQuestId, sound);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_USE_CUSTOM_SOUND), "audio-lines", ModColors.INTERACTIVE, () -> {
            String sound = questTag.getString("completion_sound");
            ModalOpenActions.openQuestCustomCompletionSoundPicker(state, state.contextQuestId, sound);
            state.contextQuestCompletionSoundMenuOpen = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=completion_sound_custom quest={} sound={}", state.contextQuestId, sound);
            canvasViewport.refresh();
        }));
    }

    private static void openQuestDetails(CanvasViewport canvasViewport, TabletUiState state) {
        QuestCardLayout card = canvasViewport.cardLookup().get(state.contextQuestId);
        int viewportScreenX = TabletWidgetCoordinates.screenX(canvasViewport, state.canvasPanelX + state.canvasViewportX);
        int viewportScreenY = TabletWidgetCoordinates.screenY(canvasViewport, state.canvasPanelY + state.canvasViewportY);
        if (card == null) {
            QuestDetailsWindow.openAtSource(
                    state,
                    state.contextQuestId,
                    viewportScreenX + state.contextMenuX,
                    viewportScreenY + state.contextMenuY,
                    1,
                    1
            );
            return;
        }
        QuestDetailsWindow.openAtSource(
                state,
                state.contextQuestId,
                viewportScreenX + card.x(),
                viewportScreenY + card.y(),
                card.width(),
                card.height()
        );
    }

    private static void addQuestPrerequisiteActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        ListTag prerequisites = questTag.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
        for (int i = 0; i < prerequisites.size(); i++) {
            String prerequisiteId = prerequisites.getString(i);
            String prerequisiteTitle = CanvasContextMenuSupport.readableQuestTitle(prerequisiteId);
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.remove_prerequisite", prerequisiteTitle), "delete", ModColors.WARNING, () -> {
                EditorCommandClient.runPrerequisiteAction(player, state.contextQuestId, prerequisiteId, false);
                state.contextDeleteConfirmKey = "";
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_prerequisite quest={} prerequisite={}", state.contextQuestId, prerequisiteId);
                canvasViewport.refresh();
            }));
        }
    }

    private static void addQuestRepeatableAction(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        boolean repeatable = questTag.getBoolean("repeatable");
        actions.add(new ContextAction(
                CanvasContextMenuController.tr(repeatable ? QuestVocabulary.CONTEXT_MAKE_QUEST_NOT_REPEATABLE : QuestVocabulary.CONTEXT_MAKE_QUEST_REPEATABLE),
                repeatable ? "repeat-off" : "repeat",
                repeatable ? ModColors.SUCCESS : ModColors.INTERACTIVE,
                () -> {
                    EditorCommandClient.setQuestRepeatable(player, state.contextQuestId, !repeatable);
                    state.contextDeleteConfirmKey = "";
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=quest_repeatable quest={} enabled={}", state.contextQuestId, !repeatable);
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
                    EditorCommandClient.setQuestHiddenMode(player, state.contextQuestId, lockUntilUnlocked ? prerequisitesVisible : locked);
                    if (questTag.getBoolean("visual_hidden")) {
                        EditorCommandClient.setQuestVisualHidden(player, state.contextQuestId, false);
                    }
                    state.contextDeleteConfirmKey = "";
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=quest_lock_until_unlocked quest={} enabled={}", state.contextQuestId, !lockUntilUnlocked);
                    canvasViewport.refresh();
                }));
    }

    private static void addQuestLayerActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup) {
        String layerKey = CanvasLayerOrdering.questKey(state.contextQuestId);
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedGroup, layerKey, true)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.bring_to_front"), "up", ModColors.INTERACTIVE, () -> {
                CanvasRenderer.moveQuestLayer(state, selectedGroup, state.contextQuestId, true);
                state.contextDeleteConfirmKey = "";
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=bring_to_front target=quest id={}", state.contextQuestId);
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedGroup, layerKey, false)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.send_to_back"), "down", ModColors.TEXT_MUTED, () -> {
                CanvasRenderer.moveQuestLayer(state, selectedGroup, state.contextQuestId, false);
                state.contextDeleteConfirmKey = "";
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=send_to_back target=quest id={}", state.contextQuestId);
                canvasViewport.refresh();
            }));
        }
    }
}
