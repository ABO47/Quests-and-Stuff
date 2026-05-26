package com.abo47.questsandstuff.client.tablet.icons;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ScopedItemStackTexture extends ItemStackTexture {
    private int index;
    private int ticks;
    private long lastTick;

    public ScopedItemStackTexture(ItemStack... itemStacks) {
        super(itemStacks);
    }

    public ScopedItemStackTexture(Item... items) {
        super(items);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void updateTick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            long tick = minecraft.level.getGameTime();
            if (tick == lastTick) {
                return;
            }
            lastTick = tick;
        }
        if (items.length > 1 && ++ticks % 20 == 0) {
            index = (index + 1) % items.length;
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected void drawInternal(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        if (items.length == 0) {
            return;
        }
        updateTick();
        ItemStack stack = items[Math.max(0, Math.min(index, items.length - 1))];
        if (stack == null || stack.isEmpty()) {
            return;
        }

        applyColor();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0f);
        graphics.pose().scale(width / 16.0f, height / 16.0f, 1.0f);
        Minecraft minecraft = Minecraft.getInstance();
        graphics.renderItem(stack, 0, 0);
        graphics.renderItemDecorations(minecraft.font, stack, 0, 0);
        graphics.pose().popPose();
        graphics.flush();
        restoreGuiState(graphics);
    }

    private void applyColor() {
        int color = getColor();
        float alpha = ((color >>> 24) & 0xFF) / 255.0f;
        float red = ((color >>> 16) & 0xFF) / 255.0f;
        float green = ((color >>> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    private static void restoreGuiState(GuiGraphics graphics) {
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
    }
}
