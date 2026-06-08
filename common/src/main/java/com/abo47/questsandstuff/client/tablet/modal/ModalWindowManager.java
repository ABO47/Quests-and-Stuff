package com.abo47.questsandstuff.client.tablet.modal;


import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class ModalWindowManager {
    public enum ModalType {
        NONE(null, null),
        ICON_PICKER(state -> state.iconPickerOpen, (state, open) -> state.iconPickerOpen = open),
        ASSET_PICKER(state -> state.assetPickerOpen, (state, open) -> state.assetPickerOpen = open),
        BIOME_PICKER(state -> state.biomePickerOpen, (state, open) -> state.biomePickerOpen = open),
        ADVANCEMENT_PICKER(state -> state.advancementPickerOpen, (state, open) -> state.advancementPickerOpen = open),
        RECIPE_PICKER(state -> state.recipePickerOpen, (state, open) -> state.recipePickerOpen = open),
        STRUCTURE_PICKER(state -> state.structurePickerOpen, (state, open) -> state.structurePickerOpen = open),
        BLOCK_PICKER(state -> state.blockPickerOpen, (state, open) -> state.blockPickerOpen = open),
        STAT_PICKER(state -> state.statPickerOpen, (state, open) -> state.statPickerOpen = open),
        DIMENSION_PICKER(state -> state.dimensionPickerOpen, (state, open) -> state.dimensionPickerOpen = open),
        LOOT_TABLE_PICKER(state -> state.lootTablePickerOpen, (state, open) -> state.lootTablePickerOpen = open),
        ITEM_INVENTORY_PICKER(state -> state.itemInventoryPickerOpen, (state, open) -> state.itemInventoryPickerOpen = open),
        SOUND_PICKER(state -> state.soundPickerOpen, (state, open) -> state.soundPickerOpen = open),
        COLOR_PICKER(state -> state.colorPickerOpen, (state, open) -> state.colorPickerOpen = open),
        THEME_PICKER(state -> state.themePickerOpen, (state, open) -> state.themePickerOpen = open),
        ENTITY_VARIANT_PICKER(state -> state.entityVariantPickerOpen, (state, open) -> state.entityVariantPickerOpen = open),
        PREREQUISITES_MANAGER(state -> state.prerequisitesManagerOpen, (state, open) -> state.prerequisitesManagerOpen = open),
        SETTINGS_PANEL(state -> state.settingsPanelOpen, (state, open) -> state.settingsPanelOpen = open);

        private final FlagReader flagReader;
        private final FlagWriter flagWriter;

        ModalType(FlagReader flagReader, FlagWriter flagWriter) {
            this.flagReader = flagReader;
            this.flagWriter = flagWriter;
        }

        public boolean flagOpen(TabletUiState state) {
            return state != null && flagReader != null && flagReader.open(state);
        }

        private void setFlag(TabletUiState state, boolean open) {
            if (state != null && flagWriter != null) {
                flagWriter.set(state, open);
            }
        }
    }

    private ModalWindowManager() {
    }

    public static void applyOpenFlags(TabletUiState state, ModalType activeType) {
        ModalType safeType = activeType == null ? ModalType.NONE : activeType;
        for (ModalType type : ModalType.values()) {
            type.setFlag(state, type == safeType);
        }
    }

    public static ModalType typeFromFlags(TabletUiState state) {
        if (state == null) {
            return ModalType.NONE;
        }
        for (ModalType type : ModalType.values()) {
            if (type.flagOpen(state)) {
                return type;
            }
        }
        return ModalType.NONE;
    }

    private interface FlagReader {
        boolean open(TabletUiState state);
    }

    private interface FlagWriter {
        void set(TabletUiState state, boolean open);
    }
}
