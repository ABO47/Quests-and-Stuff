package com.abo47.questsandstuff.network.editor;

import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

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

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> QuestServices.editor(player.server).addQuest(player, group, questId, x, y, title));
        }
        context.setPacketHandled(true);
    }
}
