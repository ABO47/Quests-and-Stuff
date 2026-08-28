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

public class DynamicClippingTexture implements IGuiTexture {
    private final ResourceLocation imageLocation;
    private final int origW;
    private final int origH;
    private final int left;
    private final int right;
    private final int top;
    private final int bottom;
    private boolean referenceSet = false;
    private int refW = -1;
    private int refH = -1;

    public DynamicClippingTexture(ResourceLocation imageLocation, int origW, int origH) {
        this(imageLocation, origW, origH, 0, 0, 0, 0);
    }

    public DynamicClippingTexture(ResourceLocation imageLocation, int origW, int origH, int left, int right, int top, int bottom) {
        this.imageLocation = imageLocation;
        this.origW = origW;
        this.origH = origH;
        this.left = Math.max(0, left);
        this.right = Math.max(0, right);
        this.top = Math.max(0, top);
        this.bottom = Math.max(0, bottom);
    }

    public void setReferenceSize(int w, int h) {
        if (!referenceSet && w > 0 && h > 0) {
            referenceSet = true;
            refW = w;
            refH = h;
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        if (width <= 0 || height <= 0 || origW <= 0 || origH <= 0) return;

        int L = Math.min(left, origW);
        int R = Math.min(right, Math.max(0, origW - L));
        int T = Math.min(top, origH);
        int B = Math.min(bottom, Math.max(0, origH - T));
        int midW = origW - L - R;
        int midH = origH - T - B;
        if (midW <= 0 || midH <= 0) {
            stretch(graphics, x, y, width, height);
            return;
        }

        int gw = width - L - R;
        int gh = height - T - B;
        if (gw <= 0 || gh <= 0) {
            stretch(graphics, x, y, width, height);
            return;
        }

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, imageLocation);
        var matrix = graphics.pose().last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR);

        float uL1 = (float) L / origW;
        float uR0 = (float) (origW - R) / origW;
        float vT1 = (float) T / origH;
        float vB0 = (float) (origH - B) / origH;
        float uM0 = uL1;
        float uM1 = uR0;
        float vM0 = vT1;
        float vM1 = vB0;
        if (gw < midW) {
            float crop = (midW - gw) / 2f;
            uM0 = (L + crop) / origW;
            uM1 = (L + crop + gw) / origW;
        }
        if (gh < midH) {
            float crop = (midH - gh) / 2f;
            vM0 = (T + crop) / origH;
            vM1 = (T + crop + gh) / origH;
        }

        slice(buffer, matrix, x, y, L, T, 0, uL1, 0, vT1);
        slice(buffer, matrix, x + L, y, gw, T, uM0, uM1, 0, vT1);
        slice(buffer, matrix, x + width - R, y, R, T, uR0, 1, 0, vT1);
        slice(buffer, matrix, x, y + T, L, gh, 0, uL1, vM0, vM1);
        slice(buffer, matrix, x + L, y + T, gw, gh, uM0, uM1, vM0, vM1);
        slice(buffer, matrix, x + width - R, y + T, R, gh, uR0, 1, vM0, vM1);
        slice(buffer, matrix, x, y + height - B, L, B, 0, uL1, vB0, 1);
        slice(buffer, matrix, x + L, y + height - B, gw, B, uM0, uM1, vB0, 1);
        slice(buffer, matrix, x + width - R, y + height - B, R, B, uR0, 1, vB0, 1);

        tessellator.end();
    }

    private void stretch(GuiGraphics graphics, float x, float y, int width, int height) {
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, imageLocation);
        var matrix = graphics.pose().last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR);
        quad(buffer, matrix, x, y, x + width, y + height, 0, 0, 1, 1);
        tessellator.end();
    }

    private static void slice(BufferBuilder buffer, org.joml.Matrix4f matrix, float x, float y, int w, int h, float u0, float u1, float v0, float v1) {
        if (w <= 0 || h <= 0) return;
        quad(buffer, matrix, x, y, x + w, y + h, u0, v0, u1, v1);
    }

    private static void quad(BufferBuilder buffer, org.joml.Matrix4f matrix, float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1) {
        buffer.vertex(matrix, x0, y1, 0).uv(u0, v1).color(-1).endVertex();
        buffer.vertex(matrix, x1, y1, 0).uv(u1, v1).color(-1).endVertex();
        buffer.vertex(matrix, x1, y0, 0).uv(u1, v0).color(-1).endVertex();
        buffer.vertex(matrix, x0, y0, 0).uv(u0, v0).color(-1).endVertex();
    }
}
