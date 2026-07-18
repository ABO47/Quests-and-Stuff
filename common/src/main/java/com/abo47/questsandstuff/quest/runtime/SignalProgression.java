package com.abo47.questsandstuff.quest.runtime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import com.abo47.questsandstuff.quest.runtime.progress.QuestProgressState;
import com.abo47.questsandstuff.quest.runtime.progress.QuestRuntimeIndex;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignal;
import com.abo47.questsandstuff.quest.sync.PerformanceTracker;
import com.abo47.questsandstuff.quest.sync.SyncService;
import com.abo47.questsandstuff.team.runtime.TeamProgressProviders;

final class SignalProgression {
    private final QuestDefinitionStore definitionStore;
    private final QuestProgressSavedData progressData;
    private final SyncService syncService;
    private final PerformanceTracker performanceTracker;
    private final RuntimeEngine engine;

    SignalProgression(QuestDefinitionStore definitionStore, QuestProgressSavedData progressData, SyncService syncService, PerformanceTracker performanceTracker, RuntimeEngine engine) {
        this.definitionStore = definitionStore;
        this.progressData = progressData;
        this.syncService = syncService;
        this.performanceTracker = performanceTracker;
        this.engine = engine;
    }

    void onSignal(QuestSignal signal, QuestRuntimeIndex index) {
        long start = System.nanoTime();
        if (signal.player() == null) {
            return;
        }

        ServerPlayer actor = signal.player();
        long serverTick = actor.server.getTickCount();
        UUID actorId = actor.getUUID();
        List<UUID> teamMembers = TeamProgressProviders.members(actor.serverLevel(), actorId);
        if (teamMembers.isEmpty()) {
            teamMembers = List.of(actorId);
        }

        Set<String> changedQuestIds = new HashSet<>();
        Set<UUID> preparedTargets = new HashSet<>();
        int visitedBindings = 0;

        for (QuestRuntimeIndex.TaskBinding binding : index.bindings(signal.type())) {
            visitedBindings++;
            QuestDefinition definition = definitionStore.quests().get(binding.questId());
            if (definition == null) {
                continue;
            }

            List<UUID> targets = definition.settings().individualProgress() ? List.of(actorId) : teamMembers;

            for (UUID targetId : targets) {
                PlayerQuestState state = progressData.state(targetId);
                if (preparedTargets.add(targetId)) {
                    engine.ensureUnlocks(actor, targetId, state, changedQuestIds, serverTick);
                }

                QuestProgressState progress = state.quest(binding.questId());
                if (!progress.unlocked()) {
                    continue;
                }
                if (progress.completed() && !definition.settings().repeatable()) {
                    continue;
                }

                Tag before = progress.getTaskProgress(binding.taskId(), binding.task());
                Tag after = binding.task().test(before, signal);
                if (binding.task().storage().same(before, after)) {
                    continue;
                }

                progress.setTaskProgress(binding.taskId(), binding.task(), after);
                changedQuestIds.add(binding.questId());

                boolean justCompleted = engine.recomputeCompletion(actor, targetId, state, binding.questId(), serverTick, true);
                if (justCompleted) {
                    changedQuestIds.add(binding.questId());
                    engine.ensureUnlocks(actor, targetId, state, changedQuestIds, serverTick);
                }
            }
        }

        RuntimeSyncs.syncChangedToAll(actor, progressData, syncService, changedQuestIds);
        performanceTracker.recordSignal(System.nanoTime() - start, visitedBindings, changedQuestIds.size());
    }
}
