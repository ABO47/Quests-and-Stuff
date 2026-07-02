package com.abo47.questsandstuff.client.tablet.controls;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class CardDragGhosts {
    private CardDragGhosts() {
    }

    public static void renderChapter(WidgetGroup parent, int x, int y, int w, IGuiTexture background, String icon, String title) {
        renderBase(parent, x, y, w, TabletUiFactory.CHAPTER_CARD_H, background, 0.0f);
        if (icon != null && !icon.isBlank()) {
            parent.addWidget(new DisplayIconWidget(x + 4, y + 8, TabletUiFactory.CONTENT_ICON_SIZE, TabletUiFactory.CONTENT_ICON_SIZE, icon));
        }
        parent.addWidget(TabletUiFactory.label(x + 26, y + 12, SearchFilter.crop(title, Math.max(4, (w - 34) / 7)), withAlpha(TabletColors.TEXT_PRIMARY, 225)));
        renderVeil(parent, x, y, w, TabletUiFactory.CHAPTER_CARD_H);
    }

    public static void renderTask(WidgetGroup parent, int x, int y, int w, int h, String icon, String title, String amount, float progress) {
        renderBase(parent, x, y, w, h, null, progress);
        if (icon != null && !icon.isBlank()) {
            parent.addWidget(new DisplayIconWidget(x + 8, y + 8, TabletUiFactory.CONTENT_ICON_SIZE, TabletUiFactory.CONTENT_ICON_SIZE, icon));
        }
        int textRight = amount == null || amount.isBlank() ? x + w - 8 : x + w - 38;
        int fieldW = Math.max(18, textRight - (x + 30));
        parent.addWidget(TabletUiFactory.label(x + 30, y + 11, SearchFilter.crop(title, Math.max(4, fieldW / 7)), withAlpha(TabletColors.TEXT_PRIMARY, 210)));
        if (amount != null && !amount.isBlank()) {
            parent.addWidget(TabletUiFactory.label(x + w - 34, y + 12, SearchFilter.crop(amount, 5), withAlpha(TabletColors.TEXT_MUTED, 210)));
        }
        renderVeil(parent, x, y, w, h);
    }

    private static void renderBase(WidgetGroup parent, int x, int y, int w, int h, IGuiTexture background, float progress) {
        WidgetGroup card = TabletUiFactory.panel(x, y, w, h, withAlpha(TabletColors.SUCCESS, 58), TabletColors.SUCCESS);
        parent.addWidget(card);
        if (background != null) {
            parent.addWidget(new ImageWidget(x + 1, y + 1, Math.max(1, w - 2), Math.max(1, h - 2), background));
        }
        int fillW = Math.round((w - 2) * Math.max(0.0f, Math.min(1.0f, progress)));
        if (fillW > 0) {
            parent.addWidget(TabletUiFactory.panel(x + 1, y + 1, Math.max(1, fillW), h - 2, withAlpha(TabletColors.SUCCESS, 62), 0x00000000));
        }
    }

    private static void renderVeil(WidgetGroup parent, int x, int y, int w, int h) {
        WidgetGroup veil = new WidgetGroup(x, y, w, h);
        veil.setBackground(SurfaceFactory.bordered(withAlpha(TabletColors.SUCCESS, 36), TabletColors.SUCCESS));
        parent.addWidget(veil);
    }
}
