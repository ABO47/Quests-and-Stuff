package com.abo47.questsandstuff.client.tablet.editor;

import com.abo47.questsandstuff.network.QuestNetwork;
import com.abo47.questsandstuff.network.editor.C2SEditorCommandPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

final class EditorCommandSender {
    private EditorCommandSender() {
    }

    static String id(String value) {
        return value == null ? "" : value.trim();
    }

    static String value(String value) {
        return value == null ? "" : value.trim();
    }

    static CompoundTag questPayload(String questId) {
        CompoundTag payload = new CompoundTag();
        payload.putString("quest", questId);
        return payload;
    }

    static void send(String command, CompoundTag payload) {
        QuestNetwork.sendToServer(new C2SEditorCommandPacket(command, payload));
    }

    static void run(Player player, String command, CompoundTag payload, Consumer<ServerPlayer> serverAction) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverAction.accept(serverPlayer);
            return;
        }
        send(command, payload);
    }
}
