package com.abo47.questsandstuff.client.compat.recipeviewer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.tags.TagKey;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.quest.lock.ClientItemLocks;
import com.abo47.questsandstuff.client.quest.lock.ClientLockEvents;
import com.abo47.questsandstuff.client.quest.lock.ClientBookFilter;
import com.abo47.questsandstuff.client.quest.lock.LockClientRefresh;
import com.abo47.questsandstuff.quest.runtime.lock.LockedCraftingRecipe;

public final class ItemLockViewerSync implements ClientLockEvents.Listener {
    private static final int EMI_DEBOUNCE_TICKS = 40;
    private static final Set<String> WARNED_MESSAGES = new HashSet<>();
    private static final ItemLockViewerSync INSTANCE = new ItemLockViewerSync();

    private Set<String> appliedHidden = null;
    private Set<ResourceLocation> appliedHiddenRecipes = Set.of();
    private boolean emiDirty = false;
    private int emiCooldownTicks = 0;
    private boolean registered;

    private ItemLockViewerSync() {
    }

    public static void ensureSubscribed() {
        if (!INSTANCE.registered) {
            INSTANCE.registered = true;
            ClientLockEvents.register(INSTANCE);
        }
    }

    public static void reset() {
        INSTANCE.appliedHidden = null;
        INSTANCE.appliedHiddenRecipes = Set.of();
        INSTANCE.emiDirty = false;
        INSTANCE.emiCooldownTicks = 0;
        INSTANCE.registered = false;
        ClientLockEvents.unregister(INSTANCE);
        LockClientRefresh.reset();
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LockClientRefresh.tickClient(minecraft);
        INSTANCE.tickEmiDebounce();
    }

    @Override
    public void onLockStatesChanged() {
        applyCurrentState();
    }

    private void applyCurrentState() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Set<String> current = ClientItemLocks.entries();
        if (current.equals(appliedHidden)) {
            refreshBook(minecraft);
            return;
        }
        List<ItemStack> hide = stacksFor(difference(current, appliedHidden));
        List<ItemStack> show = stacksFor(difference(appliedHidden, current));
        List<Recipe<?>> hideRecipes = lockedRecipes(minecraft);
        List<Recipe<?>> showRecipes = shownRecipes(appliedHiddenRecipes);

        boolean anyViewer = emiMarkDirty();
        anyViewer |= jeiApply(hide, show, hideRecipes, showRecipes);
        anyViewer |= reiApply(hide, show);

        appliedHidden = Set.copyOf(current);
        appliedHiddenRecipes = idsOf(hideRecipes);
        QuestsAndStuffMod.LOGGER.info(
                "[QnS:Lock] viewer sync applied items hidden={} shown={}, recipes gated={} (viewers reachable={})",
                hide.size(), show.size(), hideRecipes.size(), anyViewer);
        refreshBook(minecraft);
    }

    private void refreshBook(Minecraft minecraft) {
        if (minecraft.player != null) {
            ClientBookFilter.refresh(minecraft.player.getRecipeBook());
        }
    }

    private void tickEmiDebounce() {
        if (emiCooldownTicks > 0) {
            emiCooldownTicks--;
        }
        if (emiDirty && emiCooldownTicks == 0) {
            emiDirty = false;
            emiCooldownTicks = EMI_DEBOUNCE_TICKS;
            if (emiReload()) {
                QuestsAndStuffMod.LOGGER.info("[QnS:Lock] EMI reload flushed for lock change");
            }
        }
    }

    private boolean emiMarkDirty() {
        emiDirty = true;
        return true;
    }

    private static List<Recipe<?>> lockedRecipes(Minecraft minecraft) {
        RegistryAccess access = minecraft.level.registryAccess();
        List<Recipe<?>> result = new ArrayList<>();
        for (CraftingRecipe recipe : minecraft.level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            if (!(recipe instanceof LockedCraftingRecipe locked)) {
                continue;
            }
            ItemStack output = locked.inner().getResultItem(access);
            if (!output.isEmpty() && ClientItemLocks.isLocked(output)) {
                result.add(recipe);
            }
        }
        return result;
    }

    private List<Recipe<?>> shownRecipes(Set<ResourceLocation> previouslyHidden) {
        if (previouslyHidden.isEmpty()) {
            return List.of();
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return List.of();
        }
        RegistryAccess access = minecraft.level.registryAccess();
        List<Recipe<?>> result = new ArrayList<>();
        for (CraftingRecipe recipe : minecraft.level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            if (!(recipe instanceof LockedCraftingRecipe locked)) {
                continue;
            }
            if (!previouslyHidden.contains(locked.getId())) {
                continue;
            }
            ItemStack output = locked.inner().getResultItem(access);
            if (output.isEmpty() || !ClientItemLocks.isLocked(output)) {
                result.add(recipe);
            }
        }
        return result;
    }

    private static Set<ResourceLocation> idsOf(List<Recipe<?>> recipes) {
        if (recipes.isEmpty()) {
            return Set.of();
        }
        Set<ResourceLocation> ids = new HashSet<>();
        for (Recipe<?> recipe : recipes) {
            ids.add(recipe.getId());
        }
        return ids;
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
            appendStacks(entry, stacks);
        }
        return stacks;
    }

    private static void appendStacks(String entry, List<ItemStack> stacks) {
        if (entry == null || entry.isBlank()) {
            return;
        }
        if (!entry.startsWith("#")) {
            ItemStack direct = directStack(entry);
            if (!direct.isEmpty()) {
                stacks.add(direct);
            }
            return;
        }
        ResourceLocation tagId = ResourceLocation.tryParse(entry.substring(1));
        if (tagId == null) {
            return;
        }
        var tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), tagId);
        BuiltInRegistries.ITEM.getTag(tagKey).ifPresent(named -> {
            for (var holder : named) {
                ItemStack stack = new ItemStack(holder.value());
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
        });
    }

    private static ItemStack directStack(String entry) {
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

    private static boolean jeiApply(
            List<ItemStack> hideItems,
            List<ItemStack> showItems,
            List<Recipe<?>> hideRecipes,
            List<Recipe<?>> showRecipes) {
        LockViewerBridge bridge = LockViewerBridges.jei();
        if (bridge == null) {
            return false;
        }
        try {
            boolean ok = bridge.applyIngredientDeltas(hideItems, showItems);
            ok |= bridge.applyRecipeStates(hideRecipes, showRecipes);
            return ok;
        } catch (Exception error) {
            warnOnce("JEI sync failed: " + error);
            return false;
        }
    }

    private static boolean emiReload() {
        try {
            Class<?> reloadManager = Class.forName("dev.emi.emi.runtime.EmiReloadManager");
            java.lang.reflect.Method reload = reloadManager.getMethod("reload");
            reload.invoke(null);
            return true;
        } catch (ClassNotFoundException notInstalled) {
            return false;
        } catch (Exception error) {
            warnOnce("EMI refresh failed: " + error);
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
            java.lang.reflect.Method of = entryStacks.getMethod("of", ItemStack.class);
            if (!hide.isEmpty()) {
                java.lang.reflect.Method removeEntry = registryClass.getMethod(
                        "removeEntry", Class.forName("me.shedaniel.rei.api.common.entry.EntryStack"));
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
            warnOnce("REI sync failed: " + error);
            return false;
        }
    }

    private static void warnOnce(String message) {
        if (WARNED_MESSAGES.add(message)) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] {}", message);
        }
    }
}
