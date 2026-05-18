package com.abo47.questsandstuff.client.tablet.details;

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

    public static void rebuild(WidgetGroup layer, TabletUiState state, Player player, Runnable refresh) {
        QuestDetailsWindowLayout.rebuild(layer, state, player, refresh);
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

    public static void applyLootTablePick(Player player, TabletUiState state, String lootTable) {
        QuestDetailsWindowActions.applyLootTablePick(player, state, lootTable);
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

    public static void openLootTablePicker(TabletUiState state, String target) {
        QuestDetailsWindowActions.openLootTablePicker(state, target);
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
