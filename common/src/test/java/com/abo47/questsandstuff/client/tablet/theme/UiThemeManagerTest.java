package com.abo47.questsandstuff.client.tablet.theme;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UiThemeManagerTest {
    private static final Map<String, Integer> CUSTOM_PALETTE = palette(
            0xFF010203,
            0xFF111213,
            0xFF212223,
            0xFF313233,
            0xFF414243,
            0xFF515253,
            0xFF616263,
            0xFF717273,
            0xFF818283,
            0xFF919293,
            0xFFA1A2A3,
            0xFFB1B2B3,
            0xFFC1C2C3,
            0xFFD1D2D3
    );

    private static final Map<String, Integer> DEFAULT_PALETTE = palette(
            ModColors.DEFAULT_SURFACE_BASE,
            ModColors.DEFAULT_SURFACE_PANEL,
            ModColors.DEFAULT_SURFACE_PANEL_ALT,
            ModColors.DEFAULT_BORDER_BASE,
            ModColors.DEFAULT_BORDER_ACCENT,
            ModColors.DEFAULT_TEXT_PRIMARY,
            ModColors.DEFAULT_TEXT_SECONDARY,
            ModColors.DEFAULT_TEXT_MUTED,
            ModColors.DEFAULT_SUCCESS,
            ModColors.DEFAULT_WARNING,
            ModColors.DEFAULT_ERROR,
            ModColors.DEFAULT_INTERACTIVE,
            ModColors.DEFAULT_SCROLL_TRACK,
            ModColors.DEFAULT_SCROLL_THUMB
    );

    @TempDir
    Path tempDir;

    @AfterEach
    void resetTheme() {
        UiThemeManager.resetGlobalPaletteToDefaults();
    }

    @Test
    void loadedThemeAppliesGlobalPaletteAndResetReturnsDefaults() throws Exception {
        Path theme = tempDir.resolve("custom.json");
        Files.writeString(theme, customThemeJson());

        UiThemeManager.applyLoadedState(UiThemeJsonCodec.loadUiThemeState(theme));
        assertEquals(CUSTOM_PALETTE, currentPalette());

        UiThemeManager.resetGlobalPaletteToDefaults();
        assertEquals(DEFAULT_PALETTE, currentPalette());
    }

    @Test
    void malformedThemeLoadFallbackResetsPreviousGlobalPalette() throws Exception {
        Path custom = tempDir.resolve("custom.json");
        Files.writeString(custom, customThemeJson());
        UiThemeManager.applyLoadedState(UiThemeJsonCodec.loadUiThemeState(custom));
        assertEquals(CUSTOM_PALETTE, currentPalette());

        Path malformed = tempDir.resolve("malformed.json");
        Files.writeString(malformed, "{not valid json");
        UiThemeManager.applyLoadedState(UiThemeJsonCodec.loadUiThemeState(malformed));

        assertEquals(DEFAULT_PALETTE, currentPalette());
    }

    private static String customThemeJson() {
        return """
                {
                  "name": "Custom",
                  "colors": {
                    "surface_base": "#FF010203",
                    "surface_panel": "#FF111213",
                    "surface_panel_alt": "#FF212223",
                    "border_base": "#FF313233",
                    "border_accent": "#FF414243",
                    "text_primary": "#FF515253",
                    "text_secondary": "#FF616263",
                    "text_muted": "#FF717273",
                    "success": "#FF818283",
                    "warning": "#FF919293",
                    "error": "#FFA1A2A3",
                    "interactive": "#FFB1B2B3",
                    "icon_scroll_track": "#FFC1C2C3",
                    "icon_scroll_thumb": "#FFD1D2D3"
                  }
                }
                """;
    }

    private static Map<String, Integer> currentPalette() {
        return palette(
                ModColors.SURFACE_BASE,
                ModColors.SURFACE_PANEL,
                ModColors.SURFACE_PANEL_ALT,
                ModColors.BORDER_BASE,
                ModColors.BORDER_ACCENT,
                ModColors.TEXT_PRIMARY,
                ModColors.TEXT_SECONDARY,
                ModColors.TEXT_MUTED,
                ModColors.SUCCESS,
                ModColors.WARNING,
                ModColors.ERROR,
                ModColors.INTERACTIVE,
                ModColors.SCROLL_TRACK,
                ModColors.SCROLL_THUMB
        );
    }

    private static Map<String, Integer> palette(
            int surfaceBase,
            int surfacePanel,
            int surfacePanelAlt,
            int borderBase,
            int borderAccent,
            int textPrimary,
            int textSecondary,
            int textMuted,
            int success,
            int warning,
            int error,
            int interactive,
            int scrollTrack,
            int scrollThumb
    ) {
        Map<String, Integer> palette = new LinkedHashMap<>();
        palette.put("SURFACE_BASE", surfaceBase);
        palette.put("SURFACE_PANEL", surfacePanel);
        palette.put("SURFACE_PANEL_ALT", surfacePanelAlt);
        palette.put("BORDER_BASE", borderBase);
        palette.put("BORDER_ACCENT", borderAccent);
        palette.put("TEXT_PRIMARY", textPrimary);
        palette.put("TEXT_SECONDARY", textSecondary);
        palette.put("TEXT_MUTED", textMuted);
        palette.put("SUCCESS", success);
        palette.put("WARNING", warning);
        palette.put("ERROR", error);
        palette.put("INTERACTIVE", interactive);
        palette.put("SCROLL_TRACK", scrollTrack);
        palette.put("SCROLL_THUMB", scrollThumb);
        return Map.copyOf(palette);
    }
}
