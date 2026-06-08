package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.controls.SearchScrollState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModalPickerStatesTest {
    @Test
    void recipeBindingResetsSearchFocusScrollDraggingAndKeepsModeSeparate() {
        TabletUiState state = new TabletUiState();
        state.recipeSearch = "minecraft:stone";
        state.recipeSearchFocused = true;
        state.recipeScroll = 42;
        state.recipeScrollDragging = true;
        state.recipeMode = RecipePickerMode.TAGS;

        ModalPickerStates.recipe(state).reset();

        assertEquals("", state.recipeSearch);
        assertFalse(state.recipeSearchFocused);
        assertEquals(0, state.recipeScroll);
        assertFalse(state.recipeScrollDragging);
        assertEquals(RecipePickerMode.TAGS, state.recipeMode);
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
    void typeLookupBindsEverySimpleResourceListPicker() {
        TabletUiState state = new TabletUiState();

        ModalPickerStates.forType(state, ModalWindowManager.ModalType.BIOME_PICKER).setSearch("plains");
        ModalPickerStates.forType(state, ModalWindowManager.ModalType.ADVANCEMENT_PICKER).setSearch("story");
        ModalPickerStates.forType(state, ModalWindowManager.ModalType.STRUCTURE_PICKER).setSearch("village");
        ModalPickerStates.forType(state, ModalWindowManager.ModalType.STAT_PICKER).setSearch("jump");
        ModalPickerStates.forType(state, ModalWindowManager.ModalType.DIMENSION_PICKER).setSearch("nether");
        ModalPickerStates.forType(state, ModalWindowManager.ModalType.LOOT_TABLE_PICKER).setSearch("chests");

        assertEquals("plains", state.biomeSearch);
        assertEquals("story", state.advancementSearch);
        assertEquals("village", state.structureSearch);
        assertEquals("jump", state.statSearch);
        assertEquals("nether", state.dimensionSearch);
        assertEquals("chests", state.lootTableSearch);
    }

    @Test
    void typeLookupRejectsModalsWithoutPickerState() {
        TabletUiState state = new TabletUiState();

        assertThrows(IllegalArgumentException.class,
                () -> ModalPickerStates.forType(state, ModalWindowManager.ModalType.SETTINGS_PANEL));
    }

    @Test
    void activePickerBindingUpdatesModalSessionPayload() {
        TabletUiState state = new TabletUiState();
        ModalOpenActions.openRecipePicker(state, "task_recipe|quest|task|questsandstuff:recipe");

        SearchScrollState picker = ModalPickerStates.recipe(state);
        picker.setSearch(" Stone\n");
        picker.setFocused(true);
        picker.setScrollValue(18);
        picker.setDragging(true);

        assertEquals(" stone ", state.recipeSearch);
        assertEquals(" stone ", state.modalSession.picker().search());
        assertTrue(state.modalSession.picker().focused());
        assertEquals(18, state.recipeScroll);
        assertEquals(18, state.modalSession.picker().scroll());
        assertTrue(state.modalSession.picker().dragging());
    }

    @Test
    void switchingPickerTypeCreatesFreshSessionPayload() {
        TabletUiState state = new TabletUiState();
        ModalOpenActions.openRecipePicker(state, "task_recipe|quest|task|questsandstuff:recipe");
        ModalPickerStates.recipe(state).setSearch("stone");
        ModalPickerStates.recipe(state).setScrollValue(22);

        ModalOpenActions.openAssetPicker(state, "reward_icon|quest|reward|icon", "icons/new.png");

        assertEquals(ModalWindowManager.ModalType.ASSET_PICKER, state.modalSession.type());
        assertEquals("", state.modalSession.picker().search());
        assertEquals(0, state.modalSession.picker().scroll());
        assertEquals("icons/new.png", state.modalSession.selectedValue());
        assertEquals("", state.modalSession.target(ModalSession.TargetSlot.QUEST_DETAILS_PICK));
        assertEquals("reward_icon|quest|reward|icon", state.modalSession.target(ModalSession.TargetSlot.QUEST_DETAILS_ASSET_PICK));
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

    @Test
    void recipeModeCycleKeepsOneModeActiveAndResetsScroll() {
        TabletUiState state = new TabletUiState();
        state.recipeScroll = 24;

        RecipePickerMode.cycle(state, 1);

        assertEquals(RecipePickerMode.TAGS, state.recipeMode);
        assertEquals(0, state.recipeScroll);
        assertEquals("mode_tags", state.recipeMode.icon());
        assertEquals("tags", state.recipeMode.logName(state.recipeSearch));
        assertEquals(1, RecipePickerMode.cycleIndex(state.recipeMode));
        assertEquals("mode_tags", RecipePickerMode.iconAt(1));

        state.recipeScroll = 18;
        RecipePickerMode.cycle(state, 1);

        assertEquals(RecipePickerMode.FLUIDS, state.recipeMode);
        assertEquals(0, state.recipeScroll);
        assertEquals("mode_fluids", state.recipeMode.icon());
        assertEquals("fluids", state.recipeMode.logName(state.recipeSearch));
        assertEquals(4, RecipePickerMode.cycleSize());

        state.recipeScroll = 9;
        RecipePickerMode.cycle(state, -1);

        assertEquals(RecipePickerMode.TAGS, state.recipeMode);
        assertEquals(0, state.recipeScroll);
    }

    @Test
    void hashRecipeSearchUsesTagModeNameWithoutChangingModeFlags() {
        TabletUiState state = new TabletUiState();
        state.recipeSearch = " #forge:ingots ";

        assertEquals("tags", state.recipeMode.logName(state.recipeSearch));
        assertEquals("mode_items", state.recipeMode.icon());
        assertEquals(RecipePickerMode.ITEMS, state.recipeMode);
    }

    @Test
    void iconModeCycleKeepsOneModeActiveAndResetsScroll() {
        TabletUiState state = new TabletUiState();
        state.iconMode = IconPickerMode.ITEMS;
        state.iconScroll = 24;

        IconPickerMode.cycle(state, true, true, false, 1);

        assertEquals(IconPickerMode.TAGS, state.iconMode);
        assertEquals(0, state.iconScroll);
        assertEquals("mode_tags", state.iconMode.icon());
        assertEquals("tags", state.iconMode.logName());

        state.iconScroll = 18;
        IconPickerMode.cycle(state, true, true, false, 1);

        assertEquals(IconPickerMode.FLUIDS, state.iconMode);
        assertEquals(0, state.iconScroll);
        assertEquals("mode_fluids", state.iconMode.icon());

        IconPickerMode.cycle(state, true, true, false, 1);

        assertEquals(IconPickerMode.ENTITIES, state.iconMode);
        assertEquals("entity", state.iconMode.icon());

        IconPickerMode.cycle(state, true, true, false, 1);

        assertEquals(IconPickerMode.INVENTORY, state.iconMode);
        assertEquals("mode_inventory", state.iconMode.icon());
        IconPickerMode[] cycle = IconPickerMode.cycleForContext(true, true, false);
        assertEquals(4, IconPickerMode.cycleIndex(state.iconMode, cycle));
        assertEquals("mode_inventory", IconPickerMode.iconAt(cycle, 4));

        IconPickerMode.cycle(state, true, true, false, -1);

        assertEquals(IconPickerMode.ENTITIES, state.iconMode);
    }

    @Test
    void useItemIconModeCyclesUsableItemsBeforeAllItems() {
        TabletUiState state = new TabletUiState();
        state.iconMode = IconPickerMode.USABLE_ITEMS;

        IconPickerMode.cycle(state, false, true, true, 1);

        assertEquals(IconPickerMode.ITEMS, state.iconMode);
        assertEquals("items", state.iconMode.logName());

        IconPickerMode.cycle(state, false, true, true, -1);

        assertEquals(IconPickerMode.USABLE_ITEMS, state.iconMode);
        assertEquals("usable_items", state.iconMode.logName());
        assertEquals("send-horizontal", state.iconMode.icon());
    }

    @Test
    void modelItemIconModeOnlyCyclesItemsAndTags() {
        TabletUiState state = new TabletUiState();
        state.iconMode = IconPickerMode.ENTITIES;

        IconPickerMode.cycleModelItems(state, 1);

        assertEquals(IconPickerMode.TAGS, state.iconMode);
        assertEquals(1, IconPickerMode.cycleIndex(state.iconMode, IconPickerMode.modelItemCycle()));
        assertEquals("mode_tags", IconPickerMode.iconAt(IconPickerMode.modelItemCycle(), 1));

        IconPickerMode.cycleModelItems(state, 1);

        assertEquals(IconPickerMode.ITEMS, state.iconMode);
    }
}
