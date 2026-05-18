package com.abo47.questsandstuff.quest.runtime;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import com.abo47.questsandstuff.quest.runtime.progress.QuestProgressState;
import com.abo47.questsandstuff.quest.runtime.reward.QuestRewardApplier;
import com.abo47.questsandstuff.quest.sync.QuestSyncService;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class QuestRewardClaims {
    private final QuestDefinitionStore definitionStore;
    private final QuestProgressSavedData progressData;
    private final QuestSyncService syncService;

    QuestRewardClaims(QuestDefinitionStore definitionStore, QuestProgressSavedData progressData, QuestSyncService syncService) {
        this.definitionStore = definitionStore;
        this.progressData = progressData;
        this.syncService = syncService;
    }

    void claimReward(ServerPlayer player, String questId, String rewardId, List<String> selectedRewardIds) {
        QuestDefinition definition = definitionStore.quests().get(questId);
        if (definition == null) {
            return;
        }

        QuestRewardDefinition reward = definition.rewards().get(rewardId);
        if (reward == null) {
            return;
        }

        PlayerQuestState state = progressData.state(player.getUUID());
        QuestProgressState questState = state.quest(questId);
        if (!questState.completed() && !definition.tasks().isEmpty()) {
            return;
        }
        if (questState.claimedRewards().contains(rewardId)) {
            return;
        }

        if (reward.selectable()) {
            if (!reward.isSelectableClaimValid(selectedRewardIds)) {
                return;
            }
            reward.grantSelected(player, selectedRewardIds);
        } else {
            reward.grant(player);
        }

        questState.claimedRewards().add(rewardId);
        syncService.sendQuestEvent(player, "reward_claimed", questId, rewardId);
        QuestRewardApplier.maybeResetRepeatable(definition, questState, player.server.getTickCount());
        progressData.setDirty();

        Set<String> changed = Set.of(questId);
        player.server.getPlayerList().getPlayers().forEach(target -> syncService.syncDelta(target, changed));
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
            if (!reward.canBeMassClaimed()) {
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
}
