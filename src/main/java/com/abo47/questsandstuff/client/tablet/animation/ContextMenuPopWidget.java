package com.abo47.questsandstuff.client.tablet.animation;

import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeTokens;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.LongSupplier;

public final class ContextMenuPopWidget extends WidgetGroup {
    private static final long MENU_MS = 95L;
    private static final float START_SCALE = 0.97f;
    private static final int SHADOW_ALPHA = 58;
    private static final int VEIL_ALPHA = 48;

    private final long fallbackStartMs = System.currentTimeMillis();
    private final LongSupplier startMsSupplier;

    private ContextMenuPopWidget(WidgetGroup content, LongSupplier startMsSupplier) {
        super(content.getSelfPositionX(), content.getSelfPositionY(), content.getSizeWidth(), content.getSizeHeight());
        this.startMsSupplier = startMsSupplier;
        content.setSelfPosition(0, 0);
        addWidget(content);
    }

    public static WidgetGroup menu(WidgetGroup content, LongSupplier startMsSupplier) {
        if (content == null) {
            return new WidgetGroup(0, 0, 1, 1);
        }
        return new ContextMenuPopWidget(content, startMsSupplier);
    }

    public static boolean running(long startMs) {
        return UiAnimationProgress.running(startMs, MENU_MS);
    }

    public static long durationMs() {
        return MENU_MS;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawPopped(graphics, true, true, () -> drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawPopped(graphics, false, true, () -> drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void drawOverlay(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawPopped(graphics, false, false, () -> super.drawOverlay(graphics, mouseX, mouseY, partialTicks));
    }

    private void drawPopped(GuiGraphics graphics, boolean drawShadow, boolean drawVeil, Runnable draw) {
        float amount = openAmount();
        if (amount <= 0.01f) {
            return;
        }
        float scale = UiAnimationProgress.interpolate(START_SCALE, 1.0f, amount);
        int x = getPositionX();
        int y = getPositionY();
        if (drawShadow) {
            drawShadow(graphics, amount);
        }
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.pose().translate(-x, -y, 0.0f);
        draw.run();
        if (drawVeil) {
            drawVeil(graphics, amount);
        }
        graphics.pose().popPose();
    }

    private float openAmount() {
        long startMs = startMs();
        if (!UiAnimationProgress.running(startMs, MENU_MS)) {
            return 1.0f;
        }
        return UiAnimationProgress.cubicOutProgress(startMs, MENU_MS);
    }

    private void drawShadow(GuiGraphics graphics, float amount) {
        int alpha = Math.round(SHADOW_ALPHA * amount);
        if (alpha <= 0) {
            return;
        }
        int x = getPositionX();
        int y = getPositionY();
        graphics.fill(x + 2, y + 3, x + getSizeWidth() + 2, y + getSizeHeight() + 3, UiThemeTokens.withAlpha(ModColors.SURFACE_BASE, alpha));
    }

    private void drawVeil(GuiGraphics graphics, float amount) {
        int alpha = Math.round(VEIL_ALPHA * (1.0f - amount));
        if (alpha <= 0) {
            return;
        }
        int x = getPositionX();
        int y = getPositionY();
        graphics.fill(x, y, x + getSizeWidth(), y + getSizeHeight(), UiThemeTokens.withAlpha(ModColors.SURFACE_BASE, alpha));
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
