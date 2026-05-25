package com.abo47.questsandstuff.network.editor;

import com.abo47.questsandstuff.network.QuestPacketContext;

import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;


public record C2SEditorGroupPacket(String action, String group, String value, int offset) {
    public static C2SEditorGroupPacket decode(FriendlyByteBuf buf) {
        return new C2SEditorGroupPacket(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readVarInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(action == null ? "" : action);
        buf.writeUtf(group == null ? "" : group);
        buf.writeUtf(value == null ? "" : value);
        buf.writeVarInt(offset);
    }

    public void handle(QuestPacketContext context) {
        ServerPlayer player = context.sender();
        if (EditorPacketGuard.canEdit(player)) {
            context.enqueueWork(() -> {
                var editor = QuestServices.editor(player.server);
                switch (action == null ? "" : action) {
                    case "create" -> editor.createGroup(player, group);
                    case "delete" -> editor.deleteGroup(player, group);
                    case "move" -> editor.moveGroup(player, group, offset);
                    case "move_to" -> editor.moveGroupToIndex(player, group, offset);
                    case "rename" -> editor.renameGroup(player, group, value);
                    case "set_icon" -> editor.setGroupIcon(player, group, value);
                    case "set_background" -> editor.setGroupBackground(player, group, value);
                    case "set_canvas_background" -> editor.setGroupCanvasBackground(player, group, value);
                    case "set_text_align" -> editor.setGroupTextAlign(player, group, value);
                    case "set_text_color" -> {
                        try {
                            editor.setGroupTextColor(player, group, Integer.parseInt(value));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    case "set_text_style" -> editor.setGroupTextStyle(player, group, value);
                    case "set_text_size" -> {
                        try {
                            editor.setGroupTextSize(player, group, Integer.parseInt(value));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    case "set_lock_until_unlocked" -> editor.setGroupLockUntilUnlocked(player, group, Boolean.parseBoolean(value));
                    default -> {
                    }
                }
            });
        }
    }
}
