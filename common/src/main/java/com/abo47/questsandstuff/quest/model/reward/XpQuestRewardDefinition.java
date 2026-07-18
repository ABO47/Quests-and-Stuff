package com.abo47.questsandstuff.quest.model.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.model.task.player.XpMode;

public record XpQuestRewardDefinition(
        String id,
        ResourceLocation type,
        int amount,
        XpMode mode,
        String title,
        String icon,
        boolean selectable
) implements QuestRewardDefinition {
    public XpQuestRewardDefinition(String id, ResourceLocation type, int amount, XpMode mode) {
        this(id, type, amount, mode, "", "", false);
    }

    public XpQuestRewardDefinition(String id, ResourceLocation type, int amount, XpMode mode, String title, String icon) {
        this(id, type, amount, mode, title, icon, false);
    }

    public static Codec<XpQuestRewardDefinition> codec(ResourceLocation type) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(XpQuestRewardDefinition::id),
                Codec.INT.fieldOf("amount").orElse(1).forGetter(XpQuestRewardDefinition::amount),
                XpMode.CODEC.fieldOf("mode").orElse(XpMode.POINTS).forGetter(XpQuestRewardDefinition::mode),
                Codec.STRING.fieldOf("title").orElse("").forGetter(XpQuestRewardDefinition::title),
                Codec.STRING.fieldOf("icon").orElse("").forGetter(XpQuestRewardDefinition::icon),
                Codec.BOOL.fieldOf("selectable").orElse(false).forGetter(XpQuestRewardDefinition::selectable)
        ).apply(instance, (id, amount, mode, title, icon, selectable) -> new XpQuestRewardDefinition(id, type, amount, mode, title, icon, selectable)));
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
