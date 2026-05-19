package com.abo47.questsandstuff.client.tablet.animation;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class SourceOriginRevealWidget extends WidgetGroup {
    private static final long WINDOW_OPEN_MS = 190L;
    private static final float FALLBACK_SCALE = 0.96f;
    private static final float MIN_SOURCE_SCALE = 0.18f;
    private static final float MAX_SOURCE_SCALE = 0.90f;

    private final long fallbackStartMs = System.currentTimeMillis();
    private final LongSupplier startMsSupplier;
    private final Supplier<SourceRect> sourceSupplier;
    private final long durationMs;

    private SourceOriginRevealWidget(WidgetGroup content, LongSupplier startMsSupplier, Supplier<SourceRect> sourceSupplier, long durationMs) {
        super(content.getSelfPositionX(), content.getSelfPositionY(), content.getSizeWidth(), content.getSizeHeight());
        this.startMsSupplier = startMsSupplier;
        this.sourceSupplier = sourceSupplier;
        this.durationMs = durationMs;
        content.setSelfPosition(0, 0);
        addWidget(content);
    }

    public static WidgetGroup window(WidgetGroup content, LongSupplier startMsSupplier, Supplier<SourceRect> sourceSupplier) {
        return wrap(content, startMsSupplier, sourceSupplier, WINDOW_OPEN_MS);
    }

    public static WidgetGroup wrap(WidgetGroup content, LongSupplier startMsSupplier, Supplier<SourceRect> sourceSupplier, long durationMs) {
        if (content == null) {
            return new WidgetGroup(0, 0, 1, 1);
        }
        return new SourceOriginRevealWidget(content, startMsSupplier, sourceSupplier, durationMs);
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawRevealed(graphics, () -> drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawRevealed(graphics, () -> drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void drawOverlay(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawRevealed(graphics, () -> super.drawOverlay(graphics, mouseX, mouseY, partialTicks));
    }

    private void drawRevealed(GuiGraphics graphics, Runnable draw) {
        long startMs = startMs();
        if (!UiAnimationProgress.running(startMs, durationMs)) {
            draw.run();
            return;
        }

        AnimatedFrame frame = animatedFrame(startMs);
        int finalX = getPositionX();
        int finalY = getPositionY();
        graphics.pose().pushPose();
        graphics.pose().translate(frame.x() - finalX, frame.y() - finalY, 0.0F);
        graphics.pose().translate(finalX, finalY, 0.0F);
        graphics.pose().scale(frame.scale(), frame.scale(), 1.0F);
        graphics.pose().translate(-finalX, -finalY, 0.0F);
        draw.run();
        graphics.pose().popPose();
    }

    private AnimatedFrame animatedFrame(long startMs) {
        float progress = UiAnimationProgress.easedProgress(startMs, durationMs);
        int finalX = getPositionX();
        int finalY = getPositionY();
        int finalW = Math.max(1, getSizeWidth());
        int finalH = Math.max(1, getSizeHeight());
        float finalCenterX = finalX + finalW / 2.0f;
        float finalCenterY = finalY + finalH / 2.0f;

        SourceRect source = source();
        float startCenterX = source == null ? finalCenterX : source.x() + source.w() / 2.0f;
        float startCenterY = source == null ? finalCenterY + 4.0f : source.y() + source.h() / 2.0f;
        float startScale = source == null ? FALLBACK_SCALE : sourceScale(source, finalW, finalH);
        float scale = UiAnimationProgress.interpolate(startScale, 1.0f, progress);
        float centerX = UiAnimationProgress.interpolate(startCenterX, finalCenterX, progress);
        float centerY = UiAnimationProgress.interpolate(startCenterY, finalCenterY, progress);
        float x = centerX - finalW * scale / 2.0f;
        float y = centerY - finalH * scale / 2.0f;
        return new AnimatedFrame(x, y, scale);
    }

    private SourceRect source() {
        if (sourceSupplier == null) {
            return null;
        }
        SourceRect source = sourceSupplier.get();
        if (source == null || source.w() <= 0 || source.h() <= 0) {
            return null;
        }
        return source;
    }

    private float sourceScale(SourceRect source, int finalW, int finalH) {
        float widthScale = source.w() / (float) Math.max(1, finalW);
        float heightScale = source.h() / (float) Math.max(1, finalH);
        float scale = Math.min(widthScale, heightScale);
        return Math.max(MIN_SOURCE_SCALE, Math.min(MAX_SOURCE_SCALE, scale));
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

    private record AnimatedFrame(float x, float y, float scale) {
    }

    public record SourceRect(int x, int y, int w, int h) {
    }
}
