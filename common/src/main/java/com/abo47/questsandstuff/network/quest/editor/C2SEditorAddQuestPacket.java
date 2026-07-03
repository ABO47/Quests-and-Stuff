package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.network.ModPacketContext;

import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;


public record C2SEditorAddQuestPacket(String chapter, String questId, int x, int y, String title) {
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
        buf.writeUtf(chapter == null ? "" : chapter);
        buf.writeUtf(questId == null ? "" : questId);
        buf.writeVarInt(x);
        buf.writeVarInt(y);
        buf.writeUtf(title == null ? "" : title);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (EditorPacketGuard.canEdit(player)) {
            context.enqueueWork(() -> QuestServiceRegistry.editor(player.server).addQuest(player, chapter, questId, x, y, title));
        }
    }
}
