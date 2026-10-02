package com.abo47.questsandstuff.client.tablet.theme.skin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import com.abo47.questsandstuff.client.tablet.icons.ScopedItemStackTexture;

public class GameItemTexture implements IGuiTexture {
    private static final int SPRITE = 16;
    private final IGuiTexture item;
    private final String mode;
    private final int tileW;
    private final int tileH;

    public GameItemTexture(ItemStack[] stacks, String mode, int leftEdge, int rightEdge, int topEdge, int bottomEdge) {
        this.item = new ScopedItemStackTexture(stacks == null ? new ItemStack[0] : stacks);
        this.mode = mode == null ? "stretch" : mode;
        if ("tile_size".equals(this.mode)) {
            this.tileW = leftEdge > 0 ? leftEdge : SPRITE;
            this.tileH = rightEdge > 0 ? rightEdge : SPRITE;
        } else {
            this.tileW = SPRITE;
            this.tileH = SPRITE;
        }
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        if ("center".equals(mode)) {
            item.draw(graphics, mouseX, mouseY, x + (width - SPRITE) / 2f, y + (height - SPRITE) / 2f, SPRITE, SPRITE);
            return;
        }
        if ("tile".equals(mode) || "tile_size".equals(mode)) {
            for (int ty = 0; ty + tileH <= height; ty += tileH) {
                for (int tx = 0; tx + tileW <= width; tx += tileW) {
                    item.draw(graphics, mouseX, mouseY, x + tx, y + ty, tileW, tileH);
                }
            }
            return;
        }
        item.draw(graphics, mouseX, mouseY, x, y, width, height);
    }
}
