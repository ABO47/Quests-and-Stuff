package com.abo47.questsandstuff.network.editor;

import com.abo47.questsandstuff.network.QuestPacketContext;

import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;


public record C2SEditorAddQuestPacket(String group, String questId, int x, int y, String title) {
    public static C2SEditorAddQuestPacket decode(FriendlyByteBuf buf) {
        return new C2SEditorAddQuestPacket(
                buf.readUtf(),
                buf.readUtf(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readUtf()
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(group == null ? "" : group);
        buf.writeUtf(questId == null ? "" : questId);
        buf.writeVarInt(x);
        buf.writeVarInt(y);
        buf.writeUtf(title == null ? "" : title);
    }

    public void handle(QuestPacketContext context) {
        ServerPlayer player = context.sender();
        if (player != null) {
            context.enqueueWork(() -> QuestServices.editor(player.server).addQuest(player, group, questId, x, y, title));
        }
    }
}
