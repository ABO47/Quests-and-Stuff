package com.abo47.questsandstuff.client.tablet.chunkclaimer;

import com.abo47.questsandstuff.chunkclaim.model.ClaimedChunk;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;

public enum ClientChunkClaimCache {
    INSTANCE;

    private volatile UUID teamId;
    private volatile List<ClaimedChunk> claims = List.of();

    public void set(UUID teamId, List<ClaimedChunk> claims) {
        this.teamId = teamId;
        this.claims = List.copyOf(claims);
    }

    public void clear() {
        this.teamId = null;
        this.claims = List.of();
    }

    public UUID teamId() {
        return teamId;
    }

    public List<ClaimedChunk> snapshot() {
        return claims;
    }

    public boolean isClaimed(ResourceLocation dim, int x, int z) {
        return find(dim, x, z) != null;
    }

    public boolean isForceLoaded(ResourceLocation dim, int x, int z) {
        ClaimedChunk chunk = find(dim, x, z);
        return chunk != null && chunk.forceLoaded();
    }

    private ClaimedChunk find(ResourceLocation dim, int x, int z) {
        for (ClaimedChunk chunk : claims) {
            if (chunk.x() == x && chunk.z() == z && chunk.dimension().equals(dim)) {
                return chunk;
            }
        }
        return null;
    }
}
