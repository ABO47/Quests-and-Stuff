package com.abo47.questsandstuff.client.tablet.controls;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class DragScrollBarWidget extends WidgetGroup {
    public static final int WIDTH = 4;
    public static final int RESERVED_WIDTH = 6;
    private static final int RAIL_WIDTH = 2;

    private final IntSupplier valueSupplier;
    private final IntSupplier maxSupplier;
    private final IntSupplier knobHeightSupplier;
    private final IntConsumer valueConsumer;
    private final BooleanSupplier draggingSupplier;
    private final Consumer<Boolean> draggingConsumer;
    private final Runnable refresh;
    private final int trackColor;
    private final int knobColor;
    private final int activeKnobColor;

    public DragScrollBarWidget(
            int x,
            int y,
            int width,
            int height,
            IntSupplier valueSupplier,
            IntSupplier maxSupplier,
            IntSupplier knobHeightSupplier,
            IntConsumer valueConsumer,
            BooleanSupplier draggingSupplier,
            Consumer<Boolean> draggingConsumer,
            Runnable refresh,
            int trackColor,
            int knobColor,
            int activeKnobColor
    ) {
        super(x, y, width, height);
        this.valueSupplier = valueSupplier;
        this.maxSupplier = maxSupplier;
        this.knobHeightSupplier = knobHeightSupplier;
        this.valueConsumer = valueConsumer;
        this.draggingSupplier = draggingSupplier;
        this.draggingConsumer = draggingConsumer;
        this.refresh = refresh;
        this.trackColor = trackColor;
        this.knobColor = knobColor;
        this.activeKnobColor = activeKnobColor;
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        int railW = Math.min(RAIL_WIDTH, Math.max(1, w));
        int railX = x + Math.max(0, (w - railW) / 2);
        graphics.fill(railX, y, railX + railW, y + h, trackColor);

        int knobH = knobHeight();
        int max = Math.max(0, maxSupplier.getAsInt());
        int current = ScrollController.clamp(valueSupplier.getAsInt(), max);
        int span = Math.max(0, h - knobH);
        int knobY = y + (max <= 0 || span <= 0 ? 0 : Math.round((float) span * ((float) current / (float) max)));
        graphics.fill(x, knobY, x + w, knobY + knobH, draggingSupplier.getAsBoolean() ? activeKnobColor : knobColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isMouseOverElement(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        draggingConsumer.accept(true);
        updateFromMouse(mouseY);
        refresh.run();
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!draggingSupplier.getAsBoolean()) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        if (updateFromMouse(mouseY)) {
            refresh.run();
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!draggingSupplier.getAsBoolean()) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        draggingConsumer.accept(false);
        refresh.run();
        return true;
    }

    private boolean updateFromMouse(double mouseY) {
        int current = valueSupplier.getAsInt();
        int next = ScrollController.byMouse(
                (int) Math.round(mouseY),
                getPositionY(),
                getSizeHeight(),
                knobHeight(),
                maxSupplier.getAsInt()
        );
        if (next == current) {
            return false;
        }
        valueConsumer.accept(next);
        return true;
    }

    private int knobHeight() {
        int h = getSizeHeight();
        return Math.max(1, Math.min(h, knobHeightSupplier.getAsInt()));
    }
}
