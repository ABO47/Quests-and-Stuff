package com.abo47.questsandstuff.client.tablet.controls.picker;

import java.util.List;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.ScrollMath;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.TileGridLayout;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;

public final class TiledPickerPanel {
    private TiledPickerPanel() {
    }

    public static <T> TileGridLayout add(
            WidgetGroup parent,
            int x,
            int y,
            int w,
            int h,
            int tileW,
            int tileH,
            int gap,
            int padX,
            int padY,
            List<T> entries,
            String emptyText,
            ScrollState scroll,
            Runnable onScroll,
            Runnable refresh,
            TileRenderer<T> renderer
    ) {
        TileGridLayout layout = TileGridLayout.calculate(w, h, tileW, tileH, gap, padX, padY, entries.size(), scroll.value());
        scroll.setValue(layout.scrollStart());
        WidgetGroup surface = new WidgetGroup(x, y, w, h) {
            @Override
            public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
                if (!isMouseOverElement(mouseX, mouseY) || layout.maxStart() <= 0) {
                    return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
                }
                int next = ScrollMath.wheel(scroll.value(), layout.maxStart(), layout.wheelStep(), wheelDelta);
                if (next != scroll.value()) {
                    scroll.setValue(next);
                    if (onScroll != null) {
                        onScroll.run();
                    }
                    refresh.run();
                }
                return true;
            }
        };
        surface.setBackground(SurfaceFactory.bordered(withAlpha(TabletColors.elevatedSurface(), 150), TabletColors.subtleBorder()));
        parent.addWidget(surface);

        if (entries.isEmpty()) {
            surface.addWidget(label(8, 8, emptyText, TabletColors.TEXT_MUTED));
            return layout;
        }

        for (int i = layout.scrollStart(); i < layout.visibleEnd(); i++) {
            int visibleIndex = i - layout.scrollStart();
            renderer.render(surface, entries.get(i), i, layout.tileX(visibleIndex), layout.tileY(visibleIndex), layout.tileW(), layout.tileH(), layout);
        }

        if (layout.showScroll()) {
            parent.addWidget(new DragScrollBarWidget(
                    x + layout.scrollBarX() + 1,
                    y + layout.scrollBarY(),
                    DragScrollBarWidget.RESERVED_WIDTH,
                    layout.scrollBarH(),
                    scroll::value,
                    layout::maxStart,
                    layout::knobH,
                    value -> {
                        scroll.setValue(value);
                        if (onScroll != null) {
                            onScroll.run();
                        }
                    },
                    scroll::dragging,
                    scroll::setDragging,
                    refresh,
                    TabletColors.scrollTrack(scroll.dragging()),
                    TabletColors.scrollThumb(false),
                    TabletColors.scrollThumb(true),
                    DragScrollBarWidget.WIDTH
            ));
        }
        return layout;
    }

    @FunctionalInterface
    public interface TileRenderer<T> {
        void render(WidgetGroup surface, T entry, int index, int tileX, int tileY, int tileW, int tileH, TileGridLayout layout);
    }
}
