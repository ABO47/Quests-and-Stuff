package com.abo47.questsandstuff.client.tablet.theme;

import java.util.Locale;

public final class UiActionColors {
    private UiActionColors() {
    }

    public static int forAction(String label, String icon, int fallback) {
        String key = normalize(icon);
        String text = normalize(label);
        if (negative(key, text)) {
            return ModColors.ERROR;
        }
        if (warning(key, text)) {
            return ModColors.WARNING;
        }
        if (positive(key, text)) {
            return ModColors.SUCCESS;
        }
        if (interactive(key, text)) {
            return ModColors.INTERACTIVE;
        }
        return fallback;
    }

    private static boolean negative(String key, String text) {
        return containsAny(key, "delete", "trash", "remove")
                || containsAny(text, "delete", "remove", "clear");
    }

    private static boolean warning(String key, String text) {
        return containsAny(key, "warning", "reset", "close", "cut", "eye-off", "lock", "repeat-off")
                || containsAny(text, "reset", "cancel", "hide", "lock", "disable", "non-repeatable", "not repeatable");
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

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
