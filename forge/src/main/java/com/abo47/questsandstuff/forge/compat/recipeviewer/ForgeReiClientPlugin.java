package com.abo47.questsandstuff.forge.compat.recipeviewer;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.forge.REIPluginClient;

@REIPluginClient
public final class ForgeReiClientPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        // Selection buttons are drawn by the shared screen overlay so Forge and Fabric use the same control.
    }
}
