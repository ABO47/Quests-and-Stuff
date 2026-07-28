package com.abo47.questsandstuff.quest.model.connection;

import com.abo47.questsandstuff.util.naming.QuestIdentity;

public record QuestConnectionMetadata(
        String sourceQuestId,
        String targetQuestId,
        int color,
        QuestConnectionMode mode,
        boolean hidden,
        String texture,
        int textureSpacing
) {
    private static final String CONNECTION_SEPARATOR = "->";

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

    public static String connectionKey(String sourceQuestId, String targetQuestId) {
        return normalizeQuestId(sourceQuestId) + CONNECTION_SEPARATOR + normalizeQuestId(targetQuestId);
    }

    public static String metadataKey(String sourceQuestId) {
        return normalizeQuestId(sourceQuestId);
    }

    public static String sourceQuestId(String connectionKey) {
        if (connectionKey == null) {
            return "";
        }
        int separator = connectionKey.indexOf(CONNECTION_SEPARATOR);
        if (separator <= 0) {
            return "";
        }
        return normalizeQuestId(connectionKey.substring(0, separator));
    }

    public static String targetQuestId(String connectionKey) {
        if (connectionKey == null) {
            return "";
        }
        int separator = connectionKey.indexOf(CONNECTION_SEPARATOR);
        if (separator < 0 || separator + CONNECTION_SEPARATOR.length() >= connectionKey.length()) {
            return "";
        }
        return normalizeQuestId(connectionKey.substring(separator + CONNECTION_SEPARATOR.length()));
    }

    public static boolean isValidConnectionKey(String connectionKey) {
        return !sourceQuestId(connectionKey).isBlank() && !targetQuestId(connectionKey).isBlank();
    }

    public static String normalizeQuestId(String questId) {
        return QuestIdentity.questId(questId);
    }

    public String connectionKey() {
        return connectionKey(sourceQuestId, targetQuestId);
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
