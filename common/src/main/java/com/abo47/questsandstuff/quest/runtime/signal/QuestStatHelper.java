package com.abo47.questsandstuff.quest.runtime.signal;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class QuestStatHelper {
    private QuestStatHelper() {
    }

    public static int readStat(ServerPlayer player, String statKey) {
        if (statKey == null || statKey.isBlank()) {
            return 0;
        }

        StatTarget target = parse(statKey.trim());
        ResourceLocation id = ResourceLocation.tryParse(target.value());
        if (!"custom".equals(target.category()) && id == null) {
            return 0;
        }

        return switch (target.category()) {
            case "custom" -> readCustom(player, target.value());
            case "mined" -> readBlock(player, id, Stats.BLOCK_MINED);
            case "crafted" -> readItem(player, id, Stats.ITEM_CRAFTED);
            case "used" -> readItem(player, id, Stats.ITEM_USED);
            case "broken" -> readItem(player, id, Stats.ITEM_BROKEN);
            case "picked_up" -> readItem(player, id, Stats.ITEM_PICKED_UP);
            case "dropped" -> readItem(player, id, Stats.ITEM_DROPPED);
            case "killed" -> readEntity(player, id, Stats.ENTITY_KILLED);
            case "killed_by" -> readEntity(player, id, Stats.ENTITY_KILLED_BY);
            default -> 0;
        };
    }

    private static StatTarget parse(String statKey) {
        int split = statKey.indexOf(':');
        if (split <= 0) {
            return new StatTarget("custom", statKey);
        }

        String category = statKey.substring(0, split);
        String value = statKey.substring(split + 1);
        if (isKnownCategory(category)) {
            return new StatTarget(category, value);
        }
        if ("minecraft".equals(category)) {
            int nestedSplit = value.indexOf(':');
            if (nestedSplit > 0) {
                String nestedCategory = value.substring(0, nestedSplit);
                if (isKnownCategory(nestedCategory)) {
                    return new StatTarget(nestedCategory, value.substring(nestedSplit + 1));
                }
            }
        }
        return new StatTarget("custom", statKey);
    }

    private static boolean isKnownCategory(String category) {
        return "custom".equals(category)
                || "mined".equals(category)
                || "crafted".equals(category)
                || "used".equals(category)
                || "broken".equals(category)
                || "picked_up".equals(category)
                || "dropped".equals(category)
                || "killed".equals(category)
                || "killed_by".equals(category);
    }

    private static int readCustom(ServerPlayer player, String raw) {
        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) {
            return 0;
        }
        ResourceLocation registered = BuiltInRegistries.CUSTOM_STAT.getOptional(id).orElse(null);
        if (registered == null) {
            return 0;
        }
        Stat<ResourceLocation> stat = Stats.CUSTOM.get(registered);
        return player.getStats().getValue(stat);
    }

    private static int readBlock(ServerPlayer player, ResourceLocation id, StatType<Block> type) {
        Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
        return block == null ? 0 : player.getStats().getValue(type.get(block));
    }

    private static int readItem(ServerPlayer player, ResourceLocation id, StatType<Item> type) {
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        return item == null ? 0 : player.getStats().getValue(type.get(item));
    }

    private static int readEntity(ServerPlayer player, ResourceLocation id, StatType<EntityType<?>> type) {
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        return entityType == null ? 0 : player.getStats().getValue(type.get(entityType));
    }

    private record StatTarget(String category, String value) {
    }
}
