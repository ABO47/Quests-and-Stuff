package com.abo47.questsandstuff.client.tablet.theme;

import com.google.gson.JsonObject;

import java.util.Locale;
import java.util.Map;

final class UiThemeDefaults {
    private UiThemeDefaults() {
    }

    static JsonObject defaultThemeJson() {
        UiThemeState defaults = UiThemeState.defaults();
        JsonObject root = new JsonObject();
        root.addProperty("name", UiThemeFiles.DEFAULT_THEME_NAME);

        JsonObject colors = new JsonObject();
        for (Map.Entry<String, Integer> entry : defaults.uiColors.entrySet()) {
            colors.addProperty(entry.getKey(), toHex(entry.getValue()));
        }
        for (Map.Entry<String, Integer> entry : defaults.roleColors.entrySet()) {
            colors.addProperty(entry.getKey(), toHex(entry.getValue()));
        }
        root.add("colors", colors);

        JsonObject iconRoles = new JsonObject();
        for (Map.Entry<String, String> entry : defaults.iconRoles.entrySet()) {
            iconRoles.addProperty(entry.getKey(), entry.getValue());
        }
        root.add("icon_roles", iconRoles);
        return root;
    }

    static JsonObject themedJson(String name, String[][] colors) {
        JsonObject root = defaultThemeJson();
        root.addProperty("name", name);
        JsonObject colorJson = root.getAsJsonObject("colors");
        for (String[] color : colors) {
            if (color.length >= 2) {
                colorJson.addProperty(color[0], color[1]);
            }
        }
        return root;
    }

    private static String toHex(int color) {
        return String.format(Locale.ROOT, "#%08X", color);
    }
}
