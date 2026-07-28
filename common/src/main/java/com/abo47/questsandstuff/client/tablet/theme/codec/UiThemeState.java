package com.abo47.questsandstuff.client.tablet.theme.codec;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.abo47.questsandstuff.client.tablet.icons.IconRegistry;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

public final class UiThemeState {
    public final Map<String, Integer> roleColors;
    public final Map<String, String> iconRoles;
    public final Map<String, Integer> uiColors;

    public UiThemeState(Map<String, Integer> roleColors, Map<String, String> iconRoles, Map<String, Integer> uiColors) {
        this.roleColors = Map.copyOf(roleColors);
        this.iconRoles = Map.copyOf(iconRoles);
        this.uiColors = Map.copyOf(uiColors);
    }

    int colorForRole(String role) {
        String normalized = normalizeRole(role);
        return roleColors.getOrDefault(normalized, roleColors.getOrDefault(UiThemeManager.ROLE_ICON_DEFAULT, TabletColors.DEFAULT_TEXT_SECONDARY));
    }

    int colorForIcon(String iconFileName) {
        String icon = normalizeIconKey(iconFileName);
        String role = iconRoles.getOrDefault(icon, inferRole(icon));
        return colorForRole(role);
    }

    public static UiThemeState defaults() {
        return new UiThemeState(defaultRoleColors(), defaultIconRoles(), defaultUiColors());
    }

    private static Map<String, Integer> defaultRoleColors() {
        Map<String, Integer> colors = new LinkedHashMap<>();
        colors.put(UiThemeManager.ROLE_ICON_DEFAULT, TabletColors.DEFAULT_TEXT_SECONDARY);
        colors.put(UiThemeManager.ROLE_ICON_INTERACTIVE, TabletColors.DEFAULT_INTERACTIVE);
        colors.put(UiThemeManager.ROLE_ICON_SUCCESS, TabletColors.DEFAULT_SUCCESS);
        colors.put(UiThemeManager.ROLE_ICON_WARNING, TabletColors.DEFAULT_WARNING);
        colors.put(UiThemeManager.ROLE_ICON_ERROR, TabletColors.DEFAULT_ERROR);
        colors.put(UiThemeManager.ROLE_ICON_MUTED, TabletColors.DEFAULT_TEXT_MUTED);
        colors.put(UiThemeManager.ROLE_ICON_SCROLL_TRACK, TabletColors.DEFAULT_BORDER_BASE);
        colors.put(UiThemeManager.ROLE_ICON_SCROLL_THUMB, TabletColors.DEFAULT_INTERACTIVE);
        colors.put(UiThemeManager.ROLE_ICON_APP_QUESTS, TabletColors.DEFAULT_INTERACTIVE);
        colors.put(UiThemeManager.ROLE_ICON_APP_TEAMS, TabletColors.DEFAULT_SUCCESS);
        colors.put(UiThemeManager.ROLE_ICON_APP_CHUNKCLAIMER, TabletColors.DEFAULT_WARNING);
        colors.put(UiThemeManager.ROLE_ICON_APP_SETTINGS, TabletColors.DEFAULT_TEXT_MUTED);
        return colors;
    }

    private static Map<String, Integer> defaultUiColors() {
        Map<String, Integer> uiColors = new LinkedHashMap<>();
        uiColors.put(UiThemeManager.UI_SURFACE_BASE, TabletColors.DEFAULT_SURFACE_BASE);
        uiColors.put(UiThemeManager.UI_SURFACE_PANEL, TabletColors.DEFAULT_SURFACE_PANEL);
        uiColors.put(UiThemeManager.UI_SURFACE_PANEL_ALT, TabletColors.DEFAULT_SURFACE_PANEL_ALT);
        uiColors.put(UiThemeManager.UI_BORDER_BASE, TabletColors.DEFAULT_BORDER_BASE);
        uiColors.put(UiThemeManager.UI_BORDER_ACCENT, TabletColors.DEFAULT_BORDER_ACCENT);
        uiColors.put(UiThemeManager.UI_TEXT_PRIMARY, TabletColors.DEFAULT_TEXT_PRIMARY);
        uiColors.put(UiThemeManager.UI_TEXT_SECONDARY, TabletColors.DEFAULT_TEXT_SECONDARY);
        uiColors.put(UiThemeManager.UI_TEXT_MUTED, TabletColors.DEFAULT_TEXT_MUTED);
        uiColors.put(UiThemeManager.UI_SUCCESS, TabletColors.DEFAULT_SUCCESS);
        uiColors.put(UiThemeManager.UI_WARNING, TabletColors.DEFAULT_WARNING);
        uiColors.put(UiThemeManager.UI_ERROR, TabletColors.DEFAULT_ERROR);
        uiColors.put(UiThemeManager.UI_INTERACTIVE, TabletColors.DEFAULT_INTERACTIVE);
        uiColors.put(UiThemeManager.UI_GLOW, TabletColors.DEFAULT_GLOW);
        uiColors.put(UiThemeManager.UI_SELECTION, TabletColors.DEFAULT_SELECTION);
        uiColors.put(UiThemeManager.UI_SCROLL_TRACK, TabletColors.DEFAULT_SCROLL_TRACK);
        uiColors.put(UiThemeManager.UI_SCROLL_THUMB, TabletColors.DEFAULT_SCROLL_THUMB);
        uiColors.put(UiThemeManager.UI_APP_QUESTS, TabletColors.DEFAULT_INTERACTIVE);
        uiColors.put(UiThemeManager.UI_APP_TEAMS, TabletColors.DEFAULT_SUCCESS);
        uiColors.put(UiThemeManager.UI_APP_CHUNKCLAIMER, TabletColors.DEFAULT_WARNING);
        uiColors.put(UiThemeManager.UI_APP_SETTINGS, TabletColors.DEFAULT_TEXT_MUTED);
        return uiColors;
    }

    private static Map<String, String> defaultIconRoles() {
        return new LinkedHashMap<>(IconRegistry.defaultIconRoles());
    }

    private static String normalizeRole(String role) {
        return role == null ? UiThemeManager.ROLE_ICON_DEFAULT : role.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeIconKey(String iconFileName) {
        String clean = iconFileName == null ? "" : iconFileName.trim().toLowerCase(Locale.ROOT);
        if (clean.endsWith(".png")) {
            clean = clean.substring(0, clean.length() - 4);
        }
        return clean;
    }

    private static String inferRole(String iconKey) {
        String v = normalizeIconKey(iconKey);
        if (v.isBlank()) {
            return UiThemeManager.ROLE_ICON_DEFAULT;
        }
        if (v.contains("disconnect")) {
            return UiThemeManager.ROLE_ICON_WARNING;
        }
        if (v.contains("delete") || v.contains("remove") || v.contains("close") || v.contains("cut")) {
            return UiThemeManager.ROLE_ICON_ERROR;
        }
        if (v.contains("add") || v.contains("paste") || v.contains("connect")) {
            return UiThemeManager.ROLE_ICON_SUCCESS;
        }
        if (v.contains("scroll_track")) {
            return UiThemeManager.ROLE_ICON_SCROLL_TRACK;
        }
        if (v.contains("scroll_thumb")) {
            return UiThemeManager.ROLE_ICON_SCROLL_THUMB;
        }
        if (v.contains("drag_handle") || v.contains("separator")) {
            return UiThemeManager.ROLE_ICON_MUTED;
        }
        if (v.contains("style_color")) {
            return UiThemeManager.ROLE_ICON_INTERACTIVE;
        }
        return UiThemeManager.ROLE_ICON_DEFAULT;
    }
}
