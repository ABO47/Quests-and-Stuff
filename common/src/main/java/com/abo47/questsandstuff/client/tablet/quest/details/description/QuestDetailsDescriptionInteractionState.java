package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

final class QuestDetailsDescriptionInteractionState {
    private static final int WHEEL_SCROLL_STEP = 18;

    private QuestDetailsDescriptionInteractionState() {
    }

    static void beginPanning(TabletUiState state, double mouseX, double mouseY) {
        state.questDetailsPanning = true;
        state.questDetailsPanStartX = (int) Math.round(mouseX);
        state.questDetailsPanStartY = (int) Math.round(mouseY);
        state.questDetailsPanStartScroll = state.questDetailsDescScroll;
    }

    static void beginBoxSelection(TabletUiState state, int lx, int visibleY, boolean additive, Runnable clearSelection) {
        state.questDetailsBoxSelecting = true;
        state.questDetailsBoxAdditive = additive;
        state.questDetailsBoxStartX = lx;
        state.questDetailsBoxStartY = visibleY;
        state.questDetailsBoxCurrentX = lx;
        state.questDetailsBoxCurrentY = visibleY;
        if (!additive) {
            clearSelection.run();
        }
    }

    static void updateBoxSelection(TabletUiState state, int lx, int visibleY) {
        state.questDetailsBoxCurrentX = lx;
        state.questDetailsBoxCurrentY = visibleY;
    }

    static boolean scrollByWheel(TabletUiState state, QuestDetailsDescriptionModel model, int viewportH, double wheelDelta) {
        int previous = state.questDetailsDescScroll;
        int next = previous + (wheelDelta < 0 ? WHEEL_SCROLL_STEP : -WHEEL_SCROLL_STEP);
        state.questDetailsDescScroll = QuestDetailsDescriptionLayout.clampDescriptionScroll(state, model, viewportH, next);
        return state.questDetailsDescScroll != previous;
    }

    static boolean recordTextClick(TabletUiState state, String id, long nowMs, long thresholdMs) {
        boolean doubleClick = id.equals(state.questDetailsTextLastClickId)
                && nowMs - state.questDetailsTextLastClickAtMs <= thresholdMs;
        state.questDetailsTextLastClickId = id;
        state.questDetailsTextLastClickAtMs = nowMs;
        return doubleClick;
    }
}
