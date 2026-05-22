package com.abo47.questsandstuff.quest.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ChapterDefinition(
        boolean visible,
        int x,
        int y,
        float scale
) {
    public static final ChapterDefinition DEFAULT = new ChapterDefinition(true, 0, 0, 1.0f);

    public static final Codec<ChapterDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("visible").orElse(true).forGetter(ChapterDefinition::visible),
            Codec.INT.fieldOf("x").orElse(0).forGetter(ChapterDefinition::x),
            Codec.INT.fieldOf("y").orElse(0).forGetter(ChapterDefinition::y),
            Codec.FLOAT.fieldOf("scale").orElse(1.0f).forGetter(ChapterDefinition::scale)
    ).apply(instance, ChapterDefinition::new));
}
