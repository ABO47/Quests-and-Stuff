package com.abo47.questsandstuff.client.tablet.quest.prerequisite;

record PrerequisiteConnectionRow(
        String sourceId,
        String targetId,
        String sourceTitle,
        String targetTitle,
        String otherTitle,
        String icon,
        PrerequisiteConnectionKind kind
) {
    String key() {
        return sourceId + "->" + targetId;
    }
}
