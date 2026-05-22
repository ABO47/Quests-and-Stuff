package com.abo47.questsandstuff.client.tablet.modal;


public final class ModalWindowManager {
    public enum ModalType {
        NONE,
        ICON_PICKER,
        ASSET_PICKER,
        BIOME_PICKER,
        ADVANCEMENT_PICKER,
        DIMENSION_PICKER,
        LOOT_TABLE_PICKER,
        ITEM_INVENTORY_PICKER,
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
        return switch (type) {
            case ICON_PICKER -> new ModalFlags(true, false, false, false, false, false, false, false, false, false, false);
            case ASSET_PICKER -> new ModalFlags(false, true, false, false, false, false, false, false, false, false, false);
            case BIOME_PICKER -> new ModalFlags(false, false, true, false, false, false, false, false, false, false, false);
            case ADVANCEMENT_PICKER -> new ModalFlags(false, false, false, true, false, false, false, false, false, false, false);
            case DIMENSION_PICKER -> new ModalFlags(false, false, false, false, true, false, false, false, false, false, false);
            case LOOT_TABLE_PICKER -> new ModalFlags(false, false, false, false, false, true, false, false, false, false, false);
            case ITEM_INVENTORY_PICKER -> new ModalFlags(false, false, false, false, false, false, true, false, false, false, false);
            case COLOR_PICKER -> new ModalFlags(false, false, false, false, false, false, false, true, false, false, false);
            case THEME_PICKER -> new ModalFlags(false, false, false, false, false, false, false, false, true, false, false);
            case ENTITY_VARIANT_PICKER -> new ModalFlags(false, false, false, false, false, false, false, false, false, true, false);
            case SETTINGS_PANEL -> new ModalFlags(false, false, false, false, false, false, false, false, false, false, true);
            default -> new ModalFlags(false, false, false, false, false, false, false, false, false, false, false);
        };
    }

    public static ModalFlags closeAll() {
        return new ModalFlags(false, false, false, false, false, false, false, false, false, false, false);
    }

    public record ModalFlags(boolean iconOpen, boolean assetOpen, boolean biomeOpen, boolean advancementOpen, boolean dimensionOpen, boolean lootTableOpen, boolean itemInventoryOpen, boolean colorOpen, boolean themeOpen, boolean entityVariantOpen, boolean settingsOpen) {
    }
}
