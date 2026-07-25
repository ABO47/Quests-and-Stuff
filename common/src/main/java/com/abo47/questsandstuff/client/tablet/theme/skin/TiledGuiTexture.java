package com.abo47.questsandstuff.client.tablet.theme.skin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import com.abo47.questsandstuff.QuestsAndStuffMod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR;

public class TiledGuiTexture implements IGuiTexture {
    private final ResourceLocation imageLocation;
    private final int tileW;
    private final int tileH;
    private boolean logged = false;

    public TiledGuiTexture(ResourceLocation imageLocation, int tileW, int tileH) {
        this.imageLocation = imageLocation;
        this.tileW = tileW;
        this.tileH = tileH;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        if (width <= 0 || height <= 0 || tileW <= 0 || tileH <= 0) return;

        if (!logged) {
            QuestsAndStuffMod.debugLog("[QnS:Skin] TiledGuiTexture.draw: tile={}x{}, target={}x{}", tileW, tileH, width, height);
            logged = true;
        }

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, imageLocation);
        var matrix = graphics.pose().last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR);
        for (int tx = 0; tx < width; tx += tileW) {
            for (int ty = 0; ty < height; ty += tileH) {
                float dw = Math.min(tileW, width - tx);
                float dh = Math.min(tileH, height - ty);
                float u = dw / tileW;
                float v = dh / tileH;
                float fx = x + tx;
                float fy = y + ty;
                buffer.vertex(matrix, fx, fy + dh, 0).uv(0, v).color(-1).endVertex();
                buffer.vertex(matrix, fx + dw, fy + dh, 0).uv(u, v).color(-1).endVertex();
                buffer.vertex(matrix, fx + dw, fy, 0).uv(u, 0).color(-1).endVertex();
                buffer.vertex(matrix, fx, fy, 0).uv(0, 0).color(-1).endVertex();
            }
        }
        tessellator.end();
    }
}
