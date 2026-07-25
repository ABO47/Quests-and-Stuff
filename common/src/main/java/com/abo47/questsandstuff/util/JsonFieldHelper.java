package com.abo47.questsandstuff.util;

import com.google.gson.JsonObject;

public final class JsonFieldHelper {
    public static boolean bool(JsonObject root, String key, boolean fallback) {
        if (root != null && root.has(key) && root.get(key).isJsonPrimitive()) {
            try {
                return root.get(key).getAsBoolean();
            } catch (Exception ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    public static int readInt(JsonObject root, String key, int fallback) {
        if (root != null && root.has(key) && root.get(key).isJsonPrimitive()) {
            try {
                return root.get(key).getAsInt();
            } catch (Exception ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    public static float readFloat(JsonObject root, String key, float fallback) {
        if (root != null && root.has(key) && root.get(key).isJsonPrimitive()) {
            try {
                return root.get(key).getAsFloat();
            } catch (Exception ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    public static String string(JsonObject root, String key, String fallback) {
        if (root != null && root.has(key) && root.get(key).isJsonPrimitive()) {
            try {
                return root.get(key).getAsString();
            } catch (Exception ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private JsonFieldHelper() {
    }
}
