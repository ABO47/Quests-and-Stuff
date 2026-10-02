package com.abo47.questsandstuff.client.tablet.assets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import com.abo47.questsandstuff.client.tablet.icons.ItemStackIconCodec;
import com.abo47.questsandstuff.client.tablet.preview.ModelAssetPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.theme.skin.GameItemTexture;

final class GameTextureCache {
    private static final Map<String, IGuiTexture> TEXTURE_CACHE = new HashMap<>();
    private static final AssetLibrary.AssetDimensions SPRITE_DIMS = new AssetLibrary.AssetDimensions(16, 16);

    private GameTextureCache() {
    }

    static boolean isGameRef(String ref) {
        if (ref == null) {
            return false;
        }
        String value = ref.trim();
        if (value.isEmpty()) {
            return false;
        }
        if (value.startsWith(ModelAssetPreviewRenderer.ITEM_ASSET_PREFIX)
                || value.startsWith(ModelAssetPreviewRenderer.BLOCK_ASSET_PREFIX)
                || value.startsWith(ModelAssetPreviewRenderer.ITEM_TAG_ASSET_PREFIX)
                || value.startsWith(ModelAssetPreviewRenderer.BLOCK_TAG_ASSET_PREFIX)
                || value.startsWith("item_stack|")
                || value.startsWith("#")) {
            return true;
        }
        if (value.contains("/") || value.contains("\\") || value.contains(".")) {
            return false;
        }
        return ResourceLocation.tryParse(value) != null;
    }

    static IGuiTexture backgroundTexture(String ref, boolean grayscale) {
        return modeTexture(ref, "stretch", 0, 0, 0, 0);
    }

    static IGuiTexture modeTexture(String ref, String mode, int leftEdge, int rightEdge, int topEdge, int bottomEdge) {
        String key = ref + "|" + mode + ":" + leftEdge + ":" + rightEdge + ":" + topEdge + ":" + bottomEdge;
        IGuiTexture cached = TEXTURE_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        ItemStack[] stacks = gameStacks(ref);
        if (stacks.length == 0) {
            return null;
        }
        IGuiTexture out = new GameItemTexture(stacks, mode, leftEdge, rightEdge, topEdge, bottomEdge);
        TEXTURE_CACHE.put(key, out);
        return out;
    }

    static AssetLibrary.AssetDimensions dimensions(String ref) {
        return gameStack(ref).isEmpty() ? null : SPRITE_DIMS;
    }

    static ItemStack gameStack(String ref) {
        String value = ref == null ? "" : ref.trim();
        if (value.startsWith("item_stack|")) {
            ItemStack stack = ItemStackIconCodec.stackFromIcon(value);
            return stack == null ? ItemStack.EMPTY : stack;
        }
        if (value.startsWith(ModelAssetPreviewRenderer.ITEM_ASSET_PREFIX)) {
            return itemStack(ModelAssetPreviewRenderer.normalizeItemPick(value.substring(ModelAssetPreviewRenderer.ITEM_ASSET_PREFIX.length())));
        }
        if (value.startsWith(ModelAssetPreviewRenderer.ITEM_TAG_ASSET_PREFIX)) {
            return firstOrEmpty(itemTagStacks(ModelAssetPreviewRenderer.normalizeItemTagPick(value.substring(ModelAssetPreviewRenderer.ITEM_TAG_ASSET_PREFIX.length()))));
        }
        if (value.startsWith(ModelAssetPreviewRenderer.BLOCK_ASSET_PREFIX)) {
            return itemStack(ModelAssetPreviewRenderer.normalizeBlockPick(value.substring(ModelAssetPreviewRenderer.BLOCK_ASSET_PREFIX.length())));
        }
        if (value.startsWith(ModelAssetPreviewRenderer.BLOCK_TAG_ASSET_PREFIX)) {
            return firstOrEmpty(blockTagStacks(ModelAssetPreviewRenderer.normalizeBlockTagPick(value.substring(ModelAssetPreviewRenderer.BLOCK_TAG_ASSET_PREFIX.length()))));
        }
        if (value.startsWith("#")) {
            String tagId = value.substring(1).trim();
            ItemStack[] tagStacks = itemTagStacks(tagId);
            if (tagStacks.length == 0) {
                tagStacks = blockTagStacks(tagId);
            }
            return firstOrEmpty(tagStacks);
        }
        ItemStack plain = itemStack(value);
        if (!plain.isEmpty()) {
            return plain;
        }
        return itemStackForBlock(value);
    }

    private static ItemStack itemStack(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(location).orElse(null);
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static ItemStack itemStackForBlock(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            return ItemStack.EMPTY;
        }
        Block block = BuiltInRegistries.BLOCK.getOptional(location).orElse(null);
        if (block == null || block.asItem() == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(block.asItem());
    }

    static ItemStack[] gameStacks(String ref) {
        String value = ref == null ? "" : ref.trim();
        if (value.startsWith(ModelAssetPreviewRenderer.ITEM_TAG_ASSET_PREFIX)) {
            return itemTagStacks(ModelAssetPreviewRenderer.normalizeItemTagPick(value.substring(ModelAssetPreviewRenderer.ITEM_TAG_ASSET_PREFIX.length())));
        }
        if (value.startsWith(ModelAssetPreviewRenderer.BLOCK_TAG_ASSET_PREFIX)) {
            return blockTagStacks(ModelAssetPreviewRenderer.normalizeBlockTagPick(value.substring(ModelAssetPreviewRenderer.BLOCK_TAG_ASSET_PREFIX.length())));
        }
        if (value.startsWith("#")) {
            String tagId = value.substring(1).trim();
            ItemStack[] tagStacks = itemTagStacks(tagId);
            return tagStacks.length == 0 ? blockTagStacks(tagId) : tagStacks;
        }
        ItemStack single = gameStack(value);
        return single.isEmpty() ? new ItemStack[0] : new ItemStack[]{single};
    }

    private static ItemStack firstOrEmpty(ItemStack[] stacks) {
        return stacks.length == 0 ? ItemStack.EMPTY : stacks[0];
    }

    private static ItemStack[] itemTagStacks(String tagId) {
        ResourceLocation id = ResourceLocation.tryParse(tagId);
        if (id == null) {
            return new ItemStack[0];
        }
        TagKey<Item> key = TagKey.create(BuiltInRegistries.ITEM.key(), id);
        List<ItemStack> out = new ArrayList<>();
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(key)) {
            if (holder.value() != Items.AIR) {
                out.add(new ItemStack(holder.value()));
            }
        }
        return out.toArray(ItemStack[]::new);
    }

    private static ItemStack[] blockTagStacks(String tagId) {
        ResourceLocation id = ResourceLocation.tryParse(tagId);
        if (id == null) {
            return new ItemStack[0];
        }
        TagKey<Block> key = TagKey.create(BuiltInRegistries.BLOCK.key(), id);
        List<ItemStack> out = new ArrayList<>();
        for (var holder : BuiltInRegistries.BLOCK.getTagOrEmpty(key)) {
            Block block = holder.value();
            if (block != null && block.asItem() != Items.AIR) {
                out.add(new ItemStack(block.asItem()));
            }
        }
        return out.toArray(ItemStack[]::new);
    }
}
