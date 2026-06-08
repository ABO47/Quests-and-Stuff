package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
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
        assertEquals("reward_icon|quest|reward|icon", state.modalSession.target(ModalSession.TargetSlot.QUEST_DETAILS_ASSET_PICK));
        assertEquals("icons/new.png", state.assetSelected);
        assertEquals("icons/new.png", state.modalSession.selectedValue());
        assertEquals("", state.assetSearch);
        assertEquals("", state.modalSession.picker().search());
        assertFalse(state.assetSearchFocused);
        assertEquals(0, state.assetGridScroll);
        assertEquals(0, state.modalSession.picker().scroll());
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
        assertEquals("task_recipe|quest|task|questsandstuff:recipe", state.modalSession.target(ModalSession.TargetSlot.QUEST_DETAILS_PICK));
        assertEquals(12, state.canvasImageLogicalX);
        assertEquals(34, state.canvasImageLogicalY);
        assertEquals("", state.recipeSearch);
        assertEquals("", state.modalSession.picker().search());
        assertFalse(state.recipeSearchFocused);
        assertEquals(0, state.recipeScroll);
        assertEquals(0, state.modalSession.picker().scroll());
        assertFalse(state.recipeScrollDragging);
        assertEquals(RecipePickerMode.ITEMS, state.recipeMode);
        assertEquals(RecipePickerMode.ITEMS.name(), state.modalSession.mode());
    }

    @Test
    void questDetailsIconPickerUsesUsableItemsModeForItemUseTargets() {
        TabletUiState state = new TabletUiState();
        state.iconSearch = "old";
        state.iconSearchFocused = true;
        state.iconScroll = 12;
        state.iconScrollDragging = true;
        state.iconMode = IconPickerMode.INVENTORY;

        ModalOpenActions.openQuestDetailsIconPicker(state, ModalTargets.taskSimpleIcon("quest", "task", "questsandstuff:item_use"));

        assertTrue(state.iconPickerOpen);
        assertEquals(ModalWindowManager.ModalType.ICON_PICKER, state.modalSession.type());
        assertEquals(ModalTargets.taskSimpleIcon("quest", "task", "questsandstuff:item_use"), state.questDetailsPickTarget);
        assertEquals(ModalTargets.taskSimpleIcon("quest", "task", "questsandstuff:item_use"), state.modalSession.target(ModalSession.TargetSlot.QUEST_DETAILS_PICK));
        assertEquals("", state.iconSearch);
        assertFalse(state.iconSearchFocused);
        assertEquals(0, state.iconScroll);
        assertFalse(state.iconScrollDragging);
        assertEquals(IconPickerMode.USABLE_ITEMS, state.iconMode);
        assertEquals(IconPickerMode.USABLE_ITEMS.name(), state.modalSession.mode());
    }

    @Test
    void canvasEntityIconPickerStartsInEntityMode() {
        TabletUiState state = new TabletUiState();
        state.iconMode = IconPickerMode.FLUIDS;

        ModalOpenActions.openCanvasEntityPicker(state, ModalTargets.canvasEntityNew("chapter"), 5, 9);

        assertTrue(state.iconPickerOpen);
        assertEquals(ModalWindowManager.ModalType.ICON_PICKER, state.modalSession.type());
        assertEquals(ModalTargets.canvasEntityNew("chapter"), state.modalCanvasEntityTarget);
        assertEquals(ModalTargets.canvasEntityNew("chapter"), state.modalSession.target(ModalSession.TargetSlot.CANVAS_ENTITY));
        assertEquals(5, state.canvasImageLogicalX);
        assertEquals(9, state.canvasImageLogicalY);
        assertEquals(IconPickerMode.ENTITIES, state.iconMode);
    }

    @Test
    void openingModalClearsActiveTransformSessions() {
        TabletUiState state = new TabletUiState();
        state.draggingCanvasImage = true;
        state.transientCanvasImages.put("image:a", image("image:a"));
        state.transientQuestPositions.put("quest/a", new CanvasPoint(10, 20));
        state.questDetailsTransformKind = "desc_image";
        state.questDetailsTransformId = "image:b";
        state.questDetailsTransientImages.put("image:b", image("image:b"));
        state.snapGuideXVisible = true;
        state.snapGuideYVisible = true;

        ModalOpenActions.openColorPicker(state, "theme", 0xFF00AA);

        assertTrue(state.colorPickerOpen);
        assertFalse(state.draggingCanvasImage);
        assertTrue(state.transientCanvasImages.isEmpty());
        assertTrue(state.transientQuestPositions.isEmpty());
        assertTrue(state.questDetailsTransformKind.isBlank());
        assertTrue(state.questDetailsTransformId.isBlank());
        assertTrue(state.questDetailsTransientImages.isEmpty());
        assertFalse(state.snapGuideXVisible);
        assertFalse(state.snapGuideYVisible);
    }

    private static CanvasImageLayer image(String id) {
        return new CanvasImageLayer(id, "item:minecraft:diamond", 10, 20, 40, 50, 0);
    }
}
