package com.abo47.questsandstuff.client.sync.cache;

import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbt;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClientCanvasLayerState {
    public static final Map<String, List<CanvasImageLayer>> GROUP_CANVAS_IMAGES = new HashMap<>();
    public static final Map<String, List<CanvasTextLayer>> GROUP_CANVAS_TEXTS = new HashMap<>();
    public static final Map<String, List<String>> GROUP_CANVAS_LAYER_ORDER = new HashMap<>();

    private ClientCanvasLayerState() {
    }

    public static void reset() {
        GROUP_CANVAS_IMAGES.clear();
        GROUP_CANVAS_TEXTS.clear();
        GROUP_CANVAS_LAYER_ORDER.clear();
    }

    public static void loadFromFullPayload(CompoundTag payload) {
        reset();
        CompoundTag groupProps = payload.getCompound("group_props");
        for (String group : groupProps.getAllKeys()) {
            CompoundTag props = groupProps.getCompound(group);
            GROUP_CANVAS_IMAGES.put(group, List.copyOf(CanvasLayerNbt.imagesFromListTag(props.getList("canvas_images", Tag.TAG_COMPOUND))));
            GROUP_CANVAS_TEXTS.put(group, List.copyOf(CanvasLayerNbt.textsFromListTag(props.getList("canvas_texts", Tag.TAG_COMPOUND))));
            GROUP_CANVAS_LAYER_ORDER.put(group, List.copyOf(CanvasLayerNbt.stringsFromListTag(props.getList("canvas_layer_order", Tag.TAG_STRING))));
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

    public static <T> void putOrRemove(Map<String, List<T>> target, String group, List<T> values) {
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
