package com.abo47.questsandstuff.client.tablet.details.description;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbt;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class QuestDetailsDescriptionModel {
    public static final String ORDER_TEXT = "text:";
    public static final String ORDER_IMAGE = "image:";
    private static final String META_PREFIX = "@qas_desc_meta:";
    private static final String TEXT_PREFIX = "@qas_desc_text:";
    private static final String IMAGE_PREFIX = "@qas_desc_image:";
    private static final int MAX_TEXT_LENGTH = 2048;

    final Map<String, CanvasTextLayer> texts = new HashMap<>();
    final Map<String, CanvasImageLayer> images = new HashMap<>();
    final List<String> order = new ArrayList<>();
    String canvasBackground = "default";

    public CanvasTextLayer text(String id) {
        return texts.get(id);
    }

    public CanvasImageLayer image(String id) {
        return images.get(id);
    }

    void putText(CanvasTextLayer text) {
        texts.put(text.id(), text);
    }

    public void putImage(CanvasImageLayer image) {
        images.put(image.id(), image);
    }

    void removeText(String id) {
        texts.remove(id);
        order.remove(ORDER_TEXT + id);
    }

    void removeImage(String id) {
        images.remove(id);
        order.remove(ORDER_IMAGE + id);
    }

    void ensureOrder(String key) {
        if (!order.contains(key)) {
            order.add(key);
        }
    }

    void bringToFront(String key) {
        if (order.remove(key)) {
            order.add(key);
        }
    }

    void sendToBack(String key) {
        if (order.remove(key)) {
            order.add(0, key);
        }
    }

    public static QuestDetailsDescriptionModel decode(CompoundTag quest) {
        QuestDetailsDescriptionModel model = new QuestDetailsDescriptionModel();
        ListTag lines = quest.getList("description", Tag.TAG_STRING);
        int fallbackY = 8;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.getString(i);
            if (line.startsWith(META_PREFIX)) {
                parseMeta(model, line.substring(META_PREFIX.length()));
            } else if (line.startsWith(TEXT_PREFIX)) {
                CanvasTextLayer text = parseText(line.substring(TEXT_PREFIX.length()));
                if (text != null) {
                    model.putText(text);
                    model.ensureOrder(ORDER_TEXT + text.id());
                }
            } else if (line.startsWith(IMAGE_PREFIX)) {
                CanvasImageLayer image = parseImage(line.substring(IMAGE_PREFIX.length()));
                if (image != null) {
                    model.putImage(image);
                    model.ensureOrder(ORDER_IMAGE + image.id());
                }
            } else if (!line.isBlank()) {
                String id = "line_" + i;
                CanvasTextLayer text = new CanvasTextLayer(id, line, 8, fallbackY, 160, 24, 0, "left", "normal", ModColors.TEXT_PRIMARY);
                model.putText(text);
                model.ensureOrder(ORDER_TEXT + id);
                fallbackY += 28;
            }
        }
        return model;
    }

    public static List<String> encode(QuestDetailsDescriptionModel model) {
        List<String> lines = new ArrayList<>();
        lines.add(META_PREFIX + metaTag(model).toString());
        for (String key : model.order) {
            if (key.startsWith(ORDER_TEXT)) {
                CanvasTextLayer text = model.texts.get(key.substring(ORDER_TEXT.length()));
                if (text != null) {
                    lines.add(TEXT_PREFIX + CanvasLayerNbt.textToTag(text.withText(limit(text.text(), MAX_TEXT_LENGTH))).toString());
                }
            } else if (key.startsWith(ORDER_IMAGE)) {
                CanvasImageLayer image = model.images.get(key.substring(ORDER_IMAGE.length()));
                if (image != null) {
                    lines.add(IMAGE_PREFIX + CanvasLayerNbt.imageToTag(image).toString());
                }
            }
        }
        return lines;
    }

    public static void save(Player player, String questId, QuestDetailsDescriptionModel model) {
        EditorCommandClient.updateQuestDescription(player, questId, encode(model));
    }

    public static void preview(String questId, QuestDetailsDescriptionModel model) {
        ClientQuestCache.setQuestDescriptionLocal(questId, encode(model));
    }

    public static String limit(String value, int max) {
        String safe = value == null ? "" : value.replace("\r\n", "\n").replace('\r', '\n');
        return safe.length() <= max ? safe : safe.substring(0, max);
    }

    private static CanvasTextLayer parseText(String snbt) {
        try {
            return CanvasLayerNbt.textFromTag(TagParser.parseTag(snbt));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static CanvasImageLayer parseImage(String snbt) {
        try {
            return CanvasLayerNbt.imageFromTag(TagParser.parseTag(snbt));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static CompoundTag metaTag(QuestDetailsDescriptionModel model) {
        CompoundTag tag = new CompoundTag();
        tag.putString("background", model.canvasBackground == null || model.canvasBackground.isBlank() ? "default" : model.canvasBackground);
        return tag;
    }

    private static void parseMeta(QuestDetailsDescriptionModel model, String snbt) {
        try {
            CompoundTag tag = TagParser.parseTag(snbt);
            model.canvasBackground = tag.contains("background", Tag.TAG_STRING) ? tag.getString("background") : "default";
            if (model.canvasBackground.isBlank()) {
                model.canvasBackground = "default";
            }
        } catch (Exception ignored) {
        }
    }
}
