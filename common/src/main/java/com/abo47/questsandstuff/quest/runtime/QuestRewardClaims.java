package com.abo47.questsandstuff.quest.runtime;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.reward.SelectableQuestRewardDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import com.abo47.questsandstuff.quest.runtime.progress.QuestProgressState;
import com.abo47.questsandstuff.quest.runtime.reward.QuestRewardApplier;
import com.abo47.questsandstuff.quest.sync.QuestSyncService;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class QuestRewardClaims {
    private final QuestDefinitionStore definitionStore;
    private final QuestProgressSavedData progressData;
    private final QuestSyncService syncService;
    private final QuestRuntimeEngine engine;

    QuestRewardClaims(QuestDefinitionStore definitionStore, QuestProgressSavedData progressData, QuestSyncService syncService, QuestRuntimeEngine engine) {
        this.definitionStore = definitionStore;
        this.progressData = progressData;
        this.syncService = syncService;
        this.engine = engine;
    }

    boolean claimReward(ServerPlayer player, String questId, String rewardId, List<String> selectedRewardIds) {
        if (player == null) {
            return false;
        }
        QuestDefinition definition = definitionStore.quests().get(questId);
        if (definition == null) {
            return false;
        }

        QuestRewardDefinition reward = definition.rewards().get(rewardId);
        if (reward == null) {
            return false;
        }

        PlayerQuestState state = progressData.state(player.getUUID());
        Set<String> changed = new HashSet<>();
        engine.ensureUnlocks(player, player.getUUID(), state, changed, player.server.getTickCount());
        QuestProgressState questState = state.quest(questId);
        if (!questState.unlocked()) {
            syncChanged(player, changed);
            return false;
        }
        if (!questState.completed() && !definition.tasks().isEmpty()) {
            syncChanged(player, changed);
            return false;
        }
        if (questState.claimedRewards().contains(rewardId)) {
            syncChanged(player, changed);
            return false;
        }

        List<String> selected = selectedRewardIds == null ? List.of() : selectedRewardIds;
        if (!reward.canClaim(player)) {
            syncChanged(player, changed);
            return false;
        }

        if (reward.selectable()) {
            if (!reward.isSelectableClaimValid(player, selected)) {
                syncChanged(player, changed);
                return false;
            }
            reward.grantSelected(player, selected);
        } else {
            reward.grant(player);
        }

        questState.claimedRewards().add(rewardId);
        syncService.sendQuestEvent(player, "reward_claimed", questId, rewardId);
        QuestRewardApplier.maybeResetRepeatable(definition, questState, player.server.getTickCount());
        changed.add(questId);
        syncChanged(player, changed);
        return true;
    }

    void claimSelectedRewardAndAvailableRewards(ServerPlayer player, String questId, String rewardId, List<String> selectedRewardIds) {
        claimAvailableRewards(player, questId);
        if (claimReward(player, questId, rewardId, selectedRewardIds) || isAlreadyClaimedValidSingletonChoice(player, questId, rewardId, selectedRewardIds)) {
            markUnselectedSingletonSelectableRewardsClaimed(player, questId, rewardId);
        }
    }

    void claimAvailableRewards(ServerPlayer player, String questId) {
        QuestDefinition definition = definitionStore.quests().get(questId);
        if (definition == null) {
            return;
        }
        for (String rewardId : definition.rewards().keySet()) {
            QuestRewardDefinition reward = definition.rewards().get(rewardId);
            if (reward == null || reward.selectable()) {
                continue;
            }
            if (!reward.canBeMassClaimed() || !reward.canClaim(player)) {
                continue;
            }
            claimReward(player, questId, rewardId, List.of());
        }
    }

    void claimAllRewards(ServerPlayer player, String questId) {
        if (questId != null && !questId.isBlank()) {
            claimAvailableRewards(player, questId);
            return;
        }

        for (String id : new ArrayList<>(definitionStore.quests().keySet())) {
            claimAvailableRewards(player, id);
        }
    }

    private void markUnselectedSingletonSelectableRewardsClaimed(ServerPlayer player, String questId, String selectedRewardId) {
        QuestDefinition definition = definitionStore.quests().get(questId);
        if (definition == null || !isSingletonSelectable(definition.rewards().get(selectedRewardId))) {
            return;
        }
        QuestProgressState questState = progressData.state(player.getUUID()).quest(questId);
        boolean changed = false;
        for (String rewardId : definition.rewards().keySet()) {
            if (rewardId.equals(selectedRewardId) || questState.claimedRewards().contains(rewardId)) {
                continue;
            }
            QuestRewardDefinition reward = definition.rewards().get(rewardId);
            if (!isSingletonSelectable(reward)) {
                continue;
            }
            questState.claimedRewards().add(rewardId);
            changed = true;
        }
        if (!changed) {
            return;
        }
        QuestRewardApplier.maybeResetRepeatable(definition, questState, player.server.getTickCount());
        syncChanged(player, Set.of(questId));
    }

    private boolean isAlreadyClaimedValidSingletonChoice(ServerPlayer player, String questId, String rewardId, List<String> selectedRewardIds) {
        QuestDefinition definition = definitionStore.quests().get(questId);
        if (definition == null) {
            return false;
        }
        QuestRewardDefinition reward = definition.rewards().get(rewardId);
        List<String> selected = selectedRewardIds == null ? List.of() : selectedRewardIds;
        if (!isSingletonSelectable(reward) || !reward.canClaim(player) || !reward.isSelectableClaimValid(player, selected)) {
            return false;
        }
        QuestProgressState questState = progressData.state(player.getUUID()).quest(questId);
        return questState.unlocked() && questState.claimedRewards().contains(rewardId);
    }

    private static boolean isSingletonSelectable(QuestRewardDefinition reward) {
        if (reward instanceof SelectableQuestRewardDefinition selectable) {
            return selectable.safeAmount() == 1 && selectable.rewards().size() == 1;
        }
        return reward != null && reward.selectable();
    }

    private void syncChanged(ServerPlayer player, Set<String> changedQuestIds) {
        if (changedQuestIds.isEmpty()) {
            return;
        }
        progressData.setDirty();
        player.server.getPlayerList().getPlayers().forEach(target -> syncService.syncDelta(target, changedQuestIds));
    }
}
