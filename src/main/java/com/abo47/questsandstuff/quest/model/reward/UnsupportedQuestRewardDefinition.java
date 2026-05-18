package com.abo47.questsandstuff.quest.model.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public record UnsupportedQuestRewardDefinition(
        String id,
        ResourceLocation type,
        int amount,
        String payload,
        boolean selectable,
        Map<String, String> args
) implements QuestRewardDefinition {
    public static Codec<UnsupportedQuestRewardDefinition> codec(ResourceLocation type) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(UnsupportedQuestRewardDefinition::id),
                Codec.INT.fieldOf("amount").orElse(1).forGetter(UnsupportedQuestRewardDefinition::amount),
                Codec.STRING.fieldOf("payload").orElse("").forGetter(UnsupportedQuestRewardDefinition::payload),
                Codec.BOOL.fieldOf("selectable").orElse(false).forGetter(UnsupportedQuestRewardDefinition::selectable),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("args").orElse(Map.of()).forGetter(UnsupportedQuestRewardDefinition::args)
        ).apply(instance, (id, amount, payload, selectable, args) -> new UnsupportedQuestRewardDefinition(id, type, amount, payload, selectable, args)));
    }

    public UnsupportedQuestRewardDefinition {
        args = args == null ? Map.of() : Map.copyOf(args);
    }

    @Override
    public boolean canBeMassClaimed() {
        return false;
    }

    @Override
    public void grant(ServerPlayer player) {
    }
}
