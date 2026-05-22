package com.abo47.questsandstuff.quest.runtime.reward;

import com.abo47.questsandstuff.quest.runtime.progress.QuestProgressState;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
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
        for (Map.Entry<String, QuestRewardDefinition> entry : definition.rewards().entrySet()) {
            if (entry.getValue().selectable()) {
                continue;
            }
            if (questState.claimedRewards().contains(entry.getKey())) {
                continue;
            }
            entry.getValue().grant(player);
            questState.claimedRewards().add(entry.getKey());
            syncService.sendQuestEvent(player, "reward_claimed", definition.id(), entry.getKey());
        }
        maybeResetRepeatable(definition, questState, serverTick);
    }

    public static void maybeResetRepeatable(QuestDefinition definition, QuestProgressState questState, long serverTick) {
        if (!definition.settings().repeatable()) {
            return;
        }
        if (!questState.completed()) {
            return;
        }
        if (!allRewardsClaimed(definition, questState)) {
            return;
        }
        questState.clearTaskProgress();
        questState.claimedRewards().clear();
        questState.setCompleted(false, serverTick);
        questState.setUnlocked(true);
    }

    private static boolean allRewardsClaimed(QuestDefinition definition, QuestProgressState questState) {
        if (definition.rewards().isEmpty()) {
            return true;
        }
        return questState.claimedRewards().containsAll(definition.rewards().keySet());
    }
}
