package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.snap.CanvasSnapBounds;
import com.abo47.questsandstuff.client.tablet.quest.canvas.snap.CanvasSnapEngine;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class CanvasSmartSnapper {
    private CanvasSmartSnapper() {
    }

    static CanvasSnapEngine.Bounds boundsForImage(TabletUiState state, CanvasImageLayer image) {
        return CanvasSnapBounds.forImage(image);
    }

    static CanvasSnapEngine.Bounds boundsForText(TabletUiState state, CanvasTextLayer text) {
        return CanvasSnapBounds.forText(text);
    }

    static CanvasSnapEngine.SnapResult snap(
            TabletUiState state,
            CanvasSnapEngine.Bounds moving,
            List<QuestCardLayout> cards,
            String group,
            Set<String> movingQuestIds,
            Set<String> movingImageIds,
            Set<String> movingTextIds
    ) {
        state.snapGuideXVisible = false;
        state.snapGuideYVisible = false;
        if (!moving.valid() || (!state.centerSnapXEnabled && !state.centerSnapYEnabled && !state.objectSnapEnabled)) {
            return CanvasSnapEngine.SnapResult.NONE;
        }

        CanvasSnapEngine.SnapResult result = CanvasSnapEngine.snap(new CanvasSnapEngine.SnapContext(
                moving,
                snapTargets(state, cards, group, movingQuestIds, movingImageIds, movingTextIds),
                new CanvasSnapEngine.SnapSettings(
                        state.centerSnapXEnabled,
                        state.centerSnapYEnabled,
                        state.objectSnapEnabled,
                        state.canvasContentW / 2.0D,
                        state.canvasContentH / 2.0D,
                        snapThresholdLogical(state)
                )
        ));
        showGuides(state, result);
        return result;
    }

    private static List<CanvasSnapEngine.Bounds> snapTargets(
            TabletUiState state,
            List<QuestCardLayout> cards,
            String group,
            Set<String> movingQuestIds,
            Set<String> movingImageIds,
            Set<String> movingTextIds
    ) {
        if (!state.objectSnapEnabled) {
            return List.of();
        }
        List<CanvasSnapEngine.Bounds> targets = new ArrayList<>();
        for (QuestCardLayout card : cards) {
            if (!movingQuestIds.contains(card.questId())) {
                targets.add(CanvasSnapBounds.forQuestCard(card));
            }
        }
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
            if (!movingImageIds.contains(image.id())) {
                targets.add(CanvasSnapBounds.forImage(image));
            }
        }
        for (CanvasTextLayer text : state.canvasTextsByGroup.getOrDefault(group, List.of())) {
            if (!movingTextIds.contains(text.id())) {
                targets.add(CanvasSnapBounds.forText(text));
            }
        }
        return targets;
    }

    private static void showGuides(TabletUiState state, CanvasSnapEngine.SnapResult result) {
        if (result.guideXVisible()) {
            state.snapGuideX = CanvasGeometry.screenX(state, result.guideX());
            state.snapGuideXVisible = true;
        }
        if (result.guideYVisible()) {
            state.snapGuideY = CanvasGeometry.screenY(state, result.guideY());
            state.snapGuideYVisible = true;
        }
    }

    private static int snapThresholdLogical(TabletUiState state) {
        float zoom = CanvasRenderer.clampZoom(state.canvasZoom);
        int screenThreshold = Math.max(1, Math.round(5.0f / zoom));
        if (!state.gridSnapLocked) {
            return screenThreshold;
        }
        int gridReach = Math.max(1, (CanvasGeometry.gridSize(state) + 1) / 2);
        return Math.max(screenThreshold, gridReach);
    }
}
