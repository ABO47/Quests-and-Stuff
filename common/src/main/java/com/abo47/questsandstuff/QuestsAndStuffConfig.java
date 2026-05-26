package com.abo47.questsandstuff;

import com.abo47.questsandstuff.platform.Services;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class QuestsAndStuffConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static boolean loaded;
    private static boolean debugLogging;
    private static boolean uiAnimations = true;
    private static boolean contextMenuAnimations = true;
    private static boolean toolsMenuAnimations = true;
    private static boolean minimapAnimations = true;
    private static boolean questWindowAnimations = true;
    private static boolean popupWindowAnimations = true;
    private static boolean connectionAnimations = true;
    private static boolean chapterSwitchAnimations = true;
    private static boolean fullScreenMode;
    private static boolean minimap = true;
    private static boolean readOnlyCanvasFocus;
    private static boolean commandRewards = true;

    private QuestsAndStuffConfig() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path file = configFile();
        if (Files.isRegularFile(file)) {
            try {
                JsonElement parsed = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
                if (parsed.isJsonObject()) {
                    read(parsed.getAsJsonObject());
                }
            } catch (Exception e) {
                QuestsAndStuffMod.LOGGER.warn("Failed reading Quests and Stuff config {}, keeping defaults", file, e);
            }
        }
        save();
    }

    public static boolean debugLoggingEnabled() {
        load();
        return debugLogging;
    }

    public static void setDebugLoggingEnabled(boolean enabled) {
        load();
        if (debugLogging != enabled) {
            debugLogging = enabled;
            save();
        }
    }

    public static boolean uiAnimationsEnabled() {
        load();
        return uiAnimations;
    }

    public static void setUiAnimationsEnabled(boolean enabled) {
        load();
        if (uiAnimations != enabled) {
            uiAnimations = enabled;
            save();
        }
    }

    public static boolean contextMenuAnimationSettingEnabled() {
        load();
        return contextMenuAnimations;
    }

    public static boolean contextMenuAnimationsEnabled() {
        load();
        return uiAnimations && contextMenuAnimations;
    }

    public static void setContextMenuAnimationsEnabled(boolean enabled) {
        load();
        if (contextMenuAnimations != enabled) {
            contextMenuAnimations = enabled;
            save();
        }
    }

    public static boolean toolsMenuAnimationSettingEnabled() {
        load();
        return toolsMenuAnimations;
    }

    public static boolean toolsMenuAnimationsEnabled() {
        load();
        return uiAnimations && toolsMenuAnimations;
    }

    public static void setToolsMenuAnimationsEnabled(boolean enabled) {
        load();
        if (toolsMenuAnimations != enabled) {
            toolsMenuAnimations = enabled;
            save();
        }
    }

    public static boolean minimapAnimationSettingEnabled() {
        load();
        return minimapAnimations;
    }

    public static boolean minimapAnimationsEnabled() {
        load();
        return uiAnimations && minimapAnimations;
    }

    public static void setMinimapAnimationsEnabled(boolean enabled) {
        load();
        if (minimapAnimations != enabled) {
            minimapAnimations = enabled;
            save();
        }
    }

    public static boolean questWindowAnimationSettingEnabled() {
        load();
        return questWindowAnimations;
    }

    public static boolean questWindowAnimationsEnabled() {
        load();
        return uiAnimations && questWindowAnimations;
    }

    public static void setQuestWindowAnimationsEnabled(boolean enabled) {
        load();
        if (questWindowAnimations != enabled) {
            questWindowAnimations = enabled;
            save();
        }
    }

    public static boolean popupWindowAnimationSettingEnabled() {
        load();
        return popupWindowAnimations;
    }

    public static boolean popupWindowAnimationsEnabled() {
        load();
        return uiAnimations && popupWindowAnimations;
    }

    public static void setPopupWindowAnimationsEnabled(boolean enabled) {
        load();
        if (popupWindowAnimations != enabled) {
            popupWindowAnimations = enabled;
            save();
        }
    }

    public static boolean connectionAnimationSettingEnabled() {
        load();
        return connectionAnimations;
    }

    public static boolean connectionAnimationsEnabled() {
        load();
        return uiAnimations && connectionAnimations;
    }

    public static void setConnectionAnimationsEnabled(boolean enabled) {
        load();
        if (connectionAnimations != enabled) {
            connectionAnimations = enabled;
            save();
        }
    }

    public static boolean chapterSwitchAnimationSettingEnabled() {
        load();
        return chapterSwitchAnimations;
    }

    public static boolean chapterSwitchAnimationsEnabled() {
        load();
        return uiAnimations && chapterSwitchAnimations;
    }

    public static void setChapterSwitchAnimationsEnabled(boolean enabled) {
        load();
        if (chapterSwitchAnimations != enabled) {
            chapterSwitchAnimations = enabled;
            save();
        }
    }

    public static boolean fullScreenModeEnabled() {
        load();
        return fullScreenMode;
    }

    public static void setFullScreenModeEnabled(boolean enabled) {
        load();
        if (fullScreenMode != enabled) {
            fullScreenMode = enabled;
            save();
        }
    }

    public static boolean minimapEnabled() {
        load();
        return minimap;
    }

    public static void setMinimapEnabled(boolean enabled) {
        load();
        if (minimap != enabled) {
            minimap = enabled;
            save();
        }
    }

    public static boolean readOnlyCanvasFocusEnabled() {
        load();
        return readOnlyCanvasFocus;
    }

    public static void setReadOnlyCanvasFocusEnabled(boolean enabled) {
        load();
        if (readOnlyCanvasFocus != enabled) {
            readOnlyCanvasFocus = enabled;
            save();
        }
    }

    public static boolean commandRewardsEnabled() {
        load();
        return commandRewards;
    }

    public static void setCommandRewardsEnabled(boolean enabled) {
        load();
        if (commandRewards != enabled) {
            commandRewards = enabled;
            save();
        }
    }

    private static void read(JsonObject root) {
        JsonObject debug = object(root, "debug");
        debugLogging = bool(debug, "debugLogging", debugLogging);

        JsonObject animations = object(root, "animations");
        uiAnimations = bool(animations, "uiAnimations", uiAnimations);
        contextMenuAnimations = bool(animations, "contextMenuAnimations", contextMenuAnimations);
        toolsMenuAnimations = bool(animations, "toolsMenuAnimations", toolsMenuAnimations);
        minimapAnimations = bool(animations, "minimapAnimations", minimapAnimations);
        questWindowAnimations = bool(animations, "questWindowAnimations", questWindowAnimations);
        popupWindowAnimations = bool(animations, "popupWindowAnimations", popupWindowAnimations);
        connectionAnimations = bool(animations, "connectionAnimations", connectionAnimations);
        chapterSwitchAnimations = bool(animations, "chapterSwitchAnimations", chapterSwitchAnimations);

        JsonObject canvas = object(root, "canvas");
        fullScreenMode = bool(canvas, "fullScreenMode", fullScreenMode);
        minimap = bool(canvas, "minimap", minimap);
        readOnlyCanvasFocus = bool(canvas, "readOnlyCanvasFocus", readOnlyCanvasFocus);

        JsonObject security = object(root, "security");
        commandRewards = bool(security, "commandRewards", commandRewards);
    }

    private static synchronized void save() {
        JsonObject root = new JsonObject();
        JsonObject debug = new JsonObject();
        debug.addProperty("debugLogging", debugLogging);
        root.add("debug", debug);

        JsonObject animations = new JsonObject();
        animations.addProperty("uiAnimations", uiAnimations);
        animations.addProperty("contextMenuAnimations", contextMenuAnimations);
        animations.addProperty("toolsMenuAnimations", toolsMenuAnimations);
        animations.addProperty("minimapAnimations", minimapAnimations);
        animations.addProperty("questWindowAnimations", questWindowAnimations);
        animations.addProperty("popupWindowAnimations", popupWindowAnimations);
        animations.addProperty("connectionAnimations", connectionAnimations);
        animations.addProperty("chapterSwitchAnimations", chapterSwitchAnimations);
        root.add("animations", animations);

        JsonObject canvas = new JsonObject();
        canvas.addProperty("fullScreenMode", fullScreenMode);
        canvas.addProperty("minimap", minimap);
        canvas.addProperty("readOnlyCanvasFocus", readOnlyCanvasFocus);
        root.add("canvas", canvas);

        JsonObject security = new JsonObject();
        security.addProperty("commandRewards", commandRewards);
        root.add("security", security);

        Path file = configFile();
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed writing Quests and Stuff config {}", file, e);
        }
    }

    private static Path configFile() {
        return Services.platform().configDir().resolve(QuestsAndStuffMod.MODID).resolve("config.json");
    }

    private static JsonObject object(JsonObject root, String key) {
        if (root != null && root.has(key) && root.get(key).isJsonObject()) {
            return root.getAsJsonObject(key);
        }
        return null;
    }

    private static boolean bool(JsonObject root, String key, boolean fallback) {
        if (root != null && root.has(key) && root.get(key).isJsonPrimitive()) {
            try {
                return root.get(key).getAsBoolean();
            } catch (Exception ignored) {
                return fallback;
            }
        }
        return fallback;
    }
}
