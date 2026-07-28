package com.abo47.questsandstuff.quest.runtime.progress;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;

public final class CompletableQuestEvaluator {
    public Set<String> progressableQuestIds(PlayerQuestState state, Map<String, QuestDefinition> quests) {
        Set<String> progressable = new LinkedHashSet<>();
        for (QuestDefinition definition : quests.values()) {
            QuestProgressState progress = state.quest(definition.id());
            if (!progress.completed() && shouldBeUnlocked(state, definition, progress)) {
                progressable.add(definition.id());
            }
        }
        return progressable;
    }

    public boolean shouldBeUnlocked(PlayerQuestState state, QuestDefinition definition, QuestProgressState progress) {
        if (progress.disabledByExclusiveChoice().contains(definition.id())) {
            return false;
        }
        if (progress.completed() || definition.prerequisites().isEmpty()) {
            return true;
        }
        for (String prerequisite : definition.prerequisites()) {
            if (!state.quest(prerequisite).completed()) {
                return false;
            }
        }
        return true;
    }

    public void initializeUnlockTasks(ServerPlayer actor, UUID ownerId, QuestDefinition definition, QuestProgressState progress) {
        ServerPlayer owner = actor.server.getPlayerList().getPlayer(ownerId);
        if (owner == null) {
            return;
        }
        for (Map.Entry<String, QuestTaskDefinition> entry : definition.tasks().entrySet()) {
            QuestTaskDefinition task = entry.getValue();
            Tag before = progress.getTaskProgress(entry.getKey(), task);
            Tag after = task.initProgress(before, owner);
            if (!task.storage().same(before, after)) {
                progress.setTaskProgress(entry.getKey(), task, after);
            }
        }
    }
}
