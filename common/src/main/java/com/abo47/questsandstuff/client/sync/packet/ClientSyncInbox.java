package com.abo47.questsandstuff.client.sync.packet;

import com.abo47.questsandstuff.network.quest.sync.SyncPacketPayloadLimits;

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
        if (sequence < lastFullSequence || !SyncPacketPayloadLimits.isValidChunkMetadata(chunkIndex, chunkCount)) {
            return;
        }
        ClientSyncChunkAccumulator accumulator = accumulatorFor(PENDING_FULL, sequence, chunkCount);
        if (accumulator == null) {
            return;
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
        if (sequence < lastDeltaSequence || sequence < lastFullSequence || !SyncPacketPayloadLimits.isValidChunkMetadata(chunkIndex, chunkCount)) {
            return;
        }
        ClientSyncChunkAccumulator accumulator = accumulatorFor(PENDING_DELTA, sequence, chunkCount);
        if (accumulator == null) {
            return;
        }
        accumulator.add(chunkIndex, payload);
        if (accumulator.complete()) {
            ClientSyncPayloadApplier.applyDeltaSync(accumulator.joinDeltaPayload());
            lastDeltaSequence = sequence;
            PENDING_DELTA.remove(sequence);
        }
    }

    public static void acceptDescriptionChunk(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        if (sequence < lastDescriptionSequence || sequence < lastFullSequence || !SyncPacketPayloadLimits.isValidChunkMetadata(chunkIndex, chunkCount)) {
            return;
        }
        ClientSyncChunkAccumulator accumulator = accumulatorFor(PENDING_DESCRIPTION, sequence, chunkCount);
        if (accumulator == null) {
            return;
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

    private static ClientSyncChunkAccumulator accumulatorFor(Map<Long, ClientSyncChunkAccumulator> pending, long sequence, int chunkCount) {
        Long newestPendingSequence = newestSequence(pending);
        if (newestPendingSequence != null && sequence < newestPendingSequence) {
            return null;
        }

        ClientSyncChunkAccumulator accumulator = pending.get(sequence);
        if (accumulator != null) {
            return accumulator.expected() == chunkCount ? accumulator : null;
        }

        pending.entrySet().removeIf(entry -> entry.getKey() < sequence);
        trimPendingSequences(pending);

        accumulator = new ClientSyncChunkAccumulator(chunkCount);
        pending.put(sequence, accumulator);
        return accumulator;
    }

    private static void trimPendingSequences(Map<Long, ClientSyncChunkAccumulator> pending) {
        while (pending.size() >= SyncPacketPayloadLimits.MAX_PENDING_SYNC_SEQUENCES) {
            Long oldestSequence = oldestSequence(pending);
            if (oldestSequence == null) {
                return;
            }
            pending.remove(oldestSequence);
        }
    }

    private static Long newestSequence(Map<Long, ClientSyncChunkAccumulator> pending) {
        Long newestSequence = null;
        for (Long sequence : pending.keySet()) {
            if (newestSequence == null || sequence > newestSequence) {
                newestSequence = sequence;
            }
        }
        return newestSequence;
    }

    private static Long oldestSequence(Map<Long, ClientSyncChunkAccumulator> pending) {
        Long oldestSequence = null;
        for (Long sequence : pending.keySet()) {
            if (oldestSequence == null || sequence < oldestSequence) {
                oldestSequence = sequence;
            }
        }
        return oldestSequence;
    }
}
