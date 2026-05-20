package com.abo47.questsandstuff.client.canvas.contextmenu;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
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
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.open_quest"), "open", ModColors.INTERACTIVE, () -> {
            openQuestDetails(canvasViewport, state);
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=open_quest quest={}", state.contextQuestId);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_CHANGE_TITLE), "rename", ModColors.INTERACTIVE, () -> {
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
        CompoundTag questTag = ClientQuestCache.quest(state.contextQuestId);
        addQuestPrerequisiteActions(actions, canvasViewport, state, player, questTag);
        addQuestVisibilityAction(actions, canvasViewport, state, player, questTag);
        actions.add(new ContextAction(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_CHANGE_COMPLETION_SOUND), "audio-lines", ModColors.INTERACTIVE, () -> {
            String sound = questTag.getString("completion_sound");
            ModalOpenActions.openQuestCompletionSoundPicker(state, state.contextQuestId, sound);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_completion_sound quest={} sound={}", state.contextQuestId, state.assetSelected);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.menu.change_icon"), "icon", ModColors.INTERACTIVE, () -> {
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

    private static void openQuestDetails(CanvasViewport canvasViewport, TabletUiState state) {
        QuestCardLayout card = canvasViewport.cardLookup().get(state.contextQuestId);
        if (card == null) {
            QuestDetailsWindow.openAtSource(
                    state,
                    state.contextQuestId,
                    canvasViewport.getPositionX() + state.contextMenuX,
                    canvasViewport.getPositionY() + state.contextMenuY,
                    1,
                    1
            );
            return;
        }
        QuestDetailsWindow.openAtSource(
                state,
                state.contextQuestId,
                canvasViewport.getPositionX() + card.x(),
                canvasViewport.getPositionY() + card.y(),
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

    private static void addQuestVisibilityAction(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        String hiddenMode = questTag.getString("hidden_mode");
        String prerequisitesVisible = QuestVisibilityMode.PREREQUISITES_VISIBLE.serializedName();
        boolean backendHidden = !hiddenMode.isBlank() && !prerequisitesVisible.equals(hiddenMode);
        boolean hiddenForUser = questTag.getBoolean("visual_hidden") || backendHidden;
        actions.add(new ContextAction(hiddenForUser ? CanvasContextMenuController.tr("ui.questsandstuff.context.reveal_quest") : CanvasContextMenuController.tr("ui.questsandstuff.context.hide_quest_until_unlocked"), hiddenForUser ? "eye" : "eye-off", ModColors.INTERACTIVE, () -> {
            if (!prerequisitesVisible.equals(questTag.getString("hidden_mode"))) {
                EditorCommandClient.setQuestHiddenMode(player, state.contextQuestId, prerequisitesVisible);
            }
            EditorCommandClient.setQuestVisualHidden(player, state.contextQuestId, !hiddenForUser);
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=quest_visual_hidden quest={} hidden={}", state.contextQuestId, !hiddenForUser);
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
