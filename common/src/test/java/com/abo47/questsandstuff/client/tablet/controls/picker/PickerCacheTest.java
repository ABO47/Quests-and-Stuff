package com.abo47.questsandstuff.client.tablet.controls.picker;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PickerCacheTest {
    @Test
    void queryCacheReusesSourceAndQueryForSameOwner() {
        PickerCache<String, String, String, String> cache = new PickerCache<>();
        AtomicInteger sourceBuilds = new AtomicInteger();
        AtomicInteger queryBuilds = new AtomicInteger();

        String first = cache.query("owner", "stone", () -> {
            sourceBuilds.incrementAndGet();
            return "source";
        }, source -> {
            queryBuilds.incrementAndGet();
            return source + ":stone";
        });
        String second = cache.query("owner", "stone", () -> {
            sourceBuilds.incrementAndGet();
            return "changed";
        }, source -> {
            queryBuilds.incrementAndGet();
            return source + ":changed";
        });

        assertEquals("source:stone", first);
        assertEquals("source:stone", second);
        assertEquals(1, sourceBuilds.get());
        assertEquals(1, queryBuilds.get());
    }

    @Test
    void ownerChangeClearsSourceAndQuery() {
        PickerCache<String, String, String, String> cache = new PickerCache<>();
        AtomicInteger sourceBuilds = new AtomicInteger();
        AtomicInteger queryBuilds = new AtomicInteger();

        cache.query("old", "same-query", () -> {
            sourceBuilds.incrementAndGet();
            return "old-source";
        }, source -> {
            queryBuilds.incrementAndGet();
            return source;
        });

        String rebuilt = cache.query("new", "same-query", () -> {
            sourceBuilds.incrementAndGet();
            return "new-source";
        }, source -> {
            queryBuilds.incrementAndGet();
            return source;
        });

        assertEquals("new-source", rebuilt);
        assertEquals(2, sourceBuilds.get());
        assertEquals(2, queryBuilds.get());
    }

    @Test
    void invalidateClearsEverything() {
        PickerCache<String, String, String, String> cache = new PickerCache<>();
        AtomicInteger sourceBuilds = new AtomicInteger();

        cache.source("owner", () -> {
            sourceBuilds.incrementAndGet();
            return "source";
        });
        cache.invalidate();
        String rebuilt = cache.source("owner", () -> {
            sourceBuilds.incrementAndGet();
            return "rebuilt";
        });

        assertEquals("rebuilt", rebuilt);
        assertEquals(2, sourceBuilds.get());
    }
}
