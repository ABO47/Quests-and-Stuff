package com.abo47.questsandstuff.network.quest.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import com.abo47.questsandstuff.network.ModPacketContext;

public final class S2CDeltaSyncPacket extends ChunkedSyncPacket {
    public S2CDeltaSyncPacket(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        super(sequence, chunkIndex, chunkCount, payload);
    }

    public S2CDeltaSyncPacket(Data d) {
        super(d);
    }

    public static S2CDeltaSyncPacket decode(FriendlyByteBuf buf) {
        return ChunkedSyncPacket.decode(buf, S2CDeltaSyncPacket::new);
    }

    @Override
    public void handle(ModPacketContext context) {
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handleDelta(sequence, chunkIndex, chunkCount, payload));
    }
}
