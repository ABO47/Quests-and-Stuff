package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.ModalSession;
import com.abo47.questsandstuff.client.tablet.modal.ModalWindowManager;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabletModalStateTest {
    @Test
    void applyingModalTypeCreatesAndReplacesTheActiveSession() {
        TabletUiState state = new TabletUiState();

        TabletModalState.applyModalType(state, ModalWindowManager.ModalType.ICON_PICKER);

        assertEquals(ModalWindowManager.ModalType.ICON_PICKER, state.modalSession.type());
        assertEquals(ModalWindowManager.ModalType.ICON_PICKER, ModalStateQueries.activeType(state));
        assertTrue(state.iconPickerOpen);

        TabletModalState.applyModalType(state, ModalWindowManager.ModalType.SOUND_PICKER);

        assertEquals(ModalWindowManager.ModalType.SOUND_PICKER, state.modalSession.type());
        assertEquals(ModalWindowManager.ModalType.SOUND_PICKER, ModalStateQueries.activeType(state));
        assertFalse(state.iconPickerOpen);
        assertTrue(state.soundPickerOpen);
    }

    @Test
    void openModalReplacesPreviousOpenModalAndCancelsClosingState() {
        TabletUiState state = new TabletUiState();
        state.modalWindowClosing = true;
        state.assetPickerOpen = true;
        state.modalSession = ModalSession.open(ModalWindowManager.ModalType.ASSET_PICKER);

        TabletModalState.openModal(state, ModalWindowManager.ModalType.LOOT_TABLE_PICKER);

        assertFalse(state.modalWindowClosing);
        assertFalse(state.assetPickerOpen);
        assertTrue(state.lootTablePickerOpen);
        assertEquals(ModalWindowManager.ModalType.LOOT_TABLE_PICKER, state.modalSession.type());
        assertEquals(ModalWindowManager.ModalType.LOOT_TABLE_PICKER, ModalStateQueries.activeType(state));
    }

    @Test
    void activeTypeUsesSessionBeforeLegacyFlags() {
        TabletUiState state = new TabletUiState();
        state.iconPickerOpen = true;
        state.modalSession = ModalSession.open(ModalWindowManager.ModalType.BLOCK_PICKER);

        assertEquals(ModalWindowManager.ModalType.BLOCK_PICKER, ModalStateQueries.activeType(state));
        assertTrue(ModalStateQueries.anyOpen(state));
    }

    @Test
    void immediateCloseClearsTheActiveSession() {
        TabletUiState state = new TabletUiState();
        state.modalSession = ModalSession.open(ModalWindowManager.ModalType.RECIPE_PICKER);
        state.recipePickerOpen = true;

        TabletModalState.closeAllModalsImmediately(state);

        assertEquals(ModalWindowManager.ModalType.NONE, state.modalSession.type());
        assertFalse(state.modalSession.active());
        assertEquals(ModalWindowManager.ModalType.NONE, ModalStateQueries.activeType(state));
    }

    @Test
    void immediateCloseClearsModalTypeTargetsAndPickerInteraction() {
        TabletUiState state = dirtyModalState();

        TabletModalState.closeAllModalsImmediately(state);

        assertEquals(ModalWindowManager.ModalType.NONE, state.modalSession.type());
        assertFalse(state.modalSession.active());
        assertEquals(ModalWindowManager.ModalType.NONE, ModalStateQueries.activeType(state));
        assertFalse(ModalStateQueries.anyOpen(state));
        assertEquals("", state.modalQuestTarget);
        assertEquals("", state.modalChapterTarget);
        assertEquals("", state.modalCanvasBackgroundTarget);
        assertEquals("", state.modalCanvasImageTarget);
        assertEquals("", state.modalCanvasEntityTarget);
        assertEquals("", state.modalCanvasModelTarget);
        assertEquals("", state.modalBlueprintTarget);
        assertEquals("", state.questDetailsPickTarget);
        assertEquals("", state.questDetailsAssetPickTarget);
        assertFalse(state.modalQuestBackgroundGrayscale);
        assertFalse(state.modalQuestBackgroundTargets.contains("quest_a"));
        assertFalse(state.modalQuestCompletionHudBackgroundTargets.contains("quest_b"));
        assertFalse(state.assetContextOpen);
        assertFalse(state.assetRenameOpen);
        assertFalse(state.blueprintCodeOpen);
        assertFalse(state.blueprintCodeImportMode);
        assertEquals("", state.blueprintCodeTarget);
        assertEquals("", state.blueprintCodeDraft);
        assertEquals("", state.blueprintCodeMessage);
        assertFalse(state.iconSearchFocused);
        assertFalse(state.iconScrollDragging);
        assertFalse(state.iconTagMode);
        assertFalse(state.iconAllItemsMode);
        assertFalse(state.iconEntityMode);
        assertFalse(state.iconInventoryMode);
        assertFalse(state.iconFluidMode);
        assertFalse(state.recipeSearchFocused);
        assertFalse(state.recipeScrollDragging);
        assertFalse(state.recipeTagMode);
        assertFalse(state.recipeInventoryMode);
        assertFalse(state.recipeFluidMode);
        assertFalse(state.blockSearchFocused);
        assertFalse(state.blockScrollDragging);
        assertFalse(state.blockTagMode);
        assertFalse(state.biomeSearchFocused);
        assertFalse(state.biomeScrollDragging);
        assertFalse(state.advancementSearchFocused);
        assertFalse(state.advancementScrollDragging);
        assertFalse(state.structureSearchFocused);
        assertFalse(state.structureScrollDragging);
        assertFalse(state.statSearchFocused);
        assertFalse(state.statScrollDragging);
        assertFalse(state.dimensionSearchFocused);
        assertFalse(state.dimensionScrollDragging);
        assertFalse(state.lootTableSearchFocused);
        assertFalse(state.lootTableScrollDragging);
        assertFalse(state.itemInventorySearchFocused);
        assertFalse(state.itemInventoryScrollDragging);
        assertFalse(state.soundSearchFocused);
        assertFalse(state.soundScrollDragging);
        assertEquals("", state.soundSelected);
        assertEquals("", state.pickerLastClickKey);
        assertEquals(0L, state.pickerLastClickAtMs);
        assertFalse(state.colorPaletteContextOpen);
        assertEquals(Integer.MIN_VALUE, state.colorPaletteContextValue);
        assertFalse(state.colorPaletteScrollDragging);
        assertFalse(state.themeScrollDragging);
        assertFalse(state.settingsScrollDragging);
        assertEquals("", state.prerequisitesManagerQuestId);
        assertEquals("", state.prerequisitesManagerSearch);
        assertFalse(state.prerequisitesManagerExternalMode);
        assertFalse(state.prerequisitesManagerContextOpen);
        assertEquals("", state.prerequisitesManagerSelectedConnectionKey);
        assertEquals("", state.prerequisitesManagerHoveredConnectionKey);
        assertFalse(state.modalWindowClosing);
        assertFalse(state.modalWindowAnimationHasSource);
        assertEquals(0L, state.modalWindowAnimationStartMs);
    }

    private static TabletUiState dirtyModalState() {
        TabletUiState state = new TabletUiState();
        state.iconPickerOpen = true;
        state.assetPickerOpen = true;
        state.biomePickerOpen = true;
        state.advancementPickerOpen = true;
        state.recipePickerOpen = true;
        state.structurePickerOpen = true;
        state.blockPickerOpen = true;
        state.statPickerOpen = true;
        state.dimensionPickerOpen = true;
        state.lootTablePickerOpen = true;
        state.itemInventoryPickerOpen = true;
        state.soundPickerOpen = true;
        state.colorPickerOpen = true;
        state.themePickerOpen = true;
        state.entityVariantPickerOpen = true;
        state.prerequisitesManagerOpen = true;
        state.settingsPanelOpen = true;
        state.modalWindowClosing = true;
        state.modalWindowAnimationHasSource = true;
        state.modalWindowAnimationStartMs = 123L;
        state.modalQuestTarget = "quest";
        state.modalChapterTarget = "chapter";
        state.modalCanvasBackgroundTarget = "canvas";
        state.modalCanvasImageTarget = "image";
        state.modalCanvasEntityTarget = "entity";
        state.modalCanvasModelTarget = "model";
        state.modalBlueprintTarget = "blueprint";
        state.questDetailsPickTarget = "details";
        state.questDetailsAssetPickTarget = "asset_details";
        state.modalQuestBackgroundTargets.add("quest_a");
        state.modalQuestBackgroundGrayscale = true;
        state.modalQuestCompletionHudBackgroundTargets.add("quest_b");
        state.assetContextOpen = true;
        state.assetRenameOpen = true;
        state.blueprintCodeOpen = true;
        state.blueprintCodeImportMode = true;
        state.blueprintCodeTarget = "target";
        state.blueprintCodeDraft = "draft";
        state.blueprintCodeMessage = "message";
        state.iconSearchFocused = true;
        state.iconScrollDragging = true;
        state.iconTagMode = true;
        state.iconAllItemsMode = true;
        state.iconEntityMode = true;
        state.iconInventoryMode = true;
        state.iconFluidMode = true;
        state.recipeSearchFocused = true;
        state.recipeScrollDragging = true;
        state.recipeTagMode = true;
        state.recipeInventoryMode = true;
        state.recipeFluidMode = true;
        state.blockSearchFocused = true;
        state.blockScrollDragging = true;
        state.blockTagMode = true;
        state.biomeSearchFocused = true;
        state.biomeScrollDragging = true;
        state.advancementSearchFocused = true;
        state.advancementScrollDragging = true;
        state.structureSearchFocused = true;
        state.structureScrollDragging = true;
        state.statSearchFocused = true;
        state.statScrollDragging = true;
        state.dimensionSearchFocused = true;
        state.dimensionScrollDragging = true;
        state.lootTableSearchFocused = true;
        state.lootTableScrollDragging = true;
        state.itemInventorySearchFocused = true;
        state.itemInventoryScrollDragging = true;
        state.soundSearchFocused = true;
        state.soundScrollDragging = true;
        state.soundSelected = "minecraft:note_block.pling";
        state.pickerLastClickKey = "click";
        state.pickerLastClickAtMs = 456L;
        state.colorPaletteContextOpen = true;
        state.colorPaletteContextValue = 0xFF00FF00;
        state.colorPaletteScrollDragging = true;
        state.themeScrollDragging = true;
        state.settingsScrollDragging = true;
        state.prerequisitesManagerQuestId = "quest";
        state.prerequisitesManagerSearch = "query";
        state.prerequisitesManagerExternalMode = true;
        state.prerequisitesManagerContextOpen = true;
        state.prerequisitesManagerSelectedConnectionKey = "selected";
        state.prerequisitesManagerHoveredConnectionKey = "hovered";
        return state;
    }
}
