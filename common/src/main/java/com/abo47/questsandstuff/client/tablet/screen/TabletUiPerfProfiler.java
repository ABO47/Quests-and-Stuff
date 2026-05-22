package com.abo47.questsandstuff.client.tablet.screen;


import com.abo47.questsandstuff.QuestsAndStuffMod;

import java.util.function.Supplier;

public final class TabletUiPerfProfiler {
    private static final long WARN_THRESHOLD_NS = 3_000_000L;

    private TabletUiPerfProfiler() {
    }

    public static void profile(String scope, Runnable runnable) {
        long start = System.nanoTime();
        runnable.run();
        log(scope, System.nanoTime() - start);
    }

    public static <T> T profile(String scope, Supplier<T> supplier) {
        long start = System.nanoTime();
        T value = supplier.get();
        log(scope, System.nanoTime() - start);
        return value;
    }

    private static void log(String scope, long elapsedNs) {
        if (elapsedNs < WARN_THRESHOLD_NS) {
            return;
        }
        double ms = elapsedNs / 1_000_000.0D;
        QuestsAndStuffMod.debugLog("[QnS:UI:Perf] {} took {}ms", scope, String.format(java.util.Locale.ROOT, "%.3f", ms));
    }
}
