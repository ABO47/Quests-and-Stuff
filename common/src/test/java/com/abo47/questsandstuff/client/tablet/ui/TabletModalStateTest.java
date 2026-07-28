package com.abo47.questsandstuff.client.tablet.ui;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.modal.IconPickerMode;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalSession;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.ModalWindowManager;
import com.abo47.questsandstuff.client.tablet.modal.RecipePickerMode;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletModalState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabletModalStateTest {
    @BeforeEach
    void enablePopupAnimations() {
        QuestsAndStuffConfig.setUiAnimationsEnabled(true);
        QuestsAndStuffConfig.setPopupWindowAnimationsEnabled(true);
    }

    @Test
    void applyingModalTypeCreatesAndReplacesTheActiveSession() {
        TabletUiState state = new TabletUiState();

        TabletModalState.applyModalType(state, ModalWindowManager.ModalType.ICON_PICKER);

        assertEquals(ModalWindowManager.ModalType.ICON_PICKER, state.modal.modalSession.type());
        assertEquals(ModalWindowManager.ModalType.ICON_PICKER, ModalStateQueries.activeType(state));
        assertTrue(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.ICON_PICKER));

        TabletModalState.applyModalType(state, ModalWindowManager.ModalType.SOUND_PICKER);

        assertEquals(ModalWindowManager.ModalType.SOUND_PICKER, state.modal.modalSession.type());
        assertEquals(ModalWindowManager.ModalType.SOUND_PICKER, ModalStateQueries.activeType(state));
        assertFalse(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.ICON_PICKER));
        assertTrue(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.SOUND_PICKER));
    }

    @Test
    void openModalReplacesPreviousOpenModalAndCancelsClosingState() {
        TabletUiState state = new TabletUiState();
        state.modal.modalWindowClosing = true;
        state.modal.modalSession = ModalSession.open(ModalWindowManager.ModalType.ASSET_PICKER);

        TabletModalState.openModal(state, ModalWindowManager.ModalType.LOOT_TABLE_PICKER);

        assertFalse(state.modal.modalWindowClosing);
        assertFalse(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.ASSET_PICKER));
        assertTrue(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.LOOT_TABLE_PICKER));
        assertEquals(ModalWindowManager.ModalType.LOOT_TABLE_PICKER, state.modal.modalSession.type());
        assertEquals(ModalWindowManager.ModalType.LOOT_TABLE_PICKER, ModalStateQueries.activeType(state));
    }

    @Test
    void openModalCapturesRecentPointerSourceInsideRoot() {
        TabletUiState state = new TabletUiState();
        state.root.tabletRootWidth = 320;
        state.root.tabletRootHeight = 240;

        TabletModalState.rememberPointerSource(state, 20, 30);
        TabletModalState.openModal(state, ModalWindowManager.ModalType.ICON_PICKER);

        assertTrue(state.modal.modalWindowAnimationHasSource);
        assertEquals(12, state.modal.modalWindowAnimationSourceX);
        assertEquals(22, state.modal.modalWindowAnimationSourceY);
        assertEquals(16, state.modal.modalWindowAnimationSourceW);
        assertEquals(16, state.modal.modalWindowAnimationSourceH);
        assertTrue(state.modal.modalWindowAnimationStartMs > 0L);
    }

    @Test
    void animatedCloseKeepsTheModalActiveUntilTheWindowDurationEnds() {
        TabletUiState state = new TabletUiState();
        TabletModalState.openModal(state, ModalWindowManager.ModalType.ICON_PICKER);
        state.pickers.iconSearchFocused = true;

        TabletModalState.closeAllModals(state);

        assertTrue(state.modal.modalWindowClosing);
        assertTrue(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.ICON_PICKER));
        assertEquals(ModalWindowManager.ModalType.ICON_PICKER, state.modal.modalSession.type());
        assertFalse(state.pickers.iconSearchFocused);
        assertFalse(TabletModalState.finishClosingIfDone(state));
        assertTrue(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.ICON_PICKER));

        state.modal.modalWindowAnimationStartMs = System.currentTimeMillis() - 1_000L;

        assertTrue(TabletModalState.finishClosingIfDone(state));
        assertFalse(state.modal.modalWindowClosing);
        assertFalse(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.ICON_PICKER));
        assertEquals(ModalWindowManager.ModalType.NONE, state.modal.modalSession.type());
        assertEquals(ModalWindowManager.ModalType.NONE, ModalStateQueries.activeType(state));
    }

    @Test
    void reopeningDuringAnimatedCloseReplacesTheSessionAndCancelsClosing() {
        TabletUiState state = new TabletUiState();
        TabletModalState.openModal(state, ModalWindowManager.ModalType.ICON_PICKER);

        TabletModalState.closeAllModals(state);
        assertTrue(state.modal.modalWindowClosing);

        TabletModalState.openModal(state, ModalWindowManager.ModalType.ASSET_PICKER);

        assertFalse(state.modal.modalWindowClosing);
        assertFalse(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.ICON_PICKER));
        assertTrue(ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.ASSET_PICKER));
        assertEquals(ModalWindowManager.ModalType.ASSET_PICKER, state.modal.modalSession.type());
        assertEquals(ModalWindowManager.ModalType.ASSET_PICKER, ModalStateQueries.activeType(state));
        assertFalse(TabletModalState.finishClosingIfDone(state));
    }

    @Test
    void activeTypeUsesTheModalSessionOnly() {
        TabletUiState state = new TabletUiState();
        state.modal.modalSession = ModalSession.open(ModalWindowManager.ModalType.BLOCK_PICKER);

        assertEquals(ModalWindowManager.ModalType.BLOCK_PICKER, ModalStateQueries.activeType(state));
        assertTrue(ModalStateQueries.anyOpen(state));
    }

    @Test
    void oldCreateQuestModalFieldsDoNotExist() {
        String[] oldFields = {
                "createQuestModalOpen",
                "createQuestTitle",
                "createQuestLogicalX",
                "createQuestLogicalY"
        };
        for (String field : oldFields) {
            assertThrows(NoSuchFieldException.class, () -> TabletUiState.class.getDeclaredField(field), field);
        }
    }

    @Test
    void immediateCloseClearsTheActiveSession() {
        TabletUiState state = new TabletUiState();
        state.modal.modalSession = ModalSession.open(ModalWindowManager.ModalType.RECIPE_PICKER);

        TabletModalState.closeAllModalsImmediately(state);

        assertEquals(ModalWindowManager.ModalType.NONE, state.modal.modalSession.type());
        assertFalse(state.modal.modalSession.active());
        assertEquals(ModalWindowManager.ModalType.NONE, ModalStateQueries.activeType(state));
    }

    @Test
    void modalSessionCapturesBatchTargetsAndClearsThemOnClose() {
        TabletUiState state = new TabletUiState();

        ModalOpenActions.openBatchQuestBackgroundPicker(state, List.of("quest_a", " quest_b "), "background.png", true);

        assertEquals(ModalWindowManager.ModalType.ASSET_PICKER, state.modal.modalSession.type());
        assertEquals("", state.modal.modalSession.target(ModalSession.TargetSlot.QUEST_BACKGROUND));
        assertTrue(state.modal.modalSession.targetSet(ModalSession.TargetSetSlot.QUEST_BACKGROUND).contains("quest_a"));
        assertTrue(state.modal.modalSession.targetSet(ModalSession.TargetSetSlot.QUEST_BACKGROUND).contains("quest_b"));
        assertEquals("background.png", state.modal.modalSession.selectedValue());

        TabletModalState.closeAllModalsImmediately(state);

        assertEquals(ModalWindowManager.ModalType.NONE, state.modal.modalSession.type());
        assertTrue(state.modal.modalSession.targetSet(ModalSession.TargetSetSlot.QUEST_BACKGROUND).isEmpty());
    }

    @Test
    void immediateCloseClearsModalTypeTargetsAndPickerInteraction() {
        TabletUiState state = dirtyModalState();

        TabletModalState.closeAllModalsImmediately(state);

        assertEquals(ModalWindowManager.ModalType.NONE, state.modal.modalSession.type());
        assertFalse(state.modal.modalSession.active());
        assertEquals(ModalWindowManager.ModalType.NONE, ModalStateQueries.activeType(state));
        assertFalse(ModalStateQueries.anyOpen(state));
        assertEquals("", state.modal.modalQuestTarget);
        assertEquals("", state.modal.modalChapterTarget);
        assertEquals("", state.modal.modalCanvasBackgroundTarget);
        assertEquals("", state.modal.modalCanvasImageTarget);
        assertEquals("", state.modal.modalCanvasEntityTarget);
        assertEquals("", state.modal.modalCanvasModelTarget);
        assertEquals("", state.modal.modalBlueprintTarget);
        assertEquals("", state.questDetails.questDetailsPickTarget);
        assertEquals("", state.questDetails.questDetailsAssetPickTarget);
        assertFalse(state.modal.modalQuestBackgroundGrayscale);
        assertFalse(state.modal.modalQuestBackgroundTargets.contains("quest_a"));
        assertFalse(state.modal.modalQuestCompletionHudBackgroundTargets.contains("quest_b"));
        assertFalse(state.pickers.assetContextOpen);
        assertFalse(state.pickers.assetRenameOpen);
        assertFalse(state.modal.blueprintCodeOpen);
        assertFalse(state.modal.blueprintCodeImportMode);
        assertEquals("", state.modal.blueprintCodeTarget);
        assertEquals("", state.modal.blueprintCodeDraft);
        assertEquals("", state.modal.blueprintCodeMessage);
        assertFalse(state.pickers.iconSearchFocused);
        assertFalse(state.pickers.iconScrollDragging);
        assertEquals(IconPickerMode.ITEMS, state.pickers.iconMode);
        assertFalse(state.pickers.recipeSearchFocused);
        assertFalse(state.pickers.recipeScrollDragging);
        assertEquals(RecipePickerMode.ITEMS, state.pickers.recipeMode);
        assertFalse(state.pickers.blockSearchFocused);
        assertFalse(state.pickers.blockScrollDragging);
        assertFalse(state.pickers.blockTagMode);
        assertFalse(state.pickers.biomeSearchFocused);
        assertFalse(state.pickers.biomeScrollDragging);
        assertFalse(state.pickers.advancementSearchFocused);
        assertFalse(state.pickers.advancementScrollDragging);
        assertFalse(state.pickers.structureSearchFocused);
        assertFalse(state.pickers.structureScrollDragging);
        assertFalse(state.pickers.statSearchFocused);
        assertFalse(state.pickers.statScrollDragging);
        assertFalse(state.pickers.dimensionSearchFocused);
        assertFalse(state.pickers.dimensionScrollDragging);
        assertFalse(state.pickers.lootTableSearchFocused);
        assertFalse(state.pickers.lootTableScrollDragging);
        assertFalse(state.pickers.itemInventorySearchFocused);
        assertFalse(state.pickers.itemInventoryScrollDragging);
        assertFalse(state.pickers.soundSearchFocused);
        assertFalse(state.pickers.soundScrollDragging);
        assertEquals("", state.pickers.soundSelected);
        assertEquals("", state.pickers.pickerLastClickKey);
        assertEquals(0L, state.pickers.pickerLastClickAtMs);
        assertFalse(state.pickers.colorPaletteContextOpen);
        assertEquals(Integer.MIN_VALUE, state.pickers.colorPaletteContextValue);
        assertFalse(state.pickers.colorPaletteScrollDragging);
        assertFalse(state.modal.themeScrollDragging);
        assertEquals("", state.modal.prerequisitesManagerQuestId);
        assertEquals("", state.modal.prerequisitesManagerSearch);
        assertFalse(state.modal.prerequisitesManagerExternalMode);
        assertFalse(state.modal.prerequisitesManagerContextOpen);
        assertEquals("", state.modal.prerequisitesManagerSelectedConnectionKey);
        assertEquals("", state.modal.prerequisitesManagerHoveredConnectionKey);
        assertFalse(state.modal.modalWindowClosing);
        assertFalse(state.modal.modalWindowAnimationHasSource);
        assertEquals(0L, state.modal.modalWindowAnimationStartMs);
    }

    private static TabletUiState dirtyModalState() {
        TabletUiState state = new TabletUiState();
        state.modal.modalSession = ModalSession.open(ModalWindowManager.ModalType.ASSET_PICKER);
        state.modal.modalWindowClosing = true;
        state.modal.modalWindowAnimationHasSource = true;
        state.modal.modalWindowAnimationStartMs = 123L;
        state.modal.modalQuestTarget = "quest";
        state.modal.modalChapterTarget = "chapter";
        state.modal.modalCanvasBackgroundTarget = "canvas";
        state.modal.modalCanvasImageTarget = "image";
        state.modal.modalCanvasEntityTarget = "entity";
        state.modal.modalCanvasModelTarget = "model";
        state.modal.modalBlueprintTarget = "blueprint";
        state.questDetails.questDetailsPickTarget = "details";
        state.questDetails.questDetailsAssetPickTarget = "asset_details";
        state.modal.modalQuestBackgroundTargets.add("quest_a");
        state.modal.modalQuestBackgroundGrayscale = true;
        state.modal.modalQuestCompletionHudBackgroundTargets.add("quest_b");
        state.pickers.assetContextOpen = true;
        state.pickers.assetRenameOpen = true;
        state.modal.blueprintCodeOpen = true;
        state.modal.blueprintCodeImportMode = true;
        state.modal.blueprintCodeTarget = "target";
        state.modal.blueprintCodeDraft = "draft";
        state.modal.blueprintCodeMessage = "message";
        state.pickers.iconSearchFocused = true;
        state.pickers.iconScrollDragging = true;
        state.pickers.iconMode = IconPickerMode.INVENTORY;
        state.pickers.recipeSearchFocused = true;
        state.pickers.recipeScrollDragging = true;
        state.pickers.recipeMode = RecipePickerMode.INVENTORY;
        state.pickers.blockSearchFocused = true;
        state.pickers.blockScrollDragging = true;
        state.pickers.blockTagMode = true;
        state.pickers.biomeSearchFocused = true;
        state.pickers.biomeScrollDragging = true;
        state.pickers.advancementSearchFocused = true;
        state.pickers.advancementScrollDragging = true;
        state.pickers.structureSearchFocused = true;
        state.pickers.structureScrollDragging = true;
        state.pickers.statSearchFocused = true;
        state.pickers.statScrollDragging = true;
        state.pickers.dimensionSearchFocused = true;
        state.pickers.dimensionScrollDragging = true;
        state.pickers.lootTableSearchFocused = true;
        state.pickers.lootTableScrollDragging = true;
        state.pickers.itemInventorySearchFocused = true;
        state.pickers.itemInventoryScrollDragging = true;
        state.pickers.soundSearchFocused = true;
        state.pickers.soundScrollDragging = true;
        state.pickers.soundSelected = "minecraft:note_block.pling";
        state.pickers.pickerLastClickKey = "click";
        state.pickers.pickerLastClickAtMs = 456L;
        state.pickers.colorPaletteContextOpen = true;
        state.pickers.colorPaletteContextValue = 0xFF00FF00;
        state.pickers.colorPaletteScrollDragging = true;
        state.modal.themeScrollDragging = true;
        state.modal.prerequisitesManagerQuestId = "quest";
        state.modal.prerequisitesManagerSearch = "query";
        state.modal.prerequisitesManagerExternalMode = true;
        state.modal.prerequisitesManagerContextOpen = true;
        state.modal.prerequisitesManagerSelectedConnectionKey = "selected";
        state.modal.prerequisitesManagerHoveredConnectionKey = "hovered";
        return state;
    }
}
