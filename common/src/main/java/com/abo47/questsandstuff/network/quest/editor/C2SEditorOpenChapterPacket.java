package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.network.ModPacketContext;

import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;


public record C2SEditorOpenChapterPacket(String chapter) {
    public static C2SEditorOpenChapterPacket decode(FriendlyByteBuf buf) {
        return new C2SEditorOpenChapterPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(group == null ? "" : group);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (EditorPacketGuard.canEdit(player)) {
            context.enqueueWork(() -> QuestServiceRegistry.editor(player.server).openChapter(player, group));
        }
    }
}
