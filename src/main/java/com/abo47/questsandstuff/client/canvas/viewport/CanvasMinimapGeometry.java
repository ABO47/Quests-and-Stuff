package com.abo47.questsandstuff.client.canvas.viewport;

import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;

public final class CanvasMinimapGeometry {
    public static final int MARGIN = 3;
    public static final int PANEL_BODY_W = 142;
    public static final int PANEL_H = 94;
    public static final int PANEL_PAD = 5;
    public static final int HANDLE_W = 6;
    public static final int HANDLE_COLLAPSED_H = 44;
    public static final int WORLD_PAD = 24;

    private CanvasMinimapGeometry() {
    }

    public static Layout layout(int viewportW, int viewportH, boolean collapsed) {
        int safeViewportW = Math.max(1, viewportW);
        int safeViewportH = Math.max(1, viewportH);
        int handleW = Math.min(HANDLE_W, safeViewportW);
        int bodyW = Math.min(PANEL_BODY_W, Math.max(72, safeViewportW - MARGIN * 2 - handleW));
        int expandedPanelW = Math.min(safeViewportW, bodyW + handleW);
        int expandedPanelH = Math.min(PANEL_H, Math.max(54, safeViewportH - MARGIN * 2));
        int collapsedPanelH = Math.min(HANDLE_COLLAPSED_H, Math.max(24, safeViewportH - MARGIN * 2));
        int panelW = collapsed ? handleW : expandedPanelW;
        int panelH = collapsed ? collapsedPanelH : expandedPanelH;
        int panelX = Math.max(0, safeViewportW - panelW - MARGIN);
        int panelY = Math.max(0, safeViewportH - panelH - MARGIN);
        int toggleW = Math.min(handleW, panelW);
        int toggleH = panelH;
        int toggleX = panelX + panelW - toggleW;
        int toggleY = panelY;

        if (collapsed) {
            return new Layout(panelX, panelY, panelW, panelH, 0, 0, 0, 0, toggleX, toggleY, toggleW, toggleH, true);
        }

        int mapX = panelX + PANEL_PAD;
        int mapY = panelY + PANEL_PAD;
        int mapW = Math.max(1, panelW - toggleW - PANEL_PAD * 2);
        int mapH = Math.max(1, panelH - PANEL_PAD * 2);
        return new Layout(panelX, panelY, panelW, panelH, mapX, mapY, mapW, mapH, toggleX, toggleY, toggleW, toggleH, false);
    }

    public static WorldBounds worldBounds(
            TabletUiState state,
            List<QuestCardLayout> cards,
            List<CanvasImageLayer> images,
            List<CanvasTextLayer> texts
    ) {
        int minX = (int) Math.floor(CanvasGeometry.screenToLogicalX(state, state.canvasContentX));
        int minY = (int) Math.floor(CanvasGeometry.screenToLogicalY(state, state.canvasContentY));
        int maxX = (int) Math.ceil(CanvasGeometry.screenToLogicalX(state, state.canvasContentX + Math.max(1, state.canvasContentW)));
        int maxY = (int) Math.ceil(CanvasGeometry.screenToLogicalY(state, state.canvasContentY + Math.max(1, state.canvasContentH)));

        for (QuestCardLayout card : cards) {
            minX = Math.min(minX, card.visualLogicalX());
            minY = Math.min(minY, card.visualLogicalY());
            maxX = Math.max(maxX, card.logicalRight());
            maxY = Math.max(maxY, card.logicalBottom());
        }
        for (CanvasImageLayer image : images) {
            minX = Math.min(minX, image.x());
            minY = Math.min(minY, image.y());
            maxX = Math.max(maxX, image.x() + image.w());
            maxY = Math.max(maxY, image.y() + image.h());
        }
        for (CanvasTextLayer text : texts) {
            minX = Math.min(minX, text.x());
            minY = Math.min(minY, text.y());
            maxX = Math.max(maxX, text.x() + text.w());
            maxY = Math.max(maxY, text.y() + text.h());
        }

        minX -= WORLD_PAD;
        minY -= WORLD_PAD;
        maxX += WORLD_PAD;
        maxY += WORLD_PAD;
        return new WorldBounds(minX, minY, Math.max(1, maxX - minX), Math.max(1, maxY - minY));
    }

    public static Projection projection(Layout layout, WorldBounds world) {
        if (layout.collapsed() || layout.mapW() <= 0 || layout.mapH() <= 0) {
            return new Projection(0, 0, 0, 0, 1.0f, world);
        }
        float scale = Math.min(layout.mapW() / (float) Math.max(1, world.width()), layout.mapH() / (float) Math.max(1, world.height()));
        scale = Math.max(0.0001f, scale);
        int drawW = Math.max(1, Math.min(layout.mapW(), Math.round(world.width() * scale)));
        int drawH = Math.max(1, Math.min(layout.mapH(), Math.round(world.height() * scale)));
        int drawX = layout.mapX() + (layout.mapW() - drawW) / 2;
        int drawY = layout.mapY() + (layout.mapH() - drawH) / 2;
        return new Projection(drawX, drawY, drawW, drawH, scale, world);
    }

    public static int mapX(Projection projection, double logicalX) {
        return projection.drawX() + Math.round((float) ((logicalX - projection.world().minX()) * projection.scale()));
    }

    public static int mapY(Projection projection, double logicalY) {
        return projection.drawY() + Math.round((float) ((logicalY - projection.world().minY()) * projection.scale()));
    }

    public static int mapWorldX(TabletUiState state, int localX) {
        float xNorm = (float) (localX - state.minimapX) / (float) Math.max(1, state.minimapW);
        xNorm = Math.max(0.0f, Math.min(1.0f, xNorm));
        return state.minimapWorldMinX + Math.round(xNorm * Math.max(1, state.minimapWorldWidth));
    }

    public static int mapWorldY(TabletUiState state, int localY) {
        float yNorm = (float) (localY - state.minimapY) / (float) Math.max(1, state.minimapH);
        yNorm = Math.max(0.0f, Math.min(1.0f, yNorm));
        return state.minimapWorldMinY + Math.round(yNorm * Math.max(1, state.minimapWorldHeight));
    }

    public static boolean hit(int localX, int localY, int x, int y, int w, int h) {
        return w > 0 && h > 0 && localX >= x && localX < x + w && localY >= y && localY < y + h;
    }

    public record Layout(
            int panelX,
            int panelY,
            int panelW,
            int panelH,
            int mapX,
            int mapY,
            int mapW,
            int mapH,
            int toggleX,
            int toggleY,
            int toggleW,
            int toggleH,
            boolean collapsed
    ) {
    }

    public record WorldBounds(int minX, int minY, int width, int height) {
    }

    public record Projection(int drawX, int drawY, int drawW, int drawH, float scale, WorldBounds world) {
    }
}
