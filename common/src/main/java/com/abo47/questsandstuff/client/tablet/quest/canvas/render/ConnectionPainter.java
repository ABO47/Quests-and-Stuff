package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.DynamicTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawRectOutline;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR;

final class ConnectionPainter {
    private static final ResourceLocation CONNECTION_CHEVRON = ResourceLocation.tryBuild("questsandstuff", "textures/gui/chevron.png");
    private static final float CHEVRON_U0 = 50.0f / 256.0f;
    private static final float CHEVRON_U1 = 206.0f / 256.0f;
    private static final int CHEVRON_BASE_W = 5;
    private static final int CHEVRON_BASE_H = 9;
    private static final float DARKEN_FACTOR = 0.52f;
    private static final float SCALE_CLAMP_MIN = 0.25f;
    private static final float SCALE_CLAMP_MAX = 2.0f;
    private static final int PENDING_FILL_ALPHA = 72;
    private static final int PENDING_OUTLINE_ALPHA = 220;
    private static final float ANIMATION_ALPHA_BASE = 0.58f;
    private static final float ANIMATION_ALPHA_PROGRESS = 0.42f;
    private static final int DEFAULT_SPACING = 5;

    private ConnectionPainter() {
    }

    static void renderConnectionLines(WidgetGroup canvasViewport, TabletUiState state, List<ConnectionLine> lines) {
        if (lines.isEmpty()) {
            return;
        }
        canvasViewport.addWidget(new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int originX = getPositionX();
                int originY = getPositionY();
                int clipMinX = originX - state.canvas.canvasLivePanX;
                int clipMinY = originY - state.canvas.canvasLivePanY;
                int clipMaxX = clipMinX + getSizeWidth();
                int clipMaxY = clipMinY + getSizeHeight();
                long now = System.currentTimeMillis();
                for (ConnectionLine line : lines) {
                    drawConnection(graphics, originX, originY, state, line, mouseX, mouseY, now, clipMinX, clipMinY, clipMaxX, clipMaxY);
                }
            }
        });
    }

    static List<CanvasPoint> connectionPath(TabletUiState state, int originX, int originY, int sourceX, int sourceY, int targetX, int targetY, boolean direct) {
        if (direct) {
            return List.of(new CanvasPoint(sourceX, sourceY), new CanvasPoint(targetX, targetY));
        }
        int cell = Math.max(1, state.canvas.gridCellPx);
        int localSourceX = sourceX - originX;
        int localTargetX = targetX - originX;
        int midLocalX = snapScreenLocalToGrid(state, (localSourceX + localTargetX) / 2, cell);
        int midX = originX + midLocalX;
        if (Math.abs(midX - sourceX) < cell / 2) {
            midX += targetX >= sourceX ? cell : -cell;
        }
        return List.of(
                new CanvasPoint(sourceX, sourceY),
                new CanvasPoint(midX, sourceY),
                new CanvasPoint(midX, targetY),
                new CanvasPoint(targetX, targetY)
        );
    }

    static void drawStaticChevrons(GuiGraphics graphics, List<CanvasPoint> path, int color, int alpha, int clipMinX, int clipMinY, int clipMaxX, int clipMaxY) {
        drawTexturedChevrons(graphics, path, color, alpha, null, 0, CHEVRON_BASE_W, CHEVRON_BASE_H, clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    static void drawStaticChevrons(GuiGraphics graphics, List<CanvasPoint> path, int color, int alpha, float scale, int clipMinX, int clipMinY, int clipMaxX, int clipMaxY) {
        float safeScale = clampScale(scale);
        int gW = scaledGlyphDim(CHEVRON_BASE_W, safeScale);
        int gH = scaledGlyphDim(CHEVRON_BASE_H, safeScale);
        drawTexturedChevrons(graphics, path, color, alpha, 1.0f, safeScale, null, gW, gW, gH, clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    static void drawTexturedChevrons(GuiGraphics graphics, List<CanvasPoint> path, int color, int alpha, float scale, String textureStr, int clipMinX, int clipMinY, int clipMaxX, int clipMaxY) {
        ResourceLocation tex = resolveTexture(textureStr);
        float safeScale = clampScale(scale);
        int[] dims = tex != null ? textureDims(textureStr) : null;
        int glyphW, glyphH;
        if (dims != null && dims[0] > 0 && dims[1] > 0) {
            double baseArea = CHEVRON_BASE_W * CHEVRON_BASE_H;
            double texAspect = (double) dims[0] / (double) dims[1];
            double aW = Math.sqrt(baseArea * texAspect);
            double aH = baseArea / aW;
            glyphW = scaledGlyphDim((int) Math.round(aW), safeScale);
            glyphH = scaledGlyphDim((int) Math.round(aH), safeScale);
        } else {
            glyphW = scaledGlyphDim(CHEVRON_BASE_W, safeScale);
            glyphH = scaledGlyphDim(CHEVRON_BASE_H, safeScale);
        }
        drawTexturedChevrons(graphics, path, color, alpha, 1.0f, safeScale, tex, glyphW, glyphW, glyphH, clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    private static void drawConnection(
            GuiGraphics graphics,
            int originX,
            int originY,
            TabletUiState state,
            ConnectionLine line,
            int mouseX,
            int mouseY,
            long now,
            int clipMinX,
            int clipMinY,
            int clipMaxX,
            int clipMaxY
    ) {
        int sourceOffsetX = selectionDragOffsetX(state, line.sourceQuestId());
        int sourceOffsetY = selectionDragOffsetY(state, line.sourceQuestId());
        int targetOffsetX = selectionDragOffsetX(state, line.targetQuestId());
        int targetOffsetY = selectionDragOffsetY(state, line.targetQuestId());
        int startX = originX + line.startX() + sourceOffsetX;
        int startY = originY + line.startY() + sourceOffsetY;
        int endX = originX + line.endX() + targetOffsetX;
        int endY = originY + line.endY() + targetOffsetY;

        if (line.pending()) {
            int halfSize = CHEVRON_BASE_W;
            SurfaceFactory.fill(withAlpha(TabletColors.SUCCESS, PENDING_FILL_ALPHA)).draw(graphics, 0, 0, startX - halfSize, startY - halfSize, halfSize * 2 + 1, halfSize * 2 + 1);
            drawRectOutline(graphics, startX - halfSize, startY - halfSize, halfSize * 2 + 1, halfSize * 2 + 1, withAlpha(TabletColors.SUCCESS, PENDING_OUTLINE_ALPHA));
            return;
        }

        boolean hoveringEndpoint = isHoveringEndpoint(
                originX,
                originY,
                line,
                mouseX,
                mouseY,
                sourceOffsetX,
                sourceOffsetY,
                targetOffsetX,
                targetOffsetY
        );
        if (line.hidden() && !state.root.canEdit && !hoveringEndpoint) {
            return;
        }
        int alpha = line.hidden() && hoveringEndpoint ? ConnectionRenderStyle.VISIBLE_ALPHA : line.alpha();
        List<CanvasPoint> path = connectionPath(state, originX, originY, startX, startY, endX, endY, line.direct());
        String rawTextureStr = line.texture();
        ResourceLocation texture = resolveTexture(rawTextureStr);
        int spacing = line.textureSpacing();
        if (spacing <= 0 && texture != null) {
            int tw = textureWidth(rawTextureStr);
            if (tw > 0) spacing = Math.max(tw, DEFAULT_SPACING);
        }
        spacing = Math.max(0, spacing);
        float zoom = state.canvas.canvasZoom;
        float safeScale = clampScale(zoom);
        double baseArea = CHEVRON_BASE_W * CHEVRON_BASE_H;
        int[] dims = texture != null ? textureDims(rawTextureStr) : null;
        int glyphW, glyphH;
        if (dims != null && dims[0] > 0 && dims[1] > 0) {
            double texAspect = (double) dims[0] / (double) dims[1];
            double aW = Math.sqrt(baseArea * texAspect);
            double aH = baseArea / aW;
            glyphW = scaledGlyphDim((int) Math.round(aW), safeScale);
            glyphH = scaledGlyphDim((int) Math.round(aH), safeScale);
        } else {
            glyphW = scaledGlyphDim(CHEVRON_BASE_W, safeScale);
            glyphH = scaledGlyphDim(CHEVRON_BASE_H, safeScale);
        }
        CanvasConnectionAnimation.AnimationState animation = CanvasConnectionAnimation.current(state, line.connectionId(), now);
        if (animation.running()) {
            int animatedAlpha = Math.min(255, Math.round(alpha * (ANIMATION_ALPHA_BASE + ANIMATION_ALPHA_PROGRESS * animation.progress())));
            drawTexturedChevrons(graphics, path, line.color(), animatedAlpha, animation.progress(), texture, spacing, glyphW, glyphH, clipMinX, clipMinY, clipMaxX, clipMaxY);
            if (state.root.canEdit) {
                int bbMinX = Integer.MAX_VALUE;
                int bbMinY = Integer.MAX_VALUE;
                int bbMaxX = Integer.MIN_VALUE;
                int bbMaxY = Integer.MIN_VALUE;
                for (CanvasPoint p : path) {
                    bbMinX = Math.min(bbMinX, (int) p.x);
                    bbMinY = Math.min(bbMinY, (int) p.y);
                    bbMaxX = Math.max(bbMaxX, (int) p.x);
                    bbMaxY = Math.max(bbMaxY, (int) p.y);
                }
                int pad = 8;
                if (mouseX >= bbMinX - pad && mouseX <= bbMaxX + pad && mouseY >= bbMinY - pad && mouseY <= bbMaxY + pad) {
                    GlowShaderHelper.drawGlow(graphics, mouseX, mouseY, bbMinX - pad, bbMinY - pad, bbMaxX - bbMinX + pad * 2, bbMaxY - bbMinY + pad * 2);
                }
            }
            return;
        }
        drawTexturedChevrons(graphics, path, line.color(), alpha, texture, spacing, glyphW, glyphH, clipMinX, clipMinY, clipMaxX, clipMaxY);
        if (state.root.canEdit) {
            int bbMinX = Integer.MAX_VALUE;
            int bbMinY = Integer.MAX_VALUE;
            int bbMaxX = Integer.MIN_VALUE;
            int bbMaxY = Integer.MIN_VALUE;
            for (CanvasPoint p : path) {
                bbMinX = Math.min(bbMinX, (int) p.x);
                bbMinY = Math.min(bbMinY, (int) p.y);
                bbMaxX = Math.max(bbMaxX, (int) p.x);
                bbMaxY = Math.max(bbMaxY, (int) p.y);
            }
            int pad = 8;
            if (mouseX >= bbMinX - pad && mouseX <= bbMaxX + pad && mouseY >= bbMinY - pad && mouseY <= bbMaxY + pad) {
                GlowShaderHelper.drawGlow(graphics, mouseX, mouseY, bbMinX - pad, bbMinY - pad, bbMaxX - bbMinX + pad * 2, bbMaxY - bbMinY + pad * 2);
            }
        }
    }

    private static boolean isHoveringEndpoint(
            int originX,
            int originY,
            ConnectionLine line,
            int mouseX,
            int mouseY,
            int sourceOffsetX,
            int sourceOffsetY,
            int targetOffsetX,
            int targetOffsetY
    ) {
        return inside(mouseX, mouseY, originX + line.sourceX() + sourceOffsetX, originY + line.sourceY() + sourceOffsetY, line.sourceW(), line.sourceH())
                || inside(mouseX, mouseY, originX + line.targetX() + targetOffsetX, originY + line.targetY() + targetOffsetY, line.targetW(), line.targetH());
    }

    private static boolean inSelection(TabletUiState state, String elementId) {
        return state.canvas.canvasSelection.questIds().contains(elementId)
                || state.canvas.canvasSelection.ecIds().contains(elementId);
    }

    private static int selectionDragOffsetX(TabletUiState state, String elementId) {
        if (!state.canvas.draggingSelection || elementId == null || elementId.isBlank() || !inSelection(state, elementId)) {
            return 0;
        }
        return CanvasGeometry.screenX(state, state.canvas.dragStartBoundsLeft + state.canvas.dragSelectionDeltaX)
                - CanvasGeometry.screenX(state, state.canvas.dragStartBoundsLeft);
    }

    private static int selectionDragOffsetY(TabletUiState state, String elementId) {
        if (!state.canvas.draggingSelection || elementId == null || elementId.isBlank() || !inSelection(state, elementId)) {
            return 0;
        }
        return CanvasGeometry.screenY(state, state.canvas.dragStartBoundsTop + state.canvas.dragSelectionDeltaY)
                - CanvasGeometry.screenY(state, state.canvas.dragStartBoundsTop);
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static int snapScreenLocalToGrid(TabletUiState state, int localX, int cell) {
        int relative = localX - state.canvas.canvasContentX - state.canvas.canvasOffsetX;
        int snapped = Math.round((float) relative / (float) cell) * cell;
        return state.canvas.canvasContentX + state.canvas.canvasOffsetX + snapped;
    }

    private static final java.util.Map<String, Integer> TEX_WIDTH_CACHE = new ConcurrentHashMap<>();
    private static final java.util.Map<String, int[]> TEX_DIM_CACHE = new ConcurrentHashMap<>();

    private static int textureWidth(String textureStr) {
        if (textureStr == null || textureStr.isBlank()) return -1;
        Integer cached = TEX_WIDTH_CACHE.get(textureStr);
        if (cached != null) return cached;
        try {
            java.nio.file.Path assetsRoot = com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ASSETS_ROOT_DIR;
            AssetLibrary.AssetDimensions ad = AssetLibrary.assetDimensions(assetsRoot, textureStr);
            if (ad != null) {
                TEX_WIDTH_CACHE.put(textureStr, ad.width());
                return ad.width();
            }
        } catch (Exception ignored) {
        }
        TEX_WIDTH_CACHE.put(textureStr, -1);
        return -1;
    }

    private static int[] textureDims(String textureStr) {
        if (textureStr == null || textureStr.isBlank()) return null;
        int[] cached = TEX_DIM_CACHE.get(textureStr);
        if (cached != null) return cached.length > 0 ? cached : null;
        try {
            java.nio.file.Path assetsRoot = com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ASSETS_ROOT_DIR;
            AssetLibrary.AssetDimensions ad = AssetLibrary.assetDimensions(assetsRoot, textureStr);
            if (ad != null) {
                int[] dims = new int[]{ad.width(), ad.height()};
                TEX_DIM_CACHE.put(textureStr, dims);
                return dims;
            }
        } catch (Exception ignored) {
        }
        TEX_DIM_CACHE.put(textureStr, new int[0]);
        return null;
    }

    private static ResourceLocation resolveTexture(String textureStr) {
        if (textureStr == null || textureStr.isBlank()) {
            return null;
        }
        ResourceLocation parsed = ResourceLocation.tryParse(textureStr);
        if (parsed != null && parsed.getNamespace().equals(QuestsAndStuffMod.MODID)) {
            return parsed;
        }
        java.nio.file.Path assetsRoot = com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ASSETS_ROOT_DIR;
        try {
            com.abo47.questsandstuff.client.tablet.assets.AssetLibrary.ensureAssetsDirs(assetsRoot);
            IGuiTexture guiTexture = com.abo47.questsandstuff.client.tablet.assets.AssetLibrary.chapterBackgroundTexture(assetsRoot, textureStr);
            if (guiTexture == null) {
                return null;
            }
            if (guiTexture instanceof DynamicTexture dynamic) {
                guiTexture = dynamic.textureSupplier.get();
            }
            if (guiTexture instanceof ResourceTexture resource) {
                return resource.imageLocation;
            }
            String sanitized = sanitizeAssetId(textureStr);
            return ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "chapter_asset/" + sanitized);
        } catch (Exception e) {
            return null;
        }
    }

    private static String sanitizeAssetId(String value) {
        String lower = value.toLowerCase(java.util.Locale.ROOT);
        StringBuilder out = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.' || c == '/') {
                out.append(c);
            } else {
                out.append('_');
            }
        }
        return out.toString();
    }

    private static void drawTexturedChevrons(
            GuiGraphics graphics,
            List<CanvasPoint> path,
            int color,
            int alpha,
            ResourceLocation texture,
            int customSpacing,
            int glyphW,
            int glyphH,
            int clipMinX,
            int clipMinY,
            int clipMaxX,
            int clipMaxY
    ) {
        drawTexturedChevrons(graphics, path, color, alpha, 1.0f, texture, customSpacing, glyphW, glyphH, clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    private static void drawTexturedChevrons(
            GuiGraphics graphics,
            List<CanvasPoint> path,
            int color,
            int alpha,
            float progress,
            ResourceLocation texture,
            int customSpacing,
            int glyphW,
            int glyphH,
            int clipMinX,
            int clipMinY,
            int clipMaxX,
            int clipMaxY
    ) {
        drawTexturedChevrons(graphics, path, color, alpha, progress, 1.0f, texture, customSpacing, glyphW, glyphH, clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    private static void drawTexturedChevrons(
            GuiGraphics graphics,
            List<CanvasPoint> path,
            int color,
            int alpha,
            float progress,
            float scale,
            ResourceLocation texture,
            int customSpacing,
            int glyphW,
            int glyphH,
            int clipMinX,
            int clipMinY,
            int clipMaxX,
            int clipMaxY
    ) {
        double spacing = Math.max(glyphW, customSpacing > 0 ? (double) customSpacing : (double) DEFAULT_SPACING);
        boolean customTex = texture != null;
        double totalLength = pathLength(path);
        if (totalLength < glyphW) {
            return;
        }
        double visibleLength = Math.max(glyphW / 2.0, totalLength * Math.max(0.0f, Math.min(1.0f, progress)));
        List<ChevronGlyph> glyphs = chevronGlyphs(path, color, alpha, visibleLength, glyphW, glyphH, spacing, clipMinX, clipMinY, clipMaxX, clipMaxY);
        if (glyphs.isEmpty()) {
            return;
        }
        ResourceLocation tex = texture != null ? texture : CONNECTION_CHEVRON;
        customTex = texture != null;
        float u0 = customTex ? 0.0f : CHEVRON_U0;
        float u1 = customTex ? 1.0f : CHEVRON_U1;
        setChevronTextureFilter(tex, GL11.GL_LINEAR);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        Matrix4f matrix = graphics.pose().last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR);
        for (ChevronGlyph glyph : glyphs) {
            boolean flipU = customTex && glyph.dirX() < 0;
            emitChevronQuad(buffer, matrix, glyph, glyphW, glyphH, flipU ? u1 : u0, flipU ? u0 : u1);
        }
        tessellator.end();
        RenderSystem.disableBlend();
        setChevronTextureFilter(tex, GL11.GL_NEAREST);
    }

    private static double pathLength(List<CanvasPoint> path) {
        double total = 0.0;
        for (int i = 0; i + 1 < path.size(); i++) {
            CanvasPoint a = path.get(i);
            CanvasPoint b = path.get(i + 1);
            double dx = b.x - a.x;
            double dy = b.y - a.y;
            total += Math.sqrt(dx * dx + dy * dy);
        }
        return total;
    }

    private static List<ChevronGlyph> chevronGlyphs(
            List<CanvasPoint> path,
            int color,
            int alpha,
            double visibleLength,
            int glyphW,
            int glyphH,
            double spacing,
            int clipMinX,
            int clipMinY,
            int clipMaxX,
            int clipMaxY
    ) {
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        int lightColor = withAlpha(color, safeAlpha);
        int darkColor = withAlpha(darkenColor(color, DARKEN_FACTOR), safeAlpha);
        int pad = Math.max(glyphW, glyphH) + 2;
        double startDistance = glyphW / 2.0;
        double walked = 0.0D;
        List<ChevronGlyph> glyphs = new ArrayList<>();
        for (int i = 0; i + 1 < path.size(); i++) {
            CanvasPoint a = path.get(i);
            CanvasPoint b = path.get(i + 1);
            double dx = b.x - a.x;
            double dy = b.y - a.y;
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length <= 0.0) {
                continue;
            }
            double segmentEnd = walked + length;
            if (segmentEnd <= startDistance || walked >= visibleLength) {
                walked = segmentEnd;
                continue;
            }
            double[] range = clippedSegmentRange(
                    a.x,
                    a.y,
                    b.x,
                    b.y,
                    clipMinX - pad,
                    clipMinY - pad,
                    clipMaxX + pad,
                    clipMaxY + pad
            );
            if (range != null) {
                double rangeStart = Math.max(startDistance, walked + range[0] * length);
                double rangeEnd = Math.min(visibleLength, walked + range[1] * length);
                int firstIndex = (int) Math.ceil((rangeStart - startDistance) / spacing);
                for (double distance = startDistance + firstIndex * spacing; distance < rangeEnd; distance += spacing, firstIndex++) {
                    double segmentDistance = distance - walked;
                    double dirX = dx / length;
                    double dirY = dy / length;
                    int chevronColor = (firstIndex % 2 == 0) ? lightColor : darkColor;
                    glyphs.add(new ChevronGlyph(a.x + dirX * segmentDistance, a.y + dirY * segmentDistance, dirX, dirY, chevronColor));
                }
            }
            walked = segmentEnd;
        }
        return glyphs;
    }

    private static double[] clippedSegmentRange(double x0, double y0, double x1, double y1, double minX, double minY, double maxX, double maxY) {
        double[] range = {0.0D, 1.0D};
        double dx = x1 - x0;
        double dy = y1 - y0;
        if (!clipTest(-dx, x0 - minX, range)) {
            return null;
        }
        if (!clipTest(dx, maxX - x0, range)) {
            return null;
        }
        if (!clipTest(-dy, y0 - minY, range)) {
            return null;
        }
        if (!clipTest(dy, maxY - y0, range)) {
            return null;
        }
        return range;
    }

    private static boolean clipTest(double p, double q, double[] range) {
        if (p == 0.0D) {
            return q >= 0.0D;
        }
        double r = q / p;
        if (p < 0.0D) {
            if (r > range[1]) {
                return false;
            }
            if (r > range[0]) {
                range[0] = r;
            }
            return true;
        }
        if (r < range[0]) {
            return false;
        }
        if (r < range[1]) {
            range[1] = r;
        }
        return true;
    }

    private static void setChevronTextureFilter(ResourceLocation texture, int filter) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
    }

    private static float clampScale(float scale) {
        return Math.max(SCALE_CLAMP_MIN, Math.min(SCALE_CLAMP_MAX, scale));
    }

    private static int scaledGlyphDim(int baseDim, float scale) {
        return Math.max(1, (int) Math.round(baseDim * scale));
    }

    private static int darkenColor(int color, float factor) {
        int alpha = color & 0xFF000000;
        int r = Math.max(0, Math.min(255, Math.round(((color >> 16) & 0xFF) * factor)));
        int g = Math.max(0, Math.min(255, Math.round(((color >> 8) & 0xFF) * factor)));
        int b = Math.max(0, Math.min(255, Math.round((color & 0xFF) * factor)));
        return alpha | (r << 16) | (g << 8) | b;
    }

    private static void emitChevronQuad(BufferBuilder buffer, Matrix4f matrix, ChevronGlyph glyph, int glyphW, int glyphH, float u0, float u1) {
        float halfW = glyphW / 2.0f;
        float halfH = glyphH / 2.0f;
        emitChevronVertex(buffer, matrix, glyph, -halfW, halfH, u0, 1.0f);
        emitChevronVertex(buffer, matrix, glyph, halfW, halfH, u1, 1.0f);
        emitChevronVertex(buffer, matrix, glyph, halfW, -halfH, u1, 0.0f);
        emitChevronVertex(buffer, matrix, glyph, -halfW, -halfH, u0, 0.0f);
    }

    private static void emitChevronVertex(BufferBuilder buffer, Matrix4f matrix, ChevronGlyph glyph, float localX, float localY, float u, float v) {
        float x = (float) (glyph.x() + glyph.dirX() * localX - glyph.dirY() * localY);
        float y = (float) (glyph.y() + glyph.dirY() * localX + glyph.dirX() * localY);
        buffer.vertex(matrix, x, y, 0.0f).uv(u, v).color(glyph.color()).endVertex();
    }

    private record ChevronGlyph(double x, double y, double dirX, double dirY, int color) {
    }
}
