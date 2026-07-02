package com.abo47.questsandstuff.network.quest.sync;

import com.abo47.questsandstuff.network.ModPacketContext;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;


public final class S2CDeltaSyncPacket extends ChunkedSyncPacket {
    public S2CDeltaSyncPacket(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        super(sequence, chunkIndex, chunkCount, payload);
    }

    public static S2CDeltaSyncPacket fromBytes(FriendlyByteBuf buf) {
        Data d = ChunkedSyncPacket.decode(buf);
        return new S2CDeltaSyncPacket(d.sequence(), d.chunkIndex(), d.chunkCount(), d.payload());
    }

    @Override
    public void handle(ModPacketContext context) {
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handleDelta(sequence, chunkIndex, chunkCount, payload));
    }
}
