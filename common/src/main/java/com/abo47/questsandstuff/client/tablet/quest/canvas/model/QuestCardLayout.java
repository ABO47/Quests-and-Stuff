package com.abo47.questsandstuff.client.tablet.quest.canvas.model;

import net.minecraft.nbt.CompoundTag;

public record QuestCardLayout(
        String questId,
        CompoundTag tag,
        int logicalX,
        int logicalY,
        int logicalWidth,
        int logicalHeight,
        int slotLogicalWidth,
        int slotLogicalHeight,
        int visualLogicalX,
        int visualLogicalY,
        float scale,
        int x,
        int y,
        int width,
        int height
) {
    public int logicalRight() {
        return visualLogicalX + logicalWidth;
    }

    public int logicalBottom() {
        return visualLogicalY + logicalHeight;
    }

    public double logicalCenterX() {
        return visualLogicalX + logicalWidth / 2.0;
    }

    public double logicalCenterY() {
        return visualLogicalY + logicalHeight / 2.0;
    }

    public int centerX() {
        return x + width / 2;
    }

    public int centerY() {
        return y + height / 2;
    }

    public boolean containsScreen(int screenX, int screenY) {
        return screenX >= x && screenY >= y && screenX <= x + width && screenY <= y + height;
    }
}
