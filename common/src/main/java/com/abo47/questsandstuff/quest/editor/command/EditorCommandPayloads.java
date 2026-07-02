package com.abo47.questsandstuff.quest.editor.command;

import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbtCodec;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
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

    // ── Keys ──

    public static final String BACKGROUND = "background";
    public static final String BLUEPRINT = "blueprint";
    public static final String COLOR = "color";
    public static final String DESCRIPTION = "description";
    public static final String ENABLED = "enabled";
    public static final String EXCLUSIVE_CHOICE = "exclusive_choice";
    public static final String EXCLUSIVE_CHOICES = "exclusive_choices";
    public static final String GRAYSCALE = "grayscale";
    public static final String GRID = "grid";
    public static final String GROUP = "group";
    public static final String HIDDEN = "hidden";
    public static final String ICON = "icon";
    public static final String ID = "id";
    public static final String IMAGE = "image";
    public static final String IMAGES = "images";
    public static final String JSON = "json";
    public static final String LAYER_ORDER = "layer_order";
    public static final String MODE = "mode";
    public static final String MOVES = "moves";
    public static final String OFFSET = "offset";
    public static final String ORDER = "order";
    public static final String PREREQUISITE = "prerequisite";
    public static final String QUEST = "quest";
    public static final String QUESTS = "quests";
    public static final String REWARD = "reward";
    public static final String SCALE = "scale";
    public static final String SCALES = "scales";
    public static final String SOUND = "sound";
    public static final String SPACING = "spacing";
    public static final String SPANS = "spans";
    public static final String TEXTURE = "texture";
    public static final String TEXTURES = "textures";
    public static final String TASK = "task";
    public static final String TEXT = "text";
    public static final String TEXTS = "texts";
    public static final String VOLUME = "volume";
    public static final String X = "x";
    public static final String Y = "y";

    // ── Limits ──

    public static final long MAX_NBT_BYTES = 2L * 1024L * 1024L;
    public static final int MAX_BULK_EDIT_ENTRIES = 1024;
    public static final int MAX_DESCRIPTION_LINES = 256;
    public static final int MAX_EDITOR_JSON_LENGTH = 131_072;
    public static final int MAX_LAYER_ORDER_ENTRIES = 2048;
    public static final int MAX_TEXT_SPANS = 256;

    public static NbtAccounter nbtAccounter() {
        return new NbtAccounter(MAX_NBT_BYTES);
    }

    public static void requireAllowed(EditorCommandType type, CompoundTag payload) {
        if (!isAllowed(type, payload)) {
            String name = type == null ? "" : type.wireName();
            throw new IllegalArgumentException("Editor command payload exceeds limits: " + name);
        }
    }

    public static boolean isAllowed(EditorCommandType type, CompoundTag payload) {
        if (payload == null || type == null) {
            return true;
        }
        return switch (type) {
            case MOVE_MANY -> !exceedsLimit(payload.getList(MOVES, Tag.TAG_COMPOUND), MAX_BULK_EDIT_ENTRIES);
            case SCALE_MANY -> !exceedsLimit(payload.getList(SCALES, Tag.TAG_COMPOUND), MAX_BULK_EDIT_ENTRIES);
            case COPY_MANY -> !exceedsLimit(payload.getList(QUESTS, Tag.TAG_STRING), MAX_BULK_EDIT_ENTRIES);
            case PASTE_BLUEPRINT -> {
                CompoundTag blueprint = payload.getCompound(BLUEPRINT);
                yield !exceedsLimit(blueprint.getList(QUESTS, Tag.TAG_COMPOUND), MAX_BULK_EDIT_ENTRIES)
                        && !exceedsLimit(blueprint.getList(IMAGES, Tag.TAG_COMPOUND), MAX_BULK_EDIT_ENTRIES)
                        && !exceedsLimit(blueprint.getList(TEXTS, Tag.TAG_COMPOUND), MAX_BULK_EDIT_ENTRIES)
                        && !exceedsLimit(blueprint.getList(LAYER_ORDER, Tag.TAG_STRING), MAX_LAYER_ORDER_ENTRIES);
            }
            case DESCRIPTION_PUT -> !exceedsLimit(payload.getList(DESCRIPTION, Tag.TAG_STRING), MAX_DESCRIPTION_LINES);
            case TASK_PUT, REWARD_PUT -> !exceedsLength(payload.getString(JSON), MAX_EDITOR_JSON_LENGTH);
            case CANVAS_TEXT_PUT -> !exceedsLimit(payload.getCompound(TEXT).getList(SPANS, Tag.TAG_COMPOUND), MAX_TEXT_SPANS);
            case CANVAS_LAYER_ORDER -> !exceedsLimit(payload.getList(ORDER, Tag.TAG_STRING), MAX_LAYER_ORDER_ENTRIES);
            case CANVAS_EXCLUSIVE_CHOICE_PUT -> !exceedsLength(payload.getCompound(EXCLUSIVE_CHOICE).getString(JSON), MAX_EDITOR_JSON_LENGTH);
            case CANVAS_EXCLUSIVE_CHOICE_PUT_MANY -> !exceedsLimit(payload.getList(EXCLUSIVE_CHOICES, Tag.TAG_COMPOUND), MAX_BULK_EDIT_ENTRIES);
            case CONNECTION_TEXTURE_MANY -> !exceedsLimit(payload.getList(TEXTURES, Tag.TAG_COMPOUND), MAX_BULK_EDIT_ENTRIES);
            default -> true;
        };
    }

    public static boolean exceedsLimit(ListTag tags, int maxEntries) {
        return tags != null && tags.size() > maxEntries;
    }

    public static boolean exceedsLength(String value, int maxLength) {
        return value != null && value.length() > maxLength;
    }

    // ── Reader ──

    public static String group(CompoundTag payload) {
        return string(payload, GROUP);
    }

    public static String quest(CompoundTag payload) {
        return string(payload, QUEST);
    }

    public static String prerequisite(CompoundTag payload) {
        return string(payload, PREREQUISITE);
    }

    public static String json(CompoundTag payload) {
        return string(payload, JSON);
    }

    public static String task(CompoundTag payload) {
        return string(payload, TASK);
    }

    public static String reward(CompoundTag payload) {
        return string(payload, REWARD);
    }

    public static ListTag moves(CompoundTag payload) {
        return list(payload, MOVES, Tag.TAG_COMPOUND);
    }

    public static ListTag scales(CompoundTag payload) {
        return list(payload, SCALES, Tag.TAG_COMPOUND);
    }

    public static ListTag description(CompoundTag payload) {
        return list(payload, DESCRIPTION, Tag.TAG_STRING);
    }

    public static ListTag order(CompoundTag payload) {
        return list(payload, ORDER, Tag.TAG_STRING);
    }

    public static Set<String> questIds(CompoundTag payload) {
        return nonBlankStringSet(list(payload, QUESTS, Tag.TAG_STRING));
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

    // ── Payload Factory ──

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
                move.putString(QUEST, questId);
                move.putInt(X, xy[0]);
                move.putInt(Y, xy[1]);
                tags.add(move);
            }
        }
        payload.put(MOVES, tags);
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
                scaleTag.putString(QUEST, questId);
                scaleTag.putFloat(SCALE, scale);
                tags.add(scaleTag);
            }
        }
        payload.put(SCALES, tags);
        return payload;
    }

    public static CompoundTag copyMany(String group, Collection<String> questIds) {
        CompoundTag payload = group(group);
        payload.put(QUESTS, strings(questIds));
        return payload;
    }

    public static CompoundTag pasteClipboard(String group, int x, int y) {
        return groupPoint(group, x, y);
    }

    public static CompoundTag pasteBlueprint(String group, int x, int y, CanvasBlueprint blueprint) {
        CompoundTag payload = groupPoint(group, x, y);
        payload.put(BLUEPRINT, blueprint == null ? new CompoundTag() : blueprint.toPacketTag());
        return payload;
    }

    public static CompoundTag prerequisite(String questId, String prerequisiteId) {
        CompoundTag payload = quest(questId);
        payload.putString(PREREQUISITE, clean(prerequisiteId));
        return payload;
    }

    public static CompoundTag connectionColor(String questId, String prerequisiteId, int color) {
        CompoundTag payload = prerequisite(questId, prerequisiteId);
        payload.putInt(COLOR, color);
        return payload;
    }

    public static CompoundTag connectionMode(String questId, String prerequisiteId, boolean grid) {
        CompoundTag payload = prerequisite(questId, prerequisiteId);
        payload.putBoolean(GRID, grid);
        return payload;
    }

    public static CompoundTag connectionHidden(String questId, String prerequisiteId, boolean hidden) {
        CompoundTag payload = prerequisite(questId, prerequisiteId);
        payload.putBoolean(HIDDEN, hidden);
        return payload;
    }

    public static CompoundTag connectionTexture(String questId, String prerequisiteId, String texture) {
        CompoundTag payload = prerequisite(questId, prerequisiteId);
        payload.putString(TEXTURE, clean(texture));
        return payload;
    }

    public static CompoundTag connectionTextures(Map<String, Map<String, String>> questTextures) {
        CompoundTag payload = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<String, Map<String, String>> questEntry : questTextures.entrySet()) {
            String questId = clean(questEntry.getKey());
            if (questId.isBlank()) continue;
            for (Map.Entry<String, String> prereqEntry : questEntry.getValue().entrySet()) {
                String prereqId = clean(prereqEntry.getKey());
                if (prereqId.isBlank()) continue;
                CompoundTag entry = new CompoundTag();
                entry.putString(QUEST, questId);
                entry.putString(PREREQUISITE, prereqId);
                entry.putString(TEXTURE, clean(prereqEntry.getValue()));
                list.add(entry);
            }
        }
        payload.put(TEXTURES, list);
        return payload;
    }

    public static CompoundTag connectionTextureSpacing(String questId, String prerequisiteId, int spacing) {
        CompoundTag payload = prerequisite(questId, prerequisiteId);
        payload.putInt(SPACING, Math.max(0, spacing));
        return payload;
    }

    public static CompoundTag questIcon(String questId, String icon) {
        return questString(questId, ICON, icon);
    }

    public static CompoundTag questRepeatable(String questId, boolean enabled) {
        CompoundTag payload = quest(questId);
        payload.putBoolean(ENABLED, enabled);
        return payload;
    }

    public static CompoundTag questHiddenMode(String questId, String mode) {
        return questString(questId, MODE, mode);
    }

    public static CompoundTag questVisualHidden(String questId, boolean hidden) {
        CompoundTag payload = quest(questId);
        payload.putBoolean(HIDDEN, hidden);
        return payload;
    }

    public static CompoundTag completionSound(String questId, String sound) {
        return questString(questId, SOUND, sound);
    }

    public static CompoundTag completionSoundMany(Collection<String> questIds, String sound) {
        CompoundTag payload = questIds(questIds);
        payload.putString(SOUND, clean(sound));
        return payload;
    }

    public static CompoundTag completionSoundVolume(String questId, int volume) {
        CompoundTag payload = quest(questId);
        payload.putInt(VOLUME, volume);
        return payload;
    }

    public static CompoundTag completionSoundVolumeMany(Collection<String> questIds, int volume) {
        CompoundTag payload = questIds(questIds);
        payload.putInt(VOLUME, volume);
        return payload;
    }

    public static CompoundTag completionHudBackground(String questId, String background) {
        return questString(questId, BACKGROUND, background);
    }

    public static CompoundTag completionHudBackgroundMany(Collection<String> questIds, String background) {
        CompoundTag payload = questIds(questIds);
        payload.putString(BACKGROUND, clean(background));
        return payload;
    }

    public static CompoundTag questBackground(String questId, String background, boolean grayscale) {
        CompoundTag payload = questString(questId, BACKGROUND, background);
        payload.putBoolean(GRAYSCALE, grayscale);
        return payload;
    }

    public static CompoundTag questBackgroundMany(Collection<String> questIds, String background, boolean grayscale) {
        CompoundTag payload = questIds(questIds);
        payload.putString(BACKGROUND, clean(background));
        payload.putBoolean(GRAYSCALE, grayscale);
        return payload;
    }

    public static CompoundTag description(String questId, Collection<String> description) {
        CompoundTag payload = quest(questId);
        payload.put(DESCRIPTION, strings(description));
        return payload;
    }

    public static CompoundTag taskPut(String questId, String json) {
        return questString(questId, JSON, json);
    }

    public static CompoundTag taskRemove(String questId, String taskId) {
        return questString(questId, TASK, taskId);
    }

    public static CompoundTag taskMove(String questId, String taskId, int offset) {
        CompoundTag payload = taskRemove(questId, taskId);
        payload.putInt(OFFSET, offset);
        return payload;
    }

    public static CompoundTag rewardPut(String questId, String json) {
        return questString(questId, JSON, json);
    }

    public static CompoundTag rewardRemove(String questId, String rewardId) {
        return questString(questId, REWARD, rewardId);
    }

    public static CompoundTag rewardMove(String questId, String rewardId, int offset) {
        CompoundTag payload = rewardRemove(questId, rewardId);
        payload.putInt(OFFSET, offset);
        return payload;
    }

    public static CompoundTag canvasExclusiveChoicePut(String group, CanvasExclusiveChoice ec) {
        CompoundTag payload = group(group);
        payload.put(EXCLUSIVE_CHOICE, CanvasLayerNbtCodec.exclusiveChoiceToTag(ec));
        return payload;
    }

    public static CompoundTag canvasExclusiveChoicesPut(String group, List<CanvasExclusiveChoice> ecs) {
        CompoundTag payload = group(group);
        payload.put(EXCLUSIVE_CHOICES, CanvasLayerNbtCodec.exclusiveChoicesToListTag(ecs));
        return payload;
    }

    public static CompoundTag canvasExclusiveChoiceRemove(String group, String ecId) {
        return groupId(group, ecId);
    }

    public static CompoundTag ecConnectionHidden(String group, String sourceId, String targetId, boolean hidden) {
        CompoundTag payload = group(group);
        payload.putString(ID, sourceId);
        payload.putString(PREREQUISITE, targetId);
        payload.putBoolean(HIDDEN, hidden);
        return payload;
    }

    public static CompoundTag canvasImagePut(String group, CanvasImageLayer image) {
        CompoundTag payload = group(group);
        payload.put(IMAGE, CanvasLayerNbtCodec.imageToTag(image));
        return payload;
    }

    public static CompoundTag canvasImageRemove(String group, String imageId) {
        return groupId(group, imageId);
    }

    public static CompoundTag canvasTextPut(String group, CanvasTextLayer text) {
        CompoundTag payload = group(group);
        payload.put(TEXT, CanvasLayerNbtCodec.textToTag(text));
        return payload;
    }

    public static CompoundTag canvasTextRemove(String group, String textId) {
        return groupId(group, textId);
    }

    public static CompoundTag canvasLayerOrder(String group, Collection<String> order) {
        CompoundTag payload = group(group);
        payload.put(ORDER, strings(order));
        return payload;
    }

    public static CompoundTag quest(String questId) {
        CompoundTag payload = new CompoundTag();
        payload.putString(QUEST, clean(questId));
        return payload;
    }

    public static CompoundTag questIds(Collection<String> questIds) {
        CompoundTag payload = new CompoundTag();
        payload.put(QUESTS, strings(questIds));
        return payload;
    }

    public static CompoundTag group(String group) {
        CompoundTag payload = new CompoundTag();
        payload.putString(GROUP, clean(group));
        return payload;
    }

    private static CompoundTag groupPoint(String group, int x, int y) {
        CompoundTag payload = group(group);
        payload.putInt(X, x);
        payload.putInt(Y, y);
        return payload;
    }

    private static CompoundTag groupId(String group, String id) {
        CompoundTag payload = group(group);
        payload.putString(ID, clean(id));
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

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
