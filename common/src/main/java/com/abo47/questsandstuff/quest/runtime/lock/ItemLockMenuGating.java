package com.abo47.questsandstuff.quest.runtime.lock;

import java.lang.reflect.Field;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.abo47.questsandstuff.QuestsAndStuffMod;

public final class ItemLockMenuGating {
    private ItemLockMenuGating() {
    }

    public static void gateCraftingMenu(Player player, AbstractContainerMenu menu) {
        String simpleName = menu.getClass().getSimpleName();
        if (simpleName.equals("FurnaceMenu")) {
            gateFurnaceMenu(player, menu);
            return;
        }
        if (simpleName.equals("StonecutterMenu")) {
            gateResultContainer(player, menu, 1);
            return;
        }
        if (simpleName.equals("SmithingMenu")) {
            gateResultContainer(player, menu, 3);
            return;
        }
        if (!simpleName.equals("CraftingMenu") && !simpleName.equals("InventoryMenu")) {
            return;
        }
        if (menu.slots.isEmpty() || menu.slots.get(0) instanceof GateResultSlot) {
            return;
        }
        try {
            Field resultField = findResultContainerField(menu.getClass());
            if (resultField == null) {
                QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] no ResultContainer field on {}", simpleName);
                return;
            }
            resultField.setAccessible(true);
            ResultContainer original = (ResultContainer) resultField.get(menu);
            GateResultContainer gatedContainer = new GateResultContainer(original, player);
            resultField.set(menu, gatedContainer);

            Slot originalSlot = menu.slots.get(0);
            menu.slots.set(0, new GateResultSlot(gatedContainer, originalSlot));
            QuestsAndStuffMod.LOGGER.info("[QnS:Lock] gated crafting menu {}", simpleName);
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] menu gating failed for {}", simpleName, error);
        }
    }

    private static Field findResultContainerField(Class<?> type) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.getType().getSimpleName().equals("ResultContainer")) {
                    return field;
                }
            }
        }
        return null;
    }

    private static void gateResultContainer(Player player, AbstractContainerMenu menu, int resultSlotIndex) {
        if (resultSlotIndex >= menu.slots.size() || menu.slots.get(resultSlotIndex) instanceof GateResultSlot) {
            return;
        }
        try {
            Field resultField = findResultContainerField(menu.getClass());
            if (resultField == null) {
                QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] no ResultContainer field on {}", menu.getClass().getSimpleName());
                return;
            }
            resultField.setAccessible(true);
            ResultContainer original = (ResultContainer) resultField.get(menu);
            GateResultContainer gatedContainer = new GateResultContainer(original, player);
            resultField.set(menu, gatedContainer);

            Slot originalSlot = menu.slots.get(resultSlotIndex);
            menu.slots.set(resultSlotIndex, new GateResultSlot(gatedContainer, originalSlot));
            QuestsAndStuffMod.LOGGER.info("[QnS:Lock] gated {} result container", menu.getClass().getSimpleName());
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] result container gating failed for {}", menu.getClass().getSimpleName(), error);
        }
    }

    private static void gateFurnaceMenu(Player player, AbstractContainerMenu menu) {
        for (int index = 0; index < menu.slots.size(); index++) {
            Slot slot = menu.slots.get(index);
            String slotClass = slot.getClass().getSimpleName();
            if (slotClass.equals("FurnaceResultSlot") && !(slot instanceof GateDelegatedSlot)) {
                menu.slots.set(index, new GateDelegatedSlot(slot, player));
                QuestsAndStuffMod.LOGGER.info("[QnS:Lock] gated furnace result slot");
            } else if (index == 0 && !(slot instanceof GateSmeltingInputSlot)) {
                menu.slots.set(index, new GateSmeltingInputSlot(slot, player));
                QuestsAndStuffMod.LOGGER.info("[QnS:Lock] gated furnace input slot");
            }
        }
    }

    private static class GateSmeltingInputSlot extends Slot {
        private final Slot originalSlot;
        private final Player player;

        GateSmeltingInputSlot(Slot originalSlot, Player player) {
            super(originalSlot.container, originalSlot.getContainerSlot(), originalSlot.x, originalSlot.y);
            this.originalSlot = originalSlot;
            this.player = player;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (!originalSlot.mayPlace(stack)) {
                return false;
            }
            return !ItemLockEnforcement.smeltingOutputLocked(player, stack);
        }

        @Override
        public ItemStack getItem() {
            return originalSlot.getItem();
        }

        @Override
        public int getMaxStackSize() {
            return originalSlot.getMaxStackSize();
        }
    }

    private static class GateDelegatedSlot extends Slot {
        private final Slot originalSlot;
        private final Player player;

        GateDelegatedSlot(Slot originalSlot, Player player) {
            super(originalSlot.container, originalSlot.getContainerSlot(), originalSlot.x, originalSlot.y);
            this.originalSlot = originalSlot;
            this.player = player;
        }

        @Override
        public boolean mayPickup(Player player) {
            ItemStack stack = getItem();
            if (!stack.isEmpty() && ItemLockEnforcement.isLocked(player, stack)) {
                return false;
            }
            return originalSlot.mayPickup(player);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            if (ItemLockEnforcement.isLocked(player, stack)) {
                return;
            }
            originalSlot.onTake(player, stack);
        }

        @Override
        public ItemStack getItem() {
            return originalSlot.getItem();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return originalSlot.mayPlace(stack);
        }

        @Override
        public int getMaxStackSize() {
            return originalSlot.getMaxStackSize();
        }
    }

    private static class GateResultContainer extends ResultContainer {
        private final ResultContainer original;
        private final Player player;

        GateResultContainer(ResultContainer original, Player player) {
            this.original = original;
            this.player = player;
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            if (!stack.isEmpty() && ItemLockEnforcement.isLocked(player, stack)) {
                stack = ItemStack.EMPTY;
            }
            original.setItem(index, stack);
        }

        @Override
        public ItemStack getItem(int index) {
            return original.getItem(index);
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            return original.removeItem(index, count);
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            return original.removeItemNoUpdate(index);
        }

        @Override
        public void setChanged() {
            original.setChanged();
        }

        @Override
        public boolean stillValid(Player accessPlayer) {
            return original.stillValid(accessPlayer);
        }

        @Override
        public int getContainerSize() {
            return original.getContainerSize();
        }

        @Override
        public boolean isEmpty() {
            return original.isEmpty();
        }

        @Override
        public void clearContent() {
            original.clearContent();
        }
    }

    private static class GateResultSlot extends Slot {
        private final Slot originalSlot;

        GateResultSlot(GateResultContainer container, Slot originalSlot) {
            super(container, originalSlot.getContainerSlot(), originalSlot.x, originalSlot.y);
            this.originalSlot = originalSlot;
        }

        @Override
        public boolean mayPickup(Player player) {
            return originalSlot.mayPickup(player);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            originalSlot.onTake(player, stack);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return originalSlot.mayPlace(stack);
        }

        @Override
        public int getMaxStackSize() {
            return originalSlot.getMaxStackSize();
        }
    }
}
