package com.abo47.questsandstuff.quest.model.task.generic;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import com.abo47.questsandstuff.quest.model.storage.IntegerTaskStorage;
import com.abo47.questsandstuff.quest.model.storage.TaskStorage;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskItemLocks;
import com.abo47.questsandstuff.quest.runtime.signal.QuestInventoryTasks;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignal;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;

public record SimpleQuestTaskDefinition(
        String id,
        ResourceLocation type,
        QuestSignalType signalType,
        int goal,
        String target,
        String icon,
        String title,
        List<String> itemLocks
) implements QuestTaskDefinition {
    public SimpleQuestTaskDefinition(String id, ResourceLocation type, QuestSignalType signalType, int goal, String target, String icon) {
        this(id, type, signalType, goal, target, icon, "");
    }

    public SimpleQuestTaskDefinition(String id, ResourceLocation type, QuestSignalType signalType, int goal, String target, String icon, String title) {
        this(id, type, signalType, goal, target, icon, title, List.of());
    }

    public static Codec<SimpleQuestTaskDefinition> codec(ResourceLocation type, QuestSignalType signalType) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(SimpleQuestTaskDefinition::id),
                Codec.INT.fieldOf("amount").orElse(1).forGetter(SimpleQuestTaskDefinition::goal),
                Codec.STRING.fieldOf("target").orElse("").forGetter(SimpleQuestTaskDefinition::target),
                Codec.STRING.fieldOf("icon").orElse("").forGetter(SimpleQuestTaskDefinition::icon),
                Codec.STRING.fieldOf("title").orElse("").forGetter(SimpleQuestTaskDefinition::title),
                QuestTaskItemLocks.codec().optionalFieldOf(QuestTaskItemLocks.FIELD, List.of()).forGetter(SimpleQuestTaskDefinition::itemLocks)
        ).apply(instance, (id, goal, target, icon, title, itemLocks) -> new SimpleQuestTaskDefinition(id, type, signalType, goal, target, icon, title, itemLocks)));
    }

    public SimpleQuestTaskDefinition {
        target = target == null ? "" : target.trim();
        icon = icon == null ? "" : icon.trim();
        title = title == null ? "" : title.trim();
        itemLocks = QuestTaskItemLocks.normalize(itemLocks);
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
        if (!matchesTarget(signal.key())) {
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
        return new SimpleQuestTaskDefinition(id, type, signalType, goal, retargeted, icon, title, itemLocks);
    }

    private boolean matchesTarget(String signalKey) {
        if (target.isBlank()) {
            return true;
        }
        if (target.equals(signalKey)) {
            return true;
        }
        if (!target.startsWith("#")) {
            return false;
        }
        String tag = target.substring(1);
        if (signalType == QuestSignalType.BLOCK_INTERACT) {
            return blockKeyInTag(signalKey, tag);
        }
        return (signalType == QuestSignalType.ITEM_CRAFTED
                || signalType == QuestSignalType.ITEM_INTERACT
                || signalType == QuestSignalType.ITEM_USED)
                && QuestInventoryTasks.itemKeyInTag(signalKey, tag);
    }

    private static boolean blockKeyInTag(String blockKey, String tag) {
        ResourceLocation blockId = ResourceLocation.tryParse(blockKey);
        ResourceLocation tagId = ResourceLocation.tryParse(tag);
        if (blockId == null || tagId == null) {
            return false;
        }
        Block block = BuiltInRegistries.BLOCK.get(blockId);
        if (!blockId.equals(BuiltInRegistries.BLOCK.getKey(block))) {
            return false;
        }
        TagKey<Block> tagKey = TagKey.create(BuiltInRegistries.BLOCK.key(), tagId);
        return block.defaultBlockState().is(tagKey);
    }
}
