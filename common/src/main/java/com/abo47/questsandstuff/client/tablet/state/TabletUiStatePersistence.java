package com.abo47.questsandstuff.client.tablet.state;


import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinFillOverride;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_W_MAX;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_W_MIN;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.chapterPanelWidth;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.isChapterPanelCollapsed;

public final class TabletUiStatePersistence {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path UI_STATE_FILE = Path.of("config", "questsandstuff", "ui_state.json");

    private TabletUiStatePersistence() {
    }

    public static void read(TabletUiState state) {
        if (state == null) {
            return;
        }
        try {
            if (!Files.exists(UI_STATE_FILE)) {
                return;
            }
            JsonObject root = JsonParser.parseString(Files.readString(UI_STATE_FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            state.root.editMode = readBoolean(root, "edit_mode", state.root.editMode);
            state.questDetails.questDetailsEditMode = readBoolean(root, "quest_details_edit_mode", state.questDetails.questDetailsEditMode);
            state.canvas.gridEnabled = readBoolean(root, "grid_enabled", state.canvas.gridEnabled);
            state.canvas.gridSnapLocked = readBoolean(root, "grid_snap_locked", state.canvas.gridSnapLocked);
            state.canvas.centerSnapXEnabled = readBoolean(root, "center_snap_x_enabled", state.canvas.centerSnapXEnabled);
            state.canvas.centerSnapYEnabled = readBoolean(root, "center_snap_y_enabled", state.canvas.centerSnapYEnabled);
            state.canvas.objectSnapEnabled = readBoolean(root, "object_snap_enabled", state.canvas.objectSnapEnabled);
            state.canvas.gridCanvasLocked = readBoolean(root, "grid_canvas_locked", state.canvas.gridCanvasLocked);
            state.canvas.gridSizeIndex = readInt(root, "grid_size_index", state.canvas.gridSizeIndex);
            state.canvas.gridOpacityIndex = readInt(root, "grid_opacity_index", state.canvas.gridOpacityIndex);
            state.canvas.gridOpacityPercent = readInt(root, "grid_opacity_percent", state.canvas.gridOpacityPercent);
            state.canvas.gridColor = readInt(root, "grid_color", state.canvas.gridColor);
            state.canvas.canvasBgOpacityIndex = readInt(root, "canvas_bg_opacity_index", state.canvas.canvasBgOpacityIndex);
            state.canvas.canvasBgOpacityPercent = readInt(root, "canvas_bg_opacity_percent", state.canvas.canvasBgOpacityPercent);
            state.canvas.canvasZoom = readFloat(root, "canvas_zoom", state.canvas.canvasZoom);
            readCanvasCameras(root, state);
            state.canvas.minimapCollapsed = readBoolean(root, "minimap_collapsed", state.canvas.minimapCollapsed);
            state.root.lastApp = readString(root, "last_app", state.root.lastApp);
            state.root.selectedChapter = readString(root, "last_selected_chapter", state.root.selectedChapter);
            state.chapterPanel.chapterPanelWidth = readInt(root, "chapter_panel_width", state.chapterPanel.chapterPanelWidth);
            state.chapterPanel.chapterPanelCollapsed = readBoolean(root, "chapter_panel_collapsed", state.chapterPanel.chapterPanelCollapsed);
            state.chapterPanel.chapterSplitterLocked = readBoolean(root, "chapter_splitter_locked", state.chapterPanel.chapterSplitterLocked);
            state.chapterPanel.chapterPanelLastExpandedWidth = readInt(root, "chapter_panel_last_expanded_width", state.chapterPanel.chapterPanelLastExpandedWidth);
            state.questDetails.questDetailsLeftPanelWidth = clampQuestDetailsLeftWidth(readInt(root, "quest_details_left_panel_width", state.questDetails.questDetailsLeftPanelWidth));
            state.questDetails.questDetailsSplitterLocked = readBoolean(root, "quest_details_splitter_locked", state.questDetails.questDetailsSplitterLocked);
            state.questDetails.questDetailsGridEnabled = readBoolean(root, "quest_details_grid_enabled", state.questDetails.questDetailsGridEnabled);
            state.questDetails.questDetailsGridSnapLocked = readBoolean(root, "quest_details_grid_snap_locked", state.questDetails.questDetailsGridSnapLocked);
            state.questDetails.questDetailsCenterSnapXEnabled = readBoolean(root, "quest_details_center_snap_x_enabled", state.questDetails.questDetailsCenterSnapXEnabled);
            state.questDetails.questDetailsCenterSnapYEnabled = readBoolean(root, "quest_details_center_snap_y_enabled", state.questDetails.questDetailsCenterSnapYEnabled);
            state.questDetails.questDetailsObjectSnapEnabled = readBoolean(root, "quest_details_object_snap_enabled", state.questDetails.questDetailsObjectSnapEnabled);
            state.questDetails.questDetailsCanvasLocked = readBoolean(root, "quest_details_canvas_locked", state.questDetails.questDetailsCanvasLocked);
            state.questDetails.questDetailsGridOpacityPercent = readInt(root, "quest_details_grid_opacity_percent", state.questDetails.questDetailsGridOpacityPercent);
            state.questDetails.questDetailsCanvasBgOpacityPercent = readInt(root, "quest_details_canvas_bg_opacity_percent", state.questDetails.questDetailsCanvasBgOpacityPercent);
            readColorPalette(root, state);
        } catch (Exception exception) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed reading UI state from {}, keeping defaults", UI_STATE_FILE, exception);
        }
    }

    public static boolean readEditMode() {
        TabletUiState state = new TabletUiState();
        read(state);
        return state.root.editMode;
    }

    public static void write(TabletUiState state) {
        if (state == null) {
            return;
        }
        try {
            Files.createDirectories(UI_STATE_FILE.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("edit_mode", state.root.editMode);
            root.addProperty("quest_details_edit_mode", state.questDetails.questDetailsEditMode);
            root.addProperty("grid_enabled", state.canvas.gridEnabled);
            root.addProperty("grid_snap_locked", state.canvas.gridSnapLocked);
            root.addProperty("center_snap_x_enabled", state.canvas.centerSnapXEnabled);
            root.addProperty("center_snap_y_enabled", state.canvas.centerSnapYEnabled);
            root.addProperty("object_snap_enabled", state.canvas.objectSnapEnabled);
            root.addProperty("grid_canvas_locked", state.canvas.gridCanvasLocked);
            root.addProperty("grid_size_index", state.canvas.gridSizeIndex);
            root.addProperty("grid_opacity_index", state.canvas.gridOpacityIndex);
            root.addProperty("grid_opacity_percent", state.canvas.gridOpacityPercent);
            root.addProperty("grid_color", state.canvas.gridColor);
            root.addProperty("canvas_bg_opacity_index", state.canvas.canvasBgOpacityIndex);
            root.addProperty("canvas_bg_opacity_percent", state.canvas.canvasBgOpacityPercent);
            root.addProperty("canvas_zoom", state.canvas.canvasZoom);
            root.add("canvas_cameras", writeCanvasCameras(state));
            root.addProperty("minimap_collapsed", state.canvas.minimapCollapsed);
            root.addProperty("last_app", state.root.lastApp == null ? "" : state.root.lastApp);
            root.addProperty("last_selected_chapter", state.root.selectedChapter == null ? "" : state.root.selectedChapter);
            root.addProperty("chapter_panel_width", chapterPanelWidth(state));
            root.addProperty("chapter_panel_collapsed", isChapterPanelCollapsed(state));
            root.addProperty("chapter_splitter_locked", state.chapterPanel.chapterSplitterLocked);
            root.addProperty("chapter_panel_last_expanded_width", Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, state.chapterPanel.chapterPanelLastExpandedWidth)));
            root.addProperty("quest_details_left_panel_width", clampQuestDetailsLeftWidth(state.questDetails.questDetailsLeftPanelWidth));
            root.addProperty("quest_details_splitter_locked", state.questDetails.questDetailsSplitterLocked);
            root.addProperty("quest_details_grid_enabled", state.questDetails.questDetailsGridEnabled);
            root.addProperty("quest_details_grid_snap_locked", state.questDetails.questDetailsGridSnapLocked);
            root.addProperty("quest_details_center_snap_x_enabled", state.questDetails.questDetailsCenterSnapXEnabled);
            root.addProperty("quest_details_center_snap_y_enabled", state.questDetails.questDetailsCenterSnapYEnabled);
            root.addProperty("quest_details_object_snap_enabled", state.questDetails.questDetailsObjectSnapEnabled);
            root.addProperty("quest_details_canvas_locked", state.questDetails.questDetailsCanvasLocked);
            root.addProperty("quest_details_grid_opacity_percent", state.questDetails.questDetailsGridOpacityPercent);
            root.addProperty("quest_details_canvas_bg_opacity_percent", state.questDetails.questDetailsCanvasBgOpacityPercent);
            writeColorPalette(root, state);
            Files.writeString(UI_STATE_FILE, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed persisting UI state", e);
        }
    }

    public static void writeEditMode(boolean enabled) {
        TabletUiState state = new TabletUiState();
        read(state);
        state.root.editMode = enabled;
        write(state);
    }

    private static boolean readBoolean(JsonObject root, String key, boolean fallback) {
        if (root == null || key == null || !root.has(key)) {
            return fallback;
        }
        try {
            return root.get(key).getAsBoolean();
        } catch (RuntimeException exception) {
            logFieldFallback("boolean", key, fallback, exception);
            return fallback;
        }
    }

    private static float readFloat(JsonObject root, String key, float fallback) {
        if (root == null || key == null || !root.has(key)) {
            return fallback;
        }
        try {
            float value = root.get(key).getAsFloat();
            if (Float.isNaN(value) || Float.isInfinite(value)) {
                return fallback;
            }
            return Math.max(0.5f, Math.min(3.0f, value));
        } catch (RuntimeException exception) {
            logFieldFallback("float", key, fallback, exception);
            return fallback;
        }
    }

    private static int readInt(JsonObject root, String key, int fallback) {
        if (root == null || key == null || !root.has(key)) {
            return fallback;
        }
        try {
            return root.get(key).getAsInt();
        } catch (RuntimeException exception) {
            logFieldFallback("int", key, fallback, exception);
            return fallback;
        }
    }

    private static String readString(JsonObject root, String key, String fallback) {
        if (root == null || key == null || !root.has(key)) {
            return fallback;
        }
        try {
            return root.get(key).getAsString();
        } catch (RuntimeException exception) {
            logFieldFallback("string", key, fallback, exception);
            return fallback;
        }
    }

    private static void readCanvasCameras(JsonObject root, TabletUiState state) {
        if (root == null || state == null || !root.has("canvas_cameras") || !root.get("canvas_cameras").isJsonObject()) {
            return;
        }
        JsonObject cameras = root.getAsJsonObject("canvas_cameras");
        for (String chapter : cameras.keySet()) {
            if (group == null || group.isBlank()) {
                continue;
            }
            try {
                JsonObject camera = cameras.getAsJsonObject(group);
                double centerX = camera.has("center_x") ? camera.get("center_x").getAsDouble() : 0.0D;
                double centerY = camera.has("center_y") ? camera.get("center_y").getAsDouble() : 0.0D;
                float zoom = camera.has("zoom") ? camera.get("zoom").getAsFloat() : state.canvas.canvasZoom;
                if (Double.isFinite(centerX) && Double.isFinite(centerY) && Float.isFinite(zoom)) {
                    state.canvas.canvasCameraCentersByGroup.put(group, new com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasDoublePoint(centerX, centerY));
                    state.canvas.canvasCameraZoomsByGroup.put(group, Math.max(0.5f, Math.min(3.0f, zoom)));
                }
            } catch (RuntimeException exception) {
                QuestsAndStuffMod.LOGGER.warn(
                        "[QnS:UI] Invalid persisted canvas camera group={} file={}",
                        group,
                        UI_STATE_FILE,
                        exception
                );
            }
        }
    }

    private static void logFieldFallback(String type, String key, Object fallback, RuntimeException exception) {
        QuestsAndStuffMod.LOGGER.warn(
                "[QnS:UI] Invalid persisted UI state field type={} key={} fallback={} file={}",
                type,
                key,
                fallback,
                UI_STATE_FILE,
                exception
        );
    }

    private static JsonObject writeCanvasCameras(TabletUiState state) {
        JsonObject cameras = new JsonObject();
        if (state == null) {
            return cameras;
        }
        for (String chapter : state.canvas.canvasCameraCentersByGroup.keySet()) {
            if (group == null || group.isBlank()) {
                continue;
            }
            com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasDoublePoint center = state.canvas.canvasCameraCentersByGroup.get(group);
            if (center == null || !Double.isFinite(center.x()) || !Double.isFinite(center.y())) {
                continue;
            }
            JsonObject camera = new JsonObject();
            camera.addProperty("center_x", center.x());
            camera.addProperty("center_y", center.y());
            camera.addProperty("zoom", Math.max(0.5f, Math.min(3.0f, state.canvas.canvasCameraZoomsByGroup.getOrDefault(group, state.canvas.canvasZoom))));
            cameras.add(group, camera);
        }
        return cameras;
    }

    private static void readColorPalette(JsonObject root, TabletUiState state) {
        if (root == null || state == null || !root.has("color_palette") || !root.get("color_palette").isJsonArray()) {
            return;
        }
        JsonArray palette = root.getAsJsonArray("color_palette");
        if (palette.isEmpty()) {
            return;
        }
        state.pickers.textColorPalette.clear();
        for (int i = 0; i < palette.size(); i++) {
            try {
                state.pickers.textColorPalette.add(palette.get(i).getAsInt());
            } catch (RuntimeException exception) {
                QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Invalid color palette entry index={}", i, exception);
            }
        }
    }

    private static void writeColorPalette(JsonObject root, TabletUiState state) {
        JsonArray palette = new JsonArray();
        for (int color : state.pickers.textColorPalette) {
            palette.add(color);
        }
        root.add("color_palette", palette);
    }

    private static void readSkinFillOverrides(JsonObject root, TabletUiState state) {
        if (root == null || state == null || !root.has("skin_fill_overrides") || !root.get("skin_fill_overrides").isJsonObject()) {
            return;
        }
        JsonObject overrides = root.getAsJsonObject("skin_fill_overrides");
        state.root.skinFillOverrides.clear();
        for (String key : overrides.keySet()) {
            if (key != null && !key.isBlank() && overrides.get(key).isJsonPrimitive()) {
                String val = overrides.get(key).getAsString();
                if (SkinFillOverride.parse(val) != null) {
                    state.root.skinFillOverrides.put(key, val);
                }
            }
        }
    }

    private static void writeSkinFillOverrides(JsonObject root, TabletUiState state) {
        if (state == null || state.root.skinFillOverrides == null || state.root.skinFillOverrides.isEmpty()) {
            return;
        }
        JsonObject overrides = new JsonObject();
        for (var entry : state.root.skinFillOverrides.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null && !entry.getValue().isBlank()) {
                overrides.addProperty(entry.getKey(), entry.getValue());
            }
        }
        if (overrides.size() > 0) {
            root.add("skin_fill_overrides", overrides);
        }
    }

    private static final Path SKIN_STATE_FILE = Path.of("config", "questsandstuff", "skin_state.json");

    public static void readSkinState(TabletUiState state) {
        if (state == null) return;
        try {
            if (!Files.exists(SKIN_STATE_FILE)) return;
            JsonObject root = JsonParser.parseString(Files.readString(SKIN_STATE_FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            state.root.skinEditMode = readBoolean(root, "skin_edit_mode", state.root.skinEditMode);
            state.root.skinEditSelectedTarget = readString(root, "skin_edit_selected_target", state.root.skinEditSelectedTarget);
            readSkinFillOverrides(root, state);
        } catch (Exception exception) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed reading skin state from {}, keeping defaults", SKIN_STATE_FILE, exception);
        }
    }

    public static void writeSkinState(TabletUiState state) {
        if (state == null) return;
        try {
            Files.createDirectories(SKIN_STATE_FILE.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("skin_edit_mode", state.root.skinEditMode);
            root.addProperty("skin_edit_selected_target", state.root.skinEditSelectedTarget == null ? "" : state.root.skinEditSelectedTarget);
            writeSkinFillOverrides(root, state);
            Files.writeString(SKIN_STATE_FILE, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed persisting skin state", e);
        }
    }

    private static int clampQuestDetailsLeftWidth(int width) {
        return Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, width));
    }

}
