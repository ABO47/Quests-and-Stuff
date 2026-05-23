package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.controls.SearchFieldController;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static com.abo47.questsandstuff.client.tablet.ui.TabletModalState.openModal;

public final class ModalOpenActions {
    private ModalOpenActions() {
    }

    public static void openQuestDetailsIconPicker(TabletUiState state, String target) {
        closeBeforeOpen(state);
        state.modalQuestTarget = "";
        state.modalChapterTarget = "";
        state.questDetailsPickTarget = target == null ? "" : target;
        resetIconPicker(state);
        openModal(state, ModalWindowManager.ModalType.ICON_PICKER);
    }

    public static void openChapterIconPicker(TabletUiState state, String chapter) {
        closeBeforeOpen(state);
        state.modalChapterTarget = chapter == null ? "" : chapter;
        state.modalQuestTarget = "";
        resetIconPicker(state);
        openModal(state, ModalWindowManager.ModalType.ICON_PICKER);
    }

    public static void openQuestIconPicker(TabletUiState state, String questId) {
        closeBeforeOpen(state);
        state.modalChapterTarget = "";
        state.modalQuestTarget = questId == null ? "" : questId;
        resetIconPicker(state);
        openModal(state, ModalWindowManager.ModalType.ICON_PICKER);
    }

    public static void openBiomePicker(TabletUiState state, String target) {
        closeBeforeOpen(state);
        state.modalQuestTarget = "";
        state.modalChapterTarget = "";
        state.questDetailsPickTarget = target == null ? "" : target;
        state.biomeSearch = "";
        state.biomeScroll = 0;
        state.biomeScrollDragging = false;
        state.biomeSearchFocused = false;
        openModal(state, ModalWindowManager.ModalType.BIOME_PICKER);
    }

    public static void openAdvancementPicker(TabletUiState state, String target) {
        closeBeforeOpen(state);
        state.modalQuestTarget = "";
        state.modalChapterTarget = "";
        state.questDetailsPickTarget = target == null ? "" : target;
        state.advancementSearch = "";
        state.advancementScroll = 0;
        state.advancementScrollDragging = false;
        state.advancementSearchFocused = false;
        openModal(state, ModalWindowManager.ModalType.ADVANCEMENT_PICKER);
    }

    public static void openRecipePicker(TabletUiState state, String target) {
        closeBeforeOpen(state);
        state.modalQuestTarget = "";
        state.modalChapterTarget = "";
        state.questDetailsPickTarget = target == null ? "" : target;
        state.recipeSearch = "";
        state.recipeTagMode = false;
        state.recipeScroll = 0;
        state.recipeScrollDragging = false;
        state.recipeSearchFocused = false;
        openModal(state, ModalWindowManager.ModalType.RECIPE_PICKER);
    }

    public static void openStructurePicker(TabletUiState state, String target) {
        closeBeforeOpen(state);
        state.modalQuestTarget = "";
        state.modalChapterTarget = "";
        state.questDetailsPickTarget = target == null ? "" : target;
        state.structureSearch = "";
        state.structureScroll = 0;
        state.structureScrollDragging = false;
        state.structureSearchFocused = false;
        openModal(state, ModalWindowManager.ModalType.STRUCTURE_PICKER);
    }

    public static void openBlockPicker(TabletUiState state, String target) {
        closeBeforeOpen(state);
        state.modalQuestTarget = "";
        state.modalChapterTarget = "";
        state.questDetailsPickTarget = target == null ? "" : target;
        state.blockSearch = "";
        state.blockTagMode = false;
        state.blockScroll = 0;
        state.blockScrollDragging = false;
        state.blockSearchFocused = false;
        openModal(state, ModalWindowManager.ModalType.BLOCK_PICKER);
    }

    public static void openStatPicker(TabletUiState state, String target) {
        closeBeforeOpen(state);
        state.modalQuestTarget = "";
        state.modalChapterTarget = "";
        state.questDetailsPickTarget = target == null ? "" : target;
        state.statSearch = "";
        state.statScroll = 0;
        state.statScrollDragging = false;
        state.statSearchFocused = false;
        openModal(state, ModalWindowManager.ModalType.STAT_PICKER);
    }

    public static void openDimensionPicker(TabletUiState state, String target) {
        closeBeforeOpen(state);
        state.modalQuestTarget = "";
        state.modalChapterTarget = "";
        state.questDetailsPickTarget = target == null ? "" : target;
        state.dimensionSearch = "";
        state.dimensionScroll = 0;
        state.dimensionScrollDragging = false;
        state.dimensionSearchFocused = false;
        openModal(state, ModalWindowManager.ModalType.DIMENSION_PICKER);
    }

    public static void openLootTablePicker(TabletUiState state, String target) {
        closeBeforeOpen(state);
        state.modalQuestTarget = "";
        state.modalChapterTarget = "";
        state.questDetailsPickTarget = target == null ? "" : target;
        state.lootTableSearch = "";
        state.lootTableScroll = 0;
        state.lootTableScrollDragging = false;
        state.lootTableSearchFocused = false;
        openModal(state, ModalWindowManager.ModalType.LOOT_TABLE_PICKER);
    }

    public static void openItemInventoryPicker(TabletUiState state, String target) {
        closeBeforeOpen(state);
        state.modalQuestTarget = "";
        state.modalChapterTarget = "";
        state.questDetailsPickTarget = target == null ? "" : target;
        state.itemInventorySearch = "";
        state.itemInventoryScroll = 0;
        state.itemInventoryScrollDragging = false;
        state.itemInventorySearchFocused = false;
        openModal(state, ModalWindowManager.ModalType.ITEM_INVENTORY_PICKER);
    }

    public static void openColorPicker(TabletUiState state, String target, int color) {
        closeBeforeOpen(state);
        state.colorPickerTarget = target == null ? "" : target;
        state.colorDraft = color;
        state.colorHexDraft = SearchFieldController.toHexColor(color);
        state.colorPaletteContextOpen = false;
        state.colorPaletteContextValue = Integer.MIN_VALUE;
        state.colorPaletteScrollDragging = false;
        state.contextDeleteConfirmKey = "";
        openModal(state, ModalWindowManager.ModalType.COLOR_PICKER);
    }

    public static void openThemePicker(TabletUiState state) {
        closeBeforeOpen(state);
        state.themeScrollDragging = false;
        state.contextDeleteConfirmKey = "";
        openModal(state, ModalWindowManager.ModalType.THEME_PICKER);
    }

    public static void openSettingsPanel(TabletUiState state) {
        closeBeforeOpen(state);
        state.settingsTab = 0;
        state.settingsScroll = 0;
        state.settingsScrollDragging = false;
        state.contextDeleteConfirmKey = "";
        openModal(state, ModalWindowManager.ModalType.SETTINGS_PANEL);
    }

    public static void openAssetPicker(TabletUiState state, String target) {
        openAssetPicker(state, target, "");
    }

    public static void openAssetPicker(TabletUiState state, String target, String selectedAsset) {
        closeBeforeOpen(state);
        state.questDetailsAssetPickTarget = target == null ? "" : target;
        resetAssetPicker(state);
        state.assetSelected = selectedAsset == null ? "" : selectedAsset;
        openModal(state, ModalWindowManager.ModalType.ASSET_PICKER);
    }

    public static void openQuestCompletionSoundPicker(TabletUiState state, String questId, String currentSound) {
        closeBeforeOpen(state);
        state.modalQuestCompletionSoundTarget = questId == null ? "" : questId;
        state.modalCanvasBackgroundTarget = "";
        state.modalCanvasImageTarget = "";
        state.modalCanvasEntityTarget = "";
        state.questDetailsAssetPickTarget = "";
        state.contextDeleteConfirmKey = "";
        resetAssetPicker(state);
        state.assetBrowseDir = "sounds";
        state.assetSelected = currentSound == null || currentSound.isBlank() ? "" : currentSound;
        openModal(state, ModalWindowManager.ModalType.ASSET_PICKER);
    }

    public static void openChapterBackgroundPicker(TabletUiState state, String chapter, String currentBackground) {
        closeBeforeOpen(state);
        state.modalChapterTarget = chapter == null ? "" : chapter;
        state.modalQuestTarget = "";
        state.modalCanvasBackgroundTarget = "";
        resetAssetPicker(state);
        state.assetSelected = currentBackground == null ? "" : currentBackground;
        openModal(state, ModalWindowManager.ModalType.ASSET_PICKER);
    }

    public static void openCanvasBackgroundPicker(TabletUiState state, String group, String currentBackground) {
        closeBeforeOpen(state);
        state.modalCanvasBackgroundTarget = group == null ? "" : group;
        state.modalCanvasImageTarget = "";
        state.modalCanvasEntityTarget = "";
        state.modalChapterTarget = "";
        state.modalQuestTarget = "";
        resetAssetPicker(state);
        state.assetSelected = currentBackground == null ? "" : currentBackground;
        openModal(state, ModalWindowManager.ModalType.ASSET_PICKER);
    }

    public static void openCanvasImagePicker(TabletUiState state, String group, int logicalX, int logicalY) {
        closeBeforeOpen(state);
        state.modalCanvasImageTarget = group == null ? "" : group;
        state.modalCanvasEntityTarget = "";
        state.modalCanvasBackgroundTarget = "";
        state.modalChapterTarget = "";
        state.modalQuestTarget = "";
        state.canvasImageLogicalX = logicalX;
        state.canvasImageLogicalY = logicalY;
        resetAssetPicker(state);
        state.assetSelected = "";
        openModal(state, ModalWindowManager.ModalType.ASSET_PICKER);
    }

    public static void openCanvasEntityPicker(TabletUiState state, String target, int logicalX, int logicalY) {
        closeBeforeOpen(state);
        state.modalCanvasEntityTarget = target == null ? "" : target;
        state.modalCanvasImageTarget = "";
        state.modalCanvasBackgroundTarget = "";
        state.modalChapterTarget = "";
        state.modalQuestTarget = "";
        state.canvasImageLogicalX = logicalX;
        state.canvasImageLogicalY = logicalY;
        resetIconPicker(state);
        openModal(state, ModalWindowManager.ModalType.ICON_PICKER);
    }

    public static void openEntityVariantPicker(TabletUiState state, String target, String icon) {
        closeBeforeOpen(state);
        state.entityVariantTarget = target == null ? "" : target;
        state.entityVariantSelected = EntityPreviewRenderer.entityVariant(icon == null ? "" : icon);
        state.entityVariantFolder = "";
        state.entityVariantSearch = "";
        state.entityVariantSearchFocused = false;
        state.entityVariantScroll = 0;
        state.entityVariantScrollDragging = false;
        state.contextDeleteConfirmKey = "";
        openModal(state, ModalWindowManager.ModalType.ENTITY_VARIANT_PICKER);
    }

    private static void resetIconPicker(TabletUiState state) {
        state.iconSearch = "";
        state.iconSearchFocused = false;
        state.iconTagMode = false;
        state.iconAllItemsMode = false;
        state.iconEntityMode = false;
        state.iconScroll = 0;
        state.iconScrollDragging = false;
    }

    private static void resetAssetPicker(TabletUiState state) {
        state.assetContextOpen = false;
        state.assetRenameOpen = false;
        state.assetBrowseDir = "";
        state.assetSearch = "";
        state.assetSearchFocused = false;
        state.assetGridScroll = 0;
        state.assetGridScrollDragging = false;
    }

    private static void closeBeforeOpen(TabletUiState state) {
        ModalCloseActions.closeAllImmediately(state);
    }
}
