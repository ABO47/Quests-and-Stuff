package com.abo47.questsandstuff.network.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

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

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handleDisplayCache(sequence, payload));
        context.setPacketHandled(true);
    }
}
