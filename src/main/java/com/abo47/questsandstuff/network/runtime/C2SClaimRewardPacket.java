package com.abo47.questsandstuff.network.runtime;

import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2SClaimRewardPacket(String questId, String rewardId) {
    public static C2SClaimRewardPacket decode(FriendlyByteBuf buf) {
        return new C2SClaimRewardPacket(buf.readUtf(), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId);
        buf.writeUtf(rewardId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> QuestServices.engine(player.server).claimReward(player, questId, rewardId));
        }
        context.setPacketHandled(true);
    }
}
