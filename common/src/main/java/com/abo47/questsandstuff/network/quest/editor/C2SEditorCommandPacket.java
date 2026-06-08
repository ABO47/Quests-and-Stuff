package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.network.ModPacketContext;

import com.abo47.questsandstuff.quest.QuestServices;
import com.abo47.questsandstuff.quest.editor.command.EditorCommand;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadLimits;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadKeys;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public record C2SEditorCommandPacket(EditorCommand command) {
    public static final String PREREQUISITE_FIELD = EditorCommandPayloadKeys.PREREQUISITE;

    public C2SEditorCommandPacket(String action, CompoundTag payload) {
        this(new EditorCommand(EditorCommandType.fromWireName(action), payload));
    }

    public C2SEditorCommandPacket(EditorCommandType type, CompoundTag payload) {
        this(new EditorCommand(type, payload));
    }

    public static C2SEditorCommandPacket decode(FriendlyByteBuf buf) {
        return new C2SEditorCommandPacket(EditorCommand.decode(buf));
    }

    public void encode(FriendlyByteBuf buf) {
        command.encode(buf);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (EditorPacketGuard.canEdit(player)) {
            context.enqueueWork(() -> {
                var editor = QuestServices.editor(player.server);
                CompoundTag payload = command.payload();
                if (command.type() == EditorCommandType.MOVE_MANY) {
                    String group = EditorCommandPayloads.group(payload);
                    Map<String, int[]> moves = new HashMap<>();
                    ListTag moveTags = EditorCommandPayloads.moves(payload);
                    if (EditorCommandPayloadLimits.exceedsLimit(moveTags, EditorCommandPayloadLimits.MAX_BULK_EDIT_ENTRIES)) {
                        return;
                    }
                    for (int i = 0; i < moveTags.size(); i++) {
                        CompoundTag moveTag = moveTags.getCompound(i);
                        String questId = moveTag.getString(EditorCommandPayloadKeys.QUEST);
                        moves.put(questId, new int[]{moveTag.getInt(EditorCommandPayloadKeys.X), moveTag.getInt(EditorCommandPayloadKeys.Y)});
                    }
                    editor.moveQuestsInGroup(player, group, moves);
                    return;
                }
                if (command.type() == EditorCommandType.SCALE_MANY) {
                    String group = EditorCommandPayloads.group(payload);
                    Map<String, Float> scales = new HashMap<>();
                    ListTag scaleTags = EditorCommandPayloads.scales(payload);
                    if (EditorCommandPayloadLimits.exceedsLimit(scaleTags, EditorCommandPayloadLimits.MAX_BULK_EDIT_ENTRIES)) {
                        return;
                    }
                    for (int i = 0; i < scaleTags.size(); i++) {
                        CompoundTag scaleTag = scaleTags.getCompound(i);
                        String questId = scaleTag.getString(EditorCommandPayloadKeys.QUEST);
                        scales.put(questId, scaleTag.getFloat(EditorCommandPayloadKeys.SCALE));
                    }
                    editor.scaleQuestsInGroup(player, group, scales);
                    return;
                }
                if (command.type() == EditorCommandType.COPY_MANY) {
                    String group = EditorCommandPayloads.group(payload);
                    ListTag questTags = EditorCommandPayloads.list(payload, EditorCommandPayloadKeys.QUESTS, Tag.TAG_STRING);
                    if (EditorCommandPayloadLimits.exceedsLimit(questTags, EditorCommandPayloadLimits.MAX_BULK_EDIT_ENTRIES)) {
                        return;
                    }
                    editor.copyQuestsToClipboard(player, group, EditorCommandPayloads.questIds(payload));
                    return;
                }
                if (command.type() == EditorCommandType.PASTE_CLIPBOARD) {
                    String group = EditorCommandPayloads.group(payload);
                    int x = EditorCommandPayloads.integer(payload, EditorCommandPayloadKeys.X);
                    int y = EditorCommandPayloads.integer(payload, EditorCommandPayloadKeys.Y);
                    editor.pasteClipboardInGroup(player, group, x, y);
                    return;
                }
                if (command.type() == EditorCommandType.PASTE_BLUEPRINT) {
                    String group = EditorCommandPayloads.group(payload);
                    int x = EditorCommandPayloads.integer(payload, EditorCommandPayloadKeys.X);
                    int y = EditorCommandPayloads.integer(payload, EditorCommandPayloadKeys.Y);
                    CanvasBlueprint blueprint = CanvasBlueprint.fromPacketTag(EditorCommandPayloads.compound(payload, EditorCommandPayloadKeys.BLUEPRINT));
                    if (blueprint.isEmpty()) {
                        return;
                    }
                    editor.pasteBlueprintInGroup(player, group, x, y, blueprint);
                    return;
                }
                if (command.type() == EditorCommandType.PREREQUISITE_ADD) {
                    editor.setQuestPrerequisite(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.prerequisite(payload), true);
                    return;
                }
                if (command.type() == EditorCommandType.PREREQUISITE_REMOVE) {
                    editor.setQuestPrerequisite(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.prerequisite(payload), false);
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_ICON) {
                    editor.setQuestIcon(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.string(payload, EditorCommandPayloadKeys.ICON));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_REPEATABLE) {
                    editor.setQuestRepeatable(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.bool(payload, EditorCommandPayloadKeys.ENABLED));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_HIDDEN_MODE) {
                    editor.setQuestHiddenMode(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.string(payload, EditorCommandPayloadKeys.MODE));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_VISUAL_HIDDEN) {
                    editor.setQuestVisualHidden(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.bool(payload, EditorCommandPayloadKeys.HIDDEN));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND) {
                    editor.setQuestCompletionSound(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.string(payload, EditorCommandPayloadKeys.SOUND));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND_MANY) {
                    editor.setQuestCompletionSound(player, EditorCommandPayloads.questIds(payload), EditorCommandPayloads.string(payload, EditorCommandPayloadKeys.SOUND));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND_VOLUME) {
                    editor.setQuestCompletionSoundVolume(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.integer(payload, EditorCommandPayloadKeys.VOLUME));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND_VOLUME_MANY) {
                    editor.setQuestCompletionSoundVolume(player, EditorCommandPayloads.questIds(payload), EditorCommandPayloads.integer(payload, EditorCommandPayloadKeys.VOLUME));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_COMPLETION_HUD_BACKGROUND) {
                    editor.setQuestCompletionHudBackground(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.string(payload, EditorCommandPayloadKeys.BACKGROUND));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_COMPLETION_HUD_BACKGROUND_MANY) {
                    editor.setQuestCompletionHudBackground(player, EditorCommandPayloads.questIds(payload), EditorCommandPayloads.string(payload, EditorCommandPayloadKeys.BACKGROUND));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_BACKGROUND) {
                    editor.setQuestBackground(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.string(payload, EditorCommandPayloadKeys.BACKGROUND), EditorCommandPayloads.bool(payload, EditorCommandPayloadKeys.GRAYSCALE));
                    return;
                }
                if (command.type() == EditorCommandType.QUEST_BACKGROUND_MANY) {
                    editor.setQuestBackground(player, EditorCommandPayloads.questIds(payload), EditorCommandPayloads.string(payload, EditorCommandPayloadKeys.BACKGROUND), EditorCommandPayloads.bool(payload, EditorCommandPayloadKeys.GRAYSCALE));
                    return;
                }
                if (command.type() == EditorCommandType.DESCRIPTION_PUT) {
                    ListTag description = EditorCommandPayloads.description(payload);
                    if (EditorCommandPayloadLimits.exceedsLimit(description, EditorCommandPayloadLimits.MAX_DESCRIPTION_LINES)) {
                        return;
                    }
                    editor.updateQuestDescription(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.stringsFrom(description));
                    return;
                }
                if (command.type() == EditorCommandType.CONNECTION_COLOR) {
                    editor.setConnectionColor(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.prerequisite(payload), EditorCommandPayloads.integer(payload, EditorCommandPayloadKeys.COLOR));
                    return;
                }
                if (command.type() == EditorCommandType.CONNECTION_MODE) {
                    editor.setConnectionMode(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.prerequisite(payload), EditorCommandPayloads.bool(payload, EditorCommandPayloadKeys.GRID));
                    return;
                }
                if (command.type() == EditorCommandType.CONNECTION_HIDDEN) {
                    editor.setConnectionHidden(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.prerequisite(payload), EditorCommandPayloads.bool(payload, EditorCommandPayloadKeys.HIDDEN));
                    return;
                }
                if (command.type() == EditorCommandType.TASK_PUT) {
                    String json = EditorCommandPayloads.json(payload);
                    if (EditorCommandPayloadLimits.exceedsLength(json, EditorCommandPayloadLimits.MAX_EDITOR_JSON_LENGTH)) {
                        return;
                    }
                    editor.putQuestTask(player, EditorCommandPayloads.quest(payload), json);
                    return;
                }
                if (command.type() == EditorCommandType.TASK_REMOVE) {
                    editor.removeQuestTask(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.task(payload));
                    return;
                }
                if (command.type() == EditorCommandType.TASK_MOVE) {
                    editor.moveQuestTask(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.task(payload), EditorCommandPayloads.integer(payload, EditorCommandPayloadKeys.OFFSET));
                    return;
                }
                if (command.type() == EditorCommandType.REWARD_PUT) {
                    String json = EditorCommandPayloads.json(payload);
                    if (EditorCommandPayloadLimits.exceedsLength(json, EditorCommandPayloadLimits.MAX_EDITOR_JSON_LENGTH)) {
                        return;
                    }
                    editor.putQuestReward(player, EditorCommandPayloads.quest(payload), json);
                    return;
                }
                if (command.type() == EditorCommandType.REWARD_REMOVE) {
                    editor.removeQuestReward(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.reward(payload));
                    return;
                }
                if (command.type() == EditorCommandType.REWARD_MOVE) {
                    editor.moveQuestReward(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.reward(payload), EditorCommandPayloads.integer(payload, EditorCommandPayloadKeys.OFFSET));
                    return;
                }
                if (command.type() == EditorCommandType.CANVAS_IMAGE_PUT) {
                    editor.putCanvasImage(player, EditorCommandPayloads.group(payload), CanvasLayerNbt.imageFromTag(EditorCommandPayloads.compound(payload, EditorCommandPayloadKeys.IMAGE)));
                    return;
                }
                if (command.type() == EditorCommandType.CANVAS_IMAGE_REMOVE) {
                    editor.removeCanvasImage(player, EditorCommandPayloads.group(payload), EditorCommandPayloads.string(payload, EditorCommandPayloadKeys.ID));
                    return;
                }
                if (command.type() == EditorCommandType.CANVAS_TEXT_PUT) {
                    CompoundTag text = EditorCommandPayloads.compound(payload, EditorCommandPayloadKeys.TEXT);
                    if (EditorCommandPayloadLimits.exceedsLimit(text.getList(EditorCommandPayloadKeys.SPANS, Tag.TAG_COMPOUND), EditorCommandPayloadLimits.MAX_TEXT_SPANS)) {
                        return;
                    }
                    editor.putCanvasText(player, EditorCommandPayloads.group(payload), CanvasLayerNbt.textFromTag(text));
                    return;
                }
                if (command.type() == EditorCommandType.CANVAS_TEXT_REMOVE) {
                    editor.removeCanvasText(player, EditorCommandPayloads.group(payload), EditorCommandPayloads.string(payload, EditorCommandPayloadKeys.ID));
                    return;
                }
                if (command.type() == EditorCommandType.CANVAS_LAYER_ORDER) {
                    ListTag order = EditorCommandPayloads.order(payload);
                    if (EditorCommandPayloadLimits.exceedsLimit(order, EditorCommandPayloadLimits.MAX_LAYER_ORDER_ENTRIES)) {
                        return;
                    }
                    editor.setCanvasLayerOrder(player, EditorCommandPayloads.group(payload), EditorCommandPayloads.nonBlankStringsFrom(order));
                }
            });
        }
    }
}
