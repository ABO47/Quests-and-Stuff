package com.abo47.questsandstuff.quest.persistence.chapter;

import com.abo47.questsandstuff.util.SafeNames;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextStyleSpan;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class ChapterMetadataJsonCodec {
    private ChapterMetadataJsonCodec() {
    }

    static List<CanvasImageLayer> readCanvasImages(JsonElement element) {
        List<CanvasImageLayer> images = new ArrayList<>();
        if (element == null || !element.isJsonArray()) {
            return images;
        }
        for (JsonElement child : element.getAsJsonArray()) {
            if (!child.isJsonObject()) {
                continue;
            }
            JsonObject json = child.getAsJsonObject();
            String id = stringOr(json, "id", "");
            if (id.isBlank()) {
                continue;
            }
            int width = intOr(json, "w", 80);
            int height = intOr(json, "h", 80);
            images.add(new CanvasImageLayer(
                    id,
                    stringOr(json, "asset", ""),
                    intOr(json, "x", 0),
                    intOr(json, "y", 0),
                    width,
                    height,
                    intOr(json, "rotation", 0),
                    intOr(json, "entity_yaw", CanvasImageLayer.DEFAULT_ENTITY_YAW),
                    intOr(json, "entity_spin_speed", CanvasImageLayer.DEFAULT_ENTITY_SPIN_SPEED),
                    intOr(json, "model_pitch", CanvasImageLayer.DEFAULT_MODEL_PITCH),
                    intOr(json, "pivot_x", width / 2),
                    intOr(json, "pivot_y", height / 2)
            ));
        }
        return images;
    }

    static JsonArray writeCanvasImages(List<CanvasImageLayer> images) {
        JsonArray array = new JsonArray();
        for (CanvasImageLayer image : images) {
            JsonObject json = new JsonObject();
            json.addProperty("id", image.id());
            json.addProperty("asset", image.asset());
            json.addProperty("x", image.x());
            json.addProperty("y", image.y());
            json.addProperty("w", image.w());
            json.addProperty("h", image.h());
            json.addProperty("rotation", image.rotation());
            json.addProperty("entity_yaw", image.entityYaw());
            json.addProperty("entity_spin_speed", image.entitySpinSpeed());
            json.addProperty("model_pitch", image.modelPitch());
            json.addProperty("pivot_x", image.pivotX());
            json.addProperty("pivot_y", image.pivotY());
            array.add(json);
        }
        return array;
    }

    static List<CanvasTextLayer> readCanvasTexts(JsonElement element) {
        List<CanvasTextLayer> texts = new ArrayList<>();
        if (element == null || !element.isJsonArray()) {
            return texts;
        }
        for (JsonElement child : element.getAsJsonArray()) {
            if (!child.isJsonObject()) {
                continue;
            }
            JsonObject json = child.getAsJsonObject();
            String id = stringOr(json, "id", "");
            if (id.isBlank()) {
                continue;
            }
            texts.add(new CanvasTextLayer(id, stringOr(json, "text", ""), intOr(json, "x", 0), intOr(json, "y", 0), intOr(json, "w", 120), intOr(json, "h", 32), intOr(json, "rotation", 0), stringOr(json, "align", "left"), stringOr(json, "style", "normal"), intOr(json, "color", 0xFFFFFFFF), intOr(json, "font_size", CanvasTextLayer.DEFAULT_FONT_SIZE), readTextSpans(json.get("spans"))));
        }
        return texts;
    }

    static JsonArray writeCanvasTexts(List<CanvasTextLayer> texts) {
        JsonArray array = new JsonArray();
        for (CanvasTextLayer text : texts) {
            JsonObject json = new JsonObject();
            json.addProperty("id", text.id());
            json.addProperty("text", text.text());
            json.addProperty("x", text.x());
            json.addProperty("y", text.y());
            json.addProperty("w", text.w());
            json.addProperty("h", text.h());
            json.addProperty("rotation", text.rotation());
            json.addProperty("align", text.align());
            json.addProperty("style", text.style());
            json.addProperty("color", text.color());
            json.addProperty("font_size", text.fontSize());
            JsonArray spans = new JsonArray();
            for (CanvasTextStyleSpan span : text.spans()) {
                JsonObject spanJson = new JsonObject();
                spanJson.addProperty("start", span.start());
                spanJson.addProperty("end", span.end());
                spanJson.addProperty("style", span.style());
                spanJson.addProperty("color", span.color());
                spans.add(spanJson);
            }
            json.add("spans", spans);
            array.add(json);
        }
        return array;
    }

    static List<String> readStringArray(JsonElement element) {
        List<String> values = new ArrayList<>();
        if (element == null || !element.isJsonArray()) {
            return values;
        }
        for (JsonElement child : element.getAsJsonArray()) {
            String value = child.isJsonPrimitive() ? child.getAsString() : "";
            if (value != null && !value.isBlank() && !values.contains(value)) {
                values.add(value);
            }
        }
        return values;
    }

    static JsonArray writeStringArray(List<String> values) {
        JsonArray array = new JsonArray();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                array.add(value);
            }
        }
        return array;
    }

    static String stripJsonExtension(String name) {
        return name.toLowerCase(Locale.ROOT).endsWith(".json") ? name.substring(0, name.length() - 5) : name;
    }

    static String normalizeTextStyle(String style) {
        return switch (style == null ? "" : style.trim().toLowerCase(Locale.ROOT)) {
            case "bold", "italic", "bold_italic" -> style.trim().toLowerCase(Locale.ROOT);
            default -> "normal";
        };
    }

    static String normalizeTextAlign(String align) {
        String normalized = align == null ? "" : align.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "left", "right", "center" -> normalized;
            default -> "center";
        };
    }

    static String groupFileName(String group) {
        return SafeNames.identifier(group, "ungrouped");
    }

    private static List<CanvasTextStyleSpan> readTextSpans(JsonElement element) {
        List<CanvasTextStyleSpan> spans = new ArrayList<>();
        if (element == null || !element.isJsonArray()) {
            return spans;
        }
        for (JsonElement child : element.getAsJsonArray()) {
            if (!child.isJsonObject()) {
                continue;
            }
            JsonObject json = child.getAsJsonObject();
            spans.add(new CanvasTextStyleSpan(intOr(json, "start", 0), intOr(json, "end", 0), stringOr(json, "style", "normal"), intOr(json, "color", 0xFFFFFFFF)));
        }
        return spans;
    }

    private static String stringOr(JsonObject json, String key, String fallback) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : fallback;
    }

    private static int intOr(JsonObject json, String key, int fallback) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : fallback;
    }
}
