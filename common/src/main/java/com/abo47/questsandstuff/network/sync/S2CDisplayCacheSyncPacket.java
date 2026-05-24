package com.abo47.questsandstuff.network.sync;

import com.abo47.questsandstuff.network.QuestPacketContext;

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

    public void handle(QuestPacketContext context) {
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handleDisplayCache(sequence, payload));
    }
}
