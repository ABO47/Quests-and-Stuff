package com.abo47.questsandstuff.client.tablet.details;

import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.BODY_W;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_TOP_H_COMPACT;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.GAP;

final class QuestDetailsWindowGeometry {
    private QuestDetailsWindowGeometry() {
    }

    static int leftPanelWidth(TabletUiState state) {
        if (state == null) {
            return TabletUiFactory.CHAPTER_W;
        }
        state.questDetailsLeftPanelWidth = QuestDetailsSplitterWidget.clampDetailsLeftWidth(state.questDetailsLeftPanelWidth);
        return state.questDetailsLeftPanelWidth;
    }

    static int canvasPanelWidth(int leftW) {
        return Math.max(120, BODY_W - leftW - GAP);
    }

    static int[] mainCanvasViewport(TabletUiState state, int canvasW) {
        int topH = CANVAS_TOP_H_COMPACT;
        int availableViewportW = canvasW - QuestDetailsWindow.CONTENT_INSET * 2;
        int availableViewportH = CANVAS_H - topH - QuestDetailsWindow.CONTENT_INSET * 2;
        int innerAvailableW = Math.max(1, availableViewportW - 1);
        int innerAvailableH = Math.max(1, availableViewportH - 1);
        int cell = Math.max(1, CanvasGeometry.gridSize(state));
        int gridCols = Math.max(1, innerAvailableW / cell);
        int gridRows = Math.max(1, innerAvailableH / cell);
        int viewportW = Math.max(cell + 1, gridCols * cell + 1);
        int viewportH = Math.max(cell + 1, gridRows * cell + 1);
        int viewportX = QuestDetailsWindow.CONTENT_INSET + Math.max(0, (availableViewportW - viewportW) / 2);
        int viewportY = topH + QuestDetailsWindow.CONTENT_INSET + Math.max(0, (availableViewportH - viewportH) / 2);
        return new int[]{viewportX, viewportY, viewportW, viewportH};
    }

    static int descriptionContentWidth(TabletUiState state) {
        int leftW = leftPanelWidth(state);
        int canvasW = canvasPanelWidth(leftW);
        int[] viewport = mainCanvasViewport(state, canvasW);
        return Math.max(1, viewport[2] - 1);
    }
}
