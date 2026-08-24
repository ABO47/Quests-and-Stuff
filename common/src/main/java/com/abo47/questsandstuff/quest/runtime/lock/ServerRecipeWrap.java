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
        if (manager == null) {
            return;
        }
        try {
            resolveFields();
            Map<ResourceLocation, Recipe<?>> byName =
                    (Map<ResourceLocation, Recipe<?>>) byNameField.get(manager);
            @SuppressWarnings("unchecked")
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> buckets =
                    (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>) recipesField.get(manager);
            if (byName == null || buckets == null) {
                QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] recipe manager maps unavailable, gating skipped");
                return;
            }
            int wrapped = 0;
            Map<ResourceLocation, Recipe<?>> replacedByName = new LinkedHashMap<>(byName.size());
            for (Map.Entry<ResourceLocation, Recipe<?>> entry : byName.entrySet()) {
                Recipe<?> recipe = maybeWrap(entry.getValue());
                if (recipe != entry.getValue()) {
                    wrapped++;
                }
                replacedByName.put(entry.getKey(), recipe);
            }
            byNameField.set(manager, new LinkedHashMap<>(replacedByName));

            var craftingBucket = buckets.get(RecipeType.CRAFTING);
            if (craftingBucket != null) {
                var rebuilt = new it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap<ResourceLocation, Recipe<?>>();
                for (Map.Entry<ResourceLocation, Recipe<?>> entry : craftingBucket.entrySet()) {
                    rebuilt.put(entry.getKey(), replacedByName.getOrDefault(entry.getKey(), entry.getValue()));
                }
                Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> replacedBuckets =
                        new java.util.LinkedHashMap<>(buckets);
                replacedBuckets.put(RecipeType.CRAFTING, rebuilt);
                recipesField.set(manager, replacedBuckets);
            }
            if (wrapped > 0) {
                QuestsAndStuffMod.LOGGER.info(
                        "[QnS:Lock] wrapped {} crafting recipe(s) with lock gate", wrapped);
            } else {
                QuestsAndStuffMod.debugLog("[QnS:Lock] recipe wrap pass found nothing new");
            }
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] server recipe gating failed", error);
        }
    }

    private static Recipe<?> maybeWrap(Recipe<?> recipe) {
        if (recipe instanceof LockedCraftingRecipe || !(recipe instanceof CraftingRecipe crafting)) {
            return recipe;
        }
        return new LockedCraftingRecipe(crafting);
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
        QuestsAndStuffMod.LOGGER.info(
                "[QnS:Lock] recipe manager fields resolved: byName='{}' buckets='{}'",
                byNameField.getName(), recipesField.getName());
    }
}
