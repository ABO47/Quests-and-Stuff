package com.abo47.questsandstuff.client.tablet.text.format;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class StatTargetFormatter {
    private StatTargetFormatter() {
    }

    public static String displayName(String target) {
        StatTarget parsed = parse(target);
        if (parsed.value().isBlank()) {
            return "";
        }
        if ("custom".equals(parsed.category())) {
            return DisplayNameFormatter.resourceLeaf(parsed.value());
        }
        String name = switch (parsed.category()) {
            case "mined" -> blockName(parsed.value());
            case "crafted", "used", "broken", "picked_up", "dropped" -> itemName(parsed.value());
            case "killed", "killed_by" -> entityName(parsed.value());
            default -> DisplayNameFormatter.resourceLeaf(parsed.value());
        };
        return (verb(parsed.category()) + " " + name).trim();
    }

    private static StatTarget parse(String target) {
        String clean = target == null ? "" : target.trim();
        int split = clean.indexOf(':');
        if (split <= 0) {
            return new StatTarget("custom", clean);
        }
        String category = clean.substring(0, split);
        String value = clean.substring(split + 1);
        return isKnownCategory(category)
                ? new StatTarget(category, value)
                : new StatTarget("custom", clean);
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

    private static String verb(String category) {
        return switch (category) {
            case "mined" -> "Mine";
            case "crafted" -> "Craft";
            case "used" -> "Use";
            case "broken" -> "Break";
            case "picked_up" -> "Pick up";
            case "dropped" -> "Drop";
            case "killed" -> "Kill";
            case "killed_by" -> "Killed by";
            default -> "";
        };
    }

    private static String blockName(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return DisplayNameFormatter.resourceLeaf(value);
        }
        Block block = BuiltInRegistries.BLOCK.get(id);
        if (block == Blocks.AIR && !"minecraft:air".equals(value)) {
            return DisplayNameFormatter.resourceLeaf(value);
        }
        return block.getName().getString();
    }

    private static String itemName(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return DisplayNameFormatter.resourceLeaf(value);
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR && !"minecraft:air".equals(value)) {
            return DisplayNameFormatter.resourceLeaf(value);
        }
        return item.getDescription().getString();
    }

    private static String entityName(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return DisplayNameFormatter.resourceLeaf(value);
        }
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(id);
        if (!id.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entityType))) {
            return DisplayNameFormatter.resourceLeaf(value);
        }
        return entityType.getDescription().getString();
    }

    private record StatTarget(String category, String value) {
    }
}
