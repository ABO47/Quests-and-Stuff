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
            state.canvasBgOpacityIndex = readInt(root, "canvas_bg_opacity_index", state.canvasBgOpacityIndex);
            state.canvasBgOpacityPercent = readInt(root, "canvas_bg_opacity_percent", state.canvasBgOpacityPercent);
            state.canvasZoom = readFloat(root, "canvas_zoom", state.canvasZoom);
            state.minimapCollapsed = readBoolean(root, "minimap_collapsed", state.minimapCollapsed);
            state.selectedGroup = readString(root, "last_selected_group", state.selectedGroup);
            state.chapterPanelWidth = readInt(root, "chapter_panel_width", state.chapterPanelWidth);
            state.chapterPanelCollapsed = readBoolean(root, "chapter_panel_collapsed", state.chapterPanelCollapsed);
            state.chapterSplitterLocked = readBoolean(root, "chapter_splitter_locked", state.chapterSplitterLocked);
            state.chapterPanelLastExpandedWidth = readInt(root, "chapter_panel_last_expanded_width", state.chapterPanelLastExpandedWidth);
            state.questDetailsLeftPanelWidth = clampQuestDetailsLeftWidth(readInt(root, "quest_details_left_panel_width", state.questDetailsLeftPanelWidth));
            state.questDetailsSplitterLocked = readBoolean(root, "quest_details_splitter_locked", state.questDetailsSplitterLocked);
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
            root.addProperty("canvas_bg_opacity_index", state.canvasBgOpacityIndex);
            root.addProperty("canvas_bg_opacity_percent", state.canvasBgOpacityPercent);
            root.addProperty("canvas_zoom", state.canvasZoom);
            root.addProperty("minimap_collapsed", state.minimapCollapsed);
            root.addProperty("last_selected_group", state.selectedGroup == null ? "" : state.selectedGroup);
            root.addProperty("chapter_panel_width", chapterPanelWidth(state));
            root.addProperty("chapter_panel_collapsed", isChapterPanelCollapsed(state));
            root.addProperty("chapter_splitter_locked", state.chapterSplitterLocked);
            root.addProperty("chapter_panel_last_expanded_width", Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, state.chapterPanelLastExpandedWidth)));
            root.addProperty("quest_details_left_panel_width", clampQuestDetailsLeftWidth(state.questDetailsLeftPanelWidth));
            root.addProperty("quest_details_splitter_locked", state.questDetailsSplitterLocked);
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

    private static int clampQuestDetailsLeftWidth(int width) {
        return Math.max(120, Math.min(CHAPTER_W_MAX, Math.max(CHAPTER_W_MIN, width)));
    }
}
