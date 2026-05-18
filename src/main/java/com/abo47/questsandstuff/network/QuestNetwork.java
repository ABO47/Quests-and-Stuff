package com.abo47.questsandstuff.network;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.network.runtime.C2SClaimAllRewardsPacket;
import com.abo47.questsandstuff.network.runtime.C2SClaimRewardPacket;
import com.abo47.questsandstuff.network.runtime.C2SClaimSelectableRewardPacket;
import com.abo47.questsandstuff.network.editor.C2SEditorAddQuestPacket;
import com.abo47.questsandstuff.network.editor.C2SEditorCommandPacket;
import com.abo47.questsandstuff.network.editor.C2SEditorControlPacket;
import com.abo47.questsandstuff.network.editor.C2SEditorGroupPacket;
import com.abo47.questsandstuff.network.editor.C2SEditorOpenGroupPacket;
import com.abo47.questsandstuff.network.editor.C2SEditorOpenQuestPacket;
import com.abo47.questsandstuff.network.editor.C2SEditorRemoveQuestPacket;
import com.abo47.questsandstuff.network.editor.C2SEditorUpdateQuestPacket;
import com.abo47.questsandstuff.network.runtime.C2SManualItemSubmitPacket;
import com.abo47.questsandstuff.network.runtime.C2SManualTaskPacket;
import com.abo47.questsandstuff.network.runtime.C2SManualXpSubmitPacket;
import com.abo47.questsandstuff.network.runtime.C2SResetQuestPacket;
import com.abo47.questsandstuff.network.sync.S2CDescriptionSyncPacket;
import com.abo47.questsandstuff.network.sync.S2CDeltaSyncPacket;
import com.abo47.questsandstuff.network.sync.S2CDisplayCacheSyncPacket;
import com.abo47.questsandstuff.network.sync.S2CEditorMutationPacket;
import com.abo47.questsandstuff.network.sync.S2CFullSyncPacket;
import com.abo47.questsandstuff.network.sync.S2CPinnedSyncPacket;
import com.abo47.questsandstuff.network.sync.S2CQuestEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;

public final class QuestNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "network"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int packetId;
    private static volatile BiConsumer<ServerPlayer, Object> testPacketSink;

    private QuestNetwork() {
    }

    public static void register() {
        packetId = 0;

        CHANNEL.messageBuilder(S2CFullSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CFullSyncPacket::encode)
                .decoder(S2CFullSyncPacket::decode)
                .consumerMainThread(S2CFullSyncPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CDeltaSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CDeltaSyncPacket::encode)
                .decoder(S2CDeltaSyncPacket::decode)
                .consumerMainThread(S2CDeltaSyncPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CPinnedSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CPinnedSyncPacket::encode)
                .decoder(S2CPinnedSyncPacket::decode)
                .consumerMainThread(S2CPinnedSyncPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CDescriptionSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CDescriptionSyncPacket::encode)
                .decoder(S2CDescriptionSyncPacket::decode)
                .consumerMainThread(S2CDescriptionSyncPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CDisplayCacheSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CDisplayCacheSyncPacket::encode)
                .decoder(S2CDisplayCacheSyncPacket::decode)
                .consumerMainThread(S2CDisplayCacheSyncPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CQuestEventPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CQuestEventPacket::encode)
                .decoder(S2CQuestEventPacket::decode)
                .consumerMainThread(S2CQuestEventPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CEditorMutationPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CEditorMutationPacket::encode)
                .decoder(S2CEditorMutationPacket::decode)
                .consumerMainThread(S2CEditorMutationPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SClaimAllRewardsPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SClaimAllRewardsPacket::encode)
                .decoder(C2SClaimAllRewardsPacket::decode)
                .consumerMainThread(C2SClaimAllRewardsPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SEditorAddQuestPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SEditorAddQuestPacket::encode)
                .decoder(C2SEditorAddQuestPacket::decode)
                .consumerMainThread(C2SEditorAddQuestPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SEditorUpdateQuestPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SEditorUpdateQuestPacket::encode)
                .decoder(C2SEditorUpdateQuestPacket::decode)
                .consumerMainThread(C2SEditorUpdateQuestPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SEditorRemoveQuestPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SEditorRemoveQuestPacket::encode)
                .decoder(C2SEditorRemoveQuestPacket::decode)
                .consumerMainThread(C2SEditorRemoveQuestPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SEditorGroupPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SEditorGroupPacket::encode)
                .decoder(C2SEditorGroupPacket::decode)
                .consumerMainThread(C2SEditorGroupPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SEditorOpenGroupPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SEditorOpenGroupPacket::encode)
                .decoder(C2SEditorOpenGroupPacket::decode)
                .consumerMainThread(C2SEditorOpenGroupPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SEditorOpenQuestPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SEditorOpenQuestPacket::encode)
                .decoder(C2SEditorOpenQuestPacket::decode)
                .consumerMainThread(C2SEditorOpenQuestPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SEditorControlPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SEditorControlPacket::encode)
                .decoder(C2SEditorControlPacket::decode)
                .consumerMainThread(C2SEditorControlPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SEditorCommandPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SEditorCommandPacket::encode)
                .decoder(C2SEditorCommandPacket::decode)
                .consumerMainThread(C2SEditorCommandPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SClaimRewardPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SClaimRewardPacket::encode)
                .decoder(C2SClaimRewardPacket::decode)
                .consumerMainThread(C2SClaimRewardPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SClaimSelectableRewardPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SClaimSelectableRewardPacket::encode)
                .decoder(C2SClaimSelectableRewardPacket::decode)
                .consumerMainThread(C2SClaimSelectableRewardPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SManualTaskPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SManualTaskPacket::encode)
                .decoder(C2SManualTaskPacket::decode)
                .consumerMainThread(C2SManualTaskPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SManualItemSubmitPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SManualItemSubmitPacket::encode)
                .decoder(C2SManualItemSubmitPacket::decode)
                .consumerMainThread(C2SManualItemSubmitPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SManualXpSubmitPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SManualXpSubmitPacket::encode)
                .decoder(C2SManualXpSubmitPacket::decode)
                .consumerMainThread(C2SManualXpSubmitPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SResetQuestPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SResetQuestPacket::encode)
                .decoder(C2SResetQuestPacket::decode)
                .consumerMainThread(C2SResetQuestPacket::handle)
                .add();
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        BiConsumer<ServerPlayer, Object> sink = testPacketSink;
        if (sink != null) {
            sink.accept(player, packet);
        }
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

    public static void setTestPacketSink(BiConsumer<ServerPlayer, Object> sink) {
        testPacketSink = sink;
    }

    public static void clearTestPacketSink() {
        testPacketSink = null;
    }
}
