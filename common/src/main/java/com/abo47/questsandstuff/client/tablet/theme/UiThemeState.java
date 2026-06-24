package com.abo47.questsandstuff.client.tablet.theme;

import com.abo47.questsandstuff.client.tablet.icons.UiIconRegistry;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class UiThemeState {
    final Map<String, Integer> roleColors;
    final Map<String, String> iconRoles;
    final Map<String, Integer> uiColors;

    UiThemeState(Map<String, Integer> roleColors, Map<String, String> iconRoles, Map<String, Integer> uiColors) {
        this.roleColors = Map.copyOf(roleColors);
        this.iconRoles = Map.copyOf(iconRoles);
        this.uiColors = Map.copyOf(uiColors);
    }

    int colorForRole(String role) {
        String normalized = normalizeRole(role);
        return roleColors.getOrDefault(normalized, roleColors.getOrDefault(UiThemeManager.ROLE_ICON_DEFAULT, ModColors.DEFAULT_TEXT_SECONDARY));
    }

    int colorForIcon(String iconFileName) {
        String icon = normalizeIconKey(iconFileName);
        String role = iconRoles.getOrDefault(icon, inferRole(icon));
        return colorForRole(role);
    }

    static UiThemeState defaults() {
        return new UiThemeState(defaultRoleColors(), defaultIconRoles(), defaultUiColors());
    }

    private static Map<String, Integer> defaultRoleColors() {
        Map<String, Integer> colors = new LinkedHashMap<>();
        colors.put(UiThemeManager.ROLE_ICON_DEFAULT, ModColors.DEFAULT_TEXT_SECONDARY);
        colors.put(UiThemeManager.ROLE_ICON_INTERACTIVE, ModColors.DEFAULT_INTERACTIVE);
        colors.put(UiThemeManager.ROLE_ICON_SUCCESS, ModColors.DEFAULT_SUCCESS);
        colors.put(UiThemeManager.ROLE_ICON_WARNING, ModColors.DEFAULT_WARNING);
        colors.put(UiThemeManager.ROLE_ICON_ERROR, ModColors.DEFAULT_ERROR);
        colors.put(UiThemeManager.ROLE_ICON_MUTED, ModColors.DEFAULT_TEXT_MUTED);
        colors.put(UiThemeManager.ROLE_ICON_SCROLL_TRACK, ModColors.DEFAULT_BORDER_BASE);
        colors.put(UiThemeManager.ROLE_ICON_SCROLL_THUMB, ModColors.DEFAULT_INTERACTIVE);
        return colors;
    }

    private static Map<String, Integer> defaultUiColors() {
        Map<String, Integer> uiColors = new LinkedHashMap<>();
        uiColors.put(UiThemeManager.UI_SURFACE_BASE, ModColors.DEFAULT_SURFACE_BASE);
        uiColors.put(UiThemeManager.UI_SURFACE_PANEL, ModColors.DEFAULT_SURFACE_PANEL);
        uiColors.put(UiThemeManager.UI_SURFACE_PANEL_ALT, ModColors.DEFAULT_SURFACE_PANEL_ALT);
        uiColors.put(UiThemeManager.UI_BORDER_BASE, ModColors.DEFAULT_BORDER_BASE);
        uiColors.put(UiThemeManager.UI_BORDER_ACCENT, ModColors.DEFAULT_BORDER_ACCENT);
        uiColors.put(UiThemeManager.UI_TEXT_PRIMARY, ModColors.DEFAULT_TEXT_PRIMARY);
        uiColors.put(UiThemeManager.UI_TEXT_SECONDARY, ModColors.DEFAULT_TEXT_SECONDARY);
        uiColors.put(UiThemeManager.UI_TEXT_MUTED, ModColors.DEFAULT_TEXT_MUTED);
        uiColors.put(UiThemeManager.UI_SUCCESS, ModColors.DEFAULT_SUCCESS);
        uiColors.put(UiThemeManager.UI_WARNING, ModColors.DEFAULT_WARNING);
        uiColors.put(UiThemeManager.UI_ERROR, ModColors.DEFAULT_ERROR);
        uiColors.put(UiThemeManager.UI_INTERACTIVE, ModColors.DEFAULT_INTERACTIVE);
        return uiColors;
    }

    private static Map<String, String> defaultIconRoles() {
        return new LinkedHashMap<>(UiIconRegistry.defaultIconRoles());
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
