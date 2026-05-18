package com.abo47.questsandstuff.quest.model.reward;

import com.abo47.questsandstuff.quest.model.task.player.XpMode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record XpQuestRewardDefinition(
        String id,
        ResourceLocation type,
        int amount,
        XpMode mode,
        String title,
        String icon
) implements QuestRewardDefinition {
    public XpQuestRewardDefinition(String id, ResourceLocation type, int amount, XpMode mode) {
        this(id, type, amount, mode, "", "");
    }

    public static Codec<XpQuestRewardDefinition> codec(ResourceLocation type) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(XpQuestRewardDefinition::id),
                Codec.INT.fieldOf("amount").orElse(1).forGetter(XpQuestRewardDefinition::amount),
                XpMode.CODEC.fieldOf("mode").orElse(XpMode.POINTS).forGetter(XpQuestRewardDefinition::mode),
                Codec.STRING.fieldOf("title").orElse("").forGetter(XpQuestRewardDefinition::title),
                Codec.STRING.fieldOf("icon").orElse("").forGetter(XpQuestRewardDefinition::icon)
        ).apply(instance, (id, amount, mode, title, icon) -> new XpQuestRewardDefinition(id, type, amount, mode, title, icon)));
    }

    public XpQuestRewardDefinition {
        title = title == null ? "" : title.trim();
        icon = icon == null ? "" : icon.trim();
    }

    @Override
    public void grant(ServerPlayer player) {
        if (mode == XpMode.LEVEL) {
            player.giveExperienceLevels(safeAmount());
        } else {
            player.giveExperiencePoints(safeAmount());
        }
    }
}
