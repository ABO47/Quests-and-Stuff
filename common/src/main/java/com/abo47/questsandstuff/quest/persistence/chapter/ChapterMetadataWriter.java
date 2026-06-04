package com.abo47.questsandstuff.quest.persistence.chapter;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class ChapterMetadataWriter {
    private ChapterMetadataWriter() {
    }

    static void save(Path chaptersDir, ChapterMetadataState state, Gson gson) {
        try {
            Files.createDirectories(chaptersDir);
            Set<Path> expected = new HashSet<>();
            for (int i = 0; i < state.groupOrder.size(); i++) {
                String group = state.groupOrder.get(i);
                Path target = chaptersDir.resolve(ChapterMetadataJsonCodec.groupFileName(group) + ".json");
                expected.add(target.toAbsolutePath().normalize());
                ChapterMetadataFiles.writeAtomic(target, gson.toJson(chapterJson(state, group, i)));
            }
            for (Path path : ChapterMetadataFiles.deleteStaleJsonFiles(chaptersDir, expected)) {
                QuestsAndStuffMod.debugLog("[QnS:Store] deleted stale chapter metadata {}", path.getFileName());
            }
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed to persist chapter metadata {}", chaptersDir, e);
        }
    }

    static void saveGroups(Path chaptersDir, ChapterMetadataState state, Gson gson, Collection<String> groups) {
        if (groups == null || groups.isEmpty()) {
            return;
        }
        try {
            Files.createDirectories(chaptersDir);
            for (String group : groups) {
                if (group == null || group.isBlank()) {
                    continue;
                }
                int order = state.groupOrder.indexOf(group);
                if (order < 0) {
                    continue;
                }
                Path target = chaptersDir.resolve(ChapterMetadataJsonCodec.groupFileName(group) + ".json");
                ChapterMetadataFiles.writeAtomic(target, gson.toJson(chapterJson(state, group, order)));
            }
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed to persist chapter metadata {}", chaptersDir, e);
        }
    }

    private static JsonObject chapterJson(ChapterMetadataState state, String group, int order) {
        JsonObject json = new JsonObject();
        json.addProperty("schema_version", ChapterMetadataMigrator.CURRENT_SCHEMA);
        json.addProperty("name", group);
        json.addProperty("order", order);
        json.addProperty("icon", state.groupIcon(group));
        json.addProperty("background", state.groupBackground(group));
        json.addProperty("canvas_background", state.groupCanvasBackground(group));
        json.addProperty("text_align", state.groupTextAlign(group));
        json.addProperty("text_color", state.groupTextColor(group));
        json.addProperty("text_style", state.groupTextStyle(group));
        json.addProperty("text_size", state.groupTextSize(group));
        json.addProperty("lock_until_unlocked", state.groupLockUntilUnlocked(group));
        json.addProperty("hide_until_unlocked", state.groupHideUntilUnlocked(group));
        json.add("canvas_images", ChapterMetadataJsonCodec.writeCanvasImages(state.canvasImagesByGroup.getOrDefault(group, List.of())));
        json.add("canvas_texts", ChapterMetadataJsonCodec.writeCanvasTexts(state.canvasTextsByGroup.getOrDefault(group, List.of())));
        json.add("canvas_layer_order", ChapterMetadataJsonCodec.writeStringArray(state.canvasLayerOrderByGroup.getOrDefault(group, List.of())));
        return json;
    }
}
