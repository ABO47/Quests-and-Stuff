package com.abo47.questsandstuff.quest.runtime.lock;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record GatedCraftingRecipe(CraftingRecipe original) implements CraftingRecipe {
    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        return original.matches(inv, level);
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack output = original.assemble(inv, registryAccess);
        Player craftingPlayer = ItemLockHooks.resolveCraftingPlayer(inv);
        if (craftingPlayer != null && ItemLockEnforcement.isLocked(craftingPlayer, output)) {
            return ItemStack.EMPTY;
        }
        return output;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return original.getRemainingItems(inv);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return original.getIngredients();
    }

    @Override
    public boolean isIncomplete() {
        return original.isIncomplete();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return original.canCraftInDimensions(width, height);
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return original.getResultItem(registryAccess);
    }

    @Override
    public RecipeType<?> getType() {
        return original.getType();
    }

    @Override
    public CraftingBookCategory category() {
        return original.category();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return original.getSerializer();
    }

    @Override
    public ResourceLocation getId() {
        return original.getId();
    }

    @Override
    public String getGroup() {
        return original.getGroup();
    }

    @Override
    public ItemStack getToastSymbol() {
        return original.getToastSymbol();
    }
}
