package com.abo47.questsandstuff.client.tablet.quest.canvas.overlay;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasMinimapGeometry;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

record CanvasMinimapSnapshot(
        List<CanvasMinimapRect> quests,
        List<CanvasMinimapConnection> connections,
        CanvasMinimapGeometry.Projection projection
) {
}

record CanvasMinimapRect(
        int x,
        int y,
        int w,
        int h,
        int color,
        int alpha,
        String questId,
        CompoundTag tag
) {
}

record CanvasMinimapConnection(
        float x1,
        float y1,
        float x2,
        float y2,
        int color,
        int alpha,
        boolean direct,
        List<CanvasPoint> projectedPath
) {
}
