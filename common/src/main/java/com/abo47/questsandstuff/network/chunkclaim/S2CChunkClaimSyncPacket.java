package com.abo47.questsandstuff.network.chunkclaim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import com.abo47.questsandstuff.network.ModPacketContext;

public record S2CChunkClaimSyncPacket(CompoundTag payload) {
    public static S2CChunkClaimSyncPacket decode(FriendlyByteBuf buf) {
        return new S2CChunkClaimSyncPacket(buf.readAnySizeNbt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(payload);
    }

    public void handle(ModPacketContext context) {
        context.enqueueWork(() -> ClientboundChunkClaimPacketDispatch.handle(payload));
    }
}
