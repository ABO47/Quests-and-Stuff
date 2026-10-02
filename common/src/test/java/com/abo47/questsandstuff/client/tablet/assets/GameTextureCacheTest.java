package com.abo47.questsandstuff.client.tablet.assets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class GameTextureCacheTest {
    @Test
    public void gameRefsMatch() {
        assertTrue(GameTextureCache.isGameRef("minecraft:diamond"));
        assertTrue(GameTextureCache.isGameRef("item:minecraft:bow"));
        assertTrue(GameTextureCache.isGameRef("block:minecraft:stone"));
        assertTrue(GameTextureCache.isGameRef("#minecraft:swords"));
        assertTrue(GameTextureCache.isGameRef("item_stack|minecraft:diamond_sword"));
    }

    @Test
    public void filePathsNeverMatch() {
        assertFalse(GameTextureCache.isGameRef(null));
        assertFalse(GameTextureCache.isGameRef(""));
        assertFalse(GameTextureCache.isGameRef("pics/foo.png"));
        assertFalse(GameTextureCache.isGameRef("foo.png"));
        assertFalse(GameTextureCache.isGameRef("asset|pics/foo.png"));
    }
}
