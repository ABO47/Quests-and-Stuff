package com.abo47.questsandstuff.fabric;

import com.abo47.questsandstuff.network.QuestPacketContext;
import com.abo47.questsandstuff.network.QuestPacketType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class FabricQuestNetworkClient {
    private static boolean registered;

    private FabricQuestNetworkClient() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        ClientPlayNetworking.registerGlobalReceiver(FabricQuestNetwork.CHANNEL, (client, handler, buffer, responseSender) -> {
            QuestPacketType<?> type = FabricQuestNetwork.decodeType(buffer, QuestPacketType.Direction.PLAY_TO_CLIENT);
            if (type == null) {
                return;
            }
            Object packet = FabricQuestNetwork.decodeUnchecked(type, buffer);
            FabricQuestNetwork.handleUnchecked(type, packet, new QuestPacketContext() {
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
        if (packet == null || !ClientPlayNetworking.canSend(FabricQuestNetwork.CHANNEL)) {
            return;
        }
        FriendlyByteBuf buffer = FabricQuestNetwork.encode(packet, QuestPacketType.Direction.PLAY_TO_SERVER);
        if (buffer != null) {
            ClientPlayNetworking.send(FabricQuestNetwork.CHANNEL, buffer);
        }
    }
}