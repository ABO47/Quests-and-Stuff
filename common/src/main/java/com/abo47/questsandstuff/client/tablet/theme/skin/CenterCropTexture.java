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

public class CenterCropTexture implements IGuiTexture {
    private final ResourceLocation imageLocation;
    private final int origW;
    private final int origH;
    private boolean logged = false;

    public CenterCropTexture(ResourceLocation imageLocation, int origW, int origH) {
        this.imageLocation = imageLocation;
        this.origW = origW;
        this.origH = origH;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        if (width <= 0 || height <= 0 || origW <= 0 || origH <= 0) return;
        int guiScale = Math.max(1, (int) Minecraft.getInstance().getWindow().getGuiScale());
        int drawW = Math.max(1, Math.round(origW / (float) guiScale));
        int drawH = Math.max(1, Math.round(origH / (float) guiScale));
        int drawX = Math.round(x + (width - drawW) / 2f);
        int drawY = Math.round(y + (height - drawH) / 2f);

        if (!logged) {
            QuestsAndStuffMod.debugLog("[QnS:Skin] CenterCropTexture.draw: orig={}x{}, guiScale={}, draw={}x{} at ({},{})", origW, origH, guiScale, drawW, drawH, drawX, drawY);
            logged = true;
        }

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, imageLocation);
        var matrix = graphics.pose().last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR);
        buffer.vertex(matrix, drawX, drawY + drawH, 0).uv(0, 1).color(-1).endVertex();
        buffer.vertex(matrix, drawX + drawW, drawY + drawH, 0).uv(1, 1).color(-1).endVertex();
        buffer.vertex(matrix, drawX + drawW, drawY, 0).uv(1, 0).color(-1).endVertex();
        buffer.vertex(matrix, drawX, drawY, 0).uv(0, 0).color(-1).endVertex();
        tessellator.end();
    }
}
