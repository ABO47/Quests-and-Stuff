package com.abo47.questsandstuff.network.runtime;

import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2SClaimAllRewardsPacket(String questId) {
    public static C2SClaimAllRewardsPacket decode(FriendlyByteBuf buf) {
        return new C2SClaimAllRewardsPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId == null ? "" : questId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> QuestServices.engine(player.server)
                    .claimAllRewards(player, questId == null ? "" : questId));
        }
        context.setPacketHandled(true);
    }
}
