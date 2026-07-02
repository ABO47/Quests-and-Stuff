package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.snap.CanvasSnapBounds;
import com.abo47.questsandstuff.client.tablet.quest.canvas.snap.CanvasSnapEngine;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
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

    static CanvasSnapEngine.Bounds boundsForExclusiveChoice(TabletUiState state, CanvasExclusiveChoice ec) {
        return CanvasSnapBounds.forExclusiveChoice(ec);
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
        return snap(state, moving, cards, group, movingQuestIds, movingImageIds, movingTextIds, Set.of());
    }

    static CanvasSnapEngine.SnapResult snap(
            TabletUiState state,
            CanvasSnapEngine.Bounds moving,
            List<QuestCardLayout> cards,
            String group,
            Set<String> movingQuestIds,
            Set<String> movingImageIds,
            Set<String> movingTextIds,
            Set<String> movingEcIds
    ) {
        state.canvas.snapGuideXVisible = false;
        state.canvas.snapGuideYVisible = false;
        if (!moving.valid() || (!state.canvas.centerSnapXEnabled && !state.canvas.centerSnapYEnabled && !state.canvas.objectSnapEnabled)) {
            return CanvasSnapEngine.SnapResult.NONE;
        }

        CanvasSnapEngine.SnapResult result = CanvasSnapEngine.snap(new CanvasSnapEngine.SnapContext(
                moving,
                snapTargets(state, cards, group, movingQuestIds, movingImageIds, movingTextIds, movingEcIds),
                new CanvasSnapEngine.SnapSettings(
                        state.canvas.centerSnapXEnabled,
                        state.canvas.centerSnapYEnabled,
                        state.canvas.objectSnapEnabled,
                        state.canvas.canvasContentW / 2.0D,
                        state.canvas.canvasContentH / 2.0D,
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
            Set<String> movingTextIds,
            Set<String> movingEcIds
    ) {
        if (!state.canvas.objectSnapEnabled) {
            return List.of();
        }
        List<CanvasSnapEngine.Bounds> targets = new ArrayList<>();
        for (QuestCardLayout card : cards) {
            if (!movingQuestIds.contains(card.questId())) {
                targets.add(CanvasSnapBounds.forQuestCard(card));
            }
        }
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(group, List.of())) {
            if (!movingImageIds.contains(image.id())) {
                targets.add(CanvasSnapBounds.forImage(image));
            }
        }
        for (CanvasTextLayer text : state.canvas.canvasTextsByChapter.getOrDefault(group, List.of())) {
            if (!movingTextIds.contains(text.id())) {
                targets.add(CanvasSnapBounds.forText(text));
            }
        }
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(group, List.of())) {
            if (!movingEcIds.contains(ec.id())) {
                targets.add(CanvasSnapBounds.forExclusiveChoice(ec));
            }
        }
        return targets;
    }

    private static void showGuides(TabletUiState state, CanvasSnapEngine.SnapResult result) {
        if (result.guideXVisible()) {
            state.canvas.snapGuideX = CanvasGeometry.screenX(state, result.guideX());
            state.canvas.snapGuideXVisible = true;
        }
        if (result.guideYVisible()) {
            state.canvas.snapGuideY = CanvasGeometry.screenY(state, result.guideY());
            state.canvas.snapGuideYVisible = true;
        }
    }

    private static int snapThresholdLogical(TabletUiState state) {
        float zoom = CanvasRenderer.clampZoom(state.canvas.canvasZoom);
        int screenThreshold = Math.max(1, Math.round(5.0f / zoom));
        if (!state.canvas.gridSnapLocked) {
            return screenThreshold;
        }
        int gridReach = Math.max(1, (CanvasGeometry.gridSize(state) + 1) / 2);
        return Math.max(screenThreshold, gridReach);
    }
}
