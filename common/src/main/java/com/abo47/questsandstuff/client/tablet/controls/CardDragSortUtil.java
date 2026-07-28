package com.abo47.questsandstuff.client.tablet.controls;

import java.util.List;
import java.util.function.Function;

public final class CardDragSortUtil {
    private static final int DRAG_THRESHOLD_SQ = 9;

    private CardDragSortUtil() {
    }

    public static boolean pastDragThreshold(double mouseX, double mouseY, int startX, int startY) {
        int dx = (int) Math.round(mouseX) - startX;
        int dy = (int) Math.round(mouseY) - startY;
        return dx * dx + dy * dy >= DRAG_THRESHOLD_SQ;
    }

    public static int insertIndexAtY(int localY, int listY, int listBottom, int scroll, int pad, int cardHeight, int gap, int size) {
        if (localY < listY) {
            return 0;
        }
        if (localY > listBottom || size <= 0) {
            return Math.max(0, size);
        }
        int contentY = localY - listY + scroll - pad;
        if (contentY <= 0) {
            return 0;
        }
        int slot = Math.max(1, cardHeight + gap);
        int row = Math.max(0, contentY / slot);
        int within = contentY % slot;
        int index = row + (within > cardHeight / 2 ? 1 : 0);
        return Math.max(0, Math.min(size, index));
    }

    public static int targetIndexAfterDrop(int fromIndex, int insertIndex, int size) {
        if (fromIndex < 0 || size <= 0) {
            return -1;
        }
        int target = Math.max(0, insertIndex);
        if (target > fromIndex) {
            target--;
        }
        return Math.max(0, Math.min(size - 1, target));
    }

    public static <T> int offsetForDrop(String movingId, List<T> entries, Function<T, String> idGetter, int insertIndex) {
        if (movingId == null || movingId.isBlank() || entries == null || entries.isEmpty()) {
            return 0;
        }
        int fromIndex = -1;
        for (int i = 0; i < entries.size(); i++) {
            if (movingId.equals(idGetter.apply(entries.get(i)))) {
                fromIndex = i;
                break;
            }
        }
        int target = targetIndexAfterDrop(fromIndex, insertIndex, entries.size());
        return target < 0 ? 0 : target - fromIndex;
    }
}
