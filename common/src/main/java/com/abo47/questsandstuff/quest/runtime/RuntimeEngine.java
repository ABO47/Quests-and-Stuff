package com.abo47.questsandstuff.quest.runtime;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.reward.QuestRewards;
import com.abo47.questsandstuff.quest.model.task.QuestTasks;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.runtime.progress.CompletableQuestEvaluator;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import com.abo47.questsandstuff.quest.runtime.progress.QuestProgressState;
import com.abo47.questsandstuff.quest.runtime.progress.QuestRuntimeIndex;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignal;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;
import com.abo47.questsandstuff.quest.sync.SyncService;
import com.abo47.questsandstuff.quest.runtime.team.TeamProgressProviders;
import com.abo47.questsandstuff.quest.sync.PerformanceTracker;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class RuntimeEngine {
    private final QuestDefinitionStore definitionStore;
    private final QuestProgressSavedData progressData;
    private final SyncService syncService;
    private final CompletableQuestEvaluator completableQuests = new CompletableQuestEvaluator();
    private final RewardClaims rewardClaims;
    private final SignalProgression signalProgression;
    private final ManualSubmissions manualSubmissions;
    private final ProgressAdminActions progressAdmin;
    private QuestRuntimeIndex index;

    public RuntimeEngine(QuestDefinitionStore definitionStore, QuestProgressSavedData progressData, SyncService syncService, PerformanceTracker performanceTracker) {
        this.definitionStore = definitionStore;
        this.progressData = progressData;
        this.syncService = syncService;
        this.rewardClaims = new RewardClaims(definitionStore, progressData, syncService, this);
        this.signalProgression = new SignalProgression(definitionStore, progressData, syncService, performanceTracker, this);
        this.manualSubmissions = new ManualSubmissions(definitionStore, progressData, syncService, this);
        this.progressAdmin = new ProgressAdminActions(definitionStore, progressData, syncService, this);
        QuestTasks.bootstrapDefaults();
        QuestRewards.bootstrapDefaults();
        TeamProgressProviders.installHooks(this::onTeamMembershipChanged);
        rebuildIndex();
    }

    public void rebuildIndex() {
        this.index = new QuestRuntimeIndex(definitionStore.quests());
    }

    public void refreshIndex(Set<String> questIds) {
        if (questIds == null || questIds.isEmpty()) {
            return;
        }
        index.upsertAll(definitionsForIds(questIds));
    }

    public void preparePlayerForFullSync(ServerPlayer player) {
        if (player == null || progressData == null) {
            return;
        }

        Set<String> changedQuestIds = new HashSet<>();
        PlayerQuestState state = progressData.state(player.getUUID());
        reconcileUnlocks(player, player.getUUID(), state, changedQuestIds, player.server.getTickCount(), false);
        if (!changedQuestIds.isEmpty()) {
            progressData.setDirty();
        }
    }

    public Set<String> preparePlayersForDeltaSync(List<ServerPlayer> players, Set<String> seedQuestIds) {
        Set<String> changedQuestIds = new HashSet<>();
        if (seedQuestIds != null) {
            changedQuestIds.addAll(seedQuestIds);
        }
        if (players == null || players.isEmpty() || progressData == null || changedQuestIds.isEmpty()) {
            return changedQuestIds;
        }

        List<QuestDefinition> definitions = definitionsForIds(changedQuestIds);
        if (definitions.isEmpty()) {
            return changedQuestIds;
        }

        for (ServerPlayer player : players) {
            if (player == null) {
                continue;
            }
            Set<String> playerChanged = new HashSet<>();
            PlayerQuestState state = progressData.state(player.getUUID());
            reconcileUnlocks(player, player.getUUID(), state, playerChanged, player.server.getTickCount(), false, definitions);
            if (!playerChanged.isEmpty()) {
                changedQuestIds.addAll(playerChanged);
                progressData.setDirty();
            }
        }
        return changedQuestIds;
    }

    public void preparePlayersForFullSync(List<ServerPlayer> players) {
        if (players == null || players.isEmpty()) {
            return;
        }
        for (ServerPlayer player : players) {
            preparePlayerForFullSync(player);
        }
    }

    public void clearQuestProgress(String questId) {
        String normalized = questId == null ? "" : questId.trim();
        if (normalized.isBlank() || progressData == null) {
            return;
        }
        boolean changed = false;
        for (PlayerQuestState state : progressData.states().values()) {
            changed |= state.quests().remove(normalized) != null;
        }
        if (changed) {
            progressData.setDirty();
        }
    }

    public void onSignal(QuestSignal signal) {
        signalProgression.onSignal(signal, index);
    }

    public void claimReward(ServerPlayer player, String questId, String rewardId) {
        claimReward(player, questId, rewardId, List.of());
    }

    public void claimReward(ServerPlayer player, String questId, String rewardId, List<String> selectedRewardIds) {
        rewardClaims.claimReward(player, questId, rewardId, selectedRewardIds);
    }

    public void claimSelectedRewardAndAvailableRewards(ServerPlayer player, String questId, String rewardId, List<String> selectedRewardIds) {
        rewardClaims.claimSelectedRewardAndAvailableRewards(player, questId, rewardId, selectedRewardIds);
    }

    public void claimAvailableRewards(ServerPlayer player, String questId) {
        rewardClaims.claimAvailableRewards(player, questId);
    }

    public void claimAllRewards(ServerPlayer player, String questId) {
        rewardClaims.claimAllRewards(player, questId);
    }

    public void completeQuest(ServerPlayer player, String questId) {
        progressAdmin.completeQuest(player, questId);
    }

    public void resetQuest(ServerPlayer player, String questId) {
        progressAdmin.resetQuest(player, questId);
    }

    public void resetAll(ServerPlayer player) {
        progressAdmin.resetAll(player);
    }

    public void togglePin(ServerPlayer player, String questId) {
        progressAdmin.togglePin(player, questId);
    }

    public void runManualTask(ServerPlayer player, String taskKey) {
        onSignal(QuestSignal.of(QuestSignalType.MANUAL_CHECK, player, taskKey, 1, player.blockPosition()));
    }

    public void submitManualCheckTask(ServerPlayer player, String questId, String taskId) {
        manualSubmissions.submitCheckTask(player, questId, taskId);
    }

    public void submitManualItemTask(ServerPlayer player, String questId, String taskId) {
        manualSubmissions.submitItemTask(player, questId, taskId);
    }

    public void submitManualXpTask(ServerPlayer player, String questId, String taskId) {
        manualSubmissions.submitXpTask(player, questId, taskId);
    }

    public boolean hasQuest(String questId) {
        return definitionStore.containsQuest(questId);
    }

    public Set<String> questIds() {
        return definitionStore.questIds();
    }

    public boolean isQuestCompleted(UUID playerId, String questId) {
        return progressData.state(playerId).quest(questId).completed();
    }

    public Set<String> trackedStatTaskTargets() {
        return index.trackedStatTaskTargets();
    }

    boolean recomputeCompletion(ServerPlayer actor, UUID ownerId, PlayerQuestState state, String questId, long serverTick, boolean announce) {
        QuestDefinition definition = definitionStore.quest(questId);
        if (definition == null) {
            return false;
        }

        QuestProgressState progress = state.quest(questId);

        boolean complete = false;
        if (!definition.tasks().isEmpty()) {
            complete = true;
            for (Map.Entry<String, QuestTaskDefinition> task : definition.tasks().entrySet()) {
                if (!CompletionRules.isTaskComplete(definition, progress, task.getKey(), task.getValue())) {
                    complete = false;
                    break;
                }
            }
        }

        boolean justCompleted = complete && !progress.completed();
        if (justCompleted) {
            progress.setCompleted(true, serverTick);
            applyExclusiveChoiceDisable(actor, ownerId, state, questId);
            if (announce) {
                ServerPlayer owner = actor.server.getPlayerList().getPlayer(ownerId);
                if (owner != null) {
                    syncService.sendQuestEvent(owner, "quest_completed", definition.id(), "");
                }
            }
        }
        return justCompleted;
    }

    public void clearExclusiveChoiceDisabled(Set<String> questIds) {
        if (questIds == null || questIds.isEmpty() || progressData == null) {
            return;
        }
        boolean changed = false;
        for (PlayerQuestState state : progressData.states().values()) {
            changed |= clearExclusiveChoiceDisabledForState(state, questIds);
        }
        if (changed) {
            progressData.setDirty();
        }
    }

    public void clearExclusiveChoiceDisabledForPlayer(UUID playerId, Set<String> questIds) {
        if (questIds == null || questIds.isEmpty() || progressData == null || playerId == null) {
            return;
        }
        PlayerQuestState state = progressData.state(playerId);
        if (clearExclusiveChoiceDisabledForState(state, questIds)) {
            progressData.setDirty();
        }
    }

    public Set<String> exclusiveChoiceSiblings(String questId) {
        Set<String> siblings = new HashSet<>();
        for (String chapter : definitionStore.chapterOrder()) {
            for (CanvasExclusiveChoice ec : definitionStore.canvasExclusiveChoices(chapter)) {
                if (ec.connectionQuestIds().contains(questId)) {
                    for (String sibling : ec.connectionQuestIds()) {
                        if (!sibling.equals(questId)) {
                            siblings.add(sibling);
                        }
                    }
                }
            }
        }
        return siblings;
    }

    private boolean clearExclusiveChoiceDisabledForState(PlayerQuestState state, Set<String> questIds) {
        boolean changed = false;
        for (String questId : questIds) {
            QuestProgressState progress = state.quests().get(questId);
            if (progress != null && progress.disabledByExclusiveChoice().remove(questId)) {
                changed = true;
            }
        }
        return changed;
    }

    void applyExclusiveChoiceDisable(ServerPlayer actor, UUID ownerId, PlayerQuestState state, String completedQuestId) {
        for (String chapter : definitionStore.chapterOrder()) {
            for (CanvasExclusiveChoice ec : definitionStore.canvasExclusiveChoices(chapter)) {
                if (ec.connectionQuestIds().contains(completedQuestId)) {
                    for (String siblingId : ec.connectionQuestIds()) {
                        if (!siblingId.equals(completedQuestId)) {
                            state.quest(siblingId).setDisabledByExclusiveChoice(siblingId, true);
                        }
                    }
                }
            }
        }
    }

    void ensureUnlocks(ServerPlayer actor, UUID ownerId, PlayerQuestState state, Set<String> changedQuestIds, long tick) {
        reconcileUnlocks(actor, ownerId, state, changedQuestIds, tick, true);
    }

    private void reconcileUnlocks(ServerPlayer actor, UUID ownerId, PlayerQuestState state, Set<String> changedQuestIds, long tick, boolean announce) {
        reconcileUnlocks(actor, ownerId, state, changedQuestIds, tick, announce, definitionStore.questDefinitions());
    }

    private void reconcileUnlocks(ServerPlayer actor, UUID ownerId, PlayerQuestState state, Set<String> changedQuestIds, long tick, boolean announce, Collection<QuestDefinition> definitions) {
        Collection<QuestDefinition> candidates = definitions == null ? List.of() : definitions;
        int passes = Math.max(1, candidates.size());
        for (int pass = 0; pass < passes; pass++) {
            boolean changedThisPass = false;
            for (QuestDefinition definition : candidates) {
                if (definition == null) {
                    continue;
                }
                QuestProgressState progress = state.quest(definition.id());
                boolean shouldBeUnlocked = shouldBeUnlocked(state, definition, progress);
                if (progress.unlocked() == shouldBeUnlocked) {
                    continue;
                }

                progress.setUnlocked(shouldBeUnlocked);
                changedQuestIds.add(definition.id());
                changedThisPass = true;

                if (shouldBeUnlocked) {
                    completableQuests.initializeUnlockTasks(actor, ownerId, definition, progress);
                    boolean justCompleted = recomputeCompletion(actor, ownerId, state, definition.id(), tick, false);
                    if (justCompleted) {
                        changedQuestIds.add(definition.id());
                    }
                    if (announce && syncService != null) {
                        ServerPlayer owner = actor.server.getPlayerList().getPlayer(ownerId);
                        if (owner != null) {
                            syncService.sendQuestEvent(owner, "quest_unlocked", definition.id(), "");
                            if (justCompleted) {
                                syncService.sendQuestEvent(owner, "quest_completed", definition.id(), "");
                            }
                        }
                    }
                }
            }

            if (!changedThisPass) {
                return;
            }
        }
    }

    private List<QuestDefinition> definitionsForIds(Set<String> questIds) {
        if (questIds == null || questIds.isEmpty()) {
            return List.of();
        }
        List<QuestDefinition> definitions = new ArrayList<>();
        for (String questId : questIds) {
            QuestDefinition definition = definitionStore.quest(questId);
            if (definition != null) {
                definitions.add(definition);
            }
        }
        return definitions;
    }

    private boolean shouldBeUnlocked(PlayerQuestState state, QuestDefinition definition, QuestProgressState progress) {
        if (!completableQuests.shouldBeUnlocked(state, definition, progress)) {
            return false;
        }
        for (String chapter : definitionStore.chapterOrder()) {
            for (CanvasExclusiveChoice ec : definitionStore.canvasExclusiveChoices(chapter)) {
                if (ec.connectionQuestIds().contains(definition.id())) {
                    for (String prerequisiteId : ec.prerequisiteQuestIds()) {
                        if (!state.quest(prerequisiteId).completed()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    void onTeamMembershipChanged(net.minecraft.server.level.ServerLevel level, UUID changedPlayer) {
        TeamProgressReconciler.onTeamMembershipChanged(level, changedPlayer, definitionStore, progressData, syncService);
    }

    public void triggerTeamMembershipChanged(net.minecraft.server.level.ServerLevel level, UUID changedPlayer) {
        onTeamMembershipChanged(level, changedPlayer);
    }

    public boolean isVisibleFor(PlayerQuestState state, QuestDefinition definition) {
        return VisibilityRules.isVisibleFor(state, definition, definitionStore.quests());
    }
}
