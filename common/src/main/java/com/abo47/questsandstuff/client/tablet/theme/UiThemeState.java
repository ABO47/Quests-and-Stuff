package com.abo47.questsandstuff.client.tablet.theme;

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
        Map<String, String> iconRoles = new LinkedHashMap<>();
        iconRoles.put("close", UiThemeManager.ROLE_ICON_ERROR);
        iconRoles.put("editor", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("entity", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("align-center-horizontal", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("align-center-vertical", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("objects", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_add", UiThemeManager.ROLE_ICON_SUCCESS);
        iconRoles.put("context_rename", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_delete", UiThemeManager.ROLE_ICON_ERROR);
        iconRoles.put("context_icon", UiThemeManager.ROLE_ICON_INTERACTIVE);
        iconRoles.put("context_entity", UiThemeManager.ROLE_ICON_INTERACTIVE);
        iconRoles.put("kill_entity", UiThemeManager.ROLE_ICON_WARNING);
        iconRoles.put("manual_check", UiThemeManager.ROLE_ICON_SUCCESS);
        iconRoles.put("recipe", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("stat", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("xp", UiThemeManager.ROLE_ICON_SUCCESS);
        iconRoles.put("send-horizontal", UiThemeManager.ROLE_ICON_SUCCESS);
        iconRoles.put("biome", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("orbit", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("scissors", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_orbit", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_scissors", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_background", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_image", UiThemeManager.ROLE_ICON_SUCCESS);
        iconRoles.put("context_reset_zoom", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_reset_quest", UiThemeManager.ROLE_ICON_WARNING);
        iconRoles.put("reset_quest", UiThemeManager.ROLE_ICON_WARNING);
        iconRoles.put("context_variant", UiThemeManager.ROLE_ICON_INTERACTIVE);
        iconRoles.put("context_motion", UiThemeManager.ROLE_ICON_INTERACTIVE);
        iconRoles.put("context_style", UiThemeManager.ROLE_ICON_INTERACTIVE);
        iconRoles.put("context_move_up", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_move_down", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_open", UiThemeManager.ROLE_ICON_INTERACTIVE);
        iconRoles.put("context_eye", UiThemeManager.ROLE_ICON_INTERACTIVE);
        iconRoles.put("context_eye-off", UiThemeManager.ROLE_ICON_WARNING);
        iconRoles.put("context_audio-lines", UiThemeManager.ROLE_ICON_INTERACTIVE);
        iconRoles.put("context_connect", UiThemeManager.ROLE_ICON_SUCCESS);
        iconRoles.put("context_disconnect", UiThemeManager.ROLE_ICON_WARNING);
        iconRoles.put("context_center", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_grid", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_snap", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_inspector", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_minimap", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_select", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_drag", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_copy", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("context_paste", UiThemeManager.ROLE_ICON_SUCCESS);
        iconRoles.put("context_cut", UiThemeManager.ROLE_ICON_WARNING);
        iconRoles.put("context_properties", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("mode_items", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("mode_tags", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("picker_search", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("picker_scroll_track", UiThemeManager.ROLE_ICON_SCROLL_TRACK);
        iconRoles.put("picker_scroll_thumb", UiThemeManager.ROLE_ICON_SCROLL_THUMB);
        iconRoles.put("style_align_left", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("style_align_center", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("style_align_right", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("style_bold", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("style_italic", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("style_color", UiThemeManager.ROLE_ICON_INTERACTIVE);
        iconRoles.put("palette_add", UiThemeManager.ROLE_ICON_SUCCESS);
        iconRoles.put("palette_remove", UiThemeManager.ROLE_ICON_ERROR);
        iconRoles.put("context_separator", UiThemeManager.ROLE_ICON_MUTED);
        iconRoles.put("context_more", UiThemeManager.ROLE_ICON_DEFAULT);
        iconRoles.put("window_drag_handle", UiThemeManager.ROLE_ICON_MUTED);
        iconRoles.put("window_pin", UiThemeManager.ROLE_ICON_INTERACTIVE);
        iconRoles.put("themes", UiThemeManager.ROLE_ICON_INTERACTIVE);
        iconRoles.put("auto_claim", UiThemeManager.ROLE_ICON_SUCCESS);
        iconRoles.put("claim_all", UiThemeManager.ROLE_ICON_SUCCESS);
        return iconRoles;
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
