package com.abo47.questsandstuff.network.team;

import java.util.UUID;
import java.util.function.BiConsumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.abo47.questsandstuff.team.NbtKeys;
import com.abo47.questsandstuff.team.TeamManager;
import com.abo47.questsandstuff.team.model.TeamData;
import com.abo47.questsandstuff.team.model.TeamMember;

public final class TeamPacketHelper {
    private TeamPacketHelper() {
    }

    public static void onServer(ModPacketContext context, BiConsumer<ServerPlayer, TeamManager> action) {
        ServerPlayer player = context.sender();
        if (player == null) {
            return;
        }
        context.enqueueWork(() -> {
            ServerLevel level = player.serverLevel();
            TeamManager manager = new TeamManager(level, QuestServiceRegistry.engine(player.server));
            action.accept(player, manager);
        });
    }

    public static void send(ServerPlayer player, TeamData team) {
        CompoundTag tag = team != null ? team.toNbt() : emptyTag();
        ModNetwork.sendToPlayer(new S2CTeamSyncPacket(tag), player);
    }

    private static CompoundTag emptyTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(NbtKeys.EMPTY, true);
        return tag;
    }

    public static void broadcastToMembers(ServerLevel level, TeamData team) {
        if (team == null) return;
        for (TeamMember m : team.members()) {
            ServerPlayer member = level.getServer().getPlayerList().getPlayer(m.uuid());
            if (member != null) {
                send(member, team);
            }
        }
    }

    public static void clearPlayer(ServerLevel level, UUID playerUuid) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerUuid);
        if (player != null) {
            send(player, null);
        }
    }

    public static TeamData fromPayload(CompoundTag tag) {
        if (tag == null || tag.getBoolean(NbtKeys.EMPTY)) {
            return null;
        }
        return TeamData.fromNbt(tag);
    }
}
