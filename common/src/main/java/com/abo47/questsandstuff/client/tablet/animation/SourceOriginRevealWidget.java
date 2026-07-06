package com.abo47.questsandstuff.client.tablet.animation;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class SourceOriginRevealWidget extends WidgetGroup {
    public static final long WINDOW_OPEN_MS = 190L;
    private static final float FALLBACK_SCALE = 0.96f;
    private static final float MIN_SOURCE_SCALE = 0.18f;
    private static final float MAX_SOURCE_SCALE = 0.90f;
    private static final float CLIP_END_PROGRESS = 0.32f;
    private static final float OVERSHOOT_START = 0.68f;
    private static final float OVERSHOOT_AMOUNT = 0.012f;
    private static final int SHADOW_ALPHA = 86;
    private static final int SOFT_SHADOW_ALPHA = 42;

    private final long fallbackStartMs = System.currentTimeMillis();
    private final LongSupplier startMsSupplier;
    private final BooleanSupplier openingSupplier;
    private final Supplier<SourceRect> sourceSupplier;
    private final long durationMs;
    private final boolean shadowEnabled;

    private SourceOriginRevealWidget(WidgetGroup content, LongSupplier startMsSupplier, BooleanSupplier openingSupplier, Supplier<SourceRect> sourceSupplier, long durationMs, boolean shadowEnabled) {
        super(content.getSelfPositionX(), content.getSelfPositionY(), content.getSizeWidth(), content.getSizeHeight());
        this.startMsSupplier = startMsSupplier;
        this.openingSupplier = openingSupplier;
        this.sourceSupplier = sourceSupplier;
        this.durationMs = durationMs;
        this.shadowEnabled = shadowEnabled;
        content.setSelfPosition(0, 0);
        addWidget(content);
    }

    public static WidgetGroup window(WidgetGroup content, LongSupplier startMsSupplier, BooleanSupplier openingSupplier, Supplier<SourceRect> sourceSupplier) {
        return wrap(content, startMsSupplier, openingSupplier, sourceSupplier, WINDOW_OPEN_MS);
    }

    public static WidgetGroup windowNoShadow(WidgetGroup content, LongSupplier startMsSupplier, BooleanSupplier openingSupplier, Supplier<SourceRect> sourceSupplier) {
        return wrap(content, startMsSupplier, openingSupplier, sourceSupplier, WINDOW_OPEN_MS, false);
    }

    public static WidgetGroup wrap(WidgetGroup content, LongSupplier startMsSupplier, BooleanSupplier openingSupplier, Supplier<SourceRect> sourceSupplier, long durationMs) {
        return wrap(content, startMsSupplier, openingSupplier, sourceSupplier, durationMs, true);
    }

    public static WidgetGroup wrap(WidgetGroup content, LongSupplier startMsSupplier, BooleanSupplier openingSupplier, Supplier<SourceRect> sourceSupplier, long durationMs, boolean shadowEnabled) {
        if (content == null) {
            return new WidgetGroup(0, 0, 1, 1);
        }
        return new SourceOriginRevealWidget(content, startMsSupplier, openingSupplier, sourceSupplier, durationMs, shadowEnabled);
    }

    public static boolean windowRunning(long startMs) {
        return UiAnimationProgress.running(startMs, WINDOW_OPEN_MS);
    }

    public static float windowOpenAmount(long startMs, boolean opening) {
        if (!UiAnimationProgress.running(startMs, WINDOW_OPEN_MS)) {
            return opening ? 1.0f : 0.0f;
        }
        float progress = UiAnimationProgress.cubicOutProgress(startMs, WINDOW_OPEN_MS);
        return opening ? progress : 1.0f - progress;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawRevealed(graphics, true, () -> drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawRevealed(graphics, false, () -> drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void drawOverlay(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawRevealed(graphics, false, () -> super.drawOverlay(graphics, mouseX, mouseY, partialTicks));
    }

    private void drawRevealed(GuiGraphics graphics, boolean drawShadow, Runnable draw) {
        long startMs = startMs();
        if (!UiAnimationProgress.running(startMs, durationMs) && opening()) {
            if (drawShadow && shadowEnabled) {
                drawShadow(graphics, 1.0f);
            }
            draw.run();
            return;
        }

        AnimatedFrame frame = animatedFrame(startMs);
        if (frame.openAmount() <= 0.01f) {
            return;
        }
        ClipRect clip = clipRect(frame);
        boolean clipped = clip != null && enableClip(graphics, clip);
        int finalX = getPositionX();
        int finalY = getPositionY();
        graphics.pose().pushPose();
        graphics.pose().translate(frame.x() - finalX, frame.y() - finalY, 0.0F);
        graphics.pose().translate(finalX, finalY, 0.0F);
        graphics.pose().scale(frame.scale(), frame.scale(), 1.0F);
        graphics.pose().translate(-finalX, -finalY, 0.0F);
        if (drawShadow && shadowEnabled) {
            drawShadow(graphics, frame.openAmount());
        }
        draw.run();
        graphics.pose().popPose();
        if (clipped) {
            graphics.disableScissor();
        }
    }

    private AnimatedFrame animatedFrame(long startMs) {
        float rawProgress = UiAnimationProgress.linearProgress(startMs, durationMs);
        boolean opening = opening();
        float openAmount = opening ? UiAnimationProgress.cubicOut(rawProgress) : 1.0f - UiAnimationProgress.cubicOut(rawProgress);
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
        float scale = UiAnimationProgress.interpolate(startScale, 1.0f, openAmount) + openingOvershoot(rawProgress, opening);
        float centerX = UiAnimationProgress.interpolate(startCenterX, finalCenterX, openAmount);
        float centerY = UiAnimationProgress.interpolate(startCenterY, finalCenterY, openAmount);
        float x = centerX - finalW * scale / 2.0f;
        float y = centerY - finalH * scale / 2.0f;
        return new AnimatedFrame(x, y, scale, openAmount, source);
    }

    private float openingOvershoot(float rawProgress, boolean opening) {
        if (!opening || rawProgress <= OVERSHOOT_START || rawProgress >= 1.0f) {
            return 0.0f;
        }
        float t = (rawProgress - OVERSHOOT_START) / Math.max(0.01f, 1.0f - OVERSHOOT_START);
        return (float) Math.sin(t * Math.PI) * OVERSHOOT_AMOUNT;
    }

    private void drawShadow(GuiGraphics graphics, float openAmount) {
        int hardAlpha = Math.round(SHADOW_ALPHA * openAmount);
        int softAlpha = Math.round(SOFT_SHADOW_ALPHA * openAmount);
        if (hardAlpha <= 0 && softAlpha <= 0) {
            return;
        }
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        if (softAlpha > 0) {
            SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, softAlpha)).draw(graphics, 0, 0, x + 4, y + 5, w, h);
        }
        if (hardAlpha > 0) {
            SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, hardAlpha)).draw(graphics, 0, 0, x + 2, y + 3, w, h);
        }
    }

    private ClipRect clipRect(AnimatedFrame frame) {
        SourceRect source = frame.source();
        if (source == null || frame.openAmount() >= CLIP_END_PROGRESS) {
            return null;
        }
        float clipProgress = Math.max(0.0f, Math.min(1.0f, frame.openAmount() / CLIP_END_PROGRESS));
        int finalX = getPositionX();
        int finalY = getPositionY();
        int finalRight = finalX + getSizeWidth();
        int finalBottom = finalY + getSizeHeight();
        int left = UiAnimationProgress.interpolate(source.x(), finalX, clipProgress);
        int top = UiAnimationProgress.interpolate(source.y(), finalY, clipProgress);
        int right = UiAnimationProgress.interpolate(source.x() + source.w(), finalRight, clipProgress);
        int bottom = UiAnimationProgress.interpolate(source.y() + source.h(), finalBottom, clipProgress);
        if (right <= left || bottom <= top) {
            return null;
        }
        return new ClipRect(left, top, right, bottom);
    }

    private boolean enableClip(GuiGraphics graphics, ClipRect clip) {
        var trans = graphics.pose().last().pose();
        var topLeft = trans.transform(new Vector4f(clip.left(), clip.top(), 0, 1));
        var bottomRight = trans.transform(new Vector4f(clip.right(), clip.bottom(), 0, 1));
        int left = Math.round(Math.min(topLeft.x, bottomRight.x));
        int top = Math.round(Math.min(topLeft.y, bottomRight.y));
        int right = Math.round(Math.max(topLeft.x, bottomRight.x));
        int bottom = Math.round(Math.max(topLeft.y, bottomRight.y));
        if (right <= left || bottom <= top) {
            return false;
        }
        graphics.enableScissor(left, top, right, bottom);
        return true;
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

    private boolean opening() {
        return openingSupplier == null || openingSupplier.getAsBoolean();
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

    private record AnimatedFrame(float x, float y, float scale, float openAmount, SourceRect source) {
    }

    private record ClipRect(int left, int top, int right, int bottom) {
    }

    public record SourceRect(int x, int y, int w, int h) {
    }
}
