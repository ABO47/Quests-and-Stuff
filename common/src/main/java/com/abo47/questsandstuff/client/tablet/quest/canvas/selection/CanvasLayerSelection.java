package com.abo47.questsandstuff.client.tablet.quest.canvas.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerKey;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerKind;

public record CanvasLayerSelection(Set<CanvasLayerKey> keys) {
    public CanvasLayerSelection {
        if (keys == null || keys.isEmpty()) {
            keys = Set.of();
        } else {
            LinkedHashSet<CanvasLayerKey> normalized = new LinkedHashSet<>();
            for (CanvasLayerKey key : keys) {
                if (key != null && key.selectable()) {
                    normalized.add(key);
                }
            }
            keys = Collections.unmodifiableSet(normalized);
        }
    }

    public static CanvasLayerSelection fromIds(Collection<String> questIds, Collection<String> imageIds, Collection<String> textIds) {
        return fromIds(questIds, imageIds, textIds, List.of());
    }

    public static CanvasLayerSelection fromIds(Collection<String> questIds, Collection<String> imageIds, Collection<String> textIds, Collection<String> ecIds) {
        LinkedHashSet<CanvasLayerKey> keys = new LinkedHashSet<>();
        addAll(keys, CanvasLayerKind.QUEST, questIds);
        addAll(keys, CanvasLayerKind.IMAGE, imageIds);
        addAll(keys, CanvasLayerKind.TEXT, textIds);
        addAll(keys, CanvasLayerKind.EXCLUSIVE_CHOICE, ecIds);
        return new CanvasLayerSelection(keys);
    }

    public int size() {
        return keys.size();
    }

    public boolean isEmpty() {
        return keys.isEmpty();
    }

    public Set<String> ids(CanvasLayerKind kind) {
        if (keys.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        for (CanvasLayerKey key : keys) {
            if (key.kind() == kind) {
                ids.add(key.id());
            }
        }
        return Collections.unmodifiableSet(ids);
    }

    public List<String> orderKeys() {
        if (keys.isEmpty()) {
            return List.of();
        }
        List<String> orderKeys = new ArrayList<>();
        for (CanvasLayerKey key : keys) {
            orderKeys.add(key.orderKey());
        }
        return List.copyOf(orderKeys);
    }

    public List<String> selectedOrderKeys(List<String> orderedLayerKeys) {
        if (keys.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> selected = new LinkedHashSet<>(orderKeys());
        List<String> ordered = new ArrayList<>();
        if (orderedLayerKeys != null) {
            for (String orderKey : orderedLayerKeys) {
                if (selected.remove(orderKey)) {
                    ordered.add(orderKey);
                }
            }
        }
        ordered.addAll(selected);
        return List.copyOf(ordered);
    }

    private static void addAll(Set<CanvasLayerKey> keys, CanvasLayerKind kind, Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (String id : ids) {
            CanvasLayerKey key = switch (kind) {
                case QUEST -> CanvasLayerKey.quest(id);
                case IMAGE -> CanvasLayerKey.image(id);
                case TEXT -> CanvasLayerKey.text(id);
                case EXCLUSIVE_CHOICE -> CanvasLayerKey.exclusiveChoice(id);
                case CONNECTION -> CanvasLayerKey.connection(id);
            };
            if (key.selectable()) {
                keys.add(key);
            }
        }
    }
}
