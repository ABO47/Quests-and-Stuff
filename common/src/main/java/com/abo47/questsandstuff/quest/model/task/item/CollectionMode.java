package com.abo47.questsandstuff.quest.model.task.item;

import java.util.Locale;

import com.mojang.serialization.Codec;

public enum CollectionMode {
    AUTOMATIC,
    MANUAL,
    CONSUME;

    public static final Codec<CollectionMode> CODEC = Codec.STRING.xmap(CollectionMode::fromWire, CollectionMode::wireName);

    public String wireName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static CollectionMode fromWire(String value) {
        if (value == null) {
            return AUTOMATIC;
        }
        try {
            return CollectionMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return AUTOMATIC;
        }
    }
}
