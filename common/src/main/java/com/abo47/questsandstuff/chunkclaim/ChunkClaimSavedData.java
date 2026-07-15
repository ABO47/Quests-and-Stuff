package com.abo47.questsandstuff.chunkclaim;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.chunkclaim.model.ClaimedChunk;
import com.abo47.questsandstuff.chunkclaim.model.TeamChunkData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChunkClaimSavedData extends SavedData {
    private static final String DATA_NAME = QuestsAndStuffMod.MODID + "_chunk_claims";
    private static final String TAG_CLAIMS = "claims";
    private static final String TAG_TEAM = "team";
    private static final String TAG_DIM = "dim";
    private static final String TAG_X = "x";
    private static final String TAG_Z = "z";
    private static final String TAG_FORCE = "force";

    private final Map<UUID, List<ClaimedChunk>> byTeam = new HashMap<>();

    public ChunkClaimSavedData() {
    }

    public static ChunkClaimSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        if (overworld == null) {
            throw new IllegalStateException("Cannot access chunk claims before the overworld is available");
        }
        return overworld.getDataStorage().computeIfAbsent(
                ChunkClaimSavedData::load,
                ChunkClaimSavedData::new,
                DATA_NAME
        );
    }

    public static ChunkClaimSavedData load(CompoundTag tag) {
        ChunkClaimSavedData data = new ChunkClaimSavedData();
        ListTag claims = tag.getList(TAG_CLAIMS, Tag.TAG_COMPOUND);
        for (int i = 0; i < claims.size(); i++) {
            CompoundTag entry = claims.getCompound(i);
            UUID team = entry.getUUID(TAG_TEAM);
            ResourceLocation dim = ResourceLocation.tryParse(entry.getString(TAG_DIM));
            if (dim == null) {
                continue;
            }
            int x = entry.getInt(TAG_X);
            int z = entry.getInt(TAG_Z);
            boolean force = entry.getBoolean(TAG_FORCE);
            data.byTeam.computeIfAbsent(team, k -> new ArrayList<>()).add(new ClaimedChunk(dim, x, z, force));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        ListTag claims = new ListTag();
        for (Map.Entry<UUID, List<ClaimedChunk>> entry : byTeam.entrySet()) {
            for (ClaimedChunk chunk : entry.getValue()) {
                CompoundTag c = new CompoundTag();
                c.putUUID(TAG_TEAM, entry.getKey());
                c.putString(TAG_DIM, chunk.dimension().toString());
                c.putInt(TAG_X, chunk.x());
                c.putInt(TAG_Z, chunk.z());
                c.putBoolean(TAG_FORCE, chunk.forceLoaded());
                claims.add(c);
            }
        }
        tag.put(TAG_CLAIMS, claims);
        return tag;
    }

    public synchronized boolean claim(UUID teamId, ResourceLocation dim, int x, int z) {
        List<ClaimedChunk> chunks = byTeam.computeIfAbsent(teamId, k -> new ArrayList<>());
        for (ClaimedChunk chunk : chunks) {
            if (sameChunk(chunk, dim, x, z)) {
                return false;
            }
        }
        chunks.add(new ClaimedChunk(dim, x, z, false));
        setDirty();
        return true;
    }

    public synchronized boolean unclaim(UUID teamId, ResourceLocation dim, int x, int z) {
        List<ClaimedChunk> chunks = byTeam.get(teamId);
        if (chunks == null) {
            return false;
        }
        boolean removed = chunks.removeIf(chunk -> sameChunk(chunk, dim, x, z));
        if (removed) {
            if (chunks.isEmpty()) {
                byTeam.remove(teamId);
            }
            setDirty();
        }
        return removed;
    }

    public synchronized boolean setForce(UUID teamId, ResourceLocation dim, int x, int z, boolean force) {
        List<ClaimedChunk> chunks = byTeam.get(teamId);
        if (chunks == null) {
            return false;
        }
        boolean found = false;
        for (int i = 0; i < chunks.size(); i++) {
            ClaimedChunk chunk = chunks.get(i);
            if (sameChunk(chunk, dim, x, z)) {
                chunks.set(i, new ClaimedChunk(dim, x, z, force));
                found = true;
                break;
            }
        }
        if (found) {
            setDirty();
        }
        return found;
    }

    public synchronized boolean isClaimed(UUID teamId, ResourceLocation dim, int x, int z) {
        List<ClaimedChunk> chunks = byTeam.get(teamId);
        if (chunks == null) {
            return false;
        }
        for (ClaimedChunk chunk : chunks) {
            if (sameChunk(chunk, dim, x, z)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isForceLoaded(UUID teamId, ResourceLocation dim, int x, int z) {
        List<ClaimedChunk> chunks = byTeam.get(teamId);
        if (chunks == null) {
            return false;
        }
        for (ClaimedChunk chunk : chunks) {
            if (sameChunk(chunk, dim, x, z)) {
                return chunk.forceLoaded();
            }
        }
        return false;
    }

    public synchronized int countClaimed(UUID teamId) {
        List<ClaimedChunk> chunks = byTeam.get(teamId);
        return chunks == null ? 0 : chunks.size();
    }

    public synchronized int countForceLoaded(UUID teamId) {
        List<ClaimedChunk> chunks = byTeam.get(teamId);
        if (chunks == null) {
            return 0;
        }
        int count = 0;
        for (ClaimedChunk chunk : chunks) {
            if (chunk.forceLoaded()) {
                count++;
            }
        }
        return count;
    }

    public synchronized TeamChunkData claims(UUID teamId) {
        List<ClaimedChunk> chunks = byTeam.get(teamId);
        if (chunks == null) {
            return new TeamChunkData(List.of());
        }
        return new TeamChunkData(new ArrayList<>(chunks));
    }

    public synchronized UUID ownerTeamIdOf(ResourceLocation dim, int x, int z) {
        for (Map.Entry<UUID, List<ClaimedChunk>> entry : byTeam.entrySet()) {
            for (ClaimedChunk chunk : entry.getValue()) {
                if (sameChunk(chunk, dim, x, z)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public synchronized void forEachForceChunk(java.util.function.BiConsumer<UUID, ClaimedChunk> consumer) {
        for (Map.Entry<UUID, List<ClaimedChunk>> entry : byTeam.entrySet()) {
            for (ClaimedChunk chunk : entry.getValue()) {
                if (chunk.forceLoaded()) {
                    consumer.accept(entry.getKey(), chunk);
                }
            }
        }
    }

    public synchronized void forEachClaimed(java.util.function.BiConsumer<UUID, ClaimedChunk> consumer) {
        for (Map.Entry<UUID, List<ClaimedChunk>> entry : byTeam.entrySet()) {
            for (ClaimedChunk chunk : entry.getValue()) {
                consumer.accept(entry.getKey(), chunk);
            }
        }
    }

    public synchronized boolean removeTeam(UUID teamId) {
        List<ClaimedChunk> removed = byTeam.remove(teamId);
        if (removed != null) {
            setDirty();
            return true;
        }
        return false;
    }

    private static boolean sameChunk(ClaimedChunk chunk, ResourceLocation dim, int x, int z) {
        return chunk.x() == x && chunk.z() == z && chunk.dimension().equals(dim);
    }
}
