package com.abo47.questsandstuff;

import com.google.gson.JsonObject;

final class QuestsAndStuffConfigSections {
    private QuestsAndStuffConfigSections() {
    }

    static final class Debug {
        boolean debugLogging;

        void read(JsonObject root) {
            debugLogging = bool(root, "debugLogging", debugLogging);
        }

        JsonObject write() {
            JsonObject root = new JsonObject();
            root.addProperty("debugLogging", debugLogging);
            return root;
        }
    }

    static final class Animations {
        boolean ui = true;
        boolean contextMenu = true;
        boolean toolsMenu = true;
        boolean minimap = true;
        boolean questWindow = true;
        boolean popupWindow = true;
        boolean connection = true;
        boolean chapterSwitch = true;

        void read(JsonObject root) {
            ui = bool(root, "uiAnimations", ui);
            contextMenu = bool(root, "contextMenuAnimations", contextMenu);
            toolsMenu = bool(root, "toolsMenuAnimations", toolsMenu);
            minimap = bool(root, "minimapAnimations", minimap);
            questWindow = bool(root, "questWindowAnimations", questWindow);
            popupWindow = bool(root, "popupWindowAnimations", popupWindow);
            connection = bool(root, "connectionAnimations", connection);
            chapterSwitch = bool(root, "chapterSwitchAnimations", chapterSwitch);
        }

        JsonObject write() {
            JsonObject root = new JsonObject();
            root.addProperty("uiAnimations", ui);
            root.addProperty("contextMenuAnimations", contextMenu);
            root.addProperty("toolsMenuAnimations", toolsMenu);
            root.addProperty("minimapAnimations", minimap);
            root.addProperty("questWindowAnimations", questWindow);
            root.addProperty("popupWindowAnimations", popupWindow);
            root.addProperty("connectionAnimations", connection);
            root.addProperty("chapterSwitchAnimations", chapterSwitch);
            return root;
        }
    }

    static final class Canvas {
        boolean fullScreenMode;
        boolean minimap = true;
        boolean visualMinimap;
        boolean readOnlyFocus = true;
        boolean questEffectIcons;
        boolean miniNotifications;

        void read(JsonObject root) {
            fullScreenMode = bool(root, "fullScreenMode", fullScreenMode);
            minimap = bool(root, "minimap", minimap);
            visualMinimap = bool(root, "visualMinimap", visualMinimap);
            readOnlyFocus = bool(root, "readOnlyCanvasFocus", readOnlyFocus);
            questEffectIcons = bool(root, "questEffectIcons", questEffectIcons);
            miniNotifications = bool(root, "miniNotifications", miniNotifications);
        }

        JsonObject write() {
            JsonObject root = new JsonObject();
            root.addProperty("fullScreenMode", fullScreenMode);
            root.addProperty("minimap", minimap);
            root.addProperty("visualMinimap", visualMinimap);
            root.addProperty("readOnlyCanvasFocus", readOnlyFocus);
            root.addProperty("questEffectIcons", questEffectIcons);
            root.addProperty("miniNotifications", miniNotifications);
            return root;
        }
    }

    static final class Rewards {
        boolean autoClaim;

        void read(JsonObject root) {
            autoClaim = bool(root, "autoClaimRewards", autoClaim);
        }

        JsonObject write() {
            JsonObject root = new JsonObject();
            root.addProperty("autoClaimRewards", autoClaim);
            return root;
        }
    }

    static final class Hud {
        static final int DEFAULT_DURATION_MS = 2600;
        static final int MIN_DURATION_MS = 0;
        static final int MAX_DURATION_MS = 60000;

        boolean enabled = true;
        boolean sound = true;
        int durationMs = DEFAULT_DURATION_MS;

        void read(JsonObject root) {
            enabled = bool(root, "completionHud", enabled);
            sound = bool(root, "completionHudSound", sound);
            durationMs = normalizeDurationMs(intValue(root, "completionHudDurationMs", durationMs));
        }

        JsonObject write() {
            JsonObject root = new JsonObject();
            root.addProperty("completionHud", enabled);
            root.addProperty("completionHudSound", sound);
            root.addProperty("completionHudDurationMs", durationMs);
            return root;
        }

        static int normalizeDurationMs(int durationMs) {
            return Math.max(MIN_DURATION_MS, Math.min(MAX_DURATION_MS, durationMs));
        }
    }

    static final class Security {
        boolean commandRewards = true;

        void read(JsonObject root) {
            commandRewards = bool(root, "commandRewards", commandRewards);
        }

        JsonObject write() {
            JsonObject root = new JsonObject();
            root.addProperty("commandRewards", commandRewards);
            return root;
        }
    }

    static JsonObject object(JsonObject root, String key) {
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

    private static int intValue(JsonObject root, String key, int fallback) {
        if (root != null && root.has(key) && root.get(key).isJsonPrimitive()) {
            try {
                return root.get(key).getAsInt();
            } catch (Exception ignored) {
                return fallback;
            }
        }
        return fallback;
    }
}
