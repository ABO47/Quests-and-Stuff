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

public class DynamicClippingTexture implements IGuiTexture {
    private final ResourceLocation imageLocation;
    private final int origW;
    private final int origH;
    private boolean referenceSet = false;
    private int refW = -1;
    private int refH = -1;
    private boolean logged = false;

    public DynamicClippingTexture(ResourceLocation imageLocation, int origW, int origH) {
        this.imageLocation = imageLocation;
        this.origW = origW;
        this.origH = origH;
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

        if (width >= cmpW) {
            buffer.vertex(matrix, x, y + height, 0).uv(0, 1).color(-1).endVertex();
            buffer.vertex(matrix, x + width, y + height, 0).uv(1, 1).color(-1).endVertex();
            buffer.vertex(matrix, x + width, y, 0).uv(1, 0).color(-1).endVertex();
            buffer.vertex(matrix, x, y, 0).uv(0, 0).color(-1).endVertex();
        } else {
            float uCrop = (cmpW - width) / (2f * cmpW);
            float uMin = Math.max(0, uCrop);
            float uMax = Math.min(1, 1f - uCrop);
            buffer.vertex(matrix, x, y + height, 0).uv(uMin, 1).color(-1).endVertex();
            buffer.vertex(matrix, x + width, y + height, 0).uv(uMax, 1).color(-1).endVertex();
            buffer.vertex(matrix, x + width, y, 0).uv(uMax, 0).color(-1).endVertex();
            buffer.vertex(matrix, x, y, 0).uv(uMin, 0).color(-1).endVertex();
        }

        tessellator.end();
    }
}
