package com.abo47.questsandstuff.chunkclaim;

import com.abo47.questsandstuff.team.NbtKeys;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.chunkclaim.C2SChunkClaimActionPacket;
import com.abo47.questsandstuff.network.chunkclaim.S2CChunkClaimSyncPacket;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.abo47.questsandstuff.team.model.TeamData;
import com.abo47.questsandstuff.team.model.TeamMember;
import com.abo47.questsandstuff.team.TeamManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ChunkClaimPacketHelper {
    private ChunkClaimPacketHelper() {
    }

    public record ClaimEntry(UUID teamId, ResourceLocation dim, int x, int z, boolean forceLoaded, String claimedByName) {
    }

    public static void applyAction(ServerPlayer player, TeamManager manager,
                            C2SChunkClaimActionPacket.Action action, ResourceLocation dim, int x, int z) {
        TeamData team = manager.getTeam(player);
        if (team == null) {
            team = manager.createTeam(player);
        }
        if (team == null) {
            return;
        }
        String claimedBy = "";
        for (TeamMember m : team.members()) {
            if (m.uuid().equals(team.owner())) {
                claimedBy = m.name();
                break;
            }
        }
        ChunkClaimService service = QuestServiceRegistry.chunkClaims(player.server);
        UUID teamId = team.teamId();
        switch (action) {
            case CLAIM -> service.claim(teamId, claimedBy, dim, x, z);
            case UNCLAIM -> service.unclaim(teamId, dim, x, z);
            case TOGGLE_FORCE -> service.setForceLoaded(teamId, dim, x, z, !service.isForceLoaded(teamId, dim, x, z));
            case REQUEST -> {}
        }
        broadcastAll(player.serverLevel());
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
