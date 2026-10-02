package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.animation.UiAnimationProgress;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasInteractionGate;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class QuestHoverExpand extends WidgetGroup {
    private static final long HOVER_MS = 150L;
    private static final int MAX_TRACKED = 256;
    private static final Map<String, HoverMotion> PROGRESS = new LinkedHashMap<>();

    private final String questId;
    private final TabletUiState state;

    public QuestHoverExpand(int x, int y, int w, int h, String questId, TabletUiState state) {
        super(x, y, w, h);
        this.questId = questId == null ? "" : questId;
        this.state = state;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        float scale = scale(mouseX, mouseY);
        if (scale <= 1.001f) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            return;
        }
        graphics.pose().pushPose();
        try {
            applyScale(graphics, scale);
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        } finally {
            graphics.pose().popPose();
        }
    }

    @Override
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        float scale = scale(mouseX, mouseY);
        if (scale <= 1.001f) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            return;
        }
        graphics.pose().pushPose();
        try {
            applyScale(graphics, scale);
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        } finally {
            graphics.pose().popPose();
        }
    }

    private void applyScale(GuiGraphics graphics, float scale) {
        float centerX = getPositionX() + getSizeWidth() / 2.0f;
        float centerY = getPositionY() + getSizeHeight() / 2.0f;
        graphics.pose().translate(centerX, centerY, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.pose().translate(-centerX, -centerY, 0.0f);
    }

    private float scale(int mouseX, int mouseY) {
        if (!QuestsAndStuffConfig.questHoverExpandAnimationsEnabled()) {
            PROGRESS.remove(questId);
            return 1.0f;
        }
        int percent = QuestsAndStuffConfig.questHoverExpandPercent();
        if (percent <= 0) {
            return 1.0f;
        }
        float amount = progress(isMouseOverElement(mouseX, mouseY)
                && CanvasInteractionGate.hoverAllowed(state, this, mouseX, mouseY));
        if (amount <= 0.001f) {
            return 1.0f;
        }
        return 1.0f + amount * (percent / 100.0f);
    }

    private float progress(boolean hovered) {
        float target = hovered ? 1.0f : 0.0f;
        long now = System.currentTimeMillis();
        HoverMotion current = PROGRESS.get(questId);
        if (current == null && !hovered) {
            return 0.0f;
        }
        if (current == null || current.target != target) {
            float from = current == null ? 0.0f : current.value(now);
            current = new HoverMotion(from, target, now);
            PROGRESS.put(questId, current);
            trim();
        }
        float value = current.value(now);
        if (!hovered && value <= 0.001f) {
            PROGRESS.remove(questId);
            return 0.0f;
        }
        return value;
    }

    private static void trim() {
        while (PROGRESS.size() > MAX_TRACKED) {
            var iterator = PROGRESS.keySet().iterator();
            if (!iterator.hasNext()) {
                return;
            }
            iterator.next();
            iterator.remove();
        }
    }

    private record HoverMotion(float from, float target, long startMs) {
        float value(long now) {
            if (!UiAnimationProgress.running(startMs, HOVER_MS, now)) {
                return target;
            }
            return UiAnimationProgress.interpolate(from, target, UiAnimationProgress.cubicOutProgress(startMs, HOVER_MS, now));
        }
    }
}
