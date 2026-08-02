package com.abo47.questsandstuff.client.tablet.chunkclaimer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkMapGeometryTest {
    @Test
    void cellSizeUsesSmallestSpanDividedByGrid() {
        assertEquals(20, ChunkMapGeometry.cellSize(180, 180, 9, 9));
        assertEquals(11, ChunkMapGeometry.cellSize(100, 180, 9, 9));
        assertEquals(1, ChunkMapGeometry.cellSize(5, 5, 9, 9));
    }

    @Test
    void gridOriginCentersEvenSpan() {
        assertEquals(0, ChunkMapGeometry.gridOriginX(180, 20, 9));
        assertEquals(0, ChunkMapGeometry.gridOriginY(180, 20, 9));
    }

    @Test
    void cellPixelAndDeltaAreInverses() {
        int cell = 20;
        int origin = 0;
        int gridW = 9;
        assertEquals(80, ChunkMapGeometry.cellPixelX(origin, cell, gridW, 0));
        assertEquals(0, ChunkMapGeometry.cellPixelX(origin, cell, gridW, -4));
        assertEquals(160, ChunkMapGeometry.cellPixelX(origin, cell, gridW, 4));

        assertEquals(-4, ChunkMapGeometry.deltaX(0, origin, cell, gridW));
        assertEquals(0, ChunkMapGeometry.deltaX(80, origin, cell, gridW));
        assertEquals(4, ChunkMapGeometry.deltaX(160, origin, cell, gridW));
    }

    @Test
    void inGridRejectsOutliers() {
        assertTrue(ChunkMapGeometry.inGridX(0, 9));
        assertTrue(ChunkMapGeometry.inGridX(4, 9));
        assertTrue(ChunkMapGeometry.inGridX(-4, 9));
        assertFalse(ChunkMapGeometry.inGridX(5, 9));
        assertFalse(ChunkMapGeometry.inGridX(-5, 9));

        assertTrue(ChunkMapGeometry.inGridZ(0, 11));
        assertTrue(ChunkMapGeometry.inGridZ(5, 11));
        assertFalse(ChunkMapGeometry.inGridZ(6, 11));
    }

    @Test
    void floorDeltaUsesFloorDivisionForNegativeSpans() {
        int cell = 20;
        int gridW = 9;
        assertEquals(-4, ChunkMapGeometry.floorDeltaX(0, 0, cell, gridW));
        assertEquals(0, ChunkMapGeometry.floorDeltaX(80, 0, cell, gridW));
        assertEquals(4, ChunkMapGeometry.floorDeltaX(160, 0, cell, gridW));
        assertEquals(-4, ChunkMapGeometry.floorDeltaX(19, 0, cell, gridW));
        assertEquals(3, ChunkMapGeometry.floorDeltaX(140, 0, cell, gridW));
        assertEquals(-5, ChunkMapGeometry.floorDeltaX(-5, 0, cell, gridW));
        assertEquals(-4, ChunkMapGeometry.floorDeltaZ(19, 0, cell, 9));
        assertEquals(3, ChunkMapGeometry.floorDeltaZ(140, 0, cell, 9));
    }

    @Test
    void cellAtPicksFloorCellInsideWidget() {
        ChunkMapGeometry.ChunkMapCell cell = ChunkMapGeometry.cellAt(90, 90, 180, 180, 9, 9);
        assertTrue(cell != null, "Center of the map should be a cell");
        assertEquals(0, cell.dx(), "Center column delta");
        assertEquals(0, cell.dz(), "Center row delta");

        ChunkMapGeometry.ChunkMapCell corner = ChunkMapGeometry.cellAt(0, 0, 180, 180, 9, 9);
        assertTrue(corner != null, "Top-left corner should be a cell");
        assertEquals(-4, corner.dx(), "Top-left column delta");
        assertEquals(-4, corner.dz(), "Top-left row delta");
    }

    @Test
    void cellAtRejectsOutOfWidgetCoordinates() {
        assertNull(ChunkMapGeometry.cellAt(-5, 90, 180, 180, 9, 9), "Left of widget should be null");
        assertNull(ChunkMapGeometry.cellAt(90, -5, 180, 180, 9, 9), "Above widget should be null");
    }
}
