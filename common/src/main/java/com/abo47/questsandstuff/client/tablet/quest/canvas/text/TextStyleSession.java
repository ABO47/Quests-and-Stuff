package com.abo47.questsandstuff.client.tablet.quest.canvas.text;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class TextStyleSession {
    public enum Surface {
        MAIN_CANVAS,
        QUEST_DETAILS
    }

    private TextStyleSession() {
    }

    public static void openMainCanvas(TabletUiState state, String textId) {
        state.canvasTextMenuOpen = true;
        state.canvasTextMenuTarget = safe(textId);
    }

    public static void closeMainCanvas(TabletUiState state) {
        state.canvasTextMenuOpen = false;
        state.canvasTextMenuTarget = "";
        state.canvasTextFontSizeFieldTarget = "";
    }

    public static void openQuestDetails(TabletUiState state, String textId) {
        state.questDetailsTextStyleOpen = true;
        state.questDetailsTextStyleTarget = safe(textId);
    }

    public static void closeQuestDetails(TabletUiState state) {
        state.questDetailsTextStyleOpen = false;
        state.questDetailsTextStyleTarget = "";
        state.questDetailsTextFontSizeFieldTarget = "";
        resetQuestDetailsBounds(state);
    }

    public static boolean questDetailsOpenOrEditingFont(TabletUiState state) {
        return state.questDetailsTextStyleOpen || !state.questDetailsTextFontSizeFieldTarget.isBlank();
    }

    public static void markQuestDetailsInteraction(TabletUiState state) {
        state.questDetailsTextStyleInteractionAtMs = System.currentTimeMillis();
    }

    public static void setFontSizeTarget(TabletUiState state, Surface surface, String target) {
        if (surface == Surface.QUEST_DETAILS) {
            state.questDetailsTextFontSizeFieldTarget = safe(target);
        } else {
            state.canvasTextFontSizeFieldTarget = safe(target);
        }
    }

    public static String fontSizeTarget(TabletUiState state, Surface surface) {
        return surface == Surface.QUEST_DETAILS ? state.questDetailsTextFontSizeFieldTarget : state.canvasTextFontSizeFieldTarget;
    }

    public static boolean isAnyFontSizeFieldOpen(TabletUiState state) {
        return !state.chapterTextFontSizeFieldTarget.isBlank()
                || !state.canvasTextFontSizeFieldTarget.isBlank()
                || !state.questDetailsTextFontSizeFieldTarget.isBlank();
    }

    public static void setQuestDetailsBounds(TabletUiState state, int x, int y, int width, int height) {
        state.questDetailsTextStyleMenuX = x;
        state.questDetailsTextStyleMenuY = y;
        state.questDetailsTextStyleMenuW = width;
        state.questDetailsTextStyleMenuH = height;
    }

    public static void resetQuestDetailsBounds(TabletUiState state) {
        state.questDetailsTextStyleMenuX = 0;
        state.questDetailsTextStyleMenuY = 0;
        state.questDetailsTextStyleMenuW = 0;
        state.questDetailsTextStyleMenuH = 0;
    }

    public static boolean recentlyHandledQuestDetailsClick(TabletUiState state, long nowMillis, long windowMillis) {
        long handledAt = state.questDetailsTextStyleInteractionAtMs;
        return handledAt > 0L && nowMillis - handledAt <= windowMillis;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
