package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class CanvasChapterSwitchAnimation {
    private CanvasChapterSwitchAnimation() {
    }

    public static void trackSelectedChapter(TabletUiState state, String selectedChapter) {
        if (state == null) {
            return;
        }
        state.chapterPanel.canvasChapterSwitchGroup = normalize(selectedChapter);
        clear(state);
    }

    public static WidgetGroup wrap(TabletUiState state, WidgetGroup content) {
        return content;
    }

    public static boolean finishIfDone(TabletUiState state) {
        if (state == null || state.chapterPanel.canvasChapterSwitchAnimationStartMs <= 0L) {
            return false;
        }
        clear(state);
        return true;
    }

    private static void clear(TabletUiState state) {
        state.chapterPanel.canvasChapterSwitchAnimationStartMs = 0L;
        state.chapterPanel.canvasChapterSwitchDirection = 1;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
