package com.abo47.questsandstuff.network.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;

public final class SyncPacketPayloadLimits {
    public static final long MAX_SYNC_NBT_BYTES = 8L * 1024L * 1024L;
    public static final int MAX_SYNC_CHUNKS = 256;
    public static final int MAX_PINNED_QUESTS = 8192;
    public static final int MAX_PENDING_SYNC_SEQUENCES = 8;

    private SyncPacketPayloadLimits() {
    }

    public static CompoundTag readNbt(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt(new NbtAccounter(MAX_SYNC_NBT_BYTES));
        return tag == null ? new CompoundTag() : tag;
    }

    public static int readPinnedQuestCount(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        requireListSize("Pinned quest sync", count, MAX_PINNED_QUESTS);
        return count;
    }

    public static void requirePinnedQuestCount(int count) {
        requireListSize("Pinned quest sync", count, MAX_PINNED_QUESTS);
    }

    public static boolean isValidChunkMetadata(int chunkIndex, int chunkCount) {
        return chunkCount >= 1
                && chunkCount <= MAX_SYNC_CHUNKS
                && chunkIndex >= 0
                && chunkIndex < chunkCount;
    }

    public static void requireValidChunkMetadata(int chunkIndex, int chunkCount) {
        if (!isValidChunkMetadata(chunkIndex, chunkCount)) {
            throw new IllegalArgumentException("Invalid sync chunk metadata: index " + chunkIndex + " of " + chunkCount);
        }
    }

    private static void requireListSize(String label, int count, int max) {
        if (count < 0 || count > max) {
            throw new IllegalArgumentException(label + " count " + count + " exceeds limit " + max);
        }
    }
}
