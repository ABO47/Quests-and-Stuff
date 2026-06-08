package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModalOpenActionsTest {
    @Test
    void assetPickerOpenPreservesSelectedValueAndResetsSearchSession() {
        TabletUiState state = new TabletUiState();
        state.assetSearch = "old query";
        state.assetSearchFocused = true;
        state.assetGridScroll = 32;
        state.assetGridScrollDragging = true;
        state.assetSelected = "old.png";
        state.assetContextOpen = true;
        state.assetRenameOpen = true;

        ModalOpenActions.openAssetPicker(state, "reward_icon|quest|reward|icon", "icons/new.png");

        assertTrue(state.assetPickerOpen);
        assertEquals(ModalWindowManager.ModalType.ASSET_PICKER, state.modalSession.type());
        assertEquals("reward_icon|quest|reward|icon", state.questDetailsAssetPickTarget);
        assertEquals("icons/new.png", state.assetSelected);
        assertEquals("", state.assetSearch);
        assertFalse(state.assetSearchFocused);
        assertEquals(0, state.assetGridScroll);
        assertFalse(state.assetGridScrollDragging);
        assertFalse(state.assetContextOpen);
        assertFalse(state.assetRenameOpen);
    }

    @Test
    void recipePickerOpenAssignsTargetCoordinatesAndResetsModeState() {
        TabletUiState state = new TabletUiState();
        state.recipeSearch = "stone";
        state.recipeSearchFocused = true;
        state.recipeScroll = 44;
        state.recipeScrollDragging = true;
        state.recipeMode = RecipePickerMode.INVENTORY;

        ModalOpenActions.openCanvasRecipePicker(state, "task_recipe|quest|task|questsandstuff:recipe", 12, 34);

        assertTrue(state.recipePickerOpen);
        assertEquals(ModalWindowManager.ModalType.RECIPE_PICKER, state.modalSession.type());
        assertEquals("task_recipe|quest|task|questsandstuff:recipe", state.questDetailsPickTarget);
        assertEquals(12, state.canvasImageLogicalX);
        assertEquals(34, state.canvasImageLogicalY);
        assertEquals("", state.recipeSearch);
        assertFalse(state.recipeSearchFocused);
        assertEquals(0, state.recipeScroll);
        assertFalse(state.recipeScrollDragging);
        assertEquals(RecipePickerMode.ITEMS, state.recipeMode);
    }
}
