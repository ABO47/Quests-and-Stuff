package com.abo47.questsandstuff.client.tablet.quest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.ui.IntegratedServerActions;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.runtime.C2SClaimAllRewardsPacket;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import net.minecraft.world.entity.player.Player;

public final class RewardClaimHandler {
    private RewardClaimHandler() {
    }

    public static void claimAll(Player player, String questId) {
        String targetQuestId = questId == null ? "" : questId.trim();
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServiceRegistry.engine(serverPlayer.server).claimAllRewards(serverPlayer, targetQuestId),
                () -> ModNetwork.sendToServer(new C2SClaimAllRewardsPacket(targetQuestId)));
        QuestsAndStuffMod.debugLog("[QnS:UI] claim_all_rewards quest={}", targetQuestId.isBlank() ? "<all>" : targetQuestId);
    }
}
