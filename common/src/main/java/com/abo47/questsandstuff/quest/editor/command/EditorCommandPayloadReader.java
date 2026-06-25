package com.abo47.questsandstuff.quest.editor.command;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class EditorCommandPayloadReader {
    private EditorCommandPayloadReader() {
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
}
