package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

final class QuestTaskRewardEditActions {
    private QuestTaskRewardEditActions() {
    }

    static void beginRewardAdd(Player player, TabletUiState state, String questId, CompoundTag quest, String typePath) {
        QuestDetailsTypeChoice choice = QuestTaskTypeCatalog.rewardChoice(typePath);
        String type = choice == null ? TaskJsonFactory.MOD + typePath : choice.fullType();
        String id = TaskJsonFactory.nextId(quest.getCompound("rewards"), "reward_" + typePath);
        beginReward(player, state, questId, id, type, typePath, choice, true);
    }

    static void beginRewardChange(Player player, TabletUiState state, String questId, String id, String typePath) {
        QuestDetailsTypeChoice choice = QuestTaskTypeCatalog.rewardChoice(typePath);
        String type = choice == null ? TaskJsonFactory.MOD + typePath : choice.fullType();
        beginReward(player, state, questId, id, type, typePath, choice, false);
    }

    static void openCommandRewardEditor(TabletUiState state, String questId, String id, String command, String title, String icon) {
        QuestDetailsTransientManager.openCommandRewardEditor(state, questId, id, command, title, icon);
    }

    static void openExistingCommandRewardEditor(TabletUiState state, String questId, String id) {
        CompoundTag quest = ClientQuestStateFacade.quest(questId);
        CompoundTag reward = quest.getCompound("rewards").getCompound(id);
        JsonObject json = TaskJsonFactory.readRewardForEdit(questId, id, reward.getString("json"));
        openCommandRewardEditor(
                state,
                questId,
                id,
                TaskJsonFactory.asString(json, "command", ""),
                TaskJsonFactory.asString(json, "title", "Command"),
                TaskJsonFactory.asString(json, "icon", "minecraft:command_block")
        );
    }

    static void preserveRewardSelectableFlag(String questId, String rewardId, JsonObject next) {
        CompoundTag reward = ClientQuestStateFacade.quest(questId)
                .getCompound("rewards")
                .getCompound(rewardId);
        JsonObject existing = TaskJsonFactory.readRewardForEdit(questId, rewardId, reward.getString("json"));
        if (QuestTaskSelectableRewards.isSelectable(existing)) {
            next.addProperty("selectable", true);
        }
    }

    private static void beginReward(Player player, TabletUiState state, String questId, String id, String type, String typePath, QuestDetailsTypeChoice choice, boolean add) {
        QuestTaskEditFlow flow = choice == null ? QuestTaskEditFlow.DIRECT_JSON : choice.editFlow();
        if (flow == QuestTaskEditFlow.ITEM_SOURCE_PICKER) {
            QuestDetailsTransientManager.openItemSourcePicker(state, ModalTargets.rewardItem(questId, id, type));
            return;
        }
        if (flow == QuestTaskEditFlow.COMMAND_EDITOR) {
            openCommandRewardEditor(state, questId, id, "", "Command", "minecraft:command_block");
            return;
        }
        if (flow == QuestTaskEditFlow.LOOT_TABLE_PICKER) {
            QuestDetailsWindow.openLootTablePicker(state, ModalTargets.rewardLootTable(questId, id, type));
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details loot table picker open quest={} reward={} type={} add={}", questId, id, typePath, add);
            return;
        }
        if (flow == QuestTaskEditFlow.XP_PICKER) {
            QuestDetailsTransientManager.openXpPicker(state, questId, id, false);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details xp reward picker open quest={} reward={} add={}", questId, id, add);
            return;
        }
        EditorQuestCommandClient.putQuestRewardJson(player, questId, defaultRewardJson(id, typePath, choice).toString());
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details {} reward quest={} reward={} type={}", add ? "add" : "change", questId, id, typePath);
    }

    private static JsonObject defaultRewardJson(String id, String typePath, QuestDetailsTypeChoice choice) {
        return choice == null ? TaskJsonFactory.defaultReward(id, typePath) : choice.defaultJson(id);
    }
}
