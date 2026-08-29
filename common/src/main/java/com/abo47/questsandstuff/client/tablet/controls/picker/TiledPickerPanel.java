package com.abo47.questsandstuff.client.tablet.controls.picker;

import java.util.List;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.ScrollMath;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.TabletScissoredWidgetGroup;
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
            TileRenderer<T> renderer
    ) {
        TileGridLayout layout = TileGridLayout.calculate(w, h, tileW, tileH, gap, padX, padY, entries.size(), scroll.value());
        scroll.setValue(layout.scrollStart());
        WidgetGroup tiles = new WidgetGroup(0, 0, w, h);
        Runnable relayout = () -> {
            tiles.clearAllWidgets();
            TileGridLayout current = TileGridLayout.calculate(w, h, tileW, tileH, gap, padX, padY, entries.size(), scroll.value());
            for (int i = current.scrollStart(); i < current.visibleEnd(); i++) {
                int visibleIndex = i - current.scrollStart();
                renderer.render(tiles, entries.get(i), i, current.tileX(visibleIndex), current.tileY(visibleIndex), current.tileW(), current.tileH(), current);
            }
        };
        WidgetGroup surface = new TabletScissoredWidgetGroup(x, y, w, h) {
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
                    relayout.run();
                }
                return true;
            }
        };
        surface.setBackground(SurfaceFactory.bordered(withAlpha(TabletColors.elevatedSurface(), 190), TabletColors.subtleBorder()));
        parent.addWidget(surface);

        if (entries.isEmpty()) {
            surface.addWidget(label(Math.max(4, padX), Math.max(4, padY), emptyText, TabletColors.TEXT_MUTED));
            return layout;
        }

        surface.addWidget(tiles);
        relayout.run();

        if (layout.showScroll()) {
            surface.addWidget(new DragScrollBarWidget(
                    layout.scrollBarX() + 1,
                    layout.scrollBarY(),
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
                    relayout,
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
