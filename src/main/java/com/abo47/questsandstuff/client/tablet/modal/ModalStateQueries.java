package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class ModalStateQueries {
    private ModalStateQueries() {
    }

    public static boolean anyOpen(TabletUiState state) {
        return state != null && (state.modalWindowClosing || activeType(state) != ModalWindowManager.ModalType.NONE);
    }

    public static ModalWindowManager.ModalType activeType(TabletUiState state) {
        if (state == null) {
            return ModalWindowManager.ModalType.NONE;
        }
        if (state.iconPickerOpen) {
            return ModalWindowManager.ModalType.ICON_PICKER;
        }
        if (state.assetPickerOpen) {
            return ModalWindowManager.ModalType.ASSET_PICKER;
        }
        if (state.biomePickerOpen) {
            return ModalWindowManager.ModalType.BIOME_PICKER;
        }
        if (state.lootTablePickerOpen) {
            return ModalWindowManager.ModalType.LOOT_TABLE_PICKER;
        }
        if (state.itemInventoryPickerOpen) {
            return ModalWindowManager.ModalType.ITEM_INVENTORY_PICKER;
        }
        if (state.colorPickerOpen) {
            return ModalWindowManager.ModalType.COLOR_PICKER;
        }
        if (state.themePickerOpen) {
            return ModalWindowManager.ModalType.THEME_PICKER;
        }
        if (state.entityVariantPickerOpen) {
            return ModalWindowManager.ModalType.ENTITY_VARIANT_PICKER;
        }
        if (state.settingsPanelOpen) {
            return ModalWindowManager.ModalType.SETTINGS_PANEL;
        }
        return ModalWindowManager.ModalType.NONE;
    }
}
