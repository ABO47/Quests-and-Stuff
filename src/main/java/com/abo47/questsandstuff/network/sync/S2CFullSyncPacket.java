package com.abo47.questsandstuff.network.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2CFullSyncPacket(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
    public static S2CFullSyncPacket decode(FriendlyByteBuf buf) {
        long sequence = buf.readLong();
        int chunkIndex = buf.readVarInt();
        int chunkCount = buf.readVarInt();
        CompoundTag tag = buf.readNbt();
        return new S2CFullSyncPacket(sequence, chunkIndex, chunkCount, tag == null ? new CompoundTag() : tag);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(sequence);
        buf.writeVarInt(chunkIndex);
        buf.writeVarInt(chunkCount);
        buf.writeNbt(payload);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handleFull(sequence, chunkIndex, chunkCount, payload));
        context.setPacketHandled(true);
    }
}
