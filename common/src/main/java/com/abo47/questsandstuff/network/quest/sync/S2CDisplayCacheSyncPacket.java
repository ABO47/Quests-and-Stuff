package com.abo47.questsandstuff.network.quest.sync;

import com.abo47.questsandstuff.network.ModPacketContext;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;


public record S2CDisplayCacheSyncPacket(long sequence, CompoundTag payload) {
    public static S2CDisplayCacheSyncPacket decode(FriendlyByteBuf buf) {
        long sequence = buf.readLong();
        return new S2CDisplayCacheSyncPacket(sequence, SyncPacketPayloadLimits.readNbt(buf));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(sequence);
        buf.writeNbt(payload);
    }

    public void handle(ModPacketContext context) {
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handleDisplayCache(sequence, payload));
    }
}
