package com.abo47.questsandstuff.client.canvas.render;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.animation.UiAnimationProgress;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;
import java.util.List;

public final class CanvasChapterSwitchAnimation {
    private static final long DURATION_MS = 230L;
    private static final float START_ALPHA = 0.74f;
    private static final float START_SCALE = 0.985f;
    private static final float SLIDE_PX = 5.0f;

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

        state.canvasChapterSwitchDirection = direction(previous, next);
        state.canvasChapterSwitchAnimationStartMs = System.currentTimeMillis();
    }

    public static WidgetGroup wrap(TabletUiState state, WidgetGroup content) {
        if (state == null || content == null || !QuestsAndStuffConfig.chapterSwitchAnimationsEnabled()) {
            return content;
        }
        if (!UiAnimationProgress.running(state.canvasChapterSwitchAnimationStartMs, DURATION_MS)) {
            return content;
        }
        return new SlideFadeWidget(content, state.canvasChapterSwitchAnimationStartMs, state.canvasChapterSwitchDirection);
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

    private static int direction(String previous, String next) {
        List<String> groups = ClientQuestCache.groupOrder();
        int previousIndex = groups.indexOf(previous);
        int nextIndex = groups.indexOf(next);
        if (previousIndex >= 0 && nextIndex >= 0 && previousIndex != nextIndex) {
            return nextIndex > previousIndex ? 1 : -1;
        }
        return next.compareToIgnoreCase(previous) >= 0 ? 1 : -1;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static final class SlideFadeWidget extends WidgetGroup {
        private final long startMs;
        private final int direction;

        private SlideFadeWidget(WidgetGroup content, long startMs, int direction) {
            super(content.getSelfPositionX(), content.getSelfPositionY(), content.getSizeWidth(), content.getSizeHeight());
            this.startMs = startMs;
            this.direction = direction < 0 ? -1 : 1;
            content.setSelfPosition(0, 0);
            addWidget(content);
        }

        @Override
        public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            drawAnimated(graphics, () -> drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks));
        }

        @Override
        public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            drawAnimated(graphics, () -> drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks));
        }

        @Override
        public void drawOverlay(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            drawAnimated(graphics, () -> super.drawOverlay(graphics, mouseX, mouseY, partialTicks));
        }

        private void drawAnimated(GuiGraphics graphics, Runnable draw) {
            if (!UiAnimationProgress.running(startMs, DURATION_MS)) {
                draw.run();
                return;
            }

            float progress = smootherProgress(startMs);
            float alpha = UiAnimationProgress.interpolate(START_ALPHA, 1.0f, progress);
            float scale = UiAnimationProgress.interpolate(START_SCALE, 1.0f, progress);
            float offsetY = direction * SLIDE_PX * (1.0f - progress);
            float centerX = getPositionX() + getSizeWidth() / 2.0f;
            float centerY = getPositionY() + getSizeHeight() / 2.0f;

            graphics.pose().pushPose();
            graphics.pose().translate(0.0f, offsetY, 0.0f);
            graphics.pose().translate(centerX, centerY, 0.0f);
            graphics.pose().scale(scale, scale, 1.0f);
            graphics.pose().translate(-centerX, -centerY, 0.0f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
            draw.run();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            graphics.pose().popPose();
        }

        private float smootherProgress(long animationStartMs) {
            float t = UiAnimationProgress.linearProgress(animationStartMs, DURATION_MS);
            return t * t * t * (t * (t * 6.0f - 15.0f) + 10.0f);
        }
    }
}
