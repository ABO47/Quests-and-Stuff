package com.abo47.questsandstuff.quest.model.canvas;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

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
        return new CanvasImageLayer(
                id,
                tag.getString("asset"),
                tag.getInt("x"),
                tag.getInt("y"),
                tag.contains("w", Tag.TAG_INT) ? tag.getInt("w") : 80,
                tag.contains("h", Tag.TAG_INT) ? tag.getInt("h") : 80,
                tag.getInt("rotation"),
                tag.contains("entity_yaw", Tag.TAG_INT) ? tag.getInt("entity_yaw") : CanvasImageLayer.DEFAULT_ENTITY_YAW,
                tag.contains("entity_spin_speed", Tag.TAG_INT) ? tag.getInt("entity_spin_speed") : CanvasImageLayer.DEFAULT_ENTITY_SPIN_SPEED
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
