package com.abo47.questsandstuff.client.tablet.contextmenu;

import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

public enum ActionTone {
    NEUTRAL,
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER;

    public int accentColor(int fallback) {
        return switch (this) {
            case NEUTRAL -> fallback == 0 ? TabletColors.TEXT_MUTED : fallback;
            case PRIMARY -> TabletColors.INTERACTIVE;
            case SUCCESS -> TabletColors.SUCCESS;
            case WARNING -> TabletColors.WARNING;
            case DANGER -> TabletColors.ERROR;
        };
    }

    public int defaultAccentColor() {
        return accentColor(0);
    }

    public boolean destructive() {
        return this == WARNING || this == DANGER;
    }

    public static ActionTone fromLegacyColor(int color) {
        if (color == TabletColors.ERROR) {
            return DANGER;
        }
        if (color == TabletColors.WARNING) {
            return WARNING;
        }
        if (color == TabletColors.SUCCESS) {
            return SUCCESS;
        }
        if (color == TabletColors.INTERACTIVE) {
            return PRIMARY;
        }
        return NEUTRAL;
    }
}
