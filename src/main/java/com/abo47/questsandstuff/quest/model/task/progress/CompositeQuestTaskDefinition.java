package com.abo47.questsandstuff.quest.model.task.progress;

import com.abo47.questsandstuff.quest.model.storage.CompositeTaskStorage;
import com.abo47.questsandstuff.quest.model.storage.TaskStorage;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public record CompositeQuestTaskDefinition(
        String id,
        ResourceLocation type,
        int goal,
        List<String> children
) implements QuestTaskDefinition {
    public static Codec<CompositeQuestTaskDefinition> codec(ResourceLocation type) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(CompositeQuestTaskDefinition::id),
                Codec.INT.fieldOf("required").orElse(1).forGetter(CompositeQuestTaskDefinition::goal),
                Codec.STRING.listOf().fieldOf("children").orElse(List.of()).forGetter(CompositeQuestTaskDefinition::children)
        ).apply(instance, (id, goal, children) -> new CompositeQuestTaskDefinition(id, type, goal, children)));
    }

    public CompositeQuestTaskDefinition {
        children = children == null ? List.of() : List.copyOf(children);
    }

    @Override
    public TaskStorage<?, ? extends Tag> storage() {
        return CompositeTaskStorage.EMPTY;
    }

    @Override
    public Set<QuestSignalType> signals() {
        return Set.of();
    }

    public List<String> safeChildren() {
        return children == null ? List.of() : new ArrayList<>(children);
    }
}
