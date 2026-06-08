package com.abo47.questsandstuff.client.tablet.quest.chapter;


import com.abo47.questsandstuff.client.tablet.layout.TabletResizeCursor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;

import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.snapExpandedChapterWidth;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_W_ICON;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_W_ICON_SNAP;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_W_MAX;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_W_MIN;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.SPLITTER_W;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterPanelWidth;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.isChapterPanelCollapsed;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.persistUiState;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

public final class ChapterSplitterWidget extends WidgetGroup {
    private static final int DRAG_THRESHOLD_PX = 3;
    private static final long HOVER_PULSE_MS = 900L;

    private final TabletUiState state;
    private final Runnable refresh;
    private boolean hoverActive;
    private long hoverPulseStartMs;

    public ChapterSplitterWidget(TabletUiState state, Runnable refresh) {
        super(0, CHAPTER_Y, SPLITTER_W, CHAPTER_H);
        this.state = state;
        this.refresh = refresh;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = state.canvas.draggingChapterSplitter || isMouseOverElement(mouseX, mouseY);
        boolean resizeHovered = hovered && !state.chapterPanel.chapterSplitterLocked;
        TabletResizeCursor.update(resizeHovered);
        updateHoverPulse(hovered);

        int left = getPositionX();
        int top = getPositionY();
        int width = getSizeWidth();
        int height = getSizeHeight();
        int fill = hovered ? withAlpha(ModColors.INTERACTIVE, hoverPulseAlpha()) : ModColors.SURFACE_BASE;
        graphics.fill(left, top, left + width, top + height, fill);
        graphics.fill(left, top, left + width, top + 1, ModColors.BORDER_BASE);
        graphics.fill(left, top + height - 1, left + width, top + height, ModColors.BORDER_BASE);

        drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isMouseOverElement(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        state.canvas.draggingChapterSplitter = true;
        state.chapterPanel.chapterSplitterDragMoved = false;
        state.chapterPanel.chapterSplitterDragStartX = (int) Math.round(mouseX);
        state.chapterPanel.chapterSplitterStartWidth = chapterPanelWidth(state);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (state.chapterPanel.chapterSplitterLocked) {
            TabletResizeCursor.update(false);
            return true;
        }
        if (!state.canvas.draggingChapterSplitter) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        int dx = (int) Math.round(mouseX) - state.chapterPanel.chapterSplitterDragStartX;
        if (!state.chapterPanel.chapterSplitterDragMoved && Math.abs(dx) <= DRAG_THRESHOLD_PX) {
            return true;
        }
        state.chapterPanel.chapterSplitterDragMoved = true;
        int nextWidth = state.chapterPanel.chapterSplitterStartWidth + dx;
        if (nextWidth <= CHAPTER_W_ICON_SNAP) {
            nextWidth = CHAPTER_W_ICON;
            state.chapterPanel.chapterPanelCollapsed = true;
        } else {
            nextWidth = snapExpandedChapterWidth(nextWidth);
            state.chapterPanel.chapterPanelCollapsed = false;
            state.chapterPanel.chapterPanelLastExpandedWidth = nextWidth;
        }
        state.chapterPanel.chapterPanelWidth = nextWidth;
        refresh.run();
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!state.canvas.draggingChapterSplitter) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        boolean dragged = state.chapterPanel.chapterSplitterDragMoved;
        state.canvas.draggingChapterSplitter = false;
        state.chapterPanel.chapterSplitterDragMoved = false;
        if (!dragged) {
            toggleCollapsed();
            return true;
        }
        state.chapterPanel.chapterPanelWidth = chapterPanelWidth(state);
        state.chapterPanel.chapterPanelCollapsed = isChapterPanelCollapsed(state);
        persistUiState(state);
        TabletResizeCursor.update(false);
        refresh.run();
        return true;
    }

    private void toggleCollapsed() {
        state.canvas.draggingChapterSplitter = false;
        state.chapterPanel.chapterSplitterDragMoved = false;
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

    private void updateHoverPulse(boolean hovered) {
        if (hovered && !hoverActive) {
            hoverPulseStartMs = System.currentTimeMillis();
        }
        hoverActive = hovered;
    }

    private int hoverPulseAlpha() {
        long elapsed = Math.max(0L, System.currentTimeMillis() - hoverPulseStartMs);
        double phase = (elapsed % HOVER_PULSE_MS) / (double) HOVER_PULSE_MS;
        double wave = 0.5D + Math.sin(phase * Math.PI * 2.0D) * 0.5D;
        return 56 + (int) Math.round(wave * 62.0D);
    }
}
