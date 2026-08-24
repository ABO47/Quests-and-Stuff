package com.abo47.questsandstuff.quest.model.task;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;

public final class QuestTaskItemLocks {
    public static final String FIELD = "item_locks";
    public static final char TAG_PREFIX = '#';

    private static final Codec<List<String>> NORMALIZED_CODEC = Codec.STRING.listOf()
            .xmap(QuestTaskItemLocks::normalize, locks -> locks);

    private QuestTaskItemLocks() {
    }

    public static Codec<List<String>> codec() {
        return NORMALIZED_CODEC;
    }

    public static List<String> normalize(List<String> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String entry : entries) {
            String candidate = canonical(entry);
            if (!isValid(candidate)) {
                continue;
            }
            normalized.add(candidate);
        }
        return List.copyOf(normalized);
    }

    public static boolean isValid(String entry) {
        return parseId(canonical(entry)) != null;
    }

    public static boolean isTag(String entry) {
        return entry != null && !entry.isBlank() && entry.trim().charAt(0) == TAG_PREFIX;
    }

    public static ResourceLocation id(String entry) {
        return isTag(entry) ? null : parseId(canonical(entry));
    }

    public static ResourceLocation tagId(String entry) {
        return isTag(entry) ? parseId(canonical(entry).substring(1)) : null;
    }

    private static ResourceLocation parseId(String entry) {
        if (entry == null || entry.isBlank()) {
            return null;
        }
        String value = entry.charAt(0) == TAG_PREFIX ? entry.substring(1) : entry;
        if (value.isBlank()) {
            return null;
        }
        ResourceLocation parsed = ResourceLocation.tryParse(value);
        return parsed == null || parsed.getPath().isBlank() ? null : parsed;
    }

    private static String canonical(String entry) {
        if (entry == null) {
            return "";
        }
        String trimmed = entry.trim().toLowerCase();
        return trimmed;
    }

    public static List<String> add(List<String> entries, String entry) {
        String candidate = canonical(entry);
        if (!isValid(candidate)) {
            return normalize(entries);
        }
        List<String> merged = new ArrayList<>(entries == null ? List.of() : entries);
        merged.add(candidate);
        return normalize(merged);
    }

    public static List<String> remove(List<String> entries, String entry) {
        if (entries == null || entries.isEmpty() || entry == null || entry.isBlank()) {
            return normalize(entries);
        }
        String target = entry.trim().toLowerCase();
        List<String> remaining = new ArrayList<>();
        for (String existing : entries) {
            if (!existing.equalsIgnoreCase(target)) {
                remaining.add(existing);
            }
        }
        return normalize(remaining);
    }
}
