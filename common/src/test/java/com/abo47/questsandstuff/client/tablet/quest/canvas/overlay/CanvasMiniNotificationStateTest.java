package com.abo47.questsandstuff.client.tablet.quest.canvas.overlay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanvasMiniNotificationStateTest {
    @Test
    void showStoresTheMessagePositionAndExpiry() {
        CanvasMiniNotificationState state = new CanvasMiniNotificationState();

        state.show(" ui.questsandstuff.saved ", 12, 34, 1_000L, 500L);

        assertEquals("ui.questsandstuff.saved", state.translationKey());
        assertEquals(12, state.x());
        assertEquals(34, state.y());
        assertEquals(1_500L, state.untilMs());
        assertTrue(state.active(1_499L));
        assertFalse(state.active(1_500L));
    }

    @Test
    void blankMessageClearsTheNotification() {
        CanvasMiniNotificationState state = new CanvasMiniNotificationState();
        state.show("ui.questsandstuff.saved", 12, 34, 1_000L, 500L);

        state.show(" ", 1, 2, 2_000L, 500L);

        assertEquals("", state.translationKey());
        assertEquals(0, state.x());
        assertEquals(0, state.y());
        assertEquals(0L, state.untilMs());
        assertFalse(state.active(2_100L));
    }

    @Test
    void negativeDurationExpiresImmediately() {
        CanvasMiniNotificationState state = new CanvasMiniNotificationState();

        state.show("ui.questsandstuff.saved", 12, 34, 1_000L, -50L);

        assertFalse(state.active(1_000L));
    }
}
