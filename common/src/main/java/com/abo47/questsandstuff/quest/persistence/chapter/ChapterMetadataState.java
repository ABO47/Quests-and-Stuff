package com.abo47.questsandstuff.quest.persistence.chapter;

import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.util.naming.QuestIdentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ChapterMetadataState {
    final List<String> chapterOrder = new ArrayList<>();
    final Map<String, String> chapterIcons = new HashMap<>();
    final Map<String, String> chapterBackgrounds = new HashMap<>();
    final Map<String, String> chapterCanvasBackgrounds = new HashMap<>();
    final Map<String, String> chapterTextAlign = new HashMap<>();
    final Map<String, Integer> chapterTextColor = new HashMap<>();
    final Map<String, String> chapterTextStyle = new HashMap<>();
    final Map<String, Integer> chapterTextSize = new HashMap<>();
    final Map<String, Boolean> chapterLockUntilUnlocked = new HashMap<>();
    final Map<String, Boolean> chapterHideUntilUnlocked = new HashMap<>();
    final Map<String, List<CanvasExclusiveChoice>> canvasExclusiveChoicesByChapter = new HashMap<>();
    final Map<String, List<CanvasImageLayer>> canvasImagesByChapter = new HashMap<>();
    final Map<String, List<CanvasTextLayer>> canvasTextsByChapter = new HashMap<>();
    final Map<String, List<String>> canvasLayerOrderByChapter = new HashMap<>();

    void clear() {
        chapterOrder.clear();
        chapterIcons.clear();
        chapterBackgrounds.clear();
        chapterCanvasBackgrounds.clear();
        chapterTextAlign.clear();
        chapterTextColor.clear();
        chapterTextStyle.clear();
        chapterTextSize.clear();
        chapterLockUntilUnlocked.clear();
        chapterHideUntilUnlocked.clear();
        canvasExclusiveChoicesByChapter.clear();
        canvasImagesByChapter.clear();
        canvasTextsByChapter.clear();
        canvasLayerOrderByChapter.clear();
    }

    void setChapterOrder(List<String> chapters, Set<String> discoveredChapters) {
        chapterOrder.clear();
        if (chapters != null) {
            for (String chapter : chapters) {
                String normalized = normalizeChapterName(chapter);
                if (normalized.isBlank()) {
                    continue;
                }
                if (!chapterOrder.contains(normalized)) {
                    chapterOrder.add(normalized);
                }
            }
        }
        reconcile(discoveredChapters);
    }

    void renameChapter(String fromName, String toName) {
        String from = normalizeChapterName(fromName);
        String to = normalizeChapterName(toName);
        if (from.isBlank() || to.isBlank() || from.equals(to)) {
            return;
        }
        for (int i = 0; i < chapterOrder.size(); i++) {
            if (from.equals(chapterOrder.get(i))) {
                chapterOrder.set(i, to);
                break;
            }
        }
        moveValue(chapterIcons, from, to);
        moveValue(chapterBackgrounds, from, to);
        moveValue(chapterCanvasBackgrounds, from, to);
        moveValue(chapterTextAlign, from, to);
        moveValue(chapterTextColor, from, to);
        moveValue(chapterTextStyle, from, to);
        moveValue(chapterTextSize, from, to);
        moveValue(chapterLockUntilUnlocked, from, to);
        moveValue(chapterHideUntilUnlocked, from, to);
        moveValue(canvasExclusiveChoicesByChapter, from, to);
        moveValue(canvasImagesByChapter, from, to);
        moveValue(canvasTextsByChapter, from, to);
        moveValue(canvasLayerOrderByChapter, from, to);
    }

    String chapterIcon(String chapter) {
        return chapterIcons.getOrDefault(chapter, "");
    }

    String chapterBackground(String chapter) {
        return chapterBackgrounds.getOrDefault(chapter, "default");
    }

    String chapterCanvasBackground(String chapter) {
        return chapterCanvasBackgrounds.getOrDefault(chapter, "default");
    }

    String chapterTextAlign(String chapter) {
        return chapterTextAlign.getOrDefault(chapter, "center");
    }

    int chapterTextColor(String chapter) {
        return chapterTextColor.getOrDefault(chapter, TabletColors.WHITE);
    }

    String chapterTextStyle(String chapter) {
        return chapterTextStyle.getOrDefault(chapter, "normal");
    }

    int chapterTextSize(String chapter) {
        return chapterTextSize.getOrDefault(chapter, CanvasTextLayer.DEFAULT_FONT_SIZE);
    }

    boolean chapterLockUntilUnlocked(String chapter) {
        return chapterLockUntilUnlocked.getOrDefault(chapter, false);
    }

    boolean chapterHideUntilUnlocked(String chapter) {
        return chapterHideUntilUnlocked.getOrDefault(chapter, false);
    }

    void reconcile(Set<String> discoveredChapters) {
        Set<String> discovered = discoveredChapters == null ? Set.of() : discoveredChapters;
        chapterOrder.removeIf(chapter -> chapter == null || chapter.isBlank());
        for (String chapter : discovered) {
            if (!chapterOrder.contains(chapter)) {
                chapterOrder.add(chapter);
            }
        }
        chapterIcons.keySet().removeIf(chapter -> !chapterOrder.contains(chapter));
        chapterBackgrounds.keySet().removeIf(chapter -> !chapterOrder.contains(chapter));
        chapterCanvasBackgrounds.keySet().removeIf(chapter -> !chapterOrder.contains(chapter));
        chapterTextAlign.keySet().removeIf(chapter -> !chapterOrder.contains(chapter));
        chapterTextColor.keySet().removeIf(chapter -> !chapterOrder.contains(chapter));
        chapterTextStyle.keySet().removeIf(chapter -> !chapterOrder.contains(chapter));
        chapterTextSize.keySet().removeIf(chapter -> !chapterOrder.contains(chapter));
        chapterLockUntilUnlocked.keySet().removeIf(chapter -> !chapterOrder.contains(chapter));
        chapterHideUntilUnlocked.keySet().removeIf(chapter -> !chapterOrder.contains(chapter));
        canvasExclusiveChoicesByChapter.keySet().removeIf(chapter -> !chapterOrder.contains(chapter));
        canvasImagesByChapter.keySet().removeIf(chapter -> !chapterOrder.contains(chapter));
        canvasTextsByChapter.keySet().removeIf(chapter -> !chapterOrder.contains(chapter));
        canvasLayerOrderByChapter.keySet().removeIf(chapter -> !chapterOrder.contains(chapter));
        for (String chapter : chapterOrder) {
            chapterIcons.putIfAbsent(chapter, "");
            chapterBackgrounds.putIfAbsent(chapter, "default");
            chapterCanvasBackgrounds.putIfAbsent(chapter, "default");
            chapterTextAlign.putIfAbsent(chapter, "center");
            chapterTextColor.putIfAbsent(chapter, TabletColors.WHITE);
            chapterTextStyle.putIfAbsent(chapter, "normal");
            chapterTextSize.putIfAbsent(chapter, CanvasTextLayer.DEFAULT_FONT_SIZE);
            chapterLockUntilUnlocked.putIfAbsent(chapter, false);
            chapterHideUntilUnlocked.putIfAbsent(chapter, false);
            canvasExclusiveChoicesByChapter.putIfAbsent(chapter, List.of());
            canvasImagesByChapter.putIfAbsent(chapter, List.of());
            canvasTextsByChapter.putIfAbsent(chapter, List.of());
            canvasLayerOrderByChapter.putIfAbsent(chapter, List.of());
        }
    }

    String ensureChapter(String name) {
        String normalized = normalizeChapterName(name);
        if (normalized.isBlank()) {
            return "";
        }
        if (!chapterOrder.contains(normalized)) {
            chapterOrder.add(normalized);
        }
        reconcile(Set.of());
        return normalized;
    }

    static <T> void putOrRemove(Map<String, List<T>> target, String chapter, List<T> values) {
        if (values == null || values.isEmpty()) {
            target.remove(chapter);
        } else {
            target.put(chapter, List.copyOf(values));
        }
    }

    static <T> Map<String, List<T>> copyLayerMap(Map<String, List<T>> source) {
        Map<String, List<T>> copy = new HashMap<>();
        for (Map.Entry<String, List<T>> entry : source.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(copy);
    }

    private static <T> void moveValue(Map<String, T> map, String from, String to) {
        if (!map.containsKey(from) || map.containsKey(to)) {
            return;
        }
        map.put(to, map.remove(from));
    }

    static String normalizeChapterName(String name) {
        return QuestIdentity.chapterName(name);
    }
}
