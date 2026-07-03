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
            for (int i = 0; i < state.chapterOrder.size(); i++) {
                String chapter = state.chapterOrder.get(i);
                Path target = chaptersDir.resolve(ChapterMetadataJsonCodec.chapterFileName(chapter) + ".json");
                expected.add(target.toAbsolutePath().normalize());
                ChapterMetadataFiles.writeAtomic(target, gson.toJson(chapterJson(state, chapter, i)));
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
            for (String chapter : groups) {
                if (chapter == null || chapter.isBlank()) {
                    continue;
                }
                int order = state.chapterOrder.indexOf(chapter);
                if (order < 0) {
                    continue;
                }
                Path target = chaptersDir.resolve(ChapterMetadataJsonCodec.chapterFileName(chapter) + ".json");
                ChapterMetadataFiles.writeAtomic(target, gson.toJson(chapterJson(state, chapter, order)));
            }
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed to persist chapter metadata {}", chaptersDir, e);
        }
    }

    private static JsonObject chapterJson(ChapterMetadataState state, String chapter, int order) {
        JsonObject json = new JsonObject();
        json.addProperty("schema_version", ChapterMetadataMigrator.CURRENT_SCHEMA);
        json.addProperty("name", chapter);
        json.addProperty("order", order);
        json.addProperty("icon", state.chapterIcon(chapter));
        json.addProperty("background", state.chapterBackground(chapter));
        json.addProperty("canvas_background", state.chapterCanvasBackground(chapter));
        json.addProperty("text_align", state.chapterTextAlign(chapter));
        json.addProperty("text_color", state.chapterTextColor(chapter));
        json.addProperty("text_style", state.chapterTextStyle(chapter));
        json.addProperty("text_size", state.chapterTextSize(chapter));
        json.addProperty("lock_until_unlocked", state.chapterLockUntilUnlocked(chapter));
        json.addProperty("hide_until_unlocked", state.chapterHideUntilUnlocked(chapter));
        json.add("canvas_exclusive_choices", ChapterMetadataJsonCodec.writeCanvasExclusiveChoices(state.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of())));
        json.add("canvas_images", ChapterMetadataJsonCodec.writeCanvasImages(state.canvasImagesByChapter.getOrDefault(chapter, List.of())));
        json.add("canvas_texts", ChapterMetadataJsonCodec.writeCanvasTexts(state.canvasTextsByChapter.getOrDefault(chapter, List.of())));
        json.add("canvas_layer_order", ChapterMetadataJsonCodec.writeStringArray(state.canvasLayerOrderByChapter.getOrDefault(chapter, List.of())));
        return json;
    }
}
