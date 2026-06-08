package com.abo47.questsandstuff.quest.editor.command;

import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbt;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EditorCommandPayloads {
    private EditorCommandPayloads() {
    }

    public static CompoundTag moveMany(String group, Map<String, int[]> moves) {
        CompoundTag payload = group(group);
        ListTag tags = new ListTag();
        if (moves != null) {
            for (Map.Entry<String, int[]> entry : moves.entrySet()) {
                String questId = clean(entry.getKey());
                int[] xy = entry.getValue();
                if (questId.isBlank() || xy == null || xy.length < 2) {
                    continue;
                }
                CompoundTag move = new CompoundTag();
                move.putString(EditorCommandPayloadKeys.QUEST, questId);
                move.putInt(EditorCommandPayloadKeys.X, xy[0]);
                move.putInt(EditorCommandPayloadKeys.Y, xy[1]);
                tags.add(move);
            }
        }
        payload.put(EditorCommandPayloadKeys.MOVES, tags);
        return payload;
    }

    public static CompoundTag scaleMany(String group, Map<String, Float> scales) {
        CompoundTag payload = group(group);
        ListTag tags = new ListTag();
        if (scales != null) {
            for (Map.Entry<String, Float> entry : scales.entrySet()) {
                String questId = clean(entry.getKey());
                Float scale = entry.getValue();
                if (questId.isBlank() || scale == null || Float.isNaN(scale) || Float.isInfinite(scale)) {
                    continue;
                }
                CompoundTag scaleTag = new CompoundTag();
                scaleTag.putString(EditorCommandPayloadKeys.QUEST, questId);
                scaleTag.putFloat(EditorCommandPayloadKeys.SCALE, scale);
                tags.add(scaleTag);
            }
        }
        payload.put(EditorCommandPayloadKeys.SCALES, tags);
        return payload;
    }

    public static CompoundTag copyMany(String group, Collection<String> questIds) {
        CompoundTag payload = group(group);
        payload.put(EditorCommandPayloadKeys.QUESTS, strings(questIds));
        return payload;
    }

    public static CompoundTag pasteClipboard(String group, int x, int y) {
        return groupPoint(group, x, y);
    }

    public static CompoundTag pasteBlueprint(String group, int x, int y, CanvasBlueprint blueprint) {
        CompoundTag payload = groupPoint(group, x, y);
        payload.put(EditorCommandPayloadKeys.BLUEPRINT, blueprint == null ? new CompoundTag() : blueprint.toPacketTag());
        return payload;
    }

    public static CompoundTag prerequisite(String questId, String prerequisiteId) {
        CompoundTag payload = quest(questId);
        payload.putString(EditorCommandPayloadKeys.PREREQUISITE, clean(prerequisiteId));
        return payload;
    }

    public static CompoundTag connectionColor(String questId, String prerequisiteId, int color) {
        CompoundTag payload = prerequisite(questId, prerequisiteId);
        payload.putInt(EditorCommandPayloadKeys.COLOR, color);
        return payload;
    }

    public static CompoundTag connectionMode(String questId, String prerequisiteId, boolean grid) {
        CompoundTag payload = prerequisite(questId, prerequisiteId);
        payload.putBoolean(EditorCommandPayloadKeys.GRID, grid);
        return payload;
    }

    public static CompoundTag connectionHidden(String questId, String prerequisiteId, boolean hidden) {
        CompoundTag payload = prerequisite(questId, prerequisiteId);
        payload.putBoolean(EditorCommandPayloadKeys.HIDDEN, hidden);
        return payload;
    }

    public static CompoundTag questIcon(String questId, String icon) {
        return questString(questId, EditorCommandPayloadKeys.ICON, icon);
    }

    public static CompoundTag questRepeatable(String questId, boolean enabled) {
        CompoundTag payload = quest(questId);
        payload.putBoolean(EditorCommandPayloadKeys.ENABLED, enabled);
        return payload;
    }

    public static CompoundTag questHiddenMode(String questId, String mode) {
        return questString(questId, EditorCommandPayloadKeys.MODE, mode);
    }

    public static CompoundTag questVisualHidden(String questId, boolean hidden) {
        CompoundTag payload = quest(questId);
        payload.putBoolean(EditorCommandPayloadKeys.HIDDEN, hidden);
        return payload;
    }

    public static CompoundTag completionSound(String questId, String sound) {
        return questString(questId, EditorCommandPayloadKeys.SOUND, sound);
    }

    public static CompoundTag completionSoundMany(Collection<String> questIds, String sound) {
        CompoundTag payload = questIds(questIds);
        payload.putString(EditorCommandPayloadKeys.SOUND, clean(sound));
        return payload;
    }

    public static CompoundTag completionSoundVolume(String questId, int volume) {
        CompoundTag payload = quest(questId);
        payload.putInt(EditorCommandPayloadKeys.VOLUME, volume);
        return payload;
    }

    public static CompoundTag completionSoundVolumeMany(Collection<String> questIds, int volume) {
        CompoundTag payload = questIds(questIds);
        payload.putInt(EditorCommandPayloadKeys.VOLUME, volume);
        return payload;
    }

    public static CompoundTag completionHudBackground(String questId, String background) {
        return questString(questId, EditorCommandPayloadKeys.BACKGROUND, background);
    }

    public static CompoundTag completionHudBackgroundMany(Collection<String> questIds, String background) {
        CompoundTag payload = questIds(questIds);
        payload.putString(EditorCommandPayloadKeys.BACKGROUND, clean(background));
        return payload;
    }

    public static CompoundTag questBackground(String questId, String background, boolean grayscale) {
        CompoundTag payload = questString(questId, EditorCommandPayloadKeys.BACKGROUND, background);
        payload.putBoolean(EditorCommandPayloadKeys.GRAYSCALE, grayscale);
        return payload;
    }

    public static CompoundTag questBackgroundMany(Collection<String> questIds, String background, boolean grayscale) {
        CompoundTag payload = questIds(questIds);
        payload.putString(EditorCommandPayloadKeys.BACKGROUND, clean(background));
        payload.putBoolean(EditorCommandPayloadKeys.GRAYSCALE, grayscale);
        return payload;
    }

    public static CompoundTag description(String questId, Collection<String> description) {
        CompoundTag payload = quest(questId);
        payload.put(EditorCommandPayloadKeys.DESCRIPTION, strings(description));
        return payload;
    }

    public static CompoundTag taskPut(String questId, String json) {
        return questString(questId, EditorCommandPayloadKeys.JSON, json);
    }

    public static CompoundTag taskRemove(String questId, String taskId) {
        return questString(questId, EditorCommandPayloadKeys.TASK, taskId);
    }

    public static CompoundTag taskMove(String questId, String taskId, int offset) {
        CompoundTag payload = taskRemove(questId, taskId);
        payload.putInt(EditorCommandPayloadKeys.OFFSET, offset);
        return payload;
    }

    public static CompoundTag rewardPut(String questId, String json) {
        return questString(questId, EditorCommandPayloadKeys.JSON, json);
    }

    public static CompoundTag rewardRemove(String questId, String rewardId) {
        return questString(questId, EditorCommandPayloadKeys.REWARD, rewardId);
    }

    public static CompoundTag rewardMove(String questId, String rewardId, int offset) {
        CompoundTag payload = rewardRemove(questId, rewardId);
        payload.putInt(EditorCommandPayloadKeys.OFFSET, offset);
        return payload;
    }

    public static CompoundTag canvasImagePut(String group, CanvasImageLayer image) {
        CompoundTag payload = group(group);
        payload.put(EditorCommandPayloadKeys.IMAGE, CanvasLayerNbt.imageToTag(image));
        return payload;
    }

    public static CompoundTag canvasImageRemove(String group, String imageId) {
        return groupId(group, imageId);
    }

    public static CompoundTag canvasTextPut(String group, CanvasTextLayer text) {
        CompoundTag payload = group(group);
        payload.put(EditorCommandPayloadKeys.TEXT, CanvasLayerNbt.textToTag(text));
        return payload;
    }

    public static CompoundTag canvasTextRemove(String group, String textId) {
        return groupId(group, textId);
    }

    public static CompoundTag canvasLayerOrder(String group, Collection<String> order) {
        CompoundTag payload = group(group);
        payload.put(EditorCommandPayloadKeys.ORDER, strings(order));
        return payload;
    }

    public static CompoundTag quest(String questId) {
        CompoundTag payload = new CompoundTag();
        payload.putString(EditorCommandPayloadKeys.QUEST, clean(questId));
        return payload;
    }

    public static CompoundTag questIds(Collection<String> questIds) {
        CompoundTag payload = new CompoundTag();
        payload.put(EditorCommandPayloadKeys.QUESTS, strings(questIds));
        return payload;
    }

    public static CompoundTag group(String group) {
        CompoundTag payload = new CompoundTag();
        payload.putString(EditorCommandPayloadKeys.GROUP, clean(group));
        return payload;
    }

    public static String group(CompoundTag payload) {
        return string(payload, EditorCommandPayloadKeys.GROUP);
    }

    public static String quest(CompoundTag payload) {
        return string(payload, EditorCommandPayloadKeys.QUEST);
    }

    public static String prerequisite(CompoundTag payload) {
        return string(payload, EditorCommandPayloadKeys.PREREQUISITE);
    }

    public static String json(CompoundTag payload) {
        return string(payload, EditorCommandPayloadKeys.JSON);
    }

    public static String task(CompoundTag payload) {
        return string(payload, EditorCommandPayloadKeys.TASK);
    }

    public static String reward(CompoundTag payload) {
        return string(payload, EditorCommandPayloadKeys.REWARD);
    }

    public static ListTag moves(CompoundTag payload) {
        return list(payload, EditorCommandPayloadKeys.MOVES, Tag.TAG_COMPOUND);
    }

    public static ListTag scales(CompoundTag payload) {
        return list(payload, EditorCommandPayloadKeys.SCALES, Tag.TAG_COMPOUND);
    }

    public static ListTag description(CompoundTag payload) {
        return list(payload, EditorCommandPayloadKeys.DESCRIPTION, Tag.TAG_STRING);
    }

    public static ListTag order(CompoundTag payload) {
        return list(payload, EditorCommandPayloadKeys.ORDER, Tag.TAG_STRING);
    }

    public static Set<String> questIds(CompoundTag payload) {
        return nonBlankStringSet(list(payload, EditorCommandPayloadKeys.QUESTS, Tag.TAG_STRING));
    }

    public static List<String> stringsFrom(ListTag tags) {
        List<String> values = new ArrayList<>();
        if (tags == null) {
            return values;
        }
        for (int i = 0; i < tags.size(); i++) {
            values.add(tags.getString(i));
        }
        return values;
    }

    public static List<String> nonBlankStringsFrom(ListTag tags) {
        List<String> values = new ArrayList<>();
        if (tags == null) {
            return values;
        }
        for (int i = 0; i < tags.size(); i++) {
            String value = tags.getString(i);
            if (value != null && !value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    public static String string(CompoundTag payload, String key) {
        return payload == null ? "" : payload.getString(key);
    }

    public static int integer(CompoundTag payload, String key) {
        return payload == null ? 0 : payload.getInt(key);
    }

    public static boolean bool(CompoundTag payload, String key) {
        return payload != null && payload.getBoolean(key);
    }

    public static CompoundTag compound(CompoundTag payload, String key) {
        return payload == null ? new CompoundTag() : payload.getCompound(key);
    }

    public static ListTag list(CompoundTag payload, String key, byte elementType) {
        return payload == null ? new ListTag() : payload.getList(key, elementType);
    }

    private static CompoundTag groupPoint(String group, int x, int y) {
        CompoundTag payload = group(group);
        payload.putInt(EditorCommandPayloadKeys.X, x);
        payload.putInt(EditorCommandPayloadKeys.Y, y);
        return payload;
    }

    private static CompoundTag groupId(String group, String id) {
        CompoundTag payload = group(group);
        payload.putString(EditorCommandPayloadKeys.ID, clean(id));
        return payload;
    }

    private static CompoundTag questString(String questId, String key, String value) {
        CompoundTag payload = quest(questId);
        payload.putString(key, clean(value));
        return payload;
    }

    private static ListTag strings(Collection<String> values) {
        ListTag tags = new ListTag();
        if (values == null) {
            return tags;
        }
        for (String value : values) {
            String normalized = clean(value);
            if (!normalized.isBlank()) {
                tags.add(StringTag.valueOf(normalized));
            }
        }
        return tags;
    }

    private static Set<String> nonBlankStringSet(ListTag tags) {
        Set<String> values = new LinkedHashSet<>();
        if (tags == null) {
            return values;
        }
        for (int i = 0; i < tags.size(); i++) {
            String value = tags.getString(i);
            if (value != null && !value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
