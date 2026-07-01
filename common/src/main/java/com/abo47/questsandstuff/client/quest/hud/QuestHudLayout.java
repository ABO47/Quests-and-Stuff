package com.abo47.questsandstuff.client.quest.hud;

import com.abo47.questsandstuff.QuestsAndStuffMod;
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

public final class QuestHudLayout {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int UNSET = Integer.MIN_VALUE;
    private static final int MIN_SCALE = 60;
    private static final int MAX_SCALE = 2000;
    private static final int DEFAULT_OPACITY = 100;
    private static final int DEFAULT_COMPLETION_X = 225;
    private static final int DEFAULT_COMPLETION_Y = 241;
    private static final int DEFAULT_PINNED_X = 1;
    private static final int DEFAULT_PINNED_Y = 1;
    private static final int DEFAULT_COMPLETION_SCALE = 149;
    private static final int DEFAULT_COMPLETION_HEIGHT_SCALE = 147;
    private static final int DEFAULT_PINNED_SCALE = 104;
    private static final int DEFAULT_PINNED_HEIGHT_SCALE = 115;

    private static boolean loaded;
    private static int completionX = DEFAULT_COMPLETION_X;
    private static int completionY = DEFAULT_COMPLETION_Y;
    private static int pinnedX = DEFAULT_PINNED_X;
    private static int pinnedY = DEFAULT_PINNED_Y;
    private static int completionScale = DEFAULT_COMPLETION_SCALE;
    private static int completionHeightScale = DEFAULT_COMPLETION_HEIGHT_SCALE;
    private static int pinnedScale = DEFAULT_PINNED_SCALE;
    private static int pinnedHeightScale = DEFAULT_PINNED_HEIGHT_SCALE;
    private static String completionBackground = "";
    private static String pinnedBackground = "";
    private static int completionOpacity = DEFAULT_OPACITY;
    private static int pinnedOpacity = DEFAULT_OPACITY;
    private static boolean snapToGrid = true;

    private QuestHudLayout() {
    }

    public static synchronized HudBox completionBox(int screenWidth, int screenHeight) {
        return completionBox(screenWidth, screenHeight, QuestCompletionNotificationOverlay.width(), QuestCompletionNotificationOverlay.height());
    }

    public static synchronized HudBox completionBox(int screenWidth, int screenHeight, int width, int height) {
        load();
        return box(Element.COMPLETION, screenWidth, screenHeight, width, height);
    }

    public static synchronized HudBox pinnedBox(int screenWidth, int screenHeight, int width, int height) {
        load();
        return box(Element.PINNED, screenWidth, screenHeight, width, height);
    }

    public static synchronized void setPosition(Element element, int x, int y, int screenWidth, int screenHeight, int width, int height) {
        load();
        int clampedX = clamp(x, 0, Math.max(0, screenWidth - width));
        int clampedY = clamp(y, 0, Math.max(0, screenHeight - height));
        if (element == Element.COMPLETION) {
            completionX = clampedX;
            completionY = clampedY;
        } else {
            pinnedX = clampedX;
            pinnedY = clampedY;
        }
    }

    public static synchronized int scalePercent(Element element) {
        load();
        return element == Element.COMPLETION ? completionScale : pinnedScale;
    }

    public static synchronized int heightScalePercent(Element element) {
        load();
        return element == Element.COMPLETION ? completionHeightScale : pinnedHeightScale;
    }

    public static synchronized float scale(Element element) {
        return scalePercent(element) / 100.0f;
    }

    public static synchronized float heightScale(Element element) {
        return heightScalePercent(element) / 100.0f;
    }

    public static synchronized void setScalePercent(Element element, int percent) {
        setSizePercent(element, percent, percent);
    }

    public static synchronized void setSizePercent(Element element, int widthPercent, int heightPercent) {
        load();
        int widthScale = clamp(widthPercent, MIN_SCALE, MAX_SCALE);
        int heightScale = clamp(heightPercent, MIN_SCALE, MAX_SCALE);
        if (element == Element.COMPLETION) {
            completionScale = widthScale;
            completionHeightScale = heightScale;
        } else {
            pinnedScale = widthScale;
            pinnedHeightScale = heightScale;
        }
    }

    public static synchronized int opacityPercent(Element element) {
        load();
        return element == Element.COMPLETION ? completionOpacity : pinnedOpacity;
    }

    public static synchronized void setOpacityPercent(Element element, int percent) {
        load();
        int clamped = clamp(percent, 0, 100);
        if (element == Element.COMPLETION) {
            completionOpacity = clamped;
        } else {
            pinnedOpacity = clamped;
        }
    }

    public static synchronized String background(Element element) {
        load();
        return element == Element.COMPLETION ? completionBackground : pinnedBackground;
    }

    public static synchronized void setBackground(Element element, String background) {
        load();
        String value = background == null ? "" : background.trim();
        if (element == Element.COMPLETION) {
            completionBackground = value;
        } else {
            pinnedBackground = value;
        }
    }

    public static synchronized boolean snapToGrid() {
        load();
        return snapToGrid;
    }

    public static synchronized void setSnapToGrid(boolean enabled) {
        load();
        snapToGrid = enabled;
    }

    public static synchronized int scaledSize(Element element, int baseSize) {
        return Math.max(1, Math.round(baseSize * scale(element)));
    }

    public static synchronized int scaledHeight(Element element, int baseSize) {
        return Math.max(1, Math.round(baseSize * heightScale(element)));
    }

    public static synchronized void resetToDefaults() {
        load();
        completionX = DEFAULT_COMPLETION_X;
        completionY = DEFAULT_COMPLETION_Y;
        pinnedX = DEFAULT_PINNED_X;
        pinnedY = DEFAULT_PINNED_Y;
        completionScale = DEFAULT_COMPLETION_SCALE;
        completionHeightScale = DEFAULT_COMPLETION_HEIGHT_SCALE;
        pinnedScale = DEFAULT_PINNED_SCALE;
        pinnedHeightScale = DEFAULT_PINNED_HEIGHT_SCALE;
        completionBackground = "";
        pinnedBackground = "";
        completionOpacity = DEFAULT_OPACITY;
        pinnedOpacity = DEFAULT_OPACITY;
        snapToGrid = true;
    }

    public static synchronized Snapshot snapshot() {
        load();
        return new Snapshot(
                completionX,
                completionY,
                pinnedX,
                pinnedY,
                completionScale,
                completionHeightScale,
                pinnedScale,
                pinnedHeightScale,
                completionBackground,
                pinnedBackground,
                completionOpacity,
                pinnedOpacity,
                snapToGrid
        );
    }

    public static synchronized void restore(Snapshot snapshot) {
        if (snapshot == null) {
            resetToDefaults();
            return;
        }
        loaded = true;
        completionX = snapshot.completionX();
        completionY = snapshot.completionY();
        pinnedX = snapshot.pinnedX();
        pinnedY = snapshot.pinnedY();
        completionScale = snapshot.completionScale();
        completionHeightScale = snapshot.completionHeightScale();
        pinnedScale = snapshot.pinnedScale();
        pinnedHeightScale = snapshot.pinnedHeightScale();
        completionBackground = snapshot.completionBackground();
        pinnedBackground = snapshot.pinnedBackground();
        completionOpacity = snapshot.completionOpacity();
        pinnedOpacity = snapshot.pinnedOpacity();
        snapToGrid = snapshot.snapToGrid();
    }

    public static synchronized void save() {
        load();
        JsonObject root = new JsonObject();
        JsonObject completion = new JsonObject();
        completion.addProperty("x", completionX);
        completion.addProperty("y", completionY);
        completion.addProperty("scale", completionScale);
        completion.addProperty("height_scale", completionHeightScale);
        completion.addProperty("background", completionBackground);
        completion.addProperty("opacity", completionOpacity);
        root.add("completion", completion);

        JsonObject pinned = new JsonObject();
        pinned.addProperty("x", pinnedX);
        pinned.addProperty("y", pinnedY);
        pinned.addProperty("scale", pinnedScale);
        pinned.addProperty("height_scale", pinnedHeightScale);
        pinned.addProperty("background", pinnedBackground);
        pinned.addProperty("opacity", pinnedOpacity);
        root.add("pinned", pinned);
        root.addProperty("snapToGrid", snapToGrid);

        Path file = configFile();
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed writing Quests and Stuff HUD layout {}", file, e);
        }
    }

    private static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path file = configFile();
        if (!Files.isRegularFile(file)) {
            return;
        }
        try {
            JsonElement parsed = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
            if (!parsed.isJsonObject()) {
                return;
            }
            JsonObject root = parsed.getAsJsonObject();
            JsonObject completion = object(root, "completion");
            completionX = intValue(completion, "x", completionX);
            completionY = intValue(completion, "y", completionY);
            completionScale = clamp(intValue(completion, "scale", completionScale), MIN_SCALE, MAX_SCALE);
            completionHeightScale = clamp(intValue(completion, "height_scale", completionScale), MIN_SCALE, MAX_SCALE);
            completionBackground = stringValue(completion, "background", completionBackground);
            completionOpacity = clamp(intValue(completion, "opacity", completionOpacity), 0, 100);
            JsonObject pinned = object(root, "pinned");
            pinnedX = intValue(pinned, "x", pinnedX);
            pinnedY = intValue(pinned, "y", pinnedY);
            pinnedScale = clamp(intValue(pinned, "scale", pinnedScale), MIN_SCALE, MAX_SCALE);
            pinnedHeightScale = clamp(intValue(pinned, "height_scale", pinnedScale), MIN_SCALE, MAX_SCALE);
            pinnedBackground = stringValue(pinned, "background", pinnedBackground);
            pinnedOpacity = clamp(intValue(pinned, "opacity", pinnedOpacity), 0, 100);
            snapToGrid = boolValue(root, "snapToGrid", snapToGrid);
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed reading Quests and Stuff HUD layout {}, keeping defaults", file, e);
        }
    }

    private static HudBox box(Element element, int screenWidth, int screenHeight, int width, int height) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        int x = element == Element.COMPLETION ? completionX : pinnedX;
        int y = element == Element.COMPLETION ? completionY : pinnedY;
        if (x == UNSET || y == UNSET) {
            x = defaultX(element, screenWidth, w);
            y = defaultY(element, screenHeight, h);
        }
        return new HudBox(clamp(x, 0, Math.max(0, screenWidth - w)), clamp(y, 0, Math.max(0, screenHeight - h)), w, h);
    }

    private static int defaultX(Element element, int screenWidth, int width) {
        if (element == Element.COMPLETION) {
            return screenWidth / 2 - width / 2;
        }
        return 12;
    }

    private static int defaultY(Element element, int screenHeight, int height) {
        if (element == Element.COMPLETION) {
            return Math.max(4, screenHeight - 76);
        }
        return screenHeight / 2 - height / 2;
    }

    private static Path configFile() {
        return Services.platform().configDir().resolve(QuestsAndStuffMod.MODID).resolve("hud_layout.json");
    }

    private static JsonObject object(JsonObject root, String key) {
        if (root != null && root.has(key) && root.get(key).isJsonObject()) {
            return root.getAsJsonObject(key);
        }
        return null;
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

    private static String stringValue(JsonObject root, String key, String fallback) {
        if (root != null && root.has(key) && root.get(key).isJsonPrimitive()) {
            try {
                return root.get(key).getAsString();
            } catch (Exception ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private static boolean boolValue(JsonObject root, String key, boolean fallback) {
        if (root != null && root.has(key) && root.get(key).isJsonPrimitive()) {
            try {
                return root.get(key).getAsBoolean();
            } catch (Exception ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public enum Element {
        COMPLETION,
        PINNED
    }

    public record HudBox(int x, int y, int width, int height) {
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }

    public record Snapshot(
            int completionX,
            int completionY,
            int pinnedX,
            int pinnedY,
            int completionScale,
            int completionHeightScale,
            int pinnedScale,
            int pinnedHeightScale,
            String completionBackground,
            String pinnedBackground,
            int completionOpacity,
            int pinnedOpacity,
            boolean snapToGrid
    ) {
    }
}
