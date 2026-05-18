package com.abo47.questsandstuff.quest.model.canvas;

public record CanvasTextStyleSpan(int start, int end, String style, int color) {
    public CanvasTextStyleSpan {
        start = Math.max(0, start);
        end = Math.max(start, end);
        style = CanvasTextLayer.normalizeStyle(style);
    }

    public boolean contains(int index) {
        return index >= start && index < end;
    }

    public CanvasTextStyleSpan clampToLength(int length) {
        int safeLength = Math.max(0, length);
        return new CanvasTextStyleSpan(Math.min(start, safeLength), Math.min(end, safeLength), style, color);
    }
}
