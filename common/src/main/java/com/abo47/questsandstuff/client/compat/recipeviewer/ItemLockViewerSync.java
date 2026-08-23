package com.abo47.questsandstuff.client.compat.recipeviewer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.quest.lock.ClientItemLocks;

public final class ItemLockViewerSync {
    private static final int ATTEMPT_INTERVAL_TICKS = 40;
    private static Set<String> appliedHidden;
    private static int ticksUntilAttempt;

    private ItemLockViewerSync() {
    }

    public static void reset() {
        appliedHidden = null;
        ticksUntilAttempt = 0;
    }

    public static void tick() {
        if (!ClientItemLocks.anyLocks()) {
            if (appliedHidden != null && !appliedHidden.isEmpty()) {
                if (revealAll()) {
                    appliedHidden = Set.of();
                }
            }
            return;
        }
        if (ticksUntilAttempt > 0) {
            ticksUntilAttempt--;
            return;
        }
        ticksUntilAttempt = ATTEMPT_INTERVAL_TICKS;
        Set<String> current = ClientItemLocks.entries();
        if (current.equals(appliedHidden)) {
            return;
        }
        List<ItemStack> hide = stacksFor(difference(current, appliedHidden));
        List<ItemStack> show = stacksFor(difference(appliedHidden, current));
        if (apply(hide, show)) {
            appliedHidden = Set.copyOf(current);
            QuestsAndStuffMod.debugLog("[QnS:Lock] viewer sync applied hidden={} shown={}", hide.size(), show.size());
        }
    }

    private static Set<String> difference(Set<String> from, Set<String> remove) {
        if (from == null || from.isEmpty()) {
            return Set.of();
        }
        Set<String> result = new HashSet<>(from);
        if (remove != null) {
            result.removeAll(remove);
        }
        return result;
    }

    private static List<ItemStack> stacksFor(Set<String> entries) {
        List<ItemStack> stacks = new ArrayList<>();
        for (String entry : entries) {
            ItemStack stack = stackFor(entry);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    private static ItemStack stackFor(String entry) {
        if (entry == null || entry.startsWith("#") || entry.isBlank()) {
            return ItemStack.EMPTY;
        }
        try {
            ResourceLocation id = ResourceLocation.tryParse(entry);
            if (id == null) {
                return ItemStack.EMPTY;
            }
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item == Items.AIR || !BuiltInRegistries.ITEM.getKey(item).equals(id)) {
                return ItemStack.EMPTY;
            }
            return new ItemStack(item);
        } catch (Exception error) {
            return ItemStack.EMPTY;
        }
    }

    private static boolean apply(List<ItemStack> hide, List<ItemStack> show) {
        boolean any = false;
        any |= emiReload();
        any |= jeiApply(hide, show);
        any |= reiApply(hide, show);
        return any;
    }

    private static boolean revealAll() {
        return apply(List.of(), stacksFor(appliedHidden == null ? Set.of() : appliedHidden));
    }

    private static boolean emiReload() {
        try {
            Class<?> reloadManager = Class.forName("dev.emi.emi.runtime.EmiReloadManager");
            Method reload = reloadManager.getMethod("reload");
            reload.invoke(null);
            return true;
        } catch (ClassNotFoundException notInstalled) {
            return false;
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.debug("[QnS:Lock] EMI refresh failed", error);
            return false;
        }
    }

    private static boolean jeiApply(List<ItemStack> hide, List<ItemStack> show) {
        try {
            Class<?> internal = Class.forName("mezz.jei.common.Internal");
            Object runtime = internal.getMethod("getJeiRuntime").invoke(null);
            if (runtime == null) {
                return false;
            }
            Class<?> runtimeClass = Class.forName("mezz.jei.api.runtime.IJeiRuntime");
            Object manager = runtimeClass.getMethod("getIngredientManager").invoke(runtime);
            Class<?> managerClass = Class.forName("mezz.jei.api.runtime.IIngredientManager");
            Object itemType = vanillaItemStackType();
            Method remove = managerClass.getMethod("removeIngredientsAtRuntime", itemType.getClass(), java.util.Collection.class);
            Method add = managerClass.getMethod("addIngredientsAtRuntime", itemType.getClass(), java.util.Collection.class);
            if (!hide.isEmpty()) {
                remove.invoke(manager, itemType, hide);
            }
            if (!show.isEmpty()) {
                add.invoke(manager, itemType, show);
            }
            return true;
        } catch (ClassNotFoundException notInstalled) {
            return false;
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.debug("[QnS:Lock] JEI sync failed", error);
            return false;
        }
    }

    private static boolean reiApply(List<ItemStack> hide, List<ItemStack> show) {
        try {
            Class<?> registryClass = Class.forName("me.shedaniel.rei.api.client.registry.entry.EntryRegistry");
            Object registry = registryClass.getMethod("getInstance").invoke(null);
            if (registry == null) {
                return false;
            }
            Class<?> entryStacks = Class.forName("me.shedaniel.rei.api.common.util.EntryStacks");
            Method of = entryStacks.getMethod("of", ItemStack.class);
            if (!hide.isEmpty()) {
                Method removeEntry = registryClass.getMethod("removeEntry", Class.forName("me.shedaniel.rei.api.common.entry.EntryStack"));
                for (ItemStack stack : hide) {
                    removeEntry.invoke(registry, of.invoke(null, stack));
                }
            }
            if (!show.isEmpty()) {
                List<Object> stacks = new ArrayList<>();
                for (ItemStack stack : show) {
                    stacks.add(of.invoke(null, stack));
                }
                registryClass.getMethod("addEntries", java.util.Collection.class).invoke(registry, stacks);
            }
            return true;
        } catch (ClassNotFoundException notInstalled) {
            return false;
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.debug("[QnS:Lock] REI sync failed", error);
            return false;
        }
    }

    private static Object vanillaItemStackType() throws Exception {
        Class<?> vanillaTypes = Class.forName("mezz.jei.api.constants.VanillaTypes");
        Field field = vanillaTypes.getField("ITEM_STACK");
        return field.get(null);
    }
}
