package com.abo47.questsandstuff.network.sync;

import com.abo47.questsandstuff.network.QuestPacketContext;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;


public record S2CDeltaSyncPacket(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
    public static S2CDeltaSyncPacket decode(FriendlyByteBuf buf) {
        long sequence = buf.readLong();
        int chunkIndex = buf.readVarInt();
        int chunkCount = buf.readVarInt();
        SyncPacketPayloadLimits.requireValidChunkMetadata(chunkIndex, chunkCount);
        return new S2CDeltaSyncPacket(sequence, chunkIndex, chunkCount, SyncPacketPayloadLimits.readNbt(buf));
    }

    public void encode(FriendlyByteBuf buf) {
        SyncPacketPayloadLimits.requireValidChunkMetadata(chunkIndex, chunkCount);
        buf.writeLong(sequence);
        buf.writeVarInt(chunkIndex);
        buf.writeVarInt(chunkCount);
        buf.writeNbt(payload);
    }

    public void handle(QuestPacketContext context) {
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handleDelta(sequence, chunkIndex, chunkCount, payload));
    }
}
