package com.abo47.questsandstuff.util;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class QuestNaming {
    private static final Pattern AUTO_QUEST_ID = Pattern.compile("^quest_(\\d{4,})_(.+)$");

    private QuestNaming() {
    }

    public static String nextQuestId(String chapterName, Set<String> reservedIds) {
        String chapter = safePathSegment(chapterName, "chapter");
        Set<String> reserved = reservedIds == null ? Set.of() : reservedIds;
        int index = 1;
        while (true) {
            while (usesNumericQuestId(reserved, index, chapter)) {
                index++;
            }
            String id = "quest_" + fourDigit(index) + "_" + chapter;
            if (!reserved.contains(id)) {
                return id;
            }
            index++;
        }
    }

    public static boolean isAutoQuestId(String questId) {
        String normalized = questId == null ? "" : questId.trim();
        return AUTO_QUEST_ID.matcher(normalized).matches();
    }

    public static boolean isManagedEditorQuestId(String questId) {
        String normalized = questId == null ? "" : questId.trim();
        return isAutoQuestId(normalized);
    }

    public static String managedQuestFileName(String questId, String chapterName) {
        int numericId = numericQuestId(questId);
        if (numericId < 0) {
            return "";
        }
        return "quest_" + fourDigit(numericId) + "_" + safePathSegment(chapterName, "chapter") + ".json";
    }

    public static String chapterFolderName(String chapterName) {
        return safePathSegment(chapterName, "chapter");
    }

    public static String safePathSegment(String value, String fallback) {
        String normalized = normalizePathSegmentText(value);
        while (normalized.endsWith(".") || normalized.endsWith(" ")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isBlank()) {
            normalized = fallback == null || fallback.isBlank() ? "chapter" : fallback.trim();
        }
        return normalized;
    }

    private static String normalizePathSegmentText(String value) {
        String raw = value == null ? "" : value.trim();
        StringBuilder out = new StringBuilder(raw.length());
        boolean lastWhitespace = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (isInvalidPathChar(c)) {
                out.append('_');
                lastWhitespace = false;
            } else if (Character.isWhitespace(c)) {
                if (!lastWhitespace) {
                    out.append(' ');
                    lastWhitespace = true;
                }
            } else {
                out.append(c);
                lastWhitespace = false;
            }
        }
        return out.toString().trim();
    }

    private static boolean isInvalidPathChar(char c) {
        return c < 32 || c == '<' || c == '>' || c == ':' || c == '"' || c == '/'
                || c == '\\' || c == '|' || c == '?' || c == '*';
    }

    private static int numericQuestId(String questId) {
        String normalized = questId == null ? "" : questId.trim();
        Matcher auto = AUTO_QUEST_ID.matcher(normalized);
        if (auto.matches()) {
            return parsePositive(auto.group(1));
        }
        return -1;
    }

    private static boolean usesNumericQuestId(Set<String> reservedIds, int index, String chapterName) {
        if (reservedIds == null || reservedIds.isEmpty()) {
            return false;
        }
        String chapter = safePathSegment(chapterName, "chapter");
        for (String reservedId : reservedIds) {
            if (numericQuestId(reservedId) == index && questIdChapter(reservedId).equals(chapter)) {
                return true;
            }
        }
        return false;
    }

    private static String questIdChapter(String questId) {
        String normalized = questId == null ? "" : questId.trim();
        Matcher auto = AUTO_QUEST_ID.matcher(normalized);
        if (auto.matches()) {
            return safePathSegment(auto.group(2), "chapter");
        }
        return "";
    }

    private static int parsePositive(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : -1;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static String fourDigit(int value) {
        return String.format(Locale.ROOT, "%04d", Math.max(1, value));
    }
}
