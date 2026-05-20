package com.abo47.questsandstuff.client.tablet.details;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.details.description.QuestDetailsDescriptionClipboard;
import com.abo47.questsandstuff.client.tablet.details.description.QuestDetailsDescriptionModel;
import com.abo47.questsandstuff.client.tablet.details.description.QuestDetailsDescriptionPanel;
import com.abo47.questsandstuff.client.tablet.details.objective.QuestDetailsObjectivesPanel;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.network.QuestNetwork;
import com.abo47.questsandstuff.network.runtime.C2SClaimAllRewardsPacket;
import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

final class QuestDetailsWindowActions {
    private QuestDetailsWindowActions() {
    }

    static void applyIconPick(Player player, TabletUiState state, String entry) {
        if (QuestDetailsDescriptionPanel.applyIconPick(player, state, entry)) {
            return;
        }
        QuestDetailsObjectivesPanel.applyIconPick(player, state, entry);
    }

    static void applyBiomePick(Player player, TabletUiState state, String biome) {
        QuestDetailsObjectivesPanel.applyBiomePick(player, state, biome);
    }

    static void applyLootTablePick(Player player, TabletUiState state, String lootTable) {
        QuestDetailsObjectivesPanel.applyLootTablePick(player, state, lootTable);
    }

    static void applyInventoryItemPick(Player player, TabletUiState state, ItemStack stack) {
        QuestDetailsObjectivesPanel.applyInventoryItemPick(player, state, stack);
    }

    static void applyAssetPick(Player player, TabletUiState state, String asset) {
        QuestDetailsDescriptionPanel.applyAssetPick(player, state, asset);
    }

    static String descriptionImageAsset(String questId, String imageId) {
        return QuestDetailsDescriptionPanel.imageAsset(questId, imageId);
    }

    static void applyEntityVariantPick(Player player, TabletUiState state, String questId, String imageId, String variantKey) {
        QuestDetailsDescriptionPanel.applyEntityVariantPick(player, state, questId, imageId, variantKey);
    }

    static void applyTextColor(Player player, TabletUiState state, String target, int color) {
        QuestDetailsDescriptionPanel.applyTextColor(player, state, target, color);
    }

    static boolean handleClipboardShortcut(Player player, TabletUiState state, int keyCode) {
        String questId = editableQuestId(state);
        if (questId.isBlank()) {
            return false;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        if (keyCode == GLFW.GLFW_KEY_C) {
            return QuestDetailsDescriptionPanel.copySelectedDescriptionToClipboard(state, model);
        }
        if (keyCode == GLFW.GLFW_KEY_V) {
            int[] viewport = viewport(state);
            return QuestDetailsDescriptionClipboard.pasteFromKeyboard(player, state, questId, model, viewport[2], viewport[3]);
        }
        return false;
    }

    static boolean beginSelectedRename(TabletUiState state) {
        if (state == null || !state.questDetailsOpen || !state.canEdit || !state.questDetailsEditMode) {
            return false;
        }
        String questId = questId(state);
        if (!state.questDetailsSelectedObjectiveId.isBlank()) {
            QuestDetailsObjectivesPanel.openObjectiveRenameEditor(
                    state,
                    questId,
                    state.questDetailsSelectedObjectiveId,
                    "requirement".equals(state.questDetailsSelectedObjectiveKind)
            );
            return true;
        }
        if (!questId.isBlank()) {
            EditorCommandClient.beginQuestTitleChange(state, questId);
            state.questDetailsTitleFocused = true;
            return true;
        }
        return false;
    }

    static boolean deleteSelected(Player player, TabletUiState state) {
        String questId = questId(state);
        if (questId.isBlank()) {
            return false;
        }
        if (QuestDetailsObjectivesPanel.deleteSelectedObjective(player, state, questId)) {
            return true;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        return QuestDetailsDescriptionPanel.deleteSelectedDescription(player, state, questId, model);
    }

    static boolean duplicateSelected(Player player, TabletUiState state) {
        String questId = questId(state);
        if (questId.isBlank() || !state.questDetailsSelectedObjectiveId.isBlank()) {
            return false;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        int[] viewport = viewport(state);
        return QuestDetailsDescriptionPanel.copySelectedDescriptionToClipboard(state, model)
                && QuestDetailsDescriptionClipboard.pasteFromKeyboard(player, state, questId, model, viewport[2], viewport[3]);
    }

    static boolean selectAllDescription(TabletUiState state) {
        String questId = questId(state);
        if (questId.isBlank()) {
            return false;
        }
        state.questDetailsSelectedObjectiveKind = "";
        state.questDetailsSelectedObjectiveId = "";
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        return QuestDetailsDescriptionPanel.selectAllDescription(state, model);
    }

    static boolean nudgeSelected(Player player, TabletUiState state, int dx, int dy) {
        String questId = questId(state);
        if (questId.isBlank() || !state.questDetailsSelectedObjectiveId.isBlank()) {
            return false;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        return QuestDetailsDescriptionClipboard.nudgeSelected(player, state, questId, model, dx, dy);
    }

    static void claimAll(Player player, String questId) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        String trimmed = questId.trim();
        if (player instanceof ServerPlayer serverPlayer) {
            QuestServices.engine(serverPlayer.server).claimAllRewards(serverPlayer, trimmed);
        } else {
            QuestNetwork.sendToServer(new C2SClaimAllRewardsPacket(trimmed));
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details claim_all quest={}", trimmed);
    }

    static void openIconPicker(TabletUiState state, String target) {
        ModalOpenActions.openQuestDetailsIconPicker(state, target);
    }

    static void openBiomePicker(TabletUiState state, String target) {
        ModalOpenActions.openBiomePicker(state, target);
    }

    static void openLootTablePicker(TabletUiState state, String target) {
        ModalOpenActions.openLootTablePicker(state, target);
    }

    static void openItemInventoryPicker(TabletUiState state, String target) {
        ModalOpenActions.openItemInventoryPicker(state, target);
    }

    static void openAssetPicker(TabletUiState state, String target) {
        ModalOpenActions.openAssetPicker(state, target);
    }

    private static String editableQuestId(TabletUiState state) {
        if (state == null || !state.questDetailsOpen || !state.canEdit || !state.questDetailsEditMode) {
            return "";
        }
        return questId(state);
    }

    private static String questId(TabletUiState state) {
        return state == null ? "" : state.questDetailsQuestId == null ? "" : state.questDetailsQuestId.trim();
    }

    private static int[] viewport(TabletUiState state) {
        int leftW = QuestDetailsWindowGeometry.leftPanelWidth(state);
        int canvasW = QuestDetailsWindowGeometry.canvasPanelWidth(leftW);
        return QuestDetailsWindowGeometry.mainCanvasViewport(state, canvasW);
    }
}
