package com.abo47.questsandstuff.fabric.compat.recipeviewer.jei;

import java.util.List;
import java.util.Optional;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.compat.recipeviewer.LockViewerBridge;

public final class FabricJeiGateBridge implements LockViewerBridge {
    private IJeiRuntime runtime;

    public void onRuntimeAvailable(IJeiRuntime runtime) {
        this.runtime = runtime;
        QuestsAndStuffMod.LOGGER.info("[QnS:Lock] JEI runtime handed to lock sync");
    }

    public void onRuntimeUnavailable() {
        this.runtime = null;
    }

    @Override
    public boolean applyIngredientDeltas(List<ItemStack> hide, List<ItemStack> show) {
        if (runtime == null) {
            return false;
        }
        var manager = runtime.getIngredientManager();
        if (!hide.isEmpty()) {
            manager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, hide);
        }
        if (!show.isEmpty()) {
            manager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, show);
        }
        return true;
    }

    @Override
    public boolean applyRecipeStates(List<Recipe<?>> hide, List<Recipe<?>> show) {
        if (runtime == null || (hide.isEmpty() && show.isEmpty())) {
            return false;
        }
        IRecipeManager manager = runtime.getRecipeManager();
        int changed = 0;
        for (Recipe<?> recipe : hide) {
            if (setState(manager, recipe, true)) {
                changed++;
            }
        }
        for (Recipe<?> recipe : show) {
            if (setState(manager, recipe, false)) {
                changed++;
            }
        }
        QuestsAndStuffMod.LOGGER.info(
                "[QnS:Lock] JEI lock recipes updated, {} state change(s)", changed);
        return changed > 0;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean setState(IRecipeManager manager, Recipe<?> recipe, boolean hide) {
        ResourceLocation uid = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
        if (uid == null) {
            return false;
        }
        Optional<RecipeType<?>> typeOpt = manager.getRecipeType(uid);
        if (typeOpt.isEmpty()) {
            return false;
        }
        RecipeType type = typeOpt.get();
        Class recipeClass = type.getRecipeClass();
        if (!recipeClass.isInstance(recipe)) {
            return false;
        }
        List typed = List.of(recipeClass.cast(recipe));
        if (hide) {
            manager.hideRecipes(type, typed);
        } else {
            manager.unhideRecipes(type, typed);
        }
        return true;
    }
}
