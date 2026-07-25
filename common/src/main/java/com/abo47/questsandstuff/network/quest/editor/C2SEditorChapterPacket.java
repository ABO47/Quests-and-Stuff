package com.abo47.questsandstuff.network.quest.editor;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.network.PacketBufHelper;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;

public record C2SEditorChapterPacket(String action, String chapter, String value, int offset) {
    public static C2SEditorChapterPacket decode(FriendlyByteBuf buf) {
        return new C2SEditorChapterPacket(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readVarInt());
    }

    public void encode(FriendlyByteBuf buf) {
        PacketBufHelper.writeUtfSafe(buf, action);
        PacketBufHelper.writeUtfSafe(buf, chapter);
        PacketBufHelper.writeUtfSafe(buf, value);
        buf.writeVarInt(offset);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (EditorPacketGuard.canEdit(player)) {
            context.enqueueWork(() -> {
                var editor = QuestServiceRegistry.editor(player.server);
                switch (action == null ? "" : action) {
                    case "create" -> editor.createChapter(player, chapter);
                    case "delete" -> editor.deleteChapter(player, chapter);
                    case "move" -> editor.moveChapter(player, chapter, offset);
                    case "move_to" -> editor.moveChapterToIndex(player, chapter, offset);
                    case "rename" -> editor.renameChapter(player, chapter, value);
                    case "set_icon" -> editor.setChapterIcon(player, chapter, value);
                    case "set_background" -> editor.setChapterBackground(player, chapter, value);
                    case "set_canvas_background" -> editor.setChapterCanvasBackground(player, chapter, value);
                    case "set_text_align" -> editor.setChapterTextAlign(player, chapter, value);
                    case "set_text_color" -> {
                        try {
                            editor.setChapterTextColor(player, chapter, Integer.parseInt(value));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    case "set_text_style" -> editor.setChapterTextStyle(player, chapter, value);
                    case "set_text_size" -> {
                        try {
                            editor.setChapterTextSize(player, chapter, Integer.parseInt(value));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    case "set_lock_until_unlocked" -> editor.setChapterLockUntilUnlocked(player, chapter, Boolean.parseBoolean(value));
                    case "set_hide_until_unlocked" -> editor.setChapterHideUntilUnlocked(player, chapter, Boolean.parseBoolean(value));
                    default -> {
                    }
                }
            });
        }
    }
}
