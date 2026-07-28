package com.abo47.questsandstuff.client.tablet.assets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.abo47.questsandstuff.QuestsAndStuffMod;

final class AssetDiagnostics {
    private static final Set<String> SEEN_ONCE_KEYS = ConcurrentHashMap.newKeySet();
    private static volatile Consumer<Event> listener;

    private AssetDiagnostics() {
    }

    static void debug(String event, String message, Object... args) {
        QuestsAndStuffMod.debugLog(message, args);
        Consumer<Event> current = listener;
        if (current != null) {
            current.accept(new Event(event, message, immutableArguments(args)));
        }
    }

    static void debugOnce(String event, String key, String message, Object... args) {
        if (SEEN_ONCE_KEYS.add(event + ":" + key)) {
            debug(event, message, args);
        }
    }

    static AutoCloseable capture(Consumer<Event> nextListener) {
        Consumer<Event> previous = listener;
        listener = nextListener;
        SEEN_ONCE_KEYS.clear();
        return () -> {
            listener = previous;
            SEEN_ONCE_KEYS.clear();
        };
    }

    record Event(String event, String message, List<Object> arguments) {
    }

    private static List<Object> immutableArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return List.of();
        }
        return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(args)));
    }
}
