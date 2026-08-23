package com.abo47.questsandstuff.forge.compat.recipeviewer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import org.jetbrains.annotations.Nullable;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.quest.lock.ClientRecipePurge;

public final class ForgeJeiLockSync {
    private static final List<Recipe<?>> PENDING_HIDE = new ArrayList<>();
    private static final List<Recipe<?>> PENDING_SHOW = new ArrayList<>();

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
        PENDING_HIDE.addAll(hidden);
        PENDING_SHOW.addAll(shown);
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
        int hiddenCount = 0;
        int shownCount = 0;
        try {
            Class<?> jeiTypeClass = Class.forName("mezz.jei.api.recipe.RecipeType");
            Method hideMethod = manager.getClass().getMethod("hideRecipes", jeiTypeClass, List.class);
            Method unhideMethod = manager.getClass().getMethod("unhideRecipes", jeiTypeClass, List.class);
            for (Recipe<?> recipe : PENDING_HIDE) {
                ResourceLocation uid = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
                var jeiTypeOpt = manager.getRecipeType(uid, recipe.getClass());
                if (jeiTypeOpt.isPresent()) {
                    hideMethod.invoke(manager, jeiTypeOpt.get(), List.of(recipe));
                    hiddenCount++;
                }
            }
            for (Recipe<?> recipe : PENDING_SHOW) {
                ResourceLocation uid = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
                var jeiTypeOpt = manager.getRecipeType(uid, recipe.getClass());
                if (jeiTypeOpt.isPresent()) {
                    unhideMethod.invoke(manager, jeiTypeOpt.get(), List.of(recipe));
                    shownCount++;
                }
            }
            QuestsAndStuffMod.LOGGER.info("[QnS:Lock] jei recipes hidden={} shown={}", hiddenCount, shownCount);
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] jei recipe sync failed", error);
        }
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
