package com.abo47.questsandstuff.client.tablet.controls;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchFilterTest {
    @Test
    void normalizesUserQueries() {
        assertEquals("nether fortress", SearchFilter.normalize("  Nether Fortress  "));
        assertEquals("netherfortress", SearchFilter.normalizeKey("Nether Fortress"));
        assertEquals("nether fortress", SearchFilter.normalizeUserInput("Nether Fortress"));
    }

    @Test
    void matchesRawIdsDisplayNamesAndCompactKeys() {
        assertTrue(SearchFilter.matches("minecraft:plains", "minecraft:plains", "Plains"));
        assertTrue(SearchFilter.matches("lost city", "custom:chests/lost_city", "Lost City Chest"));
        assertTrue(SearchFilter.matches("lostcity", "custom:chests/lost_city", "Lost City Chest"));
        assertFalse(SearchFilter.matches("ocean", "minecraft:plains", "Plains"));
    }

    @Test
    void cropsNullAndLongValues() {
        assertEquals("", SearchFilter.crop(null, 8));
        assertEquals("short", SearchFilter.crop("short", 8));
        assertEquals("very...", SearchFilter.crop("very long name", 7));
    }
}
