package com.abo47.questsandstuff.client.tablet.actions;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.ui.IntegratedServerActions;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegratedServerActionsTest {
    @Test
    void nullPlayerUsesRemoteFallback() {
        AtomicBoolean local = new AtomicBoolean(false);
        AtomicBoolean remote = new AtomicBoolean(false);

        IntegratedServerActions.run(null, serverPlayer -> local.set(true), () -> remote.set(true));

        assertFalse(local.get());
        assertTrue(remote.get());
    }

    @Test
    void nullPlayerIsNotLocal() {
        assertFalse(IntegratedServerActions.canRunLocally(null));
    }
}
