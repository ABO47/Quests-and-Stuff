package com.abo47.questsandstuff.client.sync.cache;

import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbt;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClientCanvasLayerState {
    private static final Map<String, List<CanvasImageLayer>> GROUP_CANVAS_IMAGES = new HashMap<>();
    private static final Map<String, List<CanvasTextLayer>> GROUP_CANVAS_TEXTS = new HashMap<>();
    private static final Map<String, List<String>> GROUP_CANVAS_LAYER_ORDER = new HashMap<>();

    private ClientCanvasLayerState() {
    }

    public static void reset() {
        GROUP_CANVAS_IMAGES.clear();
        GROUP_CANVAS_TEXTS.clear();
        GROUP_CANVAS_LAYER_ORDER.clear();
    }

    public static void loadFromFullPayload(CompoundTag payload) {
        reset();
        mergeFromDeltaPayload(payload);
    }

    public static void mergeFromDeltaPayload(CompoundTag payload) {
        if (payload == null) {
            return;
        }
        if (payload.contains("groups", Tag.TAG_LIST)) {
            List<String> groups = ClientChapterState.groupOrderSnapshot();
            GROUP_CANVAS_IMAGES.keySet().removeIf(group -> !groups.contains(group));
            GROUP_CANVAS_TEXTS.keySet().removeIf(group -> !groups.contains(group));
            GROUP_CANVAS_LAYER_ORDER.keySet().removeIf(group -> !groups.contains(group));
            for (String group : groups) {
                ensureGroup(group);
            }
        }
        CompoundTag groupProps = payload.getCompound("group_props");
        for (String group : groupProps.getAllKeys()) {
            CompoundTag props = groupProps.getCompound(group);
            String normalized = ClientChapterState.normalizeGroup(group);
            if (normalized.isBlank()) {
                continue;
            }
            GROUP_CANVAS_IMAGES.put(normalized, List.copyOf(CanvasLayerNbt.imagesFromListTag(props.getList("canvas_images", Tag.TAG_COMPOUND))));
            GROUP_CANVAS_TEXTS.put(normalized, List.copyOf(CanvasLayerNbt.textsFromListTag(props.getList("canvas_texts", Tag.TAG_COMPOUND))));
            GROUP_CANVAS_LAYER_ORDER.put(normalized, List.copyOf(CanvasLayerNbt.stringsFromListTag(props.getList("canvas_layer_order", Tag.TAG_STRING))));
        }
    }

    public static Map<String, List<CanvasImageLayer>> imagesByGroup() {
        return copyLayerMap(GROUP_CANVAS_IMAGES);
    }

    public static Map<String, List<CanvasTextLayer>> textsByGroup() {
        return copyLayerMap(GROUP_CANVAS_TEXTS);
    }

    public static Map<String, List<String>> layerOrderByGroup() {
        return copyLayerMap(GROUP_CANVAS_LAYER_ORDER);
    }

    public static List<CanvasImageLayer> images(String group) {
        return List.copyOf(GROUP_CANVAS_IMAGES.getOrDefault(ClientChapterState.normalizeGroup(group), List.of()));
    }

    public static List<CanvasTextLayer> texts(String group) {
        return List.copyOf(GROUP_CANVAS_TEXTS.getOrDefault(ClientChapterState.normalizeGroup(group), List.of()));
    }

    public static List<String> layerOrder(String group) {
        return List.copyOf(GROUP_CANVAS_LAYER_ORDER.getOrDefault(ClientChapterState.normalizeGroup(group), List.of()));
    }

    public static void ensureGroup(String group) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank()) {
            return;
        }
        GROUP_CANVAS_IMAGES.putIfAbsent(normalized, List.of());
        GROUP_CANVAS_TEXTS.putIfAbsent(normalized, List.of());
        GROUP_CANVAS_LAYER_ORDER.putIfAbsent(normalized, List.of());
    }

    public static void renameGroup(String from, String to) {
        String source = ClientChapterState.normalizeGroup(from);
        String target = ClientChapterState.normalizeGroup(to);
        if (source.isBlank() || target.isBlank() || source.equals(target)) {
            return;
        }
        GROUP_CANVAS_IMAGES.put(target, GROUP_CANVAS_IMAGES.getOrDefault(source, List.of()));
        GROUP_CANVAS_IMAGES.remove(source);
        GROUP_CANVAS_TEXTS.put(target, GROUP_CANVAS_TEXTS.getOrDefault(source, List.of()));
        GROUP_CANVAS_TEXTS.remove(source);
        GROUP_CANVAS_LAYER_ORDER.put(target, GROUP_CANVAS_LAYER_ORDER.getOrDefault(source, List.of()));
        GROUP_CANVAS_LAYER_ORDER.remove(source);
    }

    public static void removeGroup(String group) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank()) {
            return;
        }
        GROUP_CANVAS_IMAGES.remove(normalized);
        GROUP_CANVAS_TEXTS.remove(normalized);
        GROUP_CANVAS_LAYER_ORDER.remove(normalized);
    }

    public static void putImage(String group, CanvasImageLayer image) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || image == null || image.id().isBlank()) {
            return;
        }
        List<CanvasImageLayer> images = new ArrayList<>(GROUP_CANVAS_IMAGES.getOrDefault(normalized, List.of()));
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
        GROUP_CANVAS_IMAGES.put(normalized, List.copyOf(images));
        ensureLayerOrder(normalized, "image:" + image.id());
    }

    public static boolean removeImage(String group, String imageId) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || imageId == null || imageId.isBlank()) {
            return false;
        }
        List<CanvasImageLayer> images = new ArrayList<>(GROUP_CANVAS_IMAGES.getOrDefault(normalized, List.of()));
        if (!images.removeIf(image -> image.id().equals(imageId))) {
            return false;
        }
        putOrRemove(GROUP_CANVAS_IMAGES, normalized, images);
        removeLayerOrder(normalized, "image:" + imageId);
        return true;
    }

    public static void putText(String group, CanvasTextLayer text) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || text == null || text.id().isBlank()) {
            return;
        }
        List<CanvasTextLayer> texts = new ArrayList<>(GROUP_CANVAS_TEXTS.getOrDefault(normalized, List.of()));
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
        GROUP_CANVAS_TEXTS.put(normalized, List.copyOf(texts));
        ensureLayerOrder(normalized, "text:" + text.id());
    }

    public static boolean removeText(String group, String textId) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || textId == null || textId.isBlank()) {
            return false;
        }
        List<CanvasTextLayer> texts = new ArrayList<>(GROUP_CANVAS_TEXTS.getOrDefault(normalized, List.of()));
        if (!texts.removeIf(text -> text.id().equals(textId))) {
            return false;
        }
        putOrRemove(GROUP_CANVAS_TEXTS, normalized, texts);
        removeLayerOrder(normalized, "text:" + textId);
        return true;
    }

    public static void setLayerOrder(String group, List<String> order) {
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
        putOrRemove(GROUP_CANVAS_LAYER_ORDER, normalized, sanitized);
    }

    private static void ensureLayerOrder(String group, String key) {
        List<String> order = new ArrayList<>(GROUP_CANVAS_LAYER_ORDER.getOrDefault(group, List.of()));
        if (!order.contains(key)) {
            order.add(key);
            GROUP_CANVAS_LAYER_ORDER.put(group, List.copyOf(order));
        }
    }

    private static void removeLayerOrder(String group, String key) {
        List<String> order = new ArrayList<>(GROUP_CANVAS_LAYER_ORDER.getOrDefault(group, List.of()));
        if (order.remove(key)) {
            putOrRemove(GROUP_CANVAS_LAYER_ORDER, group, order);
        }
    }

    private static <T> void putOrRemove(Map<String, List<T>> target, String group, List<T> values) {
        if (values == null || values.isEmpty()) {
            target.remove(group);
        } else {
            target.put(group, List.copyOf(values));
        }
    }

    private static <T> Map<String, List<T>> copyLayerMap(Map<String, List<T>> source) {
        Map<String, List<T>> copy = new HashMap<>();
        for (Map.Entry<String, List<T>> entry : source.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(copy);
    }
}
