package com.abo47.questsandstuff.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.network.ModPacketType;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ForgeModNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "network"),
            () -> ModNetwork.PROTOCOL,
            ModNetwork.PROTOCOL::equals,
            ModNetwork.PROTOCOL::equals
    );
    private static boolean registered;

    private ForgeModNetwork() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        ModNetwork.registerPackets(ForgeModNetwork::registerPacket);
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        if (player == null || player.connection == null || player.connection.connection == null) {
            return;
        }
        if (!player.connection.connection.isConnected() || player.connection.connection.channel() == null) {
            return;
        }
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    private static <T> void registerPacket(ModPacketType<T> packet) {
        CHANNEL.messageBuilder(packet.type(), packet.id(), direction(packet.direction()))
                .encoder((value, buffer) -> packet.encoder().encode(value, buffer))
                .decoder(packet.decoder()::decode)
                .consumerMainThread((value, contextSupplier) -> handle(packet, value, contextSupplier.get()))
                .add();
    }

    private static <T> void handle(ModPacketType<T> packet, T value, NetworkEvent.Context context) {
        packet.handler().handle(value, new ModPacketContext() {
            @Override
            public ServerPlayer sender() {
                return context.getSender();
            }

            @Override
            public void enqueueWork(Runnable work) {
                context.enqueueWork(work);
            }
        });
        context.setPacketHandled(true);
    }

    private static NetworkDirection direction(ModPacketType.Direction direction) {
        return direction == ModPacketType.Direction.PLAY_TO_CLIENT
                ? NetworkDirection.PLAY_TO_CLIENT
                : NetworkDirection.PLAY_TO_SERVER;
    }
}
