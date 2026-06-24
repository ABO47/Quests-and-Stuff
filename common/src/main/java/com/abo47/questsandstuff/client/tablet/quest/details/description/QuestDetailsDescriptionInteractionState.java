package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

final class QuestDetailsDescriptionInteractionState {
    private static final int WHEEL_SCROLL_STEP = 18;

    private QuestDetailsDescriptionInteractionState() {
    }

    static void beginPanning(TabletUiState state, double mouseX, double mouseY) {
        state.questDetails.questDetailsPanning = true;
        state.questDetails.questDetailsPanStartX = (int) Math.round(mouseX);
        state.questDetails.questDetailsPanStartY = (int) Math.round(mouseY);
        state.questDetails.questDetailsPanStartScroll = state.questDetails.questDetailsDescScroll;
    }

    static void beginBoxSelection(TabletUiState state, int lx, int visibleY, boolean additive, Runnable clearSelection) {
        state.questDetails.questDetailsBoxSelecting = true;
        state.questDetails.questDetailsBoxAdditive = additive;
        state.questDetails.questDetailsBoxStartX = lx;
        state.questDetails.questDetailsBoxStartY = visibleY;
        state.questDetails.questDetailsBoxCurrentX = lx;
        state.questDetails.questDetailsBoxCurrentY = visibleY;
        if (!additive) {
            clearSelection.run();
        }
    }

    static void updateBoxSelection(TabletUiState state, int lx, int visibleY) {
        state.questDetails.questDetailsBoxCurrentX = lx;
        state.questDetails.questDetailsBoxCurrentY = visibleY;
    }

    static boolean scrollByWheel(TabletUiState state, QuestDetailsDescriptionModel model, int viewportH, double wheelDelta) {
        int previous = state.questDetails.questDetailsDescScroll;
        int next = previous + (wheelDelta < 0 ? WHEEL_SCROLL_STEP : -WHEEL_SCROLL_STEP);
        state.questDetails.questDetailsDescScroll = QuestDetailsDescriptionLayout.clampDescriptionScroll(state, model, viewportH, next);
        return state.questDetails.questDetailsDescScroll != previous;
    }

    static boolean recordTextClick(TabletUiState state, String id, long nowMs, long thresholdMs) {
        boolean doubleClick = id.equals(state.questDetails.questDetailsTextLastClickId)
                && nowMs - state.questDetails.questDetailsTextLastClickAtMs <= thresholdMs;
        state.questDetails.questDetailsTextLastClickId = id;
        state.questDetails.questDetailsTextLastClickAtMs = nowMs;
        return doubleClick;
    }
}
