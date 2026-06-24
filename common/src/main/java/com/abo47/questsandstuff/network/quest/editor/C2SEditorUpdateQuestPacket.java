package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.network.ModPacketContext;

import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;


public record C2SEditorUpdateQuestPacket(String questId, String title, String subtitle) {
    public static C2SEditorUpdateQuestPacket decode(FriendlyByteBuf buf) {
        return new C2SEditorUpdateQuestPacket(buf.readUtf(), buf.readUtf(), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId == null ? "" : questId);
        buf.writeUtf(title == null ? "" : title);
        buf.writeUtf(subtitle == null ? "" : subtitle);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (EditorPacketGuard.canEdit(player)) {
            context.enqueueWork(() -> QuestServices.editor(player.server)
                    .updateQuestDisplay(player, questId, title, subtitle));
        }
    }
}
