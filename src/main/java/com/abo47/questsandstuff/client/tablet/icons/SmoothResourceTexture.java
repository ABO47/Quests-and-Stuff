package com.abo47.questsandstuff.client.tablet.icons;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class SmoothResourceTexture extends ResourceTexture {
    public SmoothResourceTexture(ResourceLocation imageLocation) {
        super(imageLocation);
    }

    @Override
    protected void drawSubAreaInternal(GuiGraphics graphics, float x, float y, float width, float height, float drawnU, float drawnV, float drawnWidth, float drawnHeight) {
        setFilter(GL11.GL_LINEAR);
        super.drawSubAreaInternal(graphics, x, y, width, height, drawnU, drawnV, drawnWidth, drawnHeight);
        setFilter(GL11.GL_NEAREST);
    }

    private void setFilter(int filter) {
        RenderSystem.setShaderTexture(0, imageLocation);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
    }
}
