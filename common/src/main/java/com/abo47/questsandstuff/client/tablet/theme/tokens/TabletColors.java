package com.abo47.questsandstuff.client.tablet.theme.tokens;

/**
 * Process-global tablet palette. Render paths read these mutable values directly for the
 * current theme; `UiThemeManager` is the only class that should assign them.
 */
public final class TabletColors {
    private TabletColors() {
    }

    public static final int DEFAULT_SURFACE_BASE = 0xFF171C21;
    public static final int DEFAULT_SURFACE_PANEL = 0xFF202933;
    public static final int DEFAULT_SURFACE_PANEL_ALT = 0xFF2C3742;

    public static final int DEFAULT_BORDER_BASE = 0xFF546170;
    public static final int DEFAULT_BORDER_ACCENT = 0xFF65B7C8;

    public static final int DEFAULT_TEXT_PRIMARY = 0xFFEAF1F4;
    public static final int DEFAULT_TEXT_SECONDARY = 0xFFB8C7CE;
    public static final int DEFAULT_TEXT_MUTED = 0xFF88979F;

    public static final int DEFAULT_SUCCESS = 0xFF66D38D;
    public static final int DEFAULT_WARNING = 0xFFE5B44A;
    public static final int DEFAULT_ERROR = 0xFFE06F73;
    public static final int DEFAULT_INTERACTIVE = 0xFF64C3D2;
    public static final int DEFAULT_GLOW = 0xFF64C3D2;
    public static final int DEFAULT_SELECTION = 0xFF6BA8FF;
    public static final int DEFAULT_SCROLL_TRACK = DEFAULT_BORDER_BASE;
    public static final int DEFAULT_SCROLL_THUMB = DEFAULT_INTERACTIVE;

    // Common constants
    public static final int BLACK = 0xFF000000;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int TRANSPARENT = 0x00000000;
    public static final int DIM_OVERLAY = 0x60000000;

    // Chunk claim overlay colors
    public static final int DEFAULT_CLAIMED_FILL = 0x994F8DF7;
    public static final int DEFAULT_FORCE_FILL = 0x9966D38D;
    public static final int DEFAULT_CLAIMED_EDGE = 0xFF6FA8FF;
    public static final int DEFAULT_FORCE_EDGE = 0xFF66D38D;
    public static final int DEFAULT_GRID_COLOR = 0x33546770;

    // Terrain map colors
    public static final int TERRAIN_LAVA = 0xC65F1A;
    public static final int TERRAIN_SNOW = 0xEAF2F8;
    public static final int TERRAIN_ICE = 0xA9DCEC;
    public static final int TERRAIN_SAND = 0xD9C89C;
    public static final int TERRAIN_DEFAULT_FALLBACK = 0x6E6E6E;

    // Text color palette defaults
    public static final int PALETTE_BG_LIGHT = 0xFFE8F4FF;
    public static final int PALETTE_BG_MUTED = 0xFFB7CFDF;
    public static final int PALETTE_BG_DARK = 0xFF8EA5B7;
    public static final int PALETTE_SUCCESS = 0xFF63D187;
    public static final int PALETTE_WARNING = 0xFFE7B84B;
    public static final int PALETTE_ERROR = 0xFFE06D6D;
    public static final int PALETTE_INTERACTIVE = 0xFF53A6E8;
    public static final int PALETTE_SURFACE_DARK = 0xFF1D2730;

    public static volatile int SURFACE_BASE = DEFAULT_SURFACE_BASE;
    public static volatile int SURFACE_PANEL = DEFAULT_SURFACE_PANEL;
    public static volatile int SURFACE_PANEL_ALT = DEFAULT_SURFACE_PANEL_ALT;

    public static volatile int BORDER_BASE = DEFAULT_BORDER_BASE;
    public static volatile int BORDER_ACCENT = DEFAULT_BORDER_ACCENT;

    public static volatile int TEXT_PRIMARY = DEFAULT_TEXT_PRIMARY;
    public static volatile int TEXT_SECONDARY = DEFAULT_TEXT_SECONDARY;
    public static volatile int TEXT_MUTED = DEFAULT_TEXT_MUTED;

    public static volatile int SUCCESS = DEFAULT_SUCCESS;
    public static volatile int WARNING = DEFAULT_WARNING;
    public static volatile int ERROR = DEFAULT_ERROR;
    public static volatile int INTERACTIVE = DEFAULT_INTERACTIVE;
    public static volatile int GLOW = DEFAULT_GLOW;
    public static volatile int SELECTION = DEFAULT_SELECTION;
    public static volatile int SCROLL_TRACK = DEFAULT_SCROLL_TRACK;
    public static volatile int SCROLL_THUMB = DEFAULT_SCROLL_THUMB;

    public static int elevatedSurface() {
        return mix(SURFACE_PANEL_ALT, TEXT_PRIMARY, 10);
    }

    public static int recessedSurface() {
        return mix(SURFACE_BASE, BLACK, 12);
    }

    public static int subtleBorder() {
        return mix(BORDER_BASE, SURFACE_BASE, 28);
    }

    public static int focusBorder() {
        return mix(BORDER_ACCENT, TEXT_PRIMARY, 10);
    }

    public static int hoverFill(int accent) {
        return UiThemeTokens.withAlpha(accent, 46);
    }

    public static int pressedFill(int accent) {
        return UiThemeTokens.withAlpha(accent, 76);
    }

    public static int scrollTrack(boolean active) {
        return UiThemeTokens.withAlpha(SCROLL_TRACK, active ? 190 : 140);
    }

    public static int scrollThumb(boolean active) {
        return UiThemeTokens.withAlpha(SCROLL_THUMB, active ? 255 : 220);
    }

    private static int mix(int color, int other, int otherPercent) {
        int p = Math.max(0, Math.min(100, otherPercent));
        int inv = 100 - p;
        int a = (((color >>> 24) & 0xFF) * inv + ((other >>> 24) & 0xFF) * p) / 100;
        int r = (((color >>> 16) & 0xFF) * inv + ((other >>> 16) & 0xFF) * p) / 100;
        int g = (((color >>> 8) & 0xFF) * inv + ((other >>> 8) & 0xFF) * p) / 100;
        int b = ((color & 0xFF) * inv + (other & 0xFF) * p) / 100;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
