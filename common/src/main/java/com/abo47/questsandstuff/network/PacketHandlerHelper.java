package com.abo47.questsandstuff.network;

import java.util.function.Consumer;

import net.minecraft.server.level.ServerPlayer;

public final class PacketHandlerHelper {
    private PacketHandlerHelper() {
    }

    public static void onServer(ModPacketContext context, Consumer<ServerPlayer> action) {
        ServerPlayer player = context.sender();
        if (player != null) {
            context.enqueueWork(() -> action.accept(player));
        }
    }
}
