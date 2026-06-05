package com.abo47.questsandstuff.client.canvas.recipe;

import com.abo47.questsandstuff.client.tablet.icons.ItemStackIconCodec;
import com.abo47.questsandstuff.client.tablet.icons.FluidIconCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CanvasRecipeCardAsset {
    public static final String PREFIX = "recipe_card:";
    private static final String RECIPE_MARKER = "@@recipe:";
    private static final String VIEWER_TYPE_MARKER = "@@viewer_type:";

    private CanvasRecipeCardAsset() {
    }

    public static String assetForPick(String pick) {
        if (isRecipeCardAsset(pick)) {
            String target = target(pick);
            String recipeId = recipeId(pick);
            return recipeId.isBlank() ? assetForTarget(target) : assetForRecipe(target, recipeId, viewerTypeId(pick));
        }
        String target = normalizePick(pick);
        return assetForTarget(target);
    }

    public static String assetForRecipe(String pick, String recipeId) {
        return assetForRecipe(pick, recipeId, isRecipeCardAsset(pick) ? viewerTypeId(pick) : "");
    }

    public static String assetForRecipe(String pick, String recipeId, String viewerTypeId) {
        String target = isRecipeCardAsset(pick) ? target(pick) : normalizePick(pick);
        String normalizedRecipeId = normalizeRecipeId(recipeId);
        String normalizedViewerTypeId = normalizeRecipeId(viewerTypeId);
        if (target.isBlank()) {
            return "";
        }
        if (normalizedRecipeId.isBlank()) {
            return assetForTarget(target);
        }
        String asset = PREFIX + target + RECIPE_MARKER + normalizedRecipeId;
        return normalizedViewerTypeId.isBlank() ? asset : asset + VIEWER_TYPE_MARKER + normalizedViewerTypeId;
    }

    public static boolean isRecipeCardAsset(String asset) {
        return asset != null && asset.trim().startsWith(PREFIX);
    }

    public static String target(String asset) {
        if (!isRecipeCardAsset(asset)) {
            return "";
        }
        return normalizePick(stripMetadata(asset.trim().substring(PREFIX.length())));
    }

    public static String recipeId(String asset) {
        if (!isRecipeCardAsset(asset)) {
            return "";
        }
        String body = asset.trim().substring(PREFIX.length());
        return normalizeRecipeId(metadataValue(body, RECIPE_MARKER));
    }

    public static String viewerTypeId(String asset) {
        if (!isRecipeCardAsset(asset)) {
            return "";
        }
        String body = asset.trim().substring(PREFIX.length());
        return normalizeRecipeId(metadataValue(body, VIEWER_TYPE_MARKER));
    }

    public static ItemStack outputStack(String asset) {
        String target = target(asset);
        if (target.isBlank()) {
            return ItemStack.EMPTY;
        }
        if (ItemStackIconCodec.isStackIcon(target)) {
            return ItemStackIconCodec.stackFromIcon(target);
        }
        if (FluidIconCodec.isFluidIcon(target)) {
            return FluidIconCodec.bucketStack(target);
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
        if (FluidIconCodec.isFluidIcon(target)) {
            ItemStack bucket = FluidIconCodec.bucketStack(target);
            return !bucket.isEmpty() && output.is(bucket.getItem());
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
        value = stripMetadata(value);
        if (ItemStackIconCodec.isStackIcon(value)) {
            ItemStack stack = ItemStackIconCodec.stackFromIcon(value);
            return stack.isEmpty() ? "" : ItemStackIconCodec.iconFromStack(stack);
        }
        if (FluidIconCodec.isFluidIcon(value)) {
            return FluidIconCodec.fluidId(value).isBlank() ? "" : value;
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

    private static String stripMetadata(String value) {
        int marker = firstMetadataMarker(value);
        return marker < 0 ? value : value.substring(0, marker).trim();
    }

    private static int firstMetadataMarker(String value) {
        if (value == null) {
            return -1;
        }
        int recipe = value.indexOf(RECIPE_MARKER);
        int viewerType = value.indexOf(VIEWER_TYPE_MARKER);
        if (recipe < 0) {
            return viewerType;
        }
        if (viewerType < 0) {
            return recipe;
        }
        return Math.min(recipe, viewerType);
    }

    private static String metadataValue(String body, String marker) {
        int markerIndex = body == null ? -1 : body.lastIndexOf(marker);
        if (markerIndex < 0) {
            return "";
        }
        int start = markerIndex + marker.length();
        int next = body.indexOf("@@", start);
        return (next < 0 ? body.substring(start) : body.substring(start, next)).trim();
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
        if (BuiltInRegistries.ITEM.getTagOrEmpty(key).iterator().hasNext()) {
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
