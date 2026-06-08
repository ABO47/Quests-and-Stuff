package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.nbt.CompoundTag;

final class CanvasQuestCardGeometry {
    private CanvasQuestCardGeometry() {
    }

    static QuestCardLayout layoutQuest(String questId, CompoundTag questTag, TabletUiState state, String selectedGroup) {
        CanvasPoint override = state.transientQuestPositions.get(questId);
        CompoundTag groupsTag = questTag.getCompound("groups");
        String resolvedGroup = selectedGroup == null ? "" : selectedGroup.trim();
        if (resolvedGroup.isBlank() || !groupsTag.contains(resolvedGroup)) {
            resolvedGroup = groupsTag.getAllKeys().stream().sorted().findFirst().orElse("");
        }

        CompoundTag groupTag = resolvedGroup.isBlank() ? new CompoundTag() : groupsTag.getCompound(resolvedGroup);
        int logicalX = groupTag.getInt("x");
        int logicalY = groupTag.getInt("y");
        if (override != null) {
            logicalX = override.x;
            logicalY = override.y;
        }

        float scale = scaleFromGroup(state, questId, groupTag);
        int visualLogicalW = CanvasGridMath.visualLogicalWidth(scale);
        int visualLogicalH = CanvasGridMath.visualLogicalHeight(scale);
        int slotLogicalW = CanvasGridMath.slotLogicalWidth(state, scale);
        int slotLogicalH = CanvasGridMath.slotLogicalHeight(state, scale);
        int visualLogicalX = logicalX + CanvasGridMath.visualInsetForSlot(slotLogicalW, visualLogicalW);
        int visualLogicalY = logicalY + CanvasGridMath.visualInsetForSlot(slotLogicalH, visualLogicalH);

        int slotScreenX = CanvasCoordinateMapper.screenX(state, logicalX);
        int slotScreenY = CanvasCoordinateMapper.screenY(state, logicalY);
        int slotScreenW = Math.max(1, CanvasCoordinateMapper.screenX(state, logicalX + slotLogicalW) - slotScreenX);
        int slotScreenH = Math.max(1, CanvasCoordinateMapper.screenY(state, logicalY + slotLogicalH) - slotScreenY);
        int screenW = visualScreenSize(state, visualLogicalW, slotLogicalW, slotScreenW);
        int screenH = visualScreenSize(state, visualLogicalH, slotLogicalH, slotScreenH);
        int screenX = slotScreenX + visualScreenInset(slotScreenW, screenW);
        int screenY = slotScreenY + visualScreenInset(slotScreenH, screenH);

        return new QuestCardLayout(
                questId,
                questTag,
                logicalX,
                logicalY,
                visualLogicalW,
                visualLogicalH,
                slotLogicalW,
                slotLogicalH,
                visualLogicalX,
                visualLogicalY,
                scale,
                screenX,
                screenY,
                screenW,
                screenH
        );
    }

    static CanvasPoint anchorForScreenVisualCenter(TabletUiState state, int screenX, int screenY, float scale) {
        return anchorForVisualCenter(state, CanvasCoordinateMapper.screenToLogicalX(state, screenX), CanvasCoordinateMapper.screenToLogicalY(state, screenY), scale);
    }

    static CanvasPoint anchorForVisualCenter(TabletUiState state, double logicalCenterX, double logicalCenterY, float scale) {
        int visualW = CanvasGridMath.visualLogicalWidth(scale);
        int visualH = CanvasGridMath.visualLogicalHeight(scale);
        int anchorX = (int) Math.round(logicalCenterX - CanvasGridMath.visualInsetForSlot(CanvasGridMath.slotLogicalWidth(state, scale), visualW) - visualW / 2.0);
        int anchorY = (int) Math.round(logicalCenterY - CanvasGridMath.visualInsetForSlot(CanvasGridMath.slotLogicalHeight(state, scale), visualH) - visualH / 2.0);
        return new CanvasPoint(anchorX, anchorY);
    }

    private static int visualScreenSize(TabletUiState state, int visualLogicalSize, int slotLogicalSize, int slotScreenSize) {
        int preferred = Math.max(1, Math.round(visualLogicalSize * CanvasCoordinateMapper.zoom(state)));
        int insideSlot = Math.max(1, slotScreenSize - CanvasGridMath.QUEST_CELL_MARGIN);
        if (visualLogicalSize + CanvasGridMath.QUEST_CELL_MARGIN >= slotLogicalSize) {
            return insideSlot;
        }
        return Math.max(1, Math.min(preferred, insideSlot));
    }

    private static int visualScreenInset(int slotScreenSize, int visualScreenSize) {
        if (slotScreenSize <= visualScreenSize) {
            return 0;
        }
        int centered = (slotScreenSize - visualScreenSize) / 2;
        return Math.min(slotScreenSize - visualScreenSize, Math.max(1, centered));
    }

    private static float scaleFromGroup(TabletUiState state, String questId, CompoundTag groupTag) {
        float scale = groupTag.contains("scale") ? groupTag.getFloat("scale") : 1.0f;
        Float transientScale = state.transientQuestScales.get(questId);
        if (transientScale != null && !Float.isNaN(transientScale) && !Float.isInfinite(transientScale)) {
            scale = transientScale;
        }
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            return 1.0f;
        }
        return Math.max(0.5f, scale);
    }
}
