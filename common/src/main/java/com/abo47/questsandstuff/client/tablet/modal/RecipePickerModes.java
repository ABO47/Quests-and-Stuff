package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.network.chat.Component;

final class RecipePickerModes {
    private RecipePickerModes() {
    }

    static void cycle(TabletUiState state, int direction) {
        int current = state.recipeInventoryMode ? 3 : state.recipeFluidMode ? 2 : state.recipeTagMode ? 1 : 0;
        int next = Math.floorMod(current + direction, 4);
        state.recipeTagMode = next == 1;
        state.recipeFluidMode = next == 2;
        state.recipeInventoryMode = next == 3;
        state.recipeScroll = 0;
    }

    static String icon(TabletUiState state) {
        if (state.recipeInventoryMode) {
            return "mode_inventory";
        }
        if (state.recipeFluidMode) {
            return "mode_fluids";
        }
        return state.recipeTagMode ? "mode_tags" : "mode_items";
    }

    static String name(TabletUiState state) {
        if (state.recipeInventoryMode) {
            return "inventory";
        }
        if (state.recipeFluidMode) {
            return "fluids";
        }
        return state.recipeTagMode || (state.recipeSearch != null && state.recipeSearch.trim().startsWith("#")) ? "tags" : "items";
    }

    static Component[] tooltip(TabletUiState state) {
        return new Component[]{Component.translatable("ui.questsandstuff.recipe_picker.mode." + name(state))};
    }
}
