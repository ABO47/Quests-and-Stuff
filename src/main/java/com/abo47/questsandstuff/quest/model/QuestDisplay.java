package com.abo47.questsandstuff.quest.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;

public record QuestDisplay(
        String title,
        String subtitle,
        List<String> description,
        Map<String, ChapterDefinition> groups,
        String icon,
        String iconBackground,
        String completionSound,
        boolean visualHidden
) {
    public static final String DEFAULT_COMPLETION_SOUND = "minecraft:ui.toast.challenge_complete";

    public static final QuestDisplay DEFAULT = new QuestDisplay(
            "Untitled Quest",
            "",
            List.of(),
            Map.of(),
            "minecraft:book",
            "minecraft:barrier",
            DEFAULT_COMPLETION_SOUND,
            false
    );

    public QuestDisplay(
            String title,
            String subtitle,
            List<String> description,
            Map<String, ChapterDefinition> groups,
            String icon,
            String iconBackground
    ) {
        this(title, subtitle, description, groups, icon, iconBackground, DEFAULT_COMPLETION_SOUND, false);
    }

    public static final Codec<QuestDisplay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("title").orElse("Untitled Quest").forGetter(QuestDisplay::title),
            Codec.STRING.fieldOf("subtitle").orElse("").forGetter(QuestDisplay::subtitle),
            Codec.STRING.listOf().fieldOf("description").orElse(List.of()).forGetter(QuestDisplay::description),
            Codec.unboundedMap(Codec.STRING, ChapterDefinition.CODEC).fieldOf("groups").orElse(Map.of()).forGetter(QuestDisplay::groups),
            Codec.STRING.fieldOf("icon").orElse("minecraft:book").forGetter(QuestDisplay::icon),
            Codec.STRING.fieldOf("icon_background").orElse("minecraft:barrier").forGetter(QuestDisplay::iconBackground),
            Codec.STRING.fieldOf("completion_sound").orElse(DEFAULT_COMPLETION_SOUND).forGetter(QuestDisplay::completionSound),
            Codec.BOOL.fieldOf("visual_hidden").orElse(false).forGetter(QuestDisplay::visualHidden)
    ).apply(instance, QuestDisplay::new));
}
