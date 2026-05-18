package com.abo47.questsandstuff.quest.model.task;

import com.mojang.serialization.Codec;

public enum QuestVisibilityMode {
    LOCKED("locked"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    PREREQUISITES_VISIBLE("prerequisites_visible");

    public static final Codec<QuestVisibilityMode> CODEC = Codec.STRING.xmap(
            QuestVisibilityMode::fromSerializedName,
            QuestVisibilityMode::serializedName
    );

    private final String serializedName;

    QuestVisibilityMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public static QuestVisibilityMode fromSerializedName(String value) {
        for (QuestVisibilityMode mode : values()) {
            if (mode.serializedName.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return LOCKED;
    }
}
