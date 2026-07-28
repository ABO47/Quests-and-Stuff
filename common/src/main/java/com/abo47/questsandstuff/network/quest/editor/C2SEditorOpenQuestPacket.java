package com.abo47.questsandstuff.network.quest.editor;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.network.PacketBufHelper;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;

public record C2SEditorOpenQuestPacket(String questId) {
    public static C2SEditorOpenQuestPacket decode(FriendlyByteBuf buf) {
        return new C2SEditorOpenQuestPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        PacketBufHelper.writeUtfSafe(buf, questId);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (EditorPacketGuard.canEdit(player)) {
            context.enqueueWork(() -> QuestServiceRegistry.editor(player.server).openQuest(player, questId));
        }
    }
}
