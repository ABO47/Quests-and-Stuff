package com.abo47.questsandstuff.client.compat.recipeviewer;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public interface LockViewerBridge {
    boolean applyIngredientDeltas(List<ItemStack> hide, List<ItemStack> show);

    boolean applyRecipeStates(List<Recipe<?>> hide, List<Recipe<?>> show);
}
