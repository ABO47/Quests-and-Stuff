package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

public record ConnectionLine(
        String edgeId,
        String sourceQuestId,
        String targetQuestId,
        int sourceX,
        int sourceY,
        int sourceW,
        int sourceH,
        int targetX,
        int targetY,
        int targetW,
        int targetH,
        int startX,
        int startY,
        int endX,
        int endY,
        boolean direct,
        boolean pending,
        int color,
        boolean hidden,
        int alpha
) {
}
