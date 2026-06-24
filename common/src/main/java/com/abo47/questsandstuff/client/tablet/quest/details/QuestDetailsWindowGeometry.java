package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.BODY_W;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_TOP_H_COMPACT;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_W_MAX;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_W_MIN;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.GAP;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.PANEL_W_MIN;

final class QuestDetailsWindowGeometry {
    private QuestDetailsWindowGeometry() {
    }

    static int leftPanelWidth(TabletUiState state) {
        if (state == null) {
            return TabletUiFactory.CHAPTER_W;
        }
        state.questDetails.questDetailsLeftPanelWidth = clampDetailsLeftWidth(state.questDetails.questDetailsLeftPanelWidth);
        return state.questDetails.questDetailsLeftPanelWidth;
    }

    static int clampDetailsLeftWidth(int width) {
        return Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, width));
    }

    static int canvasPanelWidth(int leftW) {
        return Math.max(PANEL_W_MIN, BODY_W - leftW - GAP);
    }

    static int[] mainCanvasViewport(TabletUiState state, int canvasW) {
        return TabletUiFactory.canvasViewportBounds(canvasW, CANVAS_H, CANVAS_TOP_H_COMPACT);
    }

    static int descriptionContentWidth(TabletUiState state) {
        int leftW = leftPanelWidth(state);
        int canvasW = canvasPanelWidth(leftW);
        int[] viewport = mainCanvasViewport(state, canvasW);
        return Math.max(1, viewport[2] - 1);
    }
}
