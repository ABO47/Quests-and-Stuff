package com.abo47.questsandstuff.client.tablet.visual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class VisualRefTest {
    @Test
    public void assetRefWrapsBarePath() {
        assertEquals("asset|pics/foo.png", VisualRef.assetRef("pics/foo.png"));
    }

    @Test
    public void assetRefIsIdempotent() {
        assertEquals("asset|pics/foo.png", VisualRef.assetRef("asset|pics/foo.png"));
    }

    @Test
    public void assetRefHandlesNullAndBlank() {
        assertEquals("asset|", VisualRef.assetRef(null));
        assertEquals("asset|", VisualRef.assetRef("   "));
    }

    @Test
    public void isAssetRefDetectsPrefix() {
        assertTrue(VisualRef.isAssetRef("asset|pics/foo.png"));
        assertFalse(VisualRef.isAssetRef("pics/foo.png"));
        assertFalse(VisualRef.isAssetRef(null));
        assertFalse(VisualRef.isAssetRef(""));
    }

    @Test
    public void assetPathStripsPrefix() {
        assertEquals("pics/foo.png", VisualRef.assetPath("asset|pics/foo.png"));
        assertEquals("", VisualRef.assetPath("pics/foo.png"));
        assertEquals("", VisualRef.assetPath(null));
    }
}
