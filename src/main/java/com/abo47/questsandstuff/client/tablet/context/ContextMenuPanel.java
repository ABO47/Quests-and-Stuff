package com.abo47.questsandstuff.client.tablet.context;

import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import java.util.List;
import java.util.function.Consumer;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CONTEXT_ROW_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.addWindowsContextRow;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class ContextMenuPanel {
    private ContextMenuPanel() {
    }

    public static WidgetGroup build(
            int x,
            int y,
            int w,
            List<ContextAction> actions,
            int start,
            int visibleRows,
            int borderColor,
            TabletUiState state,
            Consumer<ContextAction> afterAction
    ) {
        int safeVisibleRows = Math.max(1, Math.min(visibleRows, Math.max(1, actions.size())));
        int menuH = 8 + safeVisibleRows * CONTEXT_ROW_H;
        int safeStart = Math.max(0, Math.min(start, Math.max(0, actions.size() - safeVisibleRows)));
        int end = Math.min(actions.size(), safeStart + safeVisibleRows);
        boolean needsScroll = actions.size() > safeVisibleRows;
        int rowWidth = needsScroll ? w - 14 : w - 8;
        WidgetGroup menu = panel(x, y, w, menuH, withAlpha(ModColors.SURFACE_BASE, 246), borderColor);
        for (int i = safeStart; i < end; i++) {
            ContextAction action = actions.get(i);
            int rowY = 4 + (i - safeStart) * CONTEXT_ROW_H;
            addWindowsContextRow(menu, rowY, rowWidth, action.label(), action.icon(), click -> {
                ContextMenuAnimation.finish(state, ContextMenuAnimation.DEFAULT_KEY);
                action.action().run();
                if (afterAction != null) {
                    afterAction.accept(action);
                }
            });
        }
        if (needsScroll) {
            addScrollbar(menu, actions.size(), safeVisibleRows, safeStart, w);
        }
        return ContextMenuAnimation.wrap(menu, state, ContextMenuAnimation.DEFAULT_KEY);
    }

    public static int heightForRows(int visibleRows) {
        return 8 + Math.max(1, visibleRows) * CONTEXT_ROW_H;
    }

    private static void addScrollbar(WidgetGroup menu, int actionCount, int visibleRows, int start, int menuW) {
        int trackX = menuW - DragScrollBarWidget.RESERVED_WIDTH;
        int trackY = 4;
        int trackH = visibleRows * CONTEXT_ROW_H;
        int knobX = trackX + Math.max(0, (DragScrollBarWidget.RESERVED_WIDTH - DragScrollBarWidget.WIDTH) / 2);
        WidgetGroup track = new WidgetGroup(knobX + 1, trackY, 2, trackH);
        track.setBackground(Surfaces.fill(withAlpha(ModColors.BORDER_BASE, 140)));
        menu.addWidget(track);

        int knobH = Math.max(8, (trackH * visibleRows) / Math.max(1, actionCount));
        int scrollMax = Math.max(1, actionCount - visibleRows);
        int knobOffset = Math.round(((float) start / (float) scrollMax) * Math.max(0, trackH - knobH));
        WidgetGroup knob = new WidgetGroup(knobX, trackY + knobOffset, DragScrollBarWidget.WIDTH, knobH);
        knob.setBackground(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 220)));
        menu.addWidget(knob);
    }
}
