package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

final class SoundVolumeSliderWidget extends WidgetGroup {
    private static final int TRACK_PAD_X = 5;
    private static final int KNOB_W = 5;

    private final IntConsumer onChange;
    private final Runnable onCommit;
    private final BooleanSupplier dragCapture;
    private final Consumer<Boolean> setDragCapture;
    private int currentValue;
    private boolean dragging;

    SoundVolumeSliderWidget(
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
        updateTooltip();
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int left = getPositionX();
        int top = getPositionY();
        int width = getSizeWidth();
        int height = getSizeHeight();
        int trackLeft = left + TRACK_PAD_X;
        int trackRight = left + width - TRACK_PAD_X;
        int trackY = top + height / 2 - 1;
        int knobX = knobX(trackLeft, trackRight);
        int trackColor = withAlpha(ModColors.BORDER_BASE, 170);
        int activeColor = withAlpha(ModColors.INTERACTIVE, 220);
        int mutedColor = withAlpha(ModColors.SURFACE_PANEL_ALT, 190);

        graphics.fill(trackLeft, trackY, trackRight, trackY + 2, trackColor);
        graphics.fill(trackLeft, trackY, knobX + KNOB_W / 2, trackY + 2, activeColor);
        graphics.fill(knobX, top + 3, knobX + KNOB_W, top + height - 3, activeColor);
        if (KNOB_W > 2) {
            graphics.fill(knobX + 1, top + 4, knobX + KNOB_W - 1, top + height - 4, mutedColor);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isMouseOverElement(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        startDragging();
        updateFromMouse(mouseX);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button != 0 || (!isDragging() && !isMouseOverElement(mouseX, mouseY))) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        startDragging();
        updateFromMouse(mouseX);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!isDragging()) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        dragging = false;
        setDragCapture.accept(false);
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

    private void updateFromMouse(double mouseX) {
        int trackLeft = getPositionX() + TRACK_PAD_X;
        int trackRight = getPositionX() + getSizeWidth() - TRACK_PAD_X;
        double usable = Math.max(1.0, trackRight - trackLeft);
        double t = (mouseX - trackLeft) / usable;
        setValue((int) Math.round(Math.max(0.0, Math.min(1.0, t)) * 100.0));
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

    private boolean isDragging() {
        return dragging || dragCapture.getAsBoolean();
    }

    private void startDragging() {
        dragging = true;
        setDragCapture.accept(true);
    }

    private int knobX(int trackLeft, int trackRight) {
        return trackLeft - KNOB_W / 2 + (int) Math.round((currentValue / 100.0) * Math.max(1, trackRight - trackLeft));
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private void updateTooltip() {
        setHoverTooltips(new Component[]{Component.literal(currentValue + "%")});
    }
}
