package com.abo47.questsandstuff.network.quest.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import com.abo47.questsandstuff.network.ModPacketContext;

public final class S2CDescriptionSyncPacket extends ChunkedSyncPacket {
    public S2CDescriptionSyncPacket(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        super(sequence, chunkIndex, chunkCount, payload);
    }

    public S2CDescriptionSyncPacket(Data d) {
        super(d);
    }

    public static S2CDescriptionSyncPacket decode(FriendlyByteBuf buf) {
        return ChunkedSyncPacket.decode(buf, S2CDescriptionSyncPacket::new);
    }

    @Override
    public void handle(ModPacketContext context) {
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handleDescription(sequence, chunkIndex, chunkCount, payload));
    }
}
