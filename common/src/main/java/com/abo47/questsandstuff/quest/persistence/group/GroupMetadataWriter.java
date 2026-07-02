package com.abo47.questsandstuff.quest.persistence.group;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class GroupMetadataWriter {
    private GroupMetadataWriter() {
    }

    static void save(Path chaptersDir, GroupMetadataState state, Gson gson) {
        try {
            Files.createDirectories(chaptersDir);
            Set<Path> expected = new HashSet<>();
            for (int i = 0; i < state.groupOrder.size(); i++) {
                String group = state.groupOrder.get(i);
                Path target = chaptersDir.resolve(GroupMetadataJsonCodec.groupFileName(group) + ".json");
                expected.add(target.toAbsolutePath().normalize());
                GroupMetadataFiles.writeAtomic(target, gson.toJson(chapterJson(state, group, i)));
            }
            for (Path path : GroupMetadataFiles.deleteStaleJsonFiles(chaptersDir, expected)) {
                QuestsAndStuffMod.debugLog("[QnS:Store] deleted stale chapter metadata {}", path.getFileName());
            }
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed to persist chapter metadata {}", chaptersDir, e);
        }
    }

    static void saveGroups(Path chaptersDir, GroupMetadataState state, Gson gson, Collection<String> groups) {
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
                Path target = chaptersDir.resolve(GroupMetadataJsonCodec.groupFileName(group) + ".json");
                GroupMetadataFiles.writeAtomic(target, gson.toJson(chapterJson(state, group, order)));
            }
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed to persist chapter metadata {}", chaptersDir, e);
        }
    }

    private static JsonObject chapterJson(GroupMetadataState state, String group, int order) {
        JsonObject json = new JsonObject();
        json.addProperty("schema_version", GroupMetadataMigrator.CURRENT_SCHEMA);
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
        json.add("canvas_exclusive_choices", GroupMetadataJsonCodec.writeCanvasExclusiveChoices(state.canvasExclusiveChoicesByGroup.getOrDefault(group, List.of())));
        json.add("canvas_images", GroupMetadataJsonCodec.writeCanvasImages(state.canvasImagesByGroup.getOrDefault(group, List.of())));
        json.add("canvas_texts", GroupMetadataJsonCodec.writeCanvasTexts(state.canvasTextsByGroup.getOrDefault(group, List.of())));
        json.add("canvas_layer_order", GroupMetadataJsonCodec.writeStringArray(state.canvasLayerOrderByGroup.getOrDefault(group, List.of())));
        return json;
    }
}
