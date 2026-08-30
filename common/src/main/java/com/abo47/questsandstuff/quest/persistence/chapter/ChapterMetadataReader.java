package com.abo47.questsandstuff.quest.persistence.chapter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

final class ChapterMetadataReader {
    private ChapterMetadataReader() {
    }

    static boolean read(Path path, ChapterMetadataState state, Map<String, Integer> orders) {
        try {
            JsonObject raw = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            boolean migrated = ChapterMetadataMigrator.needsMigration(raw);
            JsonObject json = ChapterMetadataMigrator.migrate(raw);
            String name = ChapterMetadataState.normalizeChapterName(json.has("name") ? json.get("name").getAsString() : ChapterMetadataJsonCodec.stripJsonExtension(path.getFileName().toString()));
            if (name.isBlank()) {
                return migrated;
            }
            if (!state.chapterOrder.contains(name)) {
                state.chapterOrder.add(name);
            }
            int order = json.has("order") ? json.get("order").getAsInt() : Integer.MAX_VALUE;
            orders.put(name, order);
            readChapterChrome(json, state, name);
            readCanvasLayers(json, state, name);
            return migrated;
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.error("Failed reading chapter metadata file {}", path, e);
            return false;
        }
    }

    private static void readChapterChrome(JsonObject json, ChapterMetadataState state, String name) {
        if (json.has("icon")) {
            state.chapterIcons.put(name, json.get("icon").getAsString());
        }
        if (json.has("background")) {
            state.chapterBackgrounds.put(name, json.get("background").getAsString());
        }
        if (json.has("canvas_background")) {
            state.chapterCanvasBackgrounds.put(name, json.get("canvas_background").getAsString());
        }
        if (json.has("text_align")) {
            state.chapterTextAlign.put(name, ChapterMetadataJsonCodec.normalizeTextAlign(json.get("text_align").getAsString()));
        }
        if (json.has("text_color")) {
            state.chapterTextColor.put(name, json.get("text_color").getAsInt());
        }
        if (json.has("text_style")) {
            state.chapterTextStyle.put(name, ChapterMetadataJsonCodec.normalizeTextStyle(json.get("text_style").getAsString()));
        }
        if (json.has("text_size")) {
            state.chapterTextSize.put(name, CanvasTextLayer.clampFontSize(json.get("text_size").getAsInt()));
        }
        if (json.has("lock_until_unlocked")) {
            state.chapterLockUntilUnlocked.put(name, json.get("lock_until_unlocked").getAsBoolean());
        }
        if (json.has("hide_until_unlocked")) {
            state.chapterHideUntilUnlocked.put(name, json.get("hide_until_unlocked").getAsBoolean());
        }
    }

    private static void readCanvasLayers(JsonObject json, ChapterMetadataState state, String name) {
        List<CanvasExclusiveChoice> exclusiveChoices = ChapterMetadataJsonCodec.readCanvasExclusiveChoices(json.get("canvas_exclusive_choices"));
        if (!exclusiveChoices.isEmpty()) {
            state.canvasExclusiveChoicesByChapter.put(name, List.copyOf(exclusiveChoices));
        }
        List<CanvasImageLayer> images = ChapterMetadataJsonCodec.readCanvasImages(json.get("canvas_images"));
        if (!images.isEmpty()) {
            state.canvasImagesByChapter.put(name, List.copyOf(images));
        }
        List<CanvasTextLayer> texts = ChapterMetadataJsonCodec.readCanvasTexts(json.get("canvas_texts"));
        if (!texts.isEmpty()) {
            state.canvasTextsByChapter.put(name, List.copyOf(texts));
        }
        List<String> layerOrder = ChapterMetadataJsonCodec.readStringArray(json.get("canvas_layer_order"));
        if (!layerOrder.isEmpty()) {
            state.canvasLayerOrderByChapter.put(name, List.copyOf(layerOrder));
        }
    }
}
