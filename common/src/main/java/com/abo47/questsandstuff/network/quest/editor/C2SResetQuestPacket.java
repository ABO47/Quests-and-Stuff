package com.abo47.questsandstuff.network.quest.editor;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.network.PacketBufHelper;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.abo47.questsandstuff.quest.editor.QuestEditorPermissions;

public record C2SResetQuestPacket(String questId) {
    public static C2SResetQuestPacket decode(FriendlyByteBuf buf) {
        return new C2SResetQuestPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        PacketBufHelper.writeUtfSafe(buf, questId);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (QuestEditorPermissions.canEdit(player)) {
            context.enqueueWork(() -> QuestServiceRegistry.engine(player.server)
                    .resetQuest(player, questId == null ? "" : questId.trim()));
        }
    }
}
