package com.abo47.questsandstuff.client.sync.packet;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;

import com.abo47.questsandstuff.network.quest.sync.SyncPacketPayloadLimits;

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
        acceptChunk(PENDING_FULL, sequence, lastFullSequence, chunkIndex, chunkCount, payload,
                ClientSyncPayloadApplier::applyFullSync, ClientSyncChunkAccumulator::joinFullPayload,
                () -> PENDING_DELTA.entrySet().removeIf(entry -> entry.getKey() < sequence),
                seq -> lastFullSequence = seq);
    }

    public static void acceptDeltaChunk(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        acceptChunk(PENDING_DELTA, sequence, lastFullSequence, chunkIndex, chunkCount, payload,
                ClientSyncPayloadApplier::applyDeltaSync, ClientSyncChunkAccumulator::joinDeltaPayload,
                null, seq -> lastDeltaSequence = seq);
    }

    public static void acceptDescriptionChunk(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        acceptChunk(PENDING_DESCRIPTION, sequence, lastFullSequence, chunkIndex, chunkCount, payload,
                ClientSyncPayloadApplier::applyDescriptionSync, ClientSyncChunkAccumulator::joinDescriptionPayload,
                null, seq -> lastDescriptionSequence = seq);
    }

    private static void acceptChunk(
            Map<Long, ClientSyncChunkAccumulator> pending,
            long sequence,
            long subordinateSequence,
            int chunkIndex,
            int chunkCount,
            CompoundTag payload,
            java.util.function.Consumer<CompoundTag> applier,
            java.util.function.Function<ClientSyncChunkAccumulator, CompoundTag> joiner,
            Runnable extraCleanup,
            java.util.function.LongConsumer sequenceSetter
    ) {
        if (sequence < subordinateSequence || !SyncPacketPayloadLimits.isValidChunkMetadata(chunkIndex, chunkCount)) {
            return;
        }
        ClientSyncChunkAccumulator accumulator = accumulatorFor(pending, sequence, chunkCount);
        if (accumulator == null) {
            return;
        }
        accumulator.add(chunkIndex, payload);
        if (accumulator.complete()) {
            applier.accept(joiner.apply(accumulator));
            sequenceSetter.accept(sequence);
            pending.remove(sequence);
            if (extraCleanup != null) {
                extraCleanup.run();
            }
        }
    }

    public static boolean acceptPinnedSequence(long sequence) {
        return acceptSequence(sequence, lastPinnedSequence, seq -> lastPinnedSequence = seq);
    }

    public static boolean acceptDisplayCacheSequence(long sequence) {
        return acceptSequence(sequence, lastCacheSequence, seq -> lastCacheSequence = seq);
    }

    public static boolean acceptEventSequence(long sequence) {
        return acceptSequence(sequence, lastEventSequence, seq -> lastEventSequence = seq);
    }

    public static boolean acceptEditorMutationSequence(long sequence) {
        return acceptSequence(sequence, lastEditorMutationSequence, seq -> lastEditorMutationSequence = seq);
    }

    private static boolean acceptSequence(long sequence, long lastSequence, java.util.function.LongConsumer setter) {
        if (sequence < lastSequence) {
            return false;
        }
        setter.accept(sequence);
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
