package com.abo47.questsandstuff.client.tablet.controls;

import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public final class FontSizeSliderWidget extends WidgetGroup {
    private static final int TRACK_PAD_Y = 8;
    private static final int KNOB_H = 5;

    private final int minValue;
    private final int maxValue;
    private final IntConsumer onChange;
    private final Runnable onCommit;
    private final BooleanSupplier dragCapture;
    private final Consumer<Boolean> setDragCapture;
    private int currentValue;
    private boolean dragging;

    public FontSizeSliderWidget(int x, int y, int width, int height, int minValue, int maxValue, int currentValue, IntConsumer onChange) {
        this(x, y, width, height, minValue, maxValue, currentValue, onChange, null);
    }

    public FontSizeSliderWidget(int x, int y, int width, int height, int minValue, int maxValue, int currentValue, IntConsumer onChange, Runnable onCommit) {
        this(x, y, width, height, minValue, maxValue, currentValue, onChange, onCommit, null, null);
    }

    public FontSizeSliderWidget(
            int x,
            int y,
            int width,
            int height,
            int minValue,
            int maxValue,
            int currentValue,
            IntConsumer onChange,
            Runnable onCommit,
            BooleanSupplier dragCapture,
            Consumer<Boolean> setDragCapture
    ) {
        super(x, y, width, height);
        this.minValue = Math.min(minValue, maxValue);
        this.maxValue = Math.max(minValue, maxValue);
        this.currentValue = clamp(currentValue);
        this.onChange = onChange == null ? value -> {
        } : onChange;
        this.onCommit = onCommit == null ? () -> {
        } : onCommit;
        this.dragCapture = dragCapture == null ? () -> false : dragCapture;
        this.setDragCapture = setDragCapture == null ? dragging -> {
        } : setDragCapture;
        updateTooltip();
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int left = getPositionX();
        int top = getPositionY();
        int width = getSizeWidth();
        int height = getSizeHeight();
        int trackX = left + width / 2 - 1;
        int trackTop = top + TRACK_PAD_Y;
        int trackBottom = top + height - TRACK_PAD_Y;
        int knobY = knobY(trackTop, trackBottom);
        int knobPad = Math.max(2, Math.min(5, width / 4));
        int knobLeft = left + knobPad;
        int knobRight = Math.max(knobLeft + 1, left + width - knobPad);
        int trackColor = TabletUiFactory.withAlpha(ModColors.BORDER_BASE, 180);
        int activeColor = TabletUiFactory.withAlpha(ModColors.INTERACTIVE, 220);
        int mutedColor = TabletUiFactory.withAlpha(ModColors.SURFACE_PANEL_ALT, 170);

        graphics.fill(trackX, trackTop, trackX + 2, trackBottom, trackColor);
        graphics.fill(trackX, knobY + KNOB_H / 2, trackX + 2, trackBottom, activeColor);
        graphics.fill(knobLeft, knobY, knobRight, knobY + KNOB_H, activeColor);
        if (knobRight - knobLeft > 2) {
            graphics.fill(knobLeft + 1, knobY + 1, knobRight - 1, knobY + KNOB_H - 1, mutedColor);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isMouseOverElement(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        startDragging();
        updateFromMouse(mouseY);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button != 0 || (!isDragging() && !isMouseOverElement(mouseX, mouseY))) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        startDragging();
        updateFromMouse(mouseY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!isDragging()) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        stopDragging();
        onCommit.run();
        return true;
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!isMouseOverElement(mouseX, mouseY)) {
            return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        }
        setValue(currentValue + (wheelDelta > 0 ? 1 : -1));
        onCommit.run();
        return true;
    }

    private void updateFromMouse(double mouseY) {
        int trackTop = getPositionY() + TRACK_PAD_Y;
        int trackBottom = getPositionY() + getSizeHeight() - TRACK_PAD_Y;
        double usable = Math.max(1.0, trackBottom - trackTop);
        double t = (trackBottom - mouseY) / usable;
        int next = minValue + (int) Math.round(Math.max(0.0, Math.min(1.0, t)) * (maxValue - minValue));
        setValue(next);
    }

    private boolean isDragging() {
        return dragging || dragCapture.getAsBoolean();
    }

    private void startDragging() {
        dragging = true;
        setDragCapture.accept(true);
    }

    private void stopDragging() {
        dragging = false;
        setDragCapture.accept(false);
    }

    private void setValue(int value) {
        int next = clamp(value);
        if (next == currentValue) {
            return;
        }
        currentValue = next;
        updateTooltip();
        onChange.accept(next);
    }

    private int knobY(int trackTop, int trackBottom) {
        double range = Math.max(1.0, maxValue - minValue);
        double t = (currentValue - minValue) / range;
        return trackBottom - KNOB_H / 2 - (int) Math.round(t * Math.max(1, trackBottom - trackTop));
    }

    private int clamp(int value) {
        return Math.max(minValue, Math.min(maxValue, value));
    }

    private void updateTooltip() {
        setHoverTooltips(new Component[]{
                Component.literal("Font size: " + currentValue)
        });
    }
}
