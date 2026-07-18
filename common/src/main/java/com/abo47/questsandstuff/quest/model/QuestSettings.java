package com.abo47.questsandstuff.quest.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;

public record QuestSettings(
        boolean individualProgress,
        QuestVisibilityMode hiddenMode,
        boolean repeatable,
        boolean autoClaimRewards,
        boolean unlockNotification,
        boolean showPrerequisiteArrow
) {
    public static final QuestSettings DEFAULT = new QuestSettings(
            false,
            QuestVisibilityMode.PREREQUISITES_VISIBLE,
            false,
            false,
            false,
            true
    );
    public static final String SHOW_PREREQUISITE_ARROW_FIELD = "show_prerequisite_arrow";

    public static final Codec<QuestSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("individual_progress").orElse(false).forGetter(QuestSettings::individualProgress),
            QuestVisibilityMode.CODEC.fieldOf("hidden_mode").orElse(QuestSettings.DEFAULT.hiddenMode()).forGetter(QuestSettings::hiddenMode),
            Codec.BOOL.fieldOf("repeatable").orElse(false).forGetter(QuestSettings::repeatable),
            Codec.BOOL.fieldOf("auto_claim_rewards").orElse(false).forGetter(QuestSettings::autoClaimRewards),
            Codec.BOOL.fieldOf("unlock_notification").orElse(false).forGetter(QuestSettings::unlockNotification),
            Codec.BOOL.fieldOf(SHOW_PREREQUISITE_ARROW_FIELD).orElse(true).forGetter(QuestSettings::showPrerequisiteArrow)
    ).apply(instance, QuestSettings::new));
}
