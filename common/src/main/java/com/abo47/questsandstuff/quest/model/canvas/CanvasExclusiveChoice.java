package com.abo47.questsandstuff.quest.model.canvas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record CanvasExclusiveChoice(String id, int x, int y, int w, int h, int rotation, List<String> connectionQuestIds, List<String> prerequisiteQuestIds, String background, Map<String, Integer> connectionColors, Map<String, String> connectionModes, Map<String, String> connectionTextures, Map<String, Integer> connectionTextureSpacings, Set<String> hiddenConnections) {
    public static final int DEFAULT_WIDTH = 15;
    public static final int DEFAULT_HEIGHT = 15;
    public static final String DEFAULT_BACKGROUND = "";

    public CanvasExclusiveChoice(String id, int x, int y, int w, int h, int rotation) {
        this(id, x, y, w, h, rotation, List.of(), List.of(), "", Map.of(), Map.of(), Map.of(), Map.of(), Set.of());
    }

    public CanvasExclusiveChoice(String id, int x, int y, int w, int h, int rotation, List<String> connectionQuestIds) {
        this(id, x, y, w, h, rotation, connectionQuestIds, List.of(), "", Map.of(), Map.of(), Map.of(), Map.of(), Set.of());
    }

    public CanvasExclusiveChoice(String id, int x, int y, int w, int h, int rotation, List<String> connectionQuestIds, List<String> prerequisiteQuestIds) {
        this(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, "", Map.of(), Map.of(), Map.of(), Map.of(), Set.of());
    }

    public CanvasExclusiveChoice(String id, int x, int y, int w, int h, int rotation, List<String> connectionQuestIds, List<String> prerequisiteQuestIds, String background) {
        this(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, background, Map.of(), Map.of(), Map.of(), Map.of(), Set.of());
    }

    public CanvasExclusiveChoice(String id, int x, int y, int w, int h, int rotation, List<String> connectionQuestIds, List<String> prerequisiteQuestIds, String background, Map<String, Integer> connectionColors, Map<String, String> connectionModes) {
        this(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, background, connectionColors, connectionModes, Map.of(), Map.of(), Set.of());
    }

    public CanvasExclusiveChoice {
        id = id == null ? "" : id.trim();
        int size = Math.max(8, Math.max(w, h));
        w = size;
        h = size;
        rotation = normalizeDegrees(rotation);
        connectionQuestIds = connectionQuestIds == null ? List.of() : List.copyOf(connectionQuestIds);
        prerequisiteQuestIds = prerequisiteQuestIds == null ? List.of() : List.copyOf(prerequisiteQuestIds);
        background = background == null ? "" : background.trim();
        connectionColors = connectionColors == null ? Map.of() : Map.copyOf(connectionColors);
        connectionModes = connectionModes == null ? Map.of() : Map.copyOf(connectionModes);
        connectionTextures = connectionTextures == null ? Map.of() : Map.copyOf(connectionTextures);
        connectionTextureSpacings = connectionTextureSpacings == null ? Map.of() : Map.copyOf(connectionTextureSpacings);
        hiddenConnections = hiddenConnections == null ? Set.of() : Set.copyOf(hiddenConnections);
    }

    public CanvasExclusiveChoice moveTo(int nextX, int nextY) {
        return new CanvasExclusiveChoice(id, nextX, nextY, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, background, connectionColors, connectionModes, connectionTextures, connectionTextureSpacings, hiddenConnections);
    }

    public CanvasExclusiveChoice resizeTo(int nextW, int nextH) {
        int size = Math.max(8, Math.max(nextW, nextH));
        return new CanvasExclusiveChoice(id, x, y, size, size, rotation, connectionQuestIds, prerequisiteQuestIds, background, connectionColors, connectionModes, connectionTextures, connectionTextureSpacings, hiddenConnections);
    }

    public CanvasExclusiveChoice rotateTo(int nextRotation) {
        return new CanvasExclusiveChoice(id, x, y, w, h, nextRotation, connectionQuestIds, prerequisiteQuestIds, background, connectionColors, connectionModes, connectionTextures, connectionTextureSpacings, hiddenConnections);
    }

    public int pivotX() {
        return Math.max(1, w) / 2;
    }

    public int pivotY() {
        return Math.max(1, h) / 2;
    }

    public CanvasExclusiveChoice withConnections(List<String> nextConnections) {
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, nextConnections, prerequisiteQuestIds, background, connectionColors, connectionModes, connectionTextures, connectionTextureSpacings, hiddenConnections);
    }

    public CanvasExclusiveChoice addConnection(String questId) {
        if (questId == null || questId.isBlank()) {
            return this;
        }
        List<String> next = new ArrayList<>(connectionQuestIds);
        if (!next.contains(questId)) {
            next.add(questId);
        }
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, next, prerequisiteQuestIds, background, connectionColors, connectionModes, connectionTextures, connectionTextureSpacings, hiddenConnections);
    }

    public CanvasExclusiveChoice removeConnection(String questId) {
        if (questId == null || questId.isBlank()) {
            return this;
        }
        List<String> next = new ArrayList<>(connectionQuestIds);
        next.remove(questId);
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, next, prerequisiteQuestIds, background, connectionColors, connectionModes, connectionTextures, connectionTextureSpacings, hiddenConnections);
    }

    public CanvasExclusiveChoice addPrerequisite(String questId) {
        if (questId == null || questId.isBlank()) {
            return this;
        }
        List<String> next = new ArrayList<>(prerequisiteQuestIds);
        if (!next.contains(questId)) {
            next.add(questId);
        }
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, next, background, connectionColors, connectionModes, connectionTextures, connectionTextureSpacings, hiddenConnections);
    }

    public CanvasExclusiveChoice removePrerequisite(String questId) {
        if (questId == null || questId.isBlank()) {
            return this;
        }
        List<String> next = new ArrayList<>(prerequisiteQuestIds);
        next.remove(questId);
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, next, background, connectionColors, connectionModes, connectionTextures, connectionTextureSpacings, hiddenConnections);
    }

    public CanvasExclusiveChoice removeAllEdgeState(String questId) {
        if (questId == null || questId.isBlank()) {
            return this;
        }
        List<String> nextConnections = new ArrayList<>(connectionQuestIds);
        nextConnections.remove(questId);
        List<String> nextPrereqs = new ArrayList<>(prerequisiteQuestIds);
        nextPrereqs.remove(questId);
        Map<String, Integer> nextColors = connectionColors.containsKey(questId)
                ? withEntryRemoved(connectionColors, questId) : connectionColors;
        Map<String, String> nextModes = connectionModes.containsKey(questId)
                ? withEntryRemoved(connectionModes, questId) : connectionModes;
        Map<String, String> nextTextures = connectionTextures.containsKey(questId)
                ? withEntryRemoved(connectionTextures, questId) : connectionTextures;
        Map<String, Integer> nextSpacings = connectionTextureSpacings.containsKey(questId)
                ? withEntryRemoved(connectionTextureSpacings, questId) : connectionTextureSpacings;
        Set<String> nextHidden = hiddenConnections.contains(questId)
                ? withEntryRemoved(hiddenConnections, questId) : hiddenConnections;
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, nextConnections, nextPrereqs, background, nextColors, nextModes, nextTextures, nextSpacings, nextHidden);
    }

    private static <V> Map<String, V> withEntryRemoved(Map<String, V> map, String key) {
        Map<String, V> next = new HashMap<>(map);
        next.remove(key);
        return next;
    }

    private static Set<String> withEntryRemoved(Set<String> set, String key) {
        Set<String> next = new HashSet<>(set);
        next.remove(key);
        return next;
    }

    public CanvasExclusiveChoice withBackground(String nextBackground) {
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, nextBackground, connectionColors, connectionModes, connectionTextures, connectionTextureSpacings, hiddenConnections);
    }

    public CanvasExclusiveChoice withConnectionColor(String questId, int color) {
        Map<String, Integer> next = new HashMap<>(connectionColors);
        next.put(questId, color);
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, background, next, connectionModes, connectionTextures, connectionTextureSpacings, hiddenConnections);
    }

    public CanvasExclusiveChoice withoutConnectionColor(String questId) {
        if (!connectionColors.containsKey(questId)) return this;
        Map<String, Integer> next = new HashMap<>(connectionColors);
        next.remove(questId);
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, background, next, connectionModes, connectionTextures, connectionTextureSpacings, hiddenConnections);
    }

    public CanvasExclusiveChoice withConnectionMode(String questId, String mode) {
        Map<String, String> next = new HashMap<>(connectionModes);
        next.put(questId, mode);
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, background, connectionColors, next, connectionTextures, connectionTextureSpacings, hiddenConnections);
    }

    public CanvasExclusiveChoice withoutConnectionMode(String questId) {
        if (!connectionModes.containsKey(questId)) return this;
        Map<String, String> next = new HashMap<>(connectionModes);
        next.remove(questId);
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, background, connectionColors, next, connectionTextures, connectionTextureSpacings, hiddenConnections);
    }

    public CanvasExclusiveChoice withConnectionTextures(Map<String, String> nextTextures) {
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, background, connectionColors, connectionModes, nextTextures, connectionTextureSpacings, hiddenConnections);
    }

    public CanvasExclusiveChoice withConnectionTexture(String questId, String texture) {
        Map<String, String> next = new HashMap<>(connectionTextures);
        next.put(questId, texture == null ? "" : texture);
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, background, connectionColors, connectionModes, next, connectionTextureSpacings, hiddenConnections);
    }

    public CanvasExclusiveChoice withoutConnectionTexture(String questId) {
        if (!connectionTextures.containsKey(questId)) return this;
        Map<String, String> next = new HashMap<>(connectionTextures);
        next.remove(questId);
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, background, connectionColors, connectionModes, next, connectionTextureSpacings, hiddenConnections);
    }

    public CanvasExclusiveChoice withConnectionTextureSpacing(String questId, int spacing) {
        Map<String, Integer> next = new HashMap<>(connectionTextureSpacings);
        next.put(questId, Math.max(0, spacing));
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, background, connectionColors, connectionModes, connectionTextures, next, hiddenConnections);
    }

    public CanvasExclusiveChoice withoutConnectionTextureSpacing(String questId) {
        if (!connectionTextureSpacings.containsKey(questId)) return this;
        Map<String, Integer> next = new HashMap<>(connectionTextureSpacings);
        next.remove(questId);
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, background, connectionColors, connectionModes, connectionTextures, next, hiddenConnections);
    }

    public CanvasExclusiveChoice withHiddenConnection(String questId, boolean hidden) {
        if (hidden) {
            Set<String> next = new HashSet<>(hiddenConnections);
            next.add(questId);
            return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, background, connectionColors, connectionModes, connectionTextures, connectionTextureSpacings, next);
        }
        if (!hiddenConnections.contains(questId)) return this;
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, background, connectionColors, connectionModes, connectionTextures, connectionTextureSpacings, withEntryRemoved(hiddenConnections, questId));
    }

    public CanvasExclusiveChoice withHiddenConnections(Set<String> nextHidden) {
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, background, connectionColors, connectionModes, connectionTextures, connectionTextureSpacings, nextHidden);
    }

    private static int normalizeDegrees(int degrees) {
        return ((degrees % 360) + 360) % 360;
    }
}
