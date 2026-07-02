package com.abo47.questsandstuff.client.tablet.text;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.text.format.DisplayNameFormatter;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DisplayNameFormatterTest {
    @Test
    void resourceLeafFormatsNamespacedPaths() {
        assertEquals("Village Blacksmith", DisplayNameFormatter.resourceLeaf("minecraft:chests/village_blacksmith"));
        assertEquals("Very Cool Name", DisplayNameFormatter.resourceLeaf("mod\\nested\\very-cool_name"));
    }

    @Test
    void displayNamesOverrideRawIds() {
        assertEquals(
                "Lost City Chest",
                DisplayNameFormatter.lootTable("minecraft:chests/simple_dungeon", Map.of("minecraft:chests/simple_dungeon", "Lost City Chest"))
        );
    }

    @Test
    void biomeFallsBackToReadableResourceId() {
        assertEquals("Old Growth Pine Taiga", DisplayNameFormatter.biome("minecraft:old_growth_pine_taiga", Map.of()));
    }

    @Test
    void titleCaseNormalizesSeparatorsAndSpaces() {
        assertEquals("Hello World Name", DisplayNameFormatter.titleCase("  hello/world-name  "));
        assertEquals("Hello World Name", DisplayNameFormatter.titleCase("hello   world///name"));
    }
}
