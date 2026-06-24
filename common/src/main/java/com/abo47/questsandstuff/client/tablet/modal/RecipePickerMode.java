package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.network.chat.Component;

public enum RecipePickerMode {
    ITEMS("items", "mode_items"),
    TAGS("tags", "mode_tags"),
    FLUIDS("fluids", "mode_fluids"),
    INVENTORY("inventory", "mode_inventory");

    private static final RecipePickerMode[] CYCLE = values();

    private final String logName;
    private final String icon;

    RecipePickerMode(String logName, String icon) {
        this.logName = logName;
        this.icon = icon;
    }

    RecipePickerMode cycle(int direction) {
        return CYCLE[Math.floorMod(ordinal() + direction, CYCLE.length)];
    }

    static int cycleIndex(RecipePickerMode mode) {
        return safe(mode).ordinal();
    }

    static int cycleSize() {
        return CYCLE.length;
    }

    static String iconAt(int index) {
        return CYCLE[Math.floorMod(index, CYCLE.length)].icon();
    }

    String icon() {
        return icon;
    }

    String logName(String query) {
        if (this == ITEMS && query != null && query.trim().startsWith("#")) {
            return TAGS.logName;
        }
        return logName;
    }

    boolean showingTags(String query) {
        return this == TAGS || this == ITEMS && query != null && query.trim().startsWith("#");
    }

    Component[] tooltip(String query) {
        return new Component[]{Component.translatable("ui.questsandstuff.recipe_picker.mode." + logName(query))};
    }

    static RecipePickerMode safe(RecipePickerMode mode) {
        return mode == null ? ITEMS : mode;
    }

    static void cycle(TabletUiState state, int direction) {
        if (state == null) {
            return;
        }
        state.pickers.recipeMode = safe(state.pickers.recipeMode).cycle(direction);
        state.pickers.recipeScroll = 0;
    }

    public static void reset(TabletUiState state) {
        if (state != null) {
            state.pickers.recipeMode = ITEMS;
        }
    }
}
