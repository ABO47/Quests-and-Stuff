package com.abo47.questsandstuff.client.tablet.controls.picker;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.ScrollMath;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

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
        int rows = Math.max(1, h / rowH);
        int maxStart = Math.max(0, entries.size() - rows);
        scroll.setValue(ScrollMath.clamp(scroll.value(), maxStart));
        boolean showScroll = maxStart > 0;
        int rowW = showScroll ? w - DragScrollBarWidget.RESERVED_WIDTH - 2 : w;

        WidgetGroup list = new WidgetGroup(x, y, w, h) {
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
            list.addWidget(label(8, 8, emptyText, TabletColors.TEXT_MUTED));
            return list;
        }

        int end = Math.min(entries.size(), scroll.value() + rows);
        int rowY = 4;
        for (int i = scroll.value(); i < end; i++) {
            renderer.render(list, entries.get(i), i, rowY, rowW);
            rowY += rowH;
        }

        if (showScroll) {
            int barH = Math.max(1, rows * rowH);
            int knobH = Math.max(12, Math.round((float) rows / (float) entries.size() * barH));
            int barX = x + w - DragScrollBarWidget.RESERVED_WIDTH - 1;
            int barY = y + GRID_4;
            modal.addWidget(new DragScrollBarWidget(
                    barX + 1,
                    barY,
                    DragScrollBarWidget.RESERVED_WIDTH,
                    barH,
                    scroll::value,
                    () -> maxStart,
                    () -> knobH,
                    scroll::setValue,
                    scroll::dragging,
                    scroll::setDragging,
                    refresh,
                    TabletColors.scrollTrack(scroll.dragging()),
                    TabletColors.scrollThumb(false),
                    TabletColors.scrollThumb(true),
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
