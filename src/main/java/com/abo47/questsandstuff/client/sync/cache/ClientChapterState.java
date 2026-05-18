package com.abo47.questsandstuff.client.sync.cache;

import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ClientChapterState {
    public static final List<String> GROUP_ORDER = new ArrayList<>();
    public static final Map<String, String> GROUP_ICONS = new HashMap<>();
    public static final Map<String, String> GROUP_BACKGROUNDS = new HashMap<>();
    public static final Map<String, String> GROUP_CANVAS_BACKGROUNDS = new HashMap<>();
    public static final Map<String, String> GROUP_TEXT_ALIGN = new HashMap<>();
    public static final Map<String, Integer> GROUP_TEXT_COLOR = new HashMap<>();
    public static final Map<String, String> GROUP_TEXT_STYLE = new HashMap<>();
    public static final Map<String, Integer> GROUP_TEXT_SIZE = new HashMap<>();

    private ClientChapterState() {
    }

    public static void reset() {
        GROUP_ORDER.clear();
        GROUP_ICONS.clear();
        GROUP_BACKGROUNDS.clear();
        GROUP_CANVAS_BACKGROUNDS.clear();
        GROUP_TEXT_ALIGN.clear();
        GROUP_TEXT_COLOR.clear();
        GROUP_TEXT_STYLE.clear();
        GROUP_TEXT_SIZE.clear();
    }

    public static void loadFromFullPayload(CompoundTag payload) {
        reset();
        ListTag groupsTag = payload.getList("groups", Tag.TAG_STRING);
        for (int i = 0; i < groupsTag.size(); i++) {
            GROUP_ORDER.add(groupsTag.getString(i));
        }

        CompoundTag groupProps = payload.getCompound("group_props");
        for (String group : groupProps.getAllKeys()) {
            CompoundTag props = groupProps.getCompound(group);
            GROUP_ICONS.put(group, props.getString("icon"));
            GROUP_BACKGROUNDS.put(group, props.getString("background"));
            GROUP_CANVAS_BACKGROUNDS.put(group, props.contains("canvas_background") ? props.getString("canvas_background") : "default");
            GROUP_TEXT_ALIGN.put(group, normalizeTextAlign(props.getString("text_align")));
            if (props.contains("text_color")) {
                GROUP_TEXT_COLOR.put(group, props.getInt("text_color"));
            }
            GROUP_TEXT_STYLE.put(group, normalizeTextStyle(props.contains("text_style") ? props.getString("text_style") : "normal"));
            GROUP_TEXT_SIZE.put(group, clampTextSize(props.contains("text_size") ? props.getInt("text_size") : CanvasTextLayer.DEFAULT_FONT_SIZE));
        }
    }

    public static List<String> groupOrderSnapshot() {
        return List.copyOf(GROUP_ORDER);
    }

    public static String groupIcon(String group) {
        return GROUP_ICONS.getOrDefault(group, "");
    }

    public static String groupBackground(String group) {
        return GROUP_BACKGROUNDS.getOrDefault(group, "default");
    }

    public static String groupCanvasBackground(String group) {
        return GROUP_CANVAS_BACKGROUNDS.getOrDefault(group, "default");
    }

    public static String groupTextAlign(String group) {
        return GROUP_TEXT_ALIGN.getOrDefault(group, "center");
    }

    public static int groupTextColor(String group) {
        return GROUP_TEXT_COLOR.getOrDefault(group, 0xFFFFFFFF);
    }

    public static String groupTextStyle(String group) {
        return GROUP_TEXT_STYLE.getOrDefault(group, "normal");
    }

    public static int groupTextSize(String group) {
        return GROUP_TEXT_SIZE.getOrDefault(group, CanvasTextLayer.DEFAULT_FONT_SIZE);
    }

    public static String normalizeGroup(String value) {
        return value == null ? "" : value.trim();
    }

    public static String normalizeTextStyle(String value) {
        return CanvasTextLayer.normalizeStyle(value);
    }

    public static String normalizeTextAlign(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "left", "right", "center" -> normalized;
            default -> "center";
        };
    }

    public static int clampTextSize(int value) {
        return Math.max(CanvasTextLayer.MIN_FONT_SIZE, Math.min(18, value));
    }
}
