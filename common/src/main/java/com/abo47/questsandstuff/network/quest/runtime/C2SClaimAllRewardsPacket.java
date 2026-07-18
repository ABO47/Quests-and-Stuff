package com.abo47.questsandstuff.network.quest.runtime;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;

public record C2SClaimAllRewardsPacket(String questId) {
    public static C2SClaimAllRewardsPacket decode(FriendlyByteBuf buf) {
        return new C2SClaimAllRewardsPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId == null ? "" : questId);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (player != null) {
            context.enqueueWork(() -> QuestServiceRegistry.engine(player.server)
                    .claimAllRewards(player, questId == null ? "" : questId));
        }
    }
}
