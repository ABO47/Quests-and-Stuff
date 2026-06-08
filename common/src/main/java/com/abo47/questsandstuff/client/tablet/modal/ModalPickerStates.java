package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.controls.SearchScrollState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class ModalPickerStates {
    private ModalPickerStates() {
    }

    public static SearchScrollState icon(TabletUiState state) {
        return SearchScrollState.bind(
                () -> state.iconSearch,
                value -> state.iconSearch = value,
                () -> state.iconSearchFocused,
                value -> state.iconSearchFocused = value,
                () -> state.iconScroll,
                value -> state.iconScroll = value,
                () -> state.iconScrollDragging,
                value -> state.iconScrollDragging = value
        );
    }

    public static SearchScrollState asset(TabletUiState state) {
        return SearchScrollState.bind(
                () -> state.assetSearch,
                value -> state.assetSearch = value,
                () -> state.assetSearchFocused,
                value -> state.assetSearchFocused = value,
                () -> state.assetGridScroll,
                value -> state.assetGridScroll = value,
                () -> state.assetGridScrollDragging,
                value -> state.assetGridScrollDragging = value
        );
    }

    public static SearchScrollState biome(TabletUiState state) {
        return SearchScrollState.bind(
                () -> state.biomeSearch,
                value -> state.biomeSearch = value,
                () -> state.biomeSearchFocused,
                value -> state.biomeSearchFocused = value,
                () -> state.biomeScroll,
                value -> state.biomeScroll = value,
                () -> state.biomeScrollDragging,
                value -> state.biomeScrollDragging = value
        );
    }

    public static SearchScrollState advancement(TabletUiState state) {
        return SearchScrollState.bind(
                () -> state.advancementSearch,
                value -> state.advancementSearch = value,
                () -> state.advancementSearchFocused,
                value -> state.advancementSearchFocused = value,
                () -> state.advancementScroll,
                value -> state.advancementScroll = value,
                () -> state.advancementScrollDragging,
                value -> state.advancementScrollDragging = value
        );
    }

    public static SearchScrollState recipe(TabletUiState state) {
        return SearchScrollState.bind(
                () -> state.recipeSearch,
                value -> state.recipeSearch = value,
                () -> state.recipeSearchFocused,
                value -> state.recipeSearchFocused = value,
                () -> state.recipeScroll,
                value -> state.recipeScroll = value,
                () -> state.recipeScrollDragging,
                value -> state.recipeScrollDragging = value
        );
    }

    public static SearchScrollState structure(TabletUiState state) {
        return SearchScrollState.bind(
                () -> state.structureSearch,
                value -> state.structureSearch = value,
                () -> state.structureSearchFocused,
                value -> state.structureSearchFocused = value,
                () -> state.structureScroll,
                value -> state.structureScroll = value,
                () -> state.structureScrollDragging,
                value -> state.structureScrollDragging = value
        );
    }

    public static SearchScrollState block(TabletUiState state) {
        return SearchScrollState.bind(
                () -> state.blockSearch,
                value -> state.blockSearch = value,
                () -> state.blockSearchFocused,
                value -> state.blockSearchFocused = value,
                () -> state.blockScroll,
                value -> state.blockScroll = value,
                () -> state.blockScrollDragging,
                value -> state.blockScrollDragging = value
        );
    }

    public static SearchScrollState stat(TabletUiState state) {
        return SearchScrollState.bind(
                () -> state.statSearch,
                value -> state.statSearch = value,
                () -> state.statSearchFocused,
                value -> state.statSearchFocused = value,
                () -> state.statScroll,
                value -> state.statScroll = value,
                () -> state.statScrollDragging,
                value -> state.statScrollDragging = value
        );
    }

    public static SearchScrollState dimension(TabletUiState state) {
        return SearchScrollState.bind(
                () -> state.dimensionSearch,
                value -> state.dimensionSearch = value,
                () -> state.dimensionSearchFocused,
                value -> state.dimensionSearchFocused = value,
                () -> state.dimensionScroll,
                value -> state.dimensionScroll = value,
                () -> state.dimensionScrollDragging,
                value -> state.dimensionScrollDragging = value
        );
    }

    public static SearchScrollState lootTable(TabletUiState state) {
        return SearchScrollState.bind(
                () -> state.lootTableSearch,
                value -> state.lootTableSearch = value,
                () -> state.lootTableSearchFocused,
                value -> state.lootTableSearchFocused = value,
                () -> state.lootTableScroll,
                value -> state.lootTableScroll = value,
                () -> state.lootTableScrollDragging,
                value -> state.lootTableScrollDragging = value
        );
    }

    public static SearchScrollState itemInventory(TabletUiState state) {
        return SearchScrollState.bind(
                () -> state.itemInventorySearch,
                value -> state.itemInventorySearch = value,
                () -> state.itemInventorySearchFocused,
                value -> state.itemInventorySearchFocused = value,
                () -> state.itemInventoryScroll,
                value -> state.itemInventoryScroll = value,
                () -> state.itemInventoryScrollDragging,
                value -> state.itemInventoryScrollDragging = value
        );
    }

    public static SearchScrollState sound(TabletUiState state) {
        return SearchScrollState.bind(
                () -> state.soundSearch,
                value -> state.soundSearch = value,
                () -> state.soundSearchFocused,
                value -> state.soundSearchFocused = value,
                () -> state.soundScroll,
                value -> state.soundScroll = value,
                () -> state.soundScrollDragging,
                value -> state.soundScrollDragging = value
        );
    }

    public static SearchScrollState entityVariant(TabletUiState state) {
        return SearchScrollState.bind(
                () -> state.entityVariantSearch,
                value -> state.entityVariantSearch = value,
                () -> state.entityVariantSearchFocused,
                value -> state.entityVariantSearchFocused = value,
                () -> state.entityVariantScroll,
                value -> state.entityVariantScroll = value,
                () -> state.entityVariantScrollDragging,
                value -> state.entityVariantScrollDragging = value
        );
    }

    public static SearchScrollState prerequisitesManager(TabletUiState state) {
        return SearchScrollState.bind(
                () -> state.prerequisitesManagerSearch,
                value -> state.prerequisitesManagerSearch = value,
                () -> state.prerequisitesManagerSearchFocused,
                value -> state.prerequisitesManagerSearchFocused = value,
                () -> state.prerequisitesManagerScroll,
                value -> state.prerequisitesManagerScroll = value,
                () -> state.prerequisitesManagerScrollDragging,
                value -> state.prerequisitesManagerScrollDragging = value
        );
    }
}
