package com.abo47.questsandstuff.client.quest.hud;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;
import java.util.Map;

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

    private record ElementConfig(int x, int y, int scale, int heightScale, String background, int opacity, boolean showBorders) {
        ElementConfig {
            background = background == null ? "" : background.trim();
        }
    }

    private static boolean loaded;
    private static final EnumMap<Element, ElementConfig> configs = new EnumMap<>(Element.class);
    private static boolean snapToGrid = true;

    static {
        resetConfigs();
    }

    private QuestHudLayoutManager() {
    }

    private static void resetConfigs() {
        configs.put(Element.COMPLETION, new ElementConfig(
                HudConstants.DEFAULT_COMPLETION_X,
                HudConstants.DEFAULT_COMPLETION_Y,
                HudConstants.DEFAULT_COMPLETION_SCALE,
                HudConstants.DEFAULT_COMPLETION_HEIGHT_SCALE,
                "",
                HudConstants.DEFAULT_OPACITY,
                true
        ));
        configs.put(Element.PINNED, new ElementConfig(
                HudConstants.DEFAULT_PINNED_X,
                HudConstants.DEFAULT_PINNED_Y,
                HudConstants.DEFAULT_PINNED_SCALE,
                HudConstants.DEFAULT_PINNED_HEIGHT_SCALE,
                "",
                HudConstants.DEFAULT_OPACITY,
                true
        ));
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
        ElementConfig cfg = configs.get(element);
        configs.put(element, new ElementConfig(clampedX, clampedY, cfg.scale(), cfg.heightScale(), cfg.background(), cfg.opacity(), cfg.showBorders()));
    }

    public static synchronized int scalePercent(Element element) {
        load();
        return configs.get(element).scale();
    }

    public static synchronized int heightScalePercent(Element element) {
        load();
        return configs.get(element).heightScale();
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
        ElementConfig cfg = configs.get(element);
        configs.put(element, new ElementConfig(cfg.x(), cfg.y(), Math.max(1, widthPercent), Math.max(1, heightPercent), cfg.background(), cfg.opacity(), cfg.showBorders()));
    }

    public static synchronized int opacityPercent(Element element) {
        load();
        return configs.get(element).opacity();
    }

    public static synchronized void setOpacityPercent(Element element, int percent) {
        load();
        ElementConfig cfg = configs.get(element);
        configs.put(element, new ElementConfig(cfg.x(), cfg.y(), cfg.scale(), cfg.heightScale(), cfg.background(), clamp(percent, 0, 100), cfg.showBorders()));
    }

    public static synchronized String background(Element element) {
        load();
        return configs.get(element).background();
    }

    public static synchronized void setBackground(Element element, String background) {
        load();
        ElementConfig cfg = configs.get(element);
        configs.put(element, new ElementConfig(cfg.x(), cfg.y(), cfg.scale(), cfg.heightScale(), background, cfg.opacity(), cfg.showBorders()));
    }

    public static synchronized boolean showBorders(Element element) {
        load();
        return configs.get(element).showBorders();
    }

    public static synchronized void setShowBorders(Element element, boolean show) {
        load();
        ElementConfig cfg = configs.get(element);
        configs.put(element, new ElementConfig(cfg.x(), cfg.y(), cfg.scale(), cfg.heightScale(), cfg.background(), cfg.opacity(), show));
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
        resetConfigs();
        snapToGrid = true;
    }

    public static synchronized Snapshot snapshot() {
        load();
        return new Snapshot(configs.clone(), snapToGrid);
    }

    public static synchronized void restore(Snapshot snapshot) {
        if (snapshot == null) {
            resetToDefaults();
            return;
        }
        loaded = true;
        configs.clear();
        configs.putAll(snapshot.configs());
        snapToGrid = snapshot.snapToGrid();
    }

    public static synchronized void save() {
        load();
        JsonObject root = new JsonObject();
        for (Map.Entry<Element, ElementConfig> entry : configs.entrySet()) {
            JsonObject obj = new JsonObject();
            String name = entry.getKey().name().toLowerCase();
            ElementConfig cfg = entry.getValue();
            obj.addProperty("x", cfg.x());
            obj.addProperty("y", cfg.y());
            obj.addProperty("scale", cfg.scale());
            obj.addProperty("height_scale", cfg.heightScale());
            obj.addProperty("background", cfg.background());
            obj.addProperty("opacity", cfg.opacity());
            obj.addProperty("show_borders", cfg.showBorders());
            root.add(name, obj);
        }
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
            for (Element element : Element.values()) {
                String name = element.name().toLowerCase();
                JsonObject obj = object(root, name);
                if (obj == null) {
                    continue;
                }
                ElementConfig current = configs.get(element);
                int x = JsonFieldHelper.readInt(obj, "x", current.x());
                int y = JsonFieldHelper.readInt(obj, "y", current.y());
                int scale = Math.max(1, JsonFieldHelper.readInt(obj, "scale", current.scale()));
                int heightScale = Math.max(1, JsonFieldHelper.readInt(obj, "height_scale", current.heightScale()));
                String background = JsonFieldHelper.string(obj, "background", current.background());
                int opacity = clamp(JsonFieldHelper.readInt(obj, "opacity", current.opacity()), 0, 100);
                boolean showBorders = JsonFieldHelper.bool(obj, "show_borders", current.showBorders());
                configs.put(element, new ElementConfig(x, y, scale, heightScale, background, opacity, showBorders));
            }
            snapToGrid = JsonFieldHelper.bool(root, "snapToGrid", snapToGrid);
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed reading Quests and Stuff HUD layout {}, keeping defaults", file, e);
        }
    }

    private static HudBox box(Element element, int screenWidth, int screenHeight, int width, int height) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        ElementConfig cfg = configs.get(element);
        int x = cfg.x();
        int y = cfg.y();
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
            Map<Element, ElementConfig> configs,
            boolean snapToGrid
    ) {
    }
}
