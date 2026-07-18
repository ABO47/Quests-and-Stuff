package com.abo47.questsandstuff.network.team;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import com.abo47.questsandstuff.network.ModPacketContext;

public record S2CTeamSyncPacket(CompoundTag payload) {
    public static S2CTeamSyncPacket decode(FriendlyByteBuf buf) {
        return new S2CTeamSyncPacket(buf.readAnySizeNbt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(payload);
    }

    public void handle(ModPacketContext context) {
        context.enqueueWork(() -> ClientboundTeamPacketDispatch.handle(payload));
    }
}
