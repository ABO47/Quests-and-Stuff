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
        state.canvasZoom = 2.0f;
        state.canvasContentW = 100;
        state.canvasContentH = 80;
        state.canvasOffsetX = -20;
        state.canvasOffsetY = -10;
        QuestCardLayout card = new QuestCardLayout("quest", new CompoundTag(), 0, 0, 10, 10, 16, 16, 80, 70, 1.0f, 0, 0, 10, 10);

        CanvasMinimapGeometry.WorldBounds bounds = CanvasMinimapGeometry.worldBounds(state, List.of(card));

        assertEquals(-14, bounds.minX());
        assertEquals(-19, bounds.minY());
        assertEquals(128, bounds.width());
        assertEquals(123, bounds.height());
    }

    @Test
    void mapsMinimapClicksIntoWorldSpace() {
        TabletUiState state = new TabletUiState();
        state.minimapX = 10;
        state.minimapY = 20;
        state.minimapW = 100;
        state.minimapH = 50;
        state.minimapWorldMinX = 50;
        state.minimapWorldMinY = -25;
        state.minimapWorldWidth = 200;
        state.minimapWorldHeight = 100;

        assertEquals(150, CanvasMinimapGeometry.mapWorldX(state, 60));
        assertEquals(25, CanvasMinimapGeometry.mapWorldY(state, 45));
    }
}
