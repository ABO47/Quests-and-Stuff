package com.abo47.questsandstuff.client.tablet.animation;

import static com.abo47.questsandstuff.client.tablet.theme.render.Surfaces.withAlpha;

import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.render.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

public final class AnchoredMenuRevealWidget extends WidgetGroup {
    private static final long MENU_MS = 165L;
    private static final int SLIDE_PX = 4;
    private static final int SHADOW_ALPHA = 72;
    private static final int SOFT_SHADOW_ALPHA = 34;
    private static final int VEIL_ALPHA = 54;

    private final long fallbackStartMs = System.currentTimeMillis();
    private final LongSupplier startMsSupplier;
    private final BooleanSupplier openingSupplier;

    private AnchoredMenuRevealWidget(WidgetGroup content, LongSupplier startMsSupplier, BooleanSupplier openingSupplier) {
        super(content.getSelfPositionX(), content.getSelfPositionY(), content.getSizeWidth(), content.getSizeHeight());
        this.startMsSupplier = startMsSupplier;
        this.openingSupplier = openingSupplier == null ? () -> true : openingSupplier;
        content.setSelfPosition(0, 0);
        addWidget(content);
    }

    public static WidgetGroup tools(WidgetGroup content, LongSupplier startMsSupplier, BooleanSupplier openingSupplier) {
        if (content == null) {
            return new WidgetGroup(0, 0, 1, 1);
        }
        return new AnchoredMenuRevealWidget(content, startMsSupplier, openingSupplier);
    }

    public static boolean running(long startMs) {
        return UiAnimationProgress.running(startMs, MENU_MS);
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawAnchored(graphics, false, true, () -> drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawAnchored(graphics, false, true, () -> drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void drawOverlay(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawAnchored(graphics, false, false, () -> super.drawOverlay(graphics, mouseX, mouseY, partialTicks));
    }

    private void drawAnchored(GuiGraphics graphics, boolean drawShadow, boolean drawVeil, Runnable draw) {
        Frame frame = frame();
        if (frame.amount() <= 0.01f || frame.visibleHeight() <= 0) {
            return;
        }
        if (drawShadow) {
            drawShadow(graphics, frame);
        }
        boolean clipped = enableClip(graphics, getPositionX(), getPositionY(), getPositionX() + getSizeWidth(), getPositionY() + frame.visibleHeight());
        graphics.pose().pushPose();
        graphics.pose().translate(0.0f, frame.offsetY(), 0.0f);
        draw.run();
        if (drawVeil) {
            drawVeil(graphics, frame);
        }
        graphics.pose().popPose();
        if (clipped) {
            graphics.disableScissor();
        }
    }

    private Frame frame() {
        long startMs = startMs();
        boolean opening = openingSupplier.getAsBoolean();
        if (!UiAnimationProgress.running(startMs, MENU_MS)) {
            float amount = opening ? 1.0f : 0.0f;
            return new Frame(amount, visibleHeight(amount), offsetY(amount), 1.0f - amount);
        }
        float raw = UiAnimationProgress.linearProgress(startMs, MENU_MS);
        float eased = raw * raw * (3.0f - 2.0f * raw);
        float amount = opening ? UiAnimationProgress.cubicOut(raw) : 1.0f - eased;
        return new Frame(amount, visibleHeight(amount), offsetY(amount), 1.0f - amount);
    }

    private int visibleHeight(float amount) {
        return UiAnimationProgress.interpolate(0, getSizeHeight(), amount);
    }

    private float offsetY(float amount) {
        return UiAnimationProgress.interpolate(-SLIDE_PX, 0.0f, amount);
    }

    private void drawShadow(GuiGraphics graphics, Frame frame) {
        int hardAlpha = Math.round(SHADOW_ALPHA * frame.amount());
        int softAlpha = Math.round(SOFT_SHADOW_ALPHA * frame.amount());
        if (hardAlpha <= 0 && softAlpha <= 0) {
            return;
        }
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = frame.visibleHeight();
        if (softAlpha > 0) {
            Surfaces.fill(withAlpha(ModColors.SURFACE_BASE, softAlpha)).draw(graphics, 0, 0, x + 3, y + 4, w, h);
        }
        if (hardAlpha > 0) {
            Surfaces.fill(withAlpha(ModColors.SURFACE_BASE, hardAlpha)).draw(graphics, 0, 0, x + 1, y + 2, w, h);
        }
    }

    private void drawVeil(GuiGraphics graphics, Frame frame) {
        int alpha = Math.round(VEIL_ALPHA * frame.veilAmount());
        if (alpha <= 0) {
            return;
        }
        int x = getPositionX();
        int y = getPositionY();
        Surfaces.fill(withAlpha(ModColors.SURFACE_BASE, alpha)).draw(graphics, 0, 0, x, y, getSizeWidth(), getSizeHeight());
    }

    private boolean enableClip(GuiGraphics graphics, int left, int top, int right, int bottom) {
        var trans = graphics.pose().last().pose();
        var topLeft = trans.transform(new Vector4f(left, top, 0, 1));
        var bottomRight = trans.transform(new Vector4f(right, bottom, 0, 1));
        int clipLeft = Math.round(Math.min(topLeft.x, bottomRight.x));
        int clipTop = Math.round(Math.min(topLeft.y, bottomRight.y));
        int clipRight = Math.round(Math.max(topLeft.x, bottomRight.x));
        int clipBottom = Math.round(Math.max(topLeft.y, bottomRight.y));
        if (clipRight <= clipLeft || clipBottom <= clipTop) {
            return false;
        }
        graphics.enableScissor(clipLeft, clipTop, clipRight, clipBottom);
        return true;
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

    private record Frame(float amount, int visibleHeight, float offsetY, float veilAmount) {
    }
}
