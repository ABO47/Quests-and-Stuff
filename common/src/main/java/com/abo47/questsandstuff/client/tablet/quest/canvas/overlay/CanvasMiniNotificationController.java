package com.abo47.questsandstuff.client.tablet.quest.canvas.overlay;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.animation.TabletAnimationTimings;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.util.MathUtils.clamp;

public final class CanvasMiniNotificationController {
    private static final long DURATION_MS = TabletAnimationTimings.MINI_NOTIFICATION_MS;
    private static final long FADE_MS = 260L;
    private static final int CURSOR_GAP = 2;

    private CanvasMiniNotificationController() {
    }

    public static void rememberPointer(CanvasViewport canvasViewport, TabletUiState state, int mouseX, int mouseY) {
        if (canvasViewport == null || state == null || !canvasViewport.isMouseOverElement(mouseX, mouseY)) {
            return;
        }
        state.canvas.canvasPointerX = clamp(mouseX - canvasViewport.getPositionX(), 0, Math.max(0, canvasViewport.getSizeWidth() - 1));
        state.canvas.canvasPointerY = clamp(mouseY - canvasViewport.getPositionY(), 0, Math.max(0, canvasViewport.getSizeHeight() - 1));
        state.canvas.canvasPointerKnown = true;
    }

    public static void show(TabletUiState state, String translationKey, int localX, int localY) {
        if (state == null || translationKey == null || translationKey.isBlank() || !QuestsAndStuffConfig.canvasMiniNotificationsEnabled()) {
            return;
        }
        state.canvas.canvasMiniNotification.show(translationKey, localX, localY, System.currentTimeMillis(), DURATION_MS);
    }

    public static void showAtPointer(TabletUiState state, CanvasViewport canvasViewport, String translationKey) {
        int x = state != null && state.canvas.canvasPointerKnown ? state.canvas.canvasPointerX : Math.max(0, canvasViewport.getSizeWidth() / 2);
        int y = state != null && state.canvas.canvasPointerKnown ? state.canvas.canvasPointerY : Math.max(0, canvasViewport.getSizeHeight() / 2);
        show(state, translationKey, x, y);
    }

    public static WidgetGroup render(CanvasViewport canvasViewport, TabletUiState state) {
        if (state == null
                || !state.canvas.canvasMiniNotification.active(System.currentTimeMillis())
                || !QuestsAndStuffConfig.canvasMiniNotificationsEnabled()) {
            return null;
        }
        WidgetGroup notice = new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                draw(graphics, state, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            }
        };
        notice.setActive(false);
        return notice;
    }

    private static void draw(GuiGraphics graphics, TabletUiState state, int originX, int originY, int viewportW, int viewportH) {
        long remaining = state.canvas.canvasMiniNotification.untilMs() - System.currentTimeMillis();
        if (remaining <= 0L) {
            return;
        }
        var font = Minecraft.getInstance().font;
        String text = I18n.get(state.canvas.canvasMiniNotification.translationKey());
        int textW = font.width(text);
        int localX = clamp(state.canvas.canvasMiniNotification.x() + CURSOR_GAP, 2, Math.max(2, viewportW - textW - 2));
        int localY = state.canvas.canvasMiniNotification.y() + CURSOR_GAP;
        if (localY + font.lineHeight > viewportH - 2) {
            localY = state.canvas.canvasMiniNotification.y() - font.lineHeight - CURSOR_GAP;
        }
        localY = clamp(localY, 2, Math.max(2, viewportH - font.lineHeight - 2));
        int alpha = remaining < FADE_MS ? clamp((int) (remaining * 255L / FADE_MS), 0, 255) : 255;
        graphics.drawString(font, text, originX + localX, originY + localY, withAlpha(TabletColors.SUCCESS, alpha), true);
    }

}
