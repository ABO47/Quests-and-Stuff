package com.abo47.questsandstuff.client.tablet.theme.skin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR;

public class CenterCropTexture implements IGuiTexture {
    private final ResourceLocation imageLocation;
    private final int origW;
    private final int origH;

    public CenterCropTexture(ResourceLocation imageLocation, int origW, int origH) {
        this.imageLocation = imageLocation;
        this.origW = origW;
        this.origH = origH;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        if (width <= 0 || height <= 0 || origW <= 0 || origH <= 0) return;
        int drawX = Math.round(x + (width - origW) / 2f);
        int drawY = Math.round(y + (height - origH) / 2f);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, imageLocation);
        var matrix = graphics.pose().last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR);
        buffer.vertex(matrix, drawX, drawY + origH, 0).uv(0, 1).color(-1).endVertex();
        buffer.vertex(matrix, drawX + origW, drawY + origH, 0).uv(1, 1).color(-1).endVertex();
        buffer.vertex(matrix, drawX + origW, drawY, 0).uv(1, 0).color(-1).endVertex();
        buffer.vertex(matrix, drawX, drawY, 0).uv(0, 0).color(-1).endVertex();
        tessellator.end();
    }
}
