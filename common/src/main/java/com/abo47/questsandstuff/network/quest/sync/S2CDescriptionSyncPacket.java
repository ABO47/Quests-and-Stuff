package com.abo47.questsandstuff.network.quest.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import com.abo47.questsandstuff.network.ModPacketContext;

public final class S2CDescriptionSyncPacket extends ChunkedSyncPacket {
    public S2CDescriptionSyncPacket(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        super(sequence, chunkIndex, chunkCount, payload);
    }

    public static S2CDescriptionSyncPacket fromBytes(FriendlyByteBuf buf) {
        Data d = ChunkedSyncPacket.decode(buf);
        return new S2CDescriptionSyncPacket(d.sequence(), d.chunkIndex(), d.chunkCount(), d.payload());
    }

    @Override
    public void handle(ModPacketContext context) {
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handleDescription(sequence, chunkIndex, chunkCount, payload));
    }
}
