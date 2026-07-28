package com.abo47.questsandstuff.client.tablet.quest.details.task;

import net.minecraft.nbt.CompoundTag;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.util.naming.StableIdAllocator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

final class TaskJsonFactory {
    static final String MOD = "questsandstuff:";
    static final String XP_CARD_ICON = "minecraft:experience_bottle";

    private TaskJsonFactory() {
    }

    static JsonObject read(String value) {
        return readResult(value).value();
    }

    static ParseResult readResult(String value) {
        String source = value == null || value.isBlank() ? "{}" : value;
        try {
            JsonElement parsed = JsonParser.parseString(source);
            if (parsed == null || !parsed.isJsonObject()) {
                return ParseResult.invalid(new JsonObject(), "expected JSON object");
            }
            return ParseResult.valid(parsed.getAsJsonObject());
        } catch (Exception error) {
            return ParseResult.invalid(new JsonObject(), error.getClass().getSimpleName() + ": " + safeMessage(error));
        }
    }

    static JsonObject readTaskForEdit(String questId, String taskId, String value) {
        return readForEdit(questId, taskId, true, value).value();
    }

    static JsonObject readRewardForEdit(String questId, String rewardId, String value) {
        return readForEdit(questId, rewardId, false, value).value();
    }

    static ParseResult readForEdit(String questId, String entryId, boolean task, String value) {
        ParseResult result = readResult(value);
        if (!result.valid()) {
            QuestsAndStuffMod.debugLog(
                    "[QnS:UI] malformed task json quest={} {}={} diagnostic={}",
                    safeId(questId),
                    task ? "task" : "reward",
                    safeId(entryId),
                    result.diagnostic()
            );
        }
        return result;
    }

    static String asString(JsonObject json, String key, String fallback) {
        if (json == null || !json.has(key) || json.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return json.get(key).getAsString();
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.debugLog(
                    "[QnS:UI] task json string fallback key={} fallback={} diagnostic={}",
                    key,
                    fallback,
                    exception.toString()
            );
            return fallback;
        }
    }

    static boolean asBoolean(JsonObject json, String key, boolean fallback) {
        if (json == null || !json.has(key) || json.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return json.get(key).getAsBoolean();
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.debugLog(
                    "[QnS:UI] task json boolean fallback key={} fallback={} diagnostic={}",
                    key,
                    fallback,
                    exception.toString()
            );
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
        QuestDetailsTypeChoice choice = QuestTaskTypeCatalog.taskChoice(typePath);
        return choice == null ? simpleDefaultTask(id, typePath) : choice.defaultJson(id);
    }

    static JsonObject defaultReward(String id, String typePath) {
        QuestDetailsTypeChoice choice = QuestTaskTypeCatalog.rewardChoice(typePath);
        return choice == null ? base(id, MOD + typePath) : choice.defaultJson(id);
    }

    static JsonObject simpleDefaultTask(String id, String typePath) {
        return simpleTask(id, MOD + typePath, defaultTarget(typePath), fallbackIcon(typePath));
    }

    static JsonObject xpTask(String id, String typePath) {
        JsonObject json = base(id, MOD + typePath);
        json.addProperty("amount", 1);
        json.addProperty("mode", "points");
        json.addProperty("collection", "automatic");
        json.addProperty("icon", XP_CARD_ICON);
        return json;
    }

    static JsonObject statTask(String id, String typePath) {
        return simpleTask(id, MOD + typePath, "minecraft:jump", "stat");
    }

    static JsonObject locationTask(String id, String typePath) {
        JsonObject json = base(id, MOD + typePath);
        json.addProperty("mode", "dimension");
        json.addProperty("dimension", "minecraft:overworld");
        json.addProperty("x", 0);
        json.addProperty("y", 64);
        json.addProperty("z", 0);
        json.addProperty("radius", 6);
        json.addProperty("icon", "minecraft:compass");
        return json;
    }

    static JsonObject checkTask(String id, String typePath) {
        JsonObject json = base(id, MOD + typePath);
        json.addProperty("target", id);
        json.addProperty("icon", "manual_check");
        return json;
    }

    static JsonObject itemTask(String id, String typePath) {
        JsonObject json = base(id, MOD + typePath);
        json.addProperty("item", "minecraft:stone");
        json.addProperty("amount", 1);
        json.addProperty("nbt", "");
        json.addProperty("collection", "automatic");
        return json;
    }

    static JsonObject itemReward(String id, String typePath) {
        JsonObject json = base(id, MOD + typePath);
        json.addProperty("item", "minecraft:diamond");
        json.addProperty("amount", 1);
        json.addProperty("nbt", "");
        json.addProperty("icon", "minecraft:diamond");
        return json;
    }

    static JsonObject xpReward(String id, String typePath) {
        JsonObject json = base(id, MOD + typePath);
        json.addProperty("amount", 1);
        json.addProperty("mode", "points");
        json.addProperty("icon", XP_CARD_ICON);
        return json;
    }

    static JsonObject lootTableReward(String id, String typePath) {
        JsonObject json = base(id, MOD + typePath);
        json.addProperty("loot_table", "minecraft:chests/simple_dungeon");
        json.addProperty("title", "Simple dungeon");
        json.addProperty("icon", "minecraft:chest");
        return json;
    }

    static JsonObject commandReward(String id, String typePath) {
        JsonObject json = base(id, MOD + typePath);
        json.addProperty("command", "say Quest reward");
        json.addProperty("title", "Command");
        json.addProperty("icon", "minecraft:command_block");
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
            case "entity_interact", "entity_interaction" -> "";
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

    private static String safeId(String value) {
        return value == null ? "" : value;
    }

    private static String safeMessage(Exception error) {
        String message = error.getMessage();
        return message == null || message.isBlank() ? "invalid JSON" : message;
    }

    record ParseResult(boolean valid, JsonObject value, String diagnostic) {
        static ParseResult valid(JsonObject value) {
            return new ParseResult(true, value == null ? new JsonObject() : value, "");
        }

        static ParseResult invalid(JsonObject value, String diagnostic) {
            String safeDiagnostic = diagnostic == null || diagnostic.isBlank() ? "invalid JSON" : diagnostic;
            return new ParseResult(false, value == null ? new JsonObject() : value, safeDiagnostic);
        }
    }
}
