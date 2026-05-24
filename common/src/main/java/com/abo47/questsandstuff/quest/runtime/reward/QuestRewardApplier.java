package com.abo47.questsandstuff.quest.runtime.reward;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
import com.abo47.questsandstuff.quest.runtime.progress.QuestProgressState;
import com.abo47.questsandstuff.quest.sync.QuestSyncService;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public final class QuestRewardApplier {
    private QuestRewardApplier() {
    }

    public static void autoClaimNonSelectableRewards(
            ServerPlayer player,
            QuestDefinition definition,
            QuestProgressState questState,
            long serverTick,
            QuestSyncService syncService
    ) {
        if (hasUnclaimedSelectableRewards(definition, questState)) {
            return;
        }
        for (Map.Entry<String, QuestRewardDefinition> entry : definition.rewards().entrySet()) {
            if (entry.getValue().selectable()) {
                continue;
            }
            if (questState.claimedRewards().contains(entry.getKey())) {
                continue;
            }
            if (!entry.getValue().canClaim(player)) {
                continue;
            }
            entry.getValue().grant(player);
            questState.claimedRewards().add(entry.getKey());
            syncService.sendQuestEvent(player, "reward_claimed", definition.id(), entry.getKey());
        }
        maybeResetRepeatable(player, definition, questState, serverTick);
    }

    public static boolean hasUnclaimedSelectableRewards(QuestDefinition definition, QuestProgressState questState) {
        if (definition == null || questState == null) {
            return false;
        }
        for (Map.Entry<String, QuestRewardDefinition> entry : definition.rewards().entrySet()) {
            QuestRewardDefinition reward = entry.getValue();
            if (reward != null && reward.selectable() && !questState.claimedRewards().contains(entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    public static boolean maybeResetRepeatable(ServerPlayer player, QuestDefinition definition, QuestProgressState questState, long serverTick) {
        if (!definition.settings().repeatable()) {
            return false;
        }
        if (!questState.completed()) {
            return false;
        }
        if (!allClaimableRewardsClaimed(player, definition, questState)) {
            return false;
        }
        questState.clearTaskProgress();
        questState.claimedRewards().clear();
        questState.setCompleted(false, serverTick);
        questState.setUnlocked(true);
        return true;
    }

    private static boolean allClaimableRewardsClaimed(ServerPlayer player, QuestDefinition definition, QuestProgressState questState) {
        if (definition.rewards().isEmpty()) {
            return true;
        }
        for (Map.Entry<String, QuestRewardDefinition> entry : definition.rewards().entrySet()) {
            if (!entry.getValue().canClaim(player)) {
                continue;
            }
            if (!questState.claimedRewards().contains(entry.getKey())) {
                return false;
            }
        }
        return true;
    }
}
