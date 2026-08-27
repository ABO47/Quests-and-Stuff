package com.abo47.questsandstuff.client.tablet.theme;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.theme.skin.SkinFillOverride;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SkinFillOverrideTest {
    @Test
    void parseNullReturnsNull() {
        assertNull(SkinFillOverride.parse(null));
    }

    @Test
    void parseBlankReturnsNull() {
        assertNull(SkinFillOverride.parse(""));
        assertNull(SkinFillOverride.parse("  "));
    }

    @Test
    void parsePlainPathDefaultsToStretchMode() {
        SkinFillOverride result = SkinFillOverride.parse("path/to/image.png");
        assertNotNull(result);
        assertEquals("stretch", result.mode());
        assertEquals("path/to/image.png", result.path());
    }

    @Test
    void parseStretchPrefix() {
        SkinFillOverride result = SkinFillOverride.parse("stretch|assets/background.png");
        assertNotNull(result);
        assertEquals("stretch", result.mode());
        assertEquals("assets/background.png", result.path());
    }

    @Test
    void parseTilePrefix() {
        SkinFillOverride result = SkinFillOverride.parse("tile|path/to/tile.png");
        assertNotNull(result);
        assertEquals("tile", result.mode());
        assertEquals("path/to/tile.png", result.path());
    }

    @Test
    void parseHrstretchPrefix() {
        SkinFillOverride result = SkinFillOverride.parse("hrstretch|path/to/bar.png");
        assertNotNull(result);
        assertEquals("hrstretch", result.mode());
        assertEquals("path/to/bar.png", result.path());
        assertEquals(0, result.leftEdge());
        assertEquals(0, result.rightEdge());
    }

    @Test
    void parseHrstretchWithEdges() {
        SkinFillOverride result = SkinFillOverride.parse("hrstretch:8:12|path/to/bar.png");
        assertNotNull(result);
        assertEquals("hrstretch", result.mode());
        assertEquals("path/to/bar.png", result.path());
        assertEquals(8, result.leftEdge());
        assertEquals(12, result.rightEdge());
    }

    @Test
    void encodeHrstretchWithEdges() {
        SkinFillOverride override = new SkinFillOverride("hrstretch", 8, 12, "bar.png");
        assertEquals("hrstretch:8:12|bar.png", override.encode());
    }

    @Test
    void roundTripHrstretchWithEdges() {
        SkinFillOverride original = new SkinFillOverride("hrstretch", 8, 12, "some/bar.png");
        String encoded = original.encode();
        SkinFillOverride parsed = SkinFillOverride.parse(encoded);
        assertNotNull(parsed);
        assertEquals(original.mode(), parsed.mode());
        assertEquals(original.path(), parsed.path());
        assertEquals(original.leftEdge(), parsed.leftEdge());
        assertEquals(original.rightEdge(), parsed.rightEdge());
    }

    @Test
    void parsePipeOnlyReturnsNull() {
        assertNull(SkinFillOverride.parse("|"));
        assertNull(SkinFillOverride.parse("stretch|"));
        assertNull(SkinFillOverride.parse("tile|"));
    }

    @Test
    void encodeStretchReturnsPlainPath() {
        SkinFillOverride override = new SkinFillOverride("stretch", "img.png");
        assertEquals("img.png", override.encode());
    }

    @Test
    void encodeTileReturnsPrefixedForm() {
        SkinFillOverride override = new SkinFillOverride("tile", "img.png");
        assertEquals("tile|img.png", override.encode());
    }

    @Test
    void roundTripStretch() {
        SkinFillOverride original = new SkinFillOverride("stretch", "some/asset.png");
        String encoded = original.encode();
        SkinFillOverride parsed = SkinFillOverride.parse(encoded);
        assertNotNull(parsed);
        assertEquals(original.mode(), parsed.mode());
        assertEquals(original.path(), parsed.path());
    }

    @Test
    void roundTripTile() {
        SkinFillOverride original = new SkinFillOverride("tile", "some/tile.png");
        String encoded = original.encode();
        SkinFillOverride parsed = SkinFillOverride.parse(encoded);
        assertNotNull(parsed);
        assertEquals(original.mode(), parsed.mode());
        assertEquals(original.path(), parsed.path());
    }

    @Test
    void clearCacheDoesNotThrow() {
        SkinFillOverride.clearCache();
        SkinFillOverride override = new SkinFillOverride("stretch", "dummy.png");
        SkinFillOverride.parse(override.encode());
        SkinFillOverride.clearCache();
    }

    @Test
    void createTextureReturnsNullForNullPath() {
        SkinFillOverride nullPath = new SkinFillOverride("stretch", null);
        assertNull(nullPath.createTexture());
        SkinFillOverride blankPath = new SkinFillOverride("stretch", "");
        assertNull(blankPath.createTexture());
    }
}
