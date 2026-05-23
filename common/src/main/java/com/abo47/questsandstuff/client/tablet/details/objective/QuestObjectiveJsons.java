package com.abo47.questsandstuff.client.tablet.details.objective;

import com.abo47.questsandstuff.util.StableIdAllocator;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;

final class QuestObjectiveJsons {
    static final String MOD = "questsandstuff:";
    static final String XP_CARD_ICON = "minecraft:experience_bottle";

    private QuestObjectiveJsons() {
    }

    static JsonObject read(String value) {
        try {
            return JsonParser.parseString(value == null || value.isBlank() ? "{}" : value).getAsJsonObject();
        } catch (Exception ignored) {
            return new JsonObject();
        }
    }

    static String asString(JsonObject json, String key, String fallback) {
        if (json == null || !json.has(key) || json.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return json.get(key).getAsString();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    static boolean asBoolean(JsonObject json, String key, boolean fallback) {
        if (json == null || !json.has(key) || json.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return json.get(key).getAsBoolean();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    static String firstPresent(JsonObject json, String... keys) {
        for (String key : keys) {
            String value = asString(json, key, "");
            if (!value.isBlank()) {
                return value;
            }
        }
        if (keys.length > 0 && keys[keys.length - 1].contains(":")) {
            return keys[keys.length - 1];
        }
        return "";
    }

    static String typePath(String type) {
        String value = type == null ? "" : type;
        int colon = value.indexOf(':');
        return colon >= 0 ? value.substring(colon + 1) : value;
    }

    static JsonObject defaultTask(String id, String typePath) {
        String type = MOD + typePath;
        if ("xp".equals(typePath)) {
            JsonObject json = base(id, type);
            json.addProperty("amount", 1);
            json.addProperty("mode", "points");
            json.addProperty("collection", "automatic");
            json.addProperty("icon", XP_CARD_ICON);
            return json;
        }
        if ("stat".equals(typePath)) {
            return simpleTask(id, type, "minecraft:jump", "stat");
        }
        if ("location".equals(typePath)) {
            JsonObject json = base(id, type);
            json.addProperty("mode", "dimension");
            json.addProperty("dimension", "minecraft:overworld");
            json.addProperty("x", 0);
            json.addProperty("y", 64);
            json.addProperty("z", 0);
            json.addProperty("radius", 6);
            json.addProperty("icon", "minecraft:compass");
            return json;
        }
        if ("check".equals(typePath) || "dummy".equals(typePath)) {
            JsonObject json = base(id, type);
            json.addProperty("target", id);
            json.addProperty("icon", "manual_check");
            return json;
        }
        if ("item".equals(typePath)) {
            JsonObject json = base(id, type);
            json.addProperty("item", "minecraft:stone");
            json.addProperty("amount", 1);
            json.addProperty("nbt", "");
            json.addProperty("collection", "automatic");
            return json;
        }
        return simpleTask(id, type, defaultTarget(typePath), fallbackIcon(typePath));
    }

    static JsonObject defaultReward(String id, String typePath) {
        String type = MOD + typePath;
        JsonObject json = base(id, type);
        if ("xp".equals(typePath)) {
            json.addProperty("amount", 1);
            json.addProperty("mode", "points");
            json.addProperty("icon", XP_CARD_ICON);
        } else if ("loot_table".equals(typePath) || "loot".equals(typePath)) {
            json.addProperty("loot_table", "minecraft:chests/simple_dungeon");
            json.addProperty("title", "Simple dungeon");
            json.addProperty("icon", "minecraft:chest");
        } else if ("command".equals(typePath)) {
            json.addProperty("command", "say Quest reward");
            json.addProperty("title", "Command");
            json.addProperty("icon", "minecraft:command_block");
        }
        return json;
    }

    static JsonObject simpleTask(String id, String type, String target, String icon) {
        JsonObject json = base(id, type);
        json.addProperty("amount", 1);
        json.addProperty("target", target == null ? "" : target);
        json.addProperty("icon", icon == null ? "" : icon);
        return json;
    }

    static JsonObject base(String id, String type) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("type", type);
        return json;
    }

    static String defaultTarget(String typePath) {
        return switch (typePath) {
            case "kill_entity" -> "minecraft:zombie";
            case "advancement" -> "minecraft:story/root";
            case "recipe" -> "recipe";
            case "structure" -> "minecraft:village";
            case "block_interact", "block_interaction" -> "minecraft:crafting_table";
            case "entity_interact", "entity_interaction" -> "minecraft:villager";
            case "changed_dimension" -> "minecraft:the_nether";
            default -> "";
        };
    }

    static String fallbackIcon(String typePath) {
        return switch (typePath) {
            case "kill_entity" -> "";
            case "advancement" -> "minecraft:book";
            case "recipe" -> "recipe";
            case "structure" -> "minecraft:map";
            case "biome" -> "biome";
            case "block_interact", "block_interaction" -> "minecraft:oak_button";
            case "entity_interact", "entity_interaction" -> "minecraft:lead";
            case "item_interact", "item_interaction", "item_use" -> "minecraft:stick";
            case "changed_dimension" -> "minecraft:obsidian";
            case "xp" -> XP_CARD_ICON;
            case "stat" -> "stat";
            case "location" -> "minecraft:compass";
            default -> "minecraft:book";
        };
    }

    static String rewardFallback(String typePath) {
        return switch (typePath) {
            case "xp" -> XP_CARD_ICON;
            case "loot", "loot_table" -> "minecraft:chest";
            case "command" -> "minecraft:command_block";
            case "selectable" -> "minecraft:bundle";
            default -> "minecraft:diamond";
        };
    }

    static String nextId(CompoundTag existing, String prefix) {
        return StableIdAllocator.nextId(prefix == null || prefix.isBlank() ? "entry" : prefix, existing == null ? java.util.Set.of() : existing.getAllKeys());
    }
}
