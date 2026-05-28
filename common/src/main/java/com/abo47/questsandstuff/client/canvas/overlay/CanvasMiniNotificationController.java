package com.abo47.questsandstuff.client.canvas.overlay;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import javax.annotation.Nonnull;

public final class CanvasMiniNotificationController {
    private static final long DURATION_MS = 900L;
    private static final long FADE_MS = 260L;
    private static final int CURSOR_GAP = 2;

    private CanvasMiniNotificationController() {
    }

    public static void rememberPointer(CanvasViewport canvasViewport, TabletUiState state, int mouseX, int mouseY) {
        if (canvasViewport == null || state == null || !canvasViewport.isMouseOverElement(mouseX, mouseY)) {
            return;
        }
        state.canvasPointerX = clamp(mouseX - canvasViewport.getPositionX(), 0, Math.max(0, canvasViewport.getSizeWidth() - 1));
        state.canvasPointerY = clamp(mouseY - canvasViewport.getPositionY(), 0, Math.max(0, canvasViewport.getSizeHeight() - 1));
        state.canvasPointerKnown = true;
    }

    public static void show(TabletUiState state, String translationKey, int localX, int localY) {
        if (state == null || translationKey == null || translationKey.isBlank() || !QuestsAndStuffConfig.canvasMiniNotificationsEnabled()) {
            return;
        }
        state.canvasMiniNotificationKey = translationKey;
        state.canvasMiniNotificationX = localX;
        state.canvasMiniNotificationY = localY;
        state.canvasMiniNotificationUntilMs = System.currentTimeMillis() + DURATION_MS;
    }

    public static void showAtPointer(TabletUiState state, CanvasViewport canvasViewport, String translationKey) {
        int x = state != null && state.canvasPointerKnown ? state.canvasPointerX : Math.max(0, canvasViewport.getSizeWidth() / 2);
        int y = state != null && state.canvasPointerKnown ? state.canvasPointerY : Math.max(0, canvasViewport.getSizeHeight() / 2);
        show(state, translationKey, x, y);
    }

    public static WidgetGroup render(CanvasViewport canvasViewport, TabletUiState state) {
        if (state == null
                || state.canvasMiniNotificationKey.isBlank()
                || System.currentTimeMillis() >= state.canvasMiniNotificationUntilMs
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
        long remaining = state.canvasMiniNotificationUntilMs - System.currentTimeMillis();
        if (remaining <= 0L) {
            return;
        }
        var font = Minecraft.getInstance().font;
        String text = I18n.get(state.canvasMiniNotificationKey);
        int textW = font.width(text);
        int localX = clamp(state.canvasMiniNotificationX + CURSOR_GAP, 2, Math.max(2, viewportW - textW - 2));
        int localY = state.canvasMiniNotificationY + CURSOR_GAP;
        if (localY + font.lineHeight > viewportH - 2) {
            localY = state.canvasMiniNotificationY - font.lineHeight - CURSOR_GAP;
        }
        localY = clamp(localY, 2, Math.max(2, viewportH - font.lineHeight - 2));
        int alpha = remaining < FADE_MS ? clamp((int) (remaining * 255L / FADE_MS), 0, 255) : 255;
        graphics.drawString(font, text, originX + localX, originY + localY, TabletUiFactory.withAlpha(ModColors.SUCCESS, alpha), true);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
