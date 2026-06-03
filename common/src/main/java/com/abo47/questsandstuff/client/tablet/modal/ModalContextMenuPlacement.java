package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;

public final class ModalContextMenuPlacement {
    private static final int MARGIN = 4;

    private ModalContextMenuPlacement() {
    }

    public static Placement fitToRootFromModal(TabletUiState state, int x, int y, int w, int h, int modalW, int modalH) {
        int modalX = modalX(state, modalW);
        int modalY = modalY(state, modalH);
        int globalX = modalX + x;
        int globalY = modalY + y;
        int rootW = TabletUiFactory.rootWidth(state);
        int rootH = TabletUiFactory.rootHeight(state);
        return new Placement(
                clamp(globalX, MARGIN, Math.max(MARGIN, rootW - w - MARGIN)) - modalX,
                clamp(globalY, MARGIN, Math.max(MARGIN, rootH - h - MARGIN)) - modalY,
                w,
                h
        );
    }

    public static int localPointerX(TabletUiState state, int modalW) {
        return state.modalWindowLastPointerX - modalX(state, modalW);
    }

    public static int localPointerY(TabletUiState state, int modalH) {
        return state.modalWindowLastPointerY - modalY(state, modalH);
    }

    public static int modalX(TabletUiState state, int modalW) {
        return (TabletUiFactory.rootWidth(state) - modalW) / 2;
    }

    public static int modalY(TabletUiState state, int modalH) {
        return (TabletUiFactory.rootHeight(state) - modalH) / 2;
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    public record Placement(int x, int y, int w, int h) {
    }
}
