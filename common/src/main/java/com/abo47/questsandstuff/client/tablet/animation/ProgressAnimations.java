package com.abo47.questsandstuff.client.tablet.animation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ProgressAnimations {
    private static final long FILL_ANIMATION_MS = TabletAnimationTimings.PROGRESS_FILL_MS;
    private static final int MAX_ANIMATIONS = 512;
    private static final Map<String, ProgressMotion> ANIMATIONS = new LinkedHashMap<>();

    private ProgressAnimations() {
    }

    public static String key(String surface, String questId) {
        String safeSurface = surface == null || surface.isBlank() ? "quest" : surface;
        String safeQuestId = questId == null || questId.isBlank() ? "unknown" : questId;
        return safeSurface + ":" + safeQuestId;
    }

    public static void reset(String key) {
        if (key != null && !key.isBlank()) {
            ANIMATIONS.remove(key);
        }
    }

    public static float value(String key, float target) {
        String safeKey = key == null || key.isBlank() ? "unknown" : key;
        float safeTarget = Math.max(0.0f, Math.min(1.0f, target));
        long now = System.currentTimeMillis();
        ProgressMotion current = ANIMATIONS.get(safeKey);
        if (current == null) {
            ProgressMotion created = new ProgressMotion(0.0f, safeTarget, safeTarget > 0.0f ? now : 0L);
            ANIMATIONS.put(safeKey, created);
            trim();
            return created.value(now);
        }
        if (Math.abs(current.target - safeTarget) > 0.001f) {
            ProgressMotion next = new ProgressMotion(current.value(now), safeTarget, now);
            ANIMATIONS.put(safeKey, next);
            return next.value(now);
        }
        return current.value(now);
    }

    private static void trim() {
        while (ANIMATIONS.size() > MAX_ANIMATIONS) {
            var iterator = ANIMATIONS.keySet().iterator();
            if (!iterator.hasNext()) {
                return;
            }
            iterator.next();
            iterator.remove();
        }
    }

    private record ProgressMotion(float from, float target, long startMs) {
        float value(long now) {
            if (!UiAnimationProgress.running(startMs, FILL_ANIMATION_MS, now)) {
                return target;
            }
            float amount = UiAnimationProgress.easedProgress(startMs, FILL_ANIMATION_MS, now);
            return UiAnimationProgress.interpolate(from, target, amount);
        }
    }
}
