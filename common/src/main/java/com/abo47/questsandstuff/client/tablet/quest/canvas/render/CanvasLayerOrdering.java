package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CanvasLayerOrdering {
    public static final String QUEST_PREFIX = "quest:";
    public static final String IMAGE_PREFIX = "image:";
    public static final String TEXT_PREFIX = "text:";
    public static final String CONNECTION_PREFIX = "connection:";

    private CanvasLayerOrdering() {
    }

    public static void moveQuestLayer(TabletUiState state, String group, String questId, boolean front) {
        moveLayer(state, group, questKey(questId), front);
    }

    public static void moveImageLayer(TabletUiState state, String group, String imageId, boolean front) {
        moveLayer(state, group, imageKey(imageId), front);
    }

    public static void moveTextLayer(TabletUiState state, String group, String textId, boolean front) {
        moveLayer(state, group, textKey(textId), front);
    }

    public static void moveConnectionLayer(TabletUiState state, String group, String edgeId, boolean front) {
        moveLayer(state, group, connectionKey(edgeId), front);
    }

    public static void moveLayers(TabletUiState state, String group, List<String> keys, boolean front) {
        if (group == null || group.isBlank() || keys == null || keys.isEmpty()) {
            return;
        }
        Set<String> selected = new LinkedHashSet<>();
        for (String key : keys) {
            if (key != null && !key.isBlank()) {
                selected.add(key);
            }
        }
        if (selected.isEmpty()) {
            return;
        }
        List<String> order = new ArrayList<>(state.canvasLayerOrderByGroup.getOrDefault(group, List.of()));
        for (String key : selected) {
            if (!order.contains(key)) {
                order.add(key);
            }
        }
        List<String> moved = new ArrayList<>();
        order.removeIf(key -> {
            boolean hit = selected.contains(key);
            if (hit) {
                moved.add(key);
            }
            return hit;
        });
        if (front) {
            order.addAll(moved);
        } else {
            order.addAll(0, moved);
        }
        state.canvasLayerOrderByGroup.put(group, keepConnectionsBehindQuests(order));
    }

    public static boolean isImageAboveQuest(TabletUiState state, String group, String imageId, String questId) {
        List<String> order = state.canvasLayerOrderByGroup.get(group);
        if (order == null || order.isEmpty()) {
            return false;
        }
        int imageIndex = order.indexOf(imageKey(imageId));
        int questIndex = order.indexOf(questKey(questId));
        return imageIndex >= 0 && questIndex >= 0 && imageIndex > questIndex;
    }

    public static boolean isTextAboveQuest(TabletUiState state, String group, String textId, String questId) {
        List<String> order = state.canvasLayerOrderByGroup.get(group);
        if (order == null || order.isEmpty()) {
            return false;
        }
        int textIndex = order.indexOf(textKey(textId));
        int questIndex = order.indexOf(questKey(questId));
        return textIndex >= 0 && questIndex >= 0 && textIndex > questIndex;
    }

    public static boolean isTextAboveImage(TabletUiState state, String group, String textId, String imageId) {
        List<String> order = state.canvasLayerOrderByGroup.get(group);
        if (order == null || order.isEmpty()) {
            return true;
        }
        int textIndex = order.indexOf(textKey(textId));
        int imageIndex = order.indexOf(imageKey(imageId));
        return textIndex >= 0 && imageIndex >= 0 && textIndex > imageIndex;
    }

    public static List<String> normalize(
            TabletUiState state,
            String group,
            List<QuestCardLayout> cards,
            List<CanvasImageLayer> images,
            List<CanvasTextLayer> texts
    ) {
        return normalize(state, group, cards, images, texts, List.of());
    }

    public static List<String> normalize(
            TabletUiState state,
            String group,
            List<QuestCardLayout> cards,
            List<CanvasImageLayer> images,
            List<CanvasTextLayer> texts,
            List<String> connectionKeys
    ) {
        if (group == null || group.isBlank()) {
            return List.of();
        }
        Set<String> valid = new HashSet<>();
        List<String> defaults = new ArrayList<>();
        for (String key : connectionKeys) {
            if (key != null && key.startsWith(CONNECTION_PREFIX) && valid.add(key)) {
                defaults.add(key);
            }
        }
        for (CanvasImageLayer image : images) {
            String key = imageKey(image.id());
            if (valid.add(key)) {
                defaults.add(key);
            }
        }
        for (CanvasTextLayer text : texts) {
            String key = textKey(text.id());
            if (valid.add(key)) {
                defaults.add(key);
            }
        }
        for (QuestCardLayout card : cards) {
            String key = questKey(card.questId());
            if (valid.add(key)) {
                defaults.add(key);
            }
        }
        List<String> existing = state.canvasLayerOrderByGroup.get(group);
        if (existing == null || existing.isEmpty()) {
            List<String> orderedDefaults = keepConnectionsBehindQuests(defaults);
            state.canvasLayerOrderByGroup.put(group, orderedDefaults);
            return orderedDefaults;
        }
        List<String> normalized = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String key : existing) {
            if (valid.contains(key) && seen.add(key)) {
                normalized.add(key);
            }
        }
        for (String key : defaults) {
            if (seen.add(key)) {
                normalized.add(key);
            }
        }
        List<String> ordered = keepConnectionsBehindQuests(normalized);
        state.canvasLayerOrderByGroup.put(group, ordered);
        return ordered;
    }

    public static void ensurePresent(TabletUiState state, String group, String key) {
        if (group == null || group.isBlank() || key == null || key.isBlank()) {
            return;
        }
        List<String> order = new ArrayList<>(state.canvasLayerOrderByGroup.getOrDefault(group, List.of()));
        if (!order.contains(key)) {
            order.add(key);
            state.canvasLayerOrderByGroup.put(group, order);
        }
    }

    public static void remove(TabletUiState state, String group, String key) {
        if (group == null || group.isBlank() || key == null || key.isBlank()) {
            return;
        }
        List<String> order = new ArrayList<>(state.canvasLayerOrderByGroup.getOrDefault(group, List.of()));
        if (order.remove(key)) {
            if (order.isEmpty()) {
                state.canvasLayerOrderByGroup.remove(group);
            } else {
                state.canvasLayerOrderByGroup.put(group, order);
            }
        }
    }

    public static Map<String, Integer> indexMap(List<String> order) {
        if (order == null || order.isEmpty()) {
            return Map.of();
        }
        Map<String, Integer> indexes = new HashMap<>();
        for (int i = 0; i < order.size(); i++) {
            String key = order.get(i);
            if (key != null && !key.isBlank()) {
                indexes.putIfAbsent(key, i);
            }
        }
        return indexes;
    }

    public static int layerIndex(Map<String, Integer> indexes, String key) {
        if (indexes == null || indexes.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        return indexes.getOrDefault(key, Integer.MAX_VALUE);
    }

    public static String questKey(String questId) {
        return QUEST_PREFIX + questId;
    }

    public static String imageKey(String imageId) {
        return IMAGE_PREFIX + imageId;
    }

    public static String textKey(String textId) {
        return TEXT_PREFIX + textId;
    }

    public static String connectionKey(String edgeId) {
        return CONNECTION_PREFIX + edgeId;
    }

    private static void moveLayer(TabletUiState state, String group, String key, boolean front) {
        if (group == null || group.isBlank() || key == null || key.isBlank()) {
            return;
        }
        List<String> order = new ArrayList<>(state.canvasLayerOrderByGroup.getOrDefault(group, List.of()));
        order.remove(key);
        if (front && key.startsWith(CONNECTION_PREFIX)) {
            int firstQuestIndex = firstQuestIndex(order);
            if (firstQuestIndex < 0) {
                order.add(key);
            } else {
                order.add(firstQuestIndex, key);
            }
        } else if (front) {
            order.add(key);
        } else {
            order.add(0, key);
        }
        state.canvasLayerOrderByGroup.put(group, order);
    }

    private static List<String> keepConnectionsBehindQuests(List<String> order) {
        if (order == null || order.isEmpty()) {
            return List.of();
        }
        int firstQuestIndex = -1;
        List<String> result = new ArrayList<>();
        List<String> delayedConnections = new ArrayList<>();
        for (String key : order) {
            if (key == null || key.isBlank()) {
                continue;
            }
            if (firstQuestIndex >= 0 && key.startsWith(CONNECTION_PREFIX)) {
                delayedConnections.add(key);
                continue;
            }
            if (firstQuestIndex < 0 && key.startsWith(QUEST_PREFIX)) {
                firstQuestIndex = result.size();
            }
            result.add(key);
        }
        if (delayedConnections.isEmpty()) {
            return order;
        }
        if (firstQuestIndex < 0) {
            result.addAll(delayedConnections);
        } else {
            result.addAll(firstQuestIndex, delayedConnections);
        }
        return result;
    }

    private static int firstQuestIndex(List<String> order) {
        if (order == null || order.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < order.size(); i++) {
            String key = order.get(i);
            if (key != null && key.startsWith(QUEST_PREFIX)) {
                return i;
            }
        }
        return -1;
    }
}
