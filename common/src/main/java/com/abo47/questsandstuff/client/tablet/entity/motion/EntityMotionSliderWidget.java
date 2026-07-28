package com.abo47.questsandstuff.client.tablet.entity.motion;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

final class EntityMotionSliderWidget extends WidgetGroup {
    private static final int TRACK_PAD_X = GRID_6;
    private static final int KNOB_W = GRID_5;

    private final int minValue;
    private final int maxValue;
    private final IntConsumer onChange;
    private final Runnable onCommit;
    private final BooleanSupplier dragCapture;
    private final Consumer<Boolean> setDragCapture;
    private int currentValue;
    private boolean dragging;

    EntityMotionSliderWidget(int x, int y, int width, int height, int minValue, int maxValue, int currentValue, IntConsumer onChange, Runnable onCommit, BooleanSupplier dragCapture, Consumer<Boolean> setDragCapture) {
        super(x, y, width, height);
        this.minValue = Math.min(minValue, maxValue);
        this.maxValue = Math.max(minValue, maxValue);
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
        IGuiTexture skinBg = getBackgroundTexture();
        if (skinBg != null && !skinBg.equals(IGuiTexture.EMPTY)) {
            skinBg.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        }

        int left = getPositionX();
        int top = getPositionY();
        int width = getSizeWidth();
        int height = getSizeHeight();
        int trackLeft = left + TRACK_PAD_X;
        int trackRight = left + width - TRACK_PAD_X;
        int trackY = top + height / 2 - 1;
        int knobX = knobX(trackLeft, trackRight);
        int trackColor = withAlpha(TabletColors.BORDER_BASE, 180);
        int activeColor = withAlpha(TabletColors.INTERACTIVE, 220);
        int mutedColor = withAlpha(TabletColors.SURFACE_PANEL_ALT, 180);

        SurfaceFactory.fill(trackColor).draw(graphics, 0, 0, trackLeft, trackY, trackRight - trackLeft, 2);
        SurfaceFactory.fill(activeColor).draw(graphics, 0, 0, trackLeft, trackY, knobX + KNOB_W / 2 - trackLeft, 2);
        SurfaceFactory.fill(activeColor).draw(graphics, 0, 0, knobX, top + 3, KNOB_W, height - 6);
        if (KNOB_W > 2) {
            SurfaceFactory.fill(mutedColor).draw(graphics, 0, 0, knobX + 1, top + 4, KNOB_W - 2, height - 8);
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
        int next = minValue + (int) Math.round(Math.max(0.0, Math.min(1.0, t)) * (maxValue - minValue));
        setValue(next);
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
        double range = Math.max(1.0, maxValue - minValue);
        double t = (currentValue - minValue) / range;
        return trackLeft - KNOB_W / 2 + (int) Math.round(t * Math.max(1, trackRight - trackLeft));
    }

    private int clamp(int value) {
        return Math.max(minValue, Math.min(maxValue, value));
    }

    private void updateTooltip() {
        setHoverTooltips(new Component[]{Component.literal(Integer.toString(currentValue))});
    }
}
