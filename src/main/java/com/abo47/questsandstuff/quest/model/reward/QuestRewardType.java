package com.abo47.questsandstuff.quest.model.reward;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public record QuestRewardType<T extends QuestRewardDefinition>(
        ResourceLocation id,
        Codec<T> codec
) {
}
