package com.abo47.questsandstuff.client.canvas.render;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.animation.UiAnimationProgress;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeTokens;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;

public final class CanvasChapterSwitchAnimation {
    private static final long DURATION_MS = 190L;
    private static final int START_VEIL_ALPHA = 82;
    private static final int START_ACCENT_ALPHA = 190;

    private CanvasChapterSwitchAnimation() {
    }

    public static void trackSelectedGroup(TabletUiState state, String selectedGroup) {
        if (state == null) {
            return;
        }
        String previous = normalize(state.canvasChapterSwitchGroup);
        String next = normalize(selectedGroup);
        if (previous.equals(next)) {
            state.canvasChapterSwitchGroup = next;
            return;
        }

        state.canvasChapterSwitchGroup = next;
        if (previous.isBlank() || next.isBlank() || !QuestsAndStuffConfig.chapterSwitchAnimationsEnabled()) {
            clear(state);
            return;
        }

        state.canvasChapterSwitchAnimationStartMs = System.currentTimeMillis();
    }

    public static WidgetGroup wrap(TabletUiState state, WidgetGroup content) {
        if (state == null || content == null || !QuestsAndStuffConfig.chapterSwitchAnimationsEnabled()) {
            return content;
        }
        if (!UiAnimationProgress.running(state.canvasChapterSwitchAnimationStartMs, DURATION_MS)) {
            return content;
        }
        return new SoftChapterFadeWidget(content, state.canvasChapterSwitchAnimationStartMs);
    }

    public static boolean finishIfDone(TabletUiState state) {
        if (state == null || state.canvasChapterSwitchAnimationStartMs <= 0L) {
            return false;
        }
        if (QuestsAndStuffConfig.chapterSwitchAnimationsEnabled()
                && UiAnimationProgress.running(state.canvasChapterSwitchAnimationStartMs, DURATION_MS)) {
            return false;
        }
        clear(state);
        return true;
    }

    private static void clear(TabletUiState state) {
        state.canvasChapterSwitchAnimationStartMs = 0L;
        state.canvasChapterSwitchDirection = 1;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static final class SoftChapterFadeWidget extends WidgetGroup {
        private final long startMs;

        private SoftChapterFadeWidget(WidgetGroup content, long startMs) {
            super(content.getSelfPositionX(), content.getSelfPositionY(), content.getSizeWidth(), content.getSizeHeight());
            this.startMs = startMs;
            content.setSelfPosition(0, 0);
            addWidget(content);
        }

        @Override
        public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            drawAnimated(graphics, () -> drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks));
        }

        @Override
        public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks);
            drawVeil(graphics);
        }

        @Override
        public void drawOverlay(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawOverlay(graphics, mouseX, mouseY, partialTicks);
        }

        private void drawAnimated(GuiGraphics graphics, Runnable draw) {
            draw.run();
        }

        private void drawVeil(GuiGraphics graphics) {
            if (!UiAnimationProgress.running(startMs, DURATION_MS)) {
                return;
            }
            float progress = UiAnimationProgress.cubicOutProgress(startMs, DURATION_MS);
            int alpha = UiAnimationProgress.interpolate(START_VEIL_ALPHA, 0, progress);
            int accentAlpha = UiAnimationProgress.interpolate(START_ACCENT_ALPHA, 0, progress);
            int x = getPositionX();
            int y = getPositionY();
            int w = getSizeWidth();
            int h = getSizeHeight();
            if (alpha > 0) {
                graphics.fill(x, y, x + w, y + h, UiThemeTokens.withAlpha(ModColors.SURFACE_BASE, alpha));
            }
            if (accentAlpha > 0) {
                int accent = UiThemeTokens.withAlpha(ModColors.INTERACTIVE, accentAlpha);
                graphics.renderOutline(x, y, w, h, accent);
                graphics.fill(x + 1, y + 1, x + Math.max(1, w - 1), y + 2, accent);
                graphics.fill(x + 1, y + Math.max(1, h - 2), x + Math.max(1, w - 1), y + Math.max(1, h - 1), accent);
            }
        }
    }
}
