package com.abo47.questsandstuff.client.tablet.icons;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

public final class SmoothResourceTexture extends ResourceTexture {
    public SmoothResourceTexture(ResourceLocation imageLocation) {
        super(imageLocation);
    }

    @Override
    protected void drawSubAreaInternal(GuiGraphics graphics, float x, float y, float width, float height, float drawnU, float drawnV, float drawnWidth, float drawnHeight) {
        RenderSystem.setShaderTexture(0, imageLocation);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        super.drawSubAreaInternal(graphics, x, y, width, height, drawnU, drawnV, drawnWidth, drawnHeight);
    }
}
