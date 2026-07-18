package com.abo47.questsandstuff.client.tablet.preview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;

import com.abo47.questsandstuff.client.tablet.icons.ScopedItemStackTexture;
import com.abo47.questsandstuff.client.tablet.text.format.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiPerfProfiler;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;

public final class ModelAssetPreviewRenderer {
    public static final String ITEM_ASSET_PREFIX = "item:";
    public static final String BLOCK_ASSET_PREFIX = "block:";
    public static final String ITEM_TAG_ASSET_PREFIX = "item_tag:";
    public static final String BLOCK_TAG_ASSET_PREFIX = "block_tag:";
    public static final int DEFAULT_BLOCK_YAW = 45;
    public static final int DEFAULT_BLOCK_PITCH = 30;
    private static final float ICON_BLOCK_FILL = 0.72F;
    private static final float CANVAS_BLOCK_FILL = 0.94F;
    private static final Map<String, ItemStack[]> ITEM_STACK_CACHE = new HashMap<>();
    private static final Map<String, ItemStackTexture> ITEM_TEXTURE_CACHE = new HashMap<>();
    private static final Map<String, List<Block>> BLOCK_TAG_CACHE = new HashMap<>();

    private ModelAssetPreviewRenderer() {
    }

    public static String itemAsset(String itemId) {
        String id = normalizeId(itemId);
        return id.isBlank() ? "" : ITEM_ASSET_PREFIX + id;
    }

    public static String blockAsset(String blockId) {
        String id = normalizeId(blockId);
        return id.isBlank() ? "" : BLOCK_ASSET_PREFIX + id;
    }

    public static String itemTagAsset(String tagId) {
        String id = normalizeTagId(tagId);
        return id.isBlank() ? "" : ITEM_TAG_ASSET_PREFIX + "#" + id;
    }

    public static String blockTagAsset(String tagId) {
        String id = normalizeTagId(tagId);
        return id.isBlank() ? "" : BLOCK_TAG_ASSET_PREFIX + "#" + id;
    }

    public static boolean isItemAsset(String asset) {
        return asset != null && asset.trim().startsWith(ITEM_ASSET_PREFIX);
    }

    public static boolean isBlockAsset(String asset) {
        return asset != null && asset.trim().startsWith(BLOCK_ASSET_PREFIX);
    }

    public static boolean isItemTagAsset(String asset) {
        return asset != null && asset.trim().startsWith(ITEM_TAG_ASSET_PREFIX);
    }

    public static boolean isBlockTagAsset(String asset) {
        return asset != null && asset.trim().startsWith(BLOCK_TAG_ASSET_PREFIX);
    }

    public static boolean isBlockModelAsset(String asset) {
        return isBlockAsset(asset) || isBlockTagAsset(asset);
    }

    public static boolean isModelAsset(String asset) {
        return isItemAsset(asset) || isBlockAsset(asset) || isItemTagAsset(asset) || isBlockTagAsset(asset);
    }

    public static String itemId(String asset) {
        return isItemAsset(asset) ? normalizeId(asset.trim().substring(ITEM_ASSET_PREFIX.length())) : "";
    }

    public static String blockId(String asset) {
        return isBlockAsset(asset) ? normalizeId(asset.trim().substring(BLOCK_ASSET_PREFIX.length())) : "";
    }

    public static String itemTagId(String asset) {
        return isItemTagAsset(asset) ? normalizeTagId(asset.trim().substring(ITEM_TAG_ASSET_PREFIX.length())) : "";
    }

    public static String blockTagId(String asset) {
        return isBlockTagAsset(asset) ? normalizeTagId(asset.trim().substring(BLOCK_TAG_ASSET_PREFIX.length())) : "";
    }

    public static String itemAssetForPick(String pick) {
        String itemId = normalizeItemPick(pick);
        if (!itemId.isBlank()) {
            return itemAsset(itemId);
        }
        String tagId = normalizeItemTagPick(pick);
        return tagId.isBlank() ? "" : itemTagAsset(tagId);
    }

    public static String blockAssetForPick(String pick) {
        String blockId = normalizeBlockPick(pick);
        if (!blockId.isBlank()) {
            return blockAsset(blockId);
        }
        String tagId = normalizeBlockTagPick(pick);
        return tagId.isBlank() ? "" : blockTagAsset(tagId);
    }

    public static String normalizeItemPick(String itemId) {
        String normalized = normalizeId(itemId);
        if (normalized.isBlank() || normalized.startsWith("#")) {
            return "";
        }
        ResourceLocation id = ResourceLocation.tryParse(normalized);
        if (id == null) {
            return "";
        }
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        return item == null || item == Items.AIR ? "" : id.toString();
    }

    public static String normalizeBlockPick(String blockId) {
        String normalized = normalizeId(blockId);
        if (normalized.isBlank() || normalized.startsWith("#")) {
            return "";
        }
        ResourceLocation id = ResourceLocation.tryParse(normalized);
        if (id == null) {
            return "";
        }
        Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
        return isRenderableBlock(block) ? id.toString() : "";
    }

    public static String normalizeItemTagPick(String tagId) {
        String normalized = normalizeTagId(tagId);
        if (normalized.isBlank()) {
            return "";
        }
        ResourceLocation id = ResourceLocation.tryParse(normalized);
        if (id == null) {
            return "";
        }
        TagKey<Item> key = TagKey.create(BuiltInRegistries.ITEM.key(), id);
        return hasItemTagEntries(key) ? id.toString() : "";
    }

    public static String normalizeBlockTagPick(String tagId) {
        String normalized = normalizeTagId(tagId);
        if (normalized.isBlank()) {
            return "";
        }
        ResourceLocation id = ResourceLocation.tryParse(normalized);
        if (id == null) {
            return "";
        }
        TagKey<Block> key = TagKey.create(BuiltInRegistries.BLOCK.key(), id);
        return hasBlockTagEntries(key) ? id.toString() : "";
    }

    public static boolean renderModelAsset(GuiGraphics graphics, int x, int y, int width, int height, String asset) {
        return renderModelAsset(graphics, x, y, width, height, asset, DEFAULT_BLOCK_YAW, DEFAULT_BLOCK_PITCH);
    }

    public static boolean renderModelAsset(GuiGraphics graphics, int x, int y, int width, int height, String asset, int yawDegrees, int pitchDegrees) {
        if (isBlockModelAsset(asset)) {
            return renderBlockAsset(graphics, x, y, width, height, asset, yawDegrees, pitchDegrees);
        }
        ItemStackTexture texture = textureForAsset(asset);
        if (texture == null) {
            return false;
        }
        texture.draw(graphics, 0, 0, x, y, width, height);
        return true;
    }

    public static boolean renderBlockModelAssetAtCenter(GuiGraphics graphics, int centerX, int centerY, int width, int height, String asset, int yawDegrees, int pitchDegrees) {
        return renderBlockModelAssetAtCenter(graphics, centerX, centerY, width, height, asset, yawDegrees, pitchDegrees, ICON_BLOCK_FILL);
    }

    public static boolean renderCanvasBlockModelAssetAtCenter(GuiGraphics graphics, int centerX, int centerY, int width, int height, String asset, int yawDegrees, int pitchDegrees) {
        return renderBlockModelAssetAtCenter(graphics, centerX, centerY, width, height, asset, yawDegrees, pitchDegrees, CANVAS_BLOCK_FILL);
    }

    private static boolean renderBlockModelAssetAtCenter(GuiGraphics graphics, int centerX, int centerY, int width, int height, String asset, int yawDegrees, int pitchDegrees, float fill) {
        Block block = blockForAsset(asset);
        if (block == null || width <= 0 || height <= 0) {
            return false;
        }
        int size = Math.max(1, Math.min(width, height));
        renderBlockPreview(graphics, centerX, centerY, size, block.defaultBlockState(), yawDegrees, pitchDegrees, fill);
        return true;
    }

    public static Component[] modelTooltip(String asset) {
        String label = modelDisplayName(asset);
        String id = modelDisplayId(asset);
        if (label.isBlank() || id.isBlank()) {
            return new Component[]{Component.literal(asset == null ? "" : asset).withStyle(ChatFormatting.GRAY)};
        }
        return new Component[]{
                Component.literal(label).withStyle(ChatFormatting.WHITE),
                Component.literal(id).withStyle(ChatFormatting.DARK_GRAY)
        };
    }

    private static ItemStack[] stacksForAsset(String asset) {
        String key = asset == null ? "" : asset.trim();
        if (key.isBlank()) {
            return new ItemStack[0];
        }
        return ITEM_STACK_CACHE.computeIfAbsent(key, ModelAssetPreviewRenderer::createStacksForAsset);
    }

    private static ItemStackTexture textureForAsset(String asset) {
        ItemStack[] stacks = stacksForAsset(asset);
        if (stacks.length == 0) {
            return null;
        }
        String key = asset == null ? "" : asset.trim();
        return ITEM_TEXTURE_CACHE.computeIfAbsent(key, ignored -> new ScopedItemStackTexture(stacks));
    }

    private static ItemStack[] createStacksForAsset(String asset) {
        if (isItemAsset(asset)) {
            ItemStack stack = itemStack(itemId(asset));
            return stack.isEmpty() ? new ItemStack[0] : new ItemStack[]{stack};
        }
        if (isItemTagAsset(asset)) {
            return itemTagStacks(itemTagId(asset));
        }
        return new ItemStack[0];
    }

    private static ItemStack itemStack(String itemId) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static boolean renderBlockAsset(GuiGraphics graphics, int x, int y, int width, int height, String asset, int yawDegrees, int pitchDegrees) {
        return renderBlockModelAssetAtCenter(graphics, x + width / 2, y + height / 2, width, height, asset, yawDegrees, pitchDegrees);
    }

    private static void renderBlockPreview(GuiGraphics graphics, int centerX, int centerY, int size, BlockState state, int yawDegrees, int pitchDegrees, float fill) {
        float scale = Math.max(1.0F, size * fill);
        TabletUiPerfProfiler.profile("ui.renderBlockPreview", () -> renderBlockPreviewInternal(graphics, centerX, centerY, state, yawDegrees, pitchDegrees, scale));
    }

    private static void renderBlockPreviewInternal(GuiGraphics graphics, int centerX, int centerY, BlockState state, int yawDegrees, int pitchDegrees, float scale) {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        Lighting.setupFor3DItems();
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 120.0F);
        graphics.pose().mulPoseMatrix(new Matrix4f().scaling(scale, -scale, scale));
        graphics.pose().mulPose(new Quaternionf()
                .rotateXYZ((float) Math.toRadians(CanvasImageLayer.normalizeDegrees(pitchDegrees)), (float) Math.toRadians(CanvasImageLayer.normalizeDegrees(yawDegrees)), 0.0F));
        graphics.pose().translate(-0.5D, -0.5D, -0.5D);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                state,
                graphics.pose(),
                graphics.bufferSource(),
                15728880,
                OverlayTexture.NO_OVERLAY
        );
        graphics.flush();
        graphics.pose().popPose();
        Lighting.setupFor3DItems();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
    }

    private static Block blockForAsset(String asset) {
        if (isBlockAsset(asset)) {
            return blockById(blockId(asset));
        }
        if (isBlockTagAsset(asset)) {
            return blockFromTag(blockTagId(asset));
        }
        return null;
    }

    private static Block blockById(String blockId) {
        ResourceLocation id = ResourceLocation.tryParse(blockId);
        if (id == null) {
            return null;
        }
        Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
        return isRenderableBlock(block) ? block : null;
    }

    private static Block blockFromTag(String tagId) {
        ResourceLocation id = ResourceLocation.tryParse(tagId);
        if (id == null) {
            return null;
        }
        TagKey<Block> key = TagKey.create(BuiltInRegistries.BLOCK.key(), id);
        List<Block> blocks = BLOCK_TAG_CACHE.computeIfAbsent(id.toString(), ignored -> blocksForTag(key));
        return blocks.isEmpty() ? null : blocks.get(cyclingIndex(blocks.size()));
    }

    private static List<Block> blocksForTag(TagKey<Block> key) {
        List<Block> blocks = new ArrayList<>();
        for (var holder : BuiltInRegistries.BLOCK.getTagOrEmpty(key)) {
            Block block = holder.value();
            if (isRenderableBlock(block)) {
                blocks.add(block);
            }
        }
        return List.copyOf(blocks);
    }

    private static ItemStack[] itemTagStacks(String tagId) {
        ResourceLocation id = ResourceLocation.tryParse(tagId);
        if (id == null) {
            return new ItemStack[0];
        }
        TagKey<Item> key = TagKey.create(BuiltInRegistries.ITEM.key(), id);
        List<ItemStack> stacks = new ArrayList<>();
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(key)) {
            Item item = holder.value();
            if (item != Items.AIR) {
                stacks.add(new ItemStack(item));
            }
        }
        return stacks.toArray(ItemStack[]::new);
    }

    private static String modelDisplayName(String asset) {
        if (isItemAsset(asset)) {
            ItemStack stack = itemStack(itemId(asset));
            return stack.isEmpty() ? "" : stack.getHoverName().getString();
        }
        if (isBlockAsset(asset)) {
            Block block = blockById(blockId(asset));
            return block == null ? "" : block.getName().getString();
        }
        if (isItemTagAsset(asset)) {
            return DisplayNameFormatter.resourceLeaf(itemTagId(asset));
        }
        if (isBlockTagAsset(asset)) {
            return DisplayNameFormatter.resourceLeaf(blockTagId(asset));
        }
        return "";
    }

    private static String modelDisplayId(String asset) {
        if (isItemAsset(asset)) {
            return itemId(asset);
        }
        if (isBlockAsset(asset)) {
            return blockId(asset);
        }
        if (isItemTagAsset(asset)) {
            return "#" + itemTagId(asset);
        }
        if (isBlockTagAsset(asset)) {
            return "#" + blockTagId(asset);
        }
        return "";
    }

    private static boolean hasItemTagEntries(TagKey<Item> tag) {
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            if (holder.value() != Items.AIR) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasBlockTagEntries(TagKey<Block> tag) {
        for (var holder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
            if (isRenderableBlock(holder.value())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRenderableBlock(Block block) {
        return block != null && block != Blocks.AIR && block != Blocks.CAVE_AIR && block != Blocks.VOID_AIR;
    }

    private static int cyclingIndex(int size) {
        if (size <= 1) {
            return 0;
        }
        return (int) ((System.currentTimeMillis() / 900L) % size);
    }

    private static String normalizeId(String id) {
        return id == null ? "" : id.trim();
    }

    private static String normalizeTagId(String id) {
        String normalized = normalizeId(id);
        return normalized.startsWith("#") ? normalized.substring(1).trim() : normalized;
    }
}
