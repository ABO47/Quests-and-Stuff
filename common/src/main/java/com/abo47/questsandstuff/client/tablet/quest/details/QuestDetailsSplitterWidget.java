package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.layout.BaseSplitterWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.snapExpandedChapterWidth;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.SPLITTER_W;

final class QuestDetailsSplitterWidget extends BaseSplitterWidget {
    QuestDetailsSplitterWidget(int x, int y, int h, TabletUiState state, Runnable refresh) {
        super(x, y, SPLITTER_W, h, state, refresh, "quest_details_splitter");
    }

    @Override
    protected boolean isSplitterLocked() {
        return state.questDetails.questDetailsSplitterLocked;
    }

    @Override
    protected boolean isSplitterDragging() {
        return state.questDetails.questDetailsDraggingSplitter;
    }

    @Override
    protected void setSplitterDragging(boolean dragging) {
        state.questDetails.questDetailsDraggingSplitter = dragging;
    }

    @Override
    protected int splitterDragStartX() {
        return state.questDetails.questDetailsSplitterDragStartX;
    }

    @Override
    protected void setSplitterDragStartX(int x) {
        state.questDetails.questDetailsSplitterDragStartX = x;
    }

    @Override
    protected int splitterStartWidth() {
        return state.questDetails.questDetailsSplitterStartWidth;
    }

    @Override
    protected void setSplitterStartWidth(int width) {
        state.questDetails.questDetailsSplitterStartWidth = width;
    }

    @Override
    protected int getLeftPanelWidth() {
        return QuestDetailsWindow.leftPanelWidth(state);
    }

    @Override
    protected void setLeftPanelWidth(int width) {
        state.questDetails.questDetailsLeftPanelWidth = width;
    }

    @Override
    protected int computeDragWidth(int rawWidth) {
        return QuestDetailsWindowGeometry.clampDetailsLeftWidth(snapExpandedChapterWidth(rawWidth));
    }

    @Override
    protected boolean cancelDragOnClickLocked() {
        return true;
    }
}
