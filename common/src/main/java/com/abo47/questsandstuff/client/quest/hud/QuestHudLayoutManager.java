package com.abo47.questsandstuff.client.quest.hud;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.platform.Services;
import com.abo47.questsandstuff.quest.persistence.GsonProvider;
import com.abo47.questsandstuff.util.JsonFieldHelper;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static com.abo47.questsandstuff.util.MathUtils.clamp;

public final class QuestHudLayoutManager {
    private static final Gson GSON = GsonProvider.GSON;
    private static final int UNSET = Integer.MIN_VALUE;



    private static boolean loaded;
    private static int completionX = HudConstants.DEFAULT_COMPLETION_X;
    private static int completionY = HudConstants.DEFAULT_COMPLETION_Y;
    private static int pinnedX = HudConstants.DEFAULT_PINNED_X;
    private static int pinnedY = HudConstants.DEFAULT_PINNED_Y;
    private static int completionScale = HudConstants.DEFAULT_COMPLETION_SCALE;
    private static int completionHeightScale = HudConstants.DEFAULT_COMPLETION_HEIGHT_SCALE;
    private static int pinnedScale = HudConstants.DEFAULT_PINNED_SCALE;
    private static int pinnedHeightScale = HudConstants.DEFAULT_PINNED_HEIGHT_SCALE;
    private static String completionBackground = "";
    private static String pinnedBackground = "";
    private static int completionOpacity = HudConstants.DEFAULT_OPACITY;
    private static int pinnedOpacity = HudConstants.DEFAULT_OPACITY;
    private static boolean completionShowBorders = true;
    private static boolean pinnedShowBorders = true;
    private static boolean snapToGrid = true;

    private QuestHudLayoutManager() {
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
        if (element == Element.COMPLETION) {
            completionScale = Math.max(1, widthPercent);
            completionHeightScale = Math.max(1, heightPercent);
        } else {
            pinnedScale = Math.max(1, widthPercent);
            pinnedHeightScale = Math.max(1, heightPercent);
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

    public static synchronized boolean showBorders(Element element) {
        load();
        return element == Element.COMPLETION ? completionShowBorders : pinnedShowBorders;
    }

    public static synchronized void setShowBorders(Element element, boolean show) {
        load();
        if (element == Element.COMPLETION) {
            completionShowBorders = show;
        } else {
            pinnedShowBorders = show;
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
        completionX = HudConstants.DEFAULT_COMPLETION_X;
        completionY = HudConstants.DEFAULT_COMPLETION_Y;
        pinnedX = HudConstants.DEFAULT_PINNED_X;
        pinnedY = HudConstants.DEFAULT_PINNED_Y;
        completionScale = HudConstants.DEFAULT_COMPLETION_SCALE;
        completionHeightScale = HudConstants.DEFAULT_COMPLETION_HEIGHT_SCALE;
        pinnedScale = HudConstants.DEFAULT_PINNED_SCALE;
        pinnedHeightScale = HudConstants.DEFAULT_PINNED_HEIGHT_SCALE;
        completionBackground = "";
        pinnedBackground = "";
        completionOpacity = HudConstants.DEFAULT_OPACITY;
        pinnedOpacity = HudConstants.DEFAULT_OPACITY;
        completionShowBorders = true;
        pinnedShowBorders = true;
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
                completionShowBorders,
                pinnedShowBorders,
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
        completionShowBorders = snapshot.completionShowBorders();
        pinnedShowBorders = snapshot.pinnedShowBorders();
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
        completion.addProperty("show_borders", completionShowBorders);
        root.add("completion", completion);

        JsonObject pinned = new JsonObject();
        pinned.addProperty("x", pinnedX);
        pinned.addProperty("y", pinnedY);
        pinned.addProperty("scale", pinnedScale);
        pinned.addProperty("height_scale", pinnedHeightScale);
        pinned.addProperty("background", pinnedBackground);
        pinned.addProperty("opacity", pinnedOpacity);
        pinned.addProperty("show_borders", pinnedShowBorders);
        root.add("pinned", pinned);
        root.addProperty("snapToGrid", snapToGrid);

        Path file = configFile();
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(tmp, GSON.toJson(root), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.move(tmp, file, java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
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
            completionX = JsonFieldHelper.readInt(completion, "x", completionX);
            completionY = JsonFieldHelper.readInt(completion, "y", completionY);
            completionScale = Math.max(1, JsonFieldHelper.readInt(completion, "scale", completionScale));
            completionHeightScale = Math.max(1, JsonFieldHelper.readInt(completion, "height_scale", completionScale));
            completionBackground = JsonFieldHelper.string(completion, "background", completionBackground);
            completionOpacity = clamp(JsonFieldHelper.readInt(completion, "opacity", completionOpacity), 0, 100);
            completionShowBorders = JsonFieldHelper.bool(completion, "show_borders", completionShowBorders);
            JsonObject pinned = object(root, "pinned");
            pinnedX = JsonFieldHelper.readInt(pinned, "x", pinnedX);
            pinnedY = JsonFieldHelper.readInt(pinned, "y", pinnedY);
            pinnedScale = Math.max(1, JsonFieldHelper.readInt(pinned, "scale", pinnedScale));
            pinnedHeightScale = Math.max(1, JsonFieldHelper.readInt(pinned, "height_scale", pinnedScale));
            pinnedBackground = JsonFieldHelper.string(pinned, "background", pinnedBackground);
            pinnedOpacity = clamp(JsonFieldHelper.readInt(pinned, "opacity", pinnedOpacity), 0, 100);
            pinnedShowBorders = JsonFieldHelper.bool(pinned, "show_borders", pinnedShowBorders);
            snapToGrid = JsonFieldHelper.bool(root, "snapToGrid", snapToGrid);
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
            boolean completionShowBorders,
            boolean pinnedShowBorders,
            boolean snapToGrid
    ) {
    }
}
