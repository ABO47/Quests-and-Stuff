package com.abo47.questsandstuff.network.editor;

import com.abo47.questsandstuff.network.QuestPacketContext;

import com.abo47.questsandstuff.quest.QuestServices;
import com.abo47.questsandstuff.quest.editor.command.EditorCommand;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadLimits;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record C2SEditorCommandPacket(EditorCommand command) {
    public static final String PREREQUISITE_FIELD = "prerequisite";

    public C2SEditorCommandPacket(String action, CompoundTag payload) {
        this(new EditorCommand(EditorCommandType.fromWireName(action), payload));
    }

    public static C2SEditorCommandPacket decode(FriendlyByteBuf buf) {
        return new C2SEditorCommandPacket(EditorCommand.decode(buf));
    }

    public void encode(FriendlyByteBuf buf) {
        command.encode(buf);
    }

    public void handle(QuestPacketContext context) {
        ServerPlayer player = context.sender();
        if (EditorPacketGuard.canEdit(player)) {
            context.enqueueWork(() -> {
                var editor = QuestServices.editor(player.server);
                CompoundTag payload = command.payload();
                if (command.type() == EditorCommandType.MOVE_MANY) {
                    String group = payload == null ? "" : payload.getString("group");
                    Map<String, int[]> moves = new HashMap<>();
                    ListTag moveTags = payload == null ? new ListTag() : payload.getList("moves", Tag.TAG_COMPOUND);
                    if (EditorCommandPayloadLimits.exceedsLimit(moveTags, EditorCommandPayloadLimits.MAX_BULK_EDIT_ENTRIES)) {
                        return;
                    }
                    for (int i = 0; i < moveTags.size(); i++) {
                        CompoundTag moveTag = moveTags.getCompound(i);
                        String questId = moveTag.getString("quest");
                        moves.put(questId, new int[]{moveTag.getInt("x"), moveTag.getInt("y")});
                    }
                    editor.moveQuestsInGroup(player, group, moves);
                    return;
                }
                if (command.type() == EditorCommandType.SCALE_MANY) {
                    String group = payload == null ? "" : payload.getString("group");
                    Map<String, Float> scales = new HashMap<>();
                    ListTag scaleTags = payload == null ? new ListTag() : payload.getList("scales", Tag.TAG_COMPOUND);
                    if (EditorCommandPayloadLimits.exceedsLimit(scaleTags, EditorCommandPayloadLimits.MAX_BULK_EDIT_ENTRIES)) {
                        return;
                    }
                    for (int i = 0; i < scaleTags.size(); i++) {
                        CompoundTag scaleTag = scaleTags.getCompound(i);
                        String questId = scaleTag.getString("quest");
                        scales.put(questId, scaleTag.getFloat("scale"));
                    }
                    editor.scaleQuestsInGroup(player, group, scales);
                    return;
                }
                if (command.type() == EditorCommandType.COPY_MANY) {
                    String group = payload == null ? "" : payload.getString("group");
                    Set<String> questIds = new LinkedHashSet<>();
                    ListTag questTags = payload == null ? new ListTag() : payload.getList("quests", Tag.TAG_STRING);
                    if (EditorCommandPayloadLimits.exceedsLimit(questTags, EditorCommandPayloadLimits.MAX_BULK_EDIT_ENTRIES)) {
                        return;
                    }
                    for (int i = 0; i < questTags.size(); i++) {
                        String questId = questTags.getString(i);
                        if (questId != null && !questId.isBlank()) {
                            questIds.add(questId);
                        }
                    }
                    editor.copyQuestsToClipboard(player, group, questIds);
                    return;
                }
                if (command.type() == EditorCommandType.PASTE_CLIPBOARD) {
                    String group = payload == null ? "" : payload.getString("group");
                    int x = payload == null ? 0 : payload.getInt("x");
                    int y = payload == null ? 0 : payload.getInt("y");
                    editor.pasteClipboardInGroup(player, group, x, y);
                    return;
                }
                if (command.type() == EditorCommandType.PREREQUISITE_ADD) {
                    editor.setQuestPrerequisite(player, payload.getString("quest"), prerequisiteId(payload), true);
                    return;
                }
                if (command.type() == EditorCommandType.PREREQUISITE_REMOVE) {
                    editor.setQuestPrerequisite(player, payload.getString("quest"), prerequisiteId(payload), false);
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_ICON) {
                    editor.setQuestIcon(player, payload.getString("quest"), payload.getString("icon"));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_AUTO_CLAIM) {
                    editor.setQuestAutoClaim(player, payload.getString("quest"), payload.getBoolean("enabled"));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_REPEATABLE) {
                    editor.setQuestRepeatable(player, payload.getString("quest"), payload.getBoolean("enabled"));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_HIDDEN_MODE) {
                    editor.setQuestHiddenMode(player, payload.getString("quest"), payload.getString("mode"));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_VISUAL_HIDDEN) {
                    editor.setQuestVisualHidden(player, payload.getString("quest"), payload.getBoolean("hidden"));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND) {
                    editor.setQuestCompletionSound(player, payload.getString("quest"), payload.getString("sound"));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND_MANY) {
                    editor.setQuestCompletionSound(player, questIdsFromPayload(payload), payload.getString("sound"));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND_VOLUME) {
                    editor.setQuestCompletionSoundVolume(player, payload.getString("quest"), payload.getInt("volume"));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND_VOLUME_MANY) {
                    editor.setQuestCompletionSoundVolume(player, questIdsFromPayload(payload), payload.getInt("volume"));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_COMPLETION_HUD_BACKGROUND) {
                    editor.setQuestCompletionHudBackground(player, payload.getString("quest"), payload.getString("background"));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_COMPLETION_HUD_BACKGROUND_MANY) {
                    editor.setQuestCompletionHudBackground(player, questIdsFromPayload(payload), payload.getString("background"));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_BACKGROUND) {
                    editor.setQuestBackground(player, payload.getString("quest"), payload.getString("background"), payload.getBoolean("grayscale"));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_BACKGROUND_MANY) {
                    editor.setQuestBackground(player, questIdsFromPayload(payload), payload.getString("background"), payload.getBoolean("grayscale"));
                    return;
                }
                if (command.type() == EditorCommandType.DESCRIPTION_PUT) {
                    ListTag description = payload.getList("description", Tag.TAG_STRING);
                    if (EditorCommandPayloadLimits.exceedsLimit(description, EditorCommandPayloadLimits.MAX_DESCRIPTION_LINES)) {
                        return;
                    }
                    editor.updateQuestDescription(player, payload.getString("quest"), stringsFromList(description));
                    return;
                }
                if (command.type() == EditorCommandType.CONNECTION_COLOR) {
                    editor.setConnectionColor(player, payload.getString("quest"), prerequisiteId(payload), payload.getInt("color"));
                    return;
                }
                if (command.type() == EditorCommandType.CONNECTION_MODE) {
                    editor.setConnectionMode(player, payload.getString("quest"), prerequisiteId(payload), payload.getBoolean("grid"));
                    return;
                }
                if (command.type() == EditorCommandType.CONNECTION_HIDDEN) {
                    editor.setConnectionHidden(player, payload.getString("quest"), prerequisiteId(payload), payload.getBoolean("hidden"));
                    return;
                }
                if (command.type() == EditorCommandType.TASK_PUT) {
                    String json = payload.getString("json");
                    if (EditorCommandPayloadLimits.exceedsLength(json, EditorCommandPayloadLimits.MAX_EDITOR_JSON_LENGTH)) {
                        return;
                    }
                    editor.putQuestTask(player, payload.getString("quest"), json);
                    return;
                }
                if (command.type() == EditorCommandType.TASK_REMOVE) {
                    editor.removeQuestTask(player, payload.getString("quest"), payload.getString("task"));
                    return;
                }
                if (command.type() == EditorCommandType.TASK_MOVE) {
                    editor.moveQuestTask(player, payload.getString("quest"), payload.getString("task"), payload.getInt("offset"));
                    return;
                }
                if (command.type() == EditorCommandType.REWARD_PUT) {
                    String json = payload.getString("json");
                    if (EditorCommandPayloadLimits.exceedsLength(json, EditorCommandPayloadLimits.MAX_EDITOR_JSON_LENGTH)) {
                        return;
                    }
                    editor.putQuestReward(player, payload.getString("quest"), json);
                    return;
                }
                if (command.type() == EditorCommandType.REWARD_REMOVE) {
                    editor.removeQuestReward(player, payload.getString("quest"), payload.getString("reward"));
                    return;
                }
                if (command.type() == EditorCommandType.REWARD_MOVE) {
                    editor.moveQuestReward(player, payload.getString("quest"), payload.getString("reward"), payload.getInt("offset"));
                    return;
                }
                if (command.type() == EditorCommandType.CANVAS_IMAGE_PUT) {
                    editor.putCanvasImage(player, payload.getString("group"), CanvasLayerNbt.imageFromTag(payload.getCompound("image")));
                    return;
                }
                if (command.type() == EditorCommandType.CANVAS_IMAGE_REMOVE) {
                    editor.removeCanvasImage(player, payload.getString("group"), payload.getString("id"));
                    return;
                }
                if (command.type() == EditorCommandType.CANVAS_TEXT_PUT) {
                    CompoundTag text = payload.getCompound("text");
                    if (EditorCommandPayloadLimits.exceedsLimit(text.getList("spans", Tag.TAG_COMPOUND), EditorCommandPayloadLimits.MAX_TEXT_SPANS)) {
                        return;
                    }
                    editor.putCanvasText(player, payload.getString("group"), CanvasLayerNbt.textFromTag(text));
                    return;
                }
                if (command.type() == EditorCommandType.CANVAS_TEXT_REMOVE) {
                    editor.removeCanvasText(player, payload.getString("group"), payload.getString("id"));
                    return;
                }
                if (command.type() == EditorCommandType.CANVAS_LAYER_ORDER) {
                    ListTag order = payload.getList("order", Tag.TAG_STRING);
                    if (EditorCommandPayloadLimits.exceedsLimit(order, EditorCommandPayloadLimits.MAX_LAYER_ORDER_ENTRIES)) {
                        return;
                    }
                    editor.setCanvasLayerOrder(player, payload.getString("group"), nonBlankStringsFromList(order));
                }
            });
        }
    }

    private static List<String> stringsFromList(ListTag tags) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) {
            values.add(tags.getString(i));
        }
        return values;
    }

    private static List<String> nonBlankStringsFromList(ListTag tags) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) {
            String value = tags.getString(i);
            if (value != null && !value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    private static String prerequisiteId(CompoundTag payload) {
        if (payload == null) {
            return "";
        }
        return payload.getString(PREREQUISITE_FIELD);
    }

    private static Set<String> questIdsFromPayload(CompoundTag payload) {
        ListTag tags = payload == null ? new ListTag() : payload.getList("quests", Tag.TAG_STRING);
        Set<String> questIds = new LinkedHashSet<>();
        if (EditorCommandPayloadLimits.exceedsLimit(tags, EditorCommandPayloadLimits.MAX_BULK_EDIT_ENTRIES)) {
            return questIds;
        }
        for (int i = 0; i < tags.size(); i++) {
            String questId = tags.getString(i);
            if (questId != null && !questId.isBlank()) {
                questIds.add(questId);
            }
        }
        return questIds;
    }
}
