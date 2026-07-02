package com.abo47.questsandstuff.util.naming;

public final class QuestIdentity {
    public static final String DEFAULT_QUEST_ID = "main/untitled";
    public static final int UI_GROUP_NAME_MAX_LENGTH = 40;

    private QuestIdentity() {
    }

    public static String questId(String value) {
        String normalized = value == null ? "" : value.trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.contains("//")) {
            normalized = normalized.replace("//", "/");
        }
        return normalized;
    }

    public static String questIdOrDefault(String value) {
        String normalized = questId(value);
        return normalized.isBlank() ? DEFAULT_QUEST_ID : normalized;
    }

    public static String groupName(String value) {
        return value == null ? "" : value.trim();
    }

    public static String uiGroupName(String value) {
        String normalized = groupName(value).replace('\n', ' ').replace('\r', ' ');
        return normalized.length() > UI_GROUP_NAME_MAX_LENGTH
                ? normalized.substring(0, UI_GROUP_NAME_MAX_LENGTH)
                : normalized;
    }

    public static String groupFolderName(String value) {
        return SafeNames.identifier(groupName(value), "ungrouped");
    }
}
