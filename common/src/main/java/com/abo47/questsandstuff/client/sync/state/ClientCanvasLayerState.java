package com.abo47.questsandstuff.client.sync.state;

import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbtCodec;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.sync.SyncKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClientCanvasLayerState {
    private static final Map<String, List<CanvasExclusiveChoice>> CHAPTER_CANVAS_EXCLUSIVE_CHOICES = new HashMap<>();
    private static final Map<String, List<CanvasImageLayer>> CHAPTER_CANVAS_IMAGES = new HashMap<>();
    private static final Map<String, List<CanvasTextLayer>> CHAPTER_CANVAS_TEXTS = new HashMap<>();
    private static final Map<String, List<String>> CHAPTER_CANVAS_LAYER_ORDER = new HashMap<>();

    private ClientCanvasLayerState() {
    }

    public static void reset() {
        CHAPTER_CANVAS_EXCLUSIVE_CHOICES.clear();
        CHAPTER_CANVAS_IMAGES.clear();
        CHAPTER_CANVAS_TEXTS.clear();
        CHAPTER_CANVAS_LAYER_ORDER.clear();
    }

    public static void loadFromFullPayload(CompoundTag payload) {
        reset();
        mergeFromDeltaPayload(payload);
    }

    public static void mergeFromDeltaPayload(CompoundTag payload) {
        if (payload == null) {
            return;
        }
        if (payload.contains(SyncKeys.CHAPTERS, Tag.TAG_LIST)) {
            List<String> groups = ClientChapterState.chapterOrderSnapshot();
            CHAPTER_CANVAS_EXCLUSIVE_CHOICES.keySet().removeIf(chapter -> !groups.contains(chapter));
            CHAPTER_CANVAS_IMAGES.keySet().removeIf(chapter -> !groups.contains(chapter));
            CHAPTER_CANVAS_TEXTS.keySet().removeIf(chapter -> !groups.contains(chapter));
            CHAPTER_CANVAS_LAYER_ORDER.keySet().removeIf(chapter -> !groups.contains(chapter));
            for (String chapter : groups) {
                ensureChapter(chapter);
            }
        }
        CompoundTag groupProps = payload.getCompound(SyncKeys.CHAPTER_PROPS);
        for (String chapter : groupProps.getAllKeys()) {
            CompoundTag props = groupProps.getCompound(chapter);
            String normalized = ClientChapterState.normalizeChapter(chapter);
            if (normalized.isBlank()) {
                continue;
            }
            CHAPTER_CANVAS_EXCLUSIVE_CHOICES.put(normalized, List.copyOf(CanvasLayerNbtCodec.exclusiveChoicesFromListTag(props.getList(SyncKeys.ChapterProps.CANVAS_EXCLUSIVE_CHOICES, Tag.TAG_COMPOUND))));
            CHAPTER_CANVAS_IMAGES.put(normalized, List.copyOf(CanvasLayerNbtCodec.imagesFromListTag(props.getList(SyncKeys.ChapterProps.CANVAS_IMAGES, Tag.TAG_COMPOUND))));
            CHAPTER_CANVAS_TEXTS.put(normalized, List.copyOf(CanvasLayerNbtCodec.textsFromListTag(props.getList(SyncKeys.ChapterProps.CANVAS_TEXTS, Tag.TAG_COMPOUND))));
            CHAPTER_CANVAS_LAYER_ORDER.put(normalized, List.copyOf(CanvasLayerNbtCodec.stringsFromListTag(props.getList(SyncKeys.ChapterProps.CANVAS_LAYER_ORDER, Tag.TAG_STRING))));
        }
    }

    public static Map<String, List<CanvasExclusiveChoice>> exclusiveChoicesByChapter() {
        return copyLayerMap(CHAPTER_CANVAS_EXCLUSIVE_CHOICES);
    }

    public static Map<String, List<CanvasImageLayer>> imagesByChapter() {
        return copyLayerMap(CHAPTER_CANVAS_IMAGES);
    }

    public static Map<String, List<CanvasTextLayer>> textsByChapter() {
        return copyLayerMap(CHAPTER_CANVAS_TEXTS);
    }

    public static Map<String, List<String>> layerOrderByChapter() {
        return copyLayerMap(CHAPTER_CANVAS_LAYER_ORDER);
    }

    public static List<CanvasExclusiveChoice> exclusiveChoices(String chapter) {
        return List.copyOf(CHAPTER_CANVAS_EXCLUSIVE_CHOICES.getOrDefault(ClientChapterState.normalizeChapter(chapter), List.of()));
    }

    public static List<CanvasImageLayer> images(String chapter) {
        return List.copyOf(CHAPTER_CANVAS_IMAGES.getOrDefault(ClientChapterState.normalizeChapter(chapter), List.of()));
    }

    public static List<CanvasTextLayer> texts(String chapter) {
        return List.copyOf(CHAPTER_CANVAS_TEXTS.getOrDefault(ClientChapterState.normalizeChapter(chapter), List.of()));
    }

    public static List<String> layerOrder(String chapter) {
        return List.copyOf(CHAPTER_CANVAS_LAYER_ORDER.getOrDefault(ClientChapterState.normalizeChapter(chapter), List.of()));
    }

    public static void ensureChapter(String chapter) {
        String normalized = ClientChapterState.normalizeChapter(chapter);
        if (normalized.isBlank()) {
            return;
        }
        CHAPTER_CANVAS_EXCLUSIVE_CHOICES.putIfAbsent(normalized, List.of());
        CHAPTER_CANVAS_IMAGES.putIfAbsent(normalized, List.of());
        CHAPTER_CANVAS_TEXTS.putIfAbsent(normalized, List.of());
        CHAPTER_CANVAS_LAYER_ORDER.putIfAbsent(normalized, List.of());
    }

    public static void renameChapter(String from, String to) {
        String source = ClientChapterState.normalizeChapter(from);
        String target = ClientChapterState.normalizeChapter(to);
        if (source.isBlank() || target.isBlank() || source.equals(target)) {
            return;
        }
        CHAPTER_CANVAS_EXCLUSIVE_CHOICES.put(target, CHAPTER_CANVAS_EXCLUSIVE_CHOICES.getOrDefault(source, List.of()));
        CHAPTER_CANVAS_EXCLUSIVE_CHOICES.remove(source);
        CHAPTER_CANVAS_IMAGES.put(target, CHAPTER_CANVAS_IMAGES.getOrDefault(source, List.of()));
        CHAPTER_CANVAS_IMAGES.remove(source);
        CHAPTER_CANVAS_TEXTS.put(target, CHAPTER_CANVAS_TEXTS.getOrDefault(source, List.of()));
        CHAPTER_CANVAS_TEXTS.remove(source);
        CHAPTER_CANVAS_LAYER_ORDER.put(target, CHAPTER_CANVAS_LAYER_ORDER.getOrDefault(source, List.of()));
        CHAPTER_CANVAS_LAYER_ORDER.remove(source);
    }

    public static void removeChapter(String chapter) {
        String normalized = ClientChapterState.normalizeChapter(chapter);
        if (normalized.isBlank()) {
            return;
        }
        CHAPTER_CANVAS_EXCLUSIVE_CHOICES.remove(normalized);
        CHAPTER_CANVAS_IMAGES.remove(normalized);
        CHAPTER_CANVAS_TEXTS.remove(normalized);
        CHAPTER_CANVAS_LAYER_ORDER.remove(normalized);
    }

    public static void putExclusiveChoice(String chapter, CanvasExclusiveChoice ec) {
        String normalized = ClientChapterState.normalizeChapter(chapter);
        if (normalized.isBlank() || ec == null || ec.id().isBlank()) {
            return;
        }
        List<CanvasExclusiveChoice> choices = new ArrayList<>(CHAPTER_CANVAS_EXCLUSIVE_CHOICES.getOrDefault(normalized, List.of()));
        boolean replaced = false;
        for (int i = 0; i < choices.size(); i++) {
            if (choices.get(i).id().equals(ec.id())) {
                choices.set(i, ec);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            choices.add(ec);
        }
        CHAPTER_CANVAS_EXCLUSIVE_CHOICES.put(normalized, List.copyOf(choices));
        ensureLayerOrder(normalized, "exclusive_choice:" + ec.id());
    }

    public static boolean removeExclusiveChoice(String chapter, String ecId) {
        String normalized = ClientChapterState.normalizeChapter(chapter);
        if (normalized.isBlank() || ecId == null || ecId.isBlank()) {
            return false;
        }
        List<CanvasExclusiveChoice> choices = new ArrayList<>(CHAPTER_CANVAS_EXCLUSIVE_CHOICES.getOrDefault(normalized, List.of()));
        if (!choices.removeIf(ec -> ec.id().equals(ecId))) {
            return false;
        }
        putOrRemove(CHAPTER_CANVAS_EXCLUSIVE_CHOICES, normalized, choices);
        removeLayerOrder(normalized, "exclusive_choice:" + ecId);
        return true;
    }

    public static void putImage(String chapter, CanvasImageLayer image) {
        String normalized = ClientChapterState.normalizeChapter(chapter);
        if (normalized.isBlank() || image == null || image.id().isBlank()) {
            return;
        }
        List<CanvasImageLayer> images = new ArrayList<>(CHAPTER_CANVAS_IMAGES.getOrDefault(normalized, List.of()));
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
        CHAPTER_CANVAS_IMAGES.put(normalized, List.copyOf(images));
        ensureLayerOrder(normalized, "image:" + image.id());
    }

    public static boolean removeImage(String chapter, String imageId) {
        String normalized = ClientChapterState.normalizeChapter(chapter);
        if (normalized.isBlank() || imageId == null || imageId.isBlank()) {
            return false;
        }
        List<CanvasImageLayer> images = new ArrayList<>(CHAPTER_CANVAS_IMAGES.getOrDefault(normalized, List.of()));
        if (!images.removeIf(image -> image.id().equals(imageId))) {
            return false;
        }
        putOrRemove(CHAPTER_CANVAS_IMAGES, normalized, images);
        removeLayerOrder(normalized, "image:" + imageId);
        return true;
    }

    public static void putText(String chapter, CanvasTextLayer text) {
        String normalized = ClientChapterState.normalizeChapter(chapter);
        if (normalized.isBlank() || text == null || text.id().isBlank()) {
            return;
        }
        List<CanvasTextLayer> texts = new ArrayList<>(CHAPTER_CANVAS_TEXTS.getOrDefault(normalized, List.of()));
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
        CHAPTER_CANVAS_TEXTS.put(normalized, List.copyOf(texts));
        ensureLayerOrder(normalized, "text:" + text.id());
    }

    public static boolean removeText(String chapter, String textId) {
        String normalized = ClientChapterState.normalizeChapter(chapter);
        if (normalized.isBlank() || textId == null || textId.isBlank()) {
            return false;
        }
        List<CanvasTextLayer> texts = new ArrayList<>(CHAPTER_CANVAS_TEXTS.getOrDefault(normalized, List.of()));
        if (!texts.removeIf(text -> text.id().equals(textId))) {
            return false;
        }
        putOrRemove(CHAPTER_CANVAS_TEXTS, normalized, texts);
        removeLayerOrder(normalized, "text:" + textId);
        return true;
    }

    public static void setLayerOrder(String chapter, List<String> order) {
        String normalized = ClientChapterState.normalizeChapter(chapter);
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
        putOrRemove(CHAPTER_CANVAS_LAYER_ORDER, normalized, sanitized);
    }

    private static void ensureLayerOrder(String chapter, String key) {
        List<String> order = new ArrayList<>(CHAPTER_CANVAS_LAYER_ORDER.getOrDefault(chapter, List.of()));
        if (!order.contains(key)) {
            order.add(key);
            CHAPTER_CANVAS_LAYER_ORDER.put(chapter, List.copyOf(order));
        }
    }

    private static void removeLayerOrder(String chapter, String key) {
        List<String> order = new ArrayList<>(CHAPTER_CANVAS_LAYER_ORDER.getOrDefault(chapter, List.of()));
        if (order.remove(key)) {
            putOrRemove(CHAPTER_CANVAS_LAYER_ORDER, chapter, order);
        }
    }

    private static <T> void putOrRemove(Map<String, List<T>> target, String chapter, List<T> values) {
        if (values == null || values.isEmpty()) {
            target.remove(chapter);
        } else {
            target.put(chapter, List.copyOf(values));
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
