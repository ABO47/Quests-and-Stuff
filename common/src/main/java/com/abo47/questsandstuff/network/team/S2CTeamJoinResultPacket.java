package com.abo47.questsandstuff.network.team;

import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.ModPacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record S2CTeamJoinResultPacket(String messageKey, boolean success) {
    public static S2CTeamJoinResultPacket decode(FriendlyByteBuf buf) {
        return new S2CTeamJoinResultPacket(buf.readUtf(32767), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(messageKey == null ? "" : messageKey);
        buf.writeBoolean(success);
    }

    public void handle(ModPacketContext context) {
        context.enqueueWork(() -> ClientboundTeamPacketDispatch.handleJoinResult(messageKey, success));
    }

    public static void send(ServerPlayer player, String messageKey, boolean success) {
        ModNetwork.sendToPlayer(new S2CTeamJoinResultPacket(messageKey, success), player);
    }
}
