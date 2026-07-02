package com.abo47.questsandstuff.client.tablet.theme.codec;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Owns the process-global tablet theme. Existing render code reads `TabletColors`
 * statics directly, so all palette mutation is routed through this manager.
 */
public final class UiThemeManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Object LOCK = new Object();
    private static final long RELOAD_CHECK_MS = 1500L;

    public static final String ROLE_ICON_DEFAULT = "icon_default";
    public static final String ROLE_ICON_INTERACTIVE = "icon_interactive";
    public static final String ROLE_ICON_SUCCESS = "icon_success";
    public static final String ROLE_ICON_WARNING = "icon_warning";
    public static final String ROLE_ICON_ERROR = "icon_error";
    public static final String ROLE_ICON_MUTED = "icon_muted";
    public static final String ROLE_ICON_SCROLL_TRACK = "icon_scroll_track";
    public static final String ROLE_ICON_SCROLL_THUMB = "icon_scroll_thumb";
    public static final String UI_SURFACE_BASE = "surface_base";
    public static final String UI_SURFACE_PANEL = "surface_panel";
    public static final String UI_SURFACE_PANEL_ALT = "surface_panel_alt";
    public static final String UI_BORDER_BASE = "border_base";
    public static final String UI_BORDER_ACCENT = "border_accent";
    public static final String UI_TEXT_PRIMARY = "text_primary";
    public static final String UI_TEXT_SECONDARY = "text_secondary";
    public static final String UI_TEXT_MUTED = "text_muted";
    public static final String UI_SUCCESS = "success";
    public static final String UI_WARNING = "warning";
    public static final String UI_ERROR = "error";
    public static final String UI_INTERACTIVE = "interactive";

    private static UiThemeState state = UiThemeState.defaults();
    private static boolean initialized = false;
    private static long lastCheckMs = 0L;
    private static long activeFileMtime = Long.MIN_VALUE;
    private static long themeFileMtime = Long.MIN_VALUE;
    private static Path loadedThemePath = null;

    private UiThemeManager() {
    }

    public static int iconColor(String iconFileName) {
        ensureFresh();
        return state.colorForIcon(iconFileName);
    }

    public static int colorForRole(String role) {
        ensureFresh();
        return state.colorForRole(role);
    }

    public static List<ThemeInfo> availableThemes() {
        ensureFresh();
        synchronized (LOCK) {
            UiThemeFiles.bootstrapIfMissing(GSON);
            List<ThemeInfo> themes = new ArrayList<>();
            try {
                UiThemeFiles.themeFiles().stream()
                        .map(UiThemeJsonCodec::readThemeInfo)
                        .forEach(themes::add);
            } catch (Exception e) {
                QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed listing UI themes in {}", UiThemeFiles.THEMES_DIR, e);
            }
            themes.sort(Comparator.comparing((ThemeInfo info) -> UiThemeFiles.DEFAULT_THEME_NAME.equals(info.id()) ? "" : info.label().toLowerCase(Locale.ROOT)));
            return List.copyOf(themes);
        }
    }

    public static String activeThemeName() {
        ensureFresh();
        synchronized (LOCK) {
            return UiThemeFiles.themeIdFromPath(loadedThemePath);
        }
    }

    public static boolean setActiveTheme(String themeName) {
        String normalized = UiThemeFiles.normalizeThemeName(themeName);
        if (normalized.isBlank()) {
            return false;
        }
        synchronized (LOCK) {
            UiThemeFiles.bootstrapIfMissing(GSON);
            Path themePath = UiThemeFiles.themePath(normalized);
            if (!UiThemeFiles.isThemePath(themePath)) {
                return false;
            }
            try {
                JsonObject active = new JsonObject();
                active.addProperty("theme", normalized);
                UiThemeFiles.writeString(UiThemeFiles.ACTIVE_THEME_FILE, GSON.toJson(active));
                installLoadedState(UiThemeJsonCodec.loadUiThemeState(themePath));
                initialized = true;
                lastCheckMs = System.currentTimeMillis();
                activeFileMtime = UiThemeFiles.safeMtime(UiThemeFiles.ACTIVE_THEME_FILE);
                themeFileMtime = UiThemeFiles.safeMtime(themePath);
                loadedThemePath = themePath;
                QuestsAndStuffMod.debugLog("[QnS:UI] Active UI theme changed to {}", normalized);
                return true;
            } catch (Exception e) {
                QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed setting active UI theme {}", normalized, e);
                return false;
            }
        }
    }

    private static void ensureFresh() {
        long now = System.currentTimeMillis();
        if (initialized && now - lastCheckMs < RELOAD_CHECK_MS) {
            return;
        }
        synchronized (LOCK) {
            now = System.currentTimeMillis();
            if (initialized && now - lastCheckMs < RELOAD_CHECK_MS) {
                return;
            }
            lastCheckMs = now;

            UiThemeFiles.bootstrapIfMissing(GSON);

            long currentActiveMtime = UiThemeFiles.safeMtime(UiThemeFiles.ACTIVE_THEME_FILE);
            Path themePath = UiThemeFiles.resolveActiveThemePath();
            long currentThemeMtime = UiThemeFiles.safeMtime(themePath);

            boolean changed = !initialized
                    || currentActiveMtime != activeFileMtime
                    || currentThemeMtime != themeFileMtime
                    || loadedThemePath == null
                    || !loadedThemePath.equals(themePath);
            if (!changed) {
                return;
            }

            installLoadedState(UiThemeJsonCodec.loadUiThemeState(themePath));
            initialized = true;
            activeFileMtime = currentActiveMtime;
            themeFileMtime = currentThemeMtime;
            loadedThemePath = themePath;
            QuestsAndStuffMod.debugLog("[QnS:UI] Loaded UI theme {}", themePath);
        }
    }

    static void applyLoadedState(UiThemeState nextState) {
        synchronized (LOCK) {
            installLoadedState(nextState);
        }
    }

    static void resetGlobalPaletteToDefaults() {
        synchronized (LOCK) {
            installLoadedState(UiThemeState.defaults());
            initialized = false;
            lastCheckMs = 0L;
            activeFileMtime = Long.MIN_VALUE;
            themeFileMtime = Long.MIN_VALUE;
            loadedThemePath = null;
        }
    }

    private static void installLoadedState(UiThemeState nextState) {
        state = nextState == null ? UiThemeState.defaults() : nextState;
        applyUiPalette(state);
    }

    private static void applyUiPalette(UiThemeState state) {
        Map<String, Integer> uiColors = state.uiColors;
        TabletColors.SURFACE_BASE = uiColors.getOrDefault(UI_SURFACE_BASE, TabletColors.DEFAULT_SURFACE_BASE);
        TabletColors.SURFACE_PANEL = uiColors.getOrDefault(UI_SURFACE_PANEL, TabletColors.DEFAULT_SURFACE_PANEL);
        TabletColors.SURFACE_PANEL_ALT = uiColors.getOrDefault(UI_SURFACE_PANEL_ALT, TabletColors.DEFAULT_SURFACE_PANEL_ALT);
        TabletColors.BORDER_BASE = uiColors.getOrDefault(UI_BORDER_BASE, TabletColors.DEFAULT_BORDER_BASE);
        TabletColors.BORDER_ACCENT = uiColors.getOrDefault(UI_BORDER_ACCENT, TabletColors.DEFAULT_BORDER_ACCENT);
        TabletColors.TEXT_PRIMARY = uiColors.getOrDefault(UI_TEXT_PRIMARY, TabletColors.DEFAULT_TEXT_PRIMARY);
        TabletColors.TEXT_SECONDARY = uiColors.getOrDefault(UI_TEXT_SECONDARY, TabletColors.DEFAULT_TEXT_SECONDARY);
        TabletColors.TEXT_MUTED = uiColors.getOrDefault(UI_TEXT_MUTED, TabletColors.DEFAULT_TEXT_MUTED);
        TabletColors.SUCCESS = uiColors.getOrDefault(UI_SUCCESS, TabletColors.DEFAULT_SUCCESS);
        TabletColors.WARNING = uiColors.getOrDefault(UI_WARNING, TabletColors.DEFAULT_WARNING);
        TabletColors.ERROR = uiColors.getOrDefault(UI_ERROR, TabletColors.DEFAULT_ERROR);
        TabletColors.INTERACTIVE = uiColors.getOrDefault(UI_INTERACTIVE, TabletColors.DEFAULT_INTERACTIVE);
        int scrollTrack = state.roleColors.getOrDefault(ROLE_ICON_SCROLL_TRACK, TabletColors.DEFAULT_SCROLL_TRACK);
        int scrollThumb = state.roleColors.getOrDefault(ROLE_ICON_SCROLL_THUMB, TabletColors.DEFAULT_SCROLL_THUMB);
        if (scrollTrack == TabletColors.DEFAULT_SCROLL_TRACK && TabletColors.BORDER_BASE != TabletColors.DEFAULT_BORDER_BASE) {
            scrollTrack = TabletColors.BORDER_BASE;
        }
        if (scrollThumb == TabletColors.DEFAULT_SCROLL_THUMB && TabletColors.INTERACTIVE != TabletColors.DEFAULT_INTERACTIVE) {
            scrollThumb = TabletColors.INTERACTIVE;
        }
        TabletColors.SCROLL_TRACK = scrollTrack;
        TabletColors.SCROLL_THUMB = scrollThumb;
    }

    public record ThemeInfo(String id, String label, int panel, int panelAlt, int accent, int success, int text) {
    }
}
