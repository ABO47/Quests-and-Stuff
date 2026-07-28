package com.abo47.questsandstuff.quest.sync;

import java.util.concurrent.atomic.AtomicLong;

import net.minecraft.nbt.CompoundTag;

public final class PerformanceTracker {
    private final AtomicLong signalCount = new AtomicLong();
    private final AtomicLong signalNanos = new AtomicLong();
    private final AtomicLong bindingsVisited = new AtomicLong();
    private final AtomicLong questUpdates = new AtomicLong();

    private final AtomicLong fullSyncPackets = new AtomicLong();
    private final AtomicLong fullSyncChunks = new AtomicLong();
    private final AtomicLong fullSyncBytes = new AtomicLong();

    private final AtomicLong deltaSyncPackets = new AtomicLong();
    private final AtomicLong deltaSyncChunks = new AtomicLong();
    private final AtomicLong deltaSyncBytes = new AtomicLong();

    public void recordSignal(long nanos, int visitedBindings, int changedQuests) {
        signalCount.incrementAndGet();
        signalNanos.addAndGet(nanos);
        bindingsVisited.addAndGet(visitedBindings);
        questUpdates.addAndGet(changedQuests);
    }

    public void recordFullSync(int packets, int chunks, long bytes) {
        fullSyncPackets.addAndGet(packets);
        fullSyncChunks.addAndGet(chunks);
        fullSyncBytes.addAndGet(bytes);
    }

    public void recordDeltaSync(int packets, int chunks, long bytes) {
        deltaSyncPackets.addAndGet(packets);
        deltaSyncChunks.addAndGet(chunks);
        deltaSyncBytes.addAndGet(bytes);
    }

    public void reset() {
        signalCount.set(0);
        signalNanos.set(0);
        bindingsVisited.set(0);
        questUpdates.set(0);
        fullSyncPackets.set(0);
        fullSyncChunks.set(0);
        fullSyncBytes.set(0);
        deltaSyncPackets.set(0);
        deltaSyncChunks.set(0);
        deltaSyncBytes.set(0);
    }

    public CompoundTag snapshotTag() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("signals", signalCount.get());
        tag.putLong("signal_nanos", signalNanos.get());
        tag.putLong("bindings", bindingsVisited.get());
        tag.putLong("quest_updates", questUpdates.get());

        tag.putLong("full_packets", fullSyncPackets.get());
        tag.putLong("full_chunks", fullSyncChunks.get());
        tag.putLong("full_bytes", fullSyncBytes.get());

        tag.putLong("delta_packets", deltaSyncPackets.get());
        tag.putLong("delta_chunks", deltaSyncChunks.get());
        tag.putLong("delta_bytes", deltaSyncBytes.get());
        return tag;
    }

    public String summaryLine() {
        long count = Math.max(1L, signalCount.get());
        long avgMicros = (signalNanos.get() / count) / 1000L;
        return "sig=" + signalCount.get()
                + " avg=" + avgMicros + "us"
                + " bind=" + bindingsVisited.get()
                + " upd=" + questUpdates.get();
    }

    public String syncLine() {
        return "full:" + fullSyncPackets.get() + "p/" + fullSyncChunks.get() + "c " + humanBytes(fullSyncBytes.get())
                + " | delta:" + deltaSyncPackets.get() + "p/" + deltaSyncChunks.get() + "c " + humanBytes(deltaSyncBytes.get());
    }

    private static String humanBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        }
        long kb = bytes / 1024;
        if (kb < 1024) {
            return kb + "KB";
        }
        return (kb / 1024) + "MB";
    }
}
