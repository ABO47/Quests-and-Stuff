package com.abo47.questsandstuff.client.tablet.quest.tools;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.animation.AnchoredMenuRevealWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class ToolMenuAnimation {
    private ToolMenuAnimation() {
    }

    public static void toggleMain(TabletUiState state) {
        if (state == null) {
            return;
        }
        if (state.canvas.toolsMenuOpen && !state.canvas.toolsMenuClosing) {
            closeMain(state);
            return;
        }
        openMain(state);
    }

    public static void openMain(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.canvas.toolsMenuOpen = true;
        state.canvas.toolsMenuClosing = false;
        finishQuestDetails(state);
        start(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] tools menu toggle open=true");
    }

    public static void closeMain(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.canvas.toolsGridSizeMenuOpen = false;
        state.canvas.toolsGridOpacityMenuOpen = false;
        if (!state.canvas.toolsMenuOpen && !state.canvas.toolsMenuClosing) {
            finishMain(state);
            return;
        }
        if (state.canvas.toolsMenuClosing && !state.canvas.toolsMenuOpen) {
            return;
        }
        if (!QuestsAndStuffConfig.toolsMenuAnimationsEnabled()) {
            finishMain(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] tools menu toggle open=false");
            return;
        }
        state.canvas.toolsMenuOpen = false;
        state.canvas.toolsMenuClosing = true;
        start(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] tools menu toggle open=false");
    }

    public static void finishMain(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.canvas.toolsMenuOpen = false;
        state.canvas.toolsMenuClosing = false;
        state.canvas.toolsGridSizeMenuOpen = false;
        state.canvas.toolsGridOpacityMenuOpen = false;
        state.canvas.toolsMenuX = 0;
        state.canvas.toolsMenuY = 0;
        state.canvas.toolsMenuW = 0;
        state.canvas.toolsMenuH = 0;
    }

    public static boolean mainVisible(TabletUiState state) {
        return state != null && (state.canvas.toolsMenuOpen || state.canvas.toolsMenuClosing);
    }

    public static boolean mainInteractive(TabletUiState state) {
        return state != null && state.canvas.toolsMenuOpen && !state.canvas.toolsMenuClosing;
    }

    public static boolean mainOpening(TabletUiState state) {
        return state != null && !state.canvas.toolsMenuClosing;
    }

    public static void toggleQuestDetails(TabletUiState state) {
        if (state == null) {
            return;
        }
        if (state.questDetails.questDetailsToolsOpen && !state.questDetails.questDetailsToolsClosing) {
            closeQuestDetails(state);
            return;
        }
        openQuestDetails(state);
    }

    public static void openQuestDetails(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.questDetails.questDetailsToolsOpen = true;
        state.questDetails.questDetailsToolsClosing = false;
        finishMain(state);
        start(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details tools toggle open=true");
    }

    public static void closeQuestDetails(TabletUiState state) {
        if (state == null) {
            return;
        }
        if (!state.questDetails.questDetailsToolsOpen && !state.questDetails.questDetailsToolsClosing) {
            finishQuestDetails(state);
            return;
        }
        if (state.questDetails.questDetailsToolsClosing && !state.questDetails.questDetailsToolsOpen) {
            return;
        }
        if (!QuestsAndStuffConfig.toolsMenuAnimationsEnabled()) {
            finishQuestDetails(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details tools toggle open=false");
            return;
        }
        state.questDetails.questDetailsToolsOpen = false;
        state.questDetails.questDetailsToolsClosing = true;
        start(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details tools toggle open=false");
    }

    public static void finishQuestDetails(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.questDetails.questDetailsToolsOpen = false;
        state.questDetails.questDetailsToolsClosing = false;
    }

    public static boolean questDetailsVisible(TabletUiState state) {
        return state != null && (state.questDetails.questDetailsToolsOpen || state.questDetails.questDetailsToolsClosing);
    }

    public static boolean questDetailsInteractive(TabletUiState state) {
        return state != null && state.questDetails.questDetailsToolsOpen && !state.questDetails.questDetailsToolsClosing;
    }

    public static boolean questDetailsOpening(TabletUiState state) {
        return state != null && !state.questDetails.questDetailsToolsClosing;
    }

    public static boolean finishClosingIfDone(TabletUiState state) {
        if (state == null) {
            return false;
        }
        boolean changed = false;
        if (state.canvas.toolsMenuClosing && closingFinished(state)) {
            finishMain(state);
            changed = true;
        }
        if (state.questDetails.questDetailsToolsClosing && closingFinished(state)) {
            finishQuestDetails(state);
            changed = true;
        }
        return changed;
    }

    private static boolean closingFinished(TabletUiState state) {
        return !QuestsAndStuffConfig.toolsMenuAnimationsEnabled()
                || !AnchoredMenuRevealWidget.running(state.canvas.toolsMenuAnimationStartMs);
    }

    private static void start(TabletUiState state) {
        state.canvas.toolsMenuAnimationStartMs = System.currentTimeMillis();
    }
}
