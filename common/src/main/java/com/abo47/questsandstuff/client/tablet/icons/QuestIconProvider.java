package com.abo47.questsandstuff.client.tablet.icons;


import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class QuestIconProvider {
    private static final Map<String, ItemStackTexture> ICON_TEXTURE_CACHE = new HashMap<>();

    private QuestIconProvider() {
    }

    public static ItemStackTexture iconTexture(String iconId) {
        String keyValue = iconId == null || iconId.isBlank() ? "minecraft:book" : iconId;
        ItemStackTexture cached = ICON_TEXTURE_CACHE.get(keyValue);
        if (cached != null) {
            return cached;
        }
        if (keyValue.startsWith("#")) {
            ResourceLocation tagId = ResourceLocation.tryParse(keyValue.substring(1));
            if (tagId != null) {
                TagKey<Item> key = TagKey.create(BuiltInRegistries.ITEM.key(), tagId);
                List<Item> items = BuiltInRegistries.ITEM.stream()
                        .filter(it -> it.builtInRegistryHolder().is(key))
                        .collect(Collectors.toList());
                if (!items.isEmpty()) {
                    ItemStack[] stacks = items.stream().map(ItemStack::new).toArray(ItemStack[]::new);
                    ItemStackTexture texture = new ItemStackTexture(stacks);
                    ICON_TEXTURE_CACHE.put(keyValue, texture);
                    return texture;
                }
                List<Item> blockItems = blockTagItems(tagId);
                if (!blockItems.isEmpty()) {
                    ItemStack[] stacks = blockItems.stream().map(ItemStack::new).toArray(ItemStack[]::new);
                    ItemStackTexture texture = new ItemStackTexture(stacks);
                    ICON_TEXTURE_CACHE.put(keyValue, texture);
                    return texture;
                }
            }
        }

        ResourceLocation key = ResourceLocation.tryParse(keyValue);
        Item item = key == null ? Items.BOOK : BuiltInRegistries.ITEM.get(key);
        if (item == null || item == Items.AIR) {
            item = Items.BOOK;
        }
        ItemStackTexture texture = new ItemStackTexture(item);
        ICON_TEXTURE_CACHE.put(keyValue, texture);
        return texture;
    }

    public static List<String> searchableEntries(String filter, boolean tagMode) {
        String rawQuery = SearchFilter.normalizeUserInput(filter);
        String query = SearchFilter.normalizeKey(rawQuery);
        List<String> entries = new ArrayList<>();

        if (tagMode || rawQuery.startsWith("#")) {
            String tagRawQuery = tagMode ? rawQuery : rawQuery.substring(1);
            String tagQuery = tagMode ? query : SearchFilter.normalizeKey(tagRawQuery);
            List<String> tags = BuiltInRegistries.ITEM.getTagNames()
                    .map(TagKey::location)
                    .map(ResourceLocation::toString)
                    .filter(id -> tagRawQuery.isBlank() || SearchFilter.matches(tagRawQuery, id, id) || SearchFilter.normalizeKey(id).contains(tagQuery))
                    .sorted()
                    .toList();
            for (String tag : tags) {
                entries.add("#" + tag);
            }
            return entries;
        }

        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null) {
                continue;
            }
            String key = id.toString();
            String display = item.getDescription().getString();
            String descKey = SearchFilter.normalizeKey(item.getDescriptionId());
            boolean include;
            if (rawQuery.isBlank()) {
                include = true;
            } else {
                include = SearchFilter.matches(rawQuery, key, display) || descKey.contains(query);
            }
            if (include) {
                entries.add(key);
            }
        }
        entries.sort(String::compareTo);
        return entries;
    }

    public static void clearCaches() {
        ICON_TEXTURE_CACHE.clear();
    }

    private static List<Item> blockTagItems(ResourceLocation tagId) {
        TagKey<Block> key = TagKey.create(BuiltInRegistries.BLOCK.key(), tagId);
        return BuiltInRegistries.BLOCK.stream()
                .filter(block -> block.builtInRegistryHolder().is(key))
                .map(Block::asItem)
                .filter(item -> item != Items.AIR)
                .collect(Collectors.toList());
    }
}
