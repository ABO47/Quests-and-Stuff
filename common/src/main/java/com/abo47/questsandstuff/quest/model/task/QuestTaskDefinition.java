package com.abo47.questsandstuff.quest.model.task;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.Codec;

import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.model.storage.BooleanTaskStorage;
import com.abo47.questsandstuff.quest.model.storage.IntegerTaskStorage;
import com.abo47.questsandstuff.quest.model.storage.TaskStorage;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignal;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;

public interface QuestTaskDefinition {
    Codec<QuestTaskDefinition> CODEC = QuestTasks.CODEC;

    String id();

    ResourceLocation type();

    default List<String> itemLocks() {
        return List.of();
    }

    TaskStorage<?, ? extends Tag> storage();

    Set<QuestSignalType> signals();

    default int goal() {
        return 1;
    }

    default int safeGoal() {
        return Math.max(1, goal());
    }

    default Tag defaultProgress() {
        return storage().createDefault();
    }

    default Tag initProgress(Tag progress, ServerPlayer player) {
        return progress == null ? defaultProgress() : progress;
    }

    default Tag test(Tag progress, QuestSignal signal) {
        return progress == null ? defaultProgress() : progress;
    }

    default float getProgress(Tag progress) {
        if (storage() == IntegerTaskStorage.INSTANCE) {
            return Math.min(1.0f, IntegerTaskStorage.INSTANCE.readInt(progress) / (float) safeGoal());
        }
        if (storage() == BooleanTaskStorage.INSTANCE) {
            return BooleanTaskStorage.INSTANCE.readBoolean(progress) ? 1.0f : 0.0f;
        }
        return 0.0f;
    }

    default boolean isComplete(Tag progress) {
        return getProgress(progress) >= 1.0f;
    }

    default QuestTaskDefinition copyForQuest(Map<String, String> copiedQuestIds) {
        return this;
    }
}
