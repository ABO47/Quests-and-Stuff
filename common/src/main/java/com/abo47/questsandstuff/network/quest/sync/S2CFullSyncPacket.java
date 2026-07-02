package com.abo47.questsandstuff.network.quest.sync;

import com.abo47.questsandstuff.network.ModPacketContext;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;


public final class S2CFullSyncPacket extends ChunkedSyncPacket {
    public S2CFullSyncPacket(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        super(sequence, chunkIndex, chunkCount, payload);
    }

    public static S2CFullSyncPacket fromBytes(FriendlyByteBuf buf) {
        Data d = ChunkedSyncPacket.decode(buf);
        return new S2CFullSyncPacket(d.sequence(), d.chunkIndex(), d.chunkCount(), d.payload());
    }

    @Override
    public void handle(ModPacketContext context) {
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handleFull(sequence, chunkIndex, chunkCount, payload));
    }
}
