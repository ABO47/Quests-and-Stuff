package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

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
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

final class CanvasContextQuestActions {
    private CanvasContextQuestActions() {
    }

    static void addQuestActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedGroup) {
        if (state.contextMenuTarget != ContextMenuTarget.QUEST || state.contextQuestId.isBlank()) {
            return;
        }
        CompoundTag questTag = ClientQuestCache.quest(state.contextQuestId);
        state.contextQuestCompletionSoundMenuOpen = false;
        if (CanvasContextMenuSupport.hasOtherQuest(canvasViewport, state.contextQuestId)) {
            actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.connect_to"), "connect", ModColors.SUCCESS, () -> {
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
        actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.open_quest"), "open", ModColors.INTERACTIVE, () -> {
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
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "fit_grid", ModColors.INTERACTIVE, () -> {
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
        addQuestPrerequisiteActions(actions, canvasViewport, state, questTag);
        addQuestVisibilityActions(actions, canvasViewport, state, player, questTag);
        addCompletionSoundActions(actions, canvasViewport, state, questTag);
        actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.menu.change_icon"), "icon", ModColors.INTERACTIVE, () -> {
            EntityIconControls.openIconPicker(state, EntityIconControls.IconPickerTarget.quest(state.contextQuestId));
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_icon quest={}", state.contextQuestId);
            canvasViewport.refresh();
        }));
        addQuestBackgroundActions(actions, canvasViewport, state, player, questTag);
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

    private static void addQuestBackgroundActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        actions.add(ContextActions.action(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_CHANGE_QUEST_BACKGROUND), "background", ModColors.INTERACTIVE, () -> {
            ModalOpenActions.openQuestBackgroundPicker(
                    state,
                    state.contextQuestId,
                    questTag.getString("quest_background"),
                    questTag.getBoolean("quest_background_grayscale")
            );
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_quest_background quest={}", state.contextQuestId);
            canvasViewport.refresh();
        }));
        if (!QuestDisplay.DEFAULT_QUEST_BACKGROUND.equals(QuestDisplay.normalizeQuestBackground(questTag.getString("quest_background")))) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_REMOVE_BACKGROUND), "delete", ModColors.WARNING, () -> {
                EditorCommandClient.setQuestBackground(player, state.contextQuestId, QuestDisplay.DEFAULT_QUEST_BACKGROUND, false);
                state.contextDeleteConfirmKey = "";
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_quest_background quest={}", state.contextQuestId);
                canvasViewport.refresh();
            }));
        }
        addCompletionHudBackgroundActions(actions, canvasViewport, state, player, questTag);
    }

    private static void addCompletionSoundActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, CompoundTag questTag) {
        actions.add(ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_COMPLETION_SOUND), "audio-lines", ModColors.INTERACTIVE, List.of(
                ContextActions.action(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_USE_GAME_SOUND), "audio-lines", ModColors.INTERACTIVE, () -> {
                    String sound = questTag.getString("completion_sound");
                    ModalOpenActions.openQuestGameSoundPicker(state, state.contextQuestId, sound);
                    state.contextQuestCompletionSoundMenuOpen = false;
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=completion_sound_game quest={} sound={}", state.contextQuestId, sound);
                    canvasViewport.refresh();
                }),
                ContextActions.action(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_USE_CUSTOM_SOUND), "audio-lines", ModColors.INTERACTIVE, () -> {
                    String sound = questTag.getString("completion_sound");
                    ModalOpenActions.openQuestCustomCompletionSoundPicker(state, state.contextQuestId, sound);
                    state.contextQuestCompletionSoundMenuOpen = false;
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=completion_sound_custom quest={} sound={}", state.contextQuestId, sound);
                    canvasViewport.refresh();
                })
        )));
    }

    private static void addCompletionHudBackgroundActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, CompoundTag questTag) {
        String currentBackground = questTag == null ? "" : questTag.getString("completion_hud_background");
        actions.add(new ContextAction(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_CHANGE_COMPLETION_HUD_BACKGROUND), "completion_hud_background", ModColors.INTERACTIVE, () -> {
            ModalOpenActions.openQuestCompletionHudBackgroundPicker(state, state.contextQuestId, currentBackground);
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_completion_hud_background quest={}", state.contextQuestId);
            canvasViewport.refresh();
        }));
        if (!QuestDisplay.DEFAULT_COMPLETION_HUD_BACKGROUND.equals(QuestDisplay.normalizeCompletionHudBackground(currentBackground))) {
            actions.add(new ContextAction(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_REMOVE_COMPLETION_HUD_BACKGROUND), "delete", ModColors.WARNING, () -> {
                EditorCommandClient.setQuestCompletionHudBackground(player, state.contextQuestId, QuestDisplay.DEFAULT_COMPLETION_HUD_BACKGROUND);
                state.contextDeleteConfirmKey = "";
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_completion_hud_background quest={}", state.contextQuestId);
                canvasViewport.refresh();
            }));
        }
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

    private static void addQuestPrerequisiteActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, CompoundTag questTag) {
        int connectionCount = prerequisiteCount(questTag) + outgoingConnectionCount(state.contextQuestId);
        if (connectionCount <= 0) {
            return;
        }
        actions.add(ContextActions.promoted(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_PREREQUISITES_MANAGER), "connect", ModColors.INTERACTIVE, () -> {
            ModalOpenActions.openPrerequisitesManager(state, state.contextQuestId);
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=prerequisites_manager quest={} connections={}", state.contextQuestId, connectionCount);
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

    private static int prerequisiteCount(CompoundTag questTag) {
        if (questTag == null) {
            return 0;
        }
        return questTag.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING).size();
    }

    private static int outgoingConnectionCount(String questId) {
        if (questId == null || questId.isBlank()) {
            return 0;
        }
        int count = 0;
        for (var entry : ClientQuestCache.questEntries()) {
            if (questId.equals(entry.getKey()) || !hasPrerequisite(entry.getValue(), questId)) {
                continue;
            }
            count++;
        }
        return count;
    }

    private static boolean hasPrerequisite(CompoundTag questTag, String prerequisiteId) {
        if (questTag == null || prerequisiteId == null || prerequisiteId.isBlank()) {
            return false;
        }
        ListTag prerequisites = questTag.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
        for (int i = 0; i < prerequisites.size(); i++) {
            if (prerequisiteId.equals(prerequisites.getString(i))) {
                return true;
            }
        }
        return false;
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
                    state.contextDeleteConfirmKey = "";
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=quest_lock_until_unlocked quest={} enabled={}", state.contextQuestId, !lockUntilUnlocked);
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
                    EditorCommandClient.setQuestVisualHidden(player, state.contextQuestId, !hidden);
                    state.contextDeleteConfirmKey = "";
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=quest_hide_until_unlocked quest={} enabled={}", state.contextQuestId, !hidden);
                    canvasViewport.refresh();
                }));
    }

    private static void addQuestLayerActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup) {
        String layerKey = CanvasLayerOrdering.questKey(state.contextQuestId);
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedGroup, layerKey, true)) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.bring_to_front"), "up", ModColors.INTERACTIVE, () -> {
                CanvasLayerMutations.moveQuestLayer(state, selectedGroup, state.contextQuestId, true);
                state.contextDeleteConfirmKey = "";
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=bring_to_front target=quest id={}", state.contextQuestId);
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedGroup, layerKey, false)) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.send_to_back"), "down", ModColors.TEXT_MUTED, () -> {
                CanvasLayerMutations.moveQuestLayer(state, selectedGroup, state.contextQuestId, false);
                state.contextDeleteConfirmKey = "";
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=send_to_back target=quest id={}", state.contextQuestId);
                canvasViewport.refresh();
            }));
        }
    }
}
