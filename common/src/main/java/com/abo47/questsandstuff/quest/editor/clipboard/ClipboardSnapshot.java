package com.abo47.questsandstuff.quest.editor.clipboard;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.abo47.questsandstuff.quest.model.QuestDefinition;

public record ClipboardSnapshot(List<Entry> entries) {
    public ClipboardSnapshot {
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public static ClipboardSnapshot empty() {
        return new ClipboardSnapshot(List.of());
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public List<Entry> sortedEntries() {
        return entries.stream()
                .sorted(Comparator
                        .comparingInt(Entry::sourceY)
                        .thenComparingInt(Entry::sourceX)
                        .thenComparing(Entry::sourceId))
                .toList();
    }

    public Set<String> sourceIds() {
        Set<String> ids = new LinkedHashSet<>();
        for (Entry entry : entries) {
            if (entry != null && !entry.sourceId().isBlank()) {
                ids.add(entry.sourceId());
            }
        }
        return Set.copyOf(ids);
    }

    public int minSourceX() {
        return entries.stream().mapToInt(Entry::sourceX).min().orElse(0);
    }

    public int minSourceY() {
        return entries.stream().mapToInt(Entry::sourceY).min().orElse(0);
    }

    public int countExternalPrerequisiteConnections() {
        Set<String> copied = sourceIds();
        int dropped = 0;
        for (Entry entry : entries) {
            if (entry == null || entry.definition() == null || entry.definition().prerequisites() == null) {
                continue;
            }
            for (String prerequisite : entry.definition().prerequisites()) {
                if (prerequisite != null && !prerequisite.isBlank() && !copied.contains(prerequisite)) {
                    dropped++;
                }
            }
        }
        return dropped;
    }

    public record Entry(
            String sourceId,
            String sourceChapter,
            int sourceX,
            int sourceY,
            float scale,
            QuestDefinition definition
    ) {
        public Entry {
            sourceId = sourceId == null ? "" : sourceId.trim();
            sourceChapter = sourceChapter == null ? "" : sourceChapter.trim();
            if (Float.isNaN(scale) || Float.isInfinite(scale)) {
                scale = 1.0f;
            }
            scale = Math.max(0.5f, scale);
        }
    }
}
