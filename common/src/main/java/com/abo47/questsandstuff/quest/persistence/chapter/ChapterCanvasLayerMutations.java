package com.abo47.questsandstuff.quest.persistence.chapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

final class ChapterCanvasLayerMutations {
    private ChapterCanvasLayerMutations() {
    }

    static <T> boolean put(ChapterMetadataState state, String group, T layer, String id, String orderKey, Map<String, List<T>> target, Function<T, String> idExtractor) {
        String normalized = state.ensureGroup(group);
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

    static <T> boolean remove(ChapterMetadataState state, String group, String id, String orderKey, Map<String, List<T>> target, Function<T, String> idExtractor) {
        String normalized = ChapterMetadataState.normalizeGroupName(group);
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

    static void setOrder(ChapterMetadataState state, String group, List<String> order) {
        List<String> sanitized = new ArrayList<>();
        if (order != null) {
            for (String key : order) {
                if (key != null && !key.isBlank() && !sanitized.contains(key)) {
                    sanitized.add(key);
                }
            }
        }
        ChapterMetadataState.putOrRemove(state.canvasLayerOrderByGroup, group, sanitized);
    }

    private static void ensureOrderEntry(ChapterMetadataState state, String group, String key) {
        if (group == null || group.isBlank() || key == null || key.isBlank()) {
            return;
        }
        List<String> order = new ArrayList<>(state.canvasLayerOrderByGroup.getOrDefault(group, List.of()));
        if (!order.contains(key)) {
            order.add(key);
            state.canvasLayerOrderByGroup.put(group, List.copyOf(order));
        }
    }

    private static void removeOrderEntry(ChapterMetadataState state, String group, String key) {
        if (group == null || group.isBlank() || key == null || key.isBlank()) {
            return;
        }
        List<String> order = new ArrayList<>(state.canvasLayerOrderByGroup.getOrDefault(group, List.of()));
        if (order.remove(key)) {
            ChapterMetadataState.putOrRemove(state.canvasLayerOrderByGroup, group, order);
        }
    }
}
