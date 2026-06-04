package com.abo47.questsandstuff.client.tablet.modal;

public final class PickerTileMetrics {
    private PickerTileMetrics() {
    }

    public static Metrics calculate(int panelW, int panelH, int entryCount) {
        return calculate(panelW, panelH, entryCount, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static Metrics calculate(int panelW, int panelH, int entryCount, int maxTileW, int maxTileH) {
        int pad = 10;
        int gap = 10;
        int count = Math.max(1, entryCount);
        int contentW = Math.max(1, panelW - pad * 2);
        int contentH = Math.max(1, panelH - pad * 2);
        int bestCols = 1;
        int bestRows = count;
        int bestScore = Integer.MIN_VALUE;
        int maxCols = Math.max(1, Math.min(count, (contentW + gap) / (72 + gap)));
        for (int cols = 1; cols <= maxCols; cols++) {
            int rows = (count + cols - 1) / cols;
            int tileW = (contentW - gap * (cols - 1)) / cols;
            int tileH = (contentH - gap * (rows - 1)) / rows;
            if (tileW < 48 || tileH < 48) {
                continue;
            }
            int emptySlots = cols * rows - count;
            int balance = Math.min(tileW, tileH);
            int area = tileW * tileH / 100;
            int aspectPenalty = Math.abs(tileW - tileH) / 4;
            int score = balance * 10 + area - aspectPenalty - emptySlots * 12;
            if (score > bestScore) {
                bestScore = score;
                bestCols = cols;
                bestRows = rows;
            }
        }
        int tileW = Math.max(48, (contentW - gap * (bestCols - 1)) / bestCols);
        int tileH = Math.max(48, (contentH - gap * (bestRows - 1)) / bestRows);
        tileW = Math.min(tileW, Math.max(48, maxTileW));
        tileH = Math.min(tileH, Math.max(48, maxTileH));
        return new Metrics(tileW, tileH, gap, pad);
    }

    public record Metrics(int tileW, int tileH, int gap, int pad) {
    }
}
