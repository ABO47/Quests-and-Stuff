package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

final class QuestObjectiveRewardEditActions {
    private QuestObjectiveRewardEditActions() {
    }

    static void beginRewardAdd(Player player, TabletUiState state, String questId, CompoundTag quest, String typePath) {
        QuestDetailsTypeChoice choice = QuestObjectiveTypeCatalog.rewardChoice(typePath);
        String type = choice == null ? QuestObjectiveJsons.MOD + typePath : choice.fullType();
        String id = QuestObjectiveJsons.nextId(quest.getCompound("rewards"), "reward_" + typePath);
        beginReward(player, state, questId, id, type, typePath, choice, true);
    }

    static void beginRewardChange(Player player, TabletUiState state, String questId, String id, String typePath) {
        QuestDetailsTypeChoice choice = QuestObjectiveTypeCatalog.rewardChoice(typePath);
        String type = choice == null ? QuestObjectiveJsons.MOD + typePath : choice.fullType();
        beginReward(player, state, questId, id, type, typePath, choice, false);
    }

    static void openCommandRewardEditor(TabletUiState state, String questId, String id, String command, String title, String icon) {
        QuestDetailsTransientState.openCommandRewardEditor(state, questId, id, command, title, icon);
    }

    static void openExistingCommandRewardEditor(TabletUiState state, String questId, String id) {
        CompoundTag quest = ClientQuestCache.quest(questId);
        CompoundTag reward = quest.getCompound("rewards").getCompound(id);
        JsonObject json = QuestObjectiveJsons.readRewardForEdit(questId, id, reward.getString("json"));
        openCommandRewardEditor(
                state,
                questId,
                id,
                QuestObjectiveJsons.asString(json, "command", ""),
                QuestObjectiveJsons.asString(json, "title", "Command"),
                QuestObjectiveJsons.asString(json, "icon", "minecraft:command_block")
        );
    }

    static void preserveRewardSelectableFlag(String questId, String rewardId, JsonObject next) {
        CompoundTag reward = ClientQuestCache.quest(questId)
                .getCompound("rewards")
                .getCompound(rewardId);
        JsonObject existing = QuestObjectiveJsons.readRewardForEdit(questId, rewardId, reward.getString("json"));
        if (QuestObjectiveSelectableRewards.isSelectable(existing)) {
            next.addProperty("selectable", true);
        }
    }

    private static void beginReward(Player player, TabletUiState state, String questId, String id, String type, String typePath, QuestDetailsTypeChoice choice, boolean add) {
        QuestObjectiveEditFlow flow = choice == null ? QuestObjectiveEditFlow.DIRECT_JSON : choice.editFlow();
        if (flow == QuestObjectiveEditFlow.ITEM_SOURCE_PICKER) {
            QuestDetailsTransientState.openItemSourcePicker(state, ModalTargets.rewardItem(questId, id, type));
            return;
        }
        if (flow == QuestObjectiveEditFlow.COMMAND_EDITOR) {
            openCommandRewardEditor(state, questId, id, "", "Command", "minecraft:command_block");
            return;
        }
        if (flow == QuestObjectiveEditFlow.LOOT_TABLE_PICKER) {
            QuestDetailsWindow.openLootTablePicker(state, ModalTargets.rewardLootTable(questId, id, type));
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details loot table picker open quest={} reward={} type={} add={}", questId, id, typePath, add);
            return;
        }
        if (flow == QuestObjectiveEditFlow.XP_PICKER) {
            QuestDetailsTransientState.openXpPicker(state, questId, id, false);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details xp reward picker open quest={} reward={} add={}", questId, id, add);
            return;
        }
        EditorCommandClient.putQuestRewardJson(player, questId, defaultRewardJson(id, typePath, choice).toString());
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details {} reward quest={} reward={} type={}", add ? "add" : "change", questId, id, typePath);
    }

    private static JsonObject defaultRewardJson(String id, String typePath, QuestDetailsTypeChoice choice) {
        return choice == null ? QuestObjectiveJsons.defaultReward(id, typePath) : choice.defaultJson(id);
    }
}
