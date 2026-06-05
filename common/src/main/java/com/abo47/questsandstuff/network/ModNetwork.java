package com.abo47.questsandstuff.network;

import com.abo47.questsandstuff.network.quest.editor.C2SEditorAddQuestPacket;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorCommandPacket;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorControlPacket;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorGroupPacket;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorOpenGroupPacket;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorOpenQuestPacket;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorRemoveQuestPacket;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorUpdateQuestPacket;
import com.abo47.questsandstuff.network.quest.runtime.C2SClaimAllRewardsPacket;
import com.abo47.questsandstuff.network.quest.runtime.C2SClaimRewardPacket;
import com.abo47.questsandstuff.network.quest.runtime.C2SClaimSelectableRewardPacket;
import com.abo47.questsandstuff.network.quest.runtime.C2SManualItemSubmitPacket;
import com.abo47.questsandstuff.network.quest.runtime.C2SManualTaskPacket;
import com.abo47.questsandstuff.network.quest.runtime.C2SManualXpSubmitPacket;
import com.abo47.questsandstuff.network.quest.runtime.C2SResetQuestPacket;
import com.abo47.questsandstuff.network.quest.runtime.C2STogglePinPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CDescriptionSyncPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CDeltaSyncPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CDisplayCacheSyncPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CEditorMutationPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CFullSyncPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CPinnedSyncPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CQuestEventPacket;
import com.abo47.questsandstuff.platform.Services;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class ModNetwork {
    public static final String PROTOCOL = "1";

    private static final List<ModPacketType<?>> PACKETS = buildPackets();
    private static final Map<Integer, ModPacketType<?>> PACKETS_BY_ID = byId(PACKETS);
    private static final Map<Class<?>, ModPacketType<?>> CLIENTBOUND_BY_CLASS = byClass(PACKETS, ModPacketType.Direction.PLAY_TO_CLIENT);
    private static final Map<Class<?>, ModPacketType<?>> SERVERBOUND_BY_CLASS = byClass(PACKETS, ModPacketType.Direction.PLAY_TO_SERVER);
    private static volatile BiConsumer<ServerPlayer, Object> testPacketSink;

    private ModNetwork() {
    }

    public static void register() {
        Services.platform().registerNetwork();
    }

    public static void registerPackets(PacketRegistrar registrar) {
        for (ModPacketType<?> packet : PACKETS) {
            registerPacket(registrar, packet);
        }
    }

    public static ModPacketType<?> packetById(int id) {
        return PACKETS_BY_ID.get(id);
    }

    public static ModPacketType<?> clientboundType(Object packet) {
        return typeFor(packet, CLIENTBOUND_BY_CLASS);
    }

    public static ModPacketType<?> serverboundType(Object packet) {
        return typeFor(packet, SERVERBOUND_BY_CLASS);
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        BiConsumer<ServerPlayer, Object> sink = testPacketSink;
        if (sink != null) {
            sink.accept(player, packet);
        }
        if (player == null || player.connection == null) {
            return;
        }
        Services.platform().sendToPlayer(packet, player);
    }

    public static void sendToServer(Object packet) {
        Services.platform().sendToServer(packet);
    }

    public static void setTestPacketSink(BiConsumer<ServerPlayer, Object> sink) {
        testPacketSink = sink;
    }

    public static void clearTestPacketSink() {
        testPacketSink = null;
    }

    private static List<ModPacketType<?>> buildPackets() {
        List<ModPacketType<?>> packets = new ArrayList<>();
        int id = 0;
        packets.add(type(id++, S2CFullSyncPacket.class, ModPacketType.Direction.PLAY_TO_CLIENT, S2CFullSyncPacket::encode, S2CFullSyncPacket::decode, S2CFullSyncPacket::handle));
        packets.add(type(id++, S2CDeltaSyncPacket.class, ModPacketType.Direction.PLAY_TO_CLIENT, S2CDeltaSyncPacket::encode, S2CDeltaSyncPacket::decode, S2CDeltaSyncPacket::handle));
        packets.add(type(id++, S2CPinnedSyncPacket.class, ModPacketType.Direction.PLAY_TO_CLIENT, S2CPinnedSyncPacket::encode, S2CPinnedSyncPacket::decode, S2CPinnedSyncPacket::handle));
        packets.add(type(id++, S2CDescriptionSyncPacket.class, ModPacketType.Direction.PLAY_TO_CLIENT, S2CDescriptionSyncPacket::encode, S2CDescriptionSyncPacket::decode, S2CDescriptionSyncPacket::handle));
        packets.add(type(id++, S2CDisplayCacheSyncPacket.class, ModPacketType.Direction.PLAY_TO_CLIENT, S2CDisplayCacheSyncPacket::encode, S2CDisplayCacheSyncPacket::decode, S2CDisplayCacheSyncPacket::handle));
        packets.add(type(id++, S2CQuestEventPacket.class, ModPacketType.Direction.PLAY_TO_CLIENT, S2CQuestEventPacket::encode, S2CQuestEventPacket::decode, S2CQuestEventPacket::handle));
        packets.add(type(id++, S2CEditorMutationPacket.class, ModPacketType.Direction.PLAY_TO_CLIENT, S2CEditorMutationPacket::encode, S2CEditorMutationPacket::decode, S2CEditorMutationPacket::handle));
        packets.add(type(id++, C2SClaimAllRewardsPacket.class, ModPacketType.Direction.PLAY_TO_SERVER, C2SClaimAllRewardsPacket::encode, C2SClaimAllRewardsPacket::decode, C2SClaimAllRewardsPacket::handle));
        packets.add(type(id++, C2SEditorAddQuestPacket.class, ModPacketType.Direction.PLAY_TO_SERVER, C2SEditorAddQuestPacket::encode, C2SEditorAddQuestPacket::decode, C2SEditorAddQuestPacket::handle));
        packets.add(type(id++, C2SEditorUpdateQuestPacket.class, ModPacketType.Direction.PLAY_TO_SERVER, C2SEditorUpdateQuestPacket::encode, C2SEditorUpdateQuestPacket::decode, C2SEditorUpdateQuestPacket::handle));
        packets.add(type(id++, C2SEditorRemoveQuestPacket.class, ModPacketType.Direction.PLAY_TO_SERVER, C2SEditorRemoveQuestPacket::encode, C2SEditorRemoveQuestPacket::decode, C2SEditorRemoveQuestPacket::handle));
        packets.add(type(id++, C2SEditorGroupPacket.class, ModPacketType.Direction.PLAY_TO_SERVER, C2SEditorGroupPacket::encode, C2SEditorGroupPacket::decode, C2SEditorGroupPacket::handle));
        packets.add(type(id++, C2SEditorOpenGroupPacket.class, ModPacketType.Direction.PLAY_TO_SERVER, C2SEditorOpenGroupPacket::encode, C2SEditorOpenGroupPacket::decode, C2SEditorOpenGroupPacket::handle));
        packets.add(type(id++, C2SEditorOpenQuestPacket.class, ModPacketType.Direction.PLAY_TO_SERVER, C2SEditorOpenQuestPacket::encode, C2SEditorOpenQuestPacket::decode, C2SEditorOpenQuestPacket::handle));
        packets.add(type(id++, C2SEditorControlPacket.class, ModPacketType.Direction.PLAY_TO_SERVER, C2SEditorControlPacket::encode, C2SEditorControlPacket::decode, C2SEditorControlPacket::handle));
        packets.add(type(id++, C2SEditorCommandPacket.class, ModPacketType.Direction.PLAY_TO_SERVER, C2SEditorCommandPacket::encode, C2SEditorCommandPacket::decode, C2SEditorCommandPacket::handle));
        packets.add(type(id++, C2SClaimRewardPacket.class, ModPacketType.Direction.PLAY_TO_SERVER, C2SClaimRewardPacket::encode, C2SClaimRewardPacket::decode, C2SClaimRewardPacket::handle));
        packets.add(type(id++, C2SClaimSelectableRewardPacket.class, ModPacketType.Direction.PLAY_TO_SERVER, C2SClaimSelectableRewardPacket::encode, C2SClaimSelectableRewardPacket::decode, C2SClaimSelectableRewardPacket::handle));
        packets.add(type(id++, C2SManualTaskPacket.class, ModPacketType.Direction.PLAY_TO_SERVER, C2SManualTaskPacket::encode, C2SManualTaskPacket::decode, C2SManualTaskPacket::handle));
        packets.add(type(id++, C2SManualItemSubmitPacket.class, ModPacketType.Direction.PLAY_TO_SERVER, C2SManualItemSubmitPacket::encode, C2SManualItemSubmitPacket::decode, C2SManualItemSubmitPacket::handle));
        packets.add(type(id++, C2SManualXpSubmitPacket.class, ModPacketType.Direction.PLAY_TO_SERVER, C2SManualXpSubmitPacket::encode, C2SManualXpSubmitPacket::decode, C2SManualXpSubmitPacket::handle));
        packets.add(type(id++, C2SResetQuestPacket.class, ModPacketType.Direction.PLAY_TO_SERVER, C2SResetQuestPacket::encode, C2SResetQuestPacket::decode, C2SResetQuestPacket::handle));
        packets.add(type(id, C2STogglePinPacket.class, ModPacketType.Direction.PLAY_TO_SERVER, C2STogglePinPacket::encode, C2STogglePinPacket::decode, C2STogglePinPacket::handle));
        return List.copyOf(packets);
    }

    private static <T> ModPacketType<T> type(
            int id,
            Class<T> type,
            ModPacketType.Direction direction,
            ModPacketType.PacketEncoder<T> encoder,
            ModPacketType.PacketDecoder<T> decoder,
            ModPacketType.PacketHandler<T> handler
    ) {
        return new ModPacketType<>(id, type, direction, encoder, decoder, handler);
    }

    private static Map<Integer, ModPacketType<?>> byId(List<ModPacketType<?>> packets) {
        Map<Integer, ModPacketType<?>> map = new HashMap<>();
        for (ModPacketType<?> packet : packets) {
            map.put(packet.id(), packet);
        }
        return Map.copyOf(map);
    }

    private static Map<Class<?>, ModPacketType<?>> byClass(List<ModPacketType<?>> packets, ModPacketType.Direction direction) {
        Map<Class<?>, ModPacketType<?>> map = new HashMap<>();
        for (ModPacketType<?> packet : packets) {
            if (packet.direction() == direction) {
                map.put(packet.type(), packet);
            }
        }
        return Map.copyOf(map);
    }

    private static ModPacketType<?> typeFor(Object packet, Map<Class<?>, ModPacketType<?>> packets) {
        if (packet == null) {
            return null;
        }
        return packets.get(packet.getClass());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerPacket(PacketRegistrar registrar, ModPacketType packet) {
        registrar.register(packet);
    }

    public interface PacketRegistrar {
        <T> void register(ModPacketType<T> packet);
    }
}
