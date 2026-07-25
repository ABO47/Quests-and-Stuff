package com.abo47.questsandstuff.network.quest.editor;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.network.PacketBufHelper;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;

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
        PacketBufHelper.writeUtfSafe(buf, chapter);
        PacketBufHelper.writeUtfSafe(buf, questId);
        buf.writeVarInt(x);
        buf.writeVarInt(y);
        PacketBufHelper.writeUtfSafe(buf, title);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (EditorPacketGuard.canEdit(player)) {
            context.enqueueWork(() -> QuestServiceRegistry.editor(player.server).addQuest(player, chapter, questId, x, y, title));
        }
    }
}
