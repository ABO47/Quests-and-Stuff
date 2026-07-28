package com.abo47.questsandstuff.quest.editor.quest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.JsonOps;

import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;

import com.google.gson.JsonParser;

public final class QuestContentEditService {
    private final EditorSessionService service;

    public QuestContentEditService(EditorSessionService service) {
        this.service = service;
    }

    public void putQuestTask(ServerPlayer player, String questId, String taskJson) {
        QuestDefinition source = service.definitionStore().quests().get(EditorSessionService.normalizeQuestId(questId));
        if (source == null || taskJson == null || taskJson.isBlank()) {
            return;
        }
        QuestTaskDefinition task = parseTask(taskJson);
        if (task == null || task.id().isBlank()) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        Map<String, QuestTaskDefinition> tasks = new LinkedHashMap<>(source.tasks());
        tasks.put(task.id(), task);
        updateQuest(player, session, QuestDefinitionEdits.withTasks(source, tasks));
    }

    public void removeQuestTask(ServerPlayer player, String questId, String taskId) {
        QuestDefinition source = service.definitionStore().quests().get(EditorSessionService.normalizeQuestId(questId));
        String normalizedTask = EditorSessionService.normalizeQuestId(taskId);
        if (source == null || normalizedTask.isBlank() || !source.tasks().containsKey(normalizedTask)) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        Map<String, QuestTaskDefinition> tasks = new LinkedHashMap<>(source.tasks());
        tasks.remove(normalizedTask);
        updateQuest(player, session, QuestDefinitionEdits.withTasks(source, tasks));
    }

    public void moveQuestTask(ServerPlayer player, String questId, String taskId, int offset) {
        QuestDefinition source = service.definitionStore().quests().get(EditorSessionService.normalizeQuestId(questId));
        if (source == null || offset == 0) {
            return;
        }
        Map<String, QuestTaskDefinition> moved = moveEntry(source.tasks(), EditorSessionService.normalizeQuestId(taskId), offset);
        if (sameKeyOrder(moved, source.tasks())) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        updateQuest(player, session, QuestDefinitionEdits.withTasks(source, moved));
    }

    public void putQuestReward(ServerPlayer player, String questId, String rewardJson) {
        QuestDefinition source = service.definitionStore().quests().get(EditorSessionService.normalizeQuestId(questId));
        if (source == null || rewardJson == null || rewardJson.isBlank()) {
            return;
        }
        QuestRewardDefinition reward = parseReward(rewardJson);
        if (reward == null || reward.id().isBlank()) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        Map<String, QuestRewardDefinition> rewards = new LinkedHashMap<>(source.rewards());
        rewards.put(reward.id(), reward);
        updateQuest(player, session, QuestDefinitionEdits.withRewards(source, rewards));
    }

    public void removeQuestReward(ServerPlayer player, String questId, String rewardId) {
        QuestDefinition source = service.definitionStore().quests().get(EditorSessionService.normalizeQuestId(questId));
        String normalizedReward = EditorSessionService.normalizeQuestId(rewardId);
        if (source == null || normalizedReward.isBlank() || !source.rewards().containsKey(normalizedReward)) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        Map<String, QuestRewardDefinition> rewards = new LinkedHashMap<>(source.rewards());
        rewards.remove(normalizedReward);
        updateQuest(player, session, QuestDefinitionEdits.withRewards(source, rewards));
    }

    public void moveQuestReward(ServerPlayer player, String questId, String rewardId, int offset) {
        QuestDefinition source = service.definitionStore().quests().get(EditorSessionService.normalizeQuestId(questId));
        if (source == null || offset == 0) {
            return;
        }
        Map<String, QuestRewardDefinition> moved = moveEntry(source.rewards(), EditorSessionService.normalizeQuestId(rewardId), offset);
        if (sameKeyOrder(moved, source.rewards())) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        updateQuest(player, session, QuestDefinitionEdits.withRewards(source, moved));
    }

    private void updateQuest(ServerPlayer player, EditorSessionService.EditorSession session, QuestDefinition updated) {
        service.definitionStore().upsert(updated);
        service.definitionStore().saveNow(updated.id());
        session.currentQuest = updated.id();
        service.postMutation(player);
        service.syncService().broadcastEditorMutation(player.server.getPlayerList().getPlayers(), "update", updated);
    }

    private static QuestTaskDefinition parseTask(String taskJson) {
        try {
            return QuestTaskDefinition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(taskJson))
                    .getOrThrow(false, QuestsAndStuffMod.LOGGER::error);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static QuestRewardDefinition parseReward(String rewardJson) {
        try {
            return QuestRewardDefinition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(rewardJson))
                    .getOrThrow(false, QuestsAndStuffMod.LOGGER::error);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static <T> Map<String, T> moveEntry(Map<String, T> source, String id, int offset) {
        if (source == null || source.isEmpty() || id == null || id.isBlank() || !source.containsKey(id)) {
            return source == null ? Map.of() : source;
        }
        List<Map.Entry<String, T>> entries = new ArrayList<>(source.entrySet());
        int index = -1;
        for (int i = 0; i < entries.size(); i++) {
            if (id.equals(entries.get(i).getKey())) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            return source;
        }
        int target = Math.max(0, Math.min(entries.size() - 1, index + offset));
        if (target == index) {
            return source;
        }
        Map.Entry<String, T> entry = entries.remove(index);
        entries.add(target, entry);
        Map<String, T> moved = new LinkedHashMap<>();
        for (Map.Entry<String, T> movedEntry : entries) {
            moved.put(movedEntry.getKey(), movedEntry.getValue());
        }
        return moved;
    }

    private static <T> boolean sameKeyOrder(Map<String, T> left, Map<String, T> right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null || left.size() != right.size()) {
            return false;
        }
        return new ArrayList<>(left.keySet()).equals(new ArrayList<>(right.keySet()));
    }
}
