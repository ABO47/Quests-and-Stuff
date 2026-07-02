package com.abo47.questsandstuff.quest.model.task.item;

import com.abo47.questsandstuff.quest.model.storage.IntegerTaskStorage;
import com.abo47.questsandstuff.quest.model.storage.TaskStorage;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.runtime.signal.QuestInventoryTasks;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignal;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record GatherItemQuestTaskDefinition(
        String id,
        ResourceLocation type,
        ResourceLocation item,
        String tag,
        String nbt,
        int goal,
        CollectionMode collection,
        String title,
        String icon
) implements QuestTaskDefinition {
    private static final ResourceLocation AIR = ResourceLocation.tryBuild("minecraft", "air");

    public GatherItemQuestTaskDefinition(String id, ResourceLocation type, ResourceLocation item, String tag, String nbt, int goal, CollectionMode collection) {
        this(id, type, item, tag, nbt, goal, collection, "", "");
    }

    public GatherItemQuestTaskDefinition(String id, ResourceLocation type, ResourceLocation item, String nbt, int goal, CollectionMode collection) {
        this(id, type, item, "", nbt, goal, collection, "", "");
    }

    public static Codec<GatherItemQuestTaskDefinition> codec(ResourceLocation type) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(GatherItemQuestTaskDefinition::id),
                ResourceLocation.CODEC.optionalFieldOf("item", AIR).forGetter(GatherItemQuestTaskDefinition::item),
                Codec.STRING.fieldOf("tag").orElse("").forGetter(GatherItemQuestTaskDefinition::tag),
                Codec.STRING.fieldOf("nbt").orElse("").forGetter(GatherItemQuestTaskDefinition::nbt),
                Codec.INT.fieldOf("amount").orElse(1).forGetter(GatherItemQuestTaskDefinition::goal),
                CollectionMode.CODEC.fieldOf("collection").orElse(CollectionMode.AUTOMATIC).forGetter(GatherItemQuestTaskDefinition::collection),
                Codec.STRING.fieldOf("title").orElse("").forGetter(GatherItemQuestTaskDefinition::title),
                Codec.STRING.fieldOf("icon").orElse("").forGetter(GatherItemQuestTaskDefinition::icon)
        ).apply(instance, (id, item, tag, nbt, goal, collection, title, icon) -> new GatherItemQuestTaskDefinition(id, type, item, tag, nbt, goal, collection, title, icon)));
    }

    public GatherItemQuestTaskDefinition {
        tag = tag == null ? "" : tag.trim();
        if (tag.startsWith("#")) {
            tag = tag.substring(1);
        }
        title = title == null ? "" : title.trim();
        icon = icon == null ? "" : icon.trim();
    }

    @Override
    public TaskStorage<?, ? extends Tag> storage() {
        return IntegerTaskStorage.INSTANCE;
    }

    @Override
    public Set<QuestSignalType> signals() {
        return Set.of(QuestSignalType.ITEM_COLLECTED, QuestSignalType.INVENTORY_CHANGED, QuestSignalType.MANUAL_ITEM_SUBMIT);
    }

    @Override
    public Tag initProgress(Tag progress, ServerPlayer player) {
        Tag current = progress == null ? defaultProgress() : progress;
        if (collection != CollectionMode.AUTOMATIC || player == null) {
            return current;
        }
        return IntegerTaskStorage.INSTANCE.max(current, countMatching(player), safeGoal());
    }

    @Override
    public Tag test(Tag progress, QuestSignal signal) {
        Tag current = progress == null ? defaultProgress() : progress;
        if (collection == CollectionMode.AUTOMATIC) {
            if (signal.type() != QuestSignalType.ITEM_COLLECTED && signal.type() != QuestSignalType.INVENTORY_CHANGED) {
                return current;
            }
            if (isFluidTask() && signal.type() == QuestSignalType.INVENTORY_CHANGED) {
                return automaticInventoryProgress(current, signal);
            }
            if (!matchesItemKey(signal.key())) {
                return current;
            }
            return automaticInventoryProgress(current, signal);
        }
        if (signal.type() != QuestSignalType.MANUAL_ITEM_SUBMIT || !matchesItemKey(signal.key())) {
            return current;
        }
        return IntegerTaskStorage.INSTANCE.add(current, Math.max(0, signal.amount()), safeGoal());
    }

    public boolean usesTag() {
        return !tag.isBlank();
    }

    public boolean matchesItemKey(String itemKey) {
        if (isFluidTask()) {
            return QuestInventoryTasks.itemContainsFluid(itemKey, icon);
        }
        if (usesTag()) {
            return QuestInventoryTasks.itemKeyInTag(itemKey, tag);
        }
        return item != null && item.toString().equals(itemKey);
    }

    public int countMatching(ServerPlayer player) {
        if (isFluidTask()) {
            return QuestInventoryTasks.countFluidContainers(player, icon);
        }
        if (usesTag()) {
            return QuestInventoryTasks.countItemsByTag(player, tag, nbt);
        }
        return QuestInventoryTasks.countItems(player, item, nbt);
    }

    public int consumeMatching(ServerPlayer player, int max) {
        if (isFluidTask()) {
            return 0;
        }
        if (usesTag()) {
            return QuestInventoryTasks.consumeItemsByTag(player, tag, nbt, max);
        }
        return QuestInventoryTasks.consumeItems(player, item, nbt, max);
    }

    private Tag automaticInventoryProgress(Tag current, QuestSignal signal) {
        if (signal.player() != null) {
            return IntegerTaskStorage.INSTANCE.max(current, countMatching(signal.player()), safeGoal());
        }
        return IntegerTaskStorage.INSTANCE.max(current, Math.max(0, signal.amount()), safeGoal());
    }

    private boolean isFluidTask() {
        return QuestInventoryTasks.isFluidIcon(icon);
    }
}
