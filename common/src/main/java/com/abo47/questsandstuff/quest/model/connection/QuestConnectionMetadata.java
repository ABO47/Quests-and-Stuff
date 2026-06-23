package com.abo47.questsandstuff.quest.model.connection;

import com.abo47.questsandstuff.util.QuestIdentity;

public record QuestConnectionMetadata(
        String sourceQuestId,
        String targetQuestId,
        int color,
        QuestConnectionMode mode,
        boolean hidden,
        String texture,
        int textureSpacing
) {
    private static final String EDGE_SEPARATOR = "->";

    public QuestConnectionMetadata {
        sourceQuestId = normalizeQuestId(sourceQuestId);
        targetQuestId = normalizeQuestId(targetQuestId);
        mode = mode == null ? QuestConnectionMode.DIRECT : mode;
        texture = texture == null ? "" : texture;
        textureSpacing = Math.max(0, textureSpacing);
    }

    public static QuestConnectionMetadata direct(String sourceQuestId, String targetQuestId, int color, boolean hidden) {
        return new QuestConnectionMetadata(sourceQuestId, targetQuestId, color, QuestConnectionMode.DIRECT, hidden, "", 0);
    }

    public static QuestConnectionMetadata grid(String sourceQuestId, String targetQuestId, int color, boolean hidden) {
        return new QuestConnectionMetadata(sourceQuestId, targetQuestId, color, QuestConnectionMode.GRID, hidden, "", 0);
    }

    public static String edgeKey(String sourceQuestId, String targetQuestId) {
        return normalizeQuestId(sourceQuestId) + EDGE_SEPARATOR + normalizeQuestId(targetQuestId);
    }

    public static String metadataKey(String sourceQuestId) {
        return normalizeQuestId(sourceQuestId);
    }

    public static String sourceQuestId(String edgeKey) {
        if (edgeKey == null) {
            return "";
        }
        int separator = edgeKey.indexOf(EDGE_SEPARATOR);
        if (separator <= 0) {
            return "";
        }
        return normalizeQuestId(edgeKey.substring(0, separator));
    }

    public static String targetQuestId(String edgeKey) {
        if (edgeKey == null) {
            return "";
        }
        int separator = edgeKey.indexOf(EDGE_SEPARATOR);
        if (separator < 0 || separator + EDGE_SEPARATOR.length() >= edgeKey.length()) {
            return "";
        }
        return normalizeQuestId(edgeKey.substring(separator + EDGE_SEPARATOR.length()));
    }

    public static boolean isValidEdgeKey(String edgeKey) {
        return !sourceQuestId(edgeKey).isBlank() && !targetQuestId(edgeKey).isBlank();
    }

    public static String normalizeQuestId(String questId) {
        return QuestIdentity.questId(questId);
    }

    public String edgeKey() {
        return edgeKey(sourceQuestId, targetQuestId);
    }

    public String metadataKey() {
        return metadataKey(sourceQuestId);
    }

    public boolean direct() {
        return mode == QuestConnectionMode.DIRECT;
    }

    public boolean grid() {
        return mode == QuestConnectionMode.GRID;
    }
}
