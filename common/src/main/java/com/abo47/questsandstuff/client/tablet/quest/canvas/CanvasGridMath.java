package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;

final class CanvasGridMath {
    static final int QUEST_CELL_MARGIN = 1;
    private static final int QUEST_SLOT_SIZE = 16;

    private CanvasGridMath() {
    }

    static int snapToGrid(TabletUiState state, int value) {
        if (!state.gridSnapLocked) {
            return value;
        }
        int step = gridSize(state);
        return Math.round((float) value / (float) step) * step;
    }

    static int snapValueToGrid(int value, int grid) {
        int safeGrid = Math.max(1, grid);
        return Math.round((float) value / (float) safeGrid) * safeGrid;
    }

    static int snapVisualSpanToGridSlot(int value, int grid, int min) {
        int safeGrid = Math.max(1, grid);
        int safeMin = Math.max(1, min);
        int slot = Math.max(safeGrid, Math.round((float) (Math.max(1, value) + QUEST_CELL_MARGIN) / (float) safeGrid) * safeGrid);
        int visual = Math.max(1, slot - QUEST_CELL_MARGIN);
        while (visual < safeMin) {
            slot += safeGrid;
            visual = Math.max(1, slot - QUEST_CELL_MARGIN);
        }
        return visual;
    }

    static CanvasGeometry.GridVisualBox fitVisualBoxToGridSlot(int x, int y, int width, int height, int grid, int minWidth, int minHeight) {
        int safeGrid = Math.max(1, grid);
        int visualW = snapVisualSpanToGridSlot(width, safeGrid, minWidth);
        int visualH = snapVisualSpanToGridSlot(height, safeGrid, minHeight);
        int slotW = slotSpanForVisualSize(visualW);
        int slotH = slotSpanForVisualSize(visualH);
        int insetX = visualInsetForSlot(slotW, visualW);
        int insetY = visualInsetForSlot(slotH, visualH);
        int slotX = snapValueToGrid(x - insetX, safeGrid);
        int slotY = snapValueToGrid(y - insetY, safeGrid);
        return new CanvasGeometry.GridVisualBox(slotX + insetX, slotY + insetY, visualW, visualH, slotX, slotY, slotW, slotH);
    }

    static int slotSpanForVisualSize(int visualSize) {
        return slotLogicalSpan(Math.max(1, visualSize));
    }

    static int visualInsetForSlot(int slotLogicalSize, int visualLogicalSize) {
        if (slotLogicalSize <= visualLogicalSize) {
            return 0;
        }
        int centered = (slotLogicalSize - visualLogicalSize) / 2;
        return Math.min(slotLogicalSize - visualLogicalSize, Math.max(QUEST_CELL_MARGIN, centered));
    }

    static float snapScaleToGrid(TabletUiState state, float scale) {
        float normalized = Math.max(0.5f, scale);
        if (!state.gridSnapLocked) {
            return normalized;
        }
        int grid = gridSize(state);
        float targetVisible = Math.max(1.0f, TabletUiFactory.CARD_W * normalized);
        int cells = Math.max(1, Math.round((targetVisible + QUEST_CELL_MARGIN) / Math.max(1.0f, grid)));
        float snapped = ((cells * grid) - QUEST_CELL_MARGIN) / (float) TabletUiFactory.CARD_W;
        return Math.max(0.5f, snapped);
    }

    static int visualInsetX(TabletUiState state, float scale) {
        return visualInsetForSlot(slotLogicalWidth(state, scale), visualLogicalWidth(scale));
    }

    static int visualInsetY(TabletUiState state, float scale) {
        return visualInsetForSlot(slotLogicalHeight(state, scale), visualLogicalHeight(scale));
    }

    static int visualLogicalWidth(float scale) {
        return Math.max(1, Math.round(TabletUiFactory.CARD_W * Math.max(0.5f, scale)));
    }

    static int visualLogicalHeight(float scale) {
        return Math.max(1, Math.round(TabletUiFactory.CARD_H * Math.max(0.5f, scale)));
    }

    static int slotLogicalWidth(TabletUiState state, float scale) {
        return slotSpanForVisualSize(visualLogicalWidth(scale));
    }

    static int slotLogicalHeight(TabletUiState state, float scale) {
        return slotSpanForVisualSize(visualLogicalHeight(scale));
    }

    static int gridSize(TabletUiState state) {
        int index = Math.max(0, Math.min(TabletUiFactory.GRID_SIZES.length - 1, state.gridSizeIndex));
        return Math.max(1, TabletUiFactory.GRID_SIZES[index]);
    }

    private static int slotLogicalSpan(int visualLogicalSize) {
        int needed = Math.max(1, visualLogicalSize + QUEST_CELL_MARGIN);
        int cells = Math.max(1, (needed + QUEST_SLOT_SIZE - 1) / QUEST_SLOT_SIZE);
        return Math.max(QUEST_SLOT_SIZE, cells * QUEST_SLOT_SIZE);
    }
}
