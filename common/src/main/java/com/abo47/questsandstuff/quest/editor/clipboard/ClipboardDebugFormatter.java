package com.abo47.questsandstuff.quest.editor.clipboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ClipboardDebugFormatter {
    private ClipboardDebugFormatter() {
    }

    public static List<String> sortedStrings(Iterable<String> values) {
        if (values == null) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (String value : values) {
            if (value != null) {
                out.add(value);
            }
        }
        out.sort(String::compareTo);
        return out;
    }

    public static String sortedConnectionColors(Map<String, Integer> colors) {
        if (colors == null || colors.isEmpty()) {
            return "{}";
        }
        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : colors.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                entries.add(entry.getKey() + "=" + String.format(Locale.ROOT, "0x%08X", entry.getValue()));
            }
        }
        entries.sort(String::compareTo);
        return entries.toString();
    }

    public static String sortedStringMap(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return "{}";
        }
        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                entries.add(entry.getKey() + "=" + entry.getValue());
            }
        }
        entries.sort(String::compareTo);
        return entries.toString();
    }

    public static String clipboardSourceSummary(List<ClipboardSnapshot.Entry> entries) {
        if (entries == null || entries.isEmpty()) {
            return "[]";
        }
        List<String> out = new ArrayList<>();
        for (ClipboardSnapshot.Entry entry : entries) {
            if (entry != null) {
                out.add(entry.sourceId() + "@" + entry.sourceChapter() + "(" + entry.sourceX() + "," + entry.sourceY() + ")");
            }
        }
        out.sort(String::compareTo);
        return out.toString();
    }
}
