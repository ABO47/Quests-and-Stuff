package com.abo47.questsandstuff.client.tablet.quest.details;


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
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

final class QuestDetailsSplitterWidget extends WidgetGroup {
    private static final int MIN_DETAILS_LEFT_W = 120;
    private static final long HOVER_PULSE_MS = 900L;

    private final TabletUiState state;
    private final Runnable refresh;
    private boolean hoverActive;
    private long hoverPulseStartMs;

    QuestDetailsSplitterWidget(int x, TabletUiState state, Runnable refresh) {
        super(x, CHAPTER_Y, SPLITTER_W, CHAPTER_H);
        this.state = state;
        this.refresh = refresh;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = state.questDetails.questDetailsDraggingSplitter || isMouseOverElement(mouseX, mouseY);
        boolean resizeHovered = hovered && !state.questDetails.questDetailsSplitterLocked;
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
        if (state.questDetails.questDetailsSplitterLocked) {
            state.questDetails.questDetailsDraggingSplitter = false;
            TabletResizeCursor.update(false);
            return true;
        }
        state.questDetails.questDetailsDraggingSplitter = true;
        state.questDetails.questDetailsSplitterDragStartX = (int) Math.round(mouseX);
        state.questDetails.questDetailsSplitterStartWidth = QuestDetailsWindow.leftPanelWidth(state);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (state.questDetails.questDetailsSplitterLocked) {
            state.questDetails.questDetailsDraggingSplitter = false;
            TabletResizeCursor.update(false);
            return true;
        }
        if (!state.questDetails.questDetailsDraggingSplitter) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        int dx = (int) Math.round(mouseX) - state.questDetails.questDetailsSplitterDragStartX;
        int nextWidth = snapExpandedChapterWidth(state.questDetails.questDetailsSplitterStartWidth + dx);
        state.questDetails.questDetailsLeftPanelWidth = clampDetailsLeftWidth(nextWidth);
        refresh.run();
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!state.questDetails.questDetailsDraggingSplitter) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        state.questDetails.questDetailsDraggingSplitter = false;
        state.questDetails.questDetailsLeftPanelWidth = QuestDetailsWindow.leftPanelWidth(state);
        TabletResizeCursor.update(false);
        persistUiState(state);
        refresh.run();
        return true;
    }

    static int clampDetailsLeftWidth(int width) {
        return Math.max(MIN_DETAILS_LEFT_W, Math.min(CHAPTER_W_MAX, Math.max(CHAPTER_W_MIN, width)));
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
