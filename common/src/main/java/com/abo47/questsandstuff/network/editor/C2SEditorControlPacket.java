package com.abo47.questsandstuff.network.editor;

import com.abo47.questsandstuff.network.QuestPacketContext;

import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;


public record C2SEditorControlPacket(String action) {
    public static C2SEditorControlPacket decode(FriendlyByteBuf buf) {
        return new C2SEditorControlPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(action == null ? "" : action);
    }

    public void handle(QuestPacketContext context) {
        ServerPlayer player = context.sender();
        if (player != null) {
            context.enqueueWork(() -> {
                var editor = QuestServices.editor(player.server);
                String op = action == null ? "" : action;
                if ("undo".equals(op)) {
                    editor.undo(player);
                } else if ("redo".equals(op)) {
                    editor.redo(player);
                }
            });
        }
    }
}
