package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipePickerModeControllerTest {
    @Test
    void searchChangesNormalizeInputAndResetScroll() {
        TabletUiState state = new TabletUiState();
        state.pickers.recipeScroll = 42;

        String rawQuery = "  Stone  Brick  ";
        String query = RecipePickerModeController.setSearch(state, rawQuery);

        assertEquals(SearchFilter.normalizeUserInput(rawQuery), query);
        assertEquals(SearchFilter.normalizeUserInput(rawQuery), state.pickers.recipeSearch);
        assertEquals(0, state.pickers.recipeScroll);
    }

    @Test
    void cycleRoutesThroughControllerAndResetsScroll() {
        TabletUiState state = new TabletUiState();
        state.pickers.recipeMode = RecipePickerMode.ITEMS;
        state.pickers.recipeScroll = 17;

        RecipePickerMode mode = RecipePickerModeController.cycle(state, 1);

        assertEquals(RecipePickerMode.TAGS, mode);
        assertEquals(RecipePickerMode.TAGS, state.pickers.recipeMode);
        assertEquals(0, state.pickers.recipeScroll);
        assertEquals(1, RecipePickerModeController.cycleIndex(state));
    }

    @Test
    void contentKindsSeparateRecipeFluidAndInventorySurfaces() {
        assertEquals(RecipePickerModeController.ContentKind.RECIPES, RecipePickerModeController.contentKind(RecipePickerMode.ITEMS));
        assertEquals(RecipePickerModeController.ContentKind.RECIPES, RecipePickerModeController.contentKind(RecipePickerMode.TAGS));
        assertEquals(RecipePickerModeController.ContentKind.FLUIDS, RecipePickerModeController.contentKind(RecipePickerMode.FLUIDS));
        assertEquals(RecipePickerModeController.ContentKind.INVENTORY, RecipePickerModeController.contentKind(RecipePickerMode.INVENTORY));
    }
}
