package com.abo47.questsandstuff.quest.editor.command;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;

public final class EditorCommandPayloadLimits {
    public static final long MAX_NBT_BYTES = 2L * 1024L * 1024L;
    public static final int MAX_BULK_EDIT_ENTRIES = 1024;
    public static final int MAX_DESCRIPTION_LINES = 256;
    public static final int MAX_EDITOR_JSON_LENGTH = 131_072;
    public static final int MAX_LAYER_ORDER_ENTRIES = 2048;
    public static final int MAX_TEXT_SPANS = 256;

    private EditorCommandPayloadLimits() {
    }

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
            case MOVE_MANY -> !exceedsLimit(payload.getList(EditorCommandPayloadKeys.MOVES, Tag.TAG_COMPOUND), MAX_BULK_EDIT_ENTRIES);
            case SCALE_MANY -> !exceedsLimit(payload.getList(EditorCommandPayloadKeys.SCALES, Tag.TAG_COMPOUND), MAX_BULK_EDIT_ENTRIES);
            case COPY_MANY -> !exceedsLimit(payload.getList(EditorCommandPayloadKeys.QUESTS, Tag.TAG_STRING), MAX_BULK_EDIT_ENTRIES);
            case PASTE_BLUEPRINT -> {
                CompoundTag blueprint = payload.getCompound(EditorCommandPayloadKeys.BLUEPRINT);
                yield !exceedsLimit(blueprint.getList(EditorCommandPayloadKeys.QUESTS, Tag.TAG_COMPOUND), MAX_BULK_EDIT_ENTRIES)
                        && !exceedsLimit(blueprint.getList(EditorCommandPayloadKeys.IMAGES, Tag.TAG_COMPOUND), MAX_BULK_EDIT_ENTRIES)
                        && !exceedsLimit(blueprint.getList(EditorCommandPayloadKeys.TEXTS, Tag.TAG_COMPOUND), MAX_BULK_EDIT_ENTRIES)
                        && !exceedsLimit(blueprint.getList(EditorCommandPayloadKeys.LAYER_ORDER, Tag.TAG_STRING), MAX_LAYER_ORDER_ENTRIES);
            }
            case DESCRIPTION_PUT -> !exceedsLimit(payload.getList(EditorCommandPayloadKeys.DESCRIPTION, Tag.TAG_STRING), MAX_DESCRIPTION_LINES);
            case TASK_PUT, REWARD_PUT -> !exceedsLength(payload.getString(EditorCommandPayloadKeys.JSON), MAX_EDITOR_JSON_LENGTH);
            case CANVAS_TEXT_PUT -> !exceedsLimit(payload.getCompound(EditorCommandPayloadKeys.TEXT).getList(EditorCommandPayloadKeys.SPANS, Tag.TAG_COMPOUND), MAX_TEXT_SPANS);
            case CANVAS_LAYER_ORDER -> !exceedsLimit(payload.getList(EditorCommandPayloadKeys.ORDER, Tag.TAG_STRING), MAX_LAYER_ORDER_ENTRIES);
            default -> true;
        };
    }

    public static boolean exceedsLimit(ListTag tags, int maxEntries) {
        return tags != null && tags.size() > maxEntries;
    }

    public static boolean exceedsLength(String value, int maxLength) {
        return value != null && value.length() > maxLength;
    }
}
