package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMode;

record ConnectionRenderStyle(
        String edgeId,
        String sourceQuestId,
        String targetQuestId,
        int color,
        QuestConnectionMode mode,
        boolean hidden,
        int alpha,
        String texture,
        int textureSpacing
) {
    static final int VISIBLE_ALPHA = 245;
    static final int HIDDEN_ALPHA = 64;

    static ConnectionRenderStyle fromMetadata(QuestConnectionMetadata metadata) {
        return new ConnectionRenderStyle(
                metadata.edgeKey(),
                metadata.sourceQuestId(),
                metadata.targetQuestId(),
                metadata.color(),
                metadata.mode(),
                metadata.hidden(),
                metadata.hidden() ? HIDDEN_ALPHA : VISIBLE_ALPHA,
                metadata.texture(),
                metadata.textureSpacing()
        );
    }

    boolean direct() {
        return mode != QuestConnectionMode.GRID;
    }
}
