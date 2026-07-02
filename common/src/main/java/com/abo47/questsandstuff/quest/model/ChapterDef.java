package com.abo47.questsandstuff.quest.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ChapterDef(
        boolean visible,
        int x,
        int y,
        float scale
) {
    public static final ChapterDef DEFAULT = new ChapterDef(true, 0, 0, 1.0f);

    public static final Codec<ChapterDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("visible").orElse(true).forGetter(ChapterDef::visible),
            Codec.INT.fieldOf("x").orElse(0).forGetter(ChapterDef::x),
            Codec.INT.fieldOf("y").orElse(0).forGetter(ChapterDef::y),
            Codec.FLOAT.fieldOf("scale").orElse(1.0f).forGetter(ChapterDef::scale)
    ).apply(instance, ChapterDef::new));
}
