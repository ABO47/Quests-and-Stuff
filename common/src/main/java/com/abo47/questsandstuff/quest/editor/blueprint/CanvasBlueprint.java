package com.abo47.questsandstuff.quest.editor.blueprint;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbt;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public record CanvasBlueprint(
        int schema,
        String name,
        int originX,
        int originY,
        List<QuestEntry> quests,
        List<CanvasImageLayer> images,
        List<CanvasTextLayer> texts,
        List<String> layerOrder
) {
    public static final int CURRENT_SCHEMA = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public CanvasBlueprint {
        schema = schema <= 0 ? CURRENT_SCHEMA : schema;
        name = name == null ? "" : name.trim();
        quests = quests == null ? List.of() : List.copyOf(quests);
        images = images == null ? List.of() : List.copyOf(images);
        texts = texts == null ? List.of() : List.copyOf(texts);
        layerOrder = layerOrder == null ? List.of() : List.copyOf(layerOrder);
    }

    public CanvasBlueprint(String name, int originX, int originY, List<QuestEntry> quests, List<CanvasImageLayer> images, List<CanvasTextLayer> texts, List<String> layerOrder) {
        this(CURRENT_SCHEMA, name, originX, originY, quests, images, texts, layerOrder);
    }

    public boolean isEmpty() {
        return quests.isEmpty() && images.isEmpty() && texts.isEmpty();
    }

    public int contentCount() {
        return quests.size() + images.size() + texts.size();
    }

    public CompoundTag toPacketTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema", schema);
        tag.putString("name", name);
        tag.putInt("origin_x", originX);
        tag.putInt("origin_y", originY);
        ListTag questTags = new ListTag();
        for (QuestEntry quest : quests) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("source_id", quest.sourceId());
            entryTag.putString("source_group", quest.sourceGroup());
            entryTag.putInt("source_x", quest.sourceX());
            entryTag.putInt("source_y", quest.sourceY());
            entryTag.putFloat("scale", quest.scale());
            Tag definitionTag = QuestDefinition.CODEC.encodeStart(NbtOps.INSTANCE, quest.definition())
                    .resultOrPartial(diagnostic -> QuestsAndStuffMod.LOGGER.warn(
                            "[QnS:Blueprint] Failed encoding quest definition for packet source={} diagnostic={}",
                            quest.sourceId(),
                            diagnostic
                    ))
                    .orElseGet(CompoundTag::new);
            if (definitionTag instanceof CompoundTag compound) {
                entryTag.put("definition", compound);
            }
            questTags.add(entryTag);
        }
        tag.put("quests", questTags);
        tag.put("images", CanvasLayerNbt.imagesToListTag(images));
        tag.put("texts", CanvasLayerNbt.textsToListTag(texts));
        tag.put("layer_order", CanvasLayerNbt.stringsToListTag(layerOrder));
        return tag;
    }

    public static CanvasBlueprint fromPacketTag(CompoundTag tag) {
        if (tag == null) {
            return empty();
        }
        List<QuestEntry> quests = new ArrayList<>();
        ListTag questTags = tag.getList("quests", Tag.TAG_COMPOUND);
        for (int i = 0; i < questTags.size(); i++) {
            CompoundTag entryTag = questTags.getCompound(i);
            QuestDefinition definition = definitionFromNbt(entryTag.get("definition"));
            if (definition == null) {
                continue;
            }
            quests.add(new QuestEntry(
                    entryTag.getString("source_id"),
                    entryTag.getString("source_group"),
                    entryTag.getInt("source_x"),
                    entryTag.getInt("source_y"),
                    entryTag.getFloat("scale"),
                    definition
            ));
        }
        return new CanvasBlueprint(
                tag.contains("schema", Tag.TAG_INT) ? tag.getInt("schema") : CURRENT_SCHEMA,
                tag.getString("name"),
                tag.getInt("origin_x"),
                tag.getInt("origin_y"),
                quests,
                CanvasLayerNbt.imagesFromListTag(tag.getList("images", Tag.TAG_COMPOUND)),
                CanvasLayerNbt.textsFromListTag(tag.getList("texts", Tag.TAG_COMPOUND)),
                CanvasLayerNbt.stringsFromListTag(tag.getList("layer_order", Tag.TAG_STRING))
        );
    }

    public String toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("schema", schema);
        root.addProperty("name", name);
        root.addProperty("origin_x", originX);
        root.addProperty("origin_y", originY);
        JsonArray questArray = new JsonArray();
        for (QuestEntry quest : quests) {
            JsonObject entry = new JsonObject();
            entry.addProperty("source_id", quest.sourceId());
            entry.addProperty("source_group", quest.sourceGroup());
            entry.addProperty("source_x", quest.sourceX());
            entry.addProperty("source_y", quest.sourceY());
            entry.addProperty("scale", quest.scale());
            entry.add("definition", QuestDefinition.CODEC.encodeStart(JsonOps.INSTANCE, quest.definition())
                    .resultOrPartial(diagnostic -> QuestsAndStuffMod.LOGGER.warn(
                            "[QnS:Blueprint] Failed encoding quest definition for json source={} diagnostic={}",
                            quest.sourceId(),
                            diagnostic
                    ))
                    .orElseGet(JsonObject::new));
            questArray.add(entry);
        }
        root.add("quests", questArray);
        root.add("images", imagesToJson(images));
        root.add("texts", textsToJson(texts));
        JsonArray order = new JsonArray();
        for (String key : layerOrder) {
            if (key != null && !key.isBlank()) {
                order.add(key);
            }
        }
        root.add("layer_order", order);
        return GSON.toJson(root);
    }

    public static CanvasBlueprint fromJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return empty();
        }
        try {
            JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
            List<QuestEntry> quests = new ArrayList<>();
            JsonArray questArray = array(root, "quests");
            for (JsonElement element : questArray) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject entry = element.getAsJsonObject();
                String sourceId = string(entry, "source_id");
                QuestDefinition definition = QuestDefinition.CODEC.parse(JsonOps.INSTANCE, entry.get("definition"))
                        .resultOrPartial(diagnostic -> QuestsAndStuffMod.LOGGER.warn(
                                "[QnS:Blueprint] Failed parsing quest definition from json source={} diagnostic={}",
                                sourceId,
                                diagnostic
                        ))
                        .orElse(null);
                if (definition == null) {
                    continue;
                }
                quests.add(new QuestEntry(
                        sourceId,
                        string(entry, "source_group"),
                        integer(entry, "source_x", 0, "quest:" + sourceId),
                        integer(entry, "source_y", 0, "quest:" + sourceId),
                        floating(entry, "scale", 1.0f, "quest:" + sourceId),
                        definition
                ));
            }

            List<String> order = new ArrayList<>();
            JsonArray orderArray = array(root, "layer_order");
            for (JsonElement element : orderArray) {
                String value = stringValue(element, "layer_order");
                if (!value.isBlank()) {
                    order.add(value);
                }
            }
            return new CanvasBlueprint(
                    integer(root, "schema", CURRENT_SCHEMA, "root"),
                    string(root, "name"),
                    integer(root, "origin_x", 0, "root"),
                    integer(root, "origin_y", 0, "root"),
                    quests,
                    imagesFromJson(array(root, "images")),
                    textsFromJson(array(root, "texts")),
                    order
            );
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.LOGGER.warn(
                    "[QnS:Blueprint] Failed parsing blueprint json length={}",
                    raw.length(),
                    exception
            );
            return empty();
        }
    }

    public static CanvasBlueprint empty() {
        return new CanvasBlueprint(CURRENT_SCHEMA, "", 0, 0, List.of(), List.of(), List.of(), List.of());
    }

    private static QuestDefinition definitionFromNbt(Tag tag) {
        if (tag == null) {
            return null;
        }
        return QuestDefinition.CODEC.parse(NbtOps.INSTANCE, tag)
                .resultOrPartial(diagnostic -> QuestsAndStuffMod.LOGGER.warn(
                        "[QnS:Blueprint] Failed parsing quest definition from packet diagnostic={}",
                        diagnostic
                ))
                .orElse(null);
    }

    private static JsonArray imagesToJson(List<CanvasImageLayer> images) {
        JsonArray array = new JsonArray();
        for (CanvasImageLayer image : images) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", image.id());
            entry.addProperty("asset", image.asset());
            entry.addProperty("x", image.x());
            entry.addProperty("y", image.y());
            entry.addProperty("w", image.w());
            entry.addProperty("h", image.h());
            entry.addProperty("rotation", image.rotation());
            entry.addProperty("entity_yaw", image.entityYaw());
            entry.addProperty("entity_spin_speed", image.entitySpinSpeed());
            entry.addProperty("model_pitch", image.modelPitch());
            entry.addProperty("pivot_x", image.pivotX());
            entry.addProperty("pivot_y", image.pivotY());
            array.add(entry);
        }
        return array;
    }

    private static List<CanvasImageLayer> imagesFromJson(JsonArray array) {
        List<CanvasImageLayer> images = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject entry = element.getAsJsonObject();
            String id = string(entry, "id");
            if (id.isBlank()) {
                continue;
            }
            int width = integer(entry, "w", 80, "image:" + id);
            int height = integer(entry, "h", 80, "image:" + id);
            images.add(new CanvasImageLayer(
                    id,
                    string(entry, "asset"),
                    integer(entry, "x", 0, "image:" + id),
                    integer(entry, "y", 0, "image:" + id),
                    width,
                    height,
                    integer(entry, "rotation", 0, "image:" + id),
                    integer(entry, "entity_yaw", CanvasImageLayer.DEFAULT_ENTITY_YAW, "image:" + id),
                    integer(entry, "entity_spin_speed", CanvasImageLayer.DEFAULT_ENTITY_SPIN_SPEED, "image:" + id),
                    integer(entry, "model_pitch", CanvasImageLayer.DEFAULT_MODEL_PITCH, "image:" + id),
                    integer(entry, "pivot_x", width / 2, "image:" + id),
                    integer(entry, "pivot_y", height / 2, "image:" + id)
            ));
        }
        return images;
    }

    private static JsonArray textsToJson(List<CanvasTextLayer> texts) {
        JsonArray array = new JsonArray();
        for (CanvasTextLayer text : texts) {
            array.add(nbtToJson(CanvasLayerNbt.textToTag(text)));
        }
        return array;
    }

    private static List<CanvasTextLayer> textsFromJson(JsonArray array) {
        List<CanvasTextLayer> texts = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            CanvasTextLayer text = CanvasLayerNbt.textFromTag(jsonToNbt(element.getAsJsonObject()));
            if (text != null) {
                texts.add(text);
            }
        }
        return texts;
    }

    private static JsonObject nbtToJson(CompoundTag tag) {
        JsonObject object = new JsonObject();
        for (String key : tag.getAllKeys()) {
            Tag value = tag.get(key);
            if (value instanceof ListTag list) {
                JsonArray array = new JsonArray();
                for (int i = 0; i < list.size(); i++) {
                    Tag child = list.get(i);
                    if (child instanceof CompoundTag compound) {
                        array.add(nbtToJson(compound));
                    }
                }
                object.add(key, array);
            } else if (value != null) {
                if (tag.contains(key, Tag.TAG_INT)) {
                    object.addProperty(key, tag.getInt(key));
                } else {
                    object.addProperty(key, tag.getString(key));
                }
            }
        }
        return object;
    }

    private static CompoundTag jsonToNbt(JsonObject object) {
        CompoundTag tag = new CompoundTag();
        for (MapEntry entry : entries(object)) {
            JsonElement value = entry.value();
            if (value == null || value.isJsonNull()) {
                continue;
            }
            if (value.isJsonArray()) {
                ListTag list = new ListTag();
                for (JsonElement child : value.getAsJsonArray()) {
                    if (child.isJsonObject()) {
                        list.add(jsonToNbt(child.getAsJsonObject()));
                    }
                }
                tag.put(entry.key(), list);
            } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                tag.putInt(entry.key(), value.getAsInt());
            } else {
                tag.putString(entry.key(), value.getAsString());
            }
        }
        return tag;
    }

    private static List<MapEntry> entries(JsonObject object) {
        List<MapEntry> entries = new ArrayList<>();
        for (var entry : object.entrySet()) {
            entries.add(new MapEntry(entry.getKey(), entry.getValue()));
        }
        return entries;
    }

    private static JsonArray array(JsonObject object, String key) {
        JsonElement element = object == null ? null : object.get(key);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : new JsonArray();
    }

    private static String string(JsonObject object, String key) {
        JsonElement element = object == null ? null : object.get(key);
        return element == null || element.isJsonNull() ? "" : element.getAsString();
    }

    private static int integer(JsonObject object, String key, int fallback, String context) {
        try {
            JsonElement element = object == null ? null : object.get(key);
            return element == null || element.isJsonNull() ? fallback : element.getAsInt();
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.LOGGER.warn(
                    "[QnS:Blueprint] Failed reading integer field context={} key={} fallback={}",
                    context,
                    key,
                    fallback,
                    exception
            );
            return fallback;
        }
    }

    private static float floating(JsonObject object, String key, float fallback, String context) {
        try {
            JsonElement element = object == null ? null : object.get(key);
            float value = element == null || element.isJsonNull() ? fallback : element.getAsFloat();
            return Float.isNaN(value) || Float.isInfinite(value) ? fallback : value;
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.LOGGER.warn(
                    "[QnS:Blueprint] Failed reading float field context={} key={} fallback={}",
                    context,
                    key,
                    fallback,
                    exception
            );
            return fallback;
        }
    }

    private static String stringValue(JsonElement element, String context) {
        try {
            return element == null || element.isJsonNull() ? "" : element.getAsString();
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Blueprint] Failed reading string value context={}", context, exception);
            return "";
        }
    }

    private record MapEntry(String key, JsonElement value) {
    }

    public record QuestEntry(
            String sourceId,
            String sourceGroup,
            int sourceX,
            int sourceY,
            float scale,
            QuestDefinition definition
    ) {
        public QuestEntry {
            sourceId = sourceId == null ? "" : sourceId.trim();
            sourceGroup = sourceGroup == null ? "" : sourceGroup.trim();
            if (Float.isNaN(scale) || Float.isInfinite(scale)) {
                scale = 1.0f;
            }
            scale = Math.max(0.5f, scale);
        }
    }
}
