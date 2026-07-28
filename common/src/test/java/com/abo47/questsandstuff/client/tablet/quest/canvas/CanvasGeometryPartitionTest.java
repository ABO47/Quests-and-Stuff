package com.abo47.questsandstuff.client.tablet.quest.canvas;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CanvasGeometryPartitionTest {
    @Test
    void coordinateMapperConvertsBetweenLogicalAndScreenSpace() {
        TabletUiState state = new TabletUiState();
        state.canvas.canvasContentX = 10;
        state.canvas.canvasContentY = 20;
        state.canvas.canvasOffsetX = 5;
        state.canvas.canvasOffsetY = -3;
        state.canvas.canvasZoom = 2.0f;

        assertEquals(29, CanvasGeometry.screenX(state, 7));
        assertEquals(5.0, CanvasGeometry.screenToLogicalX(state, 25), 0.0001);
        assertEquals(41, CanvasGeometry.screenY(state, 12));
        assertEquals(4.0, CanvasGeometry.screenToLogicalY(state, 25), 0.0001);
    }

    @Test
    void questCardGeometryBuildsSlotAndVisualBounds() {
        TabletUiState state = new TabletUiState();
        state.canvas.canvasContentX = 4;
        state.canvas.canvasContentY = 6;
        state.canvas.canvasZoom = 1.0f;
        CompoundTag questTag = new CompoundTag();
        CompoundTag groups = new CompoundTag();
        CompoundTag main = new CompoundTag();
        main.putInt("x", 32);
        main.putInt("y", 48);
        main.putFloat("scale", 0.5f);
        groups.put("main", main);
        questTag.put("chapters", groups);

        QuestCardLayout layout = CanvasGeometry.layoutQuest("quest", questTag, state, "main");

        assertEquals(32, layout.logicalX());
        assertEquals(48, layout.logicalY());
        assertEquals(8, layout.logicalWidth());
        assertEquals(8, layout.logicalHeight());
        assertEquals(16, layout.slotLogicalWidth());
        assertEquals(16, layout.slotLogicalHeight());
        assertEquals(36, layout.visualLogicalX());
        assertEquals(52, layout.visualLogicalY());
        assertEquals(40, layout.x());
        assertEquals(58, layout.y());
        assertEquals(8, layout.width());
        assertEquals(8, layout.height());
    }

    @Test
    void gridMathFitsVisualBoundsInsideGridSlot() {
        CanvasGeometry.GridVisualBox box = CanvasGeometry.fitVisualBoxToGridSlot(9, 10, 31, 18, 16, 12, 12);

        assertEquals(17, box.x());
        assertEquals(17, box.y());
        assertEquals(31, box.width());
        assertEquals(15, box.height());
        assertEquals(16, box.slotX());
        assertEquals(16, box.slotY());
        assertEquals(32, box.slotWidth());
        assertEquals(16, box.slotHeight());
    }

    @Test
    void rotatedBoundsUsePivotForLayerGeometry() {
        assertArrayEquals(new int[]{20, 10, 30, 40}, CanvasGeometry.rotatedBoundsAtPivot(10, 20, 30, 10, 15, 5, 90));
        assertArrayEquals(new int[]{10, 20, 40, 30}, CanvasGeometry.rotatedBoundsAtPivot(10, 20, 30, 10, 15, 5, 360));
    }

    @Test
    void clampRotatedAnchorStaysInsideViewport() {
        TabletUiState state = new TabletUiState();
        state.canvas.canvasViewportW = 100;
        state.canvas.canvasViewportH = 100;
        state.canvas.canvasContentX = 0;
        state.canvas.canvasContentY = 0;
        state.canvas.canvasOffsetX = 0;
        state.canvas.canvasOffsetY = 0;
        state.canvas.canvasZoom = 1.0f;

        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, 90, 90, 20, 10, 10, 5, 90);

        assertEquals(90, clamped.x);
        assertEquals(90, clamped.y);
    }

    @Test
    void clampRotatedAnchorSlidesUnderEdgeWhenUnlocked() {
        TabletUiState state = new TabletUiState();
        state.canvas.canvasViewportW = 100;
        state.canvas.canvasViewportH = 100;
        state.canvas.canvasContentX = 0;
        state.canvas.canvasContentY = 0;
        state.canvas.canvasOffsetX = 0;
        state.canvas.canvasOffsetY = 0;
        state.canvas.canvasZoom = 1.0f;

        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, -10, -20, 20, 10, 10, 5, 0);

        assertEquals(-10, clamped.x);
        assertEquals(-5, clamped.y);
    }

    @Test
    void clampRotatedAnchorStaysFullyInsideWhenLocked() {
        TabletUiState state = new TabletUiState();
        state.canvas.gridCanvasLocked = true;
        state.canvas.canvasViewportW = 100;
        state.canvas.canvasViewportH = 100;
        state.canvas.canvasContentX = 0;
        state.canvas.canvasContentY = 0;
        state.canvas.canvasOffsetX = 0;
        state.canvas.canvasOffsetY = 0;
        state.canvas.canvasZoom = 1.0f;

        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, -10, -20, 20, 10, 10, 5, 0);

        assertEquals(0, clamped.x);
        assertEquals(0, clamped.y);
    }
}
