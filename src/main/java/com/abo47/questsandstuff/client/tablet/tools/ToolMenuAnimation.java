package com.abo47.questsandstuff.client.tablet.tools;

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
        if (state.toolsMenuOpen && !state.toolsMenuClosing) {
            closeMain(state);
            return;
        }
        openMain(state);
    }

    public static void openMain(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.toolsMenuOpen = true;
        state.toolsMenuClosing = false;
        finishQuestDetails(state);
        start(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] tools menu toggle open=true");
    }

    public static void closeMain(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.toolsGridSizeMenuOpen = false;
        state.toolsGridOpacityMenuOpen = false;
        if (!state.toolsMenuOpen && !state.toolsMenuClosing) {
            finishMain(state);
            return;
        }
        if (state.toolsMenuClosing && !state.toolsMenuOpen) {
            return;
        }
        if (!QuestsAndStuffConfig.toolsMenuAnimationsEnabled()) {
            finishMain(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] tools menu toggle open=false");
            return;
        }
        state.toolsMenuOpen = false;
        state.toolsMenuClosing = true;
        start(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] tools menu toggle open=false");
    }

    public static void finishMain(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.toolsMenuOpen = false;
        state.toolsMenuClosing = false;
        state.toolsGridSizeMenuOpen = false;
        state.toolsGridOpacityMenuOpen = false;
        state.toolsMenuX = 0;
        state.toolsMenuY = 0;
        state.toolsMenuW = 0;
        state.toolsMenuH = 0;
    }

    public static boolean mainVisible(TabletUiState state) {
        return state != null && (state.toolsMenuOpen || state.toolsMenuClosing);
    }

    public static boolean mainInteractive(TabletUiState state) {
        return state != null && state.toolsMenuOpen && !state.toolsMenuClosing;
    }

    public static boolean mainOpening(TabletUiState state) {
        return state != null && !state.toolsMenuClosing;
    }

    public static void toggleQuestDetails(TabletUiState state) {
        if (state == null) {
            return;
        }
        if (state.questDetailsToolsOpen && !state.questDetailsToolsClosing) {
            closeQuestDetails(state);
            return;
        }
        openQuestDetails(state);
    }

    public static void openQuestDetails(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.questDetailsToolsOpen = true;
        state.questDetailsToolsClosing = false;
        finishMain(state);
        start(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details tools toggle open=true");
    }

    public static void closeQuestDetails(TabletUiState state) {
        if (state == null) {
            return;
        }
        if (!state.questDetailsToolsOpen && !state.questDetailsToolsClosing) {
            finishQuestDetails(state);
            return;
        }
        if (state.questDetailsToolsClosing && !state.questDetailsToolsOpen) {
            return;
        }
        if (!QuestsAndStuffConfig.toolsMenuAnimationsEnabled()) {
            finishQuestDetails(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details tools toggle open=false");
            return;
        }
        state.questDetailsToolsOpen = false;
        state.questDetailsToolsClosing = true;
        start(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details tools toggle open=false");
    }

    public static void finishQuestDetails(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.questDetailsToolsOpen = false;
        state.questDetailsToolsClosing = false;
    }

    public static boolean questDetailsVisible(TabletUiState state) {
        return state != null && (state.questDetailsToolsOpen || state.questDetailsToolsClosing);
    }

    public static boolean questDetailsInteractive(TabletUiState state) {
        return state != null && state.questDetailsToolsOpen && !state.questDetailsToolsClosing;
    }

    public static boolean questDetailsOpening(TabletUiState state) {
        return state != null && !state.questDetailsToolsClosing;
    }

    public static boolean finishClosingIfDone(TabletUiState state) {
        if (state == null) {
            return false;
        }
        boolean changed = false;
        if (state.toolsMenuClosing && closingFinished(state)) {
            finishMain(state);
            changed = true;
        }
        if (state.questDetailsToolsClosing && closingFinished(state)) {
            finishQuestDetails(state);
            changed = true;
        }
        return changed;
    }

    private static boolean closingFinished(TabletUiState state) {
        return !QuestsAndStuffConfig.toolsMenuAnimationsEnabled()
                || !AnchoredMenuRevealWidget.running(state.toolsMenuAnimationStartMs);
    }

    private static void start(TabletUiState state) {
        state.toolsMenuAnimationStartMs = System.currentTimeMillis();
    }
}
