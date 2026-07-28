package com.abo47.questsandstuff.quest.persistence.chapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

final class ChapterCanvasLayerMutations {
    private ChapterCanvasLayerMutations() {
    }

    static <T> boolean put(ChapterMetadataState state, String chapter, T layer, String id, String orderKey, Map<String, List<T>> target, Function<T, String> idExtractor) {
        String normalized = state.ensureChapter(chapter);
        if (normalized.isBlank() || id == null || id.isBlank()) {
            return false;
        }
        List<T> values = new ArrayList<>(target.getOrDefault(normalized, List.of()));
        boolean replaced = false;
        for (int i = 0; i < values.size(); i++) {
            if (id.equals(idExtractor.apply(values.get(i)))) {
                values.set(i, layer);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            values.add(layer);
        }
        target.put(normalized, List.copyOf(values));
        ensureOrderEntry(state, normalized, orderKey);
        return true;
    }

    static <T> boolean remove(ChapterMetadataState state, String chapter, String id, String orderKey, Map<String, List<T>> target, Function<T, String> idExtractor) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (normalized.isBlank() || id == null || id.isBlank()) {
            return false;
        }
        List<T> values = new ArrayList<>(target.getOrDefault(normalized, List.of()));
        boolean removed = values.removeIf(value -> id.equals(idExtractor.apply(value)));
        if (!removed) {
            return false;
        }
        ChapterMetadataState.putOrRemove(target, normalized, values);
        removeOrderEntry(state, normalized, orderKey);
        return true;
    }

    static void setOrder(ChapterMetadataState state, String chapter, List<String> order) {
        List<String> sanitized = new ArrayList<>();
        if (order != null) {
            for (String key : order) {
                if (key != null && !key.isBlank() && !sanitized.contains(key)) {
                    sanitized.add(key);
                }
            }
        }
        ChapterMetadataState.putOrRemove(state.canvasLayerOrderByChapter, chapter, sanitized);
    }

    private static void ensureOrderEntry(ChapterMetadataState state, String chapter, String key) {
        if (chapter == null || chapter.isBlank() || key == null || key.isBlank()) {
            return;
        }
        List<String> order = new ArrayList<>(state.canvasLayerOrderByChapter.getOrDefault(chapter, List.of()));
        if (!order.contains(key)) {
            order.add(key);
            state.canvasLayerOrderByChapter.put(chapter, List.copyOf(order));
        }
    }

    private static void removeOrderEntry(ChapterMetadataState state, String chapter, String key) {
        if (chapter == null || chapter.isBlank() || key == null || key.isBlank()) {
            return;
        }
        List<String> order = new ArrayList<>(state.canvasLayerOrderByChapter.getOrDefault(chapter, List.of()));
        if (order.remove(key)) {
            ChapterMetadataState.putOrRemove(state.canvasLayerOrderByChapter, chapter, order);
        }
    }
}
