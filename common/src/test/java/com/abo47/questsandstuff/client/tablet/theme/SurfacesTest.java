package com.abo47.questsandstuff.client.tablet.theme;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SurfacesTest {
    @Test
    void withAlphaOwnsTabletAlphaComposition() {
        assertEquals(0xAB345678, Surfaces.withAlpha(0x12345678, 0xAB));
        assertEquals(0xFF345678, Surfaces.withAlpha(0xFF345678, -1));
        assertEquals(0xFF345678, Surfaces.withAlpha(0x00345678, 0x1FF));
    }

    @Test
    void transparentSurfaceHelpersUseNamedTextures() {
        assertSame(IGuiTexture.EMPTY, Surfaces.transparent());
        assertNotNull(Surfaces.transparentFill());
        assertNotNull(Surfaces.group(Surfaces.transparent()));
    }
}
