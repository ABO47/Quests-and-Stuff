package com.abo47.questsandstuff.fabric;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.network.ModPacketType;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;

public final class FabricModNetwork {
    static final ResourceLocation CHANNEL = ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "network");
    private static boolean registered;

    private FabricModNetwork() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL, (server, player, handler, buffer, responseSender) -> handleServerbound(player, buffer));
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        if (packet == null || player == null || player.connection == null || !ServerPlayNetworking.canSend(player, CHANNEL)) {
            return;
        }
        FriendlyByteBuf buffer = encode(packet, ModPacketType.Direction.PLAY_TO_CLIENT);
        if (buffer != null) {
            ServerPlayNetworking.send(player, CHANNEL, buffer);
        }
    }

    public static void sendToServer(Object packet) {
        try {
            Class<?> clientNetwork = Class.forName("com.abo47.questsandstuff.fabric.FabricModNetworkClient");
            Method method = clientNetwork.getMethod("sendToServer", Object.class);
            method.invoke(null, packet);
        } catch (ReflectiveOperationException e) {
            QuestsAndStuffMod.LOGGER.warn("Failed to send Fabric client packet", e);
        }
    }

    static FriendlyByteBuf encode(Object packet, ModPacketType.Direction direction) {
        ModPacketType<?> type = direction == ModPacketType.Direction.PLAY_TO_CLIENT
                ? ModNetwork.clientboundType(packet)
                : ModNetwork.serverboundType(packet);
        if (type == null) {
            QuestsAndStuffMod.LOGGER.warn("Tried to send unregistered {} packet {}", direction, packet.getClass().getName());
            return null;
        }
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeVarInt(type.id());
        encodeUnchecked(type, packet, buffer);
        return buffer;
    }

    private static void handleServerbound(ServerPlayer player, FriendlyByteBuf buffer) {
        ModPacketType<?> type = decodeType(buffer, ModPacketType.Direction.PLAY_TO_SERVER);
        if (type == null) {
            return;
        }
        Object packet = decodeUnchecked(type, buffer);
        handleUnchecked(type, packet, new ModPacketContext() {
            @Override
            public ServerPlayer sender() {
                return player;
            }

            @Override
            public void enqueueWork(Runnable work) {
                player.server.execute(work);
            }
        });
    }

    static ModPacketType<?> decodeType(FriendlyByteBuf buffer, ModPacketType.Direction expectedDirection) {
        int id = buffer.readVarInt();
        ModPacketType<?> type = ModNetwork.packetById(id);
        if (type == null || type.direction() != expectedDirection) {
            QuestsAndStuffMod.LOGGER.warn("Received unknown or wrong-direction Fabric packet id {}", id);
            return null;
        }
        return type;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void encodeUnchecked(ModPacketType type, Object packet, FriendlyByteBuf buffer) {
        type.encoder().encode(packet, buffer);
    }

    @SuppressWarnings("rawtypes")
    static Object decodeUnchecked(ModPacketType type, FriendlyByteBuf buffer) {
        return type.decoder().decode(buffer);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void handleUnchecked(ModPacketType type, Object packet, ModPacketContext context) {
        type.handler().handle(packet, context);
    }
}
