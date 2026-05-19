package com.abo47.questsandstuff.client.tablet.context;

import com.abo47.questsandstuff.client.tablet.animation.UiAnimationProgress;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

import javax.annotation.Nonnull;

public final class ContextMenuAnimation {
    public static final String DEFAULT_KEY = "context";
    public static final String CHAPTER_KEY = "chapter";
    private static final long OPEN_MS = 110L;

    private ContextMenuAnimation() {
    }

    public static WidgetGroup wrap(WidgetGroup content) {
        return wrap(content, null, "");
    }

    public static WidgetGroup wrap(WidgetGroup content, TabletUiState state, String key) {
        if (content == null) {
            return new WidgetGroup(0, 0, 1, 1);
        }
        return new AnimatedContextMenu(content, state, key);
    }

    public static void start(TabletUiState state, String key) {
        if (state == null) {
            return;
        }
        state.contextMenuAnimationStartMs = System.currentTimeMillis();
        state.contextMenuAnimationKey = key == null ? "" : key;
    }

    public static void finish(TabletUiState state, String key) {
        if (state == null) {
            return;
        }
        state.contextMenuAnimationStartMs = System.currentTimeMillis() - OPEN_MS;
        state.contextMenuAnimationKey = key == null ? "" : key;
    }

    private static final class AnimatedContextMenu extends WidgetGroup {
        private final long openStartMs = System.currentTimeMillis();
        private final TabletUiState state;
        private final String key;

        private AnimatedContextMenu(WidgetGroup content, TabletUiState state, String key) {
            super(content.getSelfPositionX(), content.getSelfPositionY(), content.getSizeWidth(), content.getSizeHeight());
            this.state = state;
            this.key = key == null ? "" : key;
            content.setSelfPosition(0, 0);
            addWidget(content);
        }

        @Override
        public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            drawClipped(graphics, () -> drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks));
        }

        @Override
        public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            drawClipped(graphics, () -> drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks));
        }

        @Override
        public void drawOverlay(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            drawClipped(graphics, () -> super.drawOverlay(graphics, mouseX, mouseY, partialTicks));
        }

        private void drawClipped(GuiGraphics graphics, Runnable draw) {
            int height = animatedHeight();
            if (height >= getSizeHeight()) {
                draw.run();
                return;
            }
            int x = getPositionX();
            int y = getPositionY();
            int w = getSizeWidth();
            var trans = graphics.pose().last().pose();
            var realPos = trans.transform(new Vector4f(x, y, 0, 1));
            var realPos2 = trans.transform(new Vector4f(x + w, y + height, 0, 1));
            graphics.enableScissor((int) realPos.x, (int) realPos.y, (int) realPos2.x, (int) realPos2.y);
            draw.run();
            graphics.disableScissor();
        }

        private int animatedHeight() {
            long startMs = effectiveStartMs();
            if (!UiAnimationProgress.running(startMs, OPEN_MS)) {
                return getSizeHeight();
            }
            float progress = UiAnimationProgress.easedProgress(startMs, OPEN_MS);
            return Math.max(1, UiAnimationProgress.interpolate(1, getSizeHeight(), progress));
        }

        private long effectiveStartMs() {
            if (state == null || state.contextMenuAnimationStartMs <= 0L) {
                return openStartMs;
            }
            String stateKey = state.contextMenuAnimationKey == null ? "" : state.contextMenuAnimationKey;
            return key.equals(stateKey) ? state.contextMenuAnimationStartMs : openStartMs;
        }
    }
}
