package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanvasMinimapGeometryTest {
    @Test
    void anchorsExpandedPanelToBottomRight() {
        CanvasMinimapGeometry.Layout layout = CanvasMinimapGeometry.layout(240, 180, false);
        assertEquals(89, layout.panelX());
        assertEquals(83, layout.panelY());
        assertEquals(148, layout.panelW());
        assertEquals(94, layout.panelH());
        assertEquals(231, layout.toggleX());
        assertEquals(83, layout.toggleY());
        assertEquals(132, layout.mapW());
        assertEquals(84, layout.mapH());
        assertFalse(layout.collapsed());
    }

    @Test
    void collapsedPanelOnlyKeepsToggleAtBottomRight() {
        CanvasMinimapGeometry.Layout layout = CanvasMinimapGeometry.layout(240, 180, true);
        assertEquals(231, layout.panelX());
        assertEquals(133, layout.panelY());
        assertEquals(6, layout.panelW());
        assertEquals(44, layout.panelH());
        assertEquals(231, layout.toggleX());
        assertEquals(133, layout.toggleY());
        assertTrue(layout.collapsed());
    }

    @Test
    void worldBoundsIncludeViewportAndQuestCards() {
        TabletUiState state = new TabletUiState();
        state.canvas.canvasZoom = 2.0f;
        state.canvas.canvasContentW = 100;
        state.canvas.canvasContentH = 80;
        state.canvas.canvasOffsetX = -20;
        state.canvas.canvasOffsetY = -10;
        QuestCardLayout card = new QuestCardLayout("quest", new CompoundTag(), 0, 0, 10, 10, 16, 16, 80, 70, 1.0f, 0, 0, 10, 10);

        CanvasMinimapGeometry.WorldBounds bounds = CanvasMinimapGeometry.worldBounds(state, null, List.of(card));

        assertEquals(-14, bounds.minX());
        assertEquals(-19, bounds.minY());
        assertEquals(128, bounds.width());
        assertEquals(123, bounds.height());
    }

    @Test
    void mapsMinimapClicksIntoWorldSpace() {
        TabletUiState state = new TabletUiState();
        state.canvas.minimapX = 10;
        state.canvas.minimapY = 20;
        state.canvas.minimapW = 100;
        state.canvas.minimapH = 50;
        state.canvas.minimapWorldMinX = 50;
        state.canvas.minimapWorldMinY = -25;
        state.canvas.minimapWorldWidth = 200;
        state.canvas.minimapWorldHeight = 100;

        assertEquals(150, CanvasMinimapGeometry.mapWorldX(state, 60));
        assertEquals(25, CanvasMinimapGeometry.mapWorldY(state, 45));
    }

    @Test
    void expandedLayoutDefinesPanelToggleAndMapHitRects() {
        CanvasMinimapGeometry.Layout layout = CanvasMinimapGeometry.layout(240, 180, false);

        assertTrue(CanvasMinimapGeometry.hit(90, 84, layout.panelX(), layout.panelY(), layout.panelW(), layout.panelH()));
        assertTrue(CanvasMinimapGeometry.hit(232, 84, layout.toggleX(), layout.toggleY(), layout.toggleW(), layout.toggleH()));
        assertTrue(CanvasMinimapGeometry.hit(95, 88, layout.mapX(), layout.mapY(), layout.mapW(), layout.mapH()));
        assertFalse(CanvasMinimapGeometry.hit(88, 84, layout.panelX(), layout.panelY(), layout.panelW(), layout.panelH()));
        assertFalse(CanvasMinimapGeometry.hit(229, 84, layout.toggleX(), layout.toggleY(), layout.toggleW(), layout.toggleH()));
    }

    @Test
    void collapsedLayoutUsesPanelAsToggleHitRect() {
        CanvasMinimapGeometry.Layout layout = CanvasMinimapGeometry.layout(240, 180, true);

        assertEquals(layout.panelX(), layout.toggleX());
        assertEquals(layout.panelY(), layout.toggleY());
        assertEquals(layout.panelW(), layout.toggleW());
        assertEquals(layout.panelH(), layout.toggleH());
        assertEquals(0, layout.mapW());
        assertEquals(0, layout.mapH());
        assertTrue(CanvasMinimapGeometry.hit(232, 134, layout.panelX(), layout.panelY(), layout.panelW(), layout.panelH()));
        assertTrue(CanvasMinimapGeometry.hit(232, 134, layout.toggleX(), layout.toggleY(), layout.toggleW(), layout.toggleH()));
        assertFalse(CanvasMinimapGeometry.hit(230, 134, layout.panelX(), layout.panelY(), layout.panelW(), layout.panelH()));
    }

    @Test
    void minimapDragCoordinatesClampToWorldBounds() {
        TabletUiState state = new TabletUiState();
        state.canvas.minimapX = 10;
        state.canvas.minimapY = 20;
        state.canvas.minimapW = 100;
        state.canvas.minimapH = 50;
        state.canvas.minimapWorldMinX = 50;
        state.canvas.minimapWorldMinY = -25;
        state.canvas.minimapWorldWidth = 200;
        state.canvas.minimapWorldHeight = 100;

        assertEquals(50, CanvasMinimapGeometry.mapWorldX(state, -200));
        assertEquals(250, CanvasMinimapGeometry.mapWorldX(state, 900));
        assertEquals(-25, CanvasMinimapGeometry.mapWorldY(state, -200));
        assertEquals(75, CanvasMinimapGeometry.mapWorldY(state, 900));
    }
}
