package com.abo47.questsandstuff.chunkclaim;

import com.abo47.questsandstuff.chunkclaim.model.ClaimedChunk;
import com.abo47.questsandstuff.chunkclaim.model.TeamChunkData;
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
    private static final String TAG_TEAM = "team";
    private static final String TAG_CHUNKS = "chunks";
    private static final String TAG_DIM = "dim";
    private static final String TAG_X = "x";
    private static final String TAG_Z = "z";
    private static final String TAG_FORCE = "force";

    private ChunkClaimPacketHelper() {
    }

    public record ChunkClaimSnapshot(UUID teamId, List<ClaimedChunk> chunks) {
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
        ChunkClaimService service = QuestServiceRegistry.chunkClaims(player.server);
        UUID teamId = team.teamId();
        switch (action) {
            case CLAIM -> service.claim(teamId, dim, x, z);
            case UNCLAIM -> service.unclaim(teamId, dim, x, z);
            case TOGGLE_FORCE -> service.setForceLoaded(teamId, dim, x, z, !service.isForceLoaded(teamId, dim, x, z));
            case REQUEST -> {
            }
        }
        if (action == C2SChunkClaimActionPacket.Action.REQUEST) {
            send(player, encode(teamId, service.claims(teamId)));
        } else {
            broadcastToTeam(player.serverLevel(), team);
        }
    }

    static void broadcastToTeam(ServerLevel level, TeamData team) {
        ChunkClaimService service = QuestServiceRegistry.chunkClaims(level.getServer());
        CompoundTag payload = encode(team.teamId(), service.claims(team.teamId()));
        for (TeamMember member : team.members()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(member.uuid());
            if (player != null) {
                send(player, payload);
            }
        }
    }

    static void send(ServerPlayer player, CompoundTag payload) {
        ModNetwork.sendToPlayer(new S2CChunkClaimSyncPacket(payload), player);
    }

    public static CompoundTag encode(UUID teamId, TeamChunkData data) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(TAG_TEAM, teamId);
        ListTag chunks = new ListTag();
        for (ClaimedChunk chunk : data.chunks()) {
            CompoundTag entry = new CompoundTag();
            entry.putString(TAG_DIM, chunk.dimension().toString());
            entry.putInt(TAG_X, chunk.x());
            entry.putInt(TAG_Z, chunk.z());
            entry.putBoolean(TAG_FORCE, chunk.forceLoaded());
            chunks.add(entry);
        }
        tag.put(TAG_CHUNKS, chunks);
        return tag;
    }

    public static ChunkClaimSnapshot fromPayload(CompoundTag tag) {
        if (tag == null) {
            return new ChunkClaimSnapshot(null, List.of());
        }
        UUID teamId = tag.hasUUID(TAG_TEAM) ? tag.getUUID(TAG_TEAM) : null;
        ListTag chunks = tag.getList(TAG_CHUNKS, Tag.TAG_COMPOUND);
        List<ClaimedChunk> list = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            CompoundTag entry = chunks.getCompound(i);
            ResourceLocation dim = ResourceLocation.tryParse(entry.getString(TAG_DIM));
            if (dim == null) {
                continue;
            }
            list.add(new ClaimedChunk(dim, entry.getInt(TAG_X), entry.getInt(TAG_Z), entry.getBoolean(TAG_FORCE)));
        }
        return new ChunkClaimSnapshot(teamId, list);
    }
}
