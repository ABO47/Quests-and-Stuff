package com.abo47.questsandstuff.client.tablet.animation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiAnimationProgressTest {
    @Test
    void detectsRunningWindow() {
        assertTrue(UiAnimationProgress.running(1_000L, 200L, 1_100L));
        assertFalse(UiAnimationProgress.running(1_000L, 200L, 1_200L));
        assertFalse(UiAnimationProgress.running(0L, 200L, 1_100L));
    }

    @Test
    void easesOpenAndCloseProgress() {
        assertEquals(0.0f, UiAnimationProgress.openProgress(true, true, 1_000L, 200L, 1_000L), 0.001f);
        assertEquals(0.5f, UiAnimationProgress.openProgress(true, true, 1_000L, 200L, 1_100L), 0.001f);
        assertEquals(1.0f, UiAnimationProgress.openProgress(true, true, 1_000L, 200L, 1_200L), 0.001f);

        assertEquals(1.0f, UiAnimationProgress.openProgress(false, false, 1_000L, 200L, 1_000L), 0.001f);
        assertEquals(0.5f, UiAnimationProgress.openProgress(false, false, 1_000L, 200L, 1_100L), 0.001f);
        assertEquals(0.0f, UiAnimationProgress.openProgress(false, false, 1_000L, 200L, 1_200L), 0.001f);
    }

    @Test
    void interpolatesWithClampedProgress() {
        assertEquals(44, UiAnimationProgress.interpolate(44, 168, -0.5f));
        assertEquals(106, UiAnimationProgress.interpolate(44, 168, 0.5f));
        assertEquals(168, UiAnimationProgress.interpolate(44, 168, 1.5f));
    }
}
