package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.controls.SearchScrollState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class ModalPickerStates {
    private ModalPickerStates() {
    }

    public static SearchScrollState icon(TabletUiState state) {
        return bind(
                state,
                ModalWindowManager.ModalType.ICON_PICKER,
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
        return bind(
                state,
                ModalWindowManager.ModalType.ASSET_PICKER,
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
        return bind(
                state,
                ModalWindowManager.ModalType.BIOME_PICKER,
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
        return bind(
                state,
                ModalWindowManager.ModalType.ADVANCEMENT_PICKER,
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
        return bind(
                state,
                ModalWindowManager.ModalType.RECIPE_PICKER,
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
        return bind(
                state,
                ModalWindowManager.ModalType.STRUCTURE_PICKER,
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
        return bind(
                state,
                ModalWindowManager.ModalType.BLOCK_PICKER,
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
        return bind(
                state,
                ModalWindowManager.ModalType.STAT_PICKER,
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
        return bind(
                state,
                ModalWindowManager.ModalType.DIMENSION_PICKER,
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
        return bind(
                state,
                ModalWindowManager.ModalType.LOOT_TABLE_PICKER,
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
        return bind(
                state,
                ModalWindowManager.ModalType.ITEM_INVENTORY_PICKER,
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
        return bind(
                state,
                ModalWindowManager.ModalType.SOUND_PICKER,
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
        return bind(
                state,
                ModalWindowManager.ModalType.ENTITY_VARIANT_PICKER,
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
        return bind(
                state,
                ModalWindowManager.ModalType.PREREQUISITES_MANAGER,
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

    private static SearchScrollState bind(
            TabletUiState state,
            ModalWindowManager.ModalType type,
            Supplier<String> searchSupplier,
            Consumer<String> searchConsumer,
            BooleanSupplier focusedSupplier,
            Consumer<Boolean> focusedConsumer,
            IntSupplier scrollSupplier,
            IntConsumer scrollConsumer,
            BooleanSupplier draggingSupplier,
            Consumer<Boolean> draggingConsumer
    ) {
        return SearchScrollState.bind(
                searchSupplier,
                value -> {
                    searchConsumer.accept(value);
                    ModalSession session = activeSession(state, type);
                    if (session != null) {
                        session.setPickerSearch(value);
                    }
                },
                focusedSupplier,
                value -> {
                    focusedConsumer.accept(value);
                    ModalSession session = activeSession(state, type);
                    if (session != null) {
                        session.setPickerFocused(value);
                    }
                },
                scrollSupplier,
                value -> {
                    scrollConsumer.accept(value);
                    ModalSession session = activeSession(state, type);
                    if (session != null) {
                        session.setPickerScroll(value);
                    }
                },
                draggingSupplier,
                value -> {
                    draggingConsumer.accept(value);
                    ModalSession session = activeSession(state, type);
                    if (session != null) {
                        session.setPickerDragging(value);
                    }
                }
        );
    }

    private static ModalSession activeSession(TabletUiState state, ModalWindowManager.ModalType type) {
        if (state == null || state.modalSession == null || !state.modalSession.active() || state.modalSession.type() != type) {
            return null;
        }
        return state.modalSession;
    }
}
