package com.abo47.questsandstuff.util.naming;

import java.util.Locale;

public final class SafeNames {
    private SafeNames() {
    }

    public static String identifier(String value, String fallback) {
        return sanitize(value, fallback, false);
    }

    public static String fileStem(String value, String fallback) {
        return sanitize(value, fallback, true);
    }

    private static String sanitize(String value, String fallback, boolean allowDash) {
        String raw = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        StringBuilder out = new StringBuilder(raw.length());
        boolean lastUnderscore = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '_') {
                if (!lastUnderscore) {
                    out.append('_');
                    lastUnderscore = true;
                }
            } else if (isAsciiLetterOrDigit(c) || allowDash && c == '-') {
                out.append(c);
                lastUnderscore = false;
            } else if (!lastUnderscore) {
                out.append('_');
                lastUnderscore = true;
            }
        }
        String clean = trimUnderscores(out.toString());
        return clean.isBlank() ? fallback : clean;
    }

    private static boolean isAsciiLetterOrDigit(char c) {
        return c >= 'a' && c <= 'z' || c >= '0' && c <= '9';
    }

    private static String trimUnderscores(String value) {
        int start = 0;
        int end = value.length();
        while (start < end && value.charAt(start) == '_') {
            start++;
        }
        while (end > start && value.charAt(end - 1) == '_') {
            end--;
        }
        return value.substring(start, end);
    }
}
