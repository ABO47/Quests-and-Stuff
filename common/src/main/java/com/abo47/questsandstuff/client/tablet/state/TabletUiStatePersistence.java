package com.abo47.questsandstuff.client.tablet.state;


import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_W_MAX;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_W_MIN;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterPanelWidth;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.isChapterPanelCollapsed;

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
            state.editMode = readBoolean(root, "edit_mode", state.editMode);
            state.questDetailsEditMode = readBoolean(root, "quest_details_edit_mode", state.questDetailsEditMode);
            state.gridEnabled = readBoolean(root, "grid_enabled", state.gridEnabled);
            state.gridSnapLocked = readBoolean(root, "grid_snap_locked", state.gridSnapLocked);
            state.centerSnapXEnabled = readBoolean(root, "center_snap_x_enabled", state.centerSnapXEnabled);
            state.centerSnapYEnabled = readBoolean(root, "center_snap_y_enabled", state.centerSnapYEnabled);
            state.objectSnapEnabled = readBoolean(root, "object_snap_enabled", state.objectSnapEnabled);
            state.gridCanvasLocked = readBoolean(root, "grid_canvas_locked", state.gridCanvasLocked);
            state.gridSizeIndex = readInt(root, "grid_size_index", state.gridSizeIndex);
            state.gridOpacityIndex = readInt(root, "grid_opacity_index", state.gridOpacityIndex);
            state.gridOpacityPercent = readInt(root, "grid_opacity_percent", state.gridOpacityPercent);
            state.gridColor = readInt(root, "grid_color", state.gridColor);
            state.canvasBgOpacityIndex = readInt(root, "canvas_bg_opacity_index", state.canvasBgOpacityIndex);
            state.canvasBgOpacityPercent = readInt(root, "canvas_bg_opacity_percent", state.canvasBgOpacityPercent);
            state.canvasZoom = readFloat(root, "canvas_zoom", state.canvasZoom);
            readCanvasCameras(root, state);
            state.minimapCollapsed = readBoolean(root, "minimap_collapsed", state.minimapCollapsed);
            state.selectedGroup = readString(root, "last_selected_group", state.selectedGroup);
            state.chapterPanelWidth = readInt(root, "chapter_panel_width", state.chapterPanelWidth);
            state.chapterPanelCollapsed = readBoolean(root, "chapter_panel_collapsed", state.chapterPanelCollapsed);
            state.chapterSplitterLocked = readBoolean(root, "chapter_splitter_locked", state.chapterSplitterLocked);
            state.chapterPanelLastExpandedWidth = readInt(root, "chapter_panel_last_expanded_width", state.chapterPanelLastExpandedWidth);
            state.questDetailsLeftPanelWidth = clampQuestDetailsLeftWidth(readInt(root, "quest_details_left_panel_width", state.questDetailsLeftPanelWidth));
            state.questDetailsSplitterLocked = readBoolean(root, "quest_details_splitter_locked", state.questDetailsSplitterLocked);
            state.questDetailsGridEnabled = readBoolean(root, "quest_details_grid_enabled", state.questDetailsGridEnabled);
            state.questDetailsGridSnapLocked = readBoolean(root, "quest_details_grid_snap_locked", state.questDetailsGridSnapLocked);
            state.questDetailsCenterSnapXEnabled = readBoolean(root, "quest_details_center_snap_x_enabled", state.questDetailsCenterSnapXEnabled);
            state.questDetailsCenterSnapYEnabled = readBoolean(root, "quest_details_center_snap_y_enabled", state.questDetailsCenterSnapYEnabled);
            state.questDetailsObjectSnapEnabled = readBoolean(root, "quest_details_object_snap_enabled", state.questDetailsObjectSnapEnabled);
            state.questDetailsCanvasLocked = readBoolean(root, "quest_details_canvas_locked", state.questDetailsCanvasLocked);
            state.questDetailsGridOpacityPercent = readInt(root, "quest_details_grid_opacity_percent", state.questDetailsGridOpacityPercent);
            state.questDetailsCanvasBgOpacityPercent = readInt(root, "quest_details_canvas_bg_opacity_percent", state.questDetailsCanvasBgOpacityPercent);
        } catch (Exception ignored) {
        }
    }

    public static boolean readEditMode() {
        TabletUiState state = new TabletUiState();
        read(state);
        return state.editMode;
    }

    public static void write(TabletUiState state) {
        if (state == null) {
            return;
        }
        try {
            Files.createDirectories(UI_STATE_FILE.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("edit_mode", state.editMode);
            root.addProperty("quest_details_edit_mode", state.questDetailsEditMode);
            root.addProperty("grid_enabled", state.gridEnabled);
            root.addProperty("grid_snap_locked", state.gridSnapLocked);
            root.addProperty("center_snap_x_enabled", state.centerSnapXEnabled);
            root.addProperty("center_snap_y_enabled", state.centerSnapYEnabled);
            root.addProperty("object_snap_enabled", state.objectSnapEnabled);
            root.addProperty("grid_canvas_locked", state.gridCanvasLocked);
            root.addProperty("grid_size_index", state.gridSizeIndex);
            root.addProperty("grid_opacity_index", state.gridOpacityIndex);
            root.addProperty("grid_opacity_percent", state.gridOpacityPercent);
            root.addProperty("grid_color", state.gridColor);
            root.addProperty("canvas_bg_opacity_index", state.canvasBgOpacityIndex);
            root.addProperty("canvas_bg_opacity_percent", state.canvasBgOpacityPercent);
            root.addProperty("canvas_zoom", state.canvasZoom);
            root.add("canvas_cameras", writeCanvasCameras(state));
            root.addProperty("minimap_collapsed", state.minimapCollapsed);
            root.addProperty("last_selected_group", state.selectedGroup == null ? "" : state.selectedGroup);
            root.addProperty("chapter_panel_width", chapterPanelWidth(state));
            root.addProperty("chapter_panel_collapsed", isChapterPanelCollapsed(state));
            root.addProperty("chapter_splitter_locked", state.chapterSplitterLocked);
            root.addProperty("chapter_panel_last_expanded_width", Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, state.chapterPanelLastExpandedWidth)));
            root.addProperty("quest_details_left_panel_width", clampQuestDetailsLeftWidth(state.questDetailsLeftPanelWidth));
            root.addProperty("quest_details_splitter_locked", state.questDetailsSplitterLocked);
            root.addProperty("quest_details_grid_enabled", state.questDetailsGridEnabled);
            root.addProperty("quest_details_grid_snap_locked", state.questDetailsGridSnapLocked);
            root.addProperty("quest_details_center_snap_x_enabled", state.questDetailsCenterSnapXEnabled);
            root.addProperty("quest_details_center_snap_y_enabled", state.questDetailsCenterSnapYEnabled);
            root.addProperty("quest_details_object_snap_enabled", state.questDetailsObjectSnapEnabled);
            root.addProperty("quest_details_canvas_locked", state.questDetailsCanvasLocked);
            root.addProperty("quest_details_grid_opacity_percent", state.questDetailsGridOpacityPercent);
            root.addProperty("quest_details_canvas_bg_opacity_percent", state.questDetailsCanvasBgOpacityPercent);
            Files.writeString(UI_STATE_FILE, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed persisting UI state", e);
        }
    }

    public static void writeEditMode(boolean enabled) {
        TabletUiState state = new TabletUiState();
        read(state);
        state.editMode = enabled;
        write(state);
    }

    private static boolean readBoolean(JsonObject root, String key, boolean fallback) {
        if (root == null || key == null || !root.has(key)) {
            return fallback;
        }
        try {
            return root.get(key).getAsBoolean();
        } catch (Exception ignored) {
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
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static int readInt(JsonObject root, String key, int fallback) {
        if (root == null || key == null || !root.has(key)) {
            return fallback;
        }
        try {
            return root.get(key).getAsInt();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static String readString(JsonObject root, String key, String fallback) {
        if (root == null || key == null || !root.has(key)) {
            return fallback;
        }
        try {
            return root.get(key).getAsString();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static void readCanvasCameras(JsonObject root, TabletUiState state) {
        if (root == null || state == null || !root.has("canvas_cameras") || !root.get("canvas_cameras").isJsonObject()) {
            return;
        }
        JsonObject cameras = root.getAsJsonObject("canvas_cameras");
        for (String group : cameras.keySet()) {
            if (group == null || group.isBlank()) {
                continue;
            }
            try {
                JsonObject camera = cameras.getAsJsonObject(group);
                double centerX = camera.has("center_x") ? camera.get("center_x").getAsDouble() : 0.0D;
                double centerY = camera.has("center_y") ? camera.get("center_y").getAsDouble() : 0.0D;
                float zoom = camera.has("zoom") ? camera.get("zoom").getAsFloat() : state.canvasZoom;
                if (Double.isFinite(centerX) && Double.isFinite(centerY) && Float.isFinite(zoom)) {
                    state.canvasCameraCentersByGroup.put(group, new com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasDoublePoint(centerX, centerY));
                    state.canvasCameraZoomsByGroup.put(group, Math.max(0.5f, Math.min(3.0f, zoom)));
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static JsonObject writeCanvasCameras(TabletUiState state) {
        JsonObject cameras = new JsonObject();
        if (state == null) {
            return cameras;
        }
        for (String group : state.canvasCameraCentersByGroup.keySet()) {
            if (group == null || group.isBlank()) {
                continue;
            }
            com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasDoublePoint center = state.canvasCameraCentersByGroup.get(group);
            if (center == null || !Double.isFinite(center.x()) || !Double.isFinite(center.y())) {
                continue;
            }
            JsonObject camera = new JsonObject();
            camera.addProperty("center_x", center.x());
            camera.addProperty("center_y", center.y());
            camera.addProperty("zoom", Math.max(0.5f, Math.min(3.0f, state.canvasCameraZoomsByGroup.getOrDefault(group, state.canvasZoom))));
            cameras.add(group, camera);
        }
        return cameras;
    }

    private static int clampQuestDetailsLeftWidth(int width) {
        return Math.max(120, Math.min(CHAPTER_W_MAX, Math.max(CHAPTER_W_MIN, width)));
    }
}
