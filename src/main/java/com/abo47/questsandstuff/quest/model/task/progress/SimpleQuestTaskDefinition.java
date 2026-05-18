package com.abo47.questsandstuff.quest.model.task.progress;

import com.abo47.questsandstuff.quest.model.storage.IntegerTaskStorage;
import com.abo47.questsandstuff.quest.model.storage.TaskStorage;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignal;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public record SimpleQuestTaskDefinition(
        String id,
        ResourceLocation type,
        QuestSignalType signalType,
        int goal,
        String target,
        String icon,
        String title
) implements QuestTaskDefinition {
    public SimpleQuestTaskDefinition(String id, ResourceLocation type, QuestSignalType signalType, int goal, String target, String icon) {
        this(id, type, signalType, goal, target, icon, "");
    }

    public static Codec<SimpleQuestTaskDefinition> codec(ResourceLocation type, QuestSignalType signalType) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(SimpleQuestTaskDefinition::id),
                Codec.INT.fieldOf("amount").orElse(1).forGetter(SimpleQuestTaskDefinition::goal),
                Codec.STRING.fieldOf("target").orElse("").forGetter(SimpleQuestTaskDefinition::target),
                Codec.STRING.fieldOf("icon").orElse("").forGetter(SimpleQuestTaskDefinition::icon),
                Codec.STRING.fieldOf("title").orElse("").forGetter(SimpleQuestTaskDefinition::title)
        ).apply(instance, (id, goal, target, icon, title) -> new SimpleQuestTaskDefinition(id, type, signalType, goal, target, icon, title)));
    }

    public SimpleQuestTaskDefinition {
        target = target == null ? "" : target.trim();
        icon = icon == null ? "" : icon.trim();
        title = title == null ? "" : title.trim();
    }

    @Override
    public TaskStorage<?, ? extends Tag> storage() {
        return IntegerTaskStorage.INSTANCE;
    }

    @Override
    public Set<QuestSignalType> signals() {
        return Set.of(signalType);
    }

    @Override
    public Tag test(Tag progress, QuestSignal signal) {
        if (signal.type() != signalType) {
            return progress == null ? defaultProgress() : progress;
        }
        if (!target.isBlank() && !target.equals(signal.key())) {
            return progress == null ? defaultProgress() : progress;
        }
        return IntegerTaskStorage.INSTANCE.add(progress, Math.max(1, signal.amount()), safeGoal());
    }

    @Override
    public QuestTaskDefinition copyForQuest(Map<String, String> copiedQuestIds) {
        String retargeted = copiedQuestIds.getOrDefault(target, target);
        if (retargeted.equals(target)) {
            return this;
        }
        return new SimpleQuestTaskDefinition(id, type, signalType, goal, retargeted, icon, title);
    }
}
