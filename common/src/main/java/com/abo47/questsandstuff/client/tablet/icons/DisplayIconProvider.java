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

public final class DisplayIconProvider {
    private static final Map<String, ItemStackTexture> ICON_TEXTURE_CACHE = new HashMap<>();
    private static List<String> ALL_ITEM_IDS;
    private static List<String> ALL_TAG_IDS;
    private static List<String> ALL_FLUID_ICONS;

    private DisplayIconProvider() {
    }

    public static void prewarm() {
        if (ALL_ITEM_IDS != null) {
            return;
        }
        ALL_ITEM_IDS = BuiltInRegistries.ITEM.stream()
                .map(BuiltInRegistries.ITEM::getKey)
                .filter(id -> id != null)
                .map(ResourceLocation::toString)
                .sorted()
                .toList();
        ALL_TAG_IDS = BuiltInRegistries.ITEM.getTagNames()
                .map(tag -> "#" + tag.location())
                .sorted()
                .toList();
    }

    public static void prewarmFluidEntries() {
        if (ALL_FLUID_ICONS != null) {
            return;
        }
        ALL_FLUID_ICONS = new ArrayList<>();
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid == null || fluid == Fluids.EMPTY) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
            if (id == null || isFlowingFluidId(id)) {
                continue;
            }
            String icon = FluidIconCodec.iconFromFluid(fluid);
            if (!icon.isBlank()) {
                ALL_FLUID_ICONS.add(icon);
            }
        }
        ALL_FLUID_ICONS.addAll(RecipeViewerIntegrations.fluidEntries());
        ALL_FLUID_ICONS.sort(String::compareTo);
    }

    static void invalidateCaches() {
        ALL_ITEM_IDS = null;
        ALL_TAG_IDS = null;
        ALL_FLUID_ICONS = null;
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
                List<Item> items = new ArrayList<>();
                for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(key)) {
                    Item item = holder.value();
                    if (item != Items.AIR) {
                        items.add(item);
                    }
                }
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
        return searchableItemEntries(rawQuery, query, DisplayIconProvider::isUsableItem);
    }

    public static List<String> searchableFluidEntries(String filter) {
        String rawQuery = SearchFilter.normalizeUserInput(filter);
        String query = SearchFilter.normalizeKey(rawQuery);
        List<String> entries = new ArrayList<>();
        List<String> source = ALL_FLUID_ICONS;
        if (source == null) {
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
            source = candidates.stream()
                    .filter(icon -> FluidIconCodec.isFluidIcon(icon) && !FluidIconCodec.fluidId(icon).isBlank())
                    .sorted()
                    .toList();
        }
        for (String icon : source) {
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
        return entries;
    }

    public static void clearCaches() {
        ICON_TEXTURE_CACHE.clear();
        invalidateCaches();
    }

    private static List<String> searchableTagEntries(String rawQuery, String query, boolean tagMode) {
        List<String> entries = new ArrayList<>();
        String tagRawQuery = tagMode ? rawQuery : rawQuery.substring(1);
        String tagQuery = tagMode ? query : SearchFilter.normalizeKey(tagRawQuery);
        List<String> source = ALL_TAG_IDS != null ? ALL_TAG_IDS : BuiltInRegistries.ITEM.getTagNames()
                .map(tag -> "#" + tag.location())
                .sorted()
                .toList();
        for (String tag : source) {
            String stripped = tag.startsWith("#") ? tag.substring(1) : tag;
            if (tagRawQuery.isBlank() || SearchFilter.matches(tagRawQuery, stripped, stripped) || SearchFilter.normalizeKey(stripped).contains(tagQuery)) {
                entries.add(tag);
            }
        }
        return entries;
    }

    private static List<String> searchableItemEntries(String rawQuery, String query, Predicate<Item> itemFilter) {
        List<String> entries = new ArrayList<>();
        List<String> source = ALL_ITEM_IDS;
        if (source == null) {
            source = BuiltInRegistries.ITEM.stream()
                    .map(BuiltInRegistries.ITEM::getKey)
                    .filter(id -> id != null)
                    .map(ResourceLocation::toString)
                    .sorted()
                    .toList();
        }
        for (String key : source) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            if (id == null) continue;
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item == null || item == Items.AIR || !itemFilter.test(item)) {
                continue;
            }
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
        List<Item> items = new ArrayList<>();
        for (var holder : BuiltInRegistries.BLOCK.getTagOrEmpty(key)) {
            Item item = holder.value().asItem();
            if (item != Items.AIR) {
                items.add(item);
            }
        }
        return items;
    }
}
