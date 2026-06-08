package com.abo47.questsandstuff.quest.runtime;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.storage.IntegerTaskStorage;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.item.CollectionMode;
import com.abo47.questsandstuff.quest.model.task.item.GatherItemQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.player.XpMode;
import com.abo47.questsandstuff.quest.model.task.player.XpQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.progress.CheckQuestTaskDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import com.abo47.questsandstuff.quest.runtime.progress.QuestProgressState;
import com.abo47.questsandstuff.quest.runtime.team.TeamProgressProviders;
import com.abo47.questsandstuff.quest.sync.QuestSyncService;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

final class QuestManualSubmissions {
    private final QuestDefinitionStore definitionStore;
    private final QuestProgressSavedData progressData;
    private final QuestSyncService syncService;
    private final QuestRuntimeEngine engine;

    QuestManualSubmissions(QuestDefinitionStore definitionStore, QuestProgressSavedData progressData, QuestSyncService syncService, QuestRuntimeEngine engine) {
        this.definitionStore = definitionStore;
        this.progressData = progressData;
        this.syncService = syncService;
        this.engine = engine;
    }

    void submitCheckTask(ServerPlayer player, String questId, String taskId) {
        QuestDefinition definition = definitionStore.quests().get(questId);
        if (definition == null) {
            return;
        }
        QuestTaskDefinition task = definition.tasks().get(taskId);
        if (!(task instanceof CheckQuestTaskDefinition)) {
            return;
        }

        applyManualCheckProgress(player, manualTargets(player, definition), questId, taskId, task);
    }

    void submitItemTask(ServerPlayer player, String questId, String taskId) {
        QuestDefinition definition = definitionStore.quests().get(questId);
        if (definition == null) {
            return;
        }
        QuestTaskDefinition task = definition.tasks().get(taskId);
        if (!(task instanceof GatherItemQuestTaskDefinition gather)) {
            return;
        }

        if (gather.collection() != CollectionMode.MANUAL && gather.collection() != CollectionMode.CONSUME) {
            return;
        }

        int available = gather.countMatching(player);
        List<UUID> targets = manualTargets(player, definition);

        int required = remainingForTargets(targets, questId, taskId, task.safeGoal());
        int accepted = Math.min(available, required);
        if (accepted <= 0) {
            return;
        }

        if (gather.collection() == CollectionMode.CONSUME) {
            accepted = gather.consumeMatching(player, accepted);
        }
        if (accepted <= 0) {
            return;
        }

        applyManualTaskProgress(player, targets, questId, taskId, task, accepted);
    }

    void submitXpTask(ServerPlayer player, String questId, String taskId) {
        QuestDefinition definition = definitionStore.quests().get(questId);
        if (definition == null) {
            return;
        }
        QuestTaskDefinition task = definition.tasks().get(taskId);
        if (!(task instanceof XpQuestTaskDefinition xpTask)) {
            return;
        }

        if (xpTask.collection() != CollectionMode.MANUAL && xpTask.collection() != CollectionMode.CONSUME) {
            return;
        }

        int available = xpTask.mode() == XpMode.LEVEL ? player.experienceLevel : player.totalExperience;

        List<UUID> targets = manualTargets(player, definition);

        int required = remainingForTargets(targets, questId, taskId, task.safeGoal());
        int accepted = Math.min(available, required);
        if (accepted <= 0) {
            return;
        }

        if (xpTask.collection() == CollectionMode.CONSUME) {
            if (xpTask.mode() == XpMode.LEVEL) {
                player.giveExperienceLevels(-accepted);
            } else {
                player.giveExperiencePoints(-accepted);
            }
        }

        applyManualTaskProgress(player, targets, questId, taskId, task, accepted);
    }

    private List<UUID> manualTargets(ServerPlayer player, QuestDefinition definition) {
        List<UUID> targets = definition.settings().individualProgress()
                ? List.of(player.getUUID())
                : TeamProgressProviders.members(player.serverLevel(), player.getUUID());
        return targets.isEmpty() ? List.of(player.getUUID()) : targets;
    }

    private void applyManualCheckProgress(ServerPlayer player, List<UUID> targets, String questId, String taskId, QuestTaskDefinition task) {
        long tick = player.server.getTickCount();
        Set<String> changed = new HashSet<>();
        for (UUID targetId : targets) {
            PlayerQuestState state = progressData.state(targetId);
            engine.ensureUnlocks(player, targetId, state, changed, tick);
            QuestDefinition definition = definitionStore.quests().get(questId);
            if (definition == null || !engine.isVisibleFor(state, definition)) {
                continue;
            }
            QuestProgressState questState = state.quest(questId);
            if (!questState.unlocked() || QuestCompletionRules.isTaskComplete(definition, questState, taskId, task)) {
                continue;
            }
            questState.setTaskProgress(taskId, task, QuestCompletionRules.completeProgress(task));
            changed.add(questId);
            if (engine.recomputeCompletion(player, targetId, state, questId, tick, true)) {
                engine.ensureUnlocks(player, targetId, state, changed, tick);
            }
        }
        QuestRuntimeSyncs.syncChangedToAll(player, progressData, syncService, changed);
    }

    private void applyManualTaskProgress(ServerPlayer player, List<UUID> targets, String questId, String taskId, QuestTaskDefinition task, int accepted) {
        long tick = player.server.getTickCount();
        Set<String> changed = new HashSet<>();
        for (UUID targetId : targets) {
            PlayerQuestState state = progressData.state(targetId);
            engine.ensureUnlocks(player, targetId, state, changed, tick);
            QuestProgressState questState = state.quest(questId);
            if (!questState.unlocked()) {
                continue;
            }
            questState.setTaskProgress(taskId, task, IntegerTaskStorage.INSTANCE.add(questState.getTaskProgress(taskId, task), accepted, task.safeGoal()));
            changed.add(questId);
            if (engine.recomputeCompletion(player, targetId, state, questId, tick, true)) {
                engine.ensureUnlocks(player, targetId, state, changed, tick);
            }
        }
        QuestRuntimeSyncs.syncChangedToAll(player, progressData, syncService, changed);
    }

    private int remainingForTargets(List<UUID> targets, String questId, String taskId, int goal) {
        int remaining = Integer.MAX_VALUE;
        for (UUID targetId : targets) {
            PlayerQuestState state = progressData.state(targetId);
            QuestProgressState questState = state.quest(questId);
            remaining = Math.min(remaining, Math.max(0, goal - questState.getTaskCount(taskId)));
        }
        return remaining == Integer.MAX_VALUE ? 0 : remaining;
    }
}
