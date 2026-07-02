package com.abo47.questsandstuff.client.tablet.controls;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScrollMathTest {
    @Test
    void clampsScrollValues() {
        assertEquals(0, ScrollMath.clamp(-5, 20));
        assertEquals(12, ScrollMath.clamp(12, 20));
        assertEquals(20, ScrollMath.clamp(30, 20));
        assertEquals(0, ScrollMath.clamp(30, -1));
    }

    @Test
    void wheelMovesOppositeWheelDeltaAndClamps() {
        assertEquals(4, ScrollMath.wheel(8, 20, 4, 1));
        assertEquals(12, ScrollMath.wheel(8, 20, 4, -1));
        assertEquals(0, ScrollMath.wheel(1, 20, 4, 1));
        assertEquals(20, ScrollMath.wheel(19, 20, 4, -1));
    }

    @Test
    void mapsMousePositionIntoScrollSpan() {
        assertEquals(0, ScrollMath.byMouse(0, 0, 100, 20, 50));
        assertEquals(25, ScrollMath.byMouse(50, 0, 100, 20, 50));
        assertEquals(50, ScrollMath.byMouse(100, 0, 100, 20, 50));
    }

    @Test
    void hitChecksInclusiveBounds() {
        assertTrue(ScrollMath.hit(10, 20, 10, 20, 4, 100));
        assertTrue(ScrollMath.hit(14, 120, 10, 20, 4, 100));
        assertFalse(ScrollMath.hit(15, 120, 10, 20, 4, 100));
    }
}
