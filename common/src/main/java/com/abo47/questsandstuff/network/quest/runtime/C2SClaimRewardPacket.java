package com.abo47.questsandstuff.network.quest.runtime;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;

public record C2SClaimRewardPacket(String questId, String rewardId) {
    public static C2SClaimRewardPacket decode(FriendlyByteBuf buf) {
        return new C2SClaimRewardPacket(buf.readUtf(), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId);
        buf.writeUtf(rewardId);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (player != null) {
            context.enqueueWork(() -> QuestServiceRegistry.engine(player.server).claimReward(player, questId, rewardId));
        }
    }
}
