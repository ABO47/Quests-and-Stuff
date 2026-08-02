package com.abo47.questsandstuff.client.tablet.chunkclaimer;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.network.chunkclaim.C2SChunkClaimActionPacket;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChunkMapClickActionsTest {
    private static final List<C2SChunkClaimActionPacket.Action> EMPTY = List.of();

    @Test
    void rightClickUnclaimsOrTogglesForceOff() {
        assertEquals(List.of(C2SChunkClaimActionPacket.Action.TOGGLE_FORCE),
                ChunkMapWidget.actionsFor(true, true, false, false, 1, false),
                "Right click on force loaded chunk toggles force off");
        assertEquals(List.of(C2SChunkClaimActionPacket.Action.UNCLAIM),
                ChunkMapWidget.actionsFor(true, false, false, false, 1, false),
                "Right click on claimed chunk unclaims");
        assertEquals(EMPTY,
                ChunkMapWidget.actionsFor(false, false, false, false, 1, false),
                "Right click on free chunk does nothing");
    }

    @Test
    void shiftRightClickUnforcesAndUnclaims() {
        assertEquals(List.of(C2SChunkClaimActionPacket.Action.TOGGLE_FORCE, C2SChunkClaimActionPacket.Action.UNCLAIM),
                ChunkMapWidget.actionsFor(true, true, false, false, 1, true),
                "Shift right click on forced chunk unforces and unclaims");
        assertEquals(List.of(C2SChunkClaimActionPacket.Action.UNCLAIM),
                ChunkMapWidget.actionsFor(true, false, false, false, 1, true),
                "Shift right click on claimed chunk unclaims");
        assertEquals(EMPTY,
                ChunkMapWidget.actionsFor(false, false, false, false, 1, true),
                "Shift right click on free chunk does nothing");
    }

    @Test
    void shiftClickClaimsFreeAndForcesClaimed() {
        assertEquals(List.of(C2SChunkClaimActionPacket.Action.CLAIM),
                ChunkMapWidget.actionsFor(false, false, false, false, 0, true),
                "Shift click on free chunk claims");
        assertEquals(List.of(C2SChunkClaimActionPacket.Action.TOGGLE_FORCE),
                ChunkMapWidget.actionsFor(true, false, false, false, 0, true),
                "Shift click on claimed chunk toggles force");
        assertEquals(EMPTY,
                ChunkMapWidget.actionsFor(true, true, false, false, 0, true),
                "Shift click on force loaded chunk does nothing");
    }

    @Test
    void claimArmedClaimsFreeChunksOnly() {
        assertEquals(List.of(C2SChunkClaimActionPacket.Action.CLAIM),
                ChunkMapWidget.actionsFor(false, false, true, false, 0, false),
                "Claim armed on free chunk claims");
        assertEquals(EMPTY,
                ChunkMapWidget.actionsFor(true, false, true, false, 0, false),
                "Claim armed on claimed chunk does nothing");
        assertEquals(EMPTY,
                ChunkMapWidget.actionsFor(false, false, false, false, 0, false),
                "Nothing armed does nothing");
    }

    @Test
    void forceArmedForcesOwnClaimedChunks() {
        assertEquals(List.of(C2SChunkClaimActionPacket.Action.TOGGLE_FORCE),
                ChunkMapWidget.actionsFor(true, false, false, true, 0, false),
                "Force armed on claimed chunk toggles force");
        assertEquals(EMPTY,
                ChunkMapWidget.actionsFor(false, false, false, true, 0, false),
                "Force armed on free chunk does nothing");
        assertEquals(EMPTY,
                ChunkMapWidget.actionsFor(true, true, false, true, 0, false),
                "Force armed on force loaded chunk does nothing");
    }

    @Test
    void claimAndForceArmedClaimsThenForces() {
        assertEquals(List.of(C2SChunkClaimActionPacket.Action.CLAIM, C2SChunkClaimActionPacket.Action.TOGGLE_FORCE),
                ChunkMapWidget.actionsFor(false, false, true, true, 0, false),
                "Both armed on free chunk claims and forces");
        assertEquals(List.of(C2SChunkClaimActionPacket.Action.CLAIM, C2SChunkClaimActionPacket.Action.TOGGLE_FORCE),
                ChunkMapWidget.actionsFor(true, false, true, true, 0, false),
                "Both armed on claimed chunk claims again and forces");
        assertEquals(List.of(C2SChunkClaimActionPacket.Action.CLAIM),
                ChunkMapWidget.actionsFor(true, true, true, true, 0, false),
                "Both armed on force loaded chunk still claims");
    }
}
