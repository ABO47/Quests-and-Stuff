package com.abo47.questsandstuff.client.tablet.theme.skin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import com.abo47.questsandstuff.QuestsAndStuffMod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR;

public class DynamicClippingTexture implements IGuiTexture {
    private final ResourceLocation imageLocation;
    private final int origW;
    private final int origH;
    private final int leftEdge;
    private final int rightEdge;
    private final int topEdge;
    private final int bottomEdge;
    private boolean referenceSet = false;
    private int refW = -1;
    private int refH = -1;
    private boolean logged = false;
    private static final int SEAM_OVERLAP = 1;

    public DynamicClippingTexture(ResourceLocation imageLocation, int origW, int origH) {
        this(imageLocation, origW, origH, 0, 0, 0, 0);
    }

    public DynamicClippingTexture(ResourceLocation imageLocation, int origW, int origH, int leftEdge, int rightEdge, int topEdge, int bottomEdge) {
        this.imageLocation = imageLocation;
        this.origW = origW;
        this.origH = origH;
        this.leftEdge = Math.max(0, leftEdge);
        this.rightEdge = Math.max(0, rightEdge);
        this.topEdge = Math.max(0, topEdge);
        this.bottomEdge = Math.max(0, bottomEdge);
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

        int cmpW = refW > 0 ? refW : origW;
        int cmpH = refH > 0 ? refH : origH;

        if (!logged) {
            QuestsAndStuffMod.debugLog("[QnS:Skin] DynamicClippingTexture.draw: orig={}x{}, target={}x{}, ref={}x{}", origW, origH, width, height, cmpW, cmpH);
            logged = true;
        }

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, imageLocation);
        var matrix = graphics.pose().last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR);
        float uEps = 0.5f / origW;
        float vEps = 0.5f / origH;

        int guiScale = Math.max(1, (int) Minecraft.getInstance().getWindow().getGuiScale());
        if (leftEdge > 0 || rightEdge > 0 || topEdge > 0 || bottomEdge > 0) {
            int L = Math.min(leftEdge, origW);
            int R = Math.min(rightEdge, Math.max(0, origW - L));
            int T = Math.min(topEdge, origH);
            int B = Math.min(bottomEdge, Math.max(0, origH - T));
            int midW = origW - L - R;
            int midH = origH - T - B;
            if (midW <= 0 || midH <= 0) {
                buffer.vertex(matrix, x, y + height, 0).uv(0, 1).color(-1).endVertex();
                buffer.vertex(matrix, x + width, y + height, 0).uv(1, 1).color(-1).endVertex();
                buffer.vertex(matrix, x + width, y, 0).uv(1, 0).color(-1).endVertex();
                buffer.vertex(matrix, x, y, 0).uv(0, 0).color(-1).endVertex();
                tessellator.end();
                return;
            }
            int Lpx = Math.max(0, Math.round(L / (float) guiScale));
            int Rpx = Math.max(0, Math.round(R / (float) guiScale));
            int Tpx = Math.max(0, Math.round(T / (float) guiScale));
            int Bpx = Math.max(0, Math.round(B / (float) guiScale));
            int gapW = width - Lpx - Rpx;
            int gapH = height - Tpx - Bpx;
            if (gapW < 0 || gapH < 0) {
                float uCrop = (cmpW - width) / (2f * cmpW);
                float uMin = Math.max(0, uCrop);
                float uMax = Math.min(1, 1f - uCrop);
                buffer.vertex(matrix, x, y + height, 0).uv(uMin, 1).color(-1).endVertex();
                buffer.vertex(matrix, x + width, y + height, 0).uv(uMax, 1).color(-1).endVertex();
                buffer.vertex(matrix, x + width, y, 0).uv(uMax, 0).color(-1).endVertex();
                buffer.vertex(matrix, x, y, 0).uv(uMin, 0).color(-1).endVertex();
                tessellator.end();
                return;
            }
            float uL0 = uEps;
            float uL1 = (float) L / origW - uEps;
            float uR0 = (float) (origW - R) / origW + uEps;
            float uR1 = 1 - uEps;
            float vT0 = vEps;
            float vT1 = (float) T / origH - vEps;
            float vB0 = (float) (origH - B) / origH + vEps;
            float vB1 = 1 - vEps;
            float uM0 = (float) L / origW + uEps;
            float uM1 = (float) (origW - R) / origW - uEps;
            float vM0 = (float) T / origH + vEps;
            float vM1 = (float) (origH - B) / origH - vEps;
            if (gapW < midW) {
                float crop = (midW - gapW) / 2f;
                uM0 = (L + crop) / (float) origW + uEps;
                uM1 = (L + crop + gapW) / (float) origW - uEps;
            }
            if (gapH < midH) {
                float crop = (midH - gapH) / 2f;
                vM0 = (T + crop) / (float) origH + vEps;
                vM1 = (T + crop + gapH) / (float) origH - vEps;
            }
            int ov = SEAM_OVERLAP;
            slice(buffer, matrix, x, y, Lpx + ov, Tpx + ov, uL0, uL1, vT0, vT1);
            slice(buffer, matrix, x + Lpx - ov, y, gapW + 2 * ov, Tpx + ov, uM0, uM1, vT0, vT1);
            slice(buffer, matrix, x + width - Rpx - ov, y, Rpx + ov, Tpx + ov, uR0, uR1, vT0, vT1);
            slice(buffer, matrix, x, y + Tpx - ov, Lpx + ov, gapH + 2 * ov, uL0, uL1, vM0, vM1);
            slice(buffer, matrix, x + Lpx - ov, y + Tpx - ov, gapW + 2 * ov, gapH + 2 * ov, uM0, uM1, vM0, vM1);
            slice(buffer, matrix, x + width - Rpx - ov, y + Tpx - ov, Rpx + ov, gapH + 2 * ov, uR0, uR1, vM0, vM1);
            slice(buffer, matrix, x, y + height - Bpx - ov, Lpx + ov, Bpx + ov, uL0, uL1, vB0, vB1);
            slice(buffer, matrix, x + Lpx - ov, y + height - Bpx - ov, gapW + 2 * ov, Bpx + ov, uM0, uM1, vB0, vB1);
            slice(buffer, matrix, x + width - Rpx - ov, y + height - Bpx - ov, Rpx + ov, Bpx + ov, uR0, uR1, vB0, vB1);
            tessellator.end();
            return;
        }

        if (width >= cmpW) {
            buffer.vertex(matrix, x, y + height, 0).uv(uEps, 1 - vEps).color(-1).endVertex();
            buffer.vertex(matrix, x + width, y + height, 0).uv(1 - uEps, 1 - vEps).color(-1).endVertex();
            buffer.vertex(matrix, x + width, y, 0).uv(1 - uEps, vEps).color(-1).endVertex();
            buffer.vertex(matrix, x, y, 0).uv(uEps, vEps).color(-1).endVertex();
        } else {
            float uCrop = (cmpW - width) / (2f * cmpW);
            float uMin = Math.max(uEps, uCrop + uEps);
            float uMax = Math.min(1 - uEps, 1f - uCrop - uEps);
            buffer.vertex(matrix, x, y + height, 0).uv(uMin, 1 - vEps).color(-1).endVertex();
            buffer.vertex(matrix, x + width, y + height, 0).uv(uMax, 1 - vEps).color(-1).endVertex();
            buffer.vertex(matrix, x + width, y, 0).uv(uMax, vEps).color(-1).endVertex();
            buffer.vertex(matrix, x, y, 0).uv(uMin, vEps).color(-1).endVertex();
        }

        tessellator.end();
    }

    private static void slice(BufferBuilder buffer, org.joml.Matrix4f matrix, float x, float y, int w, int h, float u0, float u1, float v0, float v1) {
        if (w <= 0 || h <= 0) return;
        buffer.vertex(matrix, x, y + h, 0).uv(u0, v1).color(-1).endVertex();
        buffer.vertex(matrix, x + w, y + h, 0).uv(u1, v1).color(-1).endVertex();
        buffer.vertex(matrix, x + w, y, 0).uv(u1, v0).color(-1).endVertex();
        buffer.vertex(matrix, x, y, 0).uv(u0, v0).color(-1).endVertex();
    }
}
