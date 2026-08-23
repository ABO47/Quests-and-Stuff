package com.abo47.questsandstuff.forge.compat.recipeviewer;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;

import org.jetbrains.annotations.Nullable;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.quest.lock.ClientRecipePurge;

public final class ForgeJeiLockSync {
    private static final List<CraftingRecipe> PENDING_HIDE = new ArrayList<>();
    private static final List<CraftingRecipe> PENDING_SHOW = new ArrayList<>();

    private ForgeJeiLockSync() {
    }

    public static void install() {
        ClientRecipePurge.DIFF_SINK[0] = ForgeJeiLockSync::onDiff;
    }

    public static void reset() {
        PENDING_HIDE.clear();
        PENDING_SHOW.clear();
    }

    public static void tick() {
        flush();
    }

    private static void onDiff(List<Recipe<?>> hidden, List<Recipe<?>> shown) {
        for (Recipe<?> recipe : hidden) {
            if (recipe instanceof CraftingRecipe crafting) {
                PENDING_HIDE.add(crafting);
            }
        }
        for (Recipe<?> recipe : shown) {
            if (recipe instanceof CraftingRecipe crafting) {
                PENDING_SHOW.add(crafting);
            }
        }
        flush();
    }

    private static void flush() {
        if (PENDING_HIDE.isEmpty() && PENDING_SHOW.isEmpty()) {
            return;
        }
        if (Minecraft.getInstance().level == null) {
            return;
        }
        IJeiRuntime runtime = fetchRuntime();
        if (runtime == null) {
            QuestsAndStuffMod.LOGGER.info("[QnS:Lock] deferring jei recipe sync (runtime not ready)");
            return;
        }
        var manager = runtime.getRecipeManager();
        var craftingType = manager.getRecipeType(
                ResourceLocation.tryBuild("minecraft", "crafting"), CraftingRecipe.class);
        if (craftingType.isEmpty()) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] jei crafting type not found");
            PENDING_HIDE.clear();
            PENDING_SHOW.clear();
            return;
        }
        if (!PENDING_HIDE.isEmpty()) {
            manager.hideRecipes(craftingType.get(), List.copyOf(PENDING_HIDE));
        }
        if (!PENDING_SHOW.isEmpty()) {
            manager.unhideRecipes(craftingType.get(), List.copyOf(PENDING_SHOW));
        }
        QuestsAndStuffMod.LOGGER.info("[QnS:Lock] jei recipes hidden={} shown={}",
                PENDING_HIDE.size(), PENDING_SHOW.size());
        PENDING_HIDE.clear();
        PENDING_SHOW.clear();
    }

    @Nullable
    private static IJeiRuntime fetchRuntime() {
        try {
            Object internal = Class.forName("mezz.jei.common.Internal")
                    .getMethod("getJeiRuntime")
                    .invoke(null);
            return (IJeiRuntime) internal;
        } catch (java.lang.reflect.InvocationTargetException startingUp) {
            return null;
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] jei runtime fetch failed", error);
            return null;
        }
    }
}
