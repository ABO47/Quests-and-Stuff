package com.abo47.questsandstuff.client.tablet.chapter;


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
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.button;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterPanelWidth;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.isChapterPanelCollapsed;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.persistUiState;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class ChapterSplitterWidget extends WidgetGroup {
    private final TabletUiState state;
    private final Runnable refresh;
    private long lastLeftClickAtMs;

    public ChapterSplitterWidget(TabletUiState state, Runnable refresh) {
        super(0, CHAPTER_Y, SPLITTER_W, CHAPTER_H);
        this.state = state;
        this.refresh = refresh;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = !state.chapterSplitterLocked && (state.draggingChapterSplitter || isMouseOverElement(mouseX, mouseY));
        TabletResizeCursor.update(hovered);

        int left = getPositionX();
        int top = getPositionY();
        int width = getSizeWidth();
        int height = getSizeHeight();
        int fill = hovered ? withAlpha(ModColors.INTERACTIVE, 96) : ModColors.SURFACE_BASE;
        int border = hovered ? ModColors.BORDER_ACCENT : ModColors.BORDER_BASE;
        graphics.fill(left, top, left + width, top + height, fill);
        graphics.fill(left, top, left + width, top + 1, border);
        graphics.fill(left, top + height - 1, left + width, top + height, border);

        drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isMouseOverElement(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        long now = System.currentTimeMillis();
        if (now - lastLeftClickAtMs <= 250L) {
            toggleCollapsed();
            lastLeftClickAtMs = 0L;
            return true;
        }
        lastLeftClickAtMs = now;
        if (state.chapterSplitterLocked) {
            state.draggingChapterSplitter = false;
            TabletResizeCursor.update(false);
            return true;
        }
        state.draggingChapterSplitter = true;
        state.chapterSplitterDragStartX = (int) Math.round(mouseX);
        state.chapterSplitterStartWidth = chapterPanelWidth(state);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (state.chapterSplitterLocked) {
            state.draggingChapterSplitter = false;
            TabletResizeCursor.update(false);
            return true;
        }
        if (!state.draggingChapterSplitter) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        int dx = (int) Math.round(mouseX) - state.chapterSplitterDragStartX;
        int nextWidth = state.chapterSplitterStartWidth + dx;
        if (nextWidth <= CHAPTER_W_ICON_SNAP) {
            nextWidth = CHAPTER_W_ICON;
            state.chapterPanelCollapsed = true;
        } else {
            nextWidth = snapExpandedChapterWidth(nextWidth);
            state.chapterPanelCollapsed = false;
            state.chapterPanelLastExpandedWidth = nextWidth;
        }
        state.chapterPanelWidth = nextWidth;
        refresh.run();
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!state.draggingChapterSplitter) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        state.draggingChapterSplitter = false;
        state.chapterPanelWidth = chapterPanelWidth(state);
        state.chapterPanelCollapsed = isChapterPanelCollapsed(state);
        persistUiState(state);
        TabletResizeCursor.update(false);
        refresh.run();
        return true;
    }

    private void toggleCollapsed() {
        state.draggingChapterSplitter = false;
        if (isChapterPanelCollapsed(state)) {
            int expandedWidth = Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, state.chapterPanelLastExpandedWidth));
            state.chapterPanelWidth = expandedWidth;
            state.chapterPanelCollapsed = false;
        } else {
            state.chapterPanelLastExpandedWidth = Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, chapterPanelWidth(state)));
            state.chapterPanelWidth = CHAPTER_W_ICON;
            state.chapterPanelCollapsed = true;
        }
        persistUiState(state);
        TabletResizeCursor.update(false);
        refresh.run();
    }
}
