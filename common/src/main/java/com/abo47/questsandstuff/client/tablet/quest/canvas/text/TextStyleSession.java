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
        state.canvas.canvasTextMenuOpen = true;
        state.canvas.canvasTextMenuTarget = safe(textId);
    }

    public static void closeMainCanvas(TabletUiState state) {
        state.canvas.canvasTextMenuOpen = false;
        state.canvas.canvasTextMenuTarget = "";
        state.canvas.canvasTextFontSizeFieldTarget = "";
    }

    public static void openQuestDetails(TabletUiState state, String textId) {
        state.questDetails.questDetailsTextStyleOpen = true;
        state.questDetails.questDetailsTextStyleTarget = safe(textId);
    }

    public static void closeQuestDetails(TabletUiState state) {
        state.questDetails.questDetailsTextStyleOpen = false;
        state.questDetails.questDetailsTextStyleTarget = "";
        state.questDetails.questDetailsTextFontSizeFieldTarget = "";
        resetQuestDetailsBounds(state);
    }

    public static boolean questDetailsOpenOrEditingFont(TabletUiState state) {
        return state.questDetails.questDetailsTextStyleOpen || !state.questDetails.questDetailsTextFontSizeFieldTarget.isBlank();
    }

    public static void markQuestDetailsInteraction(TabletUiState state) {
        state.questDetails.questDetailsTextStyleInteractionAtMs = System.currentTimeMillis();
    }

    public static void setFontSizeTarget(TabletUiState state, Surface surface, String target) {
        if (surface == Surface.QUEST_DETAILS) {
            state.questDetails.questDetailsTextFontSizeFieldTarget = safe(target);
        } else {
            state.canvas.canvasTextFontSizeFieldTarget = safe(target);
        }
    }

    public static String fontSizeTarget(TabletUiState state, Surface surface) {
        return surface == Surface.QUEST_DETAILS ? state.questDetails.questDetailsTextFontSizeFieldTarget : state.canvas.canvasTextFontSizeFieldTarget;
    }

    public static boolean isAnyFontSizeFieldOpen(TabletUiState state) {
        return !state.chapterPanel.chapterTextFontSizeFieldTarget.isBlank()
                || !state.canvas.canvasTextFontSizeFieldTarget.isBlank()
                || !state.questDetails.questDetailsTextFontSizeFieldTarget.isBlank();
    }

    public static void setQuestDetailsBounds(TabletUiState state, int x, int y, int width, int height) {
        state.questDetails.questDetailsTextStyleMenuX = x;
        state.questDetails.questDetailsTextStyleMenuY = y;
        state.questDetails.questDetailsTextStyleMenuW = width;
        state.questDetails.questDetailsTextStyleMenuH = height;
    }

    public static void resetQuestDetailsBounds(TabletUiState state) {
        state.questDetails.questDetailsTextStyleMenuX = 0;
        state.questDetails.questDetailsTextStyleMenuY = 0;
        state.questDetails.questDetailsTextStyleMenuW = 0;
        state.questDetails.questDetailsTextStyleMenuH = 0;
    }

    public static boolean recentlyHandledQuestDetailsClick(TabletUiState state, long nowMillis, long windowMillis) {
        long handledAt = state.questDetails.questDetailsTextStyleInteractionAtMs;
        return handledAt > 0L && nowMillis - handledAt <= windowMillis;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
