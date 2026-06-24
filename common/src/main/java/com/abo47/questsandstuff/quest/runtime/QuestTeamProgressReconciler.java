package com.abo47.questsandstuff.quest.runtime;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.runtime.progress.QuestProgressState;
import com.abo47.questsandstuff.quest.runtime.team.TeamProgressProviders;
import com.abo47.questsandstuff.quest.sync.QuestSyncService;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class QuestTeamProgressReconciler {
    private QuestTeamProgressReconciler() {
    }

    static void onTeamMembershipChanged(
            ServerLevel level,
            UUID changedPlayer,
            QuestDefinitionStore definitionStore,
            QuestProgressSavedData progressData,
            QuestSyncService syncService
    ) {
        List<UUID> members = TeamProgressProviders.members(level, changedPlayer);
        if (members.isEmpty()) {
            return;
        }

        Set<String> sharedQuestIds = sharedQuestIds(definitionStore);
        if (sharedQuestIds.isEmpty()) {
            return;
        }

        Set<String> changedQuests = new HashSet<>();
        for (String questId : sharedQuestIds) {
            QuestDefinition definition = definitionStore.quests().get(questId);
            if (definition == null) {
                continue;
            }
            reconcileSharedQuest(progressData, members, definition, changedQuests);
        }

        QuestRuntimeSyncs.syncChangedToOnlineMembers(level, members, progressData, syncService, changedQuests);
    }

    private static Set<String> sharedQuestIds(QuestDefinitionStore definitionStore) {
        Set<String> sharedQuestIds = new HashSet<>();
        for (QuestDefinition definition : definitionStore.quests().values()) {
            if (!definition.settings().individualProgress()) {
                sharedQuestIds.add(definition.id());
            }
        }
        return sharedQuestIds;
    }

    private static void reconcileSharedQuest(QuestProgressSavedData progressData, List<UUID> members, QuestDefinition definition, Set<String> changedQuests) {
        SharedQuestProgress merged = mergeSharedProgress(progressData, members, definition);
        for (UUID member : members) {
            QuestProgressState state = progressData.state(member).quest(definition.id());
            boolean changed = applyMergedProgress(state, definition, merged);
            if (changed) {
                changedQuests.add(definition.id());
            }
        }
    }

    private static SharedQuestProgress mergeSharedProgress(QuestProgressSavedData progressData, List<UUID> members, QuestDefinition definition) {
        boolean unlocked = false;
        boolean completed = false;
        long completedAt = 0L;
        Set<String> claimed = new HashSet<>();
        Map<String, Tag> maxTaskProgress = new HashMap<>();

        for (UUID member : members) {
            QuestProgressState state = progressData.state(member).quest(definition.id());
            unlocked |= state.unlocked();
            completed |= state.completed();
            completedAt = Math.max(completedAt, state.completedAt());
            claimed.addAll(state.claimedRewards());
            mergeTaskProgress(definition, state, maxTaskProgress);
        }
        return new SharedQuestProgress(unlocked, completed, completedAt, claimed, maxTaskProgress);
    }

    private static void mergeTaskProgress(QuestDefinition definition, QuestProgressState state, Map<String, Tag> maxTaskProgress) {
        for (Map.Entry<String, QuestTaskDefinition> taskEntry : definition.tasks().entrySet()) {
            QuestTaskDefinition task = taskEntry.getValue();
            Tag progress = state.getTaskProgress(taskEntry.getKey(), task);
            Tag existing = maxTaskProgress.get(taskEntry.getKey());
            if (existing == null || task.getProgress(progress) > task.getProgress(existing)) {
                maxTaskProgress.put(taskEntry.getKey(), progress.copy());
            }
        }
    }

    private static boolean applyMergedProgress(QuestProgressState state, QuestDefinition definition, SharedQuestProgress merged) {
        boolean changed = false;
        if (state.unlocked() != merged.unlocked()) {
            state.setUnlocked(merged.unlocked());
            changed = true;
        }
        if (state.completed() != merged.completed()) {
            state.setCompleted(merged.completed(), merged.completedAt());
            changed = true;
        }
        if (!state.claimedRewards().equals(merged.claimed())) {
            state.claimedRewards().clear();
            state.claimedRewards().addAll(merged.claimed());
            changed = true;
        }
        for (Map.Entry<String, Tag> entry : merged.maxTaskProgress().entrySet()) {
            QuestTaskDefinition task = definition.tasks().get(entry.getKey());
            if (task == null) {
                continue;
            }
            Tag existing = state.getTaskProgress(entry.getKey(), task);
            if (!task.storage().same(existing, entry.getValue()) && task.getProgress(entry.getValue()) > task.getProgress(existing)) {
                state.setTaskProgress(entry.getKey(), task, entry.getValue());
                changed = true;
            }
        }
        return changed;
    }

    private record SharedQuestProgress(boolean unlocked, boolean completed, long completedAt, Set<String> claimed, Map<String, Tag> maxTaskProgress) {
    }
}
