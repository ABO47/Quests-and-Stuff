package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.SliderWidget;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

final class PercentSliderWidget extends SliderWidget {
    private final IntConsumer onChange;
    private final Runnable onCommit;
    private final BooleanSupplier dragCapture;
    private final Consumer<Boolean> setDragCapture;
    private int currentValue;
    private boolean dragging;

    PercentSliderWidget(
            int x,
            int y,
            int width,
            int height,
            int currentValue,
            IntConsumer onChange,
            Runnable onCommit,
            BooleanSupplier dragCapture,
            Consumer<Boolean> setDragCapture
    ) {
        super(x, y, width, height);
        this.currentValue = clamp(currentValue);
        this.onChange = onChange == null ? value -> {
        } : onChange;
        this.onCommit = onCommit == null ? () -> {
        } : onCommit;
        this.dragCapture = dragCapture == null ? () -> false : dragCapture;
        this.setDragCapture = setDragCapture == null ? value -> {
        } : setDragCapture;

        setClientSideWidget();
        initTemplate();
        valueStep = 100;
        handleSize = 6;
        setBackground(Surfaces.bordered(withAlpha(ModColors.SURFACE_PANEL_ALT, 130), withAlpha(ModColors.BORDER_BASE, 180)));
        setHoverTexture(Surfaces.bordered(withAlpha(ModColors.INTERACTIVE, 42), withAlpha(ModColors.BORDER_ACCENT, 200)));
        handleTexture = Surfaces.bordered(withAlpha(ModColors.INTERACTIVE, 180), withAlpha(ModColors.INTERACTIVE, 235));
        handleHoverTexture = Surfaces.bordered(withAlpha(ModColors.INTERACTIVE, 230), ModColors.focusBorder());
        setOverlay(null);
        setValue(this.currentValue / 100.0f);
        setSliderCallback(this::setValueFromSlider);
        updateTooltip();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverElement(mouseX, mouseY)) {
            startDragging();
            super.mouseClicked(mouseX, mouseY, button);
            setValueFromSlider(getSliderValue());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        boolean activeDrag = button == 0 && (isDragging() || isMouseOverElement(mouseX, mouseY));
        if (!activeDrag) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        startDragging();
        super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        setValueFromSlider(getSliderValue());
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean wasDragging = isDragging();
        boolean handled = super.mouseReleased(mouseX, mouseY, button);
        if (button == 0 && wasDragging) {
            stopDragging();
            onCommit.run();
            return true;
        }
        return handled;
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!isMouseOverElement(mouseX, mouseY)) {
            return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        }
        setPercent(currentValue + (wheelDelta > 0 ? 1 : -1));
        onCommit.run();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        if (handled && setValueFromSlider(getSliderValue())) {
            onCommit.run();
        }
        return handled;
    }

    private boolean setValueFromSlider(float sliderValue) {
        int next = clamp(Math.round(Math.max(0.0f, Math.min(1.0f, sliderValue)) * 100.0f));
        if (next == currentValue) {
            return false;
        }
        currentValue = next;
        updateTooltip();
        onChange.accept(next);
        return true;
    }

    private void setPercent(int value) {
        int next = clamp(value);
        if (next == currentValue) {
            return;
        }
        currentValue = next;
        setValue(next / 100.0f);
        updateTooltip();
        onChange.accept(next);
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

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private void updateTooltip() {
        setHoverTooltips(new Component[]{Component.literal(currentValue + "%")});
    }
}
