package com.abo47.questsandstuff.util;

public final class MathUtils {
    public static int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    private MathUtils() {
    }
}
