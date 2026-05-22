package com.abo47.questsandstuff.client.tablet.controls;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TileGridLayoutTest {
    @Test
    void calculatesVisibleGridWithoutScrollbar() {
        TileGridLayout layout = TileGridLayout.calculate(100, 80, 20, 10, 4, 4, 4, 12, 999);

        assertEquals(4, layout.cols());
        assertEquals(5, layout.rows());
        assertEquals(20, layout.pageSize());
        assertFalse(layout.showScroll());
        assertEquals(0, layout.scrollStart());
        assertEquals(12, layout.visibleEnd());
        assertEquals(76, layout.tileX(3));
        assertEquals(18, layout.tileY(4));
    }

    @Test
    void reservesScrollbarSpaceAndClampsStart() {
        TileGridLayout layout = TileGridLayout.calculate(60, 40, 16, 10, 2, 4, 4, 20, 99);

        assertEquals(2, layout.cols());
        assertEquals(2, layout.rows());
        assertEquals(4, layout.pageSize());
        assertTrue(layout.showScroll());
        assertEquals(16, layout.maxStart());
        assertEquals(16, layout.scrollStart());
        assertEquals(2, layout.wheelStep());
        assertEquals(20, layout.visibleEnd());
        assertEquals(53, layout.scrollBarX());
        assertEquals(4, layout.scrollBarY());
        assertEquals(22, layout.scrollBarH());
        assertEquals(12, layout.knobH());
    }

    @Test
    void sanitizesInvalidInputs() {
        TileGridLayout layout = TileGridLayout.calculate(0, 0, 0, 0, -1, -2, -3, -5, -10);

        assertEquals(1, layout.tileW());
        assertEquals(1, layout.tileH());
        assertEquals(0, layout.gap());
        assertEquals(0, layout.padX());
        assertEquals(0, layout.padY());
        assertEquals(0, layout.entryCount());
        assertEquals(0, layout.scrollStart());
        assertEquals(0, layout.visibleEnd());
        assertFalse(layout.showScroll());
    }
}
