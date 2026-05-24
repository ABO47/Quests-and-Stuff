package com.abo47.questsandstuff.network.editor;

import com.abo47.questsandstuff.network.QuestPacketContext;

import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;


public record C2SEditorRemoveQuestPacket(String questId) {
    public static C2SEditorRemoveQuestPacket decode(FriendlyByteBuf buf) {
        return new C2SEditorRemoveQuestPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId == null ? "" : questId);
    }

    public void handle(QuestPacketContext context) {
        ServerPlayer player = context.sender();
        if (EditorPacketGuard.canEdit(player)) {
            context.enqueueWork(() -> QuestServices.editor(player.server).removeQuest(player, questId));
        }
    }
}
