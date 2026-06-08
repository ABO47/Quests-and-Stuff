package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.runtime.C2SClaimSelectableRewardPacket;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class QuestObjectiveSelectableRewards {
    private static final String CHOICE_SEPARATOR = "\u001F";
    private static final String STANDALONE_SELECTION_KEY = CHOICE_SEPARATOR + "standalone";

    private QuestObjectiveSelectableRewards() {
    }

    static boolean isSelectable(JsonObject json) {
        return QuestObjectiveJsons.asBoolean(json, "selectable", false) || isSelectableWrapper(json);
    }

    private static boolean isSelectableWrapper(JsonObject json) {
        return "selectable".equals(QuestObjectiveJsons.typePath(QuestObjectiveJsons.asString(json, "type", "")));
    }

    static JsonObject displayJson(JsonObject json) {
        if (!isSelectableWrapper(json)) {
            return json;
        }
        JsonElement rewards = json.get("rewards");
        if (rewards == null || !rewards.isJsonObject()) {
            return json;
        }
        for (Map.Entry<String, JsonElement> entry : rewards.getAsJsonObject().entrySet()) {
            if (entry.getValue().isJsonObject()) {
                return entry.getValue().getAsJsonObject();
            }
        }
        return json;
    }

    static List<QuestDetailsObjectiveEntry> displayEntries(List<QuestDetailsObjectiveEntry> entries, boolean editMode) {
        if (editMode) {
            return entries;
        }
        List<QuestDetailsObjectiveEntry> displayEntries = new ArrayList<>();
        for (QuestDetailsObjectiveEntry entry : entries) {
            if (!isSelectableWrapper(entry.json())) {
                displayEntries.add(entry);
                continue;
            }
            JsonElement rewards = entry.json().get("rewards");
            if (rewards == null || !rewards.isJsonObject()) {
                displayEntries.add(entry);
                continue;
            }
            int added = 0;
            for (Map.Entry<String, JsonElement> choice : rewards.getAsJsonObject().entrySet()) {
                if (!choice.getValue().isJsonObject()) {
                    continue;
                }
                JsonObject child = choice.getValue().getAsJsonObject();
                CompoundTag tag = entry.tag().copy();
                tag.putString("type", QuestObjectiveJsons.asString(child, "type", ""));
                tag.putString("json", child.toString());
                displayEntries.add(new QuestDetailsObjectiveEntry(choiceEntryId(entry.id(), choice.getKey()), tag, child));
                added++;
            }
            if (added == 0) {
                displayEntries.add(entry);
            }
        }
        return displayEntries;
    }

    static boolean isSelectableChoiceId(String id) {
        return id != null && id.contains(CHOICE_SEPARATOR);
    }

    static boolean isClaimChoiceEntry(QuestDetailsObjectiveEntry entry) {
        if (entry == null) {
            return false;
        }
        if (isSelectableChoiceId(entry.id())) {
            return true;
        }
        return isSelectable(entry.json()) && !isSelectableWrapper(entry.json());
    }

    static String choiceGroupId(String id) {
        int separator = id == null ? -1 : id.indexOf(CHOICE_SEPARATOR);
        return separator < 0 ? "" : id.substring(0, separator);
    }

    static String choiceId(String id) {
        int separator = id == null ? -1 : id.indexOf(CHOICE_SEPARATOR);
        return separator < 0 ? "" : id.substring(separator + CHOICE_SEPARATOR.length());
    }

    static void selectChoice(TabletUiState state, String id) {
        if (state == null || id == null || id.isBlank()) {
            return;
        }
        if (isSelectableChoiceId(id)) {
            String groupId = choiceGroupId(id);
            String choiceId = choiceId(id);
            if (!groupId.isBlank() && !choiceId.isBlank()) {
                state.questDetailsSelectableRewardChoices.put(groupId, choiceId);
            }
        } else {
            state.questDetailsSelectableRewardChoices.put(STANDALONE_SELECTION_KEY, id);
        }
    }

    static boolean isSelectedChoice(TabletUiState state, String id) {
        if (state == null || id == null || id.isBlank()) {
            return false;
        }
        if (!isSelectableChoiceId(id)) {
            return id.equals(state.questDetailsSelectableRewardChoices.get(STANDALONE_SELECTION_KEY));
        }
        String groupId = choiceGroupId(id);
        String choiceId = choiceId(id);
        if (groupId.isBlank() || choiceId.isBlank()) {
            return false;
        }
        return choiceId.equals(state.questDetailsSelectableRewardChoices.get(groupId));
    }

    static void makeSelectable(Player player, String questId, String rewardId) {
        CompoundTag rewards = ClientQuestCache.quest(questId).getCompound("rewards");
        JsonObject reward = QuestObjectiveJsons.readRewardForEdit(questId, rewardId, rewards.getCompound(rewardId).getString("json"));
        if (isSelectable(reward)) {
            return;
        }
        reward.addProperty("selectable", true);
        EditorCommandClient.putQuestRewardJson(player, questId, reward.toString());
        QuestsAndStuffMod.debugLog("[QnS:UI] reward marked selectable quest={} reward={}", questId, rewardId);
    }

    static boolean claimSelected(Player player, TabletUiState state, String questId) {
        if (state == null) {
            return false;
        }
        CompoundTag rewards = ClientQuestCache.quest(questId).getCompound("rewards");
        boolean claimedAny = false;
        for (String rewardId : rewards.getAllKeys()) {
            JsonObject json = QuestObjectiveJsons.read(rewards.getCompound(rewardId).getString("json"));
            if (!isSelectable(json)) {
                continue;
            }
            if (!isSelectableWrapper(json)) {
                if (!isStandaloneSelected(state, rewardId)) {
                    continue;
                }
                ModNetwork.sendToServer(new C2SClaimSelectableRewardPacket(questId, rewardId, List.of()));
                QuestsAndStuffMod.debugLog("[QnS:UI] selectable reward claimed quest={} reward={}", questId, rewardId);
                claimedAny = true;
                continue;
            }
            String choiceId = selectedChoiceForGroup(state, rewardId);
            if (choiceId.isBlank() || !choiceExists(json, choiceId)) {
                continue;
            }
            ModNetwork.sendToServer(new C2SClaimSelectableRewardPacket(questId, rewardId, List.of(choiceId)));
            QuestsAndStuffMod.debugLog("[QnS:UI] selectable reward choice claimed quest={} reward={} choice={}", questId, rewardId, choiceId);
            claimedAny = true;
        }
        return claimedAny;
    }

    static boolean allSelectableRewardsSelected(CompoundTag quest, TabletUiState state) {
        if (quest == null || state == null) {
            return false;
        }
        CompoundTag rewards = quest.getCompound("rewards");
        boolean hasSelectable = false;
        boolean hasSelected = false;
        boolean hasStandaloneSelectable = false;
        boolean hasSelectedStandalone = false;
        for (String rewardId : rewards.getAllKeys()) {
            JsonObject json = QuestObjectiveJsons.read(rewards.getCompound(rewardId).getString("json"));
            if (!isSelectable(json)) {
                continue;
            }
            hasSelectable = true;
            if (!isSelectableWrapper(json)) {
                hasStandaloneSelectable = true;
                if (isStandaloneSelected(state, rewardId)) {
                    hasSelected = true;
                    hasSelectedStandalone = true;
                }
                continue;
            }
            String choiceId = selectedChoiceForGroup(state, rewardId);
            if (choiceId.isBlank() || !choiceExists(json, choiceId)) {
                return false;
            }
            hasSelected = true;
        }
        if (hasStandaloneSelectable && !hasSelectedStandalone) {
            return false;
        }
        return hasSelectable && hasSelected;
    }

    static boolean hasSelectableReward(CompoundTag quest) {
        if (quest == null) {
            return false;
        }
        CompoundTag rewards = quest.getCompound("rewards");
        for (String rewardId : rewards.getAllKeys()) {
            JsonObject json = QuestObjectiveJsons.read(rewards.getCompound(rewardId).getString("json"));
            if (isSelectable(json)) {
                return true;
            }
        }
        return false;
    }

    private static String selectedChoiceForGroup(TabletUiState state, String groupId) {
        if (state == null || groupId == null || groupId.isBlank()) {
            return "";
        }
        String selected = state.questDetailsSelectableRewardChoices.get(groupId);
        if (selected != null && !selected.isBlank()) {
            return selected.trim();
        }
        return "";
    }

    private static boolean isStandaloneSelected(TabletUiState state, String rewardId) {
        return state != null && rewardId != null && rewardId.equals(state.questDetailsSelectableRewardChoices.get(STANDALONE_SELECTION_KEY));
    }

    private static boolean choiceExists(JsonObject selectable, String choiceId) {
        if (choiceId == null || choiceId.isBlank()) {
            return false;
        }
        JsonElement rewards = selectable.get("rewards");
        return rewards != null && rewards.isJsonObject() && rewards.getAsJsonObject().has(choiceId);
    }

    static boolean commitDisplayAmount(Player player, String questId, String rewardId, int amount) {
        if (questId == null || questId.isBlank() || rewardId == null || rewardId.isBlank()) {
            return false;
        }
        CompoundTag rewards = ClientQuestCache.quest(questId).getCompound("rewards");
        JsonObject selectable = QuestObjectiveJsons.readRewardForEdit(questId, rewardId, rewards.getCompound(rewardId).getString("json"));
        if (!isSelectableWrapper(selectable)) {
            return false;
        }
        JsonElement choicesElement = selectable.get("rewards");
        if (choicesElement == null || !choicesElement.isJsonObject()) {
            return false;
        }
        for (Map.Entry<String, JsonElement> choice : choicesElement.getAsJsonObject().entrySet()) {
            if (!choice.getValue().isJsonObject()) {
                continue;
            }
            choice.getValue().getAsJsonObject().addProperty("amount", amount);
            EditorCommandClient.putQuestRewardJson(player, questId, selectable.toString());
            QuestsAndStuffMod.debugLog("[QnS:UI] selectable reward display amount changed quest={} reward={} choice={} amount={}", questId, rewardId, choice.getKey(), amount);
            return true;
        }
        return false;
    }

    private static String choiceEntryId(String groupId, String choiceId) {
        return groupId + CHOICE_SEPARATOR + choiceId;
    }
}
