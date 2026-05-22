package com.abo47.questsandstuff.client.tablet.details.objective;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;

public final class QuestDetailsObjectivesPanel {
    static final int SECTION_GAP = 6;
    static final int TITLE_H = 18;
    static final int CARD_H = TabletUiFactory.CHAPTER_CARD_H;
    static final int CARD_GAP = TabletUiFactory.CHAPTER_CARD_GAP;
    static final int LIST_PAD = 6;
    static final int ICON = TabletUiFactory.CONTENT_ICON_SIZE;
    static final int HEADER_H = 14;

    private QuestDetailsObjectivesPanel() {
    }

    public static void rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int x, int y, int w, int h) {
        QuestObjectiveActionWidgets.renderProgress(modal, state, player, refresh, questId, quest, x, y, w, HEADER_H);
        int sectionsY = y + HEADER_H + SECTION_GAP;
        int sectionsH = Math.max(1, h - HEADER_H - SECTION_GAP);
        int sectionH = (sectionsH - SECTION_GAP) / 2;
        QuestObjectiveSectionWidget.renderRequirements(modal, state, player, refresh, questId, quest, x, sectionsY, w, sectionH);
        QuestObjectiveSectionWidget.renderRewards(modal, state, player, refresh, questId, quest, x, sectionsY + sectionH + SECTION_GAP, w, sectionsH - sectionH - SECTION_GAP);
    }

    public static void renderTypePicker(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int modalW, int modalH) {
        QuestDetailsObjectiveMenus.renderTypePicker(modal, state, player, refresh, questId, quest, modalW, modalH);
    }

    public static void applyIconPick(Player player, TabletUiState state, String entry) {
        QuestObjectiveEditActions.applyIconPick(player, state, entry);
    }

    public static void applyBiomePick(Player player, TabletUiState state, String biome) {
        QuestObjectiveEditActions.applyBiomePick(player, state, biome);
    }

    public static void applyAdvancementPick(Player player, TabletUiState state, String advancement) {
        QuestObjectiveEditActions.applyAdvancementPick(player, state, advancement);
    }

    public static void applyStructurePick(Player player, TabletUiState state, String structure) {
        QuestObjectiveEditActions.applyStructurePick(player, state, structure);
    }

    public static void applyDimensionPick(Player player, TabletUiState state, String dimension) {
        QuestObjectiveEditActions.applyDimensionPick(player, state, dimension);
    }

    public static void applyLootTablePick(Player player, TabletUiState state, String lootTable) {
        QuestObjectiveEditActions.applyLootTablePick(player, state, lootTable);
    }

    public static void applyInventoryItemPick(Player player, TabletUiState state, ItemStack stack) {
        QuestObjectiveEditActions.applyInventoryItemPick(player, state, stack);
    }

    public static void renderContextMenu(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId) {
        QuestDetailsObjectiveMenus.renderContextMenu(modal, state, player, refresh, questId);
    }

    public static boolean handleRenameKey(Player player, TabletUiState state, int keyCode) {
        return QuestObjectiveInlineFields.handleRenameKey(player, state, keyCode);
    }

    public static void openObjectiveRenameEditor(TabletUiState state, String questId, String id, boolean task) {
        QuestObjectiveInlineFields.openObjectiveRenameEditor(state, questId, id, task);
    }

    public static boolean deleteSelectedObjective(Player player, TabletUiState state, String questId) {
        return QuestObjectiveListInteractions.deleteSelected(player, state, questId);
    }

    public static boolean moveSelectedObjective(Player player, TabletUiState state, String questId, int offset) {
        return QuestObjectiveListInteractions.moveSelected(player, state, questId, offset);
    }
}
