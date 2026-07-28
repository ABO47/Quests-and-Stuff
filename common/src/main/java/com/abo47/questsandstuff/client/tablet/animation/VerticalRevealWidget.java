package com.abo47.questsandstuff.client.tablet.animation;

import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class VerticalRevealWidget extends WidgetGroup {
    private static final long SHEET_OPEN_MS = TabletAnimationTimings.SHEET_OPEN_MS;

    private final long fallbackStartMs = System.currentTimeMillis();
    private final LongSupplier startMsSupplier;
    private final BooleanSupplier openingSupplier;
    private final long durationMs;

    private VerticalRevealWidget(WidgetGroup content, LongSupplier startMsSupplier, BooleanSupplier openingSupplier, long durationMs) {
        super(content.getSelfPositionX(), content.getSelfPositionY(), content.getSizeWidth(), content.getSizeHeight());
        this.startMsSupplier = startMsSupplier;
        this.openingSupplier = openingSupplier == null ? () -> true : openingSupplier;
        this.durationMs = durationMs;
        content.setSelfPosition(0, 0);
        addWidget(content);
    }

    public static WidgetGroup sheet(WidgetGroup content, long startMs) {
        return wrap(content, () -> startMs, SHEET_OPEN_MS);
    }

    public static WidgetGroup sheet(WidgetGroup content, long startMs, BooleanSupplier openingSupplier) {
        return wrap(content, () -> startMs, openingSupplier, SHEET_OPEN_MS);
    }

    public static WidgetGroup wrap(WidgetGroup content, LongSupplier startMsSupplier, long durationMs) {
        return wrap(content, startMsSupplier, () -> true, durationMs);
    }

    public static WidgetGroup wrap(WidgetGroup content, LongSupplier startMsSupplier, BooleanSupplier openingSupplier, long durationMs) {
        if (content == null) {
            return new WidgetGroup(0, 0, 1, 1);
        }
        return new VerticalRevealWidget(content, startMsSupplier, openingSupplier, durationMs);
    }

    public static boolean sheetRunning(long startMs) {
        return UiAnimationProgress.running(startMs, SHEET_OPEN_MS);
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawSheet(graphics, () -> drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawSheet(graphics, () -> drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void drawOverlay(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawSheet(graphics, () -> super.drawOverlay(graphics, mouseX, mouseY, partialTicks));
    }

    private void drawSheet(GuiGraphics graphics, Runnable draw) {
        int visibleH = visibleHeight();
        if (visibleH <= 0) {
            return;
        }
        if (visibleH >= getSizeHeight()) {
            draw.run();
            return;
        }
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        var trans = graphics.pose().last().pose();
        var topLeft = trans.transform(new Vector4f(x, y, 0, 1));
        var bottomRight = trans.transform(new Vector4f(x + w, y + visibleH, 0, 1));
        graphics.enableScissor((int) topLeft.x, (int) topLeft.y, (int) bottomRight.x, (int) bottomRight.y);
        draw.run();
        graphics.disableScissor();
    }

    private int visibleHeight() {
        long startMs = startMs();
        boolean opening = openingSupplier.getAsBoolean();
        float progress = UiAnimationProgress.openProgress(opening, opening, startMs, durationMs);
        return UiAnimationProgress.interpolate(0, getSizeHeight(), progress);
    }

    private long startMs() {
        if (startMsSupplier != null) {
            long suppliedStartMs = startMsSupplier.getAsLong();
            if (suppliedStartMs > 0L) {
                return suppliedStartMs;
            }
        }
        return fallbackStartMs;
    }
}
