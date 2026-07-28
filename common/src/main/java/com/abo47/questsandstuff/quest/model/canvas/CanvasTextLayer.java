package com.abo47.questsandstuff.quest.model.canvas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public record CanvasTextLayer(String id, String text, int x, int y, int w, int h, int rotation, String align, String style, int color, int fontSize, List<CanvasTextStyleSpan> spans) {
    public static final int DEFAULT_FONT_SIZE = 9;
    public static final int MIN_FONT_SIZE = 1;
    public static final int MAX_FONT_SIZE = 100;

    public CanvasTextLayer(String id, String text, int x, int y, int w, int h, int rotation, String align, String style, int color) {
        this(id, text, x, y, w, h, rotation, align, style, color, DEFAULT_FONT_SIZE, List.of());
    }

    public CanvasTextLayer(String id, String text, int x, int y, int w, int h, int rotation, String align, String style, int color, List<CanvasTextStyleSpan> spans) {
        this(id, text, x, y, w, h, rotation, align, style, color, DEFAULT_FONT_SIZE, spans);
    }

    public CanvasTextLayer {
        id = id == null ? "" : id.trim();
        text = text == null ? "" : text;
        align = normalizeAlign(align);
        style = normalizeStyle(style);
        w = Math.max(24, w);
        h = Math.max(14, h);
        fontSize = clampFontSize(fontSize);
        spans = normalizeSpans(spans, text.length(), style, color);
    }

    public CanvasTextLayer moveTo(int nextX, int nextY) {
        return new CanvasTextLayer(id, text, nextX, nextY, w, h, rotation, align, style, color, fontSize, spans);
    }

    public CanvasTextLayer resizeTo(int nextW, int nextH) {
        return new CanvasTextLayer(id, text, x, y, Math.max(24, nextW), Math.max(14, nextH), rotation, align, style, color, fontSize, spans);
    }

    public CanvasTextLayer rotateTo(int nextRotation) {
        int normalized = ((nextRotation % 360) + 360) % 360;
        return new CanvasTextLayer(id, text, x, y, w, h, normalized, align, style, color, fontSize, spans);
    }

    public CanvasTextLayer withText(String nextText) {
        String value = nextText == null ? "" : nextText;
        return new CanvasTextLayer(id, value, x, y, w, h, rotation, align, style, color, fontSize, normalizeSpans(spans, value.length(), style, color));
    }

    public CanvasTextLayer replaceTextRange(int start, int end, String replacement) {
        int a = clampIndex(Math.min(start, end));
        int b = clampIndex(Math.max(start, end));
        String value = replacement == null ? "" : replacement;
        String nextText = text.substring(0, a) + value + text.substring(b);
        int delta = value.length() - (b - a);
        List<CanvasTextStyleSpan> nextSpans = new ArrayList<>();
        for (CanvasTextStyleSpan span : spans) {
            if (span.end() <= a) {
                nextSpans.add(span);
            } else if (span.start() >= b) {
                nextSpans.add(new CanvasTextStyleSpan(span.start() + delta, span.end() + delta, span.style(), span.color()));
            } else {
                if (span.start() < a) {
                    nextSpans.add(new CanvasTextStyleSpan(span.start(), a, span.style(), span.color()));
                }
                if (span.end() > b) {
                    nextSpans.add(new CanvasTextStyleSpan(a + value.length(), span.end() + delta, span.style(), span.color()));
                }
            }
        }
        return new CanvasTextLayer(id, nextText, x, y, w, h, rotation, align, style, color, fontSize, nextSpans);
    }

    public CanvasTextLayer withAlign(String nextAlign) {
        return new CanvasTextLayer(id, text, x, y, w, h, rotation, nextAlign, style, color, fontSize, spans);
    }

    public CanvasTextLayer withStyle(String nextStyle) {
        return new CanvasTextLayer(id, text, x, y, w, h, rotation, align, nextStyle, color, fontSize, spans);
    }

    public CanvasTextLayer withColor(int nextColor) {
        return new CanvasTextLayer(id, text, x, y, w, h, rotation, align, style, nextColor, fontSize, spans);
    }

    public CanvasTextLayer withFontSize(int nextFontSize) {
        return new CanvasTextLayer(id, text, x, y, w, h, rotation, align, style, color, nextFontSize, spans);
    }

    public CanvasTextLayer adjustFontSize(int delta) {
        return withFontSize(fontSize + delta);
    }

    public CanvasTextLayer withStyleRange(int start, int end, String nextStyle) {
        int a = clampIndex(Math.min(start, end));
        int b = clampIndex(Math.max(start, end));
        if (a == b) {
            return withStyle(nextStyle);
        }
        return withRangeOverride(a, b, normalizeStyle(nextStyle), null);
    }

    public CanvasTextLayer withColorRange(int start, int end, int nextColor) {
        int a = clampIndex(Math.min(start, end));
        int b = clampIndex(Math.max(start, end));
        if (a == b) {
            return withColor(nextColor);
        }
        return withRangeOverride(a, b, null, nextColor);
    }

    public CanvasTextLayer toggleStyleFlagRange(int start, int end, String flag) {
        int a = clampIndex(Math.min(start, end));
        int b = clampIndex(Math.max(start, end));
        if (a == b) {
            return withStyle(toggleStyleFlag(style, flag));
        }
        String normalizedFlag = normalizeStyleFlag(flag);
        if (normalizedFlag.isBlank()) {
            return this;
        }
        String[] styles = expandedStyles();
        int[] colors = expandedColors();
        boolean allEnabled = true;
        for (int i = a; i < b; i++) {
            if (!hasStyleFlag(styles[i], normalizedFlag)) {
                allEnabled = false;
                break;
            }
        }
        for (int i = a; i < b; i++) {
            styles[i] = setStyleFlag(styles[i], normalizedFlag, !allEnabled);
        }
        return new CanvasTextLayer(id, text, x, y, w, h, rotation, align, style, color, fontSize, spansFromArrays(styles, colors));
    }

    public boolean rangeHasStyleFlag(int start, int end, String flag) {
        int a = clampIndex(Math.min(start, end));
        int b = clampIndex(Math.max(start, end));
        String normalizedFlag = normalizeStyleFlag(flag);
        if (normalizedFlag.isBlank()) {
            return false;
        }
        if (a == b) {
            return hasStyleFlag(styleAt(a), normalizedFlag);
        }
        for (int i = a; i < b; i++) {
            if (!hasStyleFlag(styleAt(i), normalizedFlag)) {
                return false;
            }
        }
        return true;
    }

    public String styleAt(int index) {
        int safe = clampIndex(index);
        for (int i = spans.size() - 1; i >= 0; i--) {
            CanvasTextStyleSpan span = spans.get(i);
            if (span.contains(safe)) {
                return span.style();
            }
        }
        return style;
    }

    public int colorAt(int index) {
        int safe = clampIndex(index);
        for (int i = spans.size() - 1; i >= 0; i--) {
            CanvasTextStyleSpan span = spans.get(i);
            if (span.contains(safe)) {
                return span.color();
            }
        }
        return color;
    }

    private CanvasTextLayer withRangeOverride(int start, int end, String nextStyle, Integer nextColor) {
        String[] styles = expandedStyles();
        int[] colors = expandedColors();
        for (int i = start; i < end; i++) {
            if (nextStyle != null) {
                styles[i] = normalizeStyle(nextStyle);
            }
            if (nextColor != null) {
                colors[i] = nextColor;
            }
        }
        return new CanvasTextLayer(id, text, x, y, w, h, rotation, align, style, color, fontSize, spansFromArrays(styles, colors));
    }

    private String[] expandedStyles() {
        String[] styles = new String[text.length()];
        for (int i = 0; i < styles.length; i++) {
            styles[i] = style;
        }
        for (CanvasTextStyleSpan span : spans) {
            int start = Math.max(0, Math.min(span.start(), styles.length));
            int end = Math.max(start, Math.min(span.end(), styles.length));
            for (int i = start; i < end; i++) {
                styles[i] = span.style();
            }
        }
        return styles;
    }

    private int[] expandedColors() {
        int[] colors = new int[text.length()];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = color;
        }
        for (CanvasTextStyleSpan span : spans) {
            int start = Math.max(0, Math.min(span.start(), colors.length));
            int end = Math.max(start, Math.min(span.end(), colors.length));
            for (int i = start; i < end; i++) {
                colors[i] = span.color();
            }
        }
        return colors;
    }

    private List<CanvasTextStyleSpan> spansFromArrays(String[] styles, int[] colors) {
        List<CanvasTextStyleSpan> next = new ArrayList<>();
        int index = 0;
        while (index < text.length()) {
            String runStyle = normalizeStyle(styles[index]);
            int runColor = colors[index];
            int end = index + 1;
            while (end < text.length() && normalizeStyle(styles[end]).equals(runStyle) && colors[end] == runColor) {
                end++;
            }
            if (!runStyle.equals(style) || runColor != color) {
                next.add(new CanvasTextStyleSpan(index, end, runStyle, runColor));
            }
            index = end;
        }
        return next;
    }

    private int clampIndex(int index) {
        return Math.max(0, Math.min(index, text.length()));
    }

    private static String normalizeAlign(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "center", "right" -> normalized;
            default -> "left";
        };
    }

    public static int clampFontSize(int value) {
        return Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, value));
    }

    public static String normalizeStyle(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty() || "normal".equals(normalized)) {
            return "normal";
        }
        String[] parts = normalized.split("_");
        List<String> flags = new ArrayList<>();
        for (String part : parts) {
            String flag = normalizeStyleFlag(part);
            if (!flag.isEmpty()) {
                flags.add(flag);
            }
        }
        Collections.sort(flags);
        if (flags.isEmpty()) {
            return "normal";
        }
        return String.join("_", flags);
    }

    private static final List<String> KNOWN_FLAGS = Arrays.asList("bold", "italic", "underline", "strikethrough", "quote", "spoiler");

    public static boolean hasStyleFlag(String style, String flag) {
        String normalizedFlag = normalizeStyleFlag(flag);
        if (normalizedFlag.isEmpty()) {
            return false;
        }
        String normalized = normalizeStyle(style);
        if ("normal".equals(normalized)) {
            return false;
        }
        String[] parts = normalized.split("_");
        for (String part : parts) {
            if (part.equals(normalizedFlag)) {
                return true;
            }
        }
        return false;
    }

    public static String toggleStyleFlag(String style, String flag) {
        String normalizedFlag = normalizeStyleFlag(flag);
        if (normalizedFlag.isBlank()) {
            return normalizeStyle(style);
        }
        return setStyleFlag(style, normalizedFlag, !hasStyleFlag(style, normalizedFlag));
    }

    public static String setStyleFlag(String style, String flag, boolean enabled) {
        String normalizedFlag = normalizeStyleFlag(flag);
        if (normalizedFlag.isEmpty()) {
            return normalizeStyle(style);
        }
        List<String> flags = new ArrayList<>();
        String normalized = normalizeStyle(style);
        if (!"normal".equals(normalized)) {
            flags.addAll(Arrays.asList(normalized.split("_")));
        }
        if (enabled) {
            if (!flags.contains(normalizedFlag)) {
                flags.add(normalizedFlag);
            }
        } else {
            flags.remove(normalizedFlag);
        }
        Collections.sort(flags);
        if (flags.isEmpty()) {
            return "normal";
        }
        return String.join("_", flags);
    }

    public static String styleFromFlags(boolean bold, boolean italic) {
        return styleFromFlags(bold, italic, false, false, false, false);
    }

    public static String styleFromFlags(boolean bold, boolean italic, boolean underline, boolean strikethrough, boolean quote, boolean spoiler) {
        List<String> flags = new ArrayList<>();
        if (bold) flags.add("bold");
        if (italic) flags.add("italic");
        if (underline) flags.add("underline");
        if (strikethrough) flags.add("strikethrough");
        if (quote) flags.add("quote");
        if (spoiler) flags.add("spoiler");
        Collections.sort(flags);
        if (flags.isEmpty()) {
            return "normal";
        }
        return String.join("_", flags);
    }

    private static String normalizeStyleFlag(String flag) {
        String normalized = flag == null ? "" : flag.trim().toLowerCase(Locale.ROOT);
        if (KNOWN_FLAGS.contains(normalized)) {
            return normalized;
        }
        return "";
    }

    private static List<CanvasTextStyleSpan> normalizeSpans(List<CanvasTextStyleSpan> value, int textLength, String defaultStyle, int defaultColor) {
        if (value == null || value.isEmpty()) {
            return List.of();
        }
        List<CanvasTextStyleSpan> normalized = new ArrayList<>();
        for (CanvasTextStyleSpan span : value) {
            if (span == null) {
                continue;
            }
            CanvasTextStyleSpan clamped = span.clampToLength(textLength);
            if (clamped.start() == clamped.end()) {
                continue;
            }
            if (clamped.style().equals(defaultStyle) && clamped.color() == defaultColor) {
                continue;
            }
            normalized.add(clamped);
        }
        return List.copyOf(normalized);
    }
}
