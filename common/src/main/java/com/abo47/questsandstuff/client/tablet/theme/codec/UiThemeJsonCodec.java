package com.abo47.questsandstuff.client.tablet.theme.codec;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class UiThemeJsonCodec {
    private UiThemeJsonCodec() {
    }

    static UiThemeState loadUiThemeState(Path themePath) {
        UiThemeState defaults = UiThemeState.defaults();
        try {
            if (!Files.exists(themePath)) {
                return defaults;
            }
            JsonObject root = JsonParser.parseString(Files.readString(themePath, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, Integer> colors = new LinkedHashMap<>(defaults.roleColors);
            Map<String, String> iconRoles = new LinkedHashMap<>(defaults.iconRoles);
            Map<String, Integer> uiColors = new LinkedHashMap<>(defaults.uiColors);

            JsonObject colorsJson = objectOrNull(root.get("colors"));
            if (colorsJson != null) {
                for (Map.Entry<String, JsonElement> entry : colorsJson.entrySet()) {
                    String role = normalizeRole(entry.getKey());
                    int fallback = colors.getOrDefault(role, ModColors.DEFAULT_TEXT_SECONDARY);
                    colors.put(role, parseColor(entry.getValue(), fallback, themePath, "colors." + entry.getKey()));
                    String uiKey = normalizeUiColorKey(entry.getKey());
                    if (uiColors.containsKey(uiKey)) {
                        uiColors.put(uiKey, parseColor(entry.getValue(), uiColors.get(uiKey), themePath, "colors." + entry.getKey()));
                    }
                }
            }

            JsonObject iconRolesJson = objectOrNull(root.get("icon_roles"));
            if (iconRolesJson != null) {
                for (Map.Entry<String, JsonElement> entry : iconRolesJson.entrySet()) {
                    String icon = normalizeIconKey(entry.getKey());
                    if (icon.isBlank() || entry.getValue() == null || !entry.getValue().isJsonPrimitive()) {
                        continue;
                    }
                    iconRoles.put(icon, normalizeRole(entry.getValue().getAsString()));
                }
            }

            return new UiThemeState(colors, iconRoles, uiColors);
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed loading theme {}, using defaults", themePath, e);
            return defaults;
        }
    }

    static UiThemeManager.ThemeInfo readThemeInfo(Path themePath) {
        String id = UiThemeFiles.themeIdFromPath(themePath);
        UiThemeState defaults = UiThemeState.defaults();
        String label = prettifyThemeName(id);
        Map<String, Integer> colors = new LinkedHashMap<>(defaults.uiColors);
        try {
            JsonObject root = JsonParser.parseString(Files.readString(themePath, StandardCharsets.UTF_8)).getAsJsonObject();
            label = readString(root, "name", label);
            JsonObject colorJson = objectOrNull(root.get("colors"));
            if (colorJson != null) {
                for (Map.Entry<String, JsonElement> entry : colorJson.entrySet()) {
                    String key = normalizeUiColorKey(entry.getKey());
                    if (colors.containsKey(key)) {
                        colors.put(key, parseColor(entry.getValue(), colors.get(key), themePath, "colors." + entry.getKey()));
                    }
                }
            }
        } catch (Exception exception) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed reading theme info {}, using preview defaults", themePath, exception);
        }
        return new UiThemeManager.ThemeInfo(
                id,
                label.isBlank() ? prettifyThemeName(id) : label,
                colors.getOrDefault(UiThemeManager.UI_SURFACE_PANEL, ModColors.DEFAULT_SURFACE_PANEL),
                colors.getOrDefault(UiThemeManager.UI_SURFACE_PANEL_ALT, ModColors.DEFAULT_SURFACE_PANEL_ALT),
                colors.getOrDefault(UiThemeManager.UI_BORDER_ACCENT, ModColors.DEFAULT_BORDER_ACCENT),
                colors.getOrDefault(UiThemeManager.UI_SUCCESS, ModColors.DEFAULT_SUCCESS),
                colors.getOrDefault(UiThemeManager.UI_TEXT_PRIMARY, ModColors.DEFAULT_TEXT_PRIMARY)
        );
    }

    static String readString(JsonObject root, String key, String fallback) {
        if (root == null || !root.has(key) || !root.get(key).isJsonPrimitive()) {
            return fallback;
        }
        String value = root.get(key).getAsString();
        return value == null ? fallback : value.trim();
    }

    private static String prettifyThemeName(String id) {
        String clean = id == null ? "" : id.trim().replace('_', ' ').replace('-', ' ');
        if (clean.isBlank()) {
            return "Default";
        }
        StringBuilder out = new StringBuilder(clean.length());
        boolean nextUpper = true;
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            if (Character.isWhitespace(c)) {
                out.append(c);
                nextUpper = true;
            } else if (nextUpper) {
                out.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                out.append(c);
            }
        }
        return out.toString();
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

    private static String normalizeUiColorKey(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static int parseColor(JsonElement element, int fallback, Path themePath, String key) {
        if (element == null || element.isJsonNull()) {
            return fallback;
        }
        try {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                return element.getAsInt();
            }
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                String raw = element.getAsString().trim();
                if (raw.isEmpty()) {
                    return fallback;
                }
                if (raw.startsWith("#")) {
                    raw = raw.substring(1);
                } else if (raw.startsWith("0x") || raw.startsWith("0X")) {
                    raw = raw.substring(2);
                }
                if (raw.length() == 6) {
                    return (int) (0xFF000000L | Long.parseLong(raw, 16));
                }
                if (raw.length() == 8) {
                    return (int) Long.parseLong(raw, 16);
                }
                return Integer.parseInt(raw);
            }
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.LOGGER.warn(
                    "[QnS:UI] Invalid theme color theme={} key={} value={} fallback={}",
                    themePath,
                    key,
                    element,
                    String.format(Locale.ROOT, "0x%08X", fallback),
                    exception
            );
        }
        return fallback;
    }

    private static JsonObject objectOrNull(JsonElement element) {
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }
}
