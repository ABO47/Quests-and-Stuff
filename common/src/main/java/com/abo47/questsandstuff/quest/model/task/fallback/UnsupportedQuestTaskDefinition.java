package com.abo47.questsandstuff.quest.model.task.fallback;

import com.abo47.questsandstuff.quest.model.storage.IntegerTaskStorage;
import com.abo47.questsandstuff.quest.model.storage.TaskStorage;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Set;

public record UnsupportedQuestTaskDefinition(
        String id,
        ResourceLocation type,
        int goal,
        String target,
        boolean consume,
        Map<String, String> args
) implements QuestTaskDefinition {
    public static Codec<UnsupportedQuestTaskDefinition> codec(ResourceLocation type) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(UnsupportedQuestTaskDefinition::id),
                Codec.INT.fieldOf("goal").orElse(1).forGetter(UnsupportedQuestTaskDefinition::goal),
                Codec.STRING.fieldOf("target").orElse("").forGetter(UnsupportedQuestTaskDefinition::target),
                Codec.BOOL.fieldOf("consume").orElse(false).forGetter(UnsupportedQuestTaskDefinition::consume),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("args").orElse(Map.of()).forGetter(UnsupportedQuestTaskDefinition::args)
        ).apply(instance, (id, goal, target, consume, args) -> new UnsupportedQuestTaskDefinition(id, type, goal, target, consume, args)));
    }

    public UnsupportedQuestTaskDefinition {
        args = args == null ? Map.of() : Map.copyOf(args);
    }

    @Override
    public TaskStorage<?, ? extends Tag> storage() {
        return IntegerTaskStorage.INSTANCE;
    }

    @Override
    public Set<QuestSignalType> signals() {
        return Set.of();
    }
}
