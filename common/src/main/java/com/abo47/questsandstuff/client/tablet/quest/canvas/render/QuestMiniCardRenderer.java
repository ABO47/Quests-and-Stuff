package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;

import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

public final class QuestMiniCardRenderer {
    private QuestMiniCardRenderer() {
    }

    public static void drawTagCard(
            GuiGraphics graphics,
            CompoundTag tag,
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY,
            float partialTicks,
            int alpha,
            boolean hiddenOverlay,
            boolean highlighted
    ) {
        QuestCardBackgroundRenderer.drawTagBackground(graphics, tag, x, y, width, height, mouseX, mouseY, alpha);
        IconRect icon = iconRect(x, y, width, height);
        DisplayIconWidget.drawIcon(graphics, mouseX, mouseY, icon.x(), icon.y(), icon.size(), icon.size(), tag == null ? "" : tag.getString(QuestSyncKeys.Quest.ICON), partialTicks, alpha);
        if (hiddenOverlay) {
            drawHiddenOverlay(graphics, x, y, width, height, alpha, 120);
        }
        if (highlighted) {
            drawHighlightBorder(graphics, x, y, width, height, alpha);
        }
    }

    public static void drawDisplayCard(
            GuiGraphics graphics,
            QuestDisplay display,
            boolean gated,
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY,
            float partialTicks,
            int alpha,
            boolean highlighted
    ) {
        QuestDisplay safeDisplay = display == null ? QuestDisplay.DEFAULT : display;
        QuestCardBackgroundRenderer.drawDisplayBackground(graphics, safeDisplay, gated, x, y, width, height, mouseX, mouseY, alpha);
        IconRect icon = iconRect(x, y, width, height);
        DisplayIconWidget.drawIcon(graphics, mouseX, mouseY, icon.x(), icon.y(), icon.size(), icon.size(), safeDisplay.icon(), partialTicks, alpha);
        if (safeDisplay.visualHidden() || gated) {
            drawHiddenOverlay(graphics, x, y, width, height, alpha, 130);
        }
        if (highlighted) {
            drawHighlightBorder(graphics, x, y, width, height, alpha);
        }
    }

    public static IconRect iconRect(int x, int y, int width, int height) {
        int min = Math.min(width, height);
        int pad = Math.max(1, Math.round(min * 0.16f));
        int iconSize = Math.max(1, min - pad * 2);
        int iconX = x + (width - iconSize) / 2;
        int iconY = y + (height - iconSize) / 2;
        return new IconRect(iconX, iconY, iconSize);
    }

    public static int hiddenOverlayColor(int alpha, int maxAlpha) {
        int safeMax = Math.max(0, Math.min(255, maxAlpha));
        return withAlpha(ModColors.SURFACE_BASE, Math.min(safeMax, Math.max(0, Math.min(255, alpha)) / 2));
    }

    public static int highlightColor(int alpha) {
        return withAlpha(ModColors.BORDER_ACCENT, Math.max(0, Math.min(255, alpha)));
    }

    private static void drawHiddenOverlay(GuiGraphics graphics, int x, int y, int width, int height, int alpha, int maxAlpha) {
        Surfaces.fill(hiddenOverlayColor(alpha, maxAlpha)).draw(graphics, 0, 0, x, y, width, height);
    }

    public static void drawHighlightBorder(GuiGraphics graphics, int x, int y, int width, int height, int alpha) {
        int color = highlightColor(alpha);
        Surfaces.fill(color).draw(graphics, 0, 0, x - 2, y - 2, width + 4, 2);
        Surfaces.fill(color).draw(graphics, 0, 0, x - 2, y + height, width + 4, 2);
        Surfaces.fill(color).draw(graphics, 0, 0, x - 2, y, 2, height);
        Surfaces.fill(color).draw(graphics, 0, 0, x + width, y, 2, height);
    }

    public record IconRect(int x, int y, int size) {
    }
}
