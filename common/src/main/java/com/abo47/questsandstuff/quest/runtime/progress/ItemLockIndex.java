package com.abo47.questsandstuff.quest.runtime.progress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskItemLocks;

public final class ItemLockIndex {
    public record LockBinding(String questId, String taskId, QuestTaskDefinition task, boolean individualProgress) {
    }

    private final Map<ResourceLocation, List<LockBinding>> byItem = new HashMap<>();
    private final Map<ResourceLocation, List<LockBinding>> byTag = new HashMap<>();
    private int bindingCount;

    public void rebuild(Map<String, QuestDefinition> quests) {
        byItem.clear();
        byTag.clear();
        bindingCount = 0;
        if (quests == null) {
            return;
        }
        for (QuestDefinition definition : quests.values()) {
            upsert(definition);
        }
    }

    public void upsert(QuestDefinition definition) {
        removeQuest(definition == null ? "" : definition.id());
        if (definition == null || definition.id() == null || definition.id().isBlank()) {
            return;
        }
        boolean individualProgress = definition.settings().individualProgress();
        for (Map.Entry<String, QuestTaskDefinition> taskEntry : definition.tasks().entrySet()) {
            List<String> locks = taskEntry.getValue().itemLocks();
            if (locks.isEmpty()) {
                continue;
            }
            LockBinding binding = new LockBinding(definition.id(), taskEntry.getKey(), taskEntry.getValue(), individualProgress);
            for (String lock : locks) {
                ResourceLocation tagId = QuestTaskItemLocks.tagId(lock);
                if (tagId != null) {
                    byTag.computeIfAbsent(tagId, ignored -> new ArrayList<>()).add(binding);
                } else {
                    ResourceLocation itemId = QuestTaskItemLocks.id(lock);
                    if (itemId != null) {
                        byItem.computeIfAbsent(itemId, ignored -> new ArrayList<>()).add(binding);
                    }
                }
            }
            bindingCount += locks.size();
        }
    }

    public void removeQuest(String questId) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        bindingCount -= removeFrom(byItem, questId);
        bindingCount -= removeFrom(byTag, questId);
    }

    private static int removeFrom(Map<ResourceLocation, List<LockBinding>> index, String questId) {
        int removed = 0;
        for (List<LockBinding> bindings : index.values()) {
            int sizeBefore = bindings.size();
            bindings.removeIf(binding -> questId.equals(binding.questId()));
            removed += sizeBefore - bindings.size();
        }
        return removed;
    }

    public boolean isEmpty() {
        return bindingCount <= 0;
    }

    public int bindingCount() {
        return bindingCount;
    }

    public List<LockBinding> bindingsFor(ItemStack stack) {
        if (isEmpty() || stack == null || stack.isEmpty()) {
            return List.of();
        }
        Item item = stack.getItem();
        List<LockBinding> direct = byItem.get(BuiltInRegistries.ITEM.getKey(item));
        List<LockBinding> tagged = matchingTagBindings(item);
        if ((direct == null || direct.isEmpty()) && tagged.isEmpty()) {
            return List.of();
        }
        if (tagged.isEmpty()) {
            return List.copyOf(direct);
        }
        if (direct == null || direct.isEmpty()) {
            return List.copyOf(tagged);
        }
        List<LockBinding> merged = new ArrayList<>(direct);
        for (LockBinding binding : tagged) {
            if (!merged.contains(binding)) {
                merged.add(binding);
            }
        }
        return merged;
    }

    private List<LockBinding> matchingTagBindings(Item item) {
        if (byTag.isEmpty()) {
            return List.of();
        }
        List<LockBinding> matched = new ArrayList<>();
        for (Map.Entry<ResourceLocation, List<LockBinding>> entry : byTag.entrySet()) {
            if (item.builtInRegistryHolder().is(TagKey.create(BuiltInRegistries.ITEM.key(), entry.getKey()))) {
                matched.addAll(entry.getValue());
            }
        }
        return matched;
    }
}
