package com.abo47.questsandstuff.quest.persistence.group;

import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class GroupMetadataReader {
    private GroupMetadataReader() {
    }

    static boolean read(Path path, GroupMetadataState state) {
        try {
            JsonObject raw = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            boolean migrated = GroupMetadataMigrator.needsMigration(raw);
            JsonObject json = GroupMetadataMigrator.migrate(raw);
            String name = GroupMetadataState.normalizeGroupName(json.has("name") ? json.get("name").getAsString() : GroupMetadataJsonCodec.stripJsonExtension(path.getFileName().toString()));
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

    private static void readChapterChrome(JsonObject json, GroupMetadataState state, String name) {
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
            state.groupTextAlign.put(name, GroupMetadataJsonCodec.normalizeTextAlign(json.get("text_align").getAsString()));
        }
        if (json.has("text_color")) {
            state.groupTextColor.put(name, json.get("text_color").getAsInt());
        }
        if (json.has("text_style")) {
            state.groupTextStyle.put(name, GroupMetadataJsonCodec.normalizeTextStyle(json.get("text_style").getAsString()));
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

    private static void readCanvasLayers(JsonObject json, GroupMetadataState state, String name) {
        List<CanvasExclusiveChoice> exclusiveChoices = GroupMetadataJsonCodec.readCanvasExclusiveChoices(json.get("canvas_exclusive_choices"));
        if (!exclusiveChoices.isEmpty()) {
            state.canvasExclusiveChoicesByGroup.put(name, List.copyOf(exclusiveChoices));
        }
        List<CanvasImageLayer> images = GroupMetadataJsonCodec.readCanvasImages(json.get("canvas_images"));
        if (!images.isEmpty()) {
            state.canvasImagesByGroup.put(name, List.copyOf(images));
        }
        List<CanvasTextLayer> texts = GroupMetadataJsonCodec.readCanvasTexts(json.get("canvas_texts"));
        if (!texts.isEmpty()) {
            state.canvasTextsByGroup.put(name, List.copyOf(texts));
        }
        List<String> layerOrder = GroupMetadataJsonCodec.readStringArray(json.get("canvas_layer_order"));
        if (!layerOrder.isEmpty()) {
            state.canvasLayerOrderByGroup.put(name, List.copyOf(layerOrder));
        }
    }
}
