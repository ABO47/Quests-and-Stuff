package com.abo47.questsandstuff.client.tablet.quest.details.task;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class QuestTaskEditActions {
    private QuestTaskEditActions() {
    }

    static void applyIconPick(Player player, TabletUiState state, String entry) {
        QuestTaskPickerApplyActions.applyIconPick(player, state, entry);
    }

    static void applyInventoryItemPick(Player player, TabletUiState state, ItemStack stack) {
        QuestTaskPickerApplyActions.applyInventoryItemPick(player, state, stack);
    }

    static void applyBiomePick(Player player, TabletUiState state, String biome) {
        QuestTaskPickerApplyActions.applyBiomePick(player, state, biome);
    }

    static void applyAdvancementPick(Player player, TabletUiState state, String advancement) {
        QuestTaskPickerApplyActions.applyAdvancementPick(player, state, advancement);
    }

    static void applyRecipePick(Player player, TabletUiState state, String recipe) {
        QuestTaskPickerApplyActions.applyRecipePick(player, state, recipe);
    }

    static void applyStructurePick(Player player, TabletUiState state, String structure) {
        QuestTaskPickerApplyActions.applyStructurePick(player, state, structure);
    }

    static void applyBlockPick(Player player, TabletUiState state, String block) {
        QuestTaskPickerApplyActions.applyBlockPick(player, state, block);
    }

    static void applyStatPick(Player player, TabletUiState state, String stat) {
        QuestTaskPickerApplyActions.applyStatPick(player, state, stat);
    }

    static void applyDimensionPick(Player player, TabletUiState state, String dimension) {
        QuestTaskPickerApplyActions.applyDimensionPick(player, state, dimension);
    }

    static void applyLootTablePick(Player player, TabletUiState state, String lootTable) {
        QuestTaskPickerApplyActions.applyLootTablePick(player, state, lootTable);
    }

    static void applyItemLockPick(Player player, TabletUiState state, String entry) {
        QuestTaskPickerApplyActions.applyItemLockPick(player, state, entry);
    }

    static void removeItemLock(Player player, TabletUiState state, String questId, String taskId, String entry) {
        QuestTaskPickerApplyActions.removeItemLock(player, state, questId, taskId, entry);
    }

    static void beginTaskAdd(Player player, TabletUiState state, String questId, CompoundTag quest, String typePath) {
        QuestTaskTaskEditActions.beginTaskAdd(player, state, questId, quest, typePath);
    }

    static void beginTaskChange(Player player, TabletUiState state, String questId, String id, String typePath) {
        QuestTaskTaskEditActions.beginTaskChange(player, state, questId, id, typePath);
    }

    static void beginRewardAdd(Player player, TabletUiState state, String questId, CompoundTag quest, String typePath) {
        QuestTaskRewardEditActions.beginRewardAdd(player, state, questId, quest, typePath);
    }

    static void beginRewardChange(Player player, TabletUiState state, String questId, String id, String typePath) {
        QuestTaskRewardEditActions.beginRewardChange(player, state, questId, id, typePath);
    }

    static void openCommandRewardEditor(TabletUiState state, String questId, String id, String command, String title, String icon) {
        QuestTaskRewardEditActions.openCommandRewardEditor(state, questId, id, command, title, icon);
    }

    static void openExistingCommandRewardEditor(TabletUiState state, String questId, String id) {
        QuestTaskRewardEditActions.openExistingCommandRewardEditor(state, questId, id);
    }

    static void openTaskRenameEditor(TabletUiState state, String questId, String id, boolean task) {
        QuestTaskRenameActions.openTaskRenameEditor(state, questId, id, task);
    }

    static void putTaskTitle(Player player, String questId, String id, String title, boolean task) {
        QuestTaskRenameActions.putTaskTitle(player, questId, id, title, task);
    }

    public static String taskIcon(String questId, String id, boolean task) {
        return QuestTaskIconActions.taskIcon(questId, id, task);
    }

    public static boolean isEntityTaskIcon(String questId, String id, boolean task) {
        return QuestTaskIconActions.isEntityTaskIcon(questId, id, task);
    }

    public static void putTaskIcon(Player player, String questId, String id, String icon, boolean task) {
        QuestTaskIconActions.putTaskIcon(player, questId, id, icon, task);
    }

    public static void putTaskIcon(Player player, String questId, String id, String icon, boolean task, boolean sync) {
        QuestTaskIconActions.putTaskIcon(player, questId, id, icon, task, sync);
    }
}
