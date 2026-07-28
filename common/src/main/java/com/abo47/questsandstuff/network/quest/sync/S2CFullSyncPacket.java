package com.abo47.questsandstuff.network.quest.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import com.abo47.questsandstuff.network.ModPacketContext;

public final class S2CFullSyncPacket extends ChunkedSyncPacket {
    public S2CFullSyncPacket(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        super(sequence, chunkIndex, chunkCount, payload);
    }

    public S2CFullSyncPacket(Data d) {
        super(d);
    }

    public static S2CFullSyncPacket decode(FriendlyByteBuf buf) {
        return ChunkedSyncPacket.decode(buf, S2CFullSyncPacket::new);
    }

    @Override
    public void handle(ModPacketContext context) {
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handleFull(sequence, chunkIndex, chunkCount, payload));
    }
}
