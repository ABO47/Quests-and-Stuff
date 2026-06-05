package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

public final class QuestDetailsWindow {
    static final int WINDOW_W = TabletUiFactory.ROOT_W;
    static final int WINDOW_H = TabletUiFactory.ROOT_H;
    public static final int CONTENT_INSET = 6;
    public static final int TOP_Y = CONTENT_INSET;
    public static final int HEADER_H = 14;
    static final int HEADER_GAP = 4;
    static final int TOOL_SIZE = HEADER_H;

    private QuestDetailsWindow() {
    }

    public static void open(TabletUiState state, String questId) {
        QuestDetailsWindowLifecycle.open(state, questId);
    }

    public static void openAtSource(TabletUiState state, String questId, int sourceX, int sourceY, int sourceW, int sourceH) {
        QuestDetailsWindowLifecycle.openAtSource(state, questId, sourceX, sourceY, sourceW, sourceH);
    }

    public static void rebuild(WidgetGroup layer, TabletUiState state, Player player, Runnable refresh) {
        QuestDetailsWindowLayout.rebuild(layer, state, player, refresh);
    }

    public static void syncScreenOrigin(WidgetGroup layer, TabletUiState state) {
        QuestDetailsWindowLayout.syncScreenOrigin(layer, state);
    }

    public static boolean isVisible(TabletUiState state) {
        return state != null && (state.questDetailsOpen || state.questDetailsClosing);
    }

    public static boolean isInteractive(TabletUiState state) {
        return state != null && state.questDetailsOpen && !state.questDetailsClosing;
    }

    public static boolean finishCloseIfDone(TabletUiState state) {
        return QuestDetailsWindowLifecycle.finishCloseIfDone(state);
    }

    public static boolean isInside(TabletUiState state, double mouseX, double mouseY) {
        return QuestDetailsWindowHitTest.isInside(state, mouseX, mouseY);
    }

    public static boolean isContextMenuHit(TabletUiState state, double mouseX, double mouseY) {
        return QuestDetailsWindowHitTest.isContextMenuHit(state, mouseX, mouseY);
    }

    public static boolean isTextStyleMenuHit(TabletUiState state, double mouseX, double mouseY) {
        return QuestDetailsWindowHitTest.isTextStyleMenuHit(state, mouseX, mouseY);
    }

    public static boolean isTextStyleOwnerHit(TabletUiState state, double mouseX, double mouseY) {
        return QuestDetailsWindowHitTest.isTextStyleOwnerHit(state, mouseX, mouseY);
    }

    public static boolean isTextEditorHit(TabletUiState state, double mouseX, double mouseY) {
        return QuestDetailsWindowHitTest.isTextEditorHit(state, mouseX, mouseY);
    }

    public static void applyIconPick(Player player, TabletUiState state, String entry) {
        QuestDetailsWindowActions.applyIconPick(player, state, entry);
    }

    public static void applyBiomePick(Player player, TabletUiState state, String biome) {
        QuestDetailsWindowActions.applyBiomePick(player, state, biome);
    }

    public static void applyAdvancementPick(Player player, TabletUiState state, String advancement) {
        QuestDetailsWindowActions.applyAdvancementPick(player, state, advancement);
    }

    public static void applyRecipePick(Player player, TabletUiState state, String recipe) {
        QuestDetailsWindowActions.applyRecipePick(player, state, recipe);
    }

    public static void applyStructurePick(Player player, TabletUiState state, String structure) {
        QuestDetailsWindowActions.applyStructurePick(player, state, structure);
    }

    public static void applyBlockPick(Player player, TabletUiState state, String block) {
        QuestDetailsWindowActions.applyBlockPick(player, state, block);
    }

    public static void applyStatPick(Player player, TabletUiState state, String stat) {
        QuestDetailsWindowActions.applyStatPick(player, state, stat);
    }

    public static void applyDimensionPick(Player player, TabletUiState state, String dimension) {
        QuestDetailsWindowActions.applyDimensionPick(player, state, dimension);
    }

    public static void applyLootTablePick(Player player, TabletUiState state, String lootTable) {
        QuestDetailsWindowActions.applyLootTablePick(player, state, lootTable);
    }

    public static void applyInventoryItemPick(Player player, TabletUiState state, net.minecraft.world.item.ItemStack stack) {
        QuestDetailsWindowActions.applyInventoryItemPick(player, state, stack);
    }

    public static void applyAssetPick(Player player, TabletUiState state, String asset) {
        QuestDetailsWindowActions.applyAssetPick(player, state, asset);
    }

    public static String descriptionImageAsset(String questId, String imageId) {
        return QuestDetailsWindowActions.descriptionImageAsset(questId, imageId);
    }

    public static void applyEntityVariantPick(Player player, TabletUiState state, String questId, String imageId, String variantKey) {
        QuestDetailsWindowActions.applyEntityVariantPick(player, state, questId, imageId, variantKey);
    }

    public static void applyTextColor(Player player, TabletUiState state, String target, int color) {
        QuestDetailsWindowActions.applyTextColor(player, state, target, color);
    }

    public static boolean handleClipboardShortcut(Player player, TabletUiState state, int keyCode) {
        return QuestDetailsWindowActions.handleClipboardShortcut(player, state, keyCode);
    }

    public static boolean beginSelectedRename(TabletUiState state) {
        return QuestDetailsWindowActions.beginSelectedRename(state);
    }

    public static boolean deleteSelected(Player player, TabletUiState state) {
        return QuestDetailsWindowActions.deleteSelected(player, state);
    }

    public static boolean duplicateSelected(Player player, TabletUiState state) {
        return QuestDetailsWindowActions.duplicateSelected(player, state);
    }

    public static boolean selectAllDescription(TabletUiState state) {
        return QuestDetailsWindowActions.selectAllDescription(state);
    }

    public static boolean nudgeSelected(Player player, TabletUiState state, int dx, int dy) {
        return QuestDetailsWindowActions.nudgeSelected(player, state, dx, dy);
    }

    public static void claimAll(Player player, String questId) {
        QuestDetailsWindowActions.claimAll(player, questId);
    }

    public static void openIconPicker(TabletUiState state, String target) {
        QuestDetailsWindowActions.openIconPicker(state, target);
    }

    public static void openBiomePicker(TabletUiState state, String target) {
        QuestDetailsWindowActions.openBiomePicker(state, target);
    }

    public static void openAdvancementPicker(TabletUiState state, String target) {
        QuestDetailsWindowActions.openAdvancementPicker(state, target);
    }

    public static void openRecipePicker(TabletUiState state, String target) {
        QuestDetailsWindowActions.openRecipePicker(state, target);
    }

    public static void openStructurePicker(TabletUiState state, String target) {
        QuestDetailsWindowActions.openStructurePicker(state, target);
    }

    public static void openBlockPicker(TabletUiState state, String target) {
        QuestDetailsWindowActions.openBlockPicker(state, target);
    }

    public static void openStatPicker(TabletUiState state, String target) {
        QuestDetailsWindowActions.openStatPicker(state, target);
    }

    public static void openDimensionPicker(TabletUiState state, String target) {
        QuestDetailsWindowActions.openDimensionPicker(state, target);
    }

    public static void openLootTablePicker(TabletUiState state, String target) {
        QuestDetailsWindowActions.openLootTablePicker(state, target);
    }

    public static void openItemInventoryPicker(TabletUiState state, String target) {
        QuestDetailsWindowActions.openItemInventoryPicker(state, target);
    }

    public static void openAssetPicker(TabletUiState state, String target) {
        QuestDetailsWindowActions.openAssetPicker(state, target);
    }

    public static void close(TabletUiState state) {
        QuestDetailsWindowLifecycle.close(state);
    }

    public static int leftPanelWidth(TabletUiState state) {
        return QuestDetailsWindowGeometry.leftPanelWidth(state);
    }

    public static int descriptionContentWidth(TabletUiState state) {
        return QuestDetailsWindowGeometry.descriptionContentWidth(state);
    }

    static int canvasPanelWidth(int leftW) {
        return QuestDetailsWindowGeometry.canvasPanelWidth(leftW);
    }

    static int[] mainCanvasViewport(TabletUiState state, int canvasW) {
        return QuestDetailsWindowGeometry.mainCanvasViewport(state, canvasW);
    }

    static void openAdjacentQuest(TabletUiState state, String questId, int direction) {
        QuestDetailsWindowLifecycle.openAdjacentQuest(state, questId, direction);
    }
}
