package com.abo47.questsandstuff.client.tablet.modal;


public final class ModalWindowManager {
    public enum ModalType {
        NONE,
        ICON_PICKER,
        ASSET_PICKER,
        BIOME_PICKER,
        ADVANCEMENT_PICKER,
        RECIPE_PICKER,
        STRUCTURE_PICKER,
        BLOCK_PICKER,
        STAT_PICKER,
        DIMENSION_PICKER,
        LOOT_TABLE_PICKER,
        ITEM_INVENTORY_PICKER,
        SOUND_PICKER,
        COLOR_PICKER,
        THEME_PICKER,
        ENTITY_VARIANT_PICKER,
        SETTINGS_PANEL
    }

    private ModalWindowManager() {
    }

    public static ModalType activeType(boolean iconOpen, boolean assetOpen, boolean colorOpen) {
        if (iconOpen) return ModalType.ICON_PICKER;
        if (assetOpen) return ModalType.ASSET_PICKER;
        if (colorOpen) return ModalType.COLOR_PICKER;
        return ModalType.NONE;
    }

    public static boolean anyOpen(boolean iconOpen, boolean assetOpen, boolean colorOpen) {
        return iconOpen || assetOpen || colorOpen;
    }

    public static boolean anyOpen(boolean iconOpen, boolean assetOpen, boolean biomeOpen, boolean colorOpen) {
        return iconOpen || assetOpen || biomeOpen || colorOpen;
    }

    public static boolean anyOpen(boolean iconOpen, boolean assetOpen, boolean biomeOpen, boolean colorOpen, boolean themeOpen) {
        return iconOpen || assetOpen || biomeOpen || colorOpen || themeOpen;
    }

    public static boolean anyOpen(boolean iconOpen, boolean assetOpen, boolean biomeOpen, boolean colorOpen, boolean themeOpen, boolean entityVariantOpen) {
        return iconOpen || assetOpen || biomeOpen || colorOpen || themeOpen || entityVariantOpen;
    }

    public static boolean anyOpen(boolean iconOpen, boolean assetOpen, boolean biomeOpen, boolean lootTableOpen, boolean colorOpen, boolean themeOpen, boolean entityVariantOpen) {
        return iconOpen || assetOpen || biomeOpen || lootTableOpen || colorOpen || themeOpen || entityVariantOpen;
    }

    public static ModalFlags open(ModalType type) {
        return new ModalFlags(
                type == ModalType.ICON_PICKER,
                type == ModalType.ASSET_PICKER,
                type == ModalType.BIOME_PICKER,
                type == ModalType.ADVANCEMENT_PICKER,
                type == ModalType.RECIPE_PICKER,
                type == ModalType.STRUCTURE_PICKER,
                type == ModalType.BLOCK_PICKER,
                type == ModalType.STAT_PICKER,
                type == ModalType.DIMENSION_PICKER,
                type == ModalType.LOOT_TABLE_PICKER,
                type == ModalType.ITEM_INVENTORY_PICKER,
                type == ModalType.SOUND_PICKER,
                type == ModalType.COLOR_PICKER,
                type == ModalType.THEME_PICKER,
                type == ModalType.ENTITY_VARIANT_PICKER,
                type == ModalType.SETTINGS_PANEL
        );
    }

    public static ModalFlags closeAll() {
        return open(ModalType.NONE);
    }

    public record ModalFlags(boolean iconOpen, boolean assetOpen, boolean biomeOpen, boolean advancementOpen, boolean recipeOpen, boolean structureOpen, boolean blockOpen, boolean statOpen, boolean dimensionOpen, boolean lootTableOpen, boolean itemInventoryOpen, boolean soundOpen, boolean colorOpen, boolean themeOpen, boolean entityVariantOpen, boolean settingsOpen) {
    }
}
