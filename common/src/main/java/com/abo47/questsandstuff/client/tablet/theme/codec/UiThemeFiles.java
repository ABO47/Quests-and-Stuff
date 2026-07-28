package com.abo47.questsandstuff.client.tablet.theme.codec;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeCatalog;
import com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeDefaults;
import com.abo47.questsandstuff.platform.Services;
import com.abo47.questsandstuff.util.naming.SafeNames;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class UiThemeFiles {
    public static final String DEFAULT_THEME_NAME = "default";
    static final Path THEMES_DIR = Services.platform().configDir().resolve(QuestsAndStuffMod.MODID).resolve("themes");
    static final Path ACTIVE_THEME_FILE = THEMES_DIR.resolve("active_theme.json");

    private UiThemeFiles() {
    }

    public static void bootstrapIfMissing(Gson gson) {
        try {
            Files.createDirectories(THEMES_DIR);
            writeThemeIfMissing(gson, DEFAULT_THEME_NAME, UiThemeDefaults.defaultThemeJson());
            for (UiThemeCatalog.BuiltInTheme theme : UiThemeCatalog.builtIns()) {
                writeThemeIfMissing(gson, theme.id(), UiThemeDefaults.themedJson(theme.name(), theme.colors()));
                upgradeBuiltInTheme(gson, theme);
            }
            if (!Files.exists(ACTIVE_THEME_FILE)) {
                JsonObject active = new JsonObject();
                active.addProperty("theme", DEFAULT_THEME_NAME);
                writeString(ACTIVE_THEME_FILE, gson.toJson(active));
            }
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed preparing theme files in {}", THEMES_DIR, e);
        }
    }

    static List<Path> themeFiles() throws Exception {
        try (var stream = Files.list(THEMES_DIR)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                    .filter(path -> !ACTIVE_THEME_FILE.getFileName().equals(path.getFileName()))
                    .toList();
        }
    }

    static Path resolveActiveThemePath() {
        String themeName = DEFAULT_THEME_NAME;
        try {
            if (Files.exists(ACTIVE_THEME_FILE)) {
                JsonObject root = JsonParser.parseString(Files.readString(ACTIVE_THEME_FILE, StandardCharsets.UTF_8)).getAsJsonObject();
                String configured = UiThemeJsonCodec.readString(root, "theme", UiThemeJsonCodec.readString(root, "file", DEFAULT_THEME_NAME));
                if (!configured.isBlank()) {
                    themeName = configured;
                }
            }
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed reading active theme file, falling back to default", e);
        }
        String fileName = themeName.endsWith(".json") ? themeName : themeName + ".json";
        Path candidate = THEMES_DIR.resolve(fileName).normalize();
        if (!candidate.startsWith(THEMES_DIR.normalize())) {
            return THEMES_DIR.resolve(DEFAULT_THEME_NAME + ".json");
        }
        if (!Files.exists(candidate)) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Theme {} not found, falling back to default", candidate.getFileName());
            return THEMES_DIR.resolve(DEFAULT_THEME_NAME + ".json");
        }
        return candidate;
    }

    static Path themePath(String normalizedThemeName) {
        return THEMES_DIR.resolve(normalizedThemeName + ".json").normalize();
    }

    static boolean isThemePath(Path themePath) {
        return themePath.startsWith(THEMES_DIR.normalize()) && Files.exists(themePath);
    }

    static String themeIdFromPath(Path path) {
        if (path == null || path.getFileName() == null) {
            return DEFAULT_THEME_NAME;
        }
        return normalizeThemeName(path.getFileName().toString());
    }

    static String normalizeThemeName(String themeName) {
        String clean = themeName == null ? "" : themeName.trim().toLowerCase(Locale.ROOT);
        if (clean.endsWith(".json")) {
            clean = clean.substring(0, clean.length() - 5);
        }
        clean = clean.replace('\\', '/');
        int slash = clean.lastIndexOf('/');
        if (slash >= 0) {
            clean = clean.substring(slash + 1);
        }
        return SafeNames.fileStem(clean, DEFAULT_THEME_NAME);
    }

    static long safeMtime(Path file) {
        try {
            return Files.exists(file) ? Files.getLastModifiedTime(file).toMillis() : Long.MIN_VALUE;
        } catch (Exception exception) {
            QuestsAndStuffMod.debugLog(
                    "[QnS:UI] Failed reading theme mtime file={} diagnostic={}",
                    file,
                    exception.toString()
            );
            return Long.MIN_VALUE;
        }
    }

    static void writeString(Path file, String content) throws Exception {
        Files.writeString(file, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void writeThemeIfMissing(Gson gson, String id, JsonObject theme) throws Exception {
        Path file = THEMES_DIR.resolve(id + ".json");
        if (!Files.exists(file) || "default".equals(id)) {
            writeString(file, gson.toJson(theme));
        }
    }

    private static void upgradeBuiltInTheme(Gson gson, UiThemeCatalog.BuiltInTheme theme) {
        Path file = THEMES_DIR.resolve(theme.id() + ".json");
        if (!Files.exists(file)) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!root.has("colors") || !root.get("colors").isJsonObject()) {
                return;
            }
            JsonObject colors = root.getAsJsonObject("colors");
            boolean changed = false;
            for (String[] color : theme.colors()) {
                if (color.length >= 2) {
                    String existing = colors.has(color[0]) ? colors.get(color[0]).getAsString() : null;
                    if (!color[1].equals(existing)) {
                        colors.addProperty(color[0], color[1]);
                        changed = true;
                    }
                }
            }
            if (ensureDerivedColor(colors, UiThemeManager.UI_SCROLL_TRACK, UiThemeManager.UI_BORDER_BASE)) changed = true;
            if (ensureDerivedColor(colors, UiThemeManager.UI_SCROLL_THUMB, UiThemeManager.UI_INTERACTIVE)) changed = true;
            if (ensureDerivedColor(colors, UiThemeManager.UI_APP_QUESTS, UiThemeManager.UI_INTERACTIVE)) changed = true;
            if (ensureDerivedColor(colors, UiThemeManager.UI_APP_TEAMS, UiThemeManager.UI_SUCCESS)) changed = true;
            if (ensureDerivedColor(colors, UiThemeManager.UI_APP_CHUNKCLAIMER, UiThemeManager.UI_WARNING)) changed = true;
            if (ensureDerivedColor(colors, UiThemeManager.UI_APP_SETTINGS, UiThemeManager.UI_TEXT_MUTED)) changed = true;
            if (changed) {
                writeString(file, gson.toJson(root));
            }
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed upgrading built-in theme {}", theme.id(), e);
        }
    }

    private static boolean ensureDerivedColor(JsonObject colors, String target, String source) {
        if (colors.has(target)) {
            return false;
        }
        if (!colors.has(source)) {
            return false;
        }
        colors.addProperty(target, colors.get(source).getAsString());
        return true;
    }
}
