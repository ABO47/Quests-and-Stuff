package com.abo47.questsandstuff.client.tablet.details.objective;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.text.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.StatTargetFormatter;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Locale;

final class QuestObjectiveDisplayText {
    private QuestObjectiveDisplayText() {
    }

    static int amount(JsonObject json) {
        if (json == null || !json.has("amount")) {
            return 1;
        }
        try {
            return Math.max(1, json.get("amount").getAsInt());
        } catch (Exception ignored) {
            return 1;
        }
    }

    static int parsePositive(String value, int fallback) {
        try {
            return Math.max(1, Math.min(99999, Integer.parseInt(value == null ? "" : value.trim())));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    static String taskIcon(JsonObject json) {
        String explicitIcon = QuestObjectiveJsons.asString(json, "icon", "");
        String path = QuestObjectiveJsons.typePath(QuestObjectiveJsons.asString(json, "type", ""));
        if ("xp".equals(path) && (explicitIcon.isBlank() || "xp".equals(explicitIcon))) {
            return QuestObjectiveJsons.XP_CARD_ICON;
        }
        if (!explicitIcon.isBlank()) {
            return explicitIcon;
        }
        String tag = QuestObjectiveJsons.asString(json, "tag", "");
        if (!tag.isBlank()) {
            return "#" + tag;
        }
        String target = QuestObjectiveJsons.asString(json, "target", "");
        if (isEntityTask(path) && !target.isBlank()) {
            return EntityPreviewRenderer.entityAsset(target);
        }
        return QuestObjectiveJsons.firstPresent(json, "item", "fallback_item", "minecraft:book");
    }

    static String rewardIcon(JsonObject json) {
        String path = QuestObjectiveJsons.typePath(QuestObjectiveJsons.asString(json, "type", ""));
        String explicitIcon = QuestObjectiveJsons.asString(json, "icon", "");
        if ("xp".equals(path) && (explicitIcon.isBlank() || "xp".equals(explicitIcon))) {
            return QuestObjectiveJsons.XP_CARD_ICON;
        }
        if ("loot_table".equals(path) || "loot".equals(path)) {
            return QuestObjectiveJsons.firstPresent(json, "icon", "minecraft:chest");
        }
        return QuestObjectiveJsons.firstPresent(json, "icon", "item", "fallback_item", QuestObjectiveJsons.rewardFallback(path));
    }

    static String displayName(JsonObject json, String type) {
        String path = QuestObjectiveJsons.typePath(type);
        String title = QuestObjectiveJsons.asString(json, "title", "");
        if (!title.isBlank()) {
            return title;
        }
        if ("biome".equals(path)) {
            return QuestVocabulary.text(QuestVocabulary.VISIT_TARGET, readableIdName(QuestObjectiveJsons.asString(json, "target", "")));
        }
        if ("advancement".equals(path)) {
            String advancementName = DisplayNameFormatter.advancement(
                    QuestObjectiveJsons.asString(json, "target", ""),
                    ClientQuestCache.advancementDisplays()
            );
            return advancementName.isBlank() ? typeLabel(type) : advancementName;
        }
        if ("recipe".equals(path)) {
            String recipeTarget = QuestObjectiveJsons.asString(json, "target", "");
            if (recipeTarget.startsWith("#")) {
                return readableTagName(recipeTarget);
            }
            String recipeName = itemName(recipeTarget);
            return recipeName.isBlank() ? typeLabel(type) : recipeName;
        }
        if ("structure".equals(path)) {
            String structureName = DisplayNameFormatter.resourceLeaf(QuestObjectiveJsons.asString(json, "target", ""));
            return structureName.isBlank() ? typeLabel(type) : QuestVocabulary.text(QuestVocabulary.VISIT_TARGET, structureName);
        }
        if ("block_interact".equals(path) || "block_interaction".equals(path)) {
            String blockName = blockTargetName(QuestObjectiveJsons.asString(json, "target", ""));
            return blockName.isBlank() ? typeLabel(type) : QuestVocabulary.text(QuestVocabulary.INTERACT_TARGET, blockName);
        }
        if ("stat".equals(path)) {
            String statName = StatTargetFormatter.displayName(QuestObjectiveJsons.asString(json, "target", ""));
            return statName.isBlank() ? typeLabel(type) : statName;
        }
        if ("location".equals(path)) {
            return QuestVocabulary.text(QuestVocabulary.VISIT_TARGET, readableIdName(QuestObjectiveJsons.asString(json, "dimension", "")));
        }
        if ("kill_entity".equals(path)) {
            String entityId = QuestObjectiveJsons.asString(json, "target", "");
            String entityName = EntityPreviewRenderer.entityDisplayName(entityId);
            return entityName.isBlank()
                    ? QuestVocabulary.text(QuestVocabulary.KILL_ENTITY)
                    : QuestVocabulary.text(QuestVocabulary.KILL_ENTITY_NAMED, entityName);
        }
        if ("entity_interact".equals(path) || "entity_interaction".equals(path)) {
            String entityName = EntityPreviewRenderer.entityDisplayName(QuestObjectiveJsons.asString(json, "target", ""));
            return entityName.isBlank() ? typeLabel(type) : QuestVocabulary.text(QuestVocabulary.INTERACT_TARGET, entityName);
        }
        if ("loot_table".equals(path) || "loot".equals(path)) {
            String lootName = QuestObjectiveLootTableRewardEditor.displayName(QuestObjectiveJsons.asString(json, "loot_table", ""));
            if (!lootName.isBlank()) {
                return lootName;
            }
        }
        String tag = QuestObjectiveJsons.asString(json, "tag", "");
        if (!tag.isBlank()) {
            return readableTagName(tag);
        }
        String item = QuestObjectiveJsons.firstPresent(json, "item", "fallback_item", "icon");
        if (!item.isBlank()) {
            String name = itemName(item);
            if (!name.isBlank()) {
                return name;
            }
        }
        String subtitle = QuestObjectiveJsons.firstPresent(json, "target", "dimension", "loot_table", "command");
        return subtitle.isBlank() ? typeLabel(type) : subtitle;
    }

    static String typeLabel(String type) {
        String path = QuestObjectiveJsons.typePath(type);
        String spaced = path.replace('_', ' ');
        return spaced.isBlank() ? QuestVocabulary.text(QuestVocabulary.COMMON_UNKNOWN) : Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    static boolean isManualTask(JsonObject json) {
        String path = QuestObjectiveJsons.typePath(QuestObjectiveJsons.asString(json, "type", ""));
        return "check".equals(path) || "dummy".equals(path);
    }

    static boolean isManualXpTask(JsonObject json) {
        String path = QuestObjectiveJsons.typePath(QuestObjectiveJsons.asString(json, "type", ""));
        String collection = QuestObjectiveJsons.asString(json, "collection", "automatic");
        return "xp".equals(path) && ("manual".equals(collection) || "consume".equals(collection));
    }

    static boolean usesAmountField(JsonObject json, boolean task) {
        String path = QuestObjectiveJsons.typePath(QuestObjectiveJsons.asString(json, "type", ""));
        if (task) {
            return !isManualTask(json)
                    && !"biome".equals(path)
                    && !"advancement".equals(path)
                    && !"structure".equals(path)
                    && !"block_interact".equals(path)
                    && !"block_interaction".equals(path)
                    && !"location".equals(path);
        }
        return !"command".equals(path) && !"loot_table".equals(path) && !"loot".equals(path) && !"selectable".equals(path);
    }

    static String manualTarget(JsonObject json, String fallback) {
        String target = QuestObjectiveJsons.asString(json, "target", "");
        return target.isBlank() ? fallback : target;
    }

    private static boolean isEntityTask(String typePath) {
        return "kill_entity".equals(typePath)
                || "entity_interact".equals(typePath)
                || "entity_interaction".equals(typePath);
    }

    private static String itemName(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return "";
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR && !"minecraft:air".equals(value)) {
            return value;
        }
        return item.getDescription().getString();
    }

    private static String blockName(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return "";
        }
        Block block = BuiltInRegistries.BLOCK.get(id);
        if (block == Blocks.AIR && !"minecraft:air".equals(value)) {
            return value;
        }
        return block.getName().getString();
    }

    private static String blockTargetName(String value) {
        String clean = value == null ? "" : value.trim();
        if (clean.startsWith("#")) {
            return DisplayNameFormatter.resourceLeaf(clean.substring(1));
        }
        return blockName(clean);
    }

    private static String readableIdName(String value) {
        String clean = value == null ? "" : value.trim();
        int colon = clean.indexOf(':');
        if (colon >= 0) {
            clean = clean.substring(colon + 1);
        }
        clean = clean.replace('/', ' ').replace('_', ' ').trim();
        return clean.isBlank() ? QuestVocabulary.text(QuestVocabulary.BIOME_FALLBACK) : clean.toLowerCase(Locale.ROOT);
    }

    private static String readableTagName(String tag) {
        String value = tag == null ? "" : tag.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        int colon = value.indexOf(':');
        String path = colon >= 0 ? value.substring(colon + 1) : value;
        path = path.replace('\\', '/');
        if (path.isBlank()) {
            return QuestVocabulary.text(QuestVocabulary.ANY_ITEM);
        }
        String[] parts = path.split("/");
        String name;
        if (parts.length >= 2) {
            String group = singular(parts[0]);
            String material = prettify(parts[parts.length - 1]);
            name = material + " " + group;
        } else {
            name = singular(prettify(path));
        }
        return QuestVocabulary.text(QuestVocabulary.ANY_NAMED_ITEM, name);
    }

    private static String prettify(String value) {
        return value == null ? "" : value.replace('_', ' ').trim().toLowerCase(Locale.ROOT);
    }

    private static String singular(String value) {
        String clean = prettify(value);
        if (clean.endsWith("ies") && clean.length() > 3) {
            return clean.substring(0, clean.length() - 3) + "y";
        }
        if (clean.endsWith("sses") || clean.endsWith("ches") || clean.endsWith("shes")) {
            return clean.substring(0, clean.length() - 2);
        }
        if (clean.endsWith("s") && !clean.endsWith("ss")) {
            return clean.substring(0, clean.length() - 1);
        }
        return clean;
    }
}
