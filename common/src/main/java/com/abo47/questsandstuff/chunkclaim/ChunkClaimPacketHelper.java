package com.abo47.questsandstuff.chunkclaim;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.chunkclaim.C2SChunkClaimActionPacket;
import com.abo47.questsandstuff.network.chunkclaim.C2SChunkClaimBatchPacket;
import com.abo47.questsandstuff.network.chunkclaim.S2CChunkClaimSyncPacket;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.abo47.questsandstuff.team.NbtKeys;
import com.abo47.questsandstuff.team.TeamManager;
import com.abo47.questsandstuff.team.model.TeamData;
import com.abo47.questsandstuff.team.model.TeamMember;

public final class ChunkClaimPacketHelper {
    private ChunkClaimPacketHelper() {
    }

    public record ClaimEntry(UUID teamId, ResourceLocation dim, int x, int z, boolean forceLoaded, String claimedByName) {
    }

    public static void applyAction(ServerPlayer player, TeamManager manager,
                            C2SChunkClaimActionPacket.Action action, ResourceLocation dim, int x, int z) {
        TeamData team = resolveTeam(player, manager);
        if (team == null) {
            return;
        }
        ChunkClaimService service = QuestServiceRegistry.chunkClaims(player.server);
        applyOne(service, team.teamId(), ownerName(team), action, dim, x, z);
        broadcastAll(player.serverLevel());
    }

    public static void applyBatch(ServerPlayer player, TeamManager manager, ResourceLocation dim,
                            List<C2SChunkClaimBatchPacket.Entry> entries) {
        TeamData team = resolveTeam(player, manager);
        if (team == null) {
            return;
        }
        ChunkClaimService service = QuestServiceRegistry.chunkClaims(player.server);
        UUID teamId = team.teamId();
        String claimedBy = ownerName(team);
        for (C2SChunkClaimBatchPacket.Entry entry : entries) {
            applyOne(service, teamId, claimedBy, entry.action(), dim, entry.x(), entry.z());
        }
        broadcastAll(player.serverLevel());
    }

    private static TeamData resolveTeam(ServerPlayer player, TeamManager manager) {
        TeamData team = manager.getTeam(player);
        if (team == null) {
            team = manager.createTeam(player);
        }
        return team;
    }

    private static String ownerName(TeamData team) {
        for (TeamMember m : team.members()) {
            if (m.uuid().equals(team.owner())) {
                return m.name();
            }
        }
        return "";
    }

    private static void applyOne(ChunkClaimService service, UUID teamId, String claimedBy,
                            C2SChunkClaimActionPacket.Action action, ResourceLocation dim, int x, int z) {
        switch (action) {
            case CLAIM -> service.claim(teamId, claimedBy, dim, x, z);
            case UNCLAIM -> service.unclaim(teamId, dim, x, z);
            case TOGGLE_FORCE -> service.setForceLoaded(teamId, dim, x, z, !service.isForceLoaded(teamId, dim, x, z));
            case REQUEST -> {}
        }
    }

    public static void broadcastAll(ServerLevel level) {
        ChunkClaimService service = QuestServiceRegistry.chunkClaims(level.getServer());
        CompoundTag payload = encodeAll(service);
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            ModNetwork.sendToPlayer(new S2CChunkClaimSyncPacket(payload), p);
        }
    }

    public static CompoundTag encodeAll(ChunkClaimService service) {
        CompoundTag tag = new CompoundTag();
        ListTag claims = new ListTag();
        service.forEachClaim((teamId, chunk) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID(NbtKeys.TEAM, teamId);
            entry.putString(NbtKeys.DIM, chunk.dimension().toString());
            entry.putInt(NbtKeys.X, chunk.x());
            entry.putInt(NbtKeys.Z, chunk.z());
            entry.putBoolean(NbtKeys.FORCE, chunk.forceLoaded());
            if (!chunk.claimedByName().isEmpty()) {
                entry.putString(NbtKeys.PLAYER, chunk.claimedByName());
            }
            claims.add(entry);
        });
        tag.put(NbtKeys.CLAIMS, claims);
        return tag;
    }

    public static List<ClaimEntry> decodeClaims(CompoundTag tag) {
        if (tag == null) return List.of();
        ListTag claims = tag.getList(NbtKeys.CLAIMS, Tag.TAG_COMPOUND);
        List<ClaimEntry> list = new ArrayList<>();
        for (int i = 0; i < claims.size(); i++) {
            CompoundTag entry = claims.getCompound(i);
            UUID teamId = entry.getUUID(NbtKeys.TEAM);
            ResourceLocation dim = ResourceLocation.tryParse(entry.getString(NbtKeys.DIM));
            if (dim == null) continue;
            int x = entry.getInt(NbtKeys.X);
            int z = entry.getInt(NbtKeys.Z);
            boolean force = entry.getBoolean(NbtKeys.FORCE);
            String player = entry.contains(NbtKeys.PLAYER) ? entry.getString(NbtKeys.PLAYER) : "";
            list.add(new ClaimEntry(teamId, dim, x, z, force, player));
        }
        return list;
    }
}
