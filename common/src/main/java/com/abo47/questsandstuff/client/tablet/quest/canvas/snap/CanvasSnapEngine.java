package com.abo47.questsandstuff.client.tablet.quest.canvas.snap;

import java.util.List;

public final class CanvasSnapEngine {
    private CanvasSnapEngine() {
    }

    public static SnapResult snap(SnapContext context) {
        if (context == null || context.moving() == null || context.settings() == null) {
            return SnapResult.NONE;
        }
        Bounds moving = context.moving();
        SnapSettings settings = context.settings();
        if (!moving.valid() || !settings.anyEnabled()) {
            return SnapResult.NONE;
        }

        int threshold = Math.max(0, settings.threshold());
        SnapChoice objectChoice = SnapChoice.empty(threshold);
        if (settings.objectSnap()) {
            for (Bounds target : context.objectTargets()) {
                if (target != null && target.valid()) {
                    objectChoice = snapToTarget(moving, target, threshold, objectChoice);
                }
            }
        }

        SnapChoice centerChoice = SnapChoice.empty(threshold);
        if (settings.centerSnapX()) {
            centerChoice = applyX(centerChoice, centerGuideOffset(
                    moving.left(),
                    moving.centerX(),
                    moving.right(),
                    settings.centerX(),
                    threshold
            ));
        }
        if (settings.centerSnapY()) {
            centerChoice = applyY(centerChoice, centerGuideOffset(
                    moving.top(),
                    moving.centerY(),
                    moving.bottom(),
                    settings.centerY(),
                    threshold
            ));
        }

        SnapChoice choice = objectChoice.withFallback(centerChoice);
        return new SnapResult(
                choice.offsetX(),
                choice.offsetY(),
                choice.hasX(),
                choice.hasY(),
                choice.targetX(),
                choice.targetY()
        );
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

    private static SnapOffset closestOffset(
            double movingStart,
            double movingCenter,
            double movingEnd,
            double targetStart,
            double targetCenter,
            double targetEnd,
            int threshold
    ) {
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

    public record Bounds(int left, int top, int right, int bottom) {
        public static Bounds invalid() {
            return new Bounds(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        }

        public boolean valid() {
            return left != Integer.MAX_VALUE && top != Integer.MAX_VALUE && right != Integer.MIN_VALUE && bottom != Integer.MIN_VALUE;
        }

        public double centerX() {
            return (left + right) / 2.0D;
        }

        public double centerY() {
            return (top + bottom) / 2.0D;
        }
    }

    public record SnapContext(Bounds moving, List<Bounds> objectTargets, SnapSettings settings) {
        public SnapContext {
            objectTargets = objectTargets == null ? List.of() : List.copyOf(objectTargets);
        }
    }

    public record SnapSettings(boolean centerSnapX, boolean centerSnapY, boolean objectSnap, double centerX, double centerY, int threshold) {
        boolean anyEnabled() {
            return centerSnapX || centerSnapY || objectSnap;
        }
    }

    public record SnapResult(int offsetX, int offsetY, boolean guideXVisible, boolean guideYVisible, double guideX, double guideY) {
        public static final SnapResult NONE = new SnapResult(0, 0, false, false, 0.0D, 0.0D);

        public boolean hasOffset() {
            return offsetX != 0 || offsetY != 0;
        }
    }

    private record SnapChoice(int offsetX, int offsetY, int bestX, int bestY, double targetX, double targetY, boolean hasX, boolean hasY) {
        static SnapChoice empty(int threshold) {
            return new SnapChoice(0, 0, threshold + 1, threshold + 1, 0.0D, 0.0D, false, false);
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
        private static final SnapOffset NONE = new SnapOffset(0, Integer.MAX_VALUE, 0.0D);

        private boolean valid() {
            return distance != Integer.MAX_VALUE;
        }
    }
}
