package com.abo47.questsandstuff.quest.runtime.signal;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
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

        int split = statKey.indexOf(':');
        if (split <= 0) {
            return readCustom(player, statKey);
        }

        String category = statKey.substring(0, split);
        String value = statKey.substring(split + 1);
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return 0;
        }

        return switch (category) {
            case "custom" -> readCustom(player, value);
            case "mined" -> readBlock(player, id, Stats.BLOCK_MINED);
            case "crafted" -> readItem(player, id, Stats.ITEM_CRAFTED);
            case "used" -> readItem(player, id, Stats.ITEM_USED);
            case "broken" -> readItem(player, id, Stats.ITEM_BROKEN);
            case "picked_up" -> readItem(player, id, Stats.ITEM_PICKED_UP);
            case "dropped" -> readItem(player, id, Stats.ITEM_DROPPED);
            case "killed" -> readEntity(player, id, Stats.ENTITY_KILLED);
            case "killed_by" -> readEntity(player, id, Stats.ENTITY_KILLED_BY);
            default -> readCustom(player, statKey);
        };
    }

    private static int readCustom(ServerPlayer player, String raw) {
        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) {
            return 0;
        }
        Stat<ResourceLocation> stat = Stats.CUSTOM.get(id);
        return player.getStats().getValue(stat);
    }

    private static int readBlock(ServerPlayer player, ResourceLocation id, net.minecraft.stats.StatType<Block> type) {
        Block block = BuiltInRegistries.BLOCK.get(id);
        return block == null ? 0 : player.getStats().getValue(type.get(block));
    }

    private static int readItem(ServerPlayer player, ResourceLocation id, net.minecraft.stats.StatType<Item> type) {
        Item item = BuiltInRegistries.ITEM.get(id);
        return item == null ? 0 : player.getStats().getValue(type.get(item));
    }

    private static int readEntity(ServerPlayer player, ResourceLocation id, net.minecraft.stats.StatType<EntityType<?>> type) {
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(id);
        return entityType == null ? 0 : player.getStats().getValue(type.get(entityType));
    }
}
