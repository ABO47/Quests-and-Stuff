package com.abo47.questsandstuff.client.tablet.teams;

import com.abo47.questsandstuff.client.tablet.layout.BaseSplitterWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.BODY_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.BODY_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_W_MAX;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_W_MIN;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.SPLITTER_W;

final class TeamsSplitterWidget extends BaseSplitterWidget {
    TeamsSplitterWidget(int x, TabletUiState state, Runnable refresh) {
        super(0, BODY_Y, SPLITTER_W, BODY_H, state, refresh);
    }

    @Override
    protected boolean isSplitterLocked() {
        return false;
    }

    @Override
    protected boolean isSplitterDragging() {
        return state.teams.teamsSplitterDragging;
    }

    @Override
    protected void setSplitterDragging(boolean dragging) {
        state.teams.teamsSplitterDragging = dragging;
    }

    @Override
    protected int splitterDragStartX() {
        return state.teams.teamsSplitterDragStartX;
    }

    @Override
    protected void setSplitterDragStartX(int x) {
        state.teams.teamsSplitterDragStartX = x;
    }

    @Override
    protected int splitterStartWidth() {
        return state.teams.teamsSplitterStartWidth;
    }

    @Override
    protected void setSplitterStartWidth(int width) {
        state.teams.teamsSplitterStartWidth = width;
    }

    @Override
    protected int getLeftPanelWidth() {
        return state.teams.leftPanelWidth;
    }

    @Override
    protected void setLeftPanelWidth(int width) {
        state.teams.leftPanelWidth = clampWidth(width);
    }

    @Override
    protected int computeDragWidth(int rawWidth) {
        return clampWidth(rawWidth);
    }

    private static int clampWidth(int w) {
        return Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, w));
    }
}
