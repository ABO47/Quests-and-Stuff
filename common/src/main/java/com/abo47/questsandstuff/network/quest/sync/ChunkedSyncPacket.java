package com.abo47.questsandstuff.network.quest.sync;

import com.abo47.questsandstuff.network.ModPacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public abstract class ChunkedSyncPacket {
    protected final long sequence;
    protected final int chunkIndex;
    protected final int chunkCount;
    protected final CompoundTag payload;

    protected ChunkedSyncPacket(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        this.sequence = sequence;
        this.chunkIndex = chunkIndex;
        this.chunkCount = chunkCount;
        this.payload = payload;
    }

    public long sequence() { return sequence; }
    public int chunkIndex() { return chunkIndex; }
    public int chunkCount() { return chunkCount; }
    public CompoundTag payload() { return payload; }

    protected static Data decode(FriendlyByteBuf buf) {
        long sequence = buf.readLong();
        int chunkIndex = buf.readVarInt();
        int chunkCount = buf.readVarInt();
        SyncPacketPayloadLimits.requireValidChunkMetadata(chunkIndex, chunkCount);
        return new Data(sequence, chunkIndex, chunkCount, SyncPacketPayloadLimits.readNbt(buf));
    }

    public void encode(FriendlyByteBuf buf) {
        SyncPacketPayloadLimits.requireValidChunkMetadata(chunkIndex, chunkCount);
        buf.writeLong(sequence);
        buf.writeVarInt(chunkIndex);
        buf.writeVarInt(chunkCount);
        buf.writeNbt(payload);
    }

    public abstract void handle(ModPacketContext context);

    protected record Data(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
    }
}
