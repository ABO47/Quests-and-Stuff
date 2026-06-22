package com.abo47.questsandstuff.client.tablet.quest.prerequisite;

import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;

record PrerequisiteConnectionRow(
        String sourceId,
        String targetId,
        String sourceTitle,
        String targetTitle,
        String otherTitle,
        String icon,
        PrerequisiteConnectionKind kind,
        boolean exclusiveChoice
) {
    String key() {
        return QuestConnectionMetadata.edgeKey(sourceId, targetId);
    }
}
