package com.abo47.questsandstuff.quest.persistence.chapter;

import com.abo47.questsandstuff.util.naming.SafeNames;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextStyleSpan;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

    static List<CanvasExclusiveChoice> readCanvasExclusiveChoices(JsonElement element) {
        List<CanvasExclusiveChoice> choices = new ArrayList<>();
        if (element == null || !element.isJsonArray()) {
            return choices;
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
            int width = intOr(json, "w", CanvasExclusiveChoice.DEFAULT_WIDTH);
            int height = intOr(json, "h", CanvasExclusiveChoice.DEFAULT_HEIGHT);
            List<String> connections = readStringArray(json.get("connections"));
            List<String> prerequisites = readStringArray(json.get("prerequisites"));
            String background = stringOr(json, "background", "");
            Map<String, Integer> connectionColors = readIntMap(json.get("connection_colors"));
            Map<String, String> connectionModes = readStringMap(json.get("connection_modes"));
            Map<String, String> connectionTextures = readStringMap(json.get("connection_textures"));
            Map<String, Integer> connectionTextureSpacings = readIntMap(json.get("connection_texture_spacings"));
            Set<String> hiddenConnections = readStringSet(json.get("hidden_connections"));
            choices.add(new CanvasExclusiveChoice(
                    id,
                    intOr(json, "x", 0),
                    intOr(json, "y", 0),
                    width,
                    height,
                    intOr(json, "rotation", 0),
                    connections,
                    prerequisites,
                    background,
                    connectionColors,
                    connectionModes,
                    connectionTextures,
                    connectionTextureSpacings,
                    hiddenConnections
            ));
        }
        return choices;
    }

    static JsonArray writeCanvasExclusiveChoices(List<CanvasExclusiveChoice> choices) {
        JsonArray array = new JsonArray();
        for (CanvasExclusiveChoice ec : choices) {
            JsonObject json = new JsonObject();
            json.addProperty("id", ec.id());
            json.addProperty("x", ec.x());
            json.addProperty("y", ec.y());
            json.addProperty("w", ec.w());
            json.addProperty("h", ec.h());
            json.addProperty("rotation", ec.rotation());
            json.add("connections", writeStringArray(ec.connectionQuestIds()));
            if (!ec.prerequisiteQuestIds().isEmpty()) {
                json.add("prerequisites", writeStringArray(ec.prerequisiteQuestIds()));
            }
            if (!ec.background().isBlank()) {
                json.addProperty("background", ec.background());
            }
            if (!ec.connectionColors().isEmpty()) {
                json.add("connection_colors", writeIntMap(ec.connectionColors()));
            }
            if (!ec.connectionModes().isEmpty()) {
                json.add("connection_modes", writeStringMap(ec.connectionModes()));
            }
            if (!ec.connectionTextures().isEmpty()) {
                json.add("connection_textures", writeStringMap(ec.connectionTextures()));
            }
            if (!ec.connectionTextureSpacings().isEmpty()) {
                json.add("connection_texture_spacings", writeIntMap(ec.connectionTextureSpacings()));
            }
            if (!ec.hiddenConnections().isEmpty()) {
                json.add("hidden_connections", writeStringSet(ec.hiddenConnections()));
            }
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

    static Map<String, Integer> readIntMap(JsonElement element) {
        Map<String, Integer> map = new HashMap<>();
        if (element == null || !element.isJsonObject()) {
            return map;
        }
        JsonObject json = element.getAsJsonObject();
        for (String key : json.keySet()) {
            if (json.get(key).isJsonPrimitive() && json.get(key).getAsJsonPrimitive().isNumber()) {
                map.put(key, json.get(key).getAsInt());
            }
        }
        return map;
    }

    static JsonObject writeIntMap(Map<String, Integer> map) {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            json.addProperty(entry.getKey(), entry.getValue());
        }
        return json;
    }

    static Set<String> readStringSet(JsonElement element) {
        Set<String> values = new HashSet<>();
        if (element == null || !element.isJsonArray()) {
            return values;
        }
        for (JsonElement child : element.getAsJsonArray()) {
            String value = child.isJsonPrimitive() ? child.getAsString() : "";
            if (value != null && !value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    static JsonArray writeStringSet(Set<String> values) {
        JsonArray array = new JsonArray();
        if (values == null) {
            return array;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                array.add(value);
            }
        }
        return array;
    }

    static Map<String, String> readStringMap(JsonElement element) {
        Map<String, String> map = new HashMap<>();
        if (element == null || !element.isJsonObject()) {
            return map;
        }
        JsonObject json = element.getAsJsonObject();
        for (String key : json.keySet()) {
            if (json.get(key).isJsonPrimitive() && json.get(key).getAsJsonPrimitive().isString()) {
                String value = json.get(key).getAsString();
                if (!value.isBlank()) {
                    map.put(key, value);
                }
            }
        }
        return map;
    }

    static JsonObject writeStringMap(Map<String, String> map) {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            json.addProperty(entry.getKey(), entry.getValue());
        }
        return json;
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

    static String chapterFileName(String chapter) {
        return SafeNames.identifier(chapter, "default");
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
