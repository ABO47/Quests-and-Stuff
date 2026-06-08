package com.abo47.questsandstuff.client.tablet.quest.canvas.overlay;

public final class CanvasMiniNotificationState {
    private String translationKey = "";
    private int x;
    private int y;
    private long untilMs;

    public String translationKey() {
        return translationKey;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public long untilMs() {
        return untilMs;
    }

    public boolean active(long nowMs) {
        return !translationKey.isBlank() && nowMs < untilMs;
    }

    public void show(String translationKey, int x, int y, long nowMs, long durationMs) {
        String clean = translationKey == null ? "" : translationKey.trim();
        if (clean.isBlank()) {
            clear();
            return;
        }
        this.translationKey = clean;
        this.x = x;
        this.y = y;
        this.untilMs = nowMs + Math.max(0L, durationMs);
    }

    public void clear() {
        translationKey = "";
        x = 0;
        y = 0;
        untilMs = 0L;
    }
}
