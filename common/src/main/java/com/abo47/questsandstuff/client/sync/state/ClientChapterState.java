package com.abo47.questsandstuff.client.sync.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.sync.SyncKeys;
import com.abo47.questsandstuff.util.naming.QuestIdentity;

public final class ClientChapterState {
    private static final List<String> CHAPTER_ORDER = new ArrayList<>();
    private static final Map<String, String> CHAPTER_ICONS = new HashMap<>();
    private static final Map<String, String> CHAPTER_BACKGROUNDS = new HashMap<>();
    private static final Map<String, String> CHAPTER_CANVAS_BACKGROUNDS = new HashMap<>();
    private static final Map<String, String> CHAPTER_TEXT_ALIGN = new HashMap<>();
    private static final Map<String, Integer> CHAPTER_TEXT_COLOR = new HashMap<>();
    private static final Map<String, String> CHAPTER_TEXT_STYLE = new HashMap<>();
    private static final Map<String, Integer> CHAPTER_TEXT_SIZE = new HashMap<>();
    private static final Map<String, Boolean> CHAPTER_LOCK_UNTIL_UNLOCKED = new HashMap<>();
    private static final Map<String, Boolean> CHAPTER_HIDE_UNTIL_UNLOCKED = new HashMap<>();

    private ClientChapterState() {
    }

    public static void reset() {
        CHAPTER_ORDER.clear();
        CHAPTER_ICONS.clear();
        CHAPTER_BACKGROUNDS.clear();
        CHAPTER_CANVAS_BACKGROUNDS.clear();
        CHAPTER_TEXT_ALIGN.clear();
        CHAPTER_TEXT_COLOR.clear();
        CHAPTER_TEXT_STYLE.clear();
        CHAPTER_TEXT_SIZE.clear();
        CHAPTER_LOCK_UNTIL_UNLOCKED.clear();
        CHAPTER_HIDE_UNTIL_UNLOCKED.clear();
    }

    public static void loadFromFullPayload(CompoundTag payload) {
        reset();
        mergeFromDeltaPayload(payload);
    }

    public static void mergeFromDeltaPayload(CompoundTag payload) {
        if (payload == null) {
            return;
        }
        ListTag groupsTag = payload.getList(SyncKeys.CHAPTERS, Tag.TAG_STRING);
        boolean chaptersRebuilt = !groupsTag.isEmpty();
        if (chaptersRebuilt) {
            CHAPTER_ORDER.clear();
            for (int i = 0; i < groupsTag.size(); i++) {
                String chapter = normalizeChapter(groupsTag.getString(i));
                if (!chapter.isBlank() && !CHAPTER_ORDER.contains(chapter)) {
                    CHAPTER_ORDER.add(chapter);
                }
            }
            removeChaptersOutsideOrder();
        }

        CompoundTag chapterProps = payload.getCompound(SyncKeys.CHAPTER_PROPS);
        for (String chapter : chapterProps.getAllKeys()) {
            mergeChapterProps(chapter, chapterProps.getCompound(chapter));
        }
        if (chaptersRebuilt) {
            for (String chapter : CHAPTER_ORDER) {
                ensureChapterDefaults(chapter);
            }
        } else {
            for (String chapter : chapterProps.getAllKeys()) {
                ensureChapterDefaults(normalizeChapter(chapter));
            }
        }
    }

    public static List<String> chapterOrderSnapshot() {
        return List.copyOf(CHAPTER_ORDER);
    }

    public static boolean containsChapter(String chapter) {
        return CHAPTER_ORDER.contains(normalizeChapter(chapter));
    }

    public static boolean addChapter(String chapter) {
        String normalized = normalizeChapter(chapter);
        if (normalized.isBlank() || CHAPTER_ORDER.contains(normalized)) {
            return false;
        }
        CHAPTER_ORDER.add(normalized);
        ensureChapterDefaults(normalized);
        return true;
    }

    public static boolean renameChapter(String from, String to) {
        String source = normalizeChapter(from);
        String target = normalizeChapter(to);
        if (source.isBlank() || target.isBlank() || source.equals(target)) {
            return false;
        }
        int index = CHAPTER_ORDER.indexOf(source);
        if (index < 0 || CHAPTER_ORDER.contains(target)) {
            return false;
        }
        CHAPTER_ORDER.set(index, target);
        moveStringProp(CHAPTER_ICONS, source, target, "");
        moveStringProp(CHAPTER_BACKGROUNDS, source, target, "default");
        moveStringProp(CHAPTER_CANVAS_BACKGROUNDS, source, target, "default");
        moveStringProp(CHAPTER_TEXT_ALIGN, source, target, "center");
        moveStringProp(CHAPTER_TEXT_STYLE, source, target, "normal");
        Integer color = CHAPTER_TEXT_COLOR.remove(source);
        CHAPTER_TEXT_COLOR.put(target, color == null ? TabletColors.WHITE : color);
        Integer textSize = CHAPTER_TEXT_SIZE.remove(source);
        CHAPTER_TEXT_SIZE.put(target, textSize == null ? CanvasTextLayer.DEFAULT_FONT_SIZE : textSize);
        Boolean lockUntilUnlocked = CHAPTER_LOCK_UNTIL_UNLOCKED.remove(source);
        CHAPTER_LOCK_UNTIL_UNLOCKED.put(target, lockUntilUnlocked != null && lockUntilUnlocked);
        Boolean hideUntilUnlocked = CHAPTER_HIDE_UNTIL_UNLOCKED.remove(source);
        CHAPTER_HIDE_UNTIL_UNLOCKED.put(target, hideUntilUnlocked != null && hideUntilUnlocked);
        return true;
    }

    public static boolean removeChapter(String chapter) {
        String normalized = normalizeChapter(chapter);
        if (normalized.isBlank() || !CHAPTER_ORDER.remove(normalized)) {
            return false;
        }
        CHAPTER_ICONS.remove(normalized);
        CHAPTER_BACKGROUNDS.remove(normalized);
        CHAPTER_CANVAS_BACKGROUNDS.remove(normalized);
        CHAPTER_TEXT_ALIGN.remove(normalized);
        CHAPTER_TEXT_COLOR.remove(normalized);
        CHAPTER_TEXT_STYLE.remove(normalized);
        CHAPTER_TEXT_SIZE.remove(normalized);
        CHAPTER_LOCK_UNTIL_UNLOCKED.remove(normalized);
        CHAPTER_HIDE_UNTIL_UNLOCKED.remove(normalized);
        return true;
    }

    public static void moveChapter(String chapter, int offset) {
        String normalized = normalizeChapter(chapter);
        if (normalized.isBlank() || offset == 0) {
            return;
        }
        int index = CHAPTER_ORDER.indexOf(normalized);
        if (index < 0) {
            return;
        }
        int next = Math.max(0, Math.min(CHAPTER_ORDER.size() - 1, index + offset));
        moveChapterIndex(index, next, normalized);
    }

    public static void moveChapterToIndex(String chapter, int targetIndex) {
        String normalized = normalizeChapter(chapter);
        if (normalized.isBlank()) {
            return;
        }
        int index = CHAPTER_ORDER.indexOf(normalized);
        if (index < 0) {
            return;
        }
        int next = Math.max(0, Math.min(CHAPTER_ORDER.size() - 1, targetIndex));
        moveChapterIndex(index, next, normalized);
    }

    public static String chapterIcon(String chapter) {
        return CHAPTER_ICONS.getOrDefault(normalizeChapter(chapter), "");
    }

    public static String chapterBackground(String chapter) {
        return CHAPTER_BACKGROUNDS.getOrDefault(normalizeChapter(chapter), "default");
    }

    public static String chapterCanvasBackground(String chapter) {
        return CHAPTER_CANVAS_BACKGROUNDS.getOrDefault(normalizeChapter(chapter), "default");
    }

    public static String chapterTextAlign(String chapter) {
        return CHAPTER_TEXT_ALIGN.getOrDefault(normalizeChapter(chapter), "center");
    }

    public static int chapterTextColor(String chapter) {
        return CHAPTER_TEXT_COLOR.getOrDefault(normalizeChapter(chapter), TabletColors.WHITE);
    }

    public static String chapterTextStyle(String chapter) {
        return CHAPTER_TEXT_STYLE.getOrDefault(normalizeChapter(chapter), "normal");
    }

    public static int chapterTextSize(String chapter) {
        return CHAPTER_TEXT_SIZE.getOrDefault(normalizeChapter(chapter), CanvasTextLayer.DEFAULT_FONT_SIZE);
    }

    public static boolean chapterLockUntilUnlocked(String chapter) {
        return CHAPTER_LOCK_UNTIL_UNLOCKED.getOrDefault(normalizeChapter(chapter), false);
    }

    public static boolean chapterHideUntilUnlocked(String chapter) {
        return CHAPTER_HIDE_UNTIL_UNLOCKED.getOrDefault(normalizeChapter(chapter), false);
    }

    public static void setChapterIcon(String chapter, String icon) {
        String normalized = normalizeChapter(chapter);
        if (!normalized.isBlank()) {
            CHAPTER_ICONS.put(normalized, icon == null ? "" : icon.trim());
        }
    }

    public static void setChapterBackground(String chapter, String background) {
        String normalized = normalizeChapter(chapter);
        if (!normalized.isBlank()) {
            CHAPTER_BACKGROUNDS.put(normalized, background == null || background.isBlank() ? "default" : background.trim());
        }
    }

    public static void setChapterCanvasBackground(String chapter, String background) {
        String normalized = normalizeChapter(chapter);
        if (!normalized.isBlank()) {
            CHAPTER_CANVAS_BACKGROUNDS.put(normalized, background == null || background.isBlank() ? "default" : background.trim());
        }
    }

    public static void setChapterTextAlign(String chapter, String align) {
        String normalized = normalizeChapter(chapter);
        if (!normalized.isBlank()) {
            CHAPTER_TEXT_ALIGN.put(normalized, normalizeTextAlign(align));
        }
    }

    public static void setChapterTextColor(String chapter, int color) {
        String normalized = normalizeChapter(chapter);
        if (!normalized.isBlank()) {
            CHAPTER_TEXT_COLOR.put(normalized, color);
        }
    }

    public static void setChapterTextStyle(String chapter, String style) {
        String normalized = normalizeChapter(chapter);
        if (!normalized.isBlank()) {
            CHAPTER_TEXT_STYLE.put(normalized, normalizeTextStyle(style));
        }
    }

    public static void setChapterTextSize(String chapter, int size) {
        String normalized = normalizeChapter(chapter);
        if (!normalized.isBlank()) {
            CHAPTER_TEXT_SIZE.put(normalized, clampTextSize(size));
        }
    }

    public static void setChapterLockUntilUnlocked(String chapter, boolean lockUntilUnlocked) {
        String normalized = normalizeChapter(chapter);
        if (!normalized.isBlank()) {
            CHAPTER_LOCK_UNTIL_UNLOCKED.put(normalized, lockUntilUnlocked);
        }
    }

    public static void setChapterHideUntilUnlocked(String chapter, boolean hideUntilUnlocked) {
        String normalized = normalizeChapter(chapter);
        if (!normalized.isBlank()) {
            CHAPTER_HIDE_UNTIL_UNLOCKED.put(normalized, hideUntilUnlocked);
        }
    }

    public static String normalizeChapter(String value) {
        return QuestIdentity.chapterName(value);
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

    private static void moveChapterIndex(int index, int next, String chapter) {
        if (next == index) {
            return;
        }
        CHAPTER_ORDER.remove(index);
        CHAPTER_ORDER.add(next, chapter);
    }

    private static void mergeChapterProps(String rawChapter, CompoundTag props) {
        String chapter = normalizeChapter(rawChapter);
        if (chapter.isBlank()) {
            return;
        }
        if (!CHAPTER_ORDER.contains(chapter)) {
            CHAPTER_ORDER.add(chapter);
        }
        CHAPTER_ICONS.put(chapter, props.getString(SyncKeys.ChapterProps.ICON));
        CHAPTER_BACKGROUNDS.put(chapter, props.getString(SyncKeys.ChapterProps.BACKGROUND));
        CHAPTER_CANVAS_BACKGROUNDS.put(chapter, props.contains(SyncKeys.ChapterProps.CANVAS_BACKGROUND) ? props.getString(SyncKeys.ChapterProps.CANVAS_BACKGROUND) : "default");
        CHAPTER_TEXT_ALIGN.put(chapter, normalizeTextAlign(props.getString(SyncKeys.ChapterProps.TEXT_ALIGN)));
        if (props.contains(SyncKeys.ChapterProps.TEXT_COLOR)) {
            CHAPTER_TEXT_COLOR.put(chapter, props.getInt(SyncKeys.ChapterProps.TEXT_COLOR));
        }
        CHAPTER_TEXT_STYLE.put(chapter, normalizeTextStyle(props.contains(SyncKeys.ChapterProps.TEXT_STYLE) ? props.getString(SyncKeys.ChapterProps.TEXT_STYLE) : "normal"));
        CHAPTER_TEXT_SIZE.put(chapter, clampTextSize(props.contains(SyncKeys.ChapterProps.TEXT_SIZE) ? props.getInt(SyncKeys.ChapterProps.TEXT_SIZE) : CanvasTextLayer.DEFAULT_FONT_SIZE));
        CHAPTER_LOCK_UNTIL_UNLOCKED.put(chapter, props.getBoolean(SyncKeys.ChapterProps.LOCK_UNTIL_UNLOCKED));
        CHAPTER_HIDE_UNTIL_UNLOCKED.put(chapter, props.getBoolean(SyncKeys.ChapterProps.HIDE_UNTIL_UNLOCKED));
    }

    private static void ensureChapterDefaults(String chapter) {
        CHAPTER_ICONS.putIfAbsent(chapter, "");
        CHAPTER_BACKGROUNDS.putIfAbsent(chapter, "default");
        CHAPTER_CANVAS_BACKGROUNDS.putIfAbsent(chapter, "default");
        CHAPTER_TEXT_ALIGN.putIfAbsent(chapter, "center");
        CHAPTER_TEXT_COLOR.putIfAbsent(chapter, TabletColors.WHITE);
        CHAPTER_TEXT_STYLE.putIfAbsent(chapter, "normal");
        CHAPTER_TEXT_SIZE.putIfAbsent(chapter, CanvasTextLayer.DEFAULT_FONT_SIZE);
        CHAPTER_LOCK_UNTIL_UNLOCKED.putIfAbsent(chapter, false);
        CHAPTER_HIDE_UNTIL_UNLOCKED.putIfAbsent(chapter, false);
    }

    private static void removeChaptersOutsideOrder() {
        CHAPTER_ICONS.keySet().removeIf(chapter -> !CHAPTER_ORDER.contains(chapter));
        CHAPTER_BACKGROUNDS.keySet().removeIf(chapter -> !CHAPTER_ORDER.contains(chapter));
        CHAPTER_CANVAS_BACKGROUNDS.keySet().removeIf(chapter -> !CHAPTER_ORDER.contains(chapter));
        CHAPTER_TEXT_ALIGN.keySet().removeIf(chapter -> !CHAPTER_ORDER.contains(chapter));
        CHAPTER_TEXT_COLOR.keySet().removeIf(chapter -> !CHAPTER_ORDER.contains(chapter));
        CHAPTER_TEXT_STYLE.keySet().removeIf(chapter -> !CHAPTER_ORDER.contains(chapter));
        CHAPTER_TEXT_SIZE.keySet().removeIf(chapter -> !CHAPTER_ORDER.contains(chapter));
        CHAPTER_LOCK_UNTIL_UNLOCKED.keySet().removeIf(chapter -> !CHAPTER_ORDER.contains(chapter));
        CHAPTER_HIDE_UNTIL_UNLOCKED.keySet().removeIf(chapter -> !CHAPTER_ORDER.contains(chapter));
    }

    private static void moveStringProp(Map<String, String> props, String source, String target, String fallback) {
        String value = props.remove(source);
        props.put(target, value == null ? fallback : value);
    }
}
