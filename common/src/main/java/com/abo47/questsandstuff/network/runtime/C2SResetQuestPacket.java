package com.abo47.questsandstuff.network.runtime;

import com.abo47.questsandstuff.network.QuestPacketContext;

import com.abo47.questsandstuff.quest.QuestServices;
import com.abo47.questsandstuff.quest.editor.QuestEditorPermissions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;


public record C2SResetQuestPacket(String questId) {
    public static C2SResetQuestPacket decode(FriendlyByteBuf buf) {
        return new C2SResetQuestPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId == null ? "" : questId);
    }

    public void handle(QuestPacketContext context) {
        ServerPlayer player = context.sender();
        if (QuestEditorPermissions.canEdit(player)) {
            context.enqueueWork(() -> QuestServices.engine(player.server)
                    .resetQuest(player, questId == null ? "" : questId.trim()));
        }
    }
}
