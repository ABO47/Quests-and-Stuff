package com.abo47.questsandstuff.client.tablet.chunkclaimer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
