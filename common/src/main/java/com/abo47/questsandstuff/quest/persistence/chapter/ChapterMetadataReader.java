package com.abo47.questsandstuff.quest.persistence.chapter;

import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class ChapterMetadataReader {
    private ChapterMetadataReader() {
    }

    static boolean read(Path path, ChapterMetadataState state) {
        try {
            JsonObject raw = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            boolean migrated = ChapterMetadataMigrator.needsMigration(raw);
            JsonObject json = ChapterMetadataMigrator.migrate(raw);
            String name = ChapterMetadataState.normalizeGroupName(json.has("name") ? json.get("name").getAsString() : ChapterMetadataJsonCodec.stripJsonExtension(path.getFileName().toString()));
            if (name.isBlank()) {
                return migrated;
            }
            if (!state.groupOrder.contains(name)) {
                state.groupOrder.add(name);
            }
            readChapterChrome(json, state, name);
            readCanvasLayers(json, state, name);
            return migrated;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void readChapterChrome(JsonObject json, ChapterMetadataState state, String name) {
        if (json.has("icon")) {
            state.groupIcons.put(name, json.get("icon").getAsString());
        }
        if (json.has("background")) {
            state.groupBackgrounds.put(name, json.get("background").getAsString());
        }
        if (json.has("canvas_background")) {
            state.groupCanvasBackgrounds.put(name, json.get("canvas_background").getAsString());
        }
        if (json.has("text_align")) {
            state.groupTextAlign.put(name, ChapterMetadataJsonCodec.normalizeTextAlign(json.get("text_align").getAsString()));
        }
        if (json.has("text_color")) {
            state.groupTextColor.put(name, json.get("text_color").getAsInt());
        }
        if (json.has("text_style")) {
            state.groupTextStyle.put(name, ChapterMetadataJsonCodec.normalizeTextStyle(json.get("text_style").getAsString()));
        }
        if (json.has("text_size")) {
            state.groupTextSize.put(name, CanvasTextLayer.clampFontSize(json.get("text_size").getAsInt()));
        }
        if (json.has("lock_until_unlocked")) {
            state.groupLockUntilUnlocked.put(name, json.get("lock_until_unlocked").getAsBoolean());
        }
        if (json.has("hide_until_unlocked")) {
            state.groupHideUntilUnlocked.put(name, json.get("hide_until_unlocked").getAsBoolean());
        }
    }

    private static void readCanvasLayers(JsonObject json, ChapterMetadataState state, String name) {
        List<CanvasImageLayer> images = ChapterMetadataJsonCodec.readCanvasImages(json.get("canvas_images"));
        if (!images.isEmpty()) {
            state.canvasImagesByGroup.put(name, List.copyOf(images));
        }
        List<CanvasTextLayer> texts = ChapterMetadataJsonCodec.readCanvasTexts(json.get("canvas_texts"));
        if (!texts.isEmpty()) {
            state.canvasTextsByGroup.put(name, List.copyOf(texts));
        }
        List<String> layerOrder = ChapterMetadataJsonCodec.readStringArray(json.get("canvas_layer_order"));
        if (!layerOrder.isEmpty()) {
            state.canvasLayerOrderByGroup.put(name, List.copyOf(layerOrder));
        }
    }
}
