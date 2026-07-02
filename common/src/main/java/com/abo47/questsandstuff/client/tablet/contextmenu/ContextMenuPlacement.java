package com.abo47.questsandstuff.client.tablet.contextmenu;

public final class ContextMenuPlacement {
    private static final int PAD = 4;

    private ContextMenuPlacement() {
    }

    public static int fitRightOrLeft(int pointerX, int availableW, int menuW) {
        int min = PAD;
        int max = maxPosition(availableW, menuW);
        if (pointerX + menuW <= availableW - PAD) {
            return clamp(pointerX, min, max);
        }
        if (pointerX - menuW >= PAD) {
            return clamp(pointerX - menuW, min, max);
        }
        return clamp(pointerX - menuW / 2, min, max);
    }

    public static int fitBelowOrAbove(int pointerY, int availableH, int menuH) {
        int min = PAD;
        int max = maxPosition(availableH, menuH);
        if (pointerY + menuH <= availableH - PAD) {
            return clamp(pointerY, min, max);
        }
        if (pointerY - menuH >= PAD) {
            return clamp(pointerY - menuH, min, max);
        }
        return clamp(pointerY - menuH / 2, min, max);
    }

    private static int maxPosition(int available, int size) {
        return Math.max(PAD, available - size - PAD);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
