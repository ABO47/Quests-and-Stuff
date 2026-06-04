package com.abo47.questsandstuff.client.tablet.icons;


import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerIntegrations;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
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
        if (ItemStackIconCodec.isStackIcon(keyValue)) {
            ItemStack stack = ItemStackIconCodec.stackFromIcon(keyValue);
            if (!stack.isEmpty()) {
                ItemStackTexture texture = new ScopedItemStackTexture(stack);
                ICON_TEXTURE_CACHE.put(keyValue, texture);
                return texture;
            }
        }
        if (FluidIconCodec.isFluidIcon(keyValue)) {
            ItemStack bucket = FluidIconCodec.bucketStack(keyValue);
            if (!bucket.isEmpty()) {
                ItemStackTexture texture = new ScopedItemStackTexture(bucket);
                ICON_TEXTURE_CACHE.put(keyValue, texture);
                return texture;
            }
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
                    ItemStackTexture texture = new ScopedItemStackTexture(stacks);
                    ICON_TEXTURE_CACHE.put(keyValue, texture);
                    return texture;
                }
                List<Item> blockItems = blockTagItems(tagId);
                if (!blockItems.isEmpty()) {
                    ItemStack[] stacks = blockItems.stream().map(ItemStack::new).toArray(ItemStack[]::new);
                    ItemStackTexture texture = new ScopedItemStackTexture(stacks);
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
        ItemStackTexture texture = new ScopedItemStackTexture(item);
        ICON_TEXTURE_CACHE.put(keyValue, texture);
        return texture;
    }

    public static List<String> searchableEntries(String filter, boolean tagMode) {
        String rawQuery = SearchFilter.normalizeUserInput(filter);
        String query = SearchFilter.normalizeKey(rawQuery);

        if (tagMode || rawQuery.startsWith("#")) {
            return searchableTagEntries(rawQuery, query, tagMode);
        }

        return searchableItemEntries(rawQuery, query, item -> true);
    }

    public static List<String> searchableUsableItemEntries(String filter) {
        String rawQuery = SearchFilter.normalizeUserInput(filter);
        String query = SearchFilter.normalizeKey(rawQuery);
        if (rawQuery.startsWith("#")) {
            return searchableTagEntries(rawQuery, query, false);
        }
        return searchableItemEntries(rawQuery, query, QuestIconProvider::isUsableItem);
    }

    public static List<String> searchableFluidEntries(String filter) {
        String rawQuery = SearchFilter.normalizeUserInput(filter);
        String query = SearchFilter.normalizeKey(rawQuery);
        List<String> entries = new ArrayList<>();
        Set<String> candidates = new LinkedHashSet<>();
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid == null || fluid == Fluids.EMPTY) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
            if (id == null || isFlowingFluidId(id)) {
                continue;
            }
            candidates.add(FluidIconCodec.iconFromFluid(fluid));
        }
        candidates.addAll(RecipeViewerIntegrations.fluidEntries());
        for (String icon : candidates) {
            if (!FluidIconCodec.isFluidIcon(icon) || FluidIconCodec.fluidId(icon).isBlank()) {
                continue;
            }
            String key = FluidIconCodec.fluidId(icon);
            String display = FluidIconCodec.displayName(icon);
            String descKey = SearchFilter.normalizeKey(key);
            boolean include = rawQuery.isBlank()
                    || SearchFilter.matches(rawQuery, key, display)
                    || descKey.contains(query);
            if (include) {
                entries.add(icon);
            }
        }
        entries.sort(String::compareTo);
        return entries;
    }

    public static void clearCaches() {
        ICON_TEXTURE_CACHE.clear();
    }

    private static List<String> searchableTagEntries(String rawQuery, String query, boolean tagMode) {
        List<String> entries = new ArrayList<>();
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

    private static List<String> searchableItemEntries(String rawQuery, String query, Predicate<Item> itemFilter) {
        List<String> entries = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (!itemFilter.test(item)) {
                continue;
            }
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

    private static boolean isUsableItem(Item item) {
        if (item == null || item == Items.AIR) {
            return false;
        }
        return new ItemStack(item).getUseDuration() > 0;
    }

    private static boolean isFlowingFluidId(ResourceLocation id) {
        String path = id == null ? "" : id.getPath();
        return path.startsWith("flowing_") || path.startsWith("flowing/") || path.endsWith("_flowing");
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
