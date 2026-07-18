package com.abo47.questsandstuff.quest.editor.blueprint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbtCodec;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public record CanvasBlueprint(
        int schema,
        String name,
        int originX,
        int originY,
        List<QuestEntry> quests,
        List<CanvasImageLayer> images,
        List<CanvasTextLayer> texts,
        List<String> layerOrder,
        List<ExclusiveChoiceEntry> exclusiveChoices
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
        exclusiveChoices = exclusiveChoices == null ? List.of() : List.copyOf(exclusiveChoices);
    }

    public CanvasBlueprint(String name, int originX, int originY, List<QuestEntry> quests, List<CanvasImageLayer> images, List<CanvasTextLayer> texts, List<String> layerOrder) {
        this(CURRENT_SCHEMA, name, originX, originY, quests, images, texts, layerOrder, List.of());
    }

    public CanvasBlueprint(String name, int originX, int originY, List<QuestEntry> quests, List<CanvasImageLayer> images, List<CanvasTextLayer> texts, List<String> layerOrder, List<ExclusiveChoiceEntry> exclusiveChoices) {
        this(CURRENT_SCHEMA, name, originX, originY, quests, images, texts, layerOrder, exclusiveChoices);
    }

    public boolean isEmpty() {
        return quests.isEmpty() && images.isEmpty() && texts.isEmpty() && exclusiveChoices.isEmpty();
    }

    public int contentCount() {
        return quests.size() + images.size() + texts.size() + exclusiveChoices.size();
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
            entryTag.putString("source_chapter", quest.sourceChapter());
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
        tag.put("images", CanvasLayerNbtCodec.imagesToListTag(images));
        tag.put("texts", CanvasLayerNbtCodec.textsToListTag(texts));
        tag.put("exclusive_choices", exclusiveChoicesToListTag(exclusiveChoices));
        tag.put("layer_order", CanvasLayerNbtCodec.stringsToListTag(layerOrder));
        return tag;
    }

    private static ListTag exclusiveChoicesToListTag(List<ExclusiveChoiceEntry> entries) {
        ListTag list = new ListTag();
        for (ExclusiveChoiceEntry entry : entries) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("source_id", entry.sourceId());
            entryTag.putString("source_chapter", entry.sourceChapter());
            entryTag.putInt("source_x", entry.sourceX());
            entryTag.putInt("source_y", entry.sourceY());
            entryTag.putInt("source_w", entry.sourceW());
            entryTag.putInt("source_h", entry.sourceH());
            entryTag.putInt("rotation", entry.rotation());
            entryTag.putString("background", entry.background());
            entryTag.put("connections", CanvasLayerNbtCodec.stringsToListTag(entry.connections()));
            entryTag.put("prerequisites", CanvasLayerNbtCodec.stringsToListTag(List.copyOf(entry.prerequisites())));
            if (!entry.connectionColors().isEmpty()) {
                CompoundTag colors = new CompoundTag();
                for (Map.Entry<String, Integer> e : entry.connectionColors().entrySet()) {
                    colors.putInt(e.getKey(), e.getValue());
                }
                entryTag.put("connection_colors", colors);
            }
            if (!entry.connectionModes().isEmpty()) {
                CompoundTag modes = new CompoundTag();
                for (Map.Entry<String, String> e : entry.connectionModes().entrySet()) {
                    modes.putString(e.getKey(), e.getValue());
                }
                entryTag.put("connection_modes", modes);
            }
            if (!entry.connectionTextures().isEmpty()) {
                CompoundTag textures = new CompoundTag();
                for (Map.Entry<String, String> e : entry.connectionTextures().entrySet()) {
                    textures.putString(e.getKey(), e.getValue());
                }
                entryTag.put("connection_textures", textures);
            }
            if (!entry.connectionTextureSpacings().isEmpty()) {
                CompoundTag spacings = new CompoundTag();
                for (Map.Entry<String, Integer> e : entry.connectionTextureSpacings().entrySet()) {
                    spacings.putInt(e.getKey(), e.getValue());
                }
                entryTag.put("connection_texture_spacings", spacings);
            }
            if (!entry.hiddenConnections().isEmpty()) {
                entryTag.put("hidden_connections", CanvasLayerNbtCodec.stringsToListTag(new ArrayList<>(entry.hiddenConnections())));
            }
            list.add(entryTag);
        }
        return list;
    }

    private static List<ExclusiveChoiceEntry> exclusiveChoicesFromListTag(ListTag list) {
        List<ExclusiveChoiceEntry> entries = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            Map<String, Integer> connectionColors = new HashMap<>();
            if (entryTag.contains("connection_colors", Tag.TAG_COMPOUND)) {
                CompoundTag colors = entryTag.getCompound("connection_colors");
                for (String key : colors.getAllKeys()) {
                    if (colors.contains(key, Tag.TAG_INT)) {
                        connectionColors.put(key, colors.getInt(key));
                    }
                }
            }
            Map<String, String> connectionModes = new HashMap<>();
            if (entryTag.contains("connection_modes", Tag.TAG_COMPOUND)) {
                CompoundTag modes = entryTag.getCompound("connection_modes");
                for (String key : modes.getAllKeys()) {
                    if (modes.contains(key, Tag.TAG_STRING)) {
                        connectionModes.put(key, modes.getString(key));
                    }
                }
            }
            Map<String, String> connectionTextures = new HashMap<>();
            if (entryTag.contains("connection_textures", Tag.TAG_COMPOUND)) {
                CompoundTag textures = entryTag.getCompound("connection_textures");
                for (String key : textures.getAllKeys()) {
                    if (textures.contains(key, Tag.TAG_STRING)) {
                        connectionTextures.put(key, textures.getString(key));
                    }
                }
            }
            Map<String, Integer> connectionTextureSpacings = new HashMap<>();
            if (entryTag.contains("connection_texture_spacings", Tag.TAG_COMPOUND)) {
                CompoundTag spacings = entryTag.getCompound("connection_texture_spacings");
                for (String key : spacings.getAllKeys()) {
                    if (spacings.contains(key, Tag.TAG_INT)) {
                        connectionTextureSpacings.put(key, spacings.getInt(key));
                    }
                }
            }
            Set<String> hiddenConnections = Set.copyOf(CanvasLayerNbtCodec.stringsFromListTag(entryTag.getList("hidden_connections", Tag.TAG_STRING)));
            entries.add(new ExclusiveChoiceEntry(
                    entryTag.getString("source_id"),
                    entryTag.getString("source_chapter"),
                    entryTag.getInt("source_x"),
                    entryTag.getInt("source_y"),
                    entryTag.getInt("source_w"),
                    entryTag.getInt("source_h"),
                    entryTag.contains("rotation", Tag.TAG_INT) ? entryTag.getInt("rotation") : 0,
                    entryTag.getString("background"),
                    CanvasLayerNbtCodec.stringsFromListTag(entryTag.getList("connections", Tag.TAG_STRING)),
                    Set.copyOf(CanvasLayerNbtCodec.stringsFromListTag(entryTag.getList("prerequisites", Tag.TAG_STRING))),
                    connectionColors,
                    connectionModes,
                    connectionTextures,
                    connectionTextureSpacings,
                    hiddenConnections
            ));
        }
        return entries;
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
                    entryTag.getString("source_chapter"),
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
                CanvasLayerNbtCodec.imagesFromListTag(tag.getList("images", Tag.TAG_COMPOUND)),
                CanvasLayerNbtCodec.textsFromListTag(tag.getList("texts", Tag.TAG_COMPOUND)),
                CanvasLayerNbtCodec.stringsFromListTag(tag.getList("layer_order", Tag.TAG_STRING)),
                exclusiveChoicesFromListTag(tag.getList("exclusive_choices", Tag.TAG_COMPOUND))
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
            entry.addProperty("source_chapter", quest.sourceChapter());
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
        root.add("exclusive_choices", exclusiveChoicesToJson(exclusiveChoices));
        JsonArray order = new JsonArray();
        for (String key : layerOrder) {
            if (key != null && !key.isBlank()) {
                order.add(key);
            }
        }
        root.add("layer_order", order);
        return GSON.toJson(root);
    }

    private static JsonArray exclusiveChoicesToJson(List<ExclusiveChoiceEntry> entries) {
        JsonArray array = new JsonArray();
        for (ExclusiveChoiceEntry entry : entries) {
            JsonObject obj = new JsonObject();
            obj.addProperty("source_id", entry.sourceId());
            obj.addProperty("source_chapter", entry.sourceChapter());
            obj.addProperty("source_x", entry.sourceX());
            obj.addProperty("source_y", entry.sourceY());
            obj.addProperty("source_w", entry.sourceW());
            obj.addProperty("source_h", entry.sourceH());
            obj.addProperty("rotation", entry.rotation());
            obj.addProperty("background", entry.background());
            JsonArray conns = new JsonArray();
            for (String c : entry.connections()) {
                conns.add(c);
            }
            obj.add("connections", conns);
            JsonArray prereqs = new JsonArray();
            for (String p : entry.prerequisites()) {
                prereqs.add(p);
            }
            obj.add("prerequisites", prereqs);
            if (!entry.connectionColors().isEmpty()) {
                JsonObject colors = new JsonObject();
                for (Map.Entry<String, Integer> e : entry.connectionColors().entrySet()) {
                    colors.addProperty(e.getKey(), e.getValue());
                }
                obj.add("connection_colors", colors);
            }
            if (!entry.connectionModes().isEmpty()) {
                JsonObject modes = new JsonObject();
                for (Map.Entry<String, String> e : entry.connectionModes().entrySet()) {
                    modes.addProperty(e.getKey(), e.getValue());
                }
                obj.add("connection_modes", modes);
            }
            if (!entry.connectionTextures().isEmpty()) {
                JsonObject textures = new JsonObject();
                for (Map.Entry<String, String> e : entry.connectionTextures().entrySet()) {
                    textures.addProperty(e.getKey(), e.getValue());
                }
                obj.add("connection_textures", textures);
            }
            if (!entry.connectionTextureSpacings().isEmpty()) {
                JsonObject spacings = new JsonObject();
                for (Map.Entry<String, Integer> e : entry.connectionTextureSpacings().entrySet()) {
                    spacings.addProperty(e.getKey(), e.getValue());
                }
                obj.add("connection_texture_spacings", spacings);
            }
            if (!entry.hiddenConnections().isEmpty()) {
                JsonArray hidden = new JsonArray();
                for (String h : entry.hiddenConnections()) {
                    hidden.add(h);
                }
                obj.add("hidden_connections", hidden);
            }
            array.add(obj);
        }
        return array;
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
                        string(entry, "source_chapter"),
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
                    order,
                    exclusiveChoicesFromJson(array(root, "exclusive_choices"))
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
        return new CanvasBlueprint(CURRENT_SCHEMA, "", 0, 0, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private static List<ExclusiveChoiceEntry> exclusiveChoicesFromJson(JsonArray array) {
        List<ExclusiveChoiceEntry> entries = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject obj = element.getAsJsonObject();

            JsonArray connArray = array(obj, "connections");
            List<String> connections = new ArrayList<>();
            for (JsonElement c : connArray) {
                String value = stringValue(c, "connections");
                if (!value.isBlank()) {
                    connections.add(value);
                }
            }

            JsonArray prereqArray = array(obj, "prerequisites");
            Set<String> prerequisiteSet = new LinkedHashSet<>();
            for (JsonElement p : prereqArray) {
                String value = stringValue(p, "prerequisites");
                if (!value.isBlank()) {
                    prerequisiteSet.add(value);
                }
            }

            Map<String, Integer> connectionColors = new HashMap<>();
            JsonObject colorsObj = obj.getAsJsonObject("connection_colors");
            if (colorsObj != null) {
                for (var e : colorsObj.entrySet()) {
                    if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isNumber()) {
                        connectionColors.put(e.getKey(), e.getValue().getAsInt());
                    }
                }
            }

            Map<String, String> connectionModes = new HashMap<>();
            JsonObject modesObj = obj.getAsJsonObject("connection_modes");
            if (modesObj != null) {
                for (var e : modesObj.entrySet()) {
                    if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString()) {
                        connectionModes.put(e.getKey(), e.getValue().getAsString());
                    }
                }
            }

            Map<String, String> connectionTextures = new HashMap<>();
            JsonObject texturesObj = obj.getAsJsonObject("connection_textures");
            if (texturesObj != null) {
                for (var e : texturesObj.entrySet()) {
                    if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString()) {
                        connectionTextures.put(e.getKey(), e.getValue().getAsString());
                    }
                }
            }

            Map<String, Integer> connectionTextureSpacings = new HashMap<>();
            JsonObject spacingsObj = obj.getAsJsonObject("connection_texture_spacings");
            if (spacingsObj != null) {
                for (var e : spacingsObj.entrySet()) {
                    if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isNumber()) {
                        connectionTextureSpacings.put(e.getKey(), e.getValue().getAsInt());
                    }
                }
            }

            JsonArray hiddenArray = obj.getAsJsonArray("hidden_connections");
            Set<String> hiddenConnections = new LinkedHashSet<>();
            if (hiddenArray != null) {
                for (JsonElement h : hiddenArray) {
                    String value = stringValue(h, "hidden_connections");
                    if (!value.isBlank()) {
                        hiddenConnections.add(value);
                    }
                }
            }

            entries.add(new ExclusiveChoiceEntry(
                    string(obj, "source_id"),
                    string(obj, "source_chapter"),
                    integer(obj, "source_x", 0, "ec:" + string(obj, "source_id")),
                    integer(obj, "source_y", 0, "ec:" + string(obj, "source_id")),
                    integer(obj, "source_w", 79, "ec:" + string(obj, "source_id")),
                    integer(obj, "source_h", 79, "ec:" + string(obj, "source_id")),
                    integer(obj, "rotation", 0, "ec:" + string(obj, "source_id")),
                    string(obj, "background"),
                    connections,
                    prerequisiteSet,
                    connectionColors,
                    connectionModes,
                    connectionTextures,
                    connectionTextureSpacings,
                    hiddenConnections
            ));
        }
        return entries;
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
            array.add(nbtToJson(CanvasLayerNbtCodec.textToTag(text)));
        }
        return array;
    }

    private static List<CanvasTextLayer> textsFromJson(JsonArray array) {
        List<CanvasTextLayer> texts = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            CanvasTextLayer text = CanvasLayerNbtCodec.textFromTag(jsonToNbt(element.getAsJsonObject()));
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
            String sourceChapter,
            int sourceX,
            int sourceY,
            float scale,
            QuestDefinition definition
    ) {
        public QuestEntry {
            sourceId = sourceId == null ? "" : sourceId.trim();
            sourceChapter = sourceChapter == null ? "" : sourceChapter.trim();
            if (Float.isNaN(scale) || Float.isInfinite(scale)) {
                scale = 1.0f;
            }
            scale = Math.max(0.5f, scale);
        }
    }

    public record ExclusiveChoiceEntry(
            String sourceId,
            String sourceChapter,
            int sourceX,
            int sourceY,
            int sourceW,
            int sourceH,
            int rotation,
            String background,
            List<String> connections,
            Set<String> prerequisites,
            Map<String, Integer> connectionColors,
            Map<String, String> connectionModes,
            Map<String, String> connectionTextures,
            Map<String, Integer> connectionTextureSpacings,
            Set<String> hiddenConnections
    ) {
        public ExclusiveChoiceEntry {
            sourceId = sourceId == null ? "" : sourceId.trim();
            sourceChapter = sourceChapter == null ? "" : sourceChapter.trim();
            sourceW = Math.max(1, sourceW);
            sourceH = Math.max(1, sourceH);
            connections = connections == null ? List.of() : List.copyOf(connections);
            prerequisites = prerequisites == null ? Set.of() : Set.copyOf(prerequisites);
            connectionColors = connectionColors == null ? Map.of() : Map.copyOf(connectionColors);
            connectionModes = connectionModes == null ? Map.of() : Map.copyOf(connectionModes);
            connectionTextures = connectionTextures == null ? Map.of() : Map.copyOf(connectionTextures);
            connectionTextureSpacings = connectionTextureSpacings == null ? Map.of() : Map.copyOf(connectionTextureSpacings);
            hiddenConnections = hiddenConnections == null ? Set.of() : Set.copyOf(hiddenConnections);
        }
    }
}
