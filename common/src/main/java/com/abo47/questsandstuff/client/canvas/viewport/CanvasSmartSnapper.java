package com.abo47.questsandstuff.client.canvas.viewport;

import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;
import java.util.Set;

final class CanvasSmartSnapper {
    private CanvasSmartSnapper() {
    }

    static Bounds boundsForImage(TabletUiState state, CanvasImageLayer image) {
        int[] bounds = CanvasElementGeometry.logicalBoundsAtPivot(image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
        return new Bounds(bounds[0], bounds[1], bounds[2], bounds[3]);
    }

    static Bounds boundsForText(TabletUiState state, CanvasTextLayer text) {
        int[] bounds = CanvasGeometry.rotatedBounds(text.x(), text.y(), text.w(), text.h(), text.rotation());
        return new Bounds(bounds[0], bounds[1], bounds[2], bounds[3]);
    }

    static SnapResult snap(
            TabletUiState state,
            Bounds moving,
            List<QuestCardLayout> cards,
            String group,
            Set<String> movingQuestIds,
            Set<String> movingImageIds,
            Set<String> movingTextIds
    ) {
        state.snapGuideXVisible = false;
        state.snapGuideYVisible = false;
        if (!moving.valid() || (!state.centerSnapXEnabled && !state.centerSnapYEnabled && !state.objectSnapEnabled)) {
            return SnapResult.NONE;
        }

        int threshold = snapThresholdLogical(state);
        SnapChoice objectChoice = SnapChoice.empty(threshold);
        if (state.objectSnapEnabled) {
            for (QuestCardLayout card : cards) {
                if (movingQuestIds.contains(card.questId())) {
                    continue;
                }
                objectChoice = snapToTarget(moving, new Bounds(
                        card.logicalX(),
                        card.logicalY(),
                        card.logicalX() + card.slotLogicalWidth(),
                        card.logicalY() + card.slotLogicalHeight()
                ), threshold, objectChoice);
            }
            for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
                if (movingImageIds.contains(image.id())) {
                    continue;
                }
                objectChoice = snapToTarget(moving, boundsForImage(state, image), threshold, objectChoice);
            }
            for (CanvasTextLayer text : state.canvasTextsByGroup.getOrDefault(group, List.of())) {
                if (movingTextIds.contains(text.id())) {
                    continue;
                }
                objectChoice = snapToTarget(moving, boundsForText(state, text), threshold, objectChoice);
            }
        }

        SnapChoice centerChoice = SnapChoice.empty(threshold);
        if (state.centerSnapXEnabled) {
            centerChoice = applyX(centerChoice, centerGuideOffset(moving.left(), moving.centerX(), moving.right(), state.canvasContentW / 2.0, threshold));
        }
        if (state.centerSnapYEnabled) {
            centerChoice = applyY(centerChoice, centerGuideOffset(moving.top(), moving.centerY(), moving.bottom(), state.canvasContentH / 2.0, threshold));
        }

        SnapChoice choice = objectChoice.withFallback(centerChoice);
        showGuides(state, choice);
        return new SnapResult(choice.offsetX(), choice.offsetY());
    }

    private static SnapChoice snapToTarget(Bounds moving, Bounds target, int threshold, SnapChoice choice) {
        SnapOffset x = closestOffset(
                moving.left(),
                moving.centerX(),
                moving.right(),
                target.left(),
                target.centerX(),
                target.right(),
                threshold
        );
        choice = applyX(choice, x);
        SnapOffset y = closestOffset(
                moving.top(),
                moving.centerY(),
                moving.bottom(),
                target.top(),
                target.centerY(),
                target.bottom(),
                threshold
        );
        return applyY(choice, y);
    }

    private static SnapChoice applyX(SnapChoice choice, SnapOffset x) {
        if (!x.valid() || x.distance() >= choice.bestX()) {
            return choice;
        }
        return new SnapChoice(x.offset(), choice.offsetY(), x.distance(), choice.bestY(), x.target(), choice.targetY(), true, choice.hasY());
    }

    private static SnapChoice applyY(SnapChoice choice, SnapOffset y) {
        if (!y.valid() || y.distance() >= choice.bestY()) {
            return choice;
        }
        return new SnapChoice(choice.offsetX(), y.offset(), choice.bestX(), y.distance(), choice.targetX(), y.target(), choice.hasX(), true);
    }

    private static void showGuides(TabletUiState state, SnapChoice choice) {
        if (choice.hasX()) {
            state.snapGuideX = CanvasGeometry.screenX(state, choice.targetX());
            state.snapGuideXVisible = true;
        }
        if (choice.hasY()) {
            state.snapGuideY = CanvasGeometry.screenY(state, choice.targetY());
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

    private static SnapOffset closestOffset(double movingStart, double movingCenter, double movingEnd, double targetStart, double targetCenter, double targetEnd, int threshold) {
        SnapOffset best = SnapOffset.NONE;
        best = bestOf(best, movingStart, targetStart, threshold);
        best = bestOf(best, movingStart, targetCenter, threshold);
        best = bestOf(best, movingStart, targetEnd, threshold);
        best = bestOf(best, movingCenter, targetStart, threshold);
        best = bestOf(best, movingCenter, targetCenter, threshold);
        best = bestOf(best, movingCenter, targetEnd, threshold);
        best = bestOf(best, movingEnd, targetStart, threshold);
        best = bestOf(best, movingEnd, targetCenter, threshold);
        best = bestOf(best, movingEnd, targetEnd, threshold);
        return best;
    }

    private static SnapOffset centerGuideOffset(double movingStart, double movingCenter, double movingEnd, double target, int threshold) {
        return closestOffset(movingStart, movingCenter, movingEnd, target, target, target, threshold);
    }

    private static SnapOffset bestOf(SnapOffset current, double moving, double target, int threshold) {
        int offset = (int) Math.round(target - moving);
        int distance = Math.abs(offset);
        if (distance > threshold || distance >= current.distance()) {
            return current;
        }
        return new SnapOffset(offset, distance, target);
    }

    record Bounds(int left, int top, int right, int bottom) {
        boolean valid() {
            return left != Integer.MAX_VALUE && top != Integer.MAX_VALUE && right != Integer.MIN_VALUE && bottom != Integer.MIN_VALUE;
        }

        double centerX() {
            return (left + right) / 2.0;
        }

        double centerY() {
            return (top + bottom) / 2.0;
        }
    }

    record SnapResult(int offsetX, int offsetY) {
        static final SnapResult NONE = new SnapResult(0, 0);

        boolean hasOffset() {
            return offsetX != 0 || offsetY != 0;
        }
    }

    private record SnapChoice(int offsetX, int offsetY, int bestX, int bestY, double targetX, double targetY, boolean hasX, boolean hasY) {
        static SnapChoice empty(int threshold) {
            return new SnapChoice(0, 0, threshold + 1, threshold + 1, 0.0, 0.0, false, false);
        }

        SnapChoice withFallback(SnapChoice fallback) {
            if (fallback == null) {
                return this;
            }
            return new SnapChoice(
                    hasX ? offsetX : fallback.offsetX(),
                    hasY ? offsetY : fallback.offsetY(),
                    hasX ? bestX : fallback.bestX(),
                    hasY ? bestY : fallback.bestY(),
                    hasX ? targetX : fallback.targetX(),
                    hasY ? targetY : fallback.targetY(),
                    hasX || fallback.hasX(),
                    hasY || fallback.hasY()
            );
        }
    }

    private record SnapOffset(int offset, int distance, double target) {
        private static final SnapOffset NONE = new SnapOffset(0, Integer.MAX_VALUE, 0.0);

        private boolean valid() {
            return distance != Integer.MAX_VALUE;
        }
    }
}
