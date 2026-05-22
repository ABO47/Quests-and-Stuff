package com.abo47.questsandstuff.network.sync;

import com.abo47.questsandstuff.network.QuestPacketContext;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;


public record S2CDisplayCacheSyncPacket(long sequence, CompoundTag payload) {
    public static S2CDisplayCacheSyncPacket decode(FriendlyByteBuf buf) {
        long sequence = buf.readLong();
        CompoundTag tag = buf.readNbt();
        return new S2CDisplayCacheSyncPacket(sequence, tag == null ? new CompoundTag() : tag);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(sequence);
        buf.writeNbt(payload);
    }

    public void handle(QuestPacketContext context) {
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handleDisplayCache(sequence, payload));
    }
}
