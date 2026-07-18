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
import com.abo47.questsandstuff.quest.model.task.item.CollectionMode;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignal;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;

public record XpQuestTaskDefinition(
        String id,
        ResourceLocation type,
        int goal,
        XpMode mode,
        CollectionMode collection
) implements QuestTaskDefinition {
    public static Codec<XpQuestTaskDefinition> codec(ResourceLocation type) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(XpQuestTaskDefinition::id),
                Codec.INT.fieldOf("amount").orElse(1).forGetter(XpQuestTaskDefinition::goal),
                XpMode.CODEC.fieldOf("mode").orElse(XpMode.POINTS).forGetter(XpQuestTaskDefinition::mode),
                CollectionMode.CODEC.fieldOf("collection").orElse(CollectionMode.AUTOMATIC).forGetter(XpQuestTaskDefinition::collection)
        ).apply(instance, (id, goal, mode, collection) -> new XpQuestTaskDefinition(id, type, goal, mode, collection)));
    }

    @Override
    public TaskStorage<?, ? extends Tag> storage() {
        return IntegerTaskStorage.INSTANCE;
    }

    @Override
    public Set<QuestSignalType> signals() {
        return Set.of(QuestSignalType.XP_CHANGE, QuestSignalType.XP_SNAPSHOT, QuestSignalType.MANUAL_XP_SUBMIT);
    }

    @Override
    public Tag initProgress(Tag progress, ServerPlayer player) {
        Tag current = progress == null ? defaultProgress() : progress;
        if (collection != CollectionMode.AUTOMATIC || player == null) {
            return current;
        }
        int value = mode == XpMode.LEVEL ? player.experienceLevel : player.totalExperience;
        return IntegerTaskStorage.INSTANCE.max(current, value, safeGoal());
    }

    @Override
    public Tag test(Tag progress, QuestSignal signal) {
        Tag current = progress == null ? defaultProgress() : progress;
        if (collection != CollectionMode.AUTOMATIC) {
            return signal.type() == QuestSignalType.MANUAL_XP_SUBMIT
                    ? IntegerTaskStorage.INSTANCE.add(current, Math.max(0, signal.amount()), safeGoal())
                    : current;
        }
        if (signal.type() == QuestSignalType.XP_CHANGE && mode == XpMode.POINTS) {
            return IntegerTaskStorage.INSTANCE.add(current, Math.max(0, signal.amount()), safeGoal());
        }
        if (signal.type() == QuestSignalType.XP_SNAPSHOT) {
            String key = mode == XpMode.LEVEL ? "level" : "points";
            if (key.equals(signal.key())) {
                return IntegerTaskStorage.INSTANCE.max(current, Math.max(0, signal.amount()), safeGoal());
            }
        }
        return current;
    }
}
