package com.abo47.questsandstuff.client.tablet.quest.editor;

import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorCommandPacket;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
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

    static void send(EditorCommandType command, CompoundTag payload) {
        ModNetwork.sendToServer(new C2SEditorCommandPacket(command, payload));
    }

    static void run(Player player, EditorCommandType command, CompoundTag payload, Consumer<ServerPlayer> serverAction) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverAction.accept(serverPlayer);
            return;
        }
        send(command, payload);
    }
}
