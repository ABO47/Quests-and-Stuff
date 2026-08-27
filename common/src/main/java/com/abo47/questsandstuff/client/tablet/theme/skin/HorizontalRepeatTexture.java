package com.abo47.questsandstuff.client.tablet.theme.skin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import com.abo47.questsandstuff.QuestsAndStuffMod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR;

public class HorizontalRepeatTexture implements IGuiTexture {
    private final ResourceLocation imageLocation;
    private final int texW;
    private final int texH;
    private final int leftEdge;
    private final int rightEdge;
    private boolean logged = false;

    public HorizontalRepeatTexture(ResourceLocation imageLocation, int texW, int texH, int leftEdge, int rightEdge) {
        this.imageLocation = imageLocation;
        this.texW = texW;
        this.texH = texH;
        this.leftEdge = leftEdge;
        this.rightEdge = rightEdge;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        if (width <= 0 || height <= 0 || texW <= 0 || texH <= 0) return;

        int L = Math.min(leftEdge, texW);
        int R = Math.min(rightEdge, texW - L);
        int midW = texW - L - R;

        if (!logged) {
            QuestsAndStuffMod.debugLog("[QnS:Skin] HorizontalRepeatTexture.draw: tex={}x{}, target={}x{}, edges=({},{})",
                    texW, texH, width, height, L, R);
            logged = true;
        }

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, imageLocation);
        var matrix = graphics.pose().last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR);

        if (midW <= 0) {
            slice(buffer, matrix, x, y, width, height, 0, 1, 0, 1);
            tessellator.end();
            return;
        }

        float vTop = 0;
        float vBot = 1;
        float uMid0 = (float) L / texW;
        float uMid1 = (float) (L + midW) / texW;

        if (L > 0) {
            slice(buffer, matrix, x, y, L, height, 0, (float) L / texW, vTop, vBot);
        }
        if (R > 0) {
            slice(buffer, matrix, x + width - R, y, R, height, (float) (texW - R) / texW, 1, vTop, vBot);
        }

        int gap = width - L - R;
        float cx = x + L;
        int drawn = 0;
        while (drawn < gap) {
            int dw = Math.min(midW, gap - drawn);
            float u0 = uMid0;
            float u1 = uMid0 + (uMid1 - uMid0) * (dw / (float) midW);
            slice(buffer, matrix, cx, y, dw, height, u0, u1, vTop, vBot);
            cx += dw;
            drawn += dw;
        }

        tessellator.end();
    }

    private static void slice(BufferBuilder buffer, Matrix4f matrix, float x, float y, int w, int h, float u0, float u1, float v0, float v1) {
        if (w <= 0 || h <= 0) return;
        buffer.vertex(matrix, x, y + h, 0).uv(u0, v1).color(-1).endVertex();
        buffer.vertex(matrix, x + w, y + h, 0).uv(u1, v1).color(-1).endVertex();
        buffer.vertex(matrix, x + w, y, 0).uv(u1, v0).color(-1).endVertex();
        buffer.vertex(matrix, x, y, 0).uv(u0, v0).color(-1).endVertex();
    }
}
