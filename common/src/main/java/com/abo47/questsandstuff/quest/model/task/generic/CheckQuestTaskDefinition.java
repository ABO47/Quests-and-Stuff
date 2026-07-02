package com.abo47.questsandstuff.quest.model.task.generic;

import com.abo47.questsandstuff.quest.model.storage.BooleanTaskStorage;
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

public record CheckQuestTaskDefinition(
        String id,
        ResourceLocation type,
        String target,
        String title,
        String icon
) implements QuestTaskDefinition {
    public CheckQuestTaskDefinition(String id, ResourceLocation type, String target) {
        this(id, type, target, "", "");
    }

    public static Codec<CheckQuestTaskDefinition> codec(ResourceLocation type) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(CheckQuestTaskDefinition::id),
                Codec.STRING.fieldOf("target").orElse("").forGetter(CheckQuestTaskDefinition::target),
                Codec.STRING.fieldOf("title").orElse("").forGetter(CheckQuestTaskDefinition::title),
                Codec.STRING.fieldOf("icon").orElse("").forGetter(CheckQuestTaskDefinition::icon)
        ).apply(instance, (id, target, title, icon) -> new CheckQuestTaskDefinition(id, type, target, title, icon)));
    }

    public CheckQuestTaskDefinition {
        target = target == null ? "" : target.trim();
        title = title == null ? "" : title.trim();
        icon = icon == null ? "" : icon.trim();
    }

    @Override
    public TaskStorage<?, ? extends Tag> storage() {
        return BooleanTaskStorage.INSTANCE;
    }

    @Override
    public Set<QuestSignalType> signals() {
        return Set.of(QuestSignalType.MANUAL_CHECK);
    }

    @Override
    public Tag test(Tag progress, QuestSignal signal) {
        if (signal.type() != QuestSignalType.MANUAL_CHECK) {
            return progress == null ? defaultProgress() : progress;
        }
        String expected = target.isBlank() ? id : target;
        return expected.isBlank() || expected.equals(signal.key())
                ? BooleanTaskStorage.INSTANCE.set(true)
                : progress == null ? defaultProgress() : progress;
    }

    @Override
    public QuestTaskDefinition copyForQuest(Map<String, String> copiedQuestIds) {
        String retargeted = copiedQuestIds.getOrDefault(target, target);
        if (retargeted.equals(target)) {
            return this;
        }
        return new CheckQuestTaskDefinition(id, type, retargeted, title, icon);
    }
}
