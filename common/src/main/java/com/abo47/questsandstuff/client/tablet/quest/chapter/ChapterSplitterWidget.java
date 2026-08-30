package com.abo47.questsandstuff.client.tablet.quest.chapter;

import com.abo47.questsandstuff.client.tablet.layout.BaseSplitterWidget;
import com.abo47.questsandstuff.client.tablet.layout.TabletResizeCursor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.snapExpandedChapterWidth;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_W_ICON;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_W_ICON_SNAP;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_W_MAX;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_W_MIN;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_Y;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.SPLITTER_W;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.chapterPanelWidth;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.isChapterPanelCollapsed;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.persistUiState;

public final class ChapterSplitterWidget extends BaseSplitterWidget {
    private static final int DRAG_THRESHOLD_PX = 3;

    public ChapterSplitterWidget(TabletUiState state, Runnable refresh, int x) {
        super(x, CHAPTER_Y, SPLITTER_W, CHAPTER_H, state, refresh, "quests_splitter");
    }

    @Override
    protected boolean isSplitterLocked() {
        return state.chapterPanel.chapterSplitterLocked;
    }

    @Override
    protected boolean isSplitterDragging() {
        return state.canvas.draggingChapterSplitter;
    }

    @Override
    protected void setSplitterDragging(boolean dragging) {
        state.canvas.draggingChapterSplitter = dragging;
    }

    @Override
    protected int splitterDragStartX() {
        return state.chapterPanel.chapterSplitterDragStartX;
    }

    @Override
    protected void setSplitterDragStartX(int x) {
        state.chapterPanel.chapterSplitterDragStartX = x;
    }

    @Override
    protected int splitterStartWidth() {
        return state.chapterPanel.chapterSplitterStartWidth;
    }

    @Override
    protected void setSplitterStartWidth(int width) {
        state.chapterPanel.chapterSplitterStartWidth = width;
    }

    @Override
    protected int getLeftPanelWidth() {
        return chapterPanelWidth(state);
    }

    @Override
    protected void setLeftPanelWidth(int width) {
        state.chapterPanel.chapterPanelWidth = width;
    }

    @Override
    protected int dragThresholdPx() {
        return DRAG_THRESHOLD_PX;
    }

    @Override
    protected boolean hasDragMoved() {
        return state.chapterPanel.chapterSplitterDragMoved;
    }

    @Override
    protected void setDragMoved(boolean moved) {
        state.chapterPanel.chapterSplitterDragMoved = moved;
    }

    @Override
    protected int computeDragWidth(int rawWidth) {
        return snapExpandedChapterWidth(rawWidth);
    }

    @Override
    protected boolean canCollapseOnDrag() {
        return true;
    }

    @Override
    protected int collapseThresholdWidth() {
        return CHAPTER_W_ICON_SNAP;
    }

    @Override
    protected int collapsedPanelWidth() {
        return CHAPTER_W_ICON;
    }

    @Override
    protected boolean isPanelCollapsed() {
        return state.chapterPanel.chapterPanelCollapsed;
    }

    @Override
    protected void setPanelCollapsed(boolean collapsed) {
        state.chapterPanel.chapterPanelCollapsed = collapsed;
    }

    @Override
    protected void onSplitterClick() {
        if (isChapterPanelCollapsed(state)) {
            int expandedWidth = Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, state.chapterPanel.chapterPanelLastExpandedWidth));
            state.chapterPanel.chapterPanelWidth = expandedWidth;
            state.chapterPanel.chapterPanelCollapsed = false;
        } else {
            state.chapterPanel.chapterPanelLastExpandedWidth = Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, chapterPanelWidth(state)));
            state.chapterPanel.chapterPanelWidth = CHAPTER_W_ICON;
            state.chapterPanel.chapterPanelCollapsed = true;
        }
        persistUiState(state);
        TabletResizeCursor.update(false);
        refresh.run();
    }

    @Override
    protected void onSplitterRelease() {
        state.chapterPanel.chapterPanelWidth = chapterPanelWidth(state);
        state.chapterPanel.chapterPanelCollapsed = isChapterPanelCollapsed(state);
        persistUiState(state);
    }
}
