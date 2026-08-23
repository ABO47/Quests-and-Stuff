package com.abo47.questsandstuff.quest.runtime.lock;

import java.util.Iterator;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;

public final class ItemLockEnforcement {
    private static volatile boolean hookSeen;
    private static long lastRevertLogMs;

    private ItemLockEnforcement() {
    }

    private static void reportHook() {
        if (!hookSeen) {
            hookSeen = true;
            QuestsAndStuffMod.LOGGER.info("[QnS:Lock] enforcement active (first lock check ran)");
        }
    }

    public static boolean isLocked(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || player.level().isClientSide
                || stack == null
                || stack.isEmpty()
                || player.getAbilities().instabuild) {
            return false;
        }
        reportHook();
        try {
            return QuestServiceRegistry.engine(serverPlayer.server).isItemLocked(serverPlayer, stack);
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] lock check failed for {}", stack.getItem(), error);
            return false;
        }
    }

    public static void filterLoot(ObjectArrayList<ItemStack> drops, LootParams params) {
        if (drops == null || drops.isEmpty()) {
            return;
        }
        reportHook();
        try {
            ServerPlayer player = params.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer serverPlayer
                    ? serverPlayer
                    : null;
            ServerLevel level = params.getLevel();
            boolean creative = player != null && player.getAbilities().instabuild;
            if (!QuestServiceRegistry.engine(level.getServer()).itemLockIndexHasLocks()) {
                return;
            }
            Iterator<ItemStack> iterator = drops.iterator();
            while (iterator.hasNext()) {
                ItemStack stack = iterator.next();
                if (stack.isEmpty()) {
                    continue;
                }
                if (creative) {
                    continue;
                }
                boolean locked = player == null
                        ? QuestServiceRegistry.engine(level.getServer()).itemLockExists(stack)
                        : QuestServiceRegistry.engine(level.getServer()).isItemLocked(player, stack);
                if (locked) {
                    iterator.remove();
                }
            }
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.debug("[QnS:Lock] loot filter failed", error);
        }
    }

    public static boolean smeltingOutputLocked(Player player, ItemStack input) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || player.level().isClientSide
                || input == null
                || input.isEmpty()
                || player.getAbilities().instabuild) {
            return false;
        }
        try {
            var manager = serverPlayer.level().getRecipeManager();
            SingleIngredientGrid grid = new SingleIngredientGrid(input);
            for (var recipe : manager.getAllRecipesFor(RecipeType.SMELTING)) {
                if (recipe.matches(grid, serverPlayer.level())) {
                    return isLocked(player, recipe.assemble(grid, serverPlayer.level().registryAccess()));
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private record SingleIngredientGrid(ItemStack stack) implements Container {
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

    public static boolean blockResultTake(Player player, Container container, int slotIndex, ItemStack taken) {
        if (!isLocked(player, taken)) {
            return false;
        }
        ItemStack current = container.getItem(slotIndex);
        if (current.isEmpty()) {
            container.setItem(slotIndex, taken.copy());
        } else {
            current.grow(taken.getCount());
            container.setChanged();
        }
        return true;
    }

    public static void undoLockedCraft(Player player, ItemStack crafted, Container craftMatrix) {
        if (!isLocked(player, crafted)) {
            return;
        }
        removeCraftedStacks(player, crafted);
        clearCarried(player, crafted);
        refundGrid(player, craftMatrix);
        long now = System.currentTimeMillis();
        if (now - lastRevertLogMs >= 2000L) {
            lastRevertLogMs = now;
            QuestsAndStuffMod.LOGGER.info(
                    "[QnS:Lock] reverted locked craft of {} by {}",
                    crafted.getItem(), player.getName().getString());
        }
    }

    private static void clearCarried(Player player, ItemStack crafted) {
        ItemStack carried = player.containerMenu.getCarried();
        if (!carried.isEmpty() && ItemStack.isSameItemSameTags(carried, crafted)) {
            carried.shrink(crafted.getCount());
            if (carried.isEmpty()) {
                player.containerMenu.setCarried(ItemStack.EMPTY);
            }
            player.containerMenu.broadcastChanges();
        }
    }

    private static void removeCraftedStacks(Player player, ItemStack crafted) {
        int remaining = crafted.getCount();
        var inventory = player.getInventory();
        for (int index = 0; index < inventory.getContainerSize() && remaining > 0; index++) {
            ItemStack stack = inventory.getItem(index);
            if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, crafted)) {
                int removed = Math.min(remaining, stack.getCount());
                stack.shrink(removed);
                remaining -= removed;
                if (stack.isEmpty()) {
                    inventory.setItem(index, ItemStack.EMPTY);
                }
            }
        }
    }

    private static void refundGrid(Player player, Container craftMatrix) {
        for (int index = 0; index < craftMatrix.getContainerSize(); index++) {
            ItemStack ingredient = craftMatrix.getItem(index);
            if (ingredient.isEmpty() || ingredient.getItem().hasCraftingRemainingItem()) {
                continue;
            }
            ItemStack refund = ingredient.copyWithCount(1);
            if (!player.getInventory().add(refund)) {
                player.drop(refund, false);
            }
        }
    }
}
