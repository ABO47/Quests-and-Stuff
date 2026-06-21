package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
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
    public static final String EXCLUSIVE_CHOICE_PREFIX = "exclusive_choice:";
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
        List<String> order = new ArrayList<>(state.canvas.canvasLayerOrderByGroup.getOrDefault(group, List.of()));
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
        state.canvas.canvasLayerOrderByGroup.put(group, keepConnectionsBehindQuests(order));
    }

    public static List<String> normalize(
            TabletUiState state,
            String group,
            List<QuestCardLayout> cards,
            List<CanvasImageLayer> images,
            List<CanvasTextLayer> texts,
            List<String> connectionKeys,
            List<CanvasExclusiveChoice> exclusiveChoices
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
        for (CanvasExclusiveChoice ec : exclusiveChoices) {
            String key = exclusiveChoiceKey(ec.id());
            if (valid.add(key)) {
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
        List<String> existing = state.canvas.canvasLayerOrderByGroup.get(group);
        if (existing == null || existing.isEmpty()) {
            List<String> orderedDefaults = keepConnectionsBehindQuests(defaults);
            state.canvas.canvasLayerOrderByGroup.put(group, orderedDefaults);
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
        state.canvas.canvasLayerOrderByGroup.put(group, ordered);
        return ordered;
    }

    public static CanvasLayerOrder normalizedOrder(
            TabletUiState state,
            String group,
            List<QuestCardLayout> cards,
            List<CanvasImageLayer> images,
            List<CanvasTextLayer> texts,
            List<String> connectionKeys
    ) {
        return normalizedOrder(state, group, cards, images, texts, connectionKeys, List.of());
    }

    public static CanvasLayerOrder normalizedOrder(
            TabletUiState state,
            String group,
            List<QuestCardLayout> cards,
            List<CanvasImageLayer> images,
            List<CanvasTextLayer> texts,
            List<String> connectionKeys,
            List<CanvasExclusiveChoice> exclusiveChoices
    ) {
        return order(normalize(state, group, cards, images, texts, connectionKeys, exclusiveChoices));
    }

    public static CanvasLayerOrder order(List<String> orderKeys) {
        return CanvasLayerOrder.fromOrderKeys(orderKeys);
    }

    public static CanvasLayerHit resolveElementHit(List<String> orderKeys, QuestCardLayout quest, CanvasImageLayer image, CanvasTextLayer text) {
        return order(orderKeys).resolveElementHit(quest, image, text, null);
    }

    public static CanvasLayerHit resolveElementHit(List<String> orderKeys, QuestCardLayout quest, CanvasImageLayer image, CanvasTextLayer text, CanvasExclusiveChoice exclusiveChoice) {
        return order(orderKeys).resolveElementHit(quest, image, text, exclusiveChoice);
    }

    public static void moveExclusiveChoiceLayer(TabletUiState state, String group, String ecId, boolean front) {
        moveLayer(state, group, exclusiveChoiceKey(ecId), front);
    }

    public static void ensurePresent(TabletUiState state, String group, String key) {
        if (group == null || group.isBlank() || key == null || key.isBlank()) {
            return;
        }
        List<String> order = new ArrayList<>(state.canvas.canvasLayerOrderByGroup.getOrDefault(group, List.of()));
        if (!order.contains(key)) {
            order.add(key);
            state.canvas.canvasLayerOrderByGroup.put(group, order);
        }
    }

    public static void remove(TabletUiState state, String group, String key) {
        if (group == null || group.isBlank() || key == null || key.isBlank()) {
            return;
        }
        List<String> order = new ArrayList<>(state.canvas.canvasLayerOrderByGroup.getOrDefault(group, List.of()));
        if (order.remove(key)) {
            if (order.isEmpty()) {
                state.canvas.canvasLayerOrderByGroup.remove(group);
            } else {
                state.canvas.canvasLayerOrderByGroup.put(group, order);
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
        return CanvasLayerKey.quest(questId).orderKey();
    }

    public static String imageKey(String imageId) {
        return CanvasLayerKey.image(imageId).orderKey();
    }

    public static String textKey(String textId) {
        return CanvasLayerKey.text(textId).orderKey();
    }

    public static String exclusiveChoiceKey(String ecId) {
        return CanvasLayerKey.exclusiveChoice(ecId).orderKey();
    }

    public static String connectionKey(String edgeId) {
        return CanvasLayerKey.connection(edgeId).orderKey();
    }

    private static void moveLayer(TabletUiState state, String group, String key, boolean front) {
        if (group == null || group.isBlank() || key == null || key.isBlank()) {
            return;
        }
        List<String> order = new ArrayList<>(state.canvas.canvasLayerOrderByGroup.getOrDefault(group, List.of()));
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
        state.canvas.canvasLayerOrderByGroup.put(group, order);
    }

    private static List<String> keepConnectionsBehindQuests(List<String> order) {
        if (order == null || order.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        List<String> connections = new ArrayList<>();
        for (String key : order) {
            if (key == null || key.isBlank()) {
                continue;
            }
            if (key.startsWith(CONNECTION_PREFIX)) {
                connections.add(key);
            } else {
                result.add(key);
            }
        }
        if (connections.isEmpty()) {
            return order;
        }
        connections.addAll(result);
        return connections;
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
