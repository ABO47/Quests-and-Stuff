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
    private static final List<String> GROUP_ORDER = new ArrayList<>();
    private static final Map<String, String> GROUP_ICONS = new HashMap<>();
    private static final Map<String, String> GROUP_BACKGROUNDS = new HashMap<>();
    private static final Map<String, String> GROUP_CANVAS_BACKGROUNDS = new HashMap<>();
    private static final Map<String, String> GROUP_TEXT_ALIGN = new HashMap<>();
    private static final Map<String, Integer> GROUP_TEXT_COLOR = new HashMap<>();
    private static final Map<String, String> GROUP_TEXT_STYLE = new HashMap<>();
    private static final Map<String, Integer> GROUP_TEXT_SIZE = new HashMap<>();
    private static final Map<String, Boolean> GROUP_LOCK_UNTIL_UNLOCKED = new HashMap<>();
    private static final Map<String, Boolean> GROUP_HIDE_UNTIL_UNLOCKED = new HashMap<>();

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
        GROUP_LOCK_UNTIL_UNLOCKED.clear();
        GROUP_HIDE_UNTIL_UNLOCKED.clear();
    }

    public static void loadFromFullPayload(CompoundTag payload) {
        reset();
        mergeFromDeltaPayload(payload);
    }

    public static void mergeFromDeltaPayload(CompoundTag payload) {
        if (payload == null) {
            return;
        }
        ListTag groupsTag = payload.getList("groups", Tag.TAG_STRING);
        if (!groupsTag.isEmpty()) {
            GROUP_ORDER.clear();
            for (int i = 0; i < groupsTag.size(); i++) {
                String group = normalizeGroup(groupsTag.getString(i));
                if (!group.isBlank() && !GROUP_ORDER.contains(group)) {
                    GROUP_ORDER.add(group);
                }
            }
            removeGroupsOutsideOrder();
        }

        CompoundTag groupProps = payload.getCompound("group_props");
        for (String group : groupProps.getAllKeys()) {
            mergeGroupProps(group, groupProps.getCompound(group));
        }
        for (String group : GROUP_ORDER) {
            ensureGroupDefaults(group);
        }
    }

    public static List<String> groupOrderSnapshot() {
        return List.copyOf(GROUP_ORDER);
    }

    public static boolean containsGroup(String group) {
        return GROUP_ORDER.contains(normalizeGroup(group));
    }

    public static boolean addGroup(String group) {
        String normalized = normalizeGroup(group);
        if (normalized.isBlank() || GROUP_ORDER.contains(normalized)) {
            return false;
        }
        GROUP_ORDER.add(normalized);
        GROUP_ICONS.putIfAbsent(normalized, "");
        GROUP_BACKGROUNDS.putIfAbsent(normalized, "default");
        GROUP_CANVAS_BACKGROUNDS.putIfAbsent(normalized, "default");
        GROUP_TEXT_ALIGN.putIfAbsent(normalized, "center");
        GROUP_TEXT_COLOR.putIfAbsent(normalized, 0xFFFFFFFF);
        GROUP_TEXT_STYLE.putIfAbsent(normalized, "normal");
        GROUP_TEXT_SIZE.putIfAbsent(normalized, CanvasTextLayer.DEFAULT_FONT_SIZE);
        GROUP_LOCK_UNTIL_UNLOCKED.putIfAbsent(normalized, false);
        GROUP_HIDE_UNTIL_UNLOCKED.putIfAbsent(normalized, false);
        return true;
    }

    public static boolean renameGroup(String from, String to) {
        String source = normalizeGroup(from);
        String target = normalizeGroup(to);
        if (source.isBlank() || target.isBlank() || source.equals(target)) {
            return false;
        }
        int index = GROUP_ORDER.indexOf(source);
        if (index < 0 || GROUP_ORDER.contains(target)) {
            return false;
        }
        GROUP_ORDER.set(index, target);
        moveStringProp(GROUP_ICONS, source, target, "");
        moveStringProp(GROUP_BACKGROUNDS, source, target, "default");
        moveStringProp(GROUP_CANVAS_BACKGROUNDS, source, target, "default");
        moveStringProp(GROUP_TEXT_ALIGN, source, target, "center");
        moveStringProp(GROUP_TEXT_STYLE, source, target, "normal");
        Integer color = GROUP_TEXT_COLOR.remove(source);
        GROUP_TEXT_COLOR.put(target, color == null ? 0xFFFFFFFF : color);
        Integer textSize = GROUP_TEXT_SIZE.remove(source);
        GROUP_TEXT_SIZE.put(target, textSize == null ? CanvasTextLayer.DEFAULT_FONT_SIZE : textSize);
        Boolean lockUntilUnlocked = GROUP_LOCK_UNTIL_UNLOCKED.remove(source);
        GROUP_LOCK_UNTIL_UNLOCKED.put(target, lockUntilUnlocked != null && lockUntilUnlocked);
        Boolean hideUntilUnlocked = GROUP_HIDE_UNTIL_UNLOCKED.remove(source);
        GROUP_HIDE_UNTIL_UNLOCKED.put(target, hideUntilUnlocked != null && hideUntilUnlocked);
        return true;
    }

    public static boolean removeGroup(String group) {
        String normalized = normalizeGroup(group);
        if (normalized.isBlank() || !GROUP_ORDER.remove(normalized)) {
            return false;
        }
        GROUP_ICONS.remove(normalized);
        GROUP_BACKGROUNDS.remove(normalized);
        GROUP_CANVAS_BACKGROUNDS.remove(normalized);
        GROUP_TEXT_ALIGN.remove(normalized);
        GROUP_TEXT_COLOR.remove(normalized);
        GROUP_TEXT_STYLE.remove(normalized);
        GROUP_TEXT_SIZE.remove(normalized);
        GROUP_LOCK_UNTIL_UNLOCKED.remove(normalized);
        GROUP_HIDE_UNTIL_UNLOCKED.remove(normalized);
        return true;
    }

    public static void moveGroup(String group, int offset) {
        String normalized = normalizeGroup(group);
        if (normalized.isBlank() || offset == 0) {
            return;
        }
        int index = GROUP_ORDER.indexOf(normalized);
        if (index < 0) {
            return;
        }
        int next = Math.max(0, Math.min(GROUP_ORDER.size() - 1, index + offset));
        moveGroupIndex(index, next, normalized);
    }

    public static void moveGroupToIndex(String group, int targetIndex) {
        String normalized = normalizeGroup(group);
        if (normalized.isBlank()) {
            return;
        }
        int index = GROUP_ORDER.indexOf(normalized);
        if (index < 0) {
            return;
        }
        int next = Math.max(0, Math.min(GROUP_ORDER.size() - 1, targetIndex));
        moveGroupIndex(index, next, normalized);
    }

    public static String groupIcon(String group) {
        return GROUP_ICONS.getOrDefault(normalizeGroup(group), "");
    }

    public static String groupBackground(String group) {
        return GROUP_BACKGROUNDS.getOrDefault(normalizeGroup(group), "default");
    }

    public static String groupCanvasBackground(String group) {
        return GROUP_CANVAS_BACKGROUNDS.getOrDefault(normalizeGroup(group), "default");
    }

    public static String groupTextAlign(String group) {
        return GROUP_TEXT_ALIGN.getOrDefault(normalizeGroup(group), "center");
    }

    public static int groupTextColor(String group) {
        return GROUP_TEXT_COLOR.getOrDefault(normalizeGroup(group), 0xFFFFFFFF);
    }

    public static String groupTextStyle(String group) {
        return GROUP_TEXT_STYLE.getOrDefault(normalizeGroup(group), "normal");
    }

    public static int groupTextSize(String group) {
        return GROUP_TEXT_SIZE.getOrDefault(normalizeGroup(group), CanvasTextLayer.DEFAULT_FONT_SIZE);
    }

    public static boolean groupLockUntilUnlocked(String group) {
        return GROUP_LOCK_UNTIL_UNLOCKED.getOrDefault(normalizeGroup(group), false);
    }

    public static boolean groupHideUntilUnlocked(String group) {
        return GROUP_HIDE_UNTIL_UNLOCKED.getOrDefault(normalizeGroup(group), false);
    }

    public static void setGroupIcon(String group, String icon) {
        String normalized = normalizeGroup(group);
        if (!normalized.isBlank()) {
            GROUP_ICONS.put(normalized, icon == null ? "" : icon.trim());
        }
    }

    public static void setGroupBackground(String group, String background) {
        String normalized = normalizeGroup(group);
        if (!normalized.isBlank()) {
            GROUP_BACKGROUNDS.put(normalized, background == null || background.isBlank() ? "default" : background.trim());
        }
    }

    public static void setGroupCanvasBackground(String group, String background) {
        String normalized = normalizeGroup(group);
        if (!normalized.isBlank()) {
            GROUP_CANVAS_BACKGROUNDS.put(normalized, background == null || background.isBlank() ? "default" : background.trim());
        }
    }

    public static void setGroupTextAlign(String group, String align) {
        String normalized = normalizeGroup(group);
        if (!normalized.isBlank()) {
            GROUP_TEXT_ALIGN.put(normalized, normalizeTextAlign(align));
        }
    }

    public static void setGroupTextColor(String group, int color) {
        String normalized = normalizeGroup(group);
        if (!normalized.isBlank()) {
            GROUP_TEXT_COLOR.put(normalized, color);
        }
    }

    public static void setGroupTextStyle(String group, String style) {
        String normalized = normalizeGroup(group);
        if (!normalized.isBlank()) {
            GROUP_TEXT_STYLE.put(normalized, normalizeTextStyle(style));
        }
    }

    public static void setGroupTextSize(String group, int size) {
        String normalized = normalizeGroup(group);
        if (!normalized.isBlank()) {
            GROUP_TEXT_SIZE.put(normalized, clampTextSize(size));
        }
    }

    public static void setGroupLockUntilUnlocked(String group, boolean lockUntilUnlocked) {
        String normalized = normalizeGroup(group);
        if (!normalized.isBlank()) {
            GROUP_LOCK_UNTIL_UNLOCKED.put(normalized, lockUntilUnlocked);
        }
    }

    public static void setGroupHideUntilUnlocked(String group, boolean hideUntilUnlocked) {
        String normalized = normalizeGroup(group);
        if (!normalized.isBlank()) {
            GROUP_HIDE_UNTIL_UNLOCKED.put(normalized, hideUntilUnlocked);
        }
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
        return CanvasTextLayer.clampFontSize(value);
    }

    private static void moveGroupIndex(int index, int next, String group) {
        if (next == index) {
            return;
        }
        GROUP_ORDER.remove(index);
        GROUP_ORDER.add(next, group);
    }

    private static void mergeGroupProps(String rawGroup, CompoundTag props) {
        String group = normalizeGroup(rawGroup);
        if (group.isBlank()) {
            return;
        }
        if (!GROUP_ORDER.contains(group)) {
            GROUP_ORDER.add(group);
        }
        GROUP_ICONS.put(group, props.getString("icon"));
        GROUP_BACKGROUNDS.put(group, props.getString("background"));
        GROUP_CANVAS_BACKGROUNDS.put(group, props.contains("canvas_background") ? props.getString("canvas_background") : "default");
        GROUP_TEXT_ALIGN.put(group, normalizeTextAlign(props.getString("text_align")));
        if (props.contains("text_color")) {
            GROUP_TEXT_COLOR.put(group, props.getInt("text_color"));
        }
        GROUP_TEXT_STYLE.put(group, normalizeTextStyle(props.contains("text_style") ? props.getString("text_style") : "normal"));
        GROUP_TEXT_SIZE.put(group, clampTextSize(props.contains("text_size") ? props.getInt("text_size") : CanvasTextLayer.DEFAULT_FONT_SIZE));
        GROUP_LOCK_UNTIL_UNLOCKED.put(group, props.getBoolean("lock_until_unlocked"));
        GROUP_HIDE_UNTIL_UNLOCKED.put(group, props.getBoolean("hide_until_unlocked"));
    }

    private static void ensureGroupDefaults(String group) {
        GROUP_ICONS.putIfAbsent(group, "");
        GROUP_BACKGROUNDS.putIfAbsent(group, "default");
        GROUP_CANVAS_BACKGROUNDS.putIfAbsent(group, "default");
        GROUP_TEXT_ALIGN.putIfAbsent(group, "center");
        GROUP_TEXT_COLOR.putIfAbsent(group, 0xFFFFFFFF);
        GROUP_TEXT_STYLE.putIfAbsent(group, "normal");
        GROUP_TEXT_SIZE.putIfAbsent(group, CanvasTextLayer.DEFAULT_FONT_SIZE);
        GROUP_LOCK_UNTIL_UNLOCKED.putIfAbsent(group, false);
        GROUP_HIDE_UNTIL_UNLOCKED.putIfAbsent(group, false);
    }

    private static void removeGroupsOutsideOrder() {
        GROUP_ICONS.keySet().removeIf(group -> !GROUP_ORDER.contains(group));
        GROUP_BACKGROUNDS.keySet().removeIf(group -> !GROUP_ORDER.contains(group));
        GROUP_CANVAS_BACKGROUNDS.keySet().removeIf(group -> !GROUP_ORDER.contains(group));
        GROUP_TEXT_ALIGN.keySet().removeIf(group -> !GROUP_ORDER.contains(group));
        GROUP_TEXT_COLOR.keySet().removeIf(group -> !GROUP_ORDER.contains(group));
        GROUP_TEXT_STYLE.keySet().removeIf(group -> !GROUP_ORDER.contains(group));
        GROUP_TEXT_SIZE.keySet().removeIf(group -> !GROUP_ORDER.contains(group));
        GROUP_LOCK_UNTIL_UNLOCKED.keySet().removeIf(group -> !GROUP_ORDER.contains(group));
        GROUP_HIDE_UNTIL_UNLOCKED.keySet().removeIf(group -> !GROUP_ORDER.contains(group));
    }

    private static void moveStringProp(Map<String, String> props, String source, String target, String fallback) {
        String value = props.remove(source);
        props.put(target, value == null ? fallback : value);
    }
}
