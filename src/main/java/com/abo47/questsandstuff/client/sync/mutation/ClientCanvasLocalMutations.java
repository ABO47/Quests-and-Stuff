package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.client.sync.cache.ClientCanvasLayerState;

import com.abo47.questsandstuff.client.sync.cache.ClientChapterState;

import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.ArrayList;
import java.util.List;

public final class ClientCanvasLocalMutations {
    private ClientCanvasLocalMutations() {
    }

    public static void putCanvasImageLocal(String group, CanvasImageLayer image) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || image == null || image.id().isBlank()) {
            return;
        }
        List<CanvasImageLayer> images = new ArrayList<>(ClientCanvasLayerState.GROUP_CANVAS_IMAGES.getOrDefault(normalized, List.of()));
        boolean replaced = false;
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).id().equals(image.id())) {
                images.set(i, image);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            images.add(image);
        }
        ClientCanvasLayerState.GROUP_CANVAS_IMAGES.put(normalized, List.copyOf(images));
        ensureCanvasLayerOrderLocal(normalized, "image:" + image.id());
    }

    public static void removeCanvasImageLocal(String group, String imageId) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || imageId == null || imageId.isBlank()) {
            return;
        }
        List<CanvasImageLayer> images = new ArrayList<>(ClientCanvasLayerState.GROUP_CANVAS_IMAGES.getOrDefault(normalized, List.of()));
        if (images.removeIf(image -> image.id().equals(imageId))) {
            ClientCanvasLayerState.putOrRemove(ClientCanvasLayerState.GROUP_CANVAS_IMAGES, normalized, images);
            removeCanvasLayerOrderLocal(normalized, "image:" + imageId);
        }
    }

    public static void putCanvasTextLocal(String group, CanvasTextLayer text) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || text == null || text.id().isBlank()) {
            return;
        }
        List<CanvasTextLayer> texts = new ArrayList<>(ClientCanvasLayerState.GROUP_CANVAS_TEXTS.getOrDefault(normalized, List.of()));
        boolean replaced = false;
        for (int i = 0; i < texts.size(); i++) {
            if (texts.get(i).id().equals(text.id())) {
                texts.set(i, text);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            texts.add(text);
        }
        ClientCanvasLayerState.GROUP_CANVAS_TEXTS.put(normalized, List.copyOf(texts));
        ensureCanvasLayerOrderLocal(normalized, "text:" + text.id());
    }

    public static void removeCanvasTextLocal(String group, String textId) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || textId == null || textId.isBlank()) {
            return;
        }
        List<CanvasTextLayer> texts = new ArrayList<>(ClientCanvasLayerState.GROUP_CANVAS_TEXTS.getOrDefault(normalized, List.of()));
        if (texts.removeIf(text -> text.id().equals(textId))) {
            ClientCanvasLayerState.putOrRemove(ClientCanvasLayerState.GROUP_CANVAS_TEXTS, normalized, texts);
            removeCanvasLayerOrderLocal(normalized, "text:" + textId);
        }
    }

    public static void setCanvasLayerOrderLocal(String group, List<String> order) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank()) {
            return;
        }
        List<String> sanitized = new ArrayList<>();
        if (order != null) {
            for (String key : order) {
                if (key != null && !key.isBlank() && !sanitized.contains(key)) {
                    sanitized.add(key);
                }
            }
        }
        ClientCanvasLayerState.putOrRemove(ClientCanvasLayerState.GROUP_CANVAS_LAYER_ORDER, normalized, sanitized);
    }

    private static void ensureCanvasLayerOrderLocal(String group, String key) {
        List<String> order = new ArrayList<>(ClientCanvasLayerState.GROUP_CANVAS_LAYER_ORDER.getOrDefault(group, List.of()));
        if (!order.contains(key)) {
            order.add(key);
            ClientCanvasLayerState.GROUP_CANVAS_LAYER_ORDER.put(group, List.copyOf(order));
        }
    }

    private static void removeCanvasLayerOrderLocal(String group, String key) {
        List<String> order = new ArrayList<>(ClientCanvasLayerState.GROUP_CANVAS_LAYER_ORDER.getOrDefault(group, List.of()));
        if (order.remove(key)) {
            ClientCanvasLayerState.putOrRemove(ClientCanvasLayerState.GROUP_CANVAS_LAYER_ORDER, group, order);
        }
    }
}
