package com.abo47.questsandstuff.quest.model.task.player;

import com.mojang.serialization.Codec;

import java.util.Locale;

public enum XpMode {
    POINTS,
    LEVEL;

    public static final Codec<XpMode> CODEC = Codec.STRING.xmap(XpMode::fromWire, XpMode::wireName);

    public String wireName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static XpMode fromWire(String value) {
        if (value == null) {
            return POINTS;
        }
        try {
            return XpMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return POINTS;
        }
    }
}
