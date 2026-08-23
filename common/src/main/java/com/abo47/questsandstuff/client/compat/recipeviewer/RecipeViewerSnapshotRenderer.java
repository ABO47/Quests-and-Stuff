package com.abo47.questsandstuff.client.compat.recipeviewer;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import com.abo47.questsandstuff.QuestsAndStuffMod;

public final class RecipeViewerSnapshotRenderer {
    private static final int MAX_CACHED_SNAPSHOTS = 96;
    private static final Map<String, CachedSnapshot> CACHE = new LinkedHashMap<>(MAX_CACHED_SNAPSHOTS, 0.75F, true);
    private static final Map<String, SnapshotPlan> LIVE_CACHE = new LinkedHashMap<>(MAX_CACHED_SNAPSHOTS, 0.75F, true);
    private static final Set<String> FAILED_KEYS = new HashSet<>();
    private static final Set<String> LOGGED_FAILURES = new HashSet<>();
    private static Object cachedRecipeManager;
    private static long textureSerial;

    private RecipeViewerSnapshotRenderer() {
    }

    public static boolean render(GuiGraphics graphics, String cacheKey, SnapshotFactory factory, int width, int height, int pivotX, int pivotY) {
        if (graphics == null || cacheKey == null || cacheKey.isBlank() || factory == null || width <= 0 || height <= 0) {
            return false;
        }
        resetIfRecipeManagerChanged();
        synchronized (CACHE) {
            if (FAILED_KEYS.contains(cacheKey)) {
                return false;
            }
        }
        CachedSnapshot snapshot;
        synchronized (CACHE) {
            snapshot = CACHE.get(cacheKey);
        }
        if (snapshot == null) {
            SnapshotPlan plan;
            try {
                plan = factory.create();
            } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
                logFailure(cacheKey, "plan failed: " + exception.getClass().getSimpleName());
                return false;
            }
            if (plan == null || plan.width() <= 0 || plan.height() <= 0 || plan.painter() == null) {
                logFailure(cacheKey, "plan missing");
                return false;
            }
            snapshot = createSnapshot(cacheKey, plan);
            if (snapshot == null) {
                return false;
            }
            synchronized (CACHE) {
                CACHE.put(cacheKey, snapshot);
                trimCache();
            }
        }
        drawSnapshot(graphics, snapshot, width, height, pivotX, pivotY);
        return true;
    }

    public static boolean renderLive(GuiGraphics graphics, String cacheKey, SnapshotFactory factory, int width, int height, int pivotX, int pivotY) {
        if (graphics == null || cacheKey == null || cacheKey.isBlank() || factory == null || width <= 0 || height <= 0) {
            return false;
        }
        resetIfRecipeManagerChanged();
        SnapshotPlan plan;
        synchronized (CACHE) {
            plan = LIVE_CACHE.get(cacheKey);
        }
        if (plan == null) {
            try {
                plan = factory.create();
            } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
                logFailure(cacheKey, "live plan failed: " + exception.getClass().getSimpleName());
                return false;
            }
            if (plan == null || plan.width() <= 0 || plan.height() <= 0 || plan.painter() == null) {
                logFailure(cacheKey, "live plan missing");
                return false;
            }
            synchronized (CACHE) {
                LIVE_CACHE.put(cacheKey, plan);
                trimCache();
            }
        }
        drawLive(graphics, plan, width, height, pivotX, pivotY);
        return true;
    }

    private static CachedSnapshot createSnapshot(String cacheKey, SnapshotPlan plan) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null || minecraft.renderBuffers() == null) {
            return null;
        }
        int logicalWidth = Math.max(1, plan.width());
        int logicalHeight = Math.max(1, plan.height());
        int pixelScale = Math.max(1, (int) Math.round(minecraft.getWindow().getGuiScale()));
        int targetWidth = logicalWidth * pixelScale;
        int targetHeight = logicalHeight * pixelScale;
        TextureTarget target = new TextureTarget(targetWidth, targetHeight, true, Minecraft.ON_OSX);
        target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);

        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        PoseStack modelView = RenderSystem.getModelViewStack();
        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        ScissorState previousScissor = ScissorState.capture();
        NativeImage capturedImage = null;
        boolean posePushed = false;
        try {
            minecraft.renderBuffers().bufferSource().endBatch();
            target.clear(Minecraft.ON_OSX);
            target.bindWrite(true);
            RenderSystem.viewport(0, 0, targetWidth, targetHeight);
            RenderSystem.disableScissor();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            modelView.pushPose();
            posePushed = true;
            modelView.setIdentity();
            modelView.translate(0.0D, 0.0D, -2000.0D);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(
                    new Matrix4f().setOrtho(0.0F, logicalWidth, logicalHeight, 0.0F, 1000.0F, 3000.0F),
                    VertexSorting.ORTHOGRAPHIC_Z
            );
            Lighting.setupFor3DItems();

            GuiGraphics snapshotGraphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
            plan.painter().paint(snapshotGraphics);
            snapshotGraphics.bufferSource().endBatch();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            mainTarget.bindWrite(true);

            RenderSystem.bindTexture(target.getColorTextureId());
            capturedImage = new NativeImage(targetWidth, targetHeight, false);
            capturedImage.downloadTexture(0, false);
            capturedImage.flipY();
            if (!hasVisiblePixels(capturedImage)) {
                logFailure(cacheKey, "captured image was empty");
                return null;
            }
            ResourceLocation texture = snapshotTextureId(cacheKey);
            minecraft.getTextureManager().register(texture, new DynamicTexture(capturedImage));
            capturedImage = null;
            return new CachedSnapshot(texture, logicalWidth, logicalHeight);
        } catch (RuntimeException exception) {
            logFailure(cacheKey, "render failed: " + exception.getClass().getSimpleName());
            return null;
        } finally {
            if (capturedImage != null) {
                capturedImage.close();
            }
            target.destroyBuffers();
            if (posePushed) {
                modelView.popPose();
            }
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.ORTHOGRAPHIC_Z);
            mainTarget.bindWrite(true);
            RenderSystem.viewport(0, 0, minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            previousScissor.restore();
        }
    }

    private static boolean hasVisiblePixels(NativeImage image) {
        try {
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if (visibleAlpha(image.getPixelRGBA(x, y)) > 12) {
                        return true;
                    }
                }
            }
            return false;
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.debugLog("[QnS:Compat] recipe snapshot readback failed reason={}", exception.getClass().getSimpleName());
            return true;
        }
    }

    private static int visibleAlpha(int pixel) {
        return Math.max(pixel & 0xFF, (pixel >>> 24) & 0xFF);
    }

    private static void logFailure(String cacheKey, String reason) {
        synchronized (CACHE) {
            FAILED_KEYS.add(cacheKey);
        }
        synchronized (LOGGED_FAILURES) {
            if (!LOGGED_FAILURES.add(cacheKey + "|" + reason)) {
                return;
            }
        }
        QuestsAndStuffMod.LOGGER.warn("[QnS:Compat] Native recipe render unavailable for {} ({})", cacheKey, reason);
    }

    private static ResourceLocation snapshotTextureId(String cacheKey) {
        String safeKey = sanitizePath(cacheKey);
        textureSerial++;
        ResourceLocation id = ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "recipe_snapshot/" + textureSerial + "_" + safeKey);
        if (id != null) {
            return id;
        }
        return new ResourceLocation(QuestsAndStuffMod.MODID, "recipe_snapshot/" + textureSerial + "_recipe");
    }

    private static String sanitizePath(String value) {
        String source = value == null ? "recipe" : value.toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder(Math.min(source.length(), 96));
        for (int i = 0; i < source.length() && builder.length() < 96; i++) {
            char c = source.charAt(i);
            if ((c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == '_'
                    || c == '-'
                    || c == '/') {
                builder.append(c);
            } else {
                builder.append('_');
            }
        }
        return builder.length() == 0 ? "recipe" : builder.toString();
    }

    private static void drawSnapshot(GuiGraphics graphics, CachedSnapshot snapshot, int width, int height, int pivotX, int pivotY) {
        float scale = Math.min(width / (float) snapshot.width(), height / (float) snapshot.height());
        float drawWidth = snapshot.width() * scale;
        float drawHeight = snapshot.height() * scale;
        float x = -pivotX + (width - drawWidth) / 2.0F;
        float y = -pivotY + (height - drawHeight) / 2.0F;
        Matrix4f pose = graphics.pose().last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, snapshot.texture());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(pose, x, y + drawHeight, 0.0F).uv(0.0F, 1.0F).endVertex();
        builder.vertex(pose, x + drawWidth, y + drawHeight, 0.0F).uv(1.0F, 1.0F).endVertex();
        builder.vertex(pose, x + drawWidth, y, 0.0F).uv(1.0F, 0.0F).endVertex();
        builder.vertex(pose, x, y, 0.0F).uv(0.0F, 0.0F).endVertex();
        tessellator.end();
    }

    private static void drawLive(GuiGraphics graphics, SnapshotPlan plan, int width, int height, int pivotX, int pivotY) {
        float scale = Math.min(width / (float) plan.width(), height / (float) plan.height());
        float drawWidth = plan.width() * scale;
        float drawHeight = plan.height() * scale;
        float x = -pivotX + (width - drawWidth) / 2.0F;
        float y = -pivotY + (height - drawHeight) / 2.0F;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        Lighting.setupFor3DItems();
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(x, y, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            plan.painter().paint(graphics);
            graphics.bufferSource().endBatch();
        } finally {
            graphics.pose().popPose();
        }
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
    }

    private static void resetIfRecipeManagerChanged() {
        Minecraft minecraft = Minecraft.getInstance();
        Object manager = minecraft == null || minecraft.getConnection() == null ? null : minecraft.getConnection().getRecipeManager();
        synchronized (CACHE) {
            if (manager == cachedRecipeManager) {
                return;
            }
            cachedRecipeManager = manager;
            clearCache();
        }
    }

    private static void trimCache() {
        while (CACHE.size() > MAX_CACHED_SNAPSHOTS) {
            String key = CACHE.keySet().iterator().next();
            CachedSnapshot removed = CACHE.remove(key);
            if (removed != null) {
                removed.destroy();
            }
        }
        while (LIVE_CACHE.size() > MAX_CACHED_SNAPSHOTS) {
            String key = LIVE_CACHE.keySet().iterator().next();
            LIVE_CACHE.remove(key);
        }
    }

    private static void clearCache() {
        for (CachedSnapshot snapshot : CACHE.values()) {
            snapshot.destroy();
        }
        CACHE.clear();
        LIVE_CACHE.clear();
        FAILED_KEYS.clear();
        LOGGED_FAILURES.clear();
    }

    @FunctionalInterface
    public interface SnapshotFactory {
        SnapshotPlan create() throws ReflectiveOperationException;
    }

    @FunctionalInterface
    public interface SnapshotPainter {
        void paint(GuiGraphics graphics);
    }

    public record SnapshotPlan(int width, int height, SnapshotPainter painter) {
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

    private record CachedSnapshot(ResourceLocation texture, int width, int height) {
        private void destroy() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null) {
                minecraft.getTextureManager().release(texture);
            }
        }
    }
}
