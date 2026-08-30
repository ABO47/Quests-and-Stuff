package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class WorldPortalCapture {
    private WorldPortalCapture() {
    }

    private static TextureTarget target;
    private static int targetW;
    private static int targetH;

    private static TextureTarget uiTarget;
    private static int uiTargetW;
    private static int uiTargetH;

    public static boolean shouldCapture(TabletUiState state) {
        if (state == null || state.root == null || state.canvas == null) {
            return false;
        }
        if (!"quest".equals(state.root.currentApp)) {
            return false;
        }
        int percent = Math.max(0, Math.min(100, state.canvas.canvasBgOpacityPercent));
        return percent < 100;
    }

    public static boolean hasTexture() {
        return target != null;
    }

    public static boolean shouldCaptureDetails(TabletUiState state) {
        if (state == null || state.root == null || state.questDetails == null) {
            return false;
        }
        if (!QuestDetailsWindow.isVisible(state)) {
            return false;
        }
        int percent = Math.max(0, Math.min(100, state.questDetails.questDetailsCanvasBgOpacityPercent));
        return percent < 100 || state.questDetails.questDetailsCanvasCustomBg;
    }

    public static boolean hasUiTexture() {
        return uiTarget != null;
    }

    public static void capture(TabletUiState state) {
        if (!shouldCapture(state)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null || minecraft.getMainRenderTarget() == null) {
            return;
        }
        int w = minecraft.getMainRenderTarget().width;
        int h = minecraft.getMainRenderTarget().height;
        if (w <= 0 || h <= 0) {
            return;
        }
        ensureTarget(w, h);
        if (target != null) {
            blitMainToTarget(target, w, h, minecraft);
        }
    }

    public static void captureMainCanvas(TabletUiState state) {
        if (!shouldCaptureDetails(state)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null || minecraft.getMainRenderTarget() == null) {
            return;
        }
        int w = minecraft.getMainRenderTarget().width;
        int h = minecraft.getMainRenderTarget().height;
        if (w <= 0 || h <= 0) {
            return;
        }
        ensureUiTarget(w, h);
        if (uiTarget != null) {
            blitMainToTarget(uiTarget, w, h, minecraft);
        }
    }

    private static void blitMainToTarget(TextureTarget writeTarget, int w, int h, Minecraft minecraft) {
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        PoseStack modelView = RenderSystem.getModelViewStack();
        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        ScissorState previousScissor = ScissorState.capture();
        boolean posePushed = false;
        try {
            minecraft.renderBuffers().bufferSource().endBatch();
            writeTarget.clear(Minecraft.ON_OSX);
            writeTarget.bindWrite(true);
            RenderSystem.viewport(0, 0, w, h);
            RenderSystem.disableScissor();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableBlend();
            RenderSystem.disableCull();
            modelView.pushPose();
            posePushed = true;
            modelView.setIdentity();
            modelView.translate(0.0D, 0.0D, -2000.0D);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(
                    new Matrix4f().setOrtho(0.0F, w, h, 0.0F, 1000.0F, 3000.0F),
                    VertexSorting.ORTHOGRAPHIC_Z);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, mainTarget.getColorTextureId());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder builder = tessellator.getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(0.0F, 0.0F, 0.0F).uv(0.0F, 0.0F).endVertex();
            builder.vertex(w, 0.0F, 0.0F).uv(1.0F, 0.0F).endVertex();
            builder.vertex(w, h, 0.0F).uv(1.0F, 1.0F).endVertex();
            builder.vertex(0.0F, h, 0.0F).uv(0.0F, 1.0F).endVertex();
            tessellator.end();
            mainTarget.bindWrite(true);
        } finally {
            if (posePushed) {
                modelView.popPose();
            }
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.ORTHOGRAPHIC_Z);
            mainTarget.bindWrite(true);
            RenderSystem.viewport(0, 0, minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            previousScissor.restore();
        }
    }

    public static void drawInto(GuiGraphics graphics, WidgetGroup viewport, TabletUiState state) {
        drawTargetInto(graphics, viewport, target);
    }

    public static void drawUiInto(GuiGraphics graphics, WidgetGroup viewport, TabletUiState state) {
        drawTargetInto(graphics, viewport, uiTarget);
    }

    private static void drawTargetInto(GuiGraphics graphics, WidgetGroup viewport, TextureTarget src) {
        if (src == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getWindow() == null) {
            return;
        }
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        if (screenW <= 0 || screenH <= 0) {
            return;
        }
        int gx0 = viewport.getPositionX();
        int gy0 = viewport.getPositionY();
        int vw = viewport.getSizeWidth();
        int vh = viewport.getSizeHeight();
        if (vw <= 0 || vh <= 0) {
            return;
        }
        float u0 = gx0 / (float) screenW;
        float u1 = (gx0 + vw) / (float) screenW;
        float vTop = gy0 / (float) screenH;
        float vBot = (gy0 + vh) / (float) screenH;
        Matrix4f pose = graphics.pose().last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, src.getColorTextureId());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        float x0 = gx0, y0 = gy0, x1 = gx0 + vw, y1 = gy0 + vh;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(pose, x0, y0, 0.0F).uv(u0, vTop).endVertex();
        builder.vertex(pose, x1, y0, 0.0F).uv(u1, vTop).endVertex();
        builder.vertex(pose, x1, y1, 0.0F).uv(u1, vBot).endVertex();
        builder.vertex(pose, x0, y1, 0.0F).uv(u0, vBot).endVertex();
        tessellator.end();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void dispose() {
        if (target != null) {
            target.destroyBuffers();
            target = null;
            targetW = 0;
            targetH = 0;
        }
        if (uiTarget != null) {
            uiTarget.destroyBuffers();
            uiTarget = null;
            uiTargetW = 0;
            uiTargetH = 0;
        }
    }

    private static void ensureTarget(int w, int h) {
        if (target != null && targetW == w && targetH == h) {
            return;
        }
        if (target != null) {
            target.destroyBuffers();
            target = null;
        }
        target = new TextureTarget(w, h, true, Minecraft.ON_OSX);
        target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        targetW = w;
        targetH = h;
    }

    private static void ensureUiTarget(int w, int h) {
        if (uiTarget != null && uiTargetW == w && uiTargetH == h) {
            return;
        }
        if (uiTarget != null) {
            uiTarget.destroyBuffers();
            uiTarget = null;
        }
        uiTarget = new TextureTarget(w, h, true, Minecraft.ON_OSX);
        uiTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        uiTargetW = w;
        uiTargetH = h;
    }

    private record ScissorState(boolean enabled, int x, int y, int width, int height) {
        private static ScissorState capture() {
            boolean enabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
            int[] box = new int[4];
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, box);
            return new ScissorState(enabled, box[0], box[1], box[2], box[3]);
        }

        private void restore() {
            if (enabled) {
                RenderSystem.enableScissor(x, y, width, height);
            } else {
                RenderSystem.disableScissor();
            }
        }
    }
}
