package com.abo47.questsandstuff.client.tablet.controls.picker;

import java.util.List;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.ScrollMath;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.TabletScissoredWidgetGroup;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;

public final class PickerListPanel {
    private PickerListPanel() {
    }

    public static <T> WidgetGroup add(
            WidgetGroup modal,
            int x,
            int y,
            int w,
            int h,
            int rowH,
            List<T> entries,
            String emptyText,
            ScrollState scroll,
            int wheelStep,
            Runnable refresh,
            RowRenderer<T> renderer
    ) {
        return add(modal, x, y, w, h, rowH, entries, emptyText, scroll, wheelStep, refresh, renderer, GRID_4, GRID_4);
    }

    public static <T> WidgetGroup add(
            WidgetGroup modal,
            int x,
            int y,
            int w,
            int h,
            int rowH,
            List<T> entries,
            String emptyText,
            ScrollState scroll,
            int wheelStep,
            Runnable refresh,
            RowRenderer<T> renderer,
            int hPad
    ) {
        return add(modal, x, y, w, h, rowH, entries, emptyText, scroll, wheelStep, refresh, renderer, hPad, GRID_4);
    }

    public static <T> WidgetGroup add(
            WidgetGroup modal,
            int x,
            int y,
            int w,
            int h,
            int rowH,
            List<T> entries,
            String emptyText,
            ScrollState scroll,
            int wheelStep,
            Runnable refresh,
            RowRenderer<T> renderer,
            int hPad,
            int vPad
    ) {
        int rows = ScrollMath.listRows(h - vPad * 2, rowH, 0);
        int maxStart = Math.max(0, entries.size() - rows);
        scroll.setValue(ScrollMath.clamp(scroll.value(), maxStart));
        boolean showScroll = maxStart > 0;
        int rowW = showScroll ? w - hPad * 2 - DragScrollBarWidget.RESERVED_WIDTH : w - hPad * 2;
        int rowX = hPad;

        WidgetGroup list = new TabletScissoredWidgetGroup(x, y, w, h) {
            @Override
            public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
                if (!isMouseOverElement(mouseX, mouseY) || maxStart <= 0) {
                    return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
                }
                int next = ScrollMath.wheel(scroll.value(), maxStart, Math.max(1, wheelStep), wheelDelta);
                if (next != scroll.value()) {
                    scroll.setValue(next);
                    refresh.run();
                }
                return true;
            }
        };
        list.setBackground(SurfaceFactory.bordered(withAlpha(TabletColors.elevatedSurface(), 190), TabletColors.subtleBorder()));
        modal.addWidget(list);

        if (entries.isEmpty()) {
            list.addWidget(label(rowX, vPad, emptyText, TabletColors.TEXT_MUTED));
            return list;
        }

        int end = Math.min(entries.size(), scroll.value() + rows);
        int rowY = vPad;
        for (int i = scroll.value(); i < end; i++) {
            WidgetGroup row = new WidgetGroup(rowX, rowY, rowW, rowH);
            renderer.render(row, entries.get(i), i, 0, rowW);
            list.addWidget(row);
            rowY += rowH;
        }

        if (showScroll) {
            int barH = Math.max(1, rows * rowH);
            int knobH = Math.max(12, Math.round((float) rows / (float) entries.size() * barH));
            list.addWidget(new DragScrollBarWidget(
                    w - DragScrollBarWidget.RESERVED_WIDTH + 1,
                    vPad,
                    DragScrollBarWidget.RESERVED_WIDTH,
                    barH,
                    scroll::value,
                    () -> maxStart,
                    () -> knobH,
                    scroll::setValue,
                    scroll::dragging,
                    scroll::setDragging,
                    refresh,
                    DragScrollBarWidget.WIDTH
            ));
        }
        return list;
    }

    @FunctionalInterface
    public interface RowRenderer<T> {
        void render(WidgetGroup list, T entry, int index, int rowY, int rowW);
    }
}
