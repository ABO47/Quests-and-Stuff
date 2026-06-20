package com.abo47.questsandstuff.quest.model.canvas;

import java.util.ArrayList;
import java.util.List;

public record CanvasExclusiveChoice(String id, int x, int y, int w, int h, int rotation, List<String> connectionQuestIds, List<String> prerequisiteQuestIds, String background) {
    public static final int DEFAULT_WIDTH = 79;
    public static final int DEFAULT_HEIGHT = 79;
    public static final String DEFAULT_BACKGROUND = "";

    public CanvasExclusiveChoice(String id, int x, int y, int w, int h, int rotation) {
        this(id, x, y, w, h, rotation, List.of(), List.of(), "");
    }

    public CanvasExclusiveChoice(String id, int x, int y, int w, int h, int rotation, List<String> connectionQuestIds) {
        this(id, x, y, w, h, rotation, connectionQuestIds, List.of(), "");
    }

    public CanvasExclusiveChoice(String id, int x, int y, int w, int h, int rotation, List<String> connectionQuestIds, List<String> prerequisiteQuestIds) {
        this(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, "");
    }

    public CanvasExclusiveChoice {
        id = id == null ? "" : id.trim();
        w = Math.max(8, w);
        h = Math.max(8, h);
        rotation = normalizeDegrees(rotation);
        connectionQuestIds = connectionQuestIds == null ? List.of() : List.copyOf(connectionQuestIds);
        prerequisiteQuestIds = prerequisiteQuestIds == null ? List.of() : List.copyOf(prerequisiteQuestIds);
        background = background == null ? "" : background.trim();
    }

    public CanvasExclusiveChoice moveTo(int nextX, int nextY) {
        return new CanvasExclusiveChoice(id, nextX, nextY, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, background);
    }

    public CanvasExclusiveChoice resizeTo(int nextW, int nextH) {
        return new CanvasExclusiveChoice(id, x, y, Math.max(8, nextW), Math.max(8, nextH), rotation, connectionQuestIds, prerequisiteQuestIds, background);
    }

    public CanvasExclusiveChoice rotateTo(int nextRotation) {
        return new CanvasExclusiveChoice(id, x, y, w, h, nextRotation, connectionQuestIds, prerequisiteQuestIds, background);
    }

    public int pivotX() {
        return Math.max(1, w) / 2;
    }

    public int pivotY() {
        return Math.max(1, h) / 2;
    }

    public CanvasExclusiveChoice withConnections(List<String> nextConnections) {
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, nextConnections, prerequisiteQuestIds, background);
    }

    public CanvasExclusiveChoice addConnection(String questId) {
        if (questId == null || questId.isBlank()) {
            return this;
        }
        List<String> next = new ArrayList<>(connectionQuestIds);
        if (!next.contains(questId)) {
            next.add(questId);
        }
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, next, prerequisiteQuestIds, background);
    }

    public CanvasExclusiveChoice removeConnection(String questId) {
        if (questId == null || questId.isBlank()) {
            return this;
        }
        List<String> next = new ArrayList<>(connectionQuestIds);
        next.remove(questId);
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, next, prerequisiteQuestIds, background);
    }

    public CanvasExclusiveChoice addPrerequisite(String questId) {
        if (questId == null || questId.isBlank()) {
            return this;
        }
        List<String> next = new ArrayList<>(prerequisiteQuestIds);
        if (!next.contains(questId)) {
            next.add(questId);
        }
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, next, background);
    }

    public CanvasExclusiveChoice removePrerequisite(String questId) {
        if (questId == null || questId.isBlank()) {
            return this;
        }
        List<String> next = new ArrayList<>(prerequisiteQuestIds);
        next.remove(questId);
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, next, background);
    }

    public CanvasExclusiveChoice withBackground(String nextBackground) {
        return new CanvasExclusiveChoice(id, x, y, w, h, rotation, connectionQuestIds, prerequisiteQuestIds, nextBackground);
    }

    private static int normalizeDegrees(int degrees) {
        return ((degrees % 360) + 360) % 360;
    }
}
