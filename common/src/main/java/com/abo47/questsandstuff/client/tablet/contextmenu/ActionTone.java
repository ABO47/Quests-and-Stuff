package com.abo47.questsandstuff.client.tablet.contextmenu;

import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;

public enum ActionTone {
    NEUTRAL,
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER;

    public int accentColor(int fallback) {
        return switch (this) {
            case NEUTRAL -> fallback == 0 ? ModColors.TEXT_MUTED : fallback;
            case PRIMARY -> ModColors.INTERACTIVE;
            case SUCCESS -> ModColors.SUCCESS;
            case WARNING -> ModColors.WARNING;
            case DANGER -> ModColors.ERROR;
        };
    }

    public int defaultAccentColor() {
        return accentColor(0);
    }

    public boolean destructive() {
        return this == WARNING || this == DANGER;
    }

    public static ActionTone fromLegacyColor(int color) {
        if (color == ModColors.ERROR) {
            return DANGER;
        }
        if (color == ModColors.WARNING) {
            return WARNING;
        }
        if (color == ModColors.SUCCESS) {
            return SUCCESS;
        }
        if (color == ModColors.INTERACTIVE) {
            return PRIMARY;
        }
        return NEUTRAL;
    }
}
