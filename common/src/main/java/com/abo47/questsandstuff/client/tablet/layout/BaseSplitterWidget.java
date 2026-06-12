package com.abo47.questsandstuff.client.tablet.layout;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;

import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.persistUiState;

public abstract class BaseSplitterWidget extends WidgetGroup {
    private static final long HOVER_PULSE_MS = 900L;

    protected final TabletUiState state;
    protected final Runnable refresh;
    private boolean hoverActive;
    private long hoverPulseStartMs;

    public BaseSplitterWidget(int x, int y, int w, int h, TabletUiState state, Runnable refresh) {
        super(x, y, w, h);
        this.state = state;
        this.refresh = refresh;
    }

    protected abstract boolean isSplitterLocked();
    protected abstract boolean isSplitterDragging();
    protected abstract void setSplitterDragging(boolean dragging);
    protected abstract int splitterDragStartX();
    protected abstract void setSplitterDragStartX(int x);
    protected abstract int splitterStartWidth();
    protected abstract void setSplitterStartWidth(int width);
    protected abstract int getLeftPanelWidth();
    protected abstract void setLeftPanelWidth(int width);

    protected int dragThresholdPx() {
        return 0;
    }

    protected boolean hasDragMoved() {
        return true;
    }

    protected void setDragMoved(boolean moved) {
    }

    protected int computeDragWidth(int rawWidth) {
        return rawWidth;
    }

    protected boolean canCollapseOnDrag() {
        return false;
    }

    protected int collapseThresholdWidth() {
        return 0;
    }

    protected int collapsedPanelWidth() {
        return 0;
    }

    protected boolean isPanelCollapsed() {
        return false;
    }

    protected void setPanelCollapsed(boolean collapsed) {
    }

    protected void onSplitterClick() {
    }

    protected void onSplitterRelease() {
        persistUiState(state);
    }

    protected boolean cancelDragOnClickLocked() {
        return false;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = isSplitterDragging() || isMouseOverElement(mouseX, mouseY);
        boolean resizeHovered = hovered && !isSplitterLocked();
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
        if (cancelDragOnClickLocked() && isSplitterLocked()) {
            setSplitterDragging(false);
            TabletResizeCursor.update(false);
            return true;
        }
        setSplitterDragging(true);
        setDragMoved(false);
        setSplitterDragStartX((int) Math.round(mouseX));
        setSplitterStartWidth(getLeftPanelWidth());
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isSplitterLocked()) {
            TabletResizeCursor.update(false);
            return true;
        }
        if (!isSplitterDragging()) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        int dx = (int) Math.round(mouseX) - splitterDragStartX();
        int threshold = dragThresholdPx();
        if (threshold > 0 && !hasDragMoved() && Math.abs(dx) <= threshold) {
            return true;
        }
        setDragMoved(true);
        int rawWidth = splitterStartWidth() + dx;
        int nextWidth = resolveDragWidth(rawWidth);
        setLeftPanelWidth(nextWidth);
        refresh.run();
        return true;
    }

    private int resolveDragWidth(int rawWidth) {
        if (canCollapseOnDrag() && rawWidth <= collapseThresholdWidth()) {
            setPanelCollapsed(true);
            return collapsedPanelWidth();
        }
        setPanelCollapsed(false);
        return computeDragWidth(rawWidth);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!isSplitterDragging()) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        setSplitterDragging(false);
        boolean moved = hasDragMoved();
        setDragMoved(false);
        if (!moved) {
            onSplitterClick();
            return true;
        }
        setLeftPanelWidth(getLeftPanelWidth());
        onSplitterRelease();
        TabletResizeCursor.update(false);
        refresh.run();
        return true;
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
