package com.abo47.questsandstuff.client.tablet.quest.editor;


import java.util.HashMap;
import java.util.Map;

import com.abo47.questsandstuff.QuestsAndStuffMod;

public final class EditorPreviewDeduplicator {
    private static final Map<String, Long> LAST_MUTATION_NS = new HashMap<>();
    private static final long DEDUPE_NS = 20_000_000L;

    private EditorPreviewDeduplicator() {
    }

    public static void dispatch(String mutationKey, Runnable optimisticApply, Runnable sendToServer) {
        long now = System.nanoTime();
        Long previous = LAST_MUTATION_NS.get(mutationKey);
        if (previous != null && now - previous < DEDUPE_NS) {
            QuestsAndStuffMod.debugLog("[QnS:UI:Bus] dedupe skip key={}", mutationKey);
            return;
        }
        LAST_MUTATION_NS.put(mutationKey, now);
        try {
            optimisticApply.run();
            sendToServer.run();
            QuestsAndStuffMod.debugLog("[QnS:UI:Bus] dispatch key={}", mutationKey);
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.error("[QnS:UI:Bus] dispatch failed key={}", mutationKey, e);
        }
    }

    public static void clear() {
        LAST_MUTATION_NS.clear();
    }
}
