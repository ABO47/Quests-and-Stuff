package com.abo47.questsandstuff.network.editor;

import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2SEditorControlPacket(String action) {
    public static C2SEditorControlPacket decode(FriendlyByteBuf buf) {
        return new C2SEditorControlPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(action == null ? "" : action);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> {
                var editor = QuestServices.editor(player.server);
                String op = action == null ? "" : action;
                if ("undo".equals(op)) {
                    editor.undo(player);
                } else if ("redo".equals(op)) {
                    editor.redo(player);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
