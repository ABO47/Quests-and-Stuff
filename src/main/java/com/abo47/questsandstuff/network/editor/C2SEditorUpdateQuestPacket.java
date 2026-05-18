package com.abo47.questsandstuff.network.editor;

import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2SEditorUpdateQuestPacket(String questId, String title, String subtitle) {
    public static C2SEditorUpdateQuestPacket decode(FriendlyByteBuf buf) {
        return new C2SEditorUpdateQuestPacket(buf.readUtf(), buf.readUtf(), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId == null ? "" : questId);
        buf.writeUtf(title == null ? "" : title);
        buf.writeUtf(subtitle == null ? "" : subtitle);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> QuestServices.editor(player.server)
                    .updateQuestDisplay(player, questId, title, subtitle));
        }
        context.setPacketHandled(true);
    }
}
