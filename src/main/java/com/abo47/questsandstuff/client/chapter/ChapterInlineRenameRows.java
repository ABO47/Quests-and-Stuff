package com.abo47.questsandstuff.client.chapter;

import com.abo47.questsandstuff.client.tablet.controls.InlineRenameField;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class ChapterInlineRenameRows {
    private ChapterInlineRenameRows() {
    }

    static void add(
            WidgetGroup chapterList,
            int cardX,
            int y,
            int cardW,
            int iconX,
            String icon,
            Supplier<String> value,
            Consumer<String> updateValue,
            Consumer<String> commit,
            Runnable cancel
    ) {
        chapterList.addWidget(TabletUiFactory.panel(
                cardX,
                y,
                cardW,
                TabletUiFactory.CHAPTER_CARD_H,
                TabletUiFactory.withAlpha(ModColors.INTERACTIVE, 108),
                ModColors.BORDER_ACCENT
        ));
        if (icon != null && !icon.isBlank()) {
            chapterList.addWidget(new DisplayIconWidget(iconX, y + 8, TabletUiFactory.CONTENT_ICON_SIZE, TabletUiFactory.CONTENT_ICON_SIZE, icon));
        }

        int doneW = 24;
        int doneX = cardX + cardW - doneW - 2;
        int fieldX = cardX + 24;
        int fieldW = Math.max(18, doneX - fieldX - 3);
        InlineRenameField field = new InlineRenameField(
                fieldX,
                y + 8,
                fieldW,
                16,
                value,
                updateValue,
                () -> commit.accept(value.get()),
                cancel,
                null,
                null
        );
        field.setClientSideWidget();
        field.setMaxStringLength(40);
        field.setBordered(false);
        field.setBackground(Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.INTERACTIVE));
        field.setTextColor(ModColors.TEXT_PRIMARY);
        chapterList.addWidget(field);
        addFlatIconAction(chapterList, doneX + 4, y + 10, TabletUiFactory.ACTION_ICON_SIZE, "add.png", click -> commit.accept(field.getCurrentString()));
    }

    private static void addFlatIconAction(WidgetGroup parent, int x, int y, int size, String iconName, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        IGuiTexture texture = UiIconAtlas.iconTexture(iconName);
        if (texture != null) {
            parent.addWidget(new ImageWidget(x, y, size, size, texture));
        }
        parent.addWidget(TabletUiFactory.flatHitButton(x, y, size, size, callback));
    }
}
