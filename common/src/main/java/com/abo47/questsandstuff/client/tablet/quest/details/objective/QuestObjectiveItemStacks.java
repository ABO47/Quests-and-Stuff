package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.client.tablet.icons.ItemStackIconCodec;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class QuestObjectiveItemStacks {
    private QuestObjectiveItemStacks() {
    }

    static ItemStack iconStack(JsonObject json) {
        String itemId = QuestObjectiveJsons.firstPresent(json, "item", "fallback_item");
        String icon = QuestObjectiveJsons.asString(json, "icon", "");
        if (ItemStackIconCodec.isStackIcon(icon)) {
            return ItemStackIconCodec.stackFromIcon(icon);
        }
        if (hasCustomIcon(json, itemId)) {
            return ItemStack.EMPTY;
        }
        String nbt = QuestObjectiveJsons.asString(json, "nbt", "");
        if (nbt.isBlank()) {
            return ItemStack.EMPTY;
        }
        return stackFromItemData(itemId, nbt, 1);
    }

    static ItemStack viewerStack(JsonObject json) {
        String icon = QuestObjectiveJsons.asString(json, "icon", "");
        if (ItemStackIconCodec.isStackIcon(icon)) {
            return ItemStackIconCodec.stackFromIcon(icon);
        }
        String itemId = QuestObjectiveJsons.firstPresent(json, "item", "fallback_item", "icon");
        if (itemId.isBlank() || itemId.startsWith("#")) {
            return ItemStack.EMPTY;
        }
        int amount = Math.max(1, asInt(json, "amount", 1));
        return stackFromItemData(itemId, QuestObjectiveJsons.asString(json, "nbt", ""), amount);
    }

    private static int asInt(JsonObject json, String key, int fallback) {
        if (json == null || !json.has(key) || json.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return json.get(key).getAsInt();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static ItemStack stackFromItemData(String itemId, String nbt, int amount) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item, Math.max(1, amount));
        if (!nbt.isBlank()) {
            try {
                stack.setTag(TagParser.parseTag(nbt));
            } catch (Exception ignored) {
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    private static boolean hasCustomIcon(JsonObject json, String itemId) {
        String icon = QuestObjectiveJsons.asString(json, "icon", "");
        if (icon.isBlank()) {
            return false;
        }
        return !icon.equals(itemId) && !icon.equals(QuestObjectiveJsons.asString(json, "fallback_item", ""));
    }
}
