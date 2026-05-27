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
        int completionSoundVolume,
        String completionHudBackground,
        boolean visualHidden,
        String questBackground,
        boolean questBackgroundGrayscale
) {
    public static final String DEFAULT_COMPLETION_SOUND = "minecraft:ui.toast.challenge_complete";
    public static final int DEFAULT_COMPLETION_SOUND_VOLUME = 100;
    public static final String DEFAULT_COMPLETION_HUD_BACKGROUND = "";
    public static final String DEFAULT_QUEST_BACKGROUND = "default";

    public static final QuestDisplay DEFAULT = new QuestDisplay(
            "Untitled Quest",
            "",
            List.of(),
            Map.of(),
            "minecraft:book",
            "minecraft:barrier",
            DEFAULT_COMPLETION_SOUND,
            DEFAULT_COMPLETION_SOUND_VOLUME,
            DEFAULT_COMPLETION_HUD_BACKGROUND,
            false,
            DEFAULT_QUEST_BACKGROUND,
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
        this(title, subtitle, description, groups, icon, iconBackground, DEFAULT_COMPLETION_SOUND, DEFAULT_COMPLETION_SOUND_VOLUME, DEFAULT_COMPLETION_HUD_BACKGROUND, false, DEFAULT_QUEST_BACKGROUND, false);
    }

    public QuestDisplay(
            String title,
            String subtitle,
            List<String> description,
            Map<String, ChapterDefinition> groups,
            String icon,
            String iconBackground,
            String completionSound,
            boolean visualHidden
    ) {
        this(title, subtitle, description, groups, icon, iconBackground, completionSound, DEFAULT_COMPLETION_SOUND_VOLUME, DEFAULT_COMPLETION_HUD_BACKGROUND, visualHidden, DEFAULT_QUEST_BACKGROUND, false);
    }

    public QuestDisplay(
            String title,
            String subtitle,
            List<String> description,
            Map<String, ChapterDefinition> groups,
            String icon,
            String iconBackground,
            String completionSound,
            int completionSoundVolume,
            boolean visualHidden
    ) {
        this(title, subtitle, description, groups, icon, iconBackground, completionSound, completionSoundVolume, DEFAULT_COMPLETION_HUD_BACKGROUND, visualHidden, DEFAULT_QUEST_BACKGROUND, false);
    }

    public QuestDisplay(
            String title,
            String subtitle,
            List<String> description,
            Map<String, ChapterDefinition> groups,
            String icon,
            String iconBackground,
            String completionSound,
            int completionSoundVolume,
            boolean visualHidden,
            String questBackground,
            boolean questBackgroundGrayscale
    ) {
        this(title, subtitle, description, groups, icon, iconBackground, completionSound, completionSoundVolume, DEFAULT_COMPLETION_HUD_BACKGROUND, visualHidden, questBackground, questBackgroundGrayscale);
    }

    public QuestDisplay {
        completionSound = completionSound == null || completionSound.isBlank() ? DEFAULT_COMPLETION_SOUND : completionSound.trim();
        completionSoundVolume = normalizeCompletionSoundVolume(completionSoundVolume);
        completionHudBackground = normalizeCompletionHudBackground(completionHudBackground);
        questBackground = normalizeQuestBackground(questBackground);
    }

    public static final Codec<QuestDisplay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("title").orElse("Untitled Quest").forGetter(QuestDisplay::title),
            Codec.STRING.fieldOf("subtitle").orElse("").forGetter(QuestDisplay::subtitle),
            Codec.STRING.listOf().fieldOf("description").orElse(List.of()).forGetter(QuestDisplay::description),
            Codec.unboundedMap(Codec.STRING, ChapterDefinition.CODEC).fieldOf("groups").orElse(Map.of()).forGetter(QuestDisplay::groups),
            Codec.STRING.fieldOf("icon").orElse("minecraft:book").forGetter(QuestDisplay::icon),
            Codec.STRING.fieldOf("icon_background").orElse("minecraft:barrier").forGetter(QuestDisplay::iconBackground),
            Codec.STRING.fieldOf("completion_sound").orElse(DEFAULT_COMPLETION_SOUND).forGetter(QuestDisplay::completionSound),
            Codec.INT.fieldOf("completion_sound_volume").orElse(DEFAULT_COMPLETION_SOUND_VOLUME).forGetter(QuestDisplay::completionSoundVolume),
            Codec.STRING.fieldOf("completion_hud_background").orElse(DEFAULT_COMPLETION_HUD_BACKGROUND).forGetter(QuestDisplay::completionHudBackground),
            Codec.BOOL.fieldOf("visual_hidden").orElse(false).forGetter(QuestDisplay::visualHidden),
            Codec.STRING.fieldOf("quest_background").orElse(DEFAULT_QUEST_BACKGROUND).forGetter(QuestDisplay::questBackground),
            Codec.BOOL.fieldOf("quest_background_grayscale").orElse(false).forGetter(QuestDisplay::questBackgroundGrayscale)
    ).apply(instance, QuestDisplay::new));

    public static int normalizeCompletionSoundVolume(int volume) {
        return Math.max(0, Math.min(100, volume));
    }

    public static String normalizeQuestBackground(String background) {
        return background == null || background.isBlank() ? DEFAULT_QUEST_BACKGROUND : background.trim();
    }

    public static String normalizeCompletionHudBackground(String background) {
        return background == null || background.isBlank() ? DEFAULT_COMPLETION_HUD_BACKGROUND : background.trim();
    }
}
