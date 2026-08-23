package com.abo47.questsandstuff.quest.runtime.lock;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import com.abo47.questsandstuff.QuestsAndStuffMod;

public final class ServerRecipeWrap {
    private static Field byNameField;
    private static Field recipesField;

    private ServerRecipeWrap() {
    }

    public static void wrapAll(RecipeManager manager) {
        try {
            resolveFields();
            Map<ResourceLocation, Recipe<?>> byName = (Map<ResourceLocation, Recipe<?>>) byNameField.get(manager);
            @SuppressWarnings("unchecked")
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> buckets =
                    (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>) recipesField.get(manager);
            if (byName == null || buckets == null) {
                return;
            }
            Map<ResourceLocation, Recipe<?>> replaced = new LinkedHashMap<>();
            int wrapped = 0;
            for (Map.Entry<ResourceLocation, Recipe<?>> entry : byName.entrySet()) {
                Recipe<?> recipe = entry.getValue();
                if (recipe instanceof GatedCraftingRecipe) {
                    replaced.put(entry.getKey(), recipe);
                    continue;
                }
                if (recipe instanceof CraftingRecipe craftingRecipe) {
                    replaced.put(entry.getKey(), new GatedCraftingRecipe(craftingRecipe));
                    wrapped++;
                    continue;
                }
                replaced.put(entry.getKey(), recipe);
            }
            byNameField.set(manager, new LinkedHashMap<>(replaced));
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> rebuiltBuckets = new LinkedHashMap<>();
            for (Map.Entry<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> bucketEntry : buckets.entrySet()) {
                var newBucket = new it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap<ResourceLocation, Recipe<?>>();
                for (Map.Entry<ResourceLocation, Recipe<?>> entry : bucketEntry.getValue().entrySet()) {
                    Recipe<?> recipe = replaced.get(entry.getKey());
                    if (recipe == null) {
                        recipe = entry.getValue();
                    }
                    if (recipe instanceof CraftingRecipe crafting && !(recipe instanceof GatedCraftingRecipe)) {
                        recipe = new GatedCraftingRecipe(crafting);
                    }
                    newBucket.put(entry.getKey(), recipe);
                }
                rebuiltBuckets.put(bucketEntry.getKey(), newBucket);
            }
            recipesField.set(manager, rebuiltBuckets);
            QuestsAndStuffMod.LOGGER.info("[QnS:Lock] gated {} crafting recipe(s) on server", wrapped);
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] server recipe gating failed", error);
        }
    }

    private static void resolveFields() throws Exception {
        if (byNameField != null && recipesField != null) {
            return;
        }
        for (Field field : RecipeManager.class.getDeclaredFields()) {
            String generic = field.getGenericType().getTypeName();
            if (!Map.class.isAssignableFrom(field.getType())) {
                continue;
            }
            boolean hasLocation = generic.contains(ResourceLocation.class.getSimpleName());
            boolean hasType = generic.contains(RecipeType.class.getSimpleName());
            boolean hasRecipe = generic.contains(Recipe.class.getSimpleName());
            if (hasLocation && hasRecipe && !hasType) {
                byNameField = field;
                byNameField.setAccessible(true);
            } else if (hasType && hasRecipe) {
                recipesField = field;
                recipesField.setAccessible(true);
            }
        }
        if (byNameField == null || recipesField == null) {
            throw new IllegalStateException("RecipeManager fields not resolved");
        }
    }
}
