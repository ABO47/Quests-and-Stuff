package com.abo47.questsandstuff.client.tablet.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkinStateCapsuleTest {
    @Test
    void skinEditModeDefaultsToFalse() {
        TabletUiState state = new TabletUiState();
        assertFalse(state.root.skinEditMode);
    }

    @Test
    void skinEditSelectedTargetDefaultsToEmpty() {
        TabletUiState state = new TabletUiState();
        assertEquals("", state.root.skinEditSelectedTarget);
    }

    @Test
    void skinFillOverridesDefaultsToEmptyMap() {
        TabletUiState state = new TabletUiState();
        assertNotNull(state.root.skinFillOverrides);
        assertTrue(state.root.skinFillOverrides.isEmpty());
    }

    @Test
    void skinEditModeCanBeToggled() {
        TabletUiState state = new TabletUiState();
        state.root.skinEditMode = true;
        assertTrue(state.root.skinEditMode);
        state.root.skinEditMode = false;
        assertFalse(state.root.skinEditMode);
    }

    @Test
    void skinEditSelectedTargetCanBeSet() {
        TabletUiState state = new TabletUiState();
        state.root.skinEditSelectedTarget = "quests_canvas";
        assertEquals("quests_canvas", state.root.skinEditSelectedTarget);
        state.root.skinEditSelectedTarget = "";
        assertEquals("", state.root.skinEditSelectedTarget);
    }

    @Test
    void skinFillOverridesStoresAndClears() {
        TabletUiState state = new TabletUiState();
        state.root.skinFillOverrides.put("home_inner", "stretch|path/to/img.png");
        assertEquals(1, state.root.skinFillOverrides.size());
        assertEquals("stretch|path/to/img.png", state.root.skinFillOverrides.get("home_inner"));

        state.root.skinFillOverrides.put("quests:quests_canvas", "tile|tile.png");
        assertEquals(2, state.root.skinFillOverrides.size());

        state.root.skinFillOverrides.clear();
        assertTrue(state.root.skinFillOverrides.isEmpty());
    }

    @Test
    void skinFillOverridesAcceptsAppPrefixedKeys() {
        TabletUiState state = new TabletUiState();
        state.root.skinFillOverrides.put("quests:home_inner", "stretch|a.png");
        state.root.skinFillOverrides.put("teams:home_inner", "stretch|b.png");
        assertEquals(2, state.root.skinFillOverrides.size());
        assertEquals("stretch|a.png", state.root.skinFillOverrides.get("quests:home_inner"));
        assertEquals("stretch|b.png", state.root.skinFillOverrides.get("teams:home_inner"));
    }

    @Test
    void skinFieldsAreIndependentOfOtherRootState() {
        TabletUiState state = new TabletUiState();
        state.root.skinEditMode = true;
        state.root.skinEditSelectedTarget = "quest_details_modal";
        state.root.skinFillOverrides.put("quest_details_modal", "tile|bg.png");

        state.root.editMode = true;
        state.root.selectedChapter = "main";
        state.root.currentApp = "quests";

        assertTrue(state.root.skinEditMode);
        assertEquals("quest_details_modal", state.root.skinEditSelectedTarget);
        assertEquals("tile|bg.png", state.root.skinFillOverrides.get("quest_details_modal"));

        assertTrue(state.root.editMode);
        assertEquals("main", state.root.selectedChapter);
        assertEquals("quests", state.root.currentApp);
    }
}
