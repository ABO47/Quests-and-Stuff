package com.abo47.questsandstuff.quest.model.canvas;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CanvasLayerNbt {
    private CanvasLayerNbt() {
    }

    public static CompoundTag imageToTag(CanvasImageLayer image) {
        CompoundTag tag = new CompoundTag();
        if (image == null) {
            return tag;
        }
        tag.putString("id", image.id());
        tag.putString("asset", image.asset());
        tag.putInt("x", image.x());
        tag.putInt("y", image.y());
        tag.putInt("w", image.w());
        tag.putInt("h", image.h());
        tag.putInt("rotation", image.rotation());
        tag.putInt("entity_yaw", image.entityYaw());
        tag.putInt("entity_spin_speed", image.entitySpinSpeed());
        tag.putInt("model_pitch", image.modelPitch());
        tag.putInt("pivot_x", image.pivotX());
        tag.putInt("pivot_y", image.pivotY());
        return tag;
    }

    public static CanvasImageLayer imageFromTag(CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        String id = tag.getString("id");
        if (id.isBlank()) {
            return null;
        }
        int width = tag.contains("w", Tag.TAG_INT) ? tag.getInt("w") : 80;
        int height = tag.contains("h", Tag.TAG_INT) ? tag.getInt("h") : 80;
        return new CanvasImageLayer(
                id,
                tag.getString("asset"),
                tag.getInt("x"),
                tag.getInt("y"),
                width,
                height,
                tag.getInt("rotation"),
                tag.contains("entity_yaw", Tag.TAG_INT) ? tag.getInt("entity_yaw") : CanvasImageLayer.DEFAULT_ENTITY_YAW,
                tag.contains("entity_spin_speed", Tag.TAG_INT) ? tag.getInt("entity_spin_speed") : CanvasImageLayer.DEFAULT_ENTITY_SPIN_SPEED,
                tag.contains("model_pitch", Tag.TAG_INT) ? tag.getInt("model_pitch") : CanvasImageLayer.DEFAULT_MODEL_PITCH,
                tag.contains("pivot_x", Tag.TAG_INT) ? tag.getInt("pivot_x") : width / 2,
                tag.contains("pivot_y", Tag.TAG_INT) ? tag.getInt("pivot_y") : height / 2
        );
    }

    public static CompoundTag textToTag(CanvasTextLayer text) {
        CompoundTag tag = new CompoundTag();
        if (text == null) {
            return tag;
        }
        tag.putString("id", text.id());
        tag.putString("text", text.text());
        tag.putInt("x", text.x());
        tag.putInt("y", text.y());
        tag.putInt("w", text.w());
        tag.putInt("h", text.h());
        tag.putInt("rotation", text.rotation());
        tag.putString("align", text.align());
        tag.putString("style", text.style());
        tag.putInt("color", text.color());
        tag.putInt("font_size", text.fontSize());
        ListTag spans = new ListTag();
        for (CanvasTextStyleSpan span : text.spans()) {
            CompoundTag spanTag = new CompoundTag();
            spanTag.putInt("start", span.start());
            spanTag.putInt("end", span.end());
            spanTag.putString("style", span.style());
            spanTag.putInt("color", span.color());
            spans.add(spanTag);
        }
        tag.put("spans", spans);
        return tag;
    }

    public static CanvasTextLayer textFromTag(CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        String id = tag.getString("id");
        if (id.isBlank()) {
            return null;
        }
        List<CanvasTextStyleSpan> spans = new ArrayList<>();
        ListTag spanTags = tag.getList("spans", Tag.TAG_COMPOUND);
        for (int i = 0; i < spanTags.size(); i++) {
            CompoundTag spanTag = spanTags.getCompound(i);
            spans.add(new CanvasTextStyleSpan(
                    spanTag.getInt("start"),
                    spanTag.getInt("end"),
                    spanTag.getString("style"),
                    spanTag.contains("color", Tag.TAG_INT) ? spanTag.getInt("color") : 0xFFFFFFFF
            ));
        }
        return new CanvasTextLayer(
                id,
                tag.getString("text"),
                tag.getInt("x"),
                tag.getInt("y"),
                tag.contains("w", Tag.TAG_INT) ? tag.getInt("w") : 120,
                tag.contains("h", Tag.TAG_INT) ? tag.getInt("h") : 32,
                tag.getInt("rotation"),
                tag.getString("align"),
                tag.getString("style"),
                tag.contains("color", Tag.TAG_INT) ? tag.getInt("color") : 0xFFFFFFFF,
                tag.contains("font_size", Tag.TAG_INT) ? tag.getInt("font_size") : CanvasTextLayer.DEFAULT_FONT_SIZE,
                spans
        );
    }

    public static ListTag imagesToListTag(List<CanvasImageLayer> images) {
        ListTag list = new ListTag();
        if (images == null) {
            return list;
        }
        for (CanvasImageLayer image : images) {
            list.add(imageToTag(image));
        }
        return list;
    }

    public static ListTag textsToListTag(List<CanvasTextLayer> texts) {
        ListTag list = new ListTag();
        if (texts == null) {
            return list;
        }
        for (CanvasTextLayer text : texts) {
            list.add(textToTag(text));
        }
        return list;
    }

    public static CompoundTag exclusiveChoiceToTag(CanvasExclusiveChoice ec) {
        CompoundTag tag = new CompoundTag();
        if (ec == null) {
            return tag;
        }
        tag.putString("id", ec.id());
        tag.putInt("x", ec.x());
        tag.putInt("y", ec.y());
        tag.putInt("w", ec.w());
        tag.putInt("h", ec.h());
        tag.putInt("rotation", ec.rotation());
        tag.put("connections", stringsToListTag(ec.connectionQuestIds()));
        if (!ec.prerequisiteQuestIds().isEmpty()) {
            tag.put("prerequisites", stringsToListTag(ec.prerequisiteQuestIds()));
        }
        if (!ec.background().isBlank()) {
            tag.putString("background", ec.background());
        }
        if (!ec.connectionColors().isEmpty()) {
            CompoundTag colors = new CompoundTag();
            for (Map.Entry<String, Integer> entry : ec.connectionColors().entrySet()) {
                colors.putInt(entry.getKey(), entry.getValue());
            }
            tag.put("connection_colors", colors);
        }
        if (!ec.connectionModes().isEmpty()) {
            CompoundTag modes = new CompoundTag();
            for (Map.Entry<String, String> entry : ec.connectionModes().entrySet()) {
                modes.putString(entry.getKey(), entry.getValue());
            }
            tag.put("connection_modes", modes);
        }
        if (!ec.connectionTextures().isEmpty()) {
            CompoundTag textures = new CompoundTag();
            for (Map.Entry<String, String> entry : ec.connectionTextures().entrySet()) {
                textures.putString(entry.getKey(), entry.getValue());
            }
            tag.put("connection_textures", textures);
        }
        if (!ec.connectionTextureSpacings().isEmpty()) {
            CompoundTag spacings = new CompoundTag();
            for (Map.Entry<String, Integer> entry : ec.connectionTextureSpacings().entrySet()) {
                spacings.putInt(entry.getKey(), entry.getValue());
            }
            tag.put("connection_texture_spacings", spacings);
        }
        if (!ec.hiddenConnections().isEmpty()) {
            tag.put("hidden_connections", stringsToListTag(new ArrayList<>(ec.hiddenConnections())));
        }
        return tag;
    }

    public static CanvasExclusiveChoice exclusiveChoiceFromTag(CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        String id = tag.getString("id");
        if (id.isBlank()) {
            return null;
        }
        int width = tag.contains("w", Tag.TAG_INT) ? tag.getInt("w") : CanvasExclusiveChoice.DEFAULT_WIDTH;
        int height = tag.contains("h", Tag.TAG_INT) ? tag.getInt("h") : CanvasExclusiveChoice.DEFAULT_HEIGHT;
        List<String> connections = stringsFromListTag(tag.getList("connections", Tag.TAG_STRING));
        List<String> prerequisites = stringsFromListTag(tag.getList("prerequisites", Tag.TAG_STRING));
        String background = tag.contains("background", Tag.TAG_STRING) ? tag.getString("background") : "";
        Map<String, Integer> connectionColors = new HashMap<>();
        if (tag.contains("connection_colors", Tag.TAG_COMPOUND)) {
            CompoundTag colors = tag.getCompound("connection_colors");
            for (String key : colors.getAllKeys()) {
                if (colors.contains(key, Tag.TAG_INT)) {
                    connectionColors.put(key, colors.getInt(key));
                }
            }
        }
        Map<String, String> connectionModes = new HashMap<>();
        if (tag.contains("connection_modes", Tag.TAG_COMPOUND)) {
            CompoundTag modes = tag.getCompound("connection_modes");
            for (String key : modes.getAllKeys()) {
                if (modes.contains(key, Tag.TAG_STRING)) {
                    connectionModes.put(key, modes.getString(key));
                }
            }
        }
        Map<String, String> connectionTextures = new HashMap<>();
        if (tag.contains("connection_textures", Tag.TAG_COMPOUND)) {
            CompoundTag textures = tag.getCompound("connection_textures");
            for (String key : textures.getAllKeys()) {
                if (textures.contains(key, Tag.TAG_STRING)) {
                    connectionTextures.put(key, textures.getString(key));
                }
            }
        }
        Map<String, Integer> connectionTextureSpacings = new HashMap<>();
        if (tag.contains("connection_texture_spacings", Tag.TAG_COMPOUND)) {
            CompoundTag spacings = tag.getCompound("connection_texture_spacings");
            for (String key : spacings.getAllKeys()) {
                if (spacings.contains(key, Tag.TAG_INT)) {
                    connectionTextureSpacings.put(key, spacings.getInt(key));
                }
            }
        }
        Set<String> hiddenConnections = new HashSet<>(stringsFromListTag(tag.getList("hidden_connections", Tag.TAG_STRING)));
        return new CanvasExclusiveChoice(
                id,
                tag.getInt("x"),
                tag.getInt("y"),
                width,
                height,
                tag.getInt("rotation"),
                connections,
                prerequisites,
                background,
                connectionColors,
                connectionModes,
                connectionTextures,
                connectionTextureSpacings,
                hiddenConnections
        );
    }

    public static ListTag exclusiveChoicesToListTag(List<CanvasExclusiveChoice> choices) {
        ListTag list = new ListTag();
        if (choices == null) {
            return list;
        }
        for (CanvasExclusiveChoice ec : choices) {
            list.add(exclusiveChoiceToTag(ec));
        }
        return list;
    }

    public static List<CanvasExclusiveChoice> exclusiveChoicesFromListTag(ListTag list) {
        List<CanvasExclusiveChoice> choices = new ArrayList<>();
        if (list == null) {
            return choices;
        }
        for (int i = 0; i < list.size(); i++) {
            CanvasExclusiveChoice ec = exclusiveChoiceFromTag(list.getCompound(i));
            if (ec != null) {
                choices.add(ec);
            }
        }
        return choices;
    }

    public static ListTag stringsToListTag(List<String> values) {
        ListTag list = new ListTag();
        if (values == null) {
            return list;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                list.add(StringTag.valueOf(value));
            }
        }
        return list;
    }

    public static List<CanvasImageLayer> imagesFromListTag(ListTag list) {
        List<CanvasImageLayer> images = new ArrayList<>();
        if (list == null) {
            return images;
        }
        for (int i = 0; i < list.size(); i++) {
            CanvasImageLayer image = imageFromTag(list.getCompound(i));
            if (image != null) {
                images.add(image);
            }
        }
        return images;
    }

    public static List<CanvasTextLayer> textsFromListTag(ListTag list) {
        List<CanvasTextLayer> texts = new ArrayList<>();
        if (list == null) {
            return texts;
        }
        for (int i = 0; i < list.size(); i++) {
            CanvasTextLayer text = textFromTag(list.getCompound(i));
            if (text != null) {
                texts.add(text);
            }
        }
        return texts;
    }

    public static List<String> stringsFromListTag(ListTag list) {
        List<String> values = new ArrayList<>();
        if (list == null) {
            return values;
        }
        for (int i = 0; i < list.size(); i++) {
            String value = list.getString(i);
            if (value != null && !value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }
}
