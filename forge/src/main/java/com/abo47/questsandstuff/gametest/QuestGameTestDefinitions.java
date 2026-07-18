package com.abo47.questsandstuff.gametest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.reward.CommandQuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.reward.ItemQuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.reward.LootTableQuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.reward.QuestRewards;
import com.abo47.questsandstuff.quest.model.reward.SelectableQuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.reward.XpQuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.generic.CheckQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.generic.CompositeQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.generic.SimpleQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.item.CollectionMode;
import com.abo47.questsandstuff.quest.model.task.item.GatherItemQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.player.LocationQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.player.StatQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.player.XpMode;
import com.abo47.questsandstuff.quest.model.task.player.XpQuestTaskDefinition;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;

final class QuestGameTestDefinitions {
    private QuestGameTestDefinitions() {
    }

    static QuestTaskDefinition task(String id, String type, int goal, String target, Map<String, String> args) {
        ResourceLocation taskType = id(type);
        String path = taskType.getPath();
        if ("item".equals(path)) {
            return new GatherItemQuestTaskDefinition(id, taskType, item(target), args.getOrDefault("nbt", ""), goal, CollectionMode.fromWire(args.get("collection_mode")));
        }
        if ("xp".equals(path)) {
            return new XpQuestTaskDefinition(id, taskType, goal, XpMode.fromWire(args.get("mode")), CollectionMode.fromWire(args.get("collection_mode")));
        }
        if ("stat".equals(path)) {
            return new StatQuestTaskDefinition(id, taskType, goal, target, args.getOrDefault("icon", ""));
        }
        if ("location".equals(path)) {
            return new LocationQuestTaskDefinition(
                    id,
                    taskType,
                    args.getOrDefault("mode", "dimension"),
                    target,
                    integer(args, "x", 0),
                    integer(args, "y", 0),
                    integer(args, "z", 0),
                    integer(args, "radius", 6),
                    args.getOrDefault("icon", "")
            );
        }
        if ("check".equals(path) || "dummy".equals(path)) {
            return new CheckQuestTaskDefinition(id, taskType, target);
        }
        if ("composite".equals(path)) {
            return new CompositeQuestTaskDefinition(id, taskType, integer(args, "required", goal), split(args.getOrDefault("children", "")));
        }
        return new SimpleQuestTaskDefinition(id, taskType, signal(path), goal, target, args.getOrDefault("icon", ""));
    }

    static QuestRewardDefinition reward(String id, String type, int amount, String payload, boolean selectable, Map<String, String> args) {
        ResourceLocation rewardType = id(type);
        String path = rewardType.getPath();
        if (selectable || "selectable".equals(path)) {
            Map<String, QuestRewardDefinition> choices = new LinkedHashMap<>();
            for (String choice : split(args.getOrDefault("choices", ""))) {
                choices.put(choice, new ItemQuestRewardDefinition(choice, QuestRewards.id("item"), item("minecraft:carrot"), 1, ""));
            }
            if (choices.isEmpty()) {
                choices.put("choice", new ItemQuestRewardDefinition("choice", QuestRewards.id("item"), item("minecraft:carrot"), 1, ""));
            }
            return new SelectableQuestRewardDefinition(id, rewardType, integer(args, "pick_count", amount), choices);
        }
        if ("item".equals(path)) {
            return new ItemQuestRewardDefinition(id, rewardType, item(payload), amount, args.getOrDefault("nbt", ""));
        }
        if ("xp".equals(path)) {
            return new XpQuestRewardDefinition(id, rewardType, amount, XpMode.fromWire(args.get("mode")));
        }
        if ("command".equals(path)) {
            return new CommandQuestRewardDefinition(id, rewardType, args.getOrDefault("command", payload));
        }
        if ("loot".equals(path) || "loot_table".equals(path)) {
            return new LootTableQuestRewardDefinition(id, rewardType, id(payload.isBlank() ? "minecraft:empty" : payload), "", "");
        }
        return new XpQuestRewardDefinition(id, QuestRewards.id("xp"), amount, XpMode.POINTS);
    }

    private static ResourceLocation id(String value) {
        if (value == null || value.isBlank()) {
            return ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "unknown");
        }
        return value.contains(":") ? ResourceLocation.tryParse(value) : ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, value);
    }

    private static ResourceLocation item(String value) {
        return id(value == null || value.isBlank() ? "minecraft:air" : value);
    }

    private static QuestSignalType signal(String path) {
        return switch (path) {
            case "advancement" -> QuestSignalType.ADVANCEMENT;
            case "recipe" -> QuestSignalType.ITEM_CRAFTED;
            case "structure" -> QuestSignalType.STRUCTURE_ENTER;
            case "biome" -> QuestSignalType.BIOME_ENTER;
            case "block_interact", "block_interaction" -> QuestSignalType.BLOCK_INTERACT;
            case "entity_interact", "entity_interaction" -> QuestSignalType.ENTITY_INTERACT;
            case "item_interact", "item_interaction" -> QuestSignalType.ITEM_INTERACT;
            case "item_use" -> QuestSignalType.ITEM_USED;
            case "changed_dimension" -> QuestSignalType.DIMENSION_CHANGED;
            default -> QuestSignalType.ENTITY_KILLED;
        };
    }

    private static int integer(Map<String, String> args, String key, int fallback) {
        try {
            return Integer.parseInt(args.getOrDefault(key, String.valueOf(fallback)));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static List<String> split(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .toList();
    }
}
