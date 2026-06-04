package com.abo47.questsandstuff.quest.runtime.signal;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class QuestInventoryTasks {
    private static final String FLUID_ICON_PREFIX = "fluid|";

    private QuestInventoryTasks() {
    }

    public static int countItems(ServerPlayer player, String itemId) {
        ResourceLocation target = ResourceLocation.tryParse(itemId);
        if (target == null) {
            return 0;
        }
        return countItems(player, target, "");
    }

    public static int countItems(ServerPlayer player, ResourceLocation target, String nbt) {
        if (player == null || target == null) {
            return 0;
        }
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty() || !QuestItemMatcher.matchesNbt(stack, nbt)) {
                continue;
            }
            ResourceLocation current = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (target.equals(current)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static int countItemsByTag(ServerPlayer player, String tag, String nbt) {
        TagKey<Item> target = itemTag(tag);
        if (player == null || target == null) {
            return 0;
        }
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty() || !QuestItemMatcher.matchesNbt(stack, nbt)) {
                continue;
            }
            if (stack.is(target)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static boolean isFluidIcon(String icon) {
        return icon != null && icon.trim().startsWith(FLUID_ICON_PREFIX);
    }

    public static int countFluidContainers(ServerPlayer player, String fluidIcon) {
        Fluid target = fluidFromIcon(fluidIcon);
        if (player == null || target == Fluids.EMPTY) {
            return 0;
        }
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (containsFluid(stack, target)) {
                count += stack.getCount();
            }
        }
        ItemStack offhand = player.getOffhandItem();
        if (containsFluid(offhand, target)) {
            count += offhand.getCount();
        }
        return count;
    }

    public static boolean itemContainsFluid(String itemId, String fluidIcon) {
        ResourceLocation itemLocation = ResourceLocation.tryParse(itemId == null ? "" : itemId);
        Fluid target = fluidFromIcon(fluidIcon);
        if (itemLocation == null || target == Fluids.EMPTY) {
            return false;
        }
        Item item = BuiltInRegistries.ITEM.get(itemLocation);
        if (item == null || item == Items.AIR) {
            return false;
        }
        return containsFluid(new ItemStack(item), target);
    }

    public static boolean itemKeyInTag(String itemId, String tag) {
        ResourceLocation itemLocation = ResourceLocation.tryParse(itemId == null ? "" : itemId);
        TagKey<Item> target = itemTag(tag);
        if (itemLocation == null || target == null) {
            return false;
        }
        Item item = BuiltInRegistries.ITEM.get(itemLocation);
        return item.builtInRegistryHolder().is(target);
    }

    public static int consumeItems(ServerPlayer player, String itemId, int max) {
        ResourceLocation target = ResourceLocation.tryParse(itemId);
        return consumeItems(player, target, "", max);
    }

    public static int consumeItems(ServerPlayer player, ResourceLocation target, String nbt, int max) {
        if (target == null || max <= 0) {
            return 0;
        }
        int consumed = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (consumed >= max) {
                break;
            }
            ResourceLocation current = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!target.equals(current)) {
                continue;
            }
            if (!QuestItemMatcher.matchesNbt(stack, nbt)) {
                continue;
            }
            int take = Math.min(max - consumed, stack.getCount());
            stack.shrink(take);
            consumed += take;
        }
        return consumed;
    }

    public static int consumeItemsByTag(ServerPlayer player, String tag, String nbt, int max) {
        TagKey<Item> target = itemTag(tag);
        if (player == null || target == null || max <= 0) {
            return 0;
        }
        int consumed = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (consumed >= max) {
                break;
            }
            if (!stack.is(target) || !QuestItemMatcher.matchesNbt(stack, nbt)) {
                continue;
            }
            int take = Math.min(max - consumed, stack.getCount());
            stack.shrink(take);
            consumed += take;
        }
        return consumed;
    }

    private static TagKey<Item> itemTag(String tag) {
        String normalized = tag == null ? "" : tag.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        ResourceLocation id = ResourceLocation.tryParse(normalized);
        return id == null ? null : TagKey.create(Registries.ITEM, id);
    }

    private static Fluid fluidFromIcon(String icon) {
        String normalized = icon == null ? "" : icon.trim();
        if (normalized.startsWith(FLUID_ICON_PREFIX)) {
            normalized = normalized.substring(FLUID_ICON_PREFIX.length()).trim();
        }
        ResourceLocation id = ResourceLocation.tryParse(normalized);
        if (id == null) {
            return Fluids.EMPTY;
        }
        Fluid fluid = BuiltInRegistries.FLUID.get(id);
        return fluid == null ? Fluids.EMPTY : fluid;
    }

    private static boolean containsFluid(ItemStack stack, Fluid target) {
        if (stack == null || stack.isEmpty() || target == null || target == Fluids.EMPTY) {
            return false;
        }
        try {
            FluidStack contained = FluidTransferHelper.getFluidContained(stack);
            return contained != null && !contained.isEmpty() && contained.getFluid() == target;
        } catch (LinkageError | RuntimeException ignored) {
            return false;
        }
    }
}
