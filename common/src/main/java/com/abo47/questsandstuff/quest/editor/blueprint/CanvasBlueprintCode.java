package com.abo47.questsandstuff.quest.editor.blueprint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class CanvasBlueprintCode {
    public static final String PREFIX = "qnsbp1:";

    private CanvasBlueprintCode() {
    }

    public static String encode(CanvasBlueprint blueprint) {
        if (blueprint == null || blueprint.isEmpty()) {
            return "";
        }
        try {
            byte[] json = blueprint.toJson().getBytes(StandardCharsets.UTF_8);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(bytes)) {
                gzip.write(json);
            }
            return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes.toByteArray());
        } catch (Exception ignored) {
            return "";
        }
    }

    public static CanvasBlueprint decode(String code) {
        String raw = code == null ? "" : code.trim();
        if (raw.isBlank()) {
            return CanvasBlueprint.empty();
        }
        if (raw.startsWith("{")) {
            return CanvasBlueprint.fromJson(raw);
        }
        String clean = compact(raw);
        if (clean.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) {
            clean = clean.substring(PREFIX.length());
        }
        try {
            byte[] compressed = Base64.getUrlDecoder().decode(padBase64(clean));
            try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
                return CanvasBlueprint.fromJson(new String(gzip.readAllBytes(), StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {
            return CanvasBlueprint.empty();
        }
    }

    private static String compact(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!Character.isWhitespace(c)) {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static String padBase64(String value) {
        int missing = (4 - value.length() % 4) % 4;
        return missing == 0 ? value : value + "=".repeat(missing);
    }
}
