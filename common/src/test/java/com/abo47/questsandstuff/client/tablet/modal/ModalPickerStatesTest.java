package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.controls.SearchScrollState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ModalPickerStatesTest {
    @Test
    void recipeBindingResetsSearchFocusScrollDraggingAndKeepsModeFlagsSeparate() {
        TabletUiState state = new TabletUiState();
        state.recipeSearch = "minecraft:stone";
        state.recipeSearchFocused = true;
        state.recipeScroll = 42;
        state.recipeScrollDragging = true;
        state.recipeTagMode = true;

        ModalPickerStates.recipe(state).reset();

        assertEquals("", state.recipeSearch);
        assertFalse(state.recipeSearchFocused);
        assertEquals(0, state.recipeScroll);
        assertFalse(state.recipeScrollDragging);
        assertEquals(true, state.recipeTagMode);
    }

    @Test
    void assetBindingTargetsGridScrollFields() {
        TabletUiState state = new TabletUiState();
        state.assetSearch = "blueprints";
        state.assetSearchFocused = true;
        state.assetGridScroll = 16;
        state.assetGridScrollDragging = true;

        SearchScrollState picker = ModalPickerStates.asset(state);
        picker.clearInteraction();

        assertEquals("blueprints", state.assetSearch);
        assertEquals(16, state.assetGridScroll);
        assertFalse(state.assetSearchFocused);
        assertFalse(state.assetGridScrollDragging);
    }

    @Test
    void prerequisitesManagerBindingNormalizesQuestSearch() {
        TabletUiState state = new TabletUiState();

        SearchScrollState picker = ModalPickerStates.prerequisitesManager(state);
        picker.setSearch(" Quest_A\n");

        assertEquals(" quest_a ", state.prerequisitesManagerSearch);
        assertEquals("quest_a", picker.normalizedSearch());
        assertEquals("questa", picker.normalizedKey());
    }
}
