package com.abo47.questsandstuff.client.tablet.details.objective;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.network.QuestNetwork;
import com.abo47.questsandstuff.network.runtime.C2SClaimSelectableRewardPacket;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class QuestObjectiveSelectableRewards {
    private static final String SELECTABLE_TYPE = QuestObjectiveJsons.MOD + "selectable";
    private static final String CHOICE_SEPARATOR = "\u001F";

    private QuestObjectiveSelectableRewards() {
    }

    static boolean isSelectable(JsonObject json) {
        return "selectable".equals(QuestObjectiveJsons.typePath(QuestObjectiveJsons.asString(json, "type", "")));
    }

    static JsonObject displayJson(JsonObject json) {
        if (!isSelectable(json)) {
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
            if (!isSelectable(entry.json())) {
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
        state.questDetailsSelectedObjectiveKind = "reward";
        state.questDetailsSelectedObjectiveId = id;
        if (!isSelectableChoiceId(id)) {
            return;
        }
        String groupId = choiceGroupId(id);
        String choiceId = choiceId(id);
        if (!groupId.isBlank() && !choiceId.isBlank()) {
            state.questDetailsSelectableRewardChoices.put(groupId, choiceId);
        }
    }

    static boolean isSelectedChoice(TabletUiState state, String id) {
        if (state == null || id == null || id.isBlank()) {
            return false;
        }
        if (!isSelectableChoiceId(id)) {
            return id.equals(state.questDetailsSelectedObjectiveId);
        }
        String groupId = choiceGroupId(id);
        String choiceId = choiceId(id);
        if (groupId.isBlank() || choiceId.isBlank()) {
            return false;
        }
        return choiceId.equals(state.questDetailsSelectableRewardChoices.get(groupId));
    }

    static List<SelectableGroup> selectableGroups(String questId, String excludeId) {
        CompoundTag rewards = ClientQuestCache.quest(questId).getCompound("rewards");
        List<SelectableGroup> groups = new ArrayList<>();
        for (String id : rewards.getAllKeys()) {
            if (id.equals(excludeId)) {
                continue;
            }
            JsonObject json = QuestObjectiveJsons.read(rewards.getCompound(id).getString("json"));
            if (isSelectable(json)) {
                String name = QuestObjectiveDisplayText.displayName(json, QuestObjectiveJsons.asString(json, "type", SELECTABLE_TYPE));
                groups.add(new SelectableGroup(id, name.isBlank() ? id : name));
            }
        }
        return groups;
    }

    static void makeSelectable(Player player, String questId, String rewardId) {
        CompoundTag rewards = ClientQuestCache.quest(questId).getCompound("rewards");
        JsonObject reward = QuestObjectiveJsons.read(rewards.getCompound(rewardId).getString("json"));
        if (isSelectable(reward)) {
            return;
        }
        JsonObject selectable = selectableBase(rewardId, reward);
        JsonObject choices = selectable.getAsJsonObject("rewards");
        String choiceId = uniqueChoiceId(choices, rewardId);
        choices.add(choiceId, childReward(reward, choiceId));
        EditorCommandClient.putQuestRewardJson(player, questId, selectable.toString());
        QuestsAndStuffMod.debugLog("[QnS:UI] reward made selectable quest={} reward={} choice={}", questId, rewardId, choiceId);
    }

    static void addToSelectable(Player player, String questId, String rewardId, String groupId) {
        if (rewardId.equals(groupId)) {
            return;
        }
        CompoundTag rewards = ClientQuestCache.quest(questId).getCompound("rewards");
        JsonObject reward = QuestObjectiveJsons.read(rewards.getCompound(rewardId).getString("json"));
        JsonObject group = QuestObjectiveJsons.read(rewards.getCompound(groupId).getString("json"));
        if (isSelectable(reward) || !isSelectable(group)) {
            return;
        }
        JsonObject choices = rewardsObject(group);
        String choiceId = uniqueChoiceId(choices, rewardId);
        choices.add(choiceId, childReward(reward, choiceId));
        EditorCommandClient.putQuestRewardJson(player, questId, group.toString());
        EditorCommandClient.removeQuestReward(player, questId, rewardId);
        QuestsAndStuffMod.debugLog("[QnS:UI] reward added to selectable quest={} reward={} group={} choice={}", questId, rewardId, groupId, choiceId);
    }

    static boolean claimSingleChoice(Player player, String questId, String rewardId, JsonObject selectable) {
        if (!isSelectable(selectable) || QuestObjectiveDisplayText.amount(selectable) != 1) {
            return false;
        }
        JsonElement rewards = selectable.get("rewards");
        if (rewards == null || !rewards.isJsonObject()) {
            return false;
        }
        String choiceId = "";
        int choices = 0;
        for (Map.Entry<String, JsonElement> entry : rewards.getAsJsonObject().entrySet()) {
            if (entry.getValue().isJsonObject()) {
                choiceId = entry.getKey();
                choices++;
            }
        }
        if (choices != 1 || choiceId.isBlank()) {
            return false;
        }
        QuestNetwork.sendToServer(new C2SClaimSelectableRewardPacket(questId, rewardId, List.of(choiceId)));
        QuestsAndStuffMod.debugLog("[QnS:UI] selectable reward single choice claimed quest={} reward={} choice={}", questId, rewardId, choiceId);
        return true;
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
            String choiceId = selectedChoiceForGroup(state, rewardId);
            if (choiceId.isBlank()) {
                continue;
            }
            if (!choiceExists(json, choiceId)) {
                continue;
            }
            QuestNetwork.sendToServer(new C2SClaimSelectableRewardPacket(questId, rewardId, List.of(choiceId)));
            QuestsAndStuffMod.debugLog("[QnS:UI] selectable reward choice claimed quest={} reward={} choice={}", questId, rewardId, choiceId);
            claimedAny = true;
        }
        if (claimedAny) {
            return true;
        }
        if (!"reward".equals(state.questDetailsSelectedObjectiveKind)) {
            return false;
        }
        String rewardId = state.questDetailsSelectedObjectiveId == null ? "" : state.questDetailsSelectedObjectiveId.trim();
        JsonObject json = QuestObjectiveJsons.read(rewards.getCompound(rewardId).getString("json"));
        return claimSingleChoice(player, questId, rewardId, json);
    }

    static boolean allSelectableRewardsSelected(CompoundTag quest, TabletUiState state) {
        if (quest == null || state == null) {
            return false;
        }
        CompoundTag rewards = quest.getCompound("rewards");
        boolean hasSelectable = false;
        boolean hasSelected = false;
        boolean hasSelectedSingleton = false;
        for (String rewardId : rewards.getAllKeys()) {
            JsonObject json = QuestObjectiveJsons.read(rewards.getCompound(rewardId).getString("json"));
            if (!isSelectable(json)) {
                continue;
            }
            hasSelectable = true;
            String choiceId = selectedChoiceForGroup(state, rewardId);
            if (choiceId.isBlank() || !choiceExists(json, choiceId)) {
                continue;
            }
            hasSelected = true;
            if (isSingletonSelectable(json)) {
                hasSelectedSingleton = true;
            }
        }
        if (!hasSelectable || !hasSelected) {
            return false;
        }
        for (String rewardId : rewards.getAllKeys()) {
            JsonObject json = QuestObjectiveJsons.read(rewards.getCompound(rewardId).getString("json"));
            if (!isSelectable(json)) {
                continue;
            }
            String choiceId = selectedChoiceForGroup(state, rewardId);
            if (!choiceId.isBlank() && choiceExists(json, choiceId)) {
                continue;
            }
            if (hasSelectedSingleton && isSingletonSelectable(json)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private static boolean isSingletonSelectable(JsonObject json) {
        if (!isSelectable(json) || QuestObjectiveDisplayText.amount(json) != 1) {
            return false;
        }
        JsonElement rewards = json.get("rewards");
        if (rewards == null || !rewards.isJsonObject()) {
            return false;
        }
        int choices = 0;
        for (Map.Entry<String, JsonElement> entry : rewards.getAsJsonObject().entrySet()) {
            if (entry.getValue().isJsonObject()) {
                choices++;
            }
            if (choices > 1) {
                return false;
            }
        }
        return choices == 1;
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
        String rewardId = state.questDetailsSelectedObjectiveId == null ? "" : state.questDetailsSelectedObjectiveId.trim();
        if (isSelectableChoiceId(rewardId) && groupId.equals(choiceGroupId(rewardId))) {
            return choiceId(rewardId);
        }
        return "";
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
        JsonObject selectable = QuestObjectiveJsons.read(rewards.getCompound(rewardId).getString("json"));
        if (!isSelectable(selectable)) {
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

    private static JsonObject selectableBase(String id, JsonObject displaySource) {
        JsonObject json = displaySource == null ? new JsonObject() : displaySource.deepCopy();
        json.addProperty("id", id);
        json.addProperty("type", SELECTABLE_TYPE);
        json.addProperty("amount", 1);
        json.add("rewards", new JsonObject());
        return json;
    }

    private static JsonObject rewardsObject(JsonObject selectable) {
        JsonElement rewards = selectable.get("rewards");
        if (rewards != null && rewards.isJsonObject()) {
            return rewards.getAsJsonObject();
        }
        JsonObject object = new JsonObject();
        selectable.add("rewards", object);
        return object;
    }

    private static JsonObject childReward(JsonObject reward, String id) {
        JsonObject child = reward.deepCopy();
        child.addProperty("id", id);
        return child;
    }

    private static String uniqueChoiceId(JsonObject choices, String baseId) {
        String clean = baseId == null || baseId.isBlank() ? "choice" : baseId.trim();
        String id = "choice_" + clean;
        int suffix = 2;
        while (choices.has(id)) {
            id = "choice_" + clean + "_" + suffix++;
        }
        return id;
    }

    record SelectableGroup(String id, String name) {
    }
}
