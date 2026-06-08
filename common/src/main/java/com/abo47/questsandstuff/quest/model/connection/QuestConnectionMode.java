package com.abo47.questsandstuff.quest.model.connection;

public enum QuestConnectionMode {
    DIRECT("direct"),
    GRID("grid");

    private final String serializedName;

    QuestConnectionMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public boolean storedInQuestMetadata() {
        return this == GRID;
    }

    public static QuestConnectionMode fromSerializedName(String value) {
        String normalized = value == null ? "" : value.trim();
        return GRID.serializedName.equalsIgnoreCase(normalized) ? GRID : DIRECT;
    }
}
