package com.abo47.questsandstuff.client.tablet.theme.tokens;

import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import java.util.Locale;

public final class UiActionColors {
    private UiActionColors() {
    }

    public static int forAction(String label, String icon, int fallback) {
        String key = normalize(icon);
        String text = normalize(label);
        if (negative(key, text)) {
            return TabletColors.ERROR;
        }
        if (warning(key, text)) {
            return TabletColors.WARNING;
        }
        if (positive(key, text)) {
            return TabletColors.SUCCESS;
        }
        if (interactive(key, text)) {
            return TabletColors.INTERACTIVE;
        }
        return fallback;
    }

    private static boolean negative(String key, String text) {
        return containsAny(key, "delete", "trash", "remove")
                || containsAny(text, "delete", "remove", "clear");
    }

    private static boolean warning(String key, String text) {
        return containsAny(key, "warning", "reset", "close", "cut", "eye-off", "repeat-off")
                || containsToken(key, "lock")
                || containsAny(text, "reset", "cancel", "hide", "disable", "non-repeatable", "not repeatable")
                || containsToken(text, "lock");
    }

    private static boolean positive(String key, String text) {
        return containsAny(key, "add", "paste", "connect", "unlock", "reveal", "claim", "manual_check")
                || containsAny(text, "add", "create", "new", "paste", "connect", "unlock", "reveal", "show", "save", "use");
    }

    private static boolean interactive(String key, String text) {
        return containsAny(key, "edit", "rename", "change", "open", "picker", "background", "icon", "style", "variant", "motion", "focus", "backpack", "selectable", "audio", "color")
                || containsAny(text, "edit", "rename", "change", "open", "pick", "choose", "background", "icon", "style", "variant", "motion", "fit", "align", "select");
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsToken(String value, String token) {
        int start = 0;
        while (start < value.length()) {
            while (start < value.length() && isTokenSeparator(value.charAt(start))) {
                start++;
            }
            int end = start;
            while (end < value.length() && !isTokenSeparator(value.charAt(end))) {
                end++;
            }
            if (end - start == token.length() && value.regionMatches(start, token, 0, token.length())) {
                return true;
            }
            start = end + 1;
        }
        return false;
    }

    private static boolean isTokenSeparator(char c) {
        return !((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
