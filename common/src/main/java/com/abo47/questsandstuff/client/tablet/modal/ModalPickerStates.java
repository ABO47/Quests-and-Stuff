package com.abo47.questsandstuff.client.tablet.modal;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.abo47.questsandstuff.client.tablet.controls.SearchScrollState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class ModalPickerStates {
    private ModalPickerStates() {
    }

    public static SearchScrollState forType(TabletUiState state, ModalWindowManager.ModalType type) {
        if (type == null) {
            throw new IllegalArgumentException("Modal type is required");
        }
        return switch (type) {
            case ICON_PICKER -> icon(state);
            case ASSET_PICKER -> asset(state);
            case BIOME_PICKER -> biome(state);
            case ADVANCEMENT_PICKER -> advancement(state);
            case RECIPE_PICKER -> recipe(state);
            case STRUCTURE_PICKER -> structure(state);
            case BLOCK_PICKER -> block(state);
            case STAT_PICKER -> stat(state);
            case DIMENSION_PICKER -> dimension(state);
            case LOOT_TABLE_PICKER -> lootTable(state);
            case ITEM_INVENTORY_PICKER -> itemInventory(state);
            case SOUND_PICKER -> sound(state);
            case ENTITY_VARIANT_PICKER -> entityVariant(state);
            case PREREQUISITES_MANAGER -> prerequisitesManager(state);
            case ITEM_LOCK_PICKER -> itemLock(state);
            default -> throw new IllegalArgumentException("Modal type has no picker state: " + type);
        };
    }

    public static SearchScrollState itemLock(TabletUiState state) {
        return bind(
                state,
                ModalWindowManager.ModalType.ITEM_LOCK_PICKER,
                () -> state.pickers.itemLockSearch,
                value -> state.pickers.itemLockSearch = value,
                () -> state.pickers.itemLockSearchFocused,
                value -> state.pickers.itemLockSearchFocused = value,
                () -> state.pickers.itemLockScroll,
                value -> state.pickers.itemLockScroll = value,
                () -> state.pickers.itemLockScrollDragging,
                value -> state.pickers.itemLockScrollDragging = value
        );
    }

    public static SearchScrollState icon(TabletUiState state) {
        return bind(
                state,
                ModalWindowManager.ModalType.ICON_PICKER,
                () -> state.pickers.iconSearch,
                value -> state.pickers.iconSearch = value,
                () -> state.pickers.iconSearchFocused,
                value -> state.pickers.iconSearchFocused = value,
                () -> state.pickers.iconScroll,
                value -> state.pickers.iconScroll = value,
                () -> state.pickers.iconScrollDragging,
                value -> state.pickers.iconScrollDragging = value
        );
    }

    public static SearchScrollState asset(TabletUiState state) {
        return bind(
                state,
                ModalWindowManager.ModalType.ASSET_PICKER,
                () -> state.pickers.assetSearch,
                value -> state.pickers.assetSearch = value,
                () -> state.pickers.assetSearchFocused,
                value -> state.pickers.assetSearchFocused = value,
                () -> state.pickers.assetGridScroll,
                value -> state.pickers.assetGridScroll = value,
                () -> state.pickers.assetGridScrollDragging,
                value -> state.pickers.assetGridScrollDragging = value
        );
    }

    public static SearchScrollState biome(TabletUiState state) {
        return bind(
                state,
                ModalWindowManager.ModalType.BIOME_PICKER,
                () -> state.pickers.biomeSearch,
                value -> state.pickers.biomeSearch = value,
                () -> state.pickers.biomeSearchFocused,
                value -> state.pickers.biomeSearchFocused = value,
                () -> state.pickers.biomeScroll,
                value -> state.pickers.biomeScroll = value,
                () -> state.pickers.biomeScrollDragging,
                value -> state.pickers.biomeScrollDragging = value
        );
    }

    public static SearchScrollState advancement(TabletUiState state) {
        return bind(
                state,
                ModalWindowManager.ModalType.ADVANCEMENT_PICKER,
                () -> state.pickers.advancementSearch,
                value -> state.pickers.advancementSearch = value,
                () -> state.pickers.advancementSearchFocused,
                value -> state.pickers.advancementSearchFocused = value,
                () -> state.pickers.advancementScroll,
                value -> state.pickers.advancementScroll = value,
                () -> state.pickers.advancementScrollDragging,
                value -> state.pickers.advancementScrollDragging = value
        );
    }

    public static SearchScrollState recipe(TabletUiState state) {
        return bind(
                state,
                ModalWindowManager.ModalType.RECIPE_PICKER,
                () -> state.pickers.recipeSearch,
                value -> state.pickers.recipeSearch = value,
                () -> state.pickers.recipeSearchFocused,
                value -> state.pickers.recipeSearchFocused = value,
                () -> state.pickers.recipeScroll,
                value -> state.pickers.recipeScroll = value,
                () -> state.pickers.recipeScrollDragging,
                value -> state.pickers.recipeScrollDragging = value
        );
    }

    public static SearchScrollState structure(TabletUiState state) {
        return bind(
                state,
                ModalWindowManager.ModalType.STRUCTURE_PICKER,
                () -> state.pickers.structureSearch,
                value -> state.pickers.structureSearch = value,
                () -> state.pickers.structureSearchFocused,
                value -> state.pickers.structureSearchFocused = value,
                () -> state.pickers.structureScroll,
                value -> state.pickers.structureScroll = value,
                () -> state.pickers.structureScrollDragging,
                value -> state.pickers.structureScrollDragging = value
        );
    }

    public static SearchScrollState block(TabletUiState state) {
        return bind(
                state,
                ModalWindowManager.ModalType.BLOCK_PICKER,
                () -> state.pickers.blockSearch,
                value -> state.pickers.blockSearch = value,
                () -> state.pickers.blockSearchFocused,
                value -> state.pickers.blockSearchFocused = value,
                () -> state.pickers.blockScroll,
                value -> state.pickers.blockScroll = value,
                () -> state.pickers.blockScrollDragging,
                value -> state.pickers.blockScrollDragging = value
        );
    }

    public static SearchScrollState stat(TabletUiState state) {
        return bind(
                state,
                ModalWindowManager.ModalType.STAT_PICKER,
                () -> state.pickers.statSearch,
                value -> state.pickers.statSearch = value,
                () -> state.pickers.statSearchFocused,
                value -> state.pickers.statSearchFocused = value,
                () -> state.pickers.statScroll,
                value -> state.pickers.statScroll = value,
                () -> state.pickers.statScrollDragging,
                value -> state.pickers.statScrollDragging = value
        );
    }

    public static SearchScrollState dimension(TabletUiState state) {
        return bind(
                state,
                ModalWindowManager.ModalType.DIMENSION_PICKER,
                () -> state.pickers.dimensionSearch,
                value -> state.pickers.dimensionSearch = value,
                () -> state.pickers.dimensionSearchFocused,
                value -> state.pickers.dimensionSearchFocused = value,
                () -> state.pickers.dimensionScroll,
                value -> state.pickers.dimensionScroll = value,
                () -> state.pickers.dimensionScrollDragging,
                value -> state.pickers.dimensionScrollDragging = value
        );
    }

    public static SearchScrollState lootTable(TabletUiState state) {
        return bind(
                state,
                ModalWindowManager.ModalType.LOOT_TABLE_PICKER,
                () -> state.pickers.lootTableSearch,
                value -> state.pickers.lootTableSearch = value,
                () -> state.pickers.lootTableSearchFocused,
                value -> state.pickers.lootTableSearchFocused = value,
                () -> state.pickers.lootTableScroll,
                value -> state.pickers.lootTableScroll = value,
                () -> state.pickers.lootTableScrollDragging,
                value -> state.pickers.lootTableScrollDragging = value
        );
    }

    public static SearchScrollState itemInventory(TabletUiState state) {
        return bind(
                state,
                ModalWindowManager.ModalType.ITEM_INVENTORY_PICKER,
                () -> state.pickers.itemInventorySearch,
                value -> state.pickers.itemInventorySearch = value,
                () -> state.pickers.itemInventorySearchFocused,
                value -> state.pickers.itemInventorySearchFocused = value,
                () -> state.pickers.itemInventoryScroll,
                value -> state.pickers.itemInventoryScroll = value,
                () -> state.pickers.itemInventoryScrollDragging,
                value -> state.pickers.itemInventoryScrollDragging = value
        );
    }

    public static SearchScrollState sound(TabletUiState state) {
        return bind(
                state,
                ModalWindowManager.ModalType.SOUND_PICKER,
                () -> state.pickers.soundSearch,
                value -> state.pickers.soundSearch = value,
                () -> state.pickers.soundSearchFocused,
                value -> state.pickers.soundSearchFocused = value,
                () -> state.pickers.soundScroll,
                value -> state.pickers.soundScroll = value,
                () -> state.pickers.soundScrollDragging,
                value -> state.pickers.soundScrollDragging = value
        );
    }

    public static SearchScrollState entityVariant(TabletUiState state) {
        return bind(
                state,
                ModalWindowManager.ModalType.ENTITY_VARIANT_PICKER,
                () -> state.pickers.entityVariantSearch,
                value -> state.pickers.entityVariantSearch = value,
                () -> state.pickers.entityVariantSearchFocused,
                value -> state.pickers.entityVariantSearchFocused = value,
                () -> state.pickers.entityVariantScroll,
                value -> state.pickers.entityVariantScroll = value,
                () -> state.pickers.entityVariantScrollDragging,
                value -> state.pickers.entityVariantScrollDragging = value
        );
    }

    public static SearchScrollState prerequisitesManager(TabletUiState state) {
        return bind(
                state,
                ModalWindowManager.ModalType.PREREQUISITES_MANAGER,
                () -> state.modal.prerequisitesManagerSearch,
                value -> state.modal.prerequisitesManagerSearch = value,
                () -> state.modal.prerequisitesManagerSearchFocused,
                value -> state.modal.prerequisitesManagerSearchFocused = value,
                () -> state.modal.prerequisitesManagerScroll,
                value -> state.modal.prerequisitesManagerScroll = value,
                () -> state.modal.prerequisitesManagerScrollDragging,
                value -> state.modal.prerequisitesManagerScrollDragging = value
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
        if (state == null || state.modal.modalSession == null || !state.modal.modalSession.active() || state.modal.modalSession.type() != type) {
            return null;
        }
        return state.modal.modalSession;
    }
}
