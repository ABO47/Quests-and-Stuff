package com.abo47.questsandstuff.quest.runtime.lock;

import java.util.Set;
import java.util.function.BiPredicate;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.quest.lock.ClientCrafterLookup;
import com.abo47.questsandstuff.client.quest.lock.ClientItemLocks;

public final class LockedCraftingRecipe implements CraftingRecipe {
    private static final Set<String> WARNED_CONTEXT =
            java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());
    static volatile BiPredicate<Player, ItemStack> gatePolicyOverride;

    private final CraftingRecipe inner;

    public LockedCraftingRecipe(CraftingRecipe inner) {
        this.inner = inner;
    }

    public CraftingRecipe inner() {
        return inner;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        if (!ItemLockEnforcement.locksActive()) {
            return inner.matches(inv, level);
        }
        Player crafter = level.isClientSide
                ? ClientCrafterLookup.byGrid(inv)
                : OpenMenuIndex.resolveByGrid(inv);
        if (crafter == null) {
            warnNoContextOnce();
            return false;
        }
        if (!inner.matches(inv, level)) {
            return false;
        }
        ItemStack output = inner.getResultItem(level.registryAccess());
        if (output.isEmpty()) {
            return true;
        }
        return !denies(crafter, output);
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack output = inner.assemble(inv, registryAccess);
        if (!ItemLockEnforcement.locksActive() || output.isEmpty()) {
            return output;
        }
        Player crafter = OpenMenuIndex.resolveByGrid(inv);
        if (crafter == null) {
            warnNoContextOnce();
            return ItemStack.EMPTY;
        }
        return denies(crafter, output) ? ItemStack.EMPTY : output;
    }

    static boolean denies(Player player, ItemStack output) {
        BiPredicate<Player, ItemStack> override = gatePolicyOverride;
        if (override != null) {
            return override.test(player, output);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            return ItemLockEnforcement.isLocked(serverPlayer, output);
        }
        return player.level().isClientSide && ClientItemLocks.isLocked(output);
    }

    private void warnNoContextOnce() {
        String id = String.valueOf(inner.getId());
        if (WARNED_CONTEXT.add(id)) {
            QuestsAndStuffMod.LOGGER.warn(
                    "[QnS:Lock] no crafter context for gated recipe {}, failing closed", id);
        }
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return inner.getRemainingItems(inv);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inner.getIngredients();
    }

    @Override
    public boolean isIncomplete() {
        return inner.isIncomplete();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return inner.canCraftInDimensions(width, height);
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return inner.getResultItem(registryAccess);
    }

    @Override
    public ItemStack getToastSymbol() {
        return inner.getToastSymbol();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return LockedRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return inner.getType();
    }

    @Override
    public ResourceLocation getId() {
        return inner.getId();
    }

    @Override
    public String getGroup() {
        return inner.getGroup();
    }

    @Override
    public CraftingBookCategory category() {
        return inner.category();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LockedCraftingRecipe other && other.inner.getId().equals(inner.getId());
    }

    @Override
    public int hashCode() {
        return inner.getId().hashCode();
    }

    @Override
    public String toString() {
        return "Locked(" + inner.getId() + ")";
    }
}
