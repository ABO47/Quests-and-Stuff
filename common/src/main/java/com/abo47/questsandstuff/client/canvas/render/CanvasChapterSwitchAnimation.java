package com.abo47.questsandstuff.client.canvas.render;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class CanvasChapterSwitchAnimation {
    private CanvasChapterSwitchAnimation() {
    }

    public static void trackSelectedGroup(TabletUiState state, String selectedGroup) {
        if (state == null) {
            return;
        }
        state.canvasChapterSwitchGroup = normalize(selectedGroup);
        clear(state);
    }

    public static WidgetGroup wrap(TabletUiState state, WidgetGroup content) {
        return content;
    }

    public static boolean finishIfDone(TabletUiState state) {
        if (state == null || state.canvasChapterSwitchAnimationStartMs <= 0L) {
            return false;
        }
        clear(state);
        return true;
    }

    private static void clear(TabletUiState state) {
        state.canvasChapterSwitchAnimationStartMs = 0L;
        state.canvasChapterSwitchDirection = 1;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
