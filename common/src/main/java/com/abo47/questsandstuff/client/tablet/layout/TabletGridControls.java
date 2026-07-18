package com.abo47.questsandstuff.client.tablet.layout;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CANVAS_BG_OPACITY;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_W;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_W_MAX;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_W_MIN;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.GRID_OPACITY;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.GRID_SIZES;

public final class TabletGridControls {
    private TabletGridControls() {
    }

    public static int clampGridSizeIndex(int index) {
        if (GRID_SIZES.length == 0) {
            return 0;
        }
        return Math.max(0, Math.min(GRID_SIZES.length - 1, index));
    }

    public static int snapExpandedChapterWidth(int width) {
        int grid = GRID_SIZES.length == 0 ? GRID_16 : Math.max(1, GRID_SIZES[clampGridSizeIndex(0)]);
        int snapped = CHAPTER_W + Math.round((float) (width - CHAPTER_W) / (float) grid) * grid;
        return Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, snapped));
    }

    public static int defaultGridOpacityPercent(TabletUiState state) {
        if (GRID_OPACITY.length == 0) {
            return Math.max(0, Math.min(100, state.canvas.gridOpacityPercent));
        }
        int index = Math.max(0, Math.min(GRID_OPACITY.length - 1, state.canvas.gridOpacityIndex));
        return Math.max(0, Math.min(100, GRID_OPACITY[index]));
    }

    public static int defaultGridColor(TabletUiState state) {
        if (state == null || state.canvas.gridColor == 0) {
            return TabletColors.TEXT_PRIMARY;
        }
        return state.canvas.gridColor;
    }

    public static int defaultCanvasBgOpacityPercent(TabletUiState state) {
        if (CANVAS_BG_OPACITY.length == 0) {
            return Math.max(0, Math.min(100, state.canvas.canvasBgOpacityPercent));
        }
        int index = Math.max(0, Math.min(CANVAS_BG_OPACITY.length - 1, state.canvas.canvasBgOpacityIndex));
        return Math.max(0, Math.min(100, CANVAS_BG_OPACITY[index]));
    }

    public static void applyGridOpacityPercent(TabletUiState state, int percent) {
        int clamped = Math.max(0, Math.min(100, percent));
        state.canvas.gridOpacityPercent = clamped;
        state.canvas.gridOpacityIndex = nearestIndex(GRID_OPACITY, clamped);
        state.canvas.toolsGridOpacityDraft = Integer.toString(clamped);
    }

    public static void applyGridColor(TabletUiState state, int color) {
        if (state != null) {
            state.canvas.gridColor = color;
        }
    }

    public static void applyCanvasBgOpacityPercent(TabletUiState state, int percent) {
        int clamped = Math.max(0, Math.min(100, percent));
        state.canvas.canvasBgOpacityPercent = clamped;
        state.canvas.canvasBgOpacityIndex = nearestIndex(CANVAS_BG_OPACITY, clamped);
    }

    public static int cyclePercent(int current, int step, boolean backwards) {
        int clampedStep = Math.max(1, step);
        int clamped = Math.max(0, Math.min(100, current));
        if (backwards) {
            return clamped <= 0 ? 100 : Math.max(0, clamped - clampedStep);
        }
        return clamped >= 100 ? 0 : Math.min(100, clamped + clampedStep);
    }

    public static int toolPercentStep() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS
                ? 5
                : 10;
    }

    private static int nearestIndex(int[] values, int percent) {
        if (values.length == 0) {
            return 0;
        }
        int clamped = Math.max(0, Math.min(100, percent));
        int bestIndex = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < values.length; i++) {
            int distance = Math.abs(values[i] - clamped);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }
}
