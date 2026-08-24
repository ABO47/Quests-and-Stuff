package com.abo47.questsandstuff.client.tablet.chunkclaimer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import com.abo47.questsandstuff.chunkclaim.ChunkClaimPacketHelper;
import com.abo47.questsandstuff.chunkclaim.model.ClaimedChunk;
import com.abo47.questsandstuff.client.tablet.teams.ClientTeamCache;

public enum ClientChunkClaimCache {
    INSTANCE;

    private volatile Map<String, ChunkClaimPacketHelper.ClaimEntry> claims = Map.of();
    private volatile int revision;

    public void setAll(List<ChunkClaimPacketHelper.ClaimEntry> entries) {
        Map<String, ChunkClaimPacketHelper.ClaimEntry> map = new HashMap<>();
        for (ChunkClaimPacketHelper.ClaimEntry e : entries) {
            map.put(key(e.dim(), e.x(), e.z()), e);
        }
        this.claims = map;
        this.revision++;
    }

    public void clear() {
        this.claims = Map.of();
        this.revision++;
    }

    public int revision() {
        return revision;
    }

    private static String key(ResourceLocation dim, int x, int z) {
        return dim + "|" + x + "," + z;
    }

    private ChunkClaimPacketHelper.ClaimEntry find(ResourceLocation dim, int x, int z) {
        return claims.get(key(dim, x, z));
    }

    public boolean isClaimed(ResourceLocation dim, int x, int z) {
        return find(dim, x, z) != null;
    }

    public boolean isForceLoaded(ResourceLocation dim, int x, int z) {
        ChunkClaimPacketHelper.ClaimEntry e = find(dim, x, z);
        return e != null && e.forceLoaded();
    }

    public String ownerName(ResourceLocation dim, int x, int z) {
        ChunkClaimPacketHelper.ClaimEntry e = find(dim, x, z);
        return e == null ? "" : e.claimedByName();
    }

    public UUID teamIdOf(ResourceLocation dim, int x, int z) {
        ChunkClaimPacketHelper.ClaimEntry e = find(dim, x, z);
        return e == null ? null : e.teamId();
    }

    public List<ChunkClaimPacketHelper.ClaimEntry> entries() {
        return List.copyOf(claims.values());
    }

    public List<ClaimedChunk> snapshot() {
        UUID localTeam = ClientTeamCache.INSTANCE.getTeam() != null
                ? ClientTeamCache.INSTANCE.getTeam().teamId() : null;
        String localName = Minecraft.getInstance().getUser().getName();
        List<ClaimedChunk> result = new ArrayList<>();
        for (ChunkClaimPacketHelper.ClaimEntry e : claims.values()) {
            if (localTeam != null && localTeam.equals(e.teamId())) {
                result.add(new ClaimedChunk(e.dim(), e.x(), e.z(), e.forceLoaded(), e.claimedByName()));
            } else if (localTeam == null && e.claimedByName().equals(localName)) {
                result.add(new ClaimedChunk(e.dim(), e.x(), e.z(), e.forceLoaded(), e.claimedByName()));
            }
        }
        return result;
    }
}
