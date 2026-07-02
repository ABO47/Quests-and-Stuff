package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class QuestObjectiveEditActions {
    private QuestObjectiveEditActions() {
    }

    static void applyIconPick(Player player, TabletUiState state, String entry) {
        QuestObjectivePickerApplyActions.applyIconPick(player, state, entry);
    }

    static void applyInventoryItemPick(Player player, TabletUiState state, ItemStack stack) {
        QuestObjectivePickerApplyActions.applyInventoryItemPick(player, state, stack);
    }

    static void applyBiomePick(Player player, TabletUiState state, String biome) {
        QuestObjectivePickerApplyActions.applyBiomePick(player, state, biome);
    }

    static void applyAdvancementPick(Player player, TabletUiState state, String advancement) {
        QuestObjectivePickerApplyActions.applyAdvancementPick(player, state, advancement);
    }

    static void applyRecipePick(Player player, TabletUiState state, String recipe) {
        QuestObjectivePickerApplyActions.applyRecipePick(player, state, recipe);
    }

    static void applyStructurePick(Player player, TabletUiState state, String structure) {
        QuestObjectivePickerApplyActions.applyStructurePick(player, state, structure);
    }

    static void applyBlockPick(Player player, TabletUiState state, String block) {
        QuestObjectivePickerApplyActions.applyBlockPick(player, state, block);
    }

    static void applyStatPick(Player player, TabletUiState state, String stat) {
        QuestObjectivePickerApplyActions.applyStatPick(player, state, stat);
    }

    static void applyDimensionPick(Player player, TabletUiState state, String dimension) {
        QuestObjectivePickerApplyActions.applyDimensionPick(player, state, dimension);
    }

    static void applyLootTablePick(Player player, TabletUiState state, String lootTable) {
        QuestObjectivePickerApplyActions.applyLootTablePick(player, state, lootTable);
    }

    static void beginTaskAdd(Player player, TabletUiState state, String questId, CompoundTag quest, String typePath) {
        QuestObjectiveTaskEditActions.beginTaskAdd(player, state, questId, quest, typePath);
    }

    static void beginTaskChange(Player player, TabletUiState state, String questId, String id, String typePath) {
        QuestObjectiveTaskEditActions.beginTaskChange(player, state, questId, id, typePath);
    }

    static void beginRewardAdd(Player player, TabletUiState state, String questId, CompoundTag quest, String typePath) {
        QuestObjectiveRewardEditActions.beginRewardAdd(player, state, questId, quest, typePath);
    }

    static void beginRewardChange(Player player, TabletUiState state, String questId, String id, String typePath) {
        QuestObjectiveRewardEditActions.beginRewardChange(player, state, questId, id, typePath);
    }

    static void openCommandRewardEditor(TabletUiState state, String questId, String id, String command, String title, String icon) {
        QuestObjectiveRewardEditActions.openCommandRewardEditor(state, questId, id, command, title, icon);
    }

    static void openExistingCommandRewardEditor(TabletUiState state, String questId, String id) {
        QuestObjectiveRewardEditActions.openExistingCommandRewardEditor(state, questId, id);
    }

    static void openObjectiveRenameEditor(TabletUiState state, String questId, String id, boolean task) {
        QuestObjectiveRenameActions.openObjectiveRenameEditor(state, questId, id, task);
    }

    static void putObjectiveTitle(Player player, String questId, String id, String title, boolean task) {
        QuestObjectiveRenameActions.putObjectiveTitle(player, questId, id, title, task);
    }

    public static String objectiveIcon(String questId, String id, boolean task) {
        return QuestObjectiveIconActions.objectiveIcon(questId, id, task);
    }

    public static boolean isEntityObjectiveIcon(String questId, String id, boolean task) {
        return QuestObjectiveIconActions.isEntityObjectiveIcon(questId, id, task);
    }

    public static void putObjectiveIcon(Player player, String questId, String id, String icon, boolean task) {
        QuestObjectiveIconActions.putObjectiveIcon(player, questId, id, icon, task);
    }

    public static void putObjectiveIcon(Player player, String questId, String id, String icon, boolean task, boolean sync) {
        QuestObjectiveIconActions.putObjectiveIcon(player, questId, id, icon, task, sync);
    }
}
