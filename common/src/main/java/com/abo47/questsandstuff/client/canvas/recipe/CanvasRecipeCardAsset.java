package com.abo47.questsandstuff.client.canvas.recipe;

import com.abo47.questsandstuff.client.tablet.icons.ItemStackIconCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CanvasRecipeCardAsset {
    public static final String PREFIX = "recipe_card:";
    private static final String RECIPE_MARKER = "@@recipe:";

    private CanvasRecipeCardAsset() {
    }

    public static String assetForPick(String pick) {
        if (isRecipeCardAsset(pick)) {
            String target = target(pick);
            String recipeId = recipeId(pick);
            return recipeId.isBlank() ? assetForTarget(target) : assetForRecipe(target, recipeId);
        }
        String target = normalizePick(pick);
        return assetForTarget(target);
    }

    public static String assetForRecipe(String pick, String recipeId) {
        String target = isRecipeCardAsset(pick) ? target(pick) : normalizePick(pick);
        String normalizedRecipeId = normalizeRecipeId(recipeId);
        if (target.isBlank()) {
            return "";
        }
        return normalizedRecipeId.isBlank() ? assetForTarget(target) : PREFIX + target + RECIPE_MARKER + normalizedRecipeId;
    }

    public static boolean isRecipeCardAsset(String asset) {
        return asset != null && asset.trim().startsWith(PREFIX);
    }

    public static String target(String asset) {
        if (!isRecipeCardAsset(asset)) {
            return "";
        }
        return normalizePick(stripRecipeId(asset.trim().substring(PREFIX.length())));
    }

    public static String recipeId(String asset) {
        if (!isRecipeCardAsset(asset)) {
            return "";
        }
        String body = asset.trim().substring(PREFIX.length());
        int marker = body.lastIndexOf(RECIPE_MARKER);
        if (marker < 0) {
            return "";
        }
        return normalizeRecipeId(body.substring(marker + RECIPE_MARKER.length()));
    }

    public static ItemStack outputStack(String asset) {
        String target = target(asset);
        if (target.isBlank()) {
            return ItemStack.EMPTY;
        }
        if (ItemStackIconCodec.isStackIcon(target)) {
            return ItemStackIconCodec.stackFromIcon(target);
        }
        if (target.startsWith("#")) {
            return firstTagStack(target.substring(1));
        }
        ResourceLocation id = ResourceLocation.tryParse(target);
        if (id == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    public static boolean matchesOutput(String target, ItemStack output) {
        if (target == null || target.isBlank() || output == null || output.isEmpty()) {
            return false;
        }
        if (ItemStackIconCodec.isStackIcon(target)) {
            ItemStack wanted = ItemStackIconCodec.stackFromIcon(target);
            return !wanted.isEmpty() && (wanted.hasTag() ? ItemStack.isSameItemSameTags(output, wanted) : output.is(wanted.getItem()));
        }
        if (target.startsWith("#")) {
            ResourceLocation id = ResourceLocation.tryParse(target.substring(1));
            if (id == null) {
                return false;
            }
            return output.is(TagKey.create(BuiltInRegistries.ITEM.key(), id));
        }
        ResourceLocation id = ResourceLocation.tryParse(target);
        if (id == null) {
            return false;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        return item != null && item != Items.AIR && output.is(item);
    }

    private static String normalizePick(String pick) {
        String value = pick == null ? "" : pick.trim();
        if (value.isBlank()) {
            return "";
        }
        value = stripRecipeId(value);
        if (ItemStackIconCodec.isStackIcon(value)) {
            ItemStack stack = ItemStackIconCodec.stackFromIcon(value);
            return stack.isEmpty() ? "" : ItemStackIconCodec.iconFromStack(stack);
        }
        if (value.startsWith("#")) {
            return normalizeTag(value.substring(1));
        }
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return "";
        }
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        return item == null || item == Items.AIR ? "" : id.toString();
    }

    private static String assetForTarget(String target) {
        return target == null || target.isBlank() ? "" : PREFIX + target;
    }

    private static String stripRecipeId(String value) {
        int marker = value == null ? -1 : value.lastIndexOf(RECIPE_MARKER);
        return marker < 0 ? value : value.substring(0, marker).trim();
    }

    private static String normalizeRecipeId(String recipeId) {
        String value = recipeId == null ? "" : recipeId.trim();
        ResourceLocation id = ResourceLocation.tryParse(value);
        return id == null ? "" : id.toString();
    }

    private static String normalizeTag(String tag) {
        String value = tag == null ? "" : tag.trim();
        if (value.startsWith("#")) {
            value = value.substring(1).trim();
        }
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return "";
        }
        TagKey<Item> key = TagKey.create(BuiltInRegistries.ITEM.key(), id);
        for (var ignored : BuiltInRegistries.ITEM.getTagOrEmpty(key)) {
            return "#" + id;
        }
        return "";
    }

    private static ItemStack firstTagStack(String tag) {
        ResourceLocation id = ResourceLocation.tryParse(tag);
        if (id == null) {
            return ItemStack.EMPTY;
        }
        TagKey<Item> key = TagKey.create(BuiltInRegistries.ITEM.key(), id);
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(key)) {
            Item item = holder.value();
            if (item != Items.AIR) {
                return new ItemStack(item);
            }
        }
        return ItemStack.EMPTY;
    }
}
