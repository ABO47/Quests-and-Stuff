package com.abo47.questsandstuff.client.tablet.assets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssetPathResolverTest {
    @TempDir
    Path root;

    @Test
    void normalizesRelativeAssetPaths() {
        assertEquals("pics/castle.png", AssetPathResolver.normalizeRelative("\\pics\\castle.png/"));
        assertEquals("", AssetPathResolver.normalizeRelative("../outside.png"));
        assertEquals("", AssetPathResolver.normalizeRelative("pics/../outside.png"));
    }

    @Test
    void keepsResolvedDirectoriesInsideRoot() {
        assertTrue(AssetPathResolver.resolveDirectory(root, "pics").startsWith(root.normalize()));
        assertNull(AssetPathResolver.resolveDirectory(root, "../bad"));
    }

    @Test
    void detectsSupportedAssetTypesByDirectory() {
        assertTrue(AssetPathResolver.isSupportedAsset("pics", "hero.webp"));
        assertTrue(AssetPathResolver.isSupportedAsset("sounds", "complete.ogg"));
    }
}
