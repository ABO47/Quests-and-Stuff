package com.abo47.questsandstuff.chunkclaim;

import com.abo47.questsandstuff.chunkclaim.model.TeamChunkData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChunkClaimSavedDataTest {
    private static final ResourceLocation OVERWORLD = new ResourceLocation("minecraft", "overworld");

    @Test
    void claimTracksOwnershipAndRejectsDuplicates() {
        ChunkClaimSavedData data = new ChunkClaimSavedData();
        UUID team = UUID.randomUUID();

        assertTrue(data.claim(team, OVERWORLD, 3, 4, "test"), "First claim succeeds");
        assertTrue(data.isClaimed(team, OVERWORLD, 3, 4), "Chunk is claimed");
        assertFalse(data.claim(team, OVERWORLD, 3, 4, "test"), "Duplicate claim rejected");
        assertEquals(team, data.ownerTeamIdOf(OVERWORLD, 3, 4), "Owner matches");
        assertEquals(1, data.countClaimed(team));
    }

    @Test
    void forceFlagIsIndependentPerChunk() {
        ChunkClaimSavedData data = new ChunkClaimSavedData();
        UUID team = UUID.randomUUID();

        data.claim(team, OVERWORLD, 1, 1, "test");
        assertFalse(data.isForceLoaded(team, OVERWORLD, 1, 1), "Not force loaded initially");
        assertTrue(data.setForce(team, OVERWORLD, 1, 1, true), "Force arming succeeds");
        assertTrue(data.isForceLoaded(team, OVERWORLD, 1, 1), "Force loaded after arming");
        assertEquals(1, data.countForceLoaded(team));
    }

    @Test
    void unclaimReleasesOwnership() {
        ChunkClaimSavedData data = new ChunkClaimSavedData();
        UUID team = UUID.randomUUID();

        data.claim(team, OVERWORLD, 5, 5, "test");
        assertTrue(data.unclaim(team, OVERWORLD, 5, 5), "Unclaim succeeds");
        assertFalse(data.isClaimed(team, OVERWORLD, 5, 5), "Chunk no longer claimed");
        assertNull(data.ownerTeamIdOf(OVERWORLD, 5, 5), "No owner after unclaim");
    }

    @Test
    void saveAndLoadRoundTripsClaims() {
        ChunkClaimSavedData data = new ChunkClaimSavedData();
        UUID team = UUID.randomUUID();
        data.claim(team, OVERWORLD, 0, 0, "alice");
        data.claim(team, OVERWORLD, 1, 2, "bob");
        data.setForce(team, OVERWORLD, 1, 2, true);

        CompoundTag tag = data.save(new CompoundTag());
        ChunkClaimSavedData restored = ChunkClaimSavedData.load(tag);

        assertEquals(2, restored.countClaimed(team), "Two chunks restored");
        assertTrue(restored.isClaimed(team, OVERWORLD, 0, 0));
        assertTrue(restored.isForceLoaded(team, OVERWORLD, 1, 2), "Force flag restored");
        assertEquals(team, restored.ownerTeamIdOf(OVERWORLD, 0, 0));
    }

    @Test
    void removeTeamClearsAllChunks() {
        ChunkClaimSavedData data = new ChunkClaimSavedData();
        UUID team = UUID.randomUUID();
        data.claim(team, OVERWORLD, 7, 8, "test");

        assertTrue(data.removeTeam(team), "Remove team succeeds");
        assertFalse(data.isClaimed(team, OVERWORLD, 7, 8), "Chunks cleared after remove");
        assertEquals(new TeamChunkData(List.of()), data.claims(team));
    }
}
