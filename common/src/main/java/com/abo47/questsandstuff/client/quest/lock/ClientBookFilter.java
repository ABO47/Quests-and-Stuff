package com.abo47.questsandstuff.client.quest.lock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.RegistryAccess;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.questsandstuff.QuestsAndStuffMod;

public final class ClientBookFilter {
    private static final Map<Object, Map<Field, List<Recipe<?>>>> SNAPSHOTS = new WeakHashMap<>();
    private static Map<Object, List<Object>> pristineGroups;
    private static boolean pristineCaptured;

    private ClientBookFilter() {
    }

    public static void rebuild(Object book) {
        try {
            Field collectionsField = findCollectionsField(book.getClass());
            if (collectionsField == null) {
                QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] book collections map not found");
                return;
            }
            collectionsField.setAccessible(true);
            Map<Object, List<Object>> current = (Map<Object, List<Object>>) collectionsField.get(book);
            if (current == null) {
                return;
            }
            capturePristine(current);

            Map<Object, List<Object>> rebuilt = new LinkedHashMap<>();
            int gatedCollections = 0;
            for (Map.Entry<Object, List<Object>> entry : pristineGroups.entrySet()) {
                List<Object> kept = new ArrayList<>();
                for (Object collection : entry.getValue()) {
                    StripResult result = stripLocked(collection);
                    if (result.remaining() > 0 || result.total() == 0) {
                        kept.add(collection);
                    } else {
                        gatedCollections++;
                    }
                }
                rebuilt.put(entry.getKey(), kept);
            }
            collectionsField.set(book, rebuilt);
            if (gatedCollections > 0) {
                QuestsAndStuffMod.LOGGER.info(
                        "[QnS:Lock] book filtered, {} collection(s) fully gated", gatedCollections);
            }
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] recipe book rebuild failed", error);
        }
    }

    public static boolean hasDisplayableRecipes(RecipeCollection collection, RecipeBook book, RecipeBookMenu<?> menu) {
        if (!collection.getDisplayRecipes(true).isEmpty()) {
            return true;
        }
        return !book.isFiltering(menu) && !collection.getDisplayRecipes(false).isEmpty();
    }

    private static void capturePristine(Map<Object, List<Object>> current) {
        if (pristineCaptured && isSameStructure(current)) {
            return;
        }
        pristineGroups = new LinkedHashMap<>();
        for (Map.Entry<Object, List<Object>> entry : current.entrySet()) {
            pristineGroups.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        pristineCaptured = true;
    }

    private static boolean isSameStructure(Map<Object, List<Object>> current) {
        for (List<Object> collections : pristineGroups.values()) {
            for (Object collection : collections) {
                if (containsCollection(current, collection)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean containsCollection(Map<Object, List<Object>> current, Object target) {
        for (List<Object> collections : current.values()) {
            if (collections.contains(target)) {
                return true;
            }
        }
        return false;
    }

    private record StripResult(int stripped, int remaining, int total) {
    }

    private static StripResult stripLocked(Object collection) throws Exception {
        int stripped = 0;
        int total = 0;
        boolean sawRecipeList = false;
        Map<Field, List<Recipe<?>>> snapshot = SNAPSHOTS.computeIfAbsent(collection, ignored -> new LinkedHashMap<>());
        for (Field field : collection.getClass().getDeclaredFields()) {
            String generic = field.getGenericType().getTypeName();
            if (!List.class.isAssignableFrom(field.getType()) || !generic.contains(Recipe.class.getSimpleName())) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(collection);
            if (!(value instanceof List)) {
                continue;
            }
            sawRecipeList = true;
            List<Recipe<?>> recipes = (List<Recipe<?>>) value;
            snapshot.putIfAbsent(field, new ArrayList<>(recipes));
            List<Recipe<?>> base = snapshot.get(field);
            total = Math.max(total, base.size());
            List<Recipe<?>> kept = new ArrayList<>();
            for (Recipe<?> recipe : base) {
                ItemStack output = recipe.getResultItem(registryAccess());
                if (!output.isEmpty() && ClientItemLocks.isLocked(output)) {
                    stripped++;
                } else {
                    kept.add(recipe);
                }
            }
            field.set(collection, kept);
        }
        return new StripResult(stripped, total - stripped, sawRecipeList ? total : 0);
    }

    private static Field findCollectionsField(Class<?> type) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                String generic = field.getGenericType().getTypeName();
                if (Map.class.isAssignableFrom(field.getType())
                        && generic.contains("RecipeBookCategories")
                        && generic.contains("RecipeCollection")) {
                    return field;
                }
            }
        }
        return null;
    }

    private static RegistryAccess registryAccess() {
        var minecraft = Minecraft.getInstance();
        return minecraft.level == null ? null : minecraft.level.registryAccess();
    }
}
