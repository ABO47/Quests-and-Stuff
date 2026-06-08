package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.network.chat.Component;

final class RecipePickerModeController {
    private RecipePickerModeController() {
    }

    static RecipePickerMode mode(TabletUiState state) {
        return state == null ? RecipePickerMode.ITEMS : RecipePickerMode.safe(state.recipeMode);
    }

    static ContentKind contentKind(TabletUiState state) {
        return contentKind(mode(state));
    }

    static ContentKind contentKind(RecipePickerMode mode) {
        return switch (RecipePickerMode.safe(mode)) {
            case INVENTORY -> ContentKind.INVENTORY;
            case FLUIDS -> ContentKind.FLUIDS;
            case ITEMS, TAGS -> ContentKind.RECIPES;
        };
    }

    static String setSearch(TabletUiState state, String value) {
        if (state == null) {
            return "";
        }
        state.recipeSearch = SearchFilter.normalizeUserInput(value);
        state.recipeScroll = 0;
        return state.recipeSearch;
    }

    static RecipePickerMode cycle(TabletUiState state, int direction) {
        RecipePickerMode.cycle(state, direction);
        return mode(state);
    }

    static int cycleIndex(TabletUiState state) {
        return RecipePickerMode.cycleIndex(mode(state));
    }

    static int cycleSize() {
        return RecipePickerMode.cycleSize();
    }

    static String iconAt(int index) {
        return RecipePickerMode.iconAt(index);
    }

    static Component[] tooltip(TabletUiState state) {
        return mode(state).tooltip(state == null ? "" : state.recipeSearch);
    }

    enum ContentKind {
        RECIPES,
        FLUIDS,
        INVENTORY
    }
}
