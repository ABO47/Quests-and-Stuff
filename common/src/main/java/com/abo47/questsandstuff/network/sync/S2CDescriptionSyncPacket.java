package com.abo47.questsandstuff.network.sync;

import com.abo47.questsandstuff.network.QuestPacketContext;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;


public record S2CDescriptionSyncPacket(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
    public static S2CDescriptionSyncPacket decode(FriendlyByteBuf buf) {
        long sequence = buf.readLong();
        int chunkIndex = buf.readVarInt();
        int chunkCount = buf.readVarInt();
        return new S2CDescriptionSyncPacket(sequence, chunkIndex, chunkCount, SyncPacketPayloadLimits.readNbt(buf));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(sequence);
        buf.writeVarInt(chunkIndex);
        buf.writeVarInt(chunkCount);
        buf.writeNbt(payload);
    }

    public void handle(QuestPacketContext context) {
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handleDescription(sequence, chunkIndex, chunkCount, payload));
    }
}
