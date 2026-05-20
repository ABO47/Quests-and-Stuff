package com.abo47.questsandstuff.network.editor;

import com.abo47.questsandstuff.quest.QuestServices;
import com.abo47.questsandstuff.quest.editor.command.EditorCommand;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

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

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> {
                var editor = QuestServices.editor(player.server);
                CompoundTag payload = command.payload();
                if (command.type() == EditorCommandType.MOVE_MANY) {
                    String group = payload == null ? "" : payload.getString("group");
                    Map<String, int[]> moves = new HashMap<>();
                    ListTag moveTags = payload == null ? new ListTag() : payload.getList("moves", Tag.TAG_COMPOUND);
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
                if (command.type() == EditorCommandType.DESCRIPTION_PUT) {
                    editor.updateQuestDescription(player, payload.getString("quest"), stringsFromList(payload.getList("description", Tag.TAG_STRING)));
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
                    editor.putQuestTask(player, payload.getString("quest"), payload.getString("json"));
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
                    editor.putQuestReward(player, payload.getString("quest"), payload.getString("json"));
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
                    editor.putCanvasText(player, payload.getString("group"), CanvasLayerNbt.textFromTag(payload.getCompound("text")));
                    return;
                }
                if (command.type() == EditorCommandType.CANVAS_TEXT_REMOVE) {
                    editor.removeCanvasText(player, payload.getString("group"), payload.getString("id"));
                    return;
                }
                if (command.type() == EditorCommandType.CANVAS_LAYER_ORDER) {
                    editor.setCanvasLayerOrder(player, payload.getString("group"), CanvasLayerNbt.stringsFromListTag(payload.getList("order", Tag.TAG_STRING)));
                }
            });
        }
        context.setPacketHandled(true);
    }

    private static List<String> stringsFromList(ListTag tags) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) {
            values.add(tags.getString(i));
        }
        return values;
    }

    private static String prerequisiteId(CompoundTag payload) {
        if (payload == null) {
            return "";
        }
        return payload.getString(PREREQUISITE_FIELD);
    }
}
