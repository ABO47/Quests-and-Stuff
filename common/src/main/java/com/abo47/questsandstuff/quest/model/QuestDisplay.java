package com.abo47.questsandstuff.quest.model;

import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record QuestDisplay(
        String title,
        String subtitle,
        List<String> description,
        Map<String, ChapterDef> chapters,
        String icon,
        String iconBackground,
        String completionSound,
        int completionSoundVolume,
        String completionHudBackground,
        boolean visualHidden,
        String questBackground,
        boolean questBackgroundGrayscale
) {
    public static final String DEFAULT_TITLE = "Untitled Quest";
    public static final String DEFAULT_SUBTITLE = "";
    public static final String DEFAULT_ICON = "minecraft:book";
    public static final String DEFAULT_ICON_BACKGROUND = "minecraft:barrier";
    public static final String DEFAULT_COMPLETION_SOUND = "minecraft:ui.toast.challenge_complete";
    public static final int DEFAULT_COMPLETION_SOUND_VOLUME = 100;
    public static final String DEFAULT_COMPLETION_HUD_BACKGROUND = "";
    public static final String DEFAULT_QUEST_BACKGROUND = "default";
    public static final boolean DEFAULT_VISUAL_HIDDEN = false;
    public static final boolean DEFAULT_QUEST_BACKGROUND_GRAYSCALE = false;

    public static final QuestDisplay DEFAULT = new QuestDisplay(
            DEFAULT_TITLE,
            DEFAULT_SUBTITLE,
            List.of(),
            Map.of(),
            DEFAULT_ICON,
            DEFAULT_ICON_BACKGROUND,
            DEFAULT_COMPLETION_SOUND,
            DEFAULT_COMPLETION_SOUND_VOLUME,
            DEFAULT_COMPLETION_HUD_BACKGROUND,
            DEFAULT_VISUAL_HIDDEN,
            DEFAULT_QUEST_BACKGROUND,
            DEFAULT_QUEST_BACKGROUND_GRAYSCALE
    );

    public static QuestDisplay forNewQuest(String title, Map<String, ChapterDef> chapters) {
        return new QuestDisplay(
                title == null ? "" : title.trim(),
                DEFAULT_SUBTITLE,
                List.of(),
                chapters == null ? Map.of() : chapters,
                DEFAULT_ICON,
                DEFAULT_ICON_BACKGROUND,
                DEFAULT_COMPLETION_SOUND,
                DEFAULT_COMPLETION_SOUND_VOLUME,
                DEFAULT_COMPLETION_HUD_BACKGROUND,
                DEFAULT_VISUAL_HIDDEN,
                DEFAULT_QUEST_BACKGROUND,
                DEFAULT_QUEST_BACKGROUND_GRAYSCALE
        );
    }

    public QuestDisplay withChapters(Map<String, ChapterDef> chapters) {
        return new QuestDisplay(
                title,
                subtitle,
                description,
                chapters == null ? Map.of() : chapters,
                icon,
                iconBackground,
                completionSound,
                completionSoundVolume,
                completionHudBackground,
                visualHidden,
                questBackground,
                questBackgroundGrayscale
        );
    }

    public QuestDisplay(
            String title,
            String subtitle,
            List<String> description,
            Map<String, ChapterDef> chapters,
            String icon,
            String iconBackground
    ) {
        this(title, subtitle, description, chapters, icon, iconBackground, DEFAULT_COMPLETION_SOUND, DEFAULT_COMPLETION_SOUND_VOLUME, DEFAULT_COMPLETION_HUD_BACKGROUND, DEFAULT_VISUAL_HIDDEN, DEFAULT_QUEST_BACKGROUND, DEFAULT_QUEST_BACKGROUND_GRAYSCALE);
    }

    public QuestDisplay(
            String title,
            String subtitle,
            List<String> description,
            Map<String, ChapterDef> chapters,
            String icon,
            String iconBackground,
            String completionSound,
            boolean visualHidden
    ) {
        this(title, subtitle, description, chapters, icon, iconBackground, completionSound, DEFAULT_COMPLETION_SOUND_VOLUME, DEFAULT_COMPLETION_HUD_BACKGROUND, visualHidden, DEFAULT_QUEST_BACKGROUND, DEFAULT_QUEST_BACKGROUND_GRAYSCALE);
    }

    public QuestDisplay(
            String title,
            String subtitle,
            List<String> description,
            Map<String, ChapterDef> chapters,
            String icon,
            String iconBackground,
            String completionSound,
            int completionSoundVolume,
            boolean visualHidden
    ) {
        this(title, subtitle, description, chapters, icon, iconBackground, completionSound, completionSoundVolume, DEFAULT_COMPLETION_HUD_BACKGROUND, visualHidden, DEFAULT_QUEST_BACKGROUND, DEFAULT_QUEST_BACKGROUND_GRAYSCALE);
    }

    public QuestDisplay(
            String title,
            String subtitle,
            List<String> description,
            Map<String, ChapterDef> chapters,
            String icon,
            String iconBackground,
            String completionSound,
            int completionSoundVolume,
            boolean visualHidden,
            String questBackground,
            boolean questBackgroundGrayscale
    ) {
        this(title, subtitle, description, chapters, icon, iconBackground, completionSound, completionSoundVolume, DEFAULT_COMPLETION_HUD_BACKGROUND, visualHidden, questBackground, questBackgroundGrayscale);
    }

    public QuestDisplay {
        title = title == null ? DEFAULT_TITLE : title;
        subtitle = subtitle == null ? DEFAULT_SUBTITLE : subtitle;
        description = description == null ? List.of() : description;
        chapters = chapters == null ? Map.of() : chapters;
        icon = normalizeIcon(icon);
        iconBackground = normalizeIconBackground(iconBackground);
        completionSound = completionSound == null || completionSound.isBlank() ? DEFAULT_COMPLETION_SOUND : completionSound.trim();
        completionSoundVolume = normalizeCompletionSoundVolume(completionSoundVolume);
        completionHudBackground = normalizeCompletionHudBackground(completionHudBackground);
        questBackground = normalizeQuestBackground(questBackground);
    }

    public static final Codec<QuestDisplay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("title").orElse(DEFAULT_TITLE).forGetter(QuestDisplay::title),
            Codec.STRING.fieldOf("subtitle").orElse(DEFAULT_SUBTITLE).forGetter(QuestDisplay::subtitle),
            Codec.STRING.listOf().fieldOf("description").orElse(List.of()).forGetter(QuestDisplay::description),
            Codec.unboundedMap(Codec.STRING, ChapterDef.CODEC).fieldOf("chapters").orElse(Map.of()).forGetter(QuestDisplay::chapters),
            Codec.STRING.fieldOf("icon").orElse(DEFAULT_ICON).forGetter(QuestDisplay::icon),
            Codec.STRING.fieldOf("icon_background").orElse(DEFAULT_ICON_BACKGROUND).forGetter(QuestDisplay::iconBackground),
            Codec.STRING.fieldOf("completion_sound").orElse(DEFAULT_COMPLETION_SOUND).forGetter(QuestDisplay::completionSound),
            Codec.INT.fieldOf("completion_sound_volume").orElse(DEFAULT_COMPLETION_SOUND_VOLUME).forGetter(QuestDisplay::completionSoundVolume),
            Codec.STRING.fieldOf("completion_hud_background").orElse(DEFAULT_COMPLETION_HUD_BACKGROUND).forGetter(QuestDisplay::completionHudBackground),
            Codec.BOOL.fieldOf("visual_hidden").orElse(DEFAULT_VISUAL_HIDDEN).forGetter(QuestDisplay::visualHidden),
            Codec.STRING.fieldOf("quest_background").orElse(DEFAULT_QUEST_BACKGROUND).forGetter(QuestDisplay::questBackground),
            Codec.BOOL.fieldOf("quest_background_grayscale").orElse(DEFAULT_QUEST_BACKGROUND_GRAYSCALE).forGetter(QuestDisplay::questBackgroundGrayscale)
    ).apply(instance, QuestDisplay::new));

    public static String normalizeIcon(String icon) {
        return icon == null || icon.isBlank() ? DEFAULT_ICON : icon.trim();
    }

    public static String normalizeIconBackground(String iconBackground) {
        return iconBackground == null || iconBackground.isBlank() ? DEFAULT_ICON_BACKGROUND : iconBackground.trim();
    }

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
