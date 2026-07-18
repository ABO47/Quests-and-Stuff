package com.abo47.questsandstuff.quest.model.task.player;

import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.model.storage.IntegerTaskStorage;
import com.abo47.questsandstuff.quest.model.storage.TaskStorage;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignal;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;
import com.abo47.questsandstuff.quest.runtime.signal.QuestStatHelper;

public record StatQuestTaskDefinition(
        String id,
        ResourceLocation type,
        int goal,
        String target,
        String icon
) implements QuestTaskDefinition {
    public static Codec<StatQuestTaskDefinition> codec(ResourceLocation type) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(StatQuestTaskDefinition::id),
                Codec.INT.fieldOf("amount").orElse(1).forGetter(StatQuestTaskDefinition::goal),
                Codec.STRING.fieldOf("target").orElse("").forGetter(StatQuestTaskDefinition::target),
                Codec.STRING.fieldOf("icon").orElse("").forGetter(StatQuestTaskDefinition::icon)
        ).apply(instance, (id, goal, target, icon) -> new StatQuestTaskDefinition(id, type, goal, target, icon)));
    }

    public StatQuestTaskDefinition {
        icon = icon == null ? "" : icon;
    }

    @Override
    public TaskStorage<?, ? extends Tag> storage() {
        return IntegerTaskStorage.INSTANCE;
    }

    @Override
    public Set<QuestSignalType> signals() {
        return Set.of(QuestSignalType.STAT_CHANGE);
    }

    @Override
    public Tag initProgress(Tag progress, ServerPlayer player) {
        Tag current = progress == null ? defaultProgress() : progress;
        if (player == null || target.isBlank()) {
            return current;
        }
        return IntegerTaskStorage.INSTANCE.max(current, QuestStatHelper.readStat(player, target), safeGoal());
    }

    @Override
    public Tag test(Tag progress, QuestSignal signal) {
        Tag current = progress == null ? defaultProgress() : progress;
        if (signal.type() != QuestSignalType.STAT_CHANGE) {
            return current;
        }
        if (!target.isBlank() && !target.equals(signal.key())) {
            return current;
        }
        return IntegerTaskStorage.INSTANCE.max(current, Math.max(0, signal.amount()), safeGoal());
    }
}
