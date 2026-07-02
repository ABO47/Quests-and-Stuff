package com.abo47.questsandstuff.quest.persistence.chapter;

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

    void setChapterOrder(List<String> groups, Set<String> discoveredGroups) {
        chapterOrder.clear();
        if (groups != null) {
            for (String group : groups) {
                String normalized = normalizeChapterName(group);
                if (normalized.isBlank()) {
                    continue;
                }
                if (!chapterOrder.contains(normalized)) {
                    chapterOrder.add(normalized);
                }
            }
        }
        reconcile(discoveredGroups);
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

    String chapterIcon(String group) {
        return chapterIcons.getOrDefault(group, "");
    }

    String chapterBackground(String group) {
        return chapterBackgrounds.getOrDefault(group, "default");
    }

    String chapterCanvasBackground(String group) {
        return chapterCanvasBackgrounds.getOrDefault(group, "default");
    }

    String chapterTextAlign(String group) {
        return chapterTextAlign.getOrDefault(group, "center");
    }

    int chapterTextColor(String group) {
        return chapterTextColor.getOrDefault(group, 0xFFFFFFFF);
    }

    String chapterTextStyle(String group) {
        return chapterTextStyle.getOrDefault(group, "normal");
    }

    int chapterTextSize(String group) {
        return chapterTextSize.getOrDefault(group, CanvasTextLayer.DEFAULT_FONT_SIZE);
    }

    boolean chapterLockUntilUnlocked(String group) {
        return chapterLockUntilUnlocked.getOrDefault(group, false);
    }

    boolean chapterHideUntilUnlocked(String group) {
        return chapterHideUntilUnlocked.getOrDefault(group, false);
    }

    void reconcile(Set<String> discoveredGroups) {
        Set<String> discovered = discoveredGroups == null ? Set.of() : discoveredGroups;
        chapterOrder.removeIf(group -> group == null || group.isBlank());
        for (String group : discovered) {
            if (!chapterOrder.contains(group)) {
                chapterOrder.add(group);
            }
        }
        chapterIcons.keySet().removeIf(group -> !chapterOrder.contains(group));
        chapterBackgrounds.keySet().removeIf(group -> !chapterOrder.contains(group));
        chapterCanvasBackgrounds.keySet().removeIf(group -> !chapterOrder.contains(group));
        chapterTextAlign.keySet().removeIf(group -> !chapterOrder.contains(group));
        chapterTextColor.keySet().removeIf(group -> !chapterOrder.contains(group));
        chapterTextStyle.keySet().removeIf(group -> !chapterOrder.contains(group));
        chapterTextSize.keySet().removeIf(group -> !chapterOrder.contains(group));
        chapterLockUntilUnlocked.keySet().removeIf(group -> !chapterOrder.contains(group));
        chapterHideUntilUnlocked.keySet().removeIf(group -> !chapterOrder.contains(group));
        canvasExclusiveChoicesByChapter.keySet().removeIf(group -> !chapterOrder.contains(group));
        canvasImagesByChapter.keySet().removeIf(group -> !chapterOrder.contains(group));
        canvasTextsByChapter.keySet().removeIf(group -> !chapterOrder.contains(group));
        canvasLayerOrderByChapter.keySet().removeIf(group -> !chapterOrder.contains(group));
        for (String group : chapterOrder) {
            chapterIcons.putIfAbsent(group, "");
            chapterBackgrounds.putIfAbsent(group, "default");
            chapterCanvasBackgrounds.putIfAbsent(group, "default");
            chapterTextAlign.putIfAbsent(group, "center");
            chapterTextColor.putIfAbsent(group, 0xFFFFFFFF);
            chapterTextStyle.putIfAbsent(group, "normal");
            chapterTextSize.putIfAbsent(group, CanvasTextLayer.DEFAULT_FONT_SIZE);
            chapterLockUntilUnlocked.putIfAbsent(group, false);
            chapterHideUntilUnlocked.putIfAbsent(group, false);
            canvasExclusiveChoicesByChapter.putIfAbsent(group, List.of());
            canvasImagesByChapter.putIfAbsent(group, List.of());
            canvasTextsByChapter.putIfAbsent(group, List.of());
            canvasLayerOrderByChapter.putIfAbsent(group, List.of());
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

    static <T> void putOrRemove(Map<String, List<T>> target, String group, List<T> values) {
        if (values == null || values.isEmpty()) {
            target.remove(group);
        } else {
            target.put(group, List.copyOf(values));
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
