package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardActions;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionClipboard;
import com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionModel;
import com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionPanel;
import com.abo47.questsandstuff.client.tablet.quest.details.objective.QuestDetailsObjectivesPanel;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.quest.reward.QuestRewardClaimActions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
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

    static void applyAdvancementPick(Player player, TabletUiState state, String advancement) {
        QuestDetailsObjectivesPanel.applyAdvancementPick(player, state, advancement);
    }

    static void applyRecipePick(Player player, TabletUiState state, String recipe) {
        if (QuestDetailsDescriptionPanel.applyRecipePick(player, state, recipe)) {
            return;
        }
        if (CanvasRecipeCardActions.applyRecipePick(player, state, recipe)) {
            return;
        }
        QuestDetailsObjectivesPanel.applyRecipePick(player, state, recipe);
    }

    static void applyStructurePick(Player player, TabletUiState state, String structure) {
        QuestDetailsObjectivesPanel.applyStructurePick(player, state, structure);
    }

    static void applyBlockPick(Player player, TabletUiState state, String block) {
        if (QuestDetailsDescriptionPanel.applyBlockPick(player, state, block)) {
            return;
        }
        QuestDetailsObjectivesPanel.applyBlockPick(player, state, block);
    }

    static void applyStatPick(Player player, TabletUiState state, String stat) {
        QuestDetailsObjectivesPanel.applyStatPick(player, state, stat);
    }

    static void applyDimensionPick(Player player, TabletUiState state, String dimension) {
        QuestDetailsObjectivesPanel.applyDimensionPick(player, state, dimension);
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
        if (state == null || !state.questDetailsOpen || !QuestDetailsEditState.canEdit(state)) {
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
        if (!QuestDetailsEditState.canEdit(state)) {
            return false;
        }
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
        if (!QuestDetailsEditState.canEdit(state)) {
            return false;
        }
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
        if (!QuestDetailsEditState.canEdit(state)) {
            return false;
        }
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
        if (!QuestDetailsEditState.canEdit(state)) {
            return false;
        }
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
        QuestRewardClaimActions.claimAll(player, trimmed);
    }

    static void openIconPicker(TabletUiState state, String target) {
        ModalOpenActions.openQuestDetailsIconPicker(state, target);
    }

    static void openBiomePicker(TabletUiState state, String target) {
        ModalOpenActions.openBiomePicker(state, target);
    }

    static void openAdvancementPicker(TabletUiState state, String target) {
        ModalOpenActions.openAdvancementPicker(state, target);
    }

    static void openRecipePicker(TabletUiState state, String target) {
        ModalOpenActions.openRecipePicker(state, target);
    }

    static void openStructurePicker(TabletUiState state, String target) {
        ModalOpenActions.openStructurePicker(state, target);
    }

    static void openBlockPicker(TabletUiState state, String target) {
        ModalOpenActions.openBlockPicker(state, target);
    }

    static void openStatPicker(TabletUiState state, String target) {
        ModalOpenActions.openStatPicker(state, target);
    }

    static void openDimensionPicker(TabletUiState state, String target) {
        ModalOpenActions.openDimensionPicker(state, target);
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
        if (state == null || !state.questDetailsOpen || !QuestDetailsEditState.canEdit(state)) {
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
