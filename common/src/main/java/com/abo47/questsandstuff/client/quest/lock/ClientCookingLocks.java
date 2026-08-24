package com.abo47.questsandstuff.client.quest.lock;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class ClientCookingLocks {
    private ClientCookingLocks() {
    }

    public static boolean inputBlocked(ItemStack input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null || !ClientItemLocks.anyLocks()) {
            return false;
        }
        try {
            SingleStackGrid grid = new SingleStackGrid(input);
            var manager = level.getRecipeManager();
            for (var recipe : manager.getAllRecipesFor(RecipeType.SMELTING)) {
                if (cookingLocked(recipe, grid, level)) {
                    return true;
                }
            }
            for (var recipe : manager.getAllRecipesFor(RecipeType.SMOKING)) {
                if (cookingLocked(recipe, grid, level)) {
                    return true;
                }
            }
            for (var recipe : manager.getAllRecipesFor(RecipeType.BLASTING)) {
                if (cookingLocked(recipe, grid, level)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    public static boolean campfireOutputBlocked(ItemStack input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null || !ClientItemLocks.anyLocks()) {
            return false;
        }
        try {
            SingleStackGrid grid = new SingleStackGrid(input);
            for (var recipe : level.getRecipeManager().getAllRecipesFor(RecipeType.CAMPFIRE_COOKING)) {
                if (!recipe.matches(grid, level)) {
                    continue;
                }
                ItemStack output = recipe.assemble(grid, level.registryAccess());
                if (!output.isEmpty() && ClientItemLocks.isLocked(output)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    private static boolean cookingLocked(AbstractCookingRecipe recipe, SingleStackGrid grid, Level level) {
        if (!recipe.matches(grid, level)) {
            return false;
        }
        ItemStack output = recipe.assemble(grid, level.registryAccess());
        return !output.isEmpty() && ClientItemLocks.isLocked(output);
    }

    private record SingleStackGrid(ItemStack stack) implements Container {
        @Override
        public int getContainerSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return stack.isEmpty();
        }

        @Override
        public ItemStack getItem(int index) {
            return index == 0 ? stack : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int index, ItemStack value) {
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player accessPlayer) {
            return true;
        }

        @Override
        public void clearContent() {
        }
    }
}
