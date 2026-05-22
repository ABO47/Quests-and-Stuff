package com.abo47.questsandstuff.client.canvas.render;

import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CanvasLayerOrdering {
    public static final String QUEST_PREFIX = "quest:";
    public static final String IMAGE_PREFIX = "image:";
    public static final String TEXT_PREFIX = "text:";

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
        if (group == null || group.isBlank()) {
            return List.of();
        }
        Set<String> valid = new HashSet<>();
        List<String> defaults = new ArrayList<>();
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
            state.canvasLayerOrderByGroup.put(group, defaults);
            return defaults;
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
        state.canvasLayerOrderByGroup.put(group, normalized);
        return normalized;
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

    public static int layerIndex(List<String> order, String key) {
        int index = order.indexOf(key);
        return index < 0 ? Integer.MAX_VALUE : index;
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

    private static void moveLayer(TabletUiState state, String group, String key, boolean front) {
        if (group == null || group.isBlank() || key == null || key.isBlank()) {
            return;
        }
        List<String> order = new ArrayList<>(state.canvasLayerOrderByGroup.getOrDefault(group, List.of()));
        order.remove(key);
        if (front) {
            order.add(key);
        } else {
            order.add(0, key);
        }
        state.canvasLayerOrderByGroup.put(group, order);
    }
}
