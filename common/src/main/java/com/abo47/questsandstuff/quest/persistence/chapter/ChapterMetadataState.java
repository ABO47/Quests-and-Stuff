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
    final List<String> groupOrder = new ArrayList<>();
    final Map<String, String> groupIcons = new HashMap<>();
    final Map<String, String> groupBackgrounds = new HashMap<>();
    final Map<String, String> groupCanvasBackgrounds = new HashMap<>();
    final Map<String, String> groupTextAlign = new HashMap<>();
    final Map<String, Integer> groupTextColor = new HashMap<>();
    final Map<String, String> groupTextStyle = new HashMap<>();
    final Map<String, Integer> groupTextSize = new HashMap<>();
    final Map<String, Boolean> groupLockUntilUnlocked = new HashMap<>();
    final Map<String, Boolean> groupHideUntilUnlocked = new HashMap<>();
    final Map<String, List<CanvasExclusiveChoice>> canvasExclusiveChoicesByGroup = new HashMap<>();
    final Map<String, List<CanvasImageLayer>> canvasImagesByGroup = new HashMap<>();
    final Map<String, List<CanvasTextLayer>> canvasTextsByGroup = new HashMap<>();
    final Map<String, List<String>> canvasLayerOrderByGroup = new HashMap<>();

    void clear() {
        groupOrder.clear();
        groupIcons.clear();
        groupBackgrounds.clear();
        groupCanvasBackgrounds.clear();
        groupTextAlign.clear();
        groupTextColor.clear();
        groupTextStyle.clear();
        groupTextSize.clear();
        groupLockUntilUnlocked.clear();
        groupHideUntilUnlocked.clear();
        canvasExclusiveChoicesByGroup.clear();
        canvasImagesByGroup.clear();
        canvasTextsByGroup.clear();
        canvasLayerOrderByGroup.clear();
    }

    void setGroupOrder(List<String> groups, Set<String> discoveredGroups) {
        groupOrder.clear();
        if (groups != null) {
            for (String group : groups) {
                String normalized = normalizeGroupName(group);
                if (normalized.isBlank()) {
                    continue;
                }
                if (!groupOrder.contains(normalized)) {
                    groupOrder.add(normalized);
                }
            }
        }
        reconcile(discoveredGroups);
    }

    void renameGroup(String fromName, String toName) {
        String from = normalizeGroupName(fromName);
        String to = normalizeGroupName(toName);
        if (from.isBlank() || to.isBlank() || from.equals(to)) {
            return;
        }
        for (int i = 0; i < groupOrder.size(); i++) {
            if (from.equals(groupOrder.get(i))) {
                groupOrder.set(i, to);
                break;
            }
        }
        moveValue(groupIcons, from, to);
        moveValue(groupBackgrounds, from, to);
        moveValue(groupCanvasBackgrounds, from, to);
        moveValue(groupTextAlign, from, to);
        moveValue(groupTextColor, from, to);
        moveValue(groupTextStyle, from, to);
        moveValue(groupTextSize, from, to);
        moveValue(groupLockUntilUnlocked, from, to);
        moveValue(groupHideUntilUnlocked, from, to);
        moveValue(canvasExclusiveChoicesByGroup, from, to);
        moveValue(canvasImagesByGroup, from, to);
        moveValue(canvasTextsByGroup, from, to);
        moveValue(canvasLayerOrderByGroup, from, to);
    }

    String groupIcon(String group) {
        return groupIcons.getOrDefault(group, "");
    }

    String groupBackground(String group) {
        return groupBackgrounds.getOrDefault(group, "default");
    }

    String groupCanvasBackground(String group) {
        return groupCanvasBackgrounds.getOrDefault(group, "default");
    }

    String groupTextAlign(String group) {
        return groupTextAlign.getOrDefault(group, "center");
    }

    int groupTextColor(String group) {
        return groupTextColor.getOrDefault(group, 0xFFFFFFFF);
    }

    String groupTextStyle(String group) {
        return groupTextStyle.getOrDefault(group, "normal");
    }

    int groupTextSize(String group) {
        return groupTextSize.getOrDefault(group, CanvasTextLayer.DEFAULT_FONT_SIZE);
    }

    boolean groupLockUntilUnlocked(String group) {
        return groupLockUntilUnlocked.getOrDefault(group, false);
    }

    boolean groupHideUntilUnlocked(String group) {
        return groupHideUntilUnlocked.getOrDefault(group, false);
    }

    void reconcile(Set<String> discoveredGroups) {
        Set<String> discovered = discoveredGroups == null ? Set.of() : discoveredGroups;
        groupOrder.removeIf(group -> group == null || group.isBlank());
        for (String group : discovered) {
            if (!groupOrder.contains(group)) {
                groupOrder.add(group);
            }
        }
        groupIcons.keySet().removeIf(group -> !groupOrder.contains(group));
        groupBackgrounds.keySet().removeIf(group -> !groupOrder.contains(group));
        groupCanvasBackgrounds.keySet().removeIf(group -> !groupOrder.contains(group));
        groupTextAlign.keySet().removeIf(group -> !groupOrder.contains(group));
        groupTextColor.keySet().removeIf(group -> !groupOrder.contains(group));
        groupTextStyle.keySet().removeIf(group -> !groupOrder.contains(group));
        groupTextSize.keySet().removeIf(group -> !groupOrder.contains(group));
        groupLockUntilUnlocked.keySet().removeIf(group -> !groupOrder.contains(group));
        groupHideUntilUnlocked.keySet().removeIf(group -> !groupOrder.contains(group));
        canvasExclusiveChoicesByGroup.keySet().removeIf(group -> !groupOrder.contains(group));
        canvasImagesByGroup.keySet().removeIf(group -> !groupOrder.contains(group));
        canvasTextsByGroup.keySet().removeIf(group -> !groupOrder.contains(group));
        canvasLayerOrderByGroup.keySet().removeIf(group -> !groupOrder.contains(group));
        for (String group : groupOrder) {
            groupIcons.putIfAbsent(group, "");
            groupBackgrounds.putIfAbsent(group, "default");
            groupCanvasBackgrounds.putIfAbsent(group, "default");
            groupTextAlign.putIfAbsent(group, "center");
            groupTextColor.putIfAbsent(group, 0xFFFFFFFF);
            groupTextStyle.putIfAbsent(group, "normal");
            groupTextSize.putIfAbsent(group, CanvasTextLayer.DEFAULT_FONT_SIZE);
            groupLockUntilUnlocked.putIfAbsent(group, false);
            groupHideUntilUnlocked.putIfAbsent(group, false);
            canvasExclusiveChoicesByGroup.putIfAbsent(group, List.of());
            canvasImagesByGroup.putIfAbsent(group, List.of());
            canvasTextsByGroup.putIfAbsent(group, List.of());
            canvasLayerOrderByGroup.putIfAbsent(group, List.of());
        }
    }

    String ensureGroup(String group) {
        String normalized = normalizeGroupName(group);
        if (normalized.isBlank()) {
            return "";
        }
        if (!groupOrder.contains(normalized)) {
            groupOrder.add(normalized);
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

    static String normalizeGroupName(String name) {
        return QuestIdentity.groupName(name);
    }
}
