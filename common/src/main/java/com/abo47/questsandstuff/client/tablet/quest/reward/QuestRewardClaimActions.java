package com.abo47.questsandstuff.client.tablet.quest.reward;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.runtime.C2SClaimAllRewardsPacket;
import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class QuestRewardClaimActions {
    private QuestRewardClaimActions() {
    }

    public static void claimAll(Player player, String questId) {
        String targetQuestId = questId == null ? "" : questId.trim();
        if (player instanceof ServerPlayer serverPlayer) {
            QuestServices.engine(serverPlayer.server).claimAllRewards(serverPlayer, targetQuestId);
        } else {
            ModNetwork.sendToServer(new C2SClaimAllRewardsPacket(targetQuestId));
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] claim_all_rewards quest={}", targetQuestId.isBlank() ? "<all>" : targetQuestId);
    }
}
