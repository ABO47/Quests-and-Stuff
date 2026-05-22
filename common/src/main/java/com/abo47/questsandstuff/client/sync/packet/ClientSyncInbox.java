package com.abo47.questsandstuff.client.sync.packet;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public final class ClientSyncInbox {
    private static final Map<Long, ClientSyncChunkAccumulator> PENDING_FULL = new HashMap<>();
    private static final Map<Long, ClientSyncChunkAccumulator> PENDING_DELTA = new HashMap<>();
    private static final Map<Long, ClientSyncChunkAccumulator> PENDING_DESCRIPTION = new HashMap<>();
    private static long lastFullSequence = 0L;
    private static long lastDeltaSequence = 0L;
    private static long lastPinnedSequence = 0L;
    private static long lastDescriptionSequence = 0L;
    private static long lastCacheSequence = 0L;
    private static long lastEventSequence = 0L;
    private static long lastEditorMutationSequence = 0L;

    private ClientSyncInbox() {
    }

    public static void reset() {
        PENDING_FULL.clear();
        PENDING_DELTA.clear();
        PENDING_DESCRIPTION.clear();
        lastFullSequence = 0L;
        lastDeltaSequence = 0L;
        lastPinnedSequence = 0L;
        lastDescriptionSequence = 0L;
        lastCacheSequence = 0L;
        lastEventSequence = 0L;
        lastEditorMutationSequence = 0L;
    }

    public static void acceptFullChunk(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        if (sequence < lastFullSequence) {
            return;
        }
        ClientSyncChunkAccumulator accumulator = PENDING_FULL.get(sequence);
        if (accumulator == null) {
            PENDING_FULL.entrySet().removeIf(entry -> entry.getKey() < sequence);
            accumulator = new ClientSyncChunkAccumulator(chunkCount);
            PENDING_FULL.put(sequence, accumulator);
        }
        accumulator.add(chunkIndex, payload);
        if (accumulator.complete()) {
            ClientSyncPayloadApplier.applyFullSync(accumulator.joinFullPayload());
            lastFullSequence = sequence;
            PENDING_FULL.remove(sequence);
            PENDING_DELTA.entrySet().removeIf(entry -> entry.getKey() < sequence);
        }
    }

    public static void acceptDeltaChunk(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        if (sequence < lastDeltaSequence || sequence < lastFullSequence) {
            return;
        }
        ClientSyncChunkAccumulator accumulator = PENDING_DELTA.get(sequence);
        if (accumulator == null) {
            PENDING_DELTA.entrySet().removeIf(entry -> entry.getKey() < sequence);
            accumulator = new ClientSyncChunkAccumulator(chunkCount);
            PENDING_DELTA.put(sequence, accumulator);
        }
        accumulator.add(chunkIndex, payload);
        if (accumulator.complete()) {
            ClientSyncPayloadApplier.applyDeltaSync(accumulator.joinDeltaPayload());
            lastDeltaSequence = sequence;
            PENDING_DELTA.remove(sequence);
        }
    }

    public static void acceptDescriptionChunk(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        if (sequence < lastDescriptionSequence || sequence < lastFullSequence) {
            return;
        }
        ClientSyncChunkAccumulator accumulator = PENDING_DESCRIPTION.get(sequence);
        if (accumulator == null) {
            PENDING_DESCRIPTION.entrySet().removeIf(entry -> entry.getKey() < sequence);
            accumulator = new ClientSyncChunkAccumulator(chunkCount);
            PENDING_DESCRIPTION.put(sequence, accumulator);
        }
        accumulator.add(chunkIndex, payload);
        if (accumulator.complete()) {
            ClientSyncPayloadApplier.applyDescriptionSync(accumulator.joinDescriptionPayload());
            lastDescriptionSequence = sequence;
            PENDING_DESCRIPTION.remove(sequence);
        }
    }

    public static boolean acceptPinnedSequence(long sequence) {
        if (sequence < lastPinnedSequence) {
            return false;
        }
        lastPinnedSequence = sequence;
        return true;
    }

    public static boolean acceptDisplayCacheSequence(long sequence) {
        if (sequence < lastCacheSequence) {
            return false;
        }
        lastCacheSequence = sequence;
        return true;
    }

    public static boolean acceptEventSequence(long sequence) {
        if (sequence < lastEventSequence) {
            return false;
        }
        lastEventSequence = sequence;
        return true;
    }

    public static boolean acceptEditorMutationSequence(long sequence) {
        if (sequence < lastEditorMutationSequence) {
            return false;
        }
        lastEditorMutationSequence = sequence;
        return true;
    }
}
