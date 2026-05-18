package com.abo47.questsandstuff.client.tablet.details;


import com.abo47.questsandstuff.client.tablet.layout.TabletResizeCursor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;

import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.snapExpandedChapterWidth;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_W_MAX;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_W_MIN;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.SPLITTER_W;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.persistUiState;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

final class QuestDetailsSplitterWidget extends WidgetGroup {
    private static final int MIN_DETAILS_LEFT_W = 120;

    private final TabletUiState state;
    private final Runnable refresh;

    QuestDetailsSplitterWidget(int x, TabletUiState state, Runnable refresh) {
        super(x, CHAPTER_Y, SPLITTER_W, CHAPTER_H);
        this.state = state;
        this.refresh = refresh;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = !state.questDetailsSplitterLocked && (state.questDetailsDraggingSplitter || isMouseOverElement(mouseX, mouseY));
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
        if (state.questDetailsSplitterLocked) {
            state.questDetailsDraggingSplitter = false;
            TabletResizeCursor.update(false);
            return true;
        }
        state.questDetailsDraggingSplitter = true;
        state.questDetailsSplitterDragStartX = (int) Math.round(mouseX);
        state.questDetailsSplitterStartWidth = QuestDetailsWindow.leftPanelWidth(state);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (state.questDetailsSplitterLocked) {
            state.questDetailsDraggingSplitter = false;
            TabletResizeCursor.update(false);
            return true;
        }
        if (!state.questDetailsDraggingSplitter) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        int dx = (int) Math.round(mouseX) - state.questDetailsSplitterDragStartX;
        int nextWidth = snapExpandedChapterWidth(state.questDetailsSplitterStartWidth + dx);
        state.questDetailsLeftPanelWidth = clampDetailsLeftWidth(nextWidth);
        refresh.run();
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!state.questDetailsDraggingSplitter) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        state.questDetailsDraggingSplitter = false;
        state.questDetailsLeftPanelWidth = QuestDetailsWindow.leftPanelWidth(state);
        TabletResizeCursor.update(false);
        persistUiState(state);
        refresh.run();
        return true;
    }

    static int clampDetailsLeftWidth(int width) {
        return Math.max(MIN_DETAILS_LEFT_W, Math.min(CHAPTER_W_MAX, Math.max(CHAPTER_W_MIN, width)));
    }
}
