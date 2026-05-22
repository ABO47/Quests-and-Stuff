package com.abo47.questsandstuff.client.tablet.controls;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScrollControllerTest {
    @Test
    void clampsScrollValues() {
        assertEquals(0, ScrollController.clamp(-5, 20));
        assertEquals(12, ScrollController.clamp(12, 20));
        assertEquals(20, ScrollController.clamp(30, 20));
        assertEquals(0, ScrollController.clamp(30, -1));
    }

    @Test
    void wheelMovesOppositeWheelDeltaAndClamps() {
        assertEquals(4, ScrollController.wheel(8, 20, 4, 1));
        assertEquals(12, ScrollController.wheel(8, 20, 4, -1));
        assertEquals(0, ScrollController.wheel(1, 20, 4, 1));
        assertEquals(20, ScrollController.wheel(19, 20, 4, -1));
    }

    @Test
    void mapsMousePositionIntoScrollSpan() {
        assertEquals(0, ScrollController.byMouse(0, 0, 100, 20, 50));
        assertEquals(25, ScrollController.byMouse(50, 0, 100, 20, 50));
        assertEquals(50, ScrollController.byMouse(100, 0, 100, 20, 50));
    }

    @Test
    void hitChecksInclusiveBounds() {
        assertTrue(ScrollController.hit(10, 20, 10, 20, 4, 100));
        assertTrue(ScrollController.hit(14, 120, 10, 20, 4, 100));
        assertFalse(ScrollController.hit(15, 120, 10, 20, 4, 100));
    }
}
