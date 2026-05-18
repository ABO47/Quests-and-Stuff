package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestState;

import com.abo47.questsandstuff.client.sync.cache.ClientCanvasLayerState;

import com.abo47.questsandstuff.client.sync.cache.ClientChapterState;

import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ClientChapterLocalMutations {
    private ClientChapterLocalMutations() {
    }

    public static void createGroupLocal(String group) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || ClientChapterState.GROUP_ORDER.contains(normalized)) {
            return;
        }
        ClientChapterState.GROUP_ORDER.add(normalized);
        ClientChapterState.GROUP_ICONS.putIfAbsent(normalized, "");
        ClientChapterState.GROUP_BACKGROUNDS.putIfAbsent(normalized, "default");
        ClientChapterState.GROUP_CANVAS_BACKGROUNDS.putIfAbsent(normalized, "default");
        ClientChapterState.GROUP_TEXT_ALIGN.putIfAbsent(normalized, "center");
        ClientChapterState.GROUP_TEXT_COLOR.putIfAbsent(normalized, 0xFFFFFFFF);
        ClientChapterState.GROUP_TEXT_STYLE.putIfAbsent(normalized, "normal");
        ClientChapterState.GROUP_TEXT_SIZE.putIfAbsent(normalized, CanvasTextLayer.DEFAULT_FONT_SIZE);
        ClientCanvasLayerState.GROUP_CANVAS_IMAGES.putIfAbsent(normalized, List.of());
        ClientCanvasLayerState.GROUP_CANVAS_TEXTS.putIfAbsent(normalized, List.of());
        ClientCanvasLayerState.GROUP_CANVAS_LAYER_ORDER.putIfAbsent(normalized, List.of());
    }

    public static void renameGroupLocal(String from, String to) {
        String source = ClientChapterState.normalizeGroup(from);
        String target = ClientChapterState.normalizeGroup(to);
        if (source.isBlank() || target.isBlank() || source.equals(target)) {
            return;
        }
        int index = ClientChapterState.GROUP_ORDER.indexOf(source);
        if (index < 0 || ClientChapterState.GROUP_ORDER.contains(target)) {
            return;
        }
        ClientChapterState.GROUP_ORDER.set(index, target);
        ClientChapterState.GROUP_ICONS.put(target, ClientChapterState.GROUP_ICONS.remove(source));
        ClientChapterState.GROUP_BACKGROUNDS.put(target, ClientChapterState.GROUP_BACKGROUNDS.remove(source));
        ClientChapterState.GROUP_CANVAS_BACKGROUNDS.put(target, ClientChapterState.GROUP_CANVAS_BACKGROUNDS.remove(source));
        ClientChapterState.GROUP_TEXT_ALIGN.put(target, ClientChapterState.GROUP_TEXT_ALIGN.remove(source));
        ClientChapterState.GROUP_TEXT_COLOR.put(target, ClientChapterState.GROUP_TEXT_COLOR.remove(source));
        ClientChapterState.GROUP_TEXT_STYLE.put(target, ClientChapterState.GROUP_TEXT_STYLE.remove(source));
        Integer textSize = ClientChapterState.GROUP_TEXT_SIZE.remove(source);
        ClientChapterState.GROUP_TEXT_SIZE.put(target, textSize == null ? CanvasTextLayer.DEFAULT_FONT_SIZE : textSize);
        ClientCanvasLayerState.GROUP_CANVAS_IMAGES.put(target, ClientCanvasLayerState.GROUP_CANVAS_IMAGES.getOrDefault(source, List.of()));
        ClientCanvasLayerState.GROUP_CANVAS_IMAGES.remove(source);
        ClientCanvasLayerState.GROUP_CANVAS_TEXTS.put(target, ClientCanvasLayerState.GROUP_CANVAS_TEXTS.getOrDefault(source, List.of()));
        ClientCanvasLayerState.GROUP_CANVAS_TEXTS.remove(source);
        ClientCanvasLayerState.GROUP_CANVAS_LAYER_ORDER.put(target, ClientCanvasLayerState.GROUP_CANVAS_LAYER_ORDER.getOrDefault(source, List.of()));
        ClientCanvasLayerState.GROUP_CANVAS_LAYER_ORDER.remove(source);
        for (CompoundTag quest : ClientQuestState.QUESTS.values()) {
            CompoundTag groups = quest.getCompound("groups");
            if (!groups.contains(source)) {
                continue;
            }
            CompoundTag view = groups.getCompound(source).copy();
            groups.remove(source);
            groups.put(target, view);
        }
    }

    public static void deleteGroupLocal(String group) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || !ClientChapterState.GROUP_ORDER.remove(normalized)) {
            return;
        }
        ClientChapterState.GROUP_ICONS.remove(normalized);
        ClientChapterState.GROUP_BACKGROUNDS.remove(normalized);
        ClientChapterState.GROUP_CANVAS_BACKGROUNDS.remove(normalized);
        ClientChapterState.GROUP_TEXT_ALIGN.remove(normalized);
        ClientChapterState.GROUP_TEXT_COLOR.remove(normalized);
        ClientChapterState.GROUP_TEXT_STYLE.remove(normalized);
        ClientChapterState.GROUP_TEXT_SIZE.remove(normalized);
        ClientCanvasLayerState.GROUP_CANVAS_IMAGES.remove(normalized);
        ClientCanvasLayerState.GROUP_CANVAS_TEXTS.remove(normalized);
        ClientCanvasLayerState.GROUP_CANVAS_LAYER_ORDER.remove(normalized);
        for (CompoundTag quest : ClientQuestState.QUESTS.values()) {
            CompoundTag groups = quest.getCompound("groups");
            if (groups.contains(normalized)) {
                groups.remove(normalized);
            }
        }
        List<String> groupless = new ArrayList<>();
        for (Map.Entry<String, CompoundTag> entry : ClientQuestState.QUESTS.entrySet()) {
            if (entry.getValue().getCompound("groups").isEmpty()) {
                groupless.add(entry.getKey());
            }
        }
        for (String questId : groupless) {
            ClientQuestLocalMutations.removeQuestLocal(questId);
        }
    }

    public static void moveGroupLocal(String group, int offset) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || offset == 0) {
            return;
        }
        int index = ClientChapterState.GROUP_ORDER.indexOf(normalized);
        if (index < 0) {
            return;
        }
        int next = Math.max(0, Math.min(ClientChapterState.GROUP_ORDER.size() - 1, index + offset));
        if (next == index) {
            return;
        }
        ClientChapterState.GROUP_ORDER.remove(index);
        ClientChapterState.GROUP_ORDER.add(next, normalized);
    }

    public static void moveGroupToIndexLocal(String group, int targetIndex) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank()) {
            return;
        }
        int index = ClientChapterState.GROUP_ORDER.indexOf(normalized);
        if (index < 0) {
            return;
        }
        int next = Math.max(0, Math.min(ClientChapterState.GROUP_ORDER.size() - 1, targetIndex));
        if (next == index) {
            return;
        }
        ClientChapterState.GROUP_ORDER.remove(index);
        ClientChapterState.GROUP_ORDER.add(next, normalized);
    }

    public static void setGroupIconLocal(String group, String icon) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (!normalized.isBlank()) {
            ClientChapterState.GROUP_ICONS.put(normalized, icon == null ? "" : icon.trim());
        }
    }

    public static void setGroupBackgroundLocal(String group, String background) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (!normalized.isBlank()) {
            ClientChapterState.GROUP_BACKGROUNDS.put(normalized, background == null || background.isBlank() ? "default" : background.trim());
        }
    }

    public static void setGroupCanvasBackgroundLocal(String group, String background) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (!normalized.isBlank()) {
            ClientChapterState.GROUP_CANVAS_BACKGROUNDS.put(normalized, background == null || background.isBlank() ? "default" : background.trim());
        }
    }

    public static void setGroupTextAlignLocal(String group, String align) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (!normalized.isBlank()) {
            ClientChapterState.GROUP_TEXT_ALIGN.put(normalized, ClientChapterState.normalizeTextAlign(align));
        }
    }

    public static void setGroupTextColorLocal(String group, int color) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (!normalized.isBlank()) {
            ClientChapterState.GROUP_TEXT_COLOR.put(normalized, color);
        }
    }

    public static void setGroupTextStyleLocal(String group, String style) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (!normalized.isBlank()) {
            ClientChapterState.GROUP_TEXT_STYLE.put(normalized, ClientChapterState.normalizeTextStyle(style));
        }
    }

    public static void setGroupTextSizeLocal(String group, int size) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (!normalized.isBlank()) {
            ClientChapterState.GROUP_TEXT_SIZE.put(normalized, ClientChapterState.clampTextSize(size));
        }
    }
}
