package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.network.chat.Component;

public enum IconPickerMode {
    USABLE_ITEMS("usable_items", "send-horizontal"),
    ITEMS("items", "mode_items"),
    TAGS("tags", "mode_tags"),
    FLUIDS("fluids", "mode_fluids"),
    ENTITIES("entities", "entity"),
    INVENTORY("inventory", "mode_inventory");

    private static final IconPickerMode[] MODEL_ITEM_CYCLE = {ITEMS, TAGS};
    private static final IconPickerMode[] USE_ITEM_CYCLE = {USABLE_ITEMS, ITEMS, TAGS, FLUIDS};
    private static final IconPickerMode[] USE_ITEM_WITH_INVENTORY_CYCLE = {USABLE_ITEMS, ITEMS, TAGS, FLUIDS, INVENTORY};
    private static final IconPickerMode[] GENERAL_CYCLE = {ITEMS, TAGS, FLUIDS};
    private static final IconPickerMode[] GENERAL_WITH_ENTITY_CYCLE = {ITEMS, TAGS, FLUIDS, ENTITIES};
    private static final IconPickerMode[] GENERAL_WITH_INVENTORY_CYCLE = {ITEMS, TAGS, FLUIDS, INVENTORY};
    private static final IconPickerMode[] GENERAL_WITH_ENTITY_AND_INVENTORY_CYCLE = {ITEMS, TAGS, FLUIDS, ENTITIES, INVENTORY};

    private final String logName;
    private final String icon;

    IconPickerMode(String logName, String icon) {
        this.logName = logName;
        this.icon = icon;
    }

    String icon() {
        return icon;
    }

    String logName() {
        return logName;
    }

    boolean showingTags() {
        return this == TAGS;
    }

    boolean showingFluids() {
        return this == FLUIDS;
    }

    boolean showingEntities() {
        return this == ENTITIES;
    }

    boolean showingInventory() {
        return this == INVENTORY;
    }

    Component[] tooltip() {
        return new Component[]{Component.translatable("ui.questsandstuff.icon_picker.mode." + logName)};
    }

    static IconPickerMode[] modelItemCycle() {
        return MODEL_ITEM_CYCLE;
    }

    static IconPickerMode[] cycleForContext(boolean supportsEntityIcons, boolean supportsInventoryIcons, boolean useItemPicker) {
        return cycleFor(supportsEntityIcons, supportsInventoryIcons, useItemPicker);
    }

    static int cycleIndex(IconPickerMode mode, IconPickerMode[] cycle) {
        IconPickerMode current = safe(mode);
        IconPickerMode[] safeCycle = cycle == null || cycle.length == 0 ? GENERAL_CYCLE : cycle;
        for (int i = 0; i < safeCycle.length; i++) {
            if (safeCycle[i] == current) {
                return i;
            }
        }
        return 0;
    }

    static String iconAt(IconPickerMode[] cycle, int index) {
        IconPickerMode[] safeCycle = cycle == null || cycle.length == 0 ? GENERAL_CYCLE : cycle;
        return safeCycle[Math.floorMod(index, safeCycle.length)].icon();
    }

    static IconPickerMode safe(IconPickerMode mode) {
        return mode == null ? ITEMS : mode;
    }

    static void normalizeForContext(
            TabletUiState state,
            boolean entityPicker,
            boolean itemModelPicker,
            boolean supportsEntityIcons,
            boolean supportsInventoryIcons,
            boolean useItemPicker
    ) {
        if (state != null) {
            state.pickers.iconMode = normalize(state.pickers.iconMode, entityPicker, itemModelPicker, supportsEntityIcons, supportsInventoryIcons, useItemPicker);
        }
    }

    static void cycleModelItems(TabletUiState state, int direction) {
        if (state == null) {
            return;
        }
        IconPickerMode current = normalize(state.pickers.iconMode, false, true, false, false, false);
        state.pickers.iconMode = cycleIn(current, direction, MODEL_ITEM_CYCLE);
        state.pickers.iconScroll = 0;
    }

    static void cycle(
            TabletUiState state,
            boolean supportsEntityIcons,
            boolean supportsInventoryIcons,
            boolean useItemPicker,
            int direction
    ) {
        if (state == null) {
            return;
        }
        IconPickerMode[] cycle = cycleFor(supportsEntityIcons, supportsInventoryIcons, useItemPicker);
        IconPickerMode current = normalize(state.pickers.iconMode, false, false, supportsEntityIcons, supportsInventoryIcons, useItemPicker);
        state.pickers.iconMode = cycleIn(current, direction, cycle);
        state.pickers.iconScroll = 0;
    }

    public static void reset(TabletUiState state) {
        resetTo(state, ITEMS);
    }

    static void resetForTarget(TabletUiState state, String target) {
        resetTo(state, initialModeForTarget(target));
    }

    static void resetTo(TabletUiState state, IconPickerMode mode) {
        if (state != null) {
            state.pickers.iconMode = safe(mode);
        }
    }

    static boolean isUseItemPickerTarget(ModalTargetParser.Target target) {
        return target != null && target.isTaskSimpleIcon() && "item_use".equals(typePath(target.type()));
    }

    private static IconPickerMode normalize(
            IconPickerMode mode,
            boolean entityPicker,
            boolean itemModelPicker,
            boolean supportsEntityIcons,
            boolean supportsInventoryIcons,
            boolean useItemPicker
    ) {
        if (entityPicker) {
            return ENTITIES;
        }
        IconPickerMode current = safe(mode);
        if (itemModelPicker) {
            return current == TAGS ? TAGS : ITEMS;
        }
        if (useItemPicker) {
            if (current == USABLE_ITEMS || current == ITEMS || current == TAGS || current == FLUIDS) {
                return current;
            }
            return current == INVENTORY && supportsInventoryIcons ? INVENTORY : USABLE_ITEMS;
        }
        if (current == USABLE_ITEMS) {
            return ITEMS;
        }
        if (current == ENTITIES && !supportsEntityIcons) {
            return ITEMS;
        }
        if (current == INVENTORY && !supportsInventoryIcons) {
            return ITEMS;
        }
        return current;
    }

    private static IconPickerMode initialModeForTarget(String target) {
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (parsed.isEntityIconPickerTarget()) {
            return ENTITIES;
        }
        if (isUseItemPickerTarget(parsed)) {
            return USABLE_ITEMS;
        }
        return ITEMS;
    }

    private static IconPickerMode cycleIn(IconPickerMode current, int direction, IconPickerMode[] cycle) {
        int currentIndex = 0;
        for (int i = 0; i < cycle.length; i++) {
            if (cycle[i] == current) {
                currentIndex = i;
                break;
            }
        }
        return cycle[Math.floorMod(currentIndex + direction, cycle.length)];
    }

    private static IconPickerMode[] cycleFor(boolean supportsEntityIcons, boolean supportsInventoryIcons, boolean useItemPicker) {
        if (useItemPicker) {
            return supportsInventoryIcons ? USE_ITEM_WITH_INVENTORY_CYCLE : USE_ITEM_CYCLE;
        }
        if (supportsEntityIcons && supportsInventoryIcons) {
            return GENERAL_WITH_ENTITY_AND_INVENTORY_CYCLE;
        }
        if (supportsEntityIcons) {
            return GENERAL_WITH_ENTITY_CYCLE;
        }
        if (supportsInventoryIcons) {
            return GENERAL_WITH_INVENTORY_CYCLE;
        }
        return GENERAL_CYCLE;
    }

    private static String typePath(String type) {
        String value = type == null ? "" : type.trim();
        int namespaceSeparator = value.indexOf(':');
        return namespaceSeparator >= 0 ? value.substring(namespaceSeparator + 1) : value;
    }
}
