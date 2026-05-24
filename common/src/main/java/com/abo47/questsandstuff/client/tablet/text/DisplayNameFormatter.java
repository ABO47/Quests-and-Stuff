package com.abo47.questsandstuff.client.tablet.text;

import java.util.Locale;
import java.util.Map;

public final class DisplayNameFormatter {
    private DisplayNameFormatter() {
    }

    public static String resourceLeaf(String id) {
        String clean = id == null ? "" : id.trim();
        if (clean.isBlank()) {
            return "";
        }
        int colon = clean.indexOf(':');
        if (colon >= 0) {
            clean = clean.substring(colon + 1);
        }
        clean = clean.replace('\\', '/');
        int slash = clean.lastIndexOf('/');
        if (slash >= 0 && slash + 1 < clean.length()) {
            clean = clean.substring(slash + 1);
        }
        return titleCase(clean.replace('_', ' '));
    }

    public static String lootTable(String lootTable, Map<String, String> displayNames) {
        String display = displayNames == null ? "" : displayNames.getOrDefault(lootTable, "");
        String source = display == null || display.isBlank() ? lootTable : display;
        String readable = resourceLeaf(source);
        return readable.isBlank() ? clean(lootTable) : readable;
    }

    public static String biome(String biome, Map<String, String> displayNames) {
        String display = displayNames == null ? "" : displayNames.getOrDefault(biome, "");
        String source = display == null || display.isBlank() ? biome : display;
        String readable = resourceLeaf(source);
        return readable.isBlank() ? clean(biome) : readable;
    }

    public static String advancement(String advancement, Map<String, String> displayNames) {
        String display = displayNames == null ? "" : displayNames.getOrDefault(advancement, "");
        if (display != null && !display.isBlank()) {
            return display.trim();
        }
        String readable = resourceLeaf(advancement);
        return readable.isBlank() ? clean(advancement) : readable;
    }

    public static String titleCase(String value) {
        String clean = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (clean.isBlank()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(clean.length());
        boolean upper = true;
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            if (Character.isWhitespace(c) || c == '-' || c == '/') {
                builder.append(' ');
                upper = true;
            } else {
                builder.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return collapseSpaces(builder.toString());
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static String collapseSpaces(String value) {
        String clean = value == null ? "" : value.trim();
        if (clean.isBlank()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(clean.length());
        boolean previousSpace = false;
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            if (c == ' ') {
                if (!previousSpace) {
                    builder.append(c);
                    previousSpace = true;
                }
            } else {
                builder.append(c);
                previousSpace = false;
            }
        }
        return builder.toString();
    }
}
