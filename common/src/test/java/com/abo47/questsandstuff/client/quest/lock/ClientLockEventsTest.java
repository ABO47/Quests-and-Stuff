package com.abo47.questsandstuff.client.quest.lock;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientLockEventsTest {
    @Test
    void firesToAllRegisteredListeners() {
        AtomicInteger first = new AtomicInteger();
        AtomicInteger second = new AtomicInteger();
        ClientLockEvents.Listener listenerOne = () -> first.incrementAndGet();
        ClientLockEvents.Listener listenerTwo = () -> second.incrementAndGet();
        try {
            ClientLockEvents.register(listenerOne);
            ClientLockEvents.register(listenerTwo);
            ClientLockEvents.fire();
        } finally {
            ClientLockEvents.unregister(listenerOne);
            ClientLockEvents.unregister(listenerTwo);
        }
        assertEquals(1, first.get());
        assertEquals(1, second.get());
    }

    @Test
    void unregisteredListenerStopsReceiving() {
        AtomicInteger count = new AtomicInteger();
        ClientLockEvents.Listener listener = count::incrementAndGet;
        try {
            ClientLockEvents.register(listener);
            ClientLockEvents.unregister(listener);
            ClientLockEvents.fire();
        } finally {
            ClientLockEvents.unregister(listener);
        }
        assertEquals(0, count.get());
    }

    @Test
    void failingListenerDoesNotBlockOthers() {
        AtomicInteger afterFailure = new AtomicInteger();
        ClientLockEvents.Listener failing = () -> {
            throw new IllegalStateException("boom");
        };
        ClientLockEvents.Listener healthy = afterFailure::incrementAndGet;
        try {
            ClientLockEvents.register(failing);
            ClientLockEvents.register(healthy);
            ClientLockEvents.fire();
        } finally {
            ClientLockEvents.unregister(failing);
            ClientLockEvents.unregister(healthy);
        }
        assertEquals(1, afterFailure.get());
    }

    @Test
    void registerIgnoresNullListener() {
        ClientLockEvents.register(null);
        ClientLockEvents.unregister(null);
        assertTrue(true);
    }
}
