package com.abo47.questsandstuff.client.tablet.theme;

import org.junit.jupiter.api.Test;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SurfaceFactoryTest {
    @Test
    void withAlphaOwnsTabletAlphaComposition() {
        assertEquals(0xAB345678, SurfaceFactory.withAlpha(0x12345678, 0xAB));
        assertEquals(0xFF345678, SurfaceFactory.withAlpha(0xFF345678, -1));
        assertEquals(0xFF345678, SurfaceFactory.withAlpha(0x00345678, 0x1FF));
    }

    @Test
    void transparentSurfaceHelpersUseNamedTextures() {
        assertSame(IGuiTexture.EMPTY, SurfaceFactory.transparent());
        assertNotNull(SurfaceFactory.transparentFill());
        assertNotNull(SurfaceFactory.group(SurfaceFactory.transparent()));
    }
}
