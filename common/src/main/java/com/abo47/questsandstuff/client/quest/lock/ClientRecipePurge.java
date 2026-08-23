package com.abo47.questsandstuff.client.quest.lock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import com.abo47.questsandstuff.QuestsAndStuffMod;

public final class ClientRecipePurge {
    private static final Map<ResourceLocation, Recipe<?>> ORIGINALS = new LinkedHashMap<>();
    public static final java.util.function.BiConsumer<List<Recipe<?>>, List<Recipe<?>>>[] DIFF_SINK = new java.util.function.BiConsumer[1];
    private static RecipeManager gatedManager;
    private static RegistryAccess registryAccess;
    private static boolean captured;

    public record RecipeDiff(List<Recipe<?>> hidden, List<Recipe<?>> shown) {
    }

    private ClientRecipePurge() {
    }


    public static void onRecipesUpdated(RecipeManager manager, RegistryAccess access) {
        registryAccess = access;
        captured = false;
        purge(manager);
    }

    public static void refresh() {
        if (!captured || gatedManager == null) {
            return;
        }
        restoreOriginals();
        purge(gatedManager);
    }

    @SuppressWarnings("unchecked")
    private static void purge(RecipeManager manager) {
        try {
            Field byNameField = resolveByNameField(manager);
            Field recipesField = resolveRecipesField(manager);
            if (byNameField == null || recipesField == null) {
                QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] recipe manager fields not found, book hiding skipped");
                return;
            }
            byNameField.setAccessible(true);
            recipesField.setAccessible(true);
            Map<ResourceLocation, Recipe<?>> byName = (Map<ResourceLocation, Recipe<?>>) byNameField.get(manager);
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes =
                    (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>) recipesField.get(manager);
            if (byName == null || recipes == null) {
                return;
            }
            QuestsAndStuffMod.LOGGER.info("[QnS:Lock] purge fields: byName='{}' ({} entries), recipes='{}'",
                    byNameField.getName(), byName.size(), recipesField.getName());
            captureOriginals(byName);
            gatedManager = manager;
            if (!ClientItemLocks.anyLocks()) {
                return;
            }

            List<ResourceLocation> lockedIds = new ArrayList<>();
            Map<ResourceLocation, Recipe<?>> filteredByName = new LinkedHashMap<>();
            List<Recipe<?>> hidden = new ArrayList<>();
            for (Map.Entry<ResourceLocation, Recipe<?>> entry : byName.entrySet()) {
                if (isLockedOutput(entry.getValue())) {
                    lockedIds.add(entry.getKey());
                    hidden.add(entry.getValue());
                } else {
                    filteredByName.put(entry.getKey(), entry.getValue());
                }
            }
            if (lockedIds.isEmpty()) {
                return;
            }
            byNameField.set(manager, new LinkedHashMap<>(filteredByName));
            int removedFromBuckets = 0;
            for (Map<ResourceLocation, Recipe<?>> bucket : recipes.values()) {
                if (bucket == null) {
                    continue;
                }
                for (ResourceLocation id : lockedIds) {
                    if (bucket.remove(id) != null) {
                        removedFromBuckets++;
                    }
                }
            }
            QuestsAndStuffMod.LOGGER.info(
                    "[QnS:Lock] recipe gate removed {} locked recipe(s), {} bucket entries",
                    lockedIds.size(), removedFromBuckets);
            rebuildRecipeBook();
            notifyDiff(hidden, List.of());
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] recipe gate failed", error);
        }
    }

    @SuppressWarnings("unchecked")
    private static void restoreOriginals() {
        try {
            Field byNameField = resolveByNameField(gatedManager);
            Field recipesField = resolveRecipesField(gatedManager);
            if (byNameField == null || recipesField == null) {
                return;
            }
            Map<ResourceLocation, Recipe<?>> byName = (Map<ResourceLocation, Recipe<?>>) byNameField.get(gatedManager);
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes =
                    (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>) recipesField.get(gatedManager);
            if (byName == null || recipes == null) {
                return;
            }
            List<Recipe<?>> currentlyHidden = new ArrayList<>();
            for (Map.Entry<ResourceLocation, Recipe<?>> entry : ORIGINALS.entrySet()) {
                if (!byName.containsKey(entry.getKey())) {
                    currentlyHidden.add(entry.getValue());
                }
            }
            byNameField.set(gatedManager, new LinkedHashMap<>(ORIGINALS));
            for (Map.Entry<ResourceLocation, Recipe<?>> entry : ORIGINALS.entrySet()) {
                Recipe<?> recipe = entry.getValue();
                Map<ResourceLocation, Recipe<?>> bucket = recipes.get(recipe.getType());
                if (bucket != null) {
                    bucket.putIfAbsent(entry.getKey(), recipe);
                }
            }
            notifyDiff(List.of(), currentlyHidden);
            rebuildRecipeBook();
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] recipe gate restore failed", error);
        }
    }

    private static void captureOriginals(Map<ResourceLocation, Recipe<?>> current) {
        if (captured) {
            return;
        }
        ORIGINALS.clear();
        ORIGINALS.putAll(current);
        captured = true;
    }

    private static boolean isLockedOutput(Recipe<?> recipe) {
        if (registryAccess == null) {
            return false;
        }
        ItemStack output = recipe.getResultItem(registryAccess);
        return !output.isEmpty() && ClientItemLocks.isLocked(output);
    }

    private static Field cachedByName;
    private static Field cachedRecipes;

    private static Field resolveByNameField(RecipeManager manager) {
        if (cachedByName != null) {
            return cachedByName;
        }
        for (Field field : RecipeManager.class.getDeclaredFields()) {
            if (isRecipeMapByLocation(field)) {
                cachedByName = field;
                return cachedByName;
            }
        }
        return null;
    }

    private static Field resolveRecipesField(RecipeManager manager) {
        if (cachedRecipes != null) {
            return cachedRecipes;
        }
        for (Field field : RecipeManager.class.getDeclaredFields()) {
            if (isBucketedRecipeMap(field)) {
                cachedRecipes = field;
                return cachedRecipes;
            }
        }
        return null;
    }

    private static boolean isRecipeMapByLocation(Field field) {
        if (!Map.class.isAssignableFrom(field.getType())) {
            return false;
        }
        String generic = field.getGenericType().getTypeName();
        return generic.contains(ResourceLocation.class.getSimpleName())
                && generic.contains(Recipe.class.getSimpleName())
                && !generic.contains(RecipeType.class.getSimpleName());
    }

    private static boolean isBucketedRecipeMap(Field field) {
        if (!Map.class.isAssignableFrom(field.getType())) {
            return false;
        }
        String generic = field.getGenericType().getTypeName();
        if (!generic.contains(RecipeType.class.getSimpleName()) || !generic.contains(Recipe.class.getSimpleName())) {
            return false;
        }
        Type argument = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
        return argument.getTypeName().contains("Map")
                && !argument.getTypeName().equals(generic);
    }

    private static void notifyDiff(List<Recipe<?>> hidden, List<Recipe<?>> shown) {
        var sink = DIFF_SINK[0];
        if (sink != null) {
            sink.accept(hidden, shown);
        }
    }

    private static void rebuildRecipeBook() {
        try {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level == null || minecraft.player == null) {
                return;
            }
            ClientBookFilter.rebuild(minecraft.player.getRecipeBook());
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] recipe book rebuild failed", error);
        }
    }
}
