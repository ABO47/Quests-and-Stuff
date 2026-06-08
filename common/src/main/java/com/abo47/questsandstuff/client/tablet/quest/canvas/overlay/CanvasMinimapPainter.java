package com.abo47.questsandstuff.client.tablet.quest.canvas.overlay;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.animation.UiAnimationProgress;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.QuestMiniCardRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasCameraController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasMinimapGeometry;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.client.utils.RenderBufferUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

final class CanvasMinimapPainter {
    static final float BODY_REVEAL_START = 0.48f;

    private static final int VISUAL_CONNECTION_GRID_STEP = 16;
    private static final float MINIMAP_CHEVRON_SCALE = 0.45f;

    private CanvasMinimapPainter() {
    }

    static void drawPanel(
            GuiGraphics graphics,
            int originX,
            int originY,
            CanvasMinimapGeometry.Layout layout,
            CanvasMinimapGeometry.Layout collapsedLayout,
            float holderProgress,
            float bodyProgress,
            int mouseX,
            int mouseY
    ) {
        int handleX = originX + layout.toggleX();
        int handleY = originY + UiAnimationProgress.interpolate(collapsedLayout.toggleY(), layout.toggleY(), holderProgress);
        int handleW = layout.toggleW();
        int handleH = UiAnimationProgress.interpolate(collapsedLayout.toggleH(), layout.toggleH(), holderProgress);
        int visibleBodyW = Math.round((layout.panelW() - layout.toggleW()) * bodyProgress);
        if (visibleBodyW > 0) {
            int bodyX = handleX - visibleBodyW;
            int bodyY = originY + layout.panelY();
            int bodyH = layout.panelH();
            graphics.fill(bodyX, bodyY, handleX, bodyY + bodyH, withAlpha(ModColors.SURFACE_BASE, 248));
            graphics.fill(bodyX, bodyY, handleX, bodyY + 1, withAlpha(ModColors.BORDER_BASE, 150));
            graphics.fill(bodyX, bodyY + bodyH - 1, handleX, bodyY + bodyH, withAlpha(ModColors.BORDER_BASE, 150));
            graphics.fill(bodyX, bodyY, bodyX + 1, bodyY + bodyH, withAlpha(ModColors.BORDER_BASE, 150));
        }
        drawHandle(graphics, handleX, handleY, handleW, handleH, mouseX, mouseY);
    }

    static float stagedProgress(float progress, float start, float end) {
        if (end <= start) {
            return progress >= end ? 1.0f : 0.0f;
        }
        float staged = (progress - start) / (end - start);
        staged = Math.max(0.0f, Math.min(1.0f, staged));
        return staged * staged * (3.0f - 2.0f * staged);
    }

    static void drawSnapshot(
            GuiGraphics graphics,
            TabletUiState state,
            CanvasMinimapSnapshot snapshot,
            int originX,
            int originY,
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
        boolean visualMode = QuestsAndStuffConfig.visualMinimapEnabled();
        for (CanvasMinimapConnection connection : snapshot.connections()) {
            if (visualMode) {
                drawMiniChevrons(graphics, connection, originX, originY);
            } else {
                drawMiniLine(
                        graphics,
                        originX + connection.x1(),
                        originY + connection.y1(),
                        originX + connection.x2(),
                        originY + connection.y2(),
                        withAlpha(connection.color(), connection.alpha())
                );
            }
        }
        for (CanvasMinimapRect quest : snapshot.quests()) {
            if (visualMode) {
                drawQuestPreview(graphics, state, quest, originX, originY, mouseX, mouseY, partialTicks);
            } else {
                drawQuestBox(graphics, originX + quest.x(), originY + quest.y(), quest.w(), quest.h(), quest.color(), quest.alpha());
            }
        }
        CanvasMinimapRect viewport = projectViewport(state, snapshot.projection());
        drawBorder(graphics, originX + viewport.x(), originY + viewport.y(), viewport.w(), viewport.h(), withAlpha(viewport.color(), viewport.alpha()));
    }

    private static CanvasMinimapRect projectViewport(TabletUiState state, CanvasMinimapGeometry.Projection projection) {
        double left = CanvasCameraController.screenToLogicalX(state, state.canvasContentX, true);
        double top = CanvasCameraController.screenToLogicalY(state, state.canvasContentY, true);
        double right = CanvasCameraController.screenToLogicalX(state, state.canvasContentX + Math.max(1, state.canvasContentW), true);
        double bottom = CanvasCameraController.screenToLogicalY(state, state.canvasContentY + Math.max(1, state.canvasContentH), true);
        int x1 = clamp(CanvasMinimapGeometry.mapX(projection, left), projection.drawX(), projection.drawX() + projection.drawW());
        int y1 = clamp(CanvasMinimapGeometry.mapY(projection, top), projection.drawY(), projection.drawY() + projection.drawH());
        int x2 = clamp(CanvasMinimapGeometry.mapX(projection, right), projection.drawX(), projection.drawX() + projection.drawW());
        int y2 = clamp(CanvasMinimapGeometry.mapY(projection, bottom), projection.drawY(), projection.drawY() + projection.drawH());
        int x = Math.min(x1, x2);
        int y = Math.min(y1, y2);
        return new CanvasMinimapRect(x, y, Math.max(1, Math.abs(x2 - x1)), Math.max(1, Math.abs(y2 - y1)), ModColors.TEXT_PRIMARY, 230, null, null);
    }

    private static void drawQuestPreview(GuiGraphics graphics, TabletUiState state, CanvasMinimapRect quest, int originX, int originY, int mouseX, int mouseY, float partialTicks) {
        int x = originX + quest.x();
        int y = originY + quest.y();
        if (quest.tag() == null) {
            drawQuestBox(graphics, x, y, quest.w(), quest.h(), quest.color(), quest.alpha());
            return;
        }

        boolean hiddenOverlay = state.canEdit && quest.tag().getBoolean("visual_hidden") && !quest.tag().getBoolean("completed");
        boolean highlighted = quest.questId() != null && state.canvasSelection.questIds().contains(quest.questId());
        QuestMiniCardRenderer.drawTagCard(graphics, quest.tag(), x, y, quest.w(), quest.h(), mouseX, mouseY, partialTicks, quest.alpha(), hiddenOverlay, highlighted);
    }

    private static void drawQuestBox(GuiGraphics graphics, int x, int y, int w, int h, int color, int alpha) {
        if (w < 5 || h < 5) {
            graphics.fill(x, y, x + w, y + h, withAlpha(color, 255));
            return;
        }
        graphics.fill(x, y, x + w, y + h, withAlpha(ModColors.SURFACE_BASE, 255));
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, withAlpha(color, 255));
    }

    private static void drawMiniChevrons(GuiGraphics graphics, CanvasMinimapConnection connection, int originX, int originY) {
        List<CanvasPoint> path = visualConnectionPath(
                Math.round(originX + connection.x1()),
                Math.round(originY + connection.y1()),
                Math.round(originX + connection.x2()),
                Math.round(originY + connection.y2()),
                connection.direct()
        );
        ConnectionRenderer.drawStaticChevrons(graphics, path, connection.color(), connection.alpha(), MINIMAP_CHEVRON_SCALE, -4096, -4096, 8192, 8192);
    }

    private static List<CanvasPoint> visualConnectionPath(int sourceX, int sourceY, int targetX, int targetY, boolean direct) {
        if (direct) {
            return List.of(new CanvasPoint(sourceX, sourceY), new CanvasPoint(targetX, targetY));
        }
        int midX = snapToStep((sourceX + targetX) / 2, VISUAL_CONNECTION_GRID_STEP);
        if (Math.abs(midX - sourceX) < VISUAL_CONNECTION_GRID_STEP / 2) {
            midX += targetX >= sourceX ? VISUAL_CONNECTION_GRID_STEP : -VISUAL_CONNECTION_GRID_STEP;
        }
        return List.of(
                new CanvasPoint(sourceX, sourceY),
                new CanvasPoint(midX, sourceY),
                new CanvasPoint(midX, targetY),
                new CanvasPoint(targetX, targetY)
        );
    }

    private static int snapToStep(int value, int step) {
        return Math.round(value / (float) Math.max(1, step)) * Math.max(1, step);
    }

    private static void drawHandle(GuiGraphics graphics, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int fill = hovered ? withAlpha(ModColors.INTERACTIVE, 115) : withAlpha(ModColors.SURFACE_PANEL_ALT, 236);
        int border = hovered ? withAlpha(ModColors.BORDER_ACCENT, 235) : withAlpha(ModColors.BORDER_BASE, 180);
        graphics.fill(x, y, x + w, y + h, fill);
        drawBorder(graphics, x, y, w, h, border);
    }

    private static void drawMiniLine(GuiGraphics graphics, float x1, float y1, float x2, float y2, int color) {
        if (x1 == x2 && y1 == y2) {
            int x = Math.round(x1);
            int y = Math.round(y1);
            graphics.fill(x, y, x + 1, y + 1, color);
            return;
        }
        Tesselator tessellator = Tesselator.getInstance();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        RenderBufferUtils.drawColorLines(
                graphics.pose(),
                buffer,
                List.of(new Vec2(x1, y1), new Vec2(x2, y2)),
                color,
                color,
                0.55f
        );
        tessellator.end();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private static void drawBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
