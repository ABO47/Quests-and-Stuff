package com.abo47.questsandstuff.client.compat.recipeviewer;

public enum RecipeViewerCapability {
    AVAILABLE("available"),
    SHOW_RECIPES("show_recipes"),
    SHOW_USES("show_uses"),
    NATIVE_SELECTION("native_selection"),
    RECIPE_KEYBIND("recipe_keybind"),
    USES_KEYBIND("uses_keybind"),
    SNAPSHOT_RENDERING("snapshot_rendering"),
    VISIBLE_RECIPE_PICK("visible_recipe_pick"),
    FLUID_ENTRIES("fluid_entries");

    private final String id;

    RecipeViewerCapability(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
