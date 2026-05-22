package com.abo47.questsandstuff.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class StableIdAllocator {
    private StableIdAllocator() {
    }

    public static String nextId(String prefix, Collection<String> existingIds) {
        String clean = cleanPrefix(prefix);
        Set<String> reserved = new HashSet<>();
        if (existingIds != null) {
            for (String existing : existingIds) {
                if (existing != null && !existing.isBlank()) {
                    reserved.add(existing);
                }
            }
        }
        for (int i = 1; i < 100_000; i++) {
            String id = clean + "_" + String.format(Locale.ROOT, "%04d", i);
            if (reserved.add(id)) {
                return id;
            }
        }
        int suffix = 100_000;
        String id;
        do {
            id = clean + "_" + suffix++;
        } while (!reserved.add(id));
        return id;
    }

    private static String cleanPrefix(String prefix) {
        return SafeNames.identifier(prefix, "id");
    }
}
