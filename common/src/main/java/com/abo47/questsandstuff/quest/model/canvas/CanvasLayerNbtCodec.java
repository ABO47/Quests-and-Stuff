package com.abo47.questsandstuff.quest.model.canvas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

public final class CanvasLayerNbtCodec {
    private CanvasLayerNbtCodec() {
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
                    spanTag.contains("color", Tag.TAG_INT) ? spanTag.getInt("color") : TabletColors.WHITE
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
                tag.contains("color", Tag.TAG_INT) ? tag.getInt("color") : TabletColors.WHITE,
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
            tag.put("connection_colors", intMapToCompound(ec.connectionColors()));
        }
        if (!ec.connectionModes().isEmpty()) {
            tag.put("connection_modes", stringMapToCompound(ec.connectionModes()));
        }
        if (!ec.connectionTextures().isEmpty()) {
            tag.put("connection_textures", stringMapToCompound(ec.connectionTextures()));
        }
        if (!ec.connectionTextureSpacings().isEmpty()) {
            tag.put("connection_texture_spacings", intMapToCompound(ec.connectionTextureSpacings()));
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
        Map<String, Integer> connectionColors = tag.contains("connection_colors", Tag.TAG_COMPOUND)
                ? intMapFromCompound(tag.getCompound("connection_colors")) : new HashMap<>();
        Map<String, String> connectionModes = tag.contains("connection_modes", Tag.TAG_COMPOUND)
                ? stringMapFromCompound(tag.getCompound("connection_modes")) : new HashMap<>();
        Map<String, String> connectionTextures = tag.contains("connection_textures", Tag.TAG_COMPOUND)
                ? stringMapFromCompound(tag.getCompound("connection_textures")) : new HashMap<>();
        Map<String, Integer> connectionTextureSpacings = tag.contains("connection_texture_spacings", Tag.TAG_COMPOUND)
                ? intMapFromCompound(tag.getCompound("connection_texture_spacings")) : new HashMap<>();
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

    public static CompoundTag intMapToCompound(Map<String, Integer> map) {
        CompoundTag tag = new CompoundTag();
        if (map == null || map.isEmpty()) {
            return tag;
        }
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            tag.putInt(entry.getKey(), entry.getValue());
        }
        return tag;
    }

    public static Map<String, Integer> intMapFromCompound(CompoundTag tag) {
        Map<String, Integer> result = new HashMap<>();
        if (tag == null) {
            return result;
        }
        for (String key : tag.getAllKeys()) {
            if (tag.contains(key, Tag.TAG_INT)) {
                result.put(key, tag.getInt(key));
            }
        }
        return result;
    }

    public static CompoundTag stringMapToCompound(Map<String, String> map) {
        CompoundTag tag = new CompoundTag();
        if (map == null || map.isEmpty()) {
            return tag;
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            tag.putString(entry.getKey(), entry.getValue());
        }
        return tag;
    }

    public static Map<String, String> stringMapFromCompound(CompoundTag tag) {
        Map<String, String> result = new HashMap<>();
        if (tag == null) {
            return result;
        }
        for (String key : tag.getAllKeys()) {
            if (tag.contains(key, Tag.TAG_STRING)) {
                result.put(key, tag.getString(key));
            }
        }
        return result;
    }
}
