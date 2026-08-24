package com.abo47.questsandstuff.client.quest.lock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
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
    private static final Map<RecipeBook, BookState> BOOKS = new WeakHashMap<>();
    private static volatile Field collectionsField;
    private static boolean warnedMissingField;

    private ClientBookFilter() {
    }

    public static void refresh(RecipeBook book) {
        if (book == null) {
            return;
        }
        try {
            Field field = resolveCollectionsField(book.getClass());
            if (field == null) {
                if (!warnedMissingField) {
                    warnedMissingField = true;
                    QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] book collections map not found");
                }
                return;
            }
            @SuppressWarnings("unchecked")
            Map<Object, List<Object>> current = (Map<Object, List<Object>>) field.get(book);
            if (current == null || current.isEmpty()) {
                return;
            }
            BookState state = BOOKS.computeIfAbsent(book, ignored -> new BookState());
            state.absorb(current);

            RegistryAccess access = registryAccess();
            Map<Object, List<Object>> rebuilt = new LinkedHashMap<>(current.size());
            int gatedCollections = 0;
            for (Map.Entry<Object, List<Object>> entry : current.entrySet()) {
                List<Object> base = state.categoryBase(entry.getKey(), entry.getValue());
                List<Object> kept = new ArrayList<>(base.size());
                for (Object collection : base) {
                    if (applyStrips(state, collection, access)) {
                        kept.add(collection);
                    } else {
                        gatedCollections++;
                    }
                }
                rebuilt.put(entry.getKey(), kept);
            }
            field.set(book, rebuilt);
            QuestsAndStuffMod.debugLog(
                    "[QnS:Lock] book filter applied, {} fully gated collection(s)", gatedCollections);
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] recipe book filter failed", error);
        }
    }

    public static void reset() {
        BOOKS.clear();
    }

    public static boolean hasDisplayableRecipes(RecipeCollection collection, RecipeBook book, RecipeBookMenu<?> menu) {
        if (!collection.getDisplayRecipes(true).isEmpty()) {
            return true;
        }
        return !book.isFiltering(menu) && !collection.getDisplayRecipes(false).isEmpty();
    }

    private static class BookState {
        private final Map<Object, List<Object>> categories = new LinkedHashMap<>();
        private final Map<Object, Map<Field, List<Recipe<?>>>> originals = new IdentityHashMap<>();

        void absorb(Map<Object, List<Object>> current) throws IllegalAccessException {
            boolean missing = false;
            for (List<Object> collections : current.values()) {
                for (Object collection : collections) {
                    if (!originals.containsKey(collection)) {
                        missing = true;
                        break;
                    }
                }
            }
            if (missing || categories.size() != current.size()) {
                categories.clear();
                for (Map.Entry<Object, List<Object>> entry : current.entrySet()) {
                    categories.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                    for (Object collection : entry.getValue()) {
                        captureOriginals(collection);
                    }
                }
                QuestsAndStuffMod.debugLog("[QnS:Lock] captured pristine recipe book state");
            }
        }

        private void captureOriginals(Object collection) throws IllegalAccessException {
            if (originals.containsKey(collection)) {
                return;
            }
            Map<Field, List<Recipe<?>>> captured = new LinkedHashMap<>();
            for (Field field : recipeListFields(collection.getClass())) {
                field.setAccessible(true);
                Object value = field.get(collection);
                if (value instanceof List<?> list) {
                    @SuppressWarnings("unchecked")
                    List<Recipe<?>> recipes = (List<Recipe<?>>) list;
                    captured.put(field, new ArrayList<>(recipes));
                }
            }
            originals.put(collection, captured);
        }

        List<Object> categoryBase(Object key, List<Object> fallback) {
            List<Object> base = categories.get(key);
            return base != null ? base : fallback;
        }

        private static List<Field> recipeListFields(Class<?> type) {
            List<Field> fields = new ArrayList<>(2);
            for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
                for (Field field : current.getDeclaredFields()) {
                    String generic = field.getGenericType().getTypeName();
                    if (List.class.isAssignableFrom(field.getType()) && generic.contains(Recipe.class.getSimpleName())) {
                        fields.add(field);
                    }
                }
            }
            return fields;
        }
    }

    private static boolean applyStrips(BookState state, Object collection, RegistryAccess access) throws IllegalAccessException {
        Map<Field, List<Recipe<?>>> captured = state.originals.get(collection);
        if (captured == null || captured.isEmpty() || access == null) {
            return true;
        }
        int total = 0;
        int keptCount = 0;
        for (Map.Entry<Field, List<Recipe<?>>> entry : captured.entrySet()) {
            List<Recipe<?>> base = entry.getValue();
            List<Recipe<?>> kept = new ArrayList<>(base.size());
            for (Recipe<?> recipe : base) {
                ItemStack output = recipe.getResultItem(access);
                total++;
                if (!output.isEmpty() && ClientItemLocks.isLocked(output)) {
                    continue;
                }
                kept.add(recipe);
                keptCount++;
            }
            entry.getKey().set(collection, kept);
        }
        return keptCount > 0 || total == 0;
    }

    private static Field resolveCollectionsField(Class<?> type) {
        Field cached = collectionsField;
        if (cached != null) {
            return cached;
        }
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                String generic = field.getGenericType().getTypeName();
                if (Map.class.isAssignableFrom(field.getType())
                        && generic.contains("RecipeBookCategories")
                        && generic.contains("RecipeCollection")) {
                    field.setAccessible(true);
                    collectionsField = field;
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
