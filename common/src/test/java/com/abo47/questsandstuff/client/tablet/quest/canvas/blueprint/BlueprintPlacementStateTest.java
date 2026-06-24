package com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlueprintPlacementStateTest {
    @Test
    void beginActivatesOnlyWhenAnAssetExists() {
        BlueprintPlacementState state = new BlueprintPlacementState();

        state.begin("  blueprints/spawn.json  ");

        assertTrue(state.active());
        assertTrue(state.hasAsset());
        assertEquals("blueprints/spawn.json", state.asset());

        state.begin("  ");

        assertFalse(state.active());
        assertFalse(state.hasAsset());
        assertEquals("", state.asset());
    }

    @Test
    void cancelAndFinishStopPlacementButKeepTheRememberedAsset() {
        BlueprintPlacementState state = new BlueprintPlacementState();
        state.begin("blueprints/camp.json");

        state.cancel();

        assertFalse(state.active());
        assertEquals("blueprints/camp.json", state.asset());

        state.begin("blueprints/camp.json");
        state.finish();

        assertFalse(state.active());
        assertEquals("blueprints/camp.json", state.asset());
    }

    @Test
    void rememberAssetDoesNotStartPlacement() {
        BlueprintPlacementState state = new BlueprintPlacementState();

        state.rememberAsset(" blueprints/tower.json ");

        assertFalse(state.active());
        assertTrue(state.hasAsset());
        assertEquals("blueprints/tower.json", state.asset());
    }
}
