package com.abo47.questsandstuff.client.tablet.theme.tokens;

import java.util.Locale;
import java.util.Map;

import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeFiles;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeState;

import com.google.gson.JsonObject;

public final class UiThemeDefaults {
    private UiThemeDefaults() {
    }

    public static JsonObject defaultThemeJson() {
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

    public static JsonObject themedJson(String name, String[][] colors) {
        JsonObject root = defaultThemeJson();
        root.addProperty("name", name);
        JsonObject colorJson = root.getAsJsonObject("colors");
        boolean customScrollTrack = false;
        boolean customScrollThumb = false;
        boolean customIconScrollTrack = false;
        boolean customIconScrollThumb = false;
        boolean customAppQuests = false;
        boolean customAppTeams = false;
        boolean customAppChunkclaimer = false;
        boolean customAppSettings = false;
        for (String[] color : colors) {
            if (color.length >= 2) {
                colorJson.addProperty(color[0], color[1]);
                customScrollTrack |= UiThemeManager.UI_SCROLL_TRACK.equals(color[0]);
                customScrollThumb |= UiThemeManager.UI_SCROLL_THUMB.equals(color[0]);
                customIconScrollTrack |= UiThemeManager.ROLE_ICON_SCROLL_TRACK.equals(color[0]);
                customIconScrollThumb |= UiThemeManager.ROLE_ICON_SCROLL_THUMB.equals(color[0]);
                customAppQuests |= UiThemeManager.UI_APP_QUESTS.equals(color[0]);
                customAppTeams |= UiThemeManager.UI_APP_TEAMS.equals(color[0]);
                customAppChunkclaimer |= UiThemeManager.UI_APP_CHUNKCLAIMER.equals(color[0]);
                customAppSettings |= UiThemeManager.UI_APP_SETTINGS.equals(color[0]);
            }
        }
        if (!customScrollTrack && colorJson.has(UiThemeManager.UI_BORDER_BASE)) {
            colorJson.addProperty(UiThemeManager.UI_SCROLL_TRACK, colorJson.get(UiThemeManager.UI_BORDER_BASE).getAsString());
        }
        if (!customScrollThumb && colorJson.has(UiThemeManager.UI_BORDER_ACCENT)) {
            colorJson.addProperty(UiThemeManager.UI_SCROLL_THUMB, colorJson.get(UiThemeManager.UI_BORDER_ACCENT).getAsString());
        }
        if (!customIconScrollTrack && colorJson.has(UiThemeManager.UI_BORDER_BASE)) {
            colorJson.addProperty(UiThemeManager.ROLE_ICON_SCROLL_TRACK, colorJson.get(UiThemeManager.UI_BORDER_BASE).getAsString());
        }
        if (!customIconScrollThumb && colorJson.has(UiThemeManager.UI_INTERACTIVE)) {
            colorJson.addProperty(UiThemeManager.ROLE_ICON_SCROLL_THUMB, colorJson.get(UiThemeManager.UI_INTERACTIVE).getAsString());
        }
        if (!customAppQuests && colorJson.has(UiThemeManager.UI_INTERACTIVE)) {
            colorJson.addProperty(UiThemeManager.UI_APP_QUESTS, colorJson.get(UiThemeManager.UI_INTERACTIVE).getAsString());
        }
        if (!customAppTeams && colorJson.has(UiThemeManager.UI_SUCCESS)) {
            colorJson.addProperty(UiThemeManager.UI_APP_TEAMS, colorJson.get(UiThemeManager.UI_SUCCESS).getAsString());
        }
        if (!customAppChunkclaimer && colorJson.has(UiThemeManager.UI_WARNING)) {
            colorJson.addProperty(UiThemeManager.UI_APP_CHUNKCLAIMER, colorJson.get(UiThemeManager.UI_WARNING).getAsString());
        }
        if (!customAppSettings && colorJson.has(UiThemeManager.UI_TEXT_MUTED)) {
            colorJson.addProperty(UiThemeManager.UI_APP_SETTINGS, colorJson.get(UiThemeManager.UI_TEXT_MUTED).getAsString());
        }
        return root;
    }

    private static String toHex(int color) {
        return String.format(Locale.ROOT, "#%08X", color);
    }
}
