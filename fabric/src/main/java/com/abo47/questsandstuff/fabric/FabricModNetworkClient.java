package com.abo47.questsandstuff.fabric;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.network.ModPacketType;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class FabricModNetworkClient {
    private static boolean registered;

    private FabricModNetworkClient() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        ClientPlayNetworking.registerGlobalReceiver(FabricModNetwork.CHANNEL, (client, handler, buffer, responseSender) -> {
            ModPacketType<?> type = FabricModNetwork.decodeType(buffer, ModPacketType.Direction.PLAY_TO_CLIENT);
            if (type == null) {
                return;
            }
            Object packet = FabricModNetwork.decodeUnchecked(type, buffer);
            FabricModNetwork.handleUnchecked(type, packet, new ModPacketContext() {
                @Override
                public ServerPlayer sender() {
                    return null;
                }

                @Override
                public void enqueueWork(Runnable work) {
                    client.execute(work);
                }
            });
        });
    }

    public static void sendToServer(Object packet) {
        if (packet == null || !ClientPlayNetworking.canSend(FabricModNetwork.CHANNEL)) {
            return;
        }
        FriendlyByteBuf buffer = FabricModNetwork.encode(packet, ModPacketType.Direction.PLAY_TO_SERVER);
        if (buffer != null) {
            ClientPlayNetworking.send(FabricModNetwork.CHANNEL, buffer);
        }
    }
}