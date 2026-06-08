package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.picker.PickerCache;
import com.abo47.questsandstuff.client.tablet.text.DisplayNameFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class RecipeChoiceIndex {
    private static final PickerCache<RecipeOwner, RecipeChoices, RecipeQuery, List<RecipeChoice>> CACHE = new PickerCache<>();

    private RecipeChoiceIndex() {
    }

    static List<RecipeChoice> recipes(String query, boolean tagMode) {
        String normalizedQuery = SearchFilter.normalize(query);
        boolean showingTags = tagMode || (query != null && query.trim().startsWith("#"));
        RecipeOwner owner = owner();
        return CACHE.query(owner, new RecipeQuery(normalizedQuery, showingTags),
                () -> buildChoices(owner.manager(), owner.registryAccess()),
                choices -> filterChoices(choices, normalizedQuery, showingTags));
    }

    static List<RecipeChoice> filterChoices(RecipeChoices choices, String query, boolean tagMode) {
        if (choices == null) {
            return List.of();
        }
        String normalizedQuery = SearchFilter.normalize(query);
        boolean showingTags = tagMode || (query != null && query.trim().startsWith("#"));
        List<RecipeChoice> source = showingTags ? choices.tags() : choices.outputs();
        if (normalizedQuery.isBlank()) {
            return source;
        }
        String compactQuery = SearchFilter.normalizeKey(normalizedQuery);
        return source.stream()
                .filter(choice -> choice.matches(normalizedQuery, compactQuery))
                .toList();
    }

    static RecipeChoices buildChoices(RecipeManager manager, RegistryAccess registryAccess) {
        Map<String, RecipeChoiceBuilder> found = new LinkedHashMap<>();
        if (manager != null && registryAccess != null) {
            for (Recipe<?> recipe : manager.getRecipes()) {
                ItemStack result = resultItem(recipe, registryAccess);
                if (result == null || result.isEmpty()) {
                    continue;
                }
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(result.getItem());
                if (itemId == null) {
                    continue;
                }
                String value = itemId.toString();
                RecipeChoiceBuilder builder = found.computeIfAbsent(value, ignored -> RecipeChoiceBuilder.of(value, result));
                builder.addRecipe(recipe.getId().toString());
            }
        }
        if (found.isEmpty()) {
            addFallback(found, "minecraft:crafting_table");
            addFallback(found, "minecraft:furnace");
            addFallback(found, "minecraft:stick");
            addFallback(found, "minecraft:bread");
        }
        List<RecipeChoice> outputs = found.values().stream()
                .map(RecipeChoiceBuilder::build)
                .sorted(Comparator.comparing(RecipeChoice::displayName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(RecipeChoice::value))
                .toList();
        List<RecipeChoice> tags = BuiltInRegistries.ITEM.getTagNames()
                .map(RecipeChoiceIndex::tagChoice)
                .sorted(Comparator.comparing(RecipeChoice::value))
                .toList();
        return new RecipeChoices(outputs, tags);
    }

    private static RecipeOwner owner() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        RecipeManager manager = connection == null ? null : connection.getRecipeManager();
        RegistryAccess registryAccess = connection == null ? null : connection.registryAccess();
        return new RecipeOwner(manager, registryAccess);
    }

    private static ItemStack resultItem(Recipe<?> recipe, RegistryAccess registryAccess) {
        try {
            return recipe.getResultItem(registryAccess);
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.debugLog("[QnS:UI] recipe output skipped recipe={} error={}", recipe.getId(), exception.toString());
            return ItemStack.EMPTY;
        }
    }

    private static void addFallback(Map<String, RecipeChoiceBuilder> found, String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR && !"minecraft:air".equals(value)) {
            return;
        }
        RecipeChoiceBuilder builder = RecipeChoiceBuilder.of(value, new ItemStack(item));
        builder.addRecipe(value);
        found.put(value, builder);
    }

    private static RecipeChoice tagChoice(TagKey<Item> tag) {
        String value = "#" + tag.location();
        String displayName = DisplayNameFormatter.resourceLeaf(tag.location().toString());
        return RecipeChoice.of(value, displayName, tagPreviews(tag), List.of(), true);
    }

    private static ItemStack[] tagPreviews(TagKey<Item> tag) {
        List<ItemStack> stacks = new ArrayList<>();
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            Item item = holder.value();
            if (item != Items.AIR) {
                stacks.add(new ItemStack(item));
            }
        }
        return stacks.toArray(ItemStack[]::new);
    }

    private record RecipeOwner(RecipeManager manager, RegistryAccess registryAccess) {
    }

    private record RecipeQuery(String query, boolean tags) {
    }

    record RecipeChoices(List<RecipeChoice> outputs, List<RecipeChoice> tags) {
        RecipeChoices {
            outputs = outputs == null ? List.of() : List.copyOf(outputs);
            tags = tags == null ? List.of() : List.copyOf(tags);
        }
    }

    private static final class RecipeChoiceBuilder {
        private final String value;
        private final String displayName;
        private final ItemStack icon;
        private final List<String> recipeIds = new ArrayList<>();

        private RecipeChoiceBuilder(String value, String displayName, ItemStack icon) {
            this.value = value;
            this.displayName = displayName;
            this.icon = icon.copy();
            this.icon.setCount(1);
        }

        static RecipeChoiceBuilder of(String value, ItemStack stack) {
            String displayName = stack.isEmpty() ? "" : stack.getHoverName().getString();
            if (displayName.isBlank()) {
                displayName = DisplayNameFormatter.resourceLeaf(value);
            }
            return new RecipeChoiceBuilder(value, displayName, stack);
        }

        void addRecipe(String recipeId) {
            if (recipeId != null && !recipeId.isBlank() && !recipeIds.contains(recipeId)) {
                recipeIds.add(recipeId);
            }
        }

        RecipeChoice build() {
            return RecipeChoice.of(value, displayName, new ItemStack[]{icon.copy()}, recipeIds, false);
        }
    }

    record RecipeChoice(
            String value,
            String displayName,
            ItemStack[] previews,
            List<String> recipeIds,
            boolean tag,
            String normalizedValue,
            String normalizedDisplayName,
            String normalizedRecipes,
            String compactValue,
            String compactDisplayName,
            String compactRecipes
    ) {
        static RecipeChoice of(String value, String displayName, ItemStack[] previews, List<String> recipeIds, boolean tag) {
            List<String> recipes = recipeIds == null ? List.of() : List.copyOf(recipeIds);
            String joinedRecipes = String.join(" ", recipes);
            String normalizedValue = SearchFilter.normalize(value);
            String normalizedDisplayName = SearchFilter.normalize(displayName);
            String normalizedRecipes = SearchFilter.normalize(joinedRecipes);
            return new RecipeChoice(
                    value,
                    displayName,
                    previews == null ? new ItemStack[0] : copyStacks(previews),
                    recipes,
                    tag,
                    normalizedValue,
                    normalizedDisplayName,
                    normalizedRecipes,
                    SearchFilter.normalizeKey(normalizedValue),
                    SearchFilter.normalizeKey(normalizedDisplayName),
                    SearchFilter.normalizeKey(normalizedRecipes)
            );
        }

        boolean matches(String query, String compactQuery) {
            return normalizedValue.contains(query)
                    || normalizedDisplayName.contains(query)
                    || normalizedRecipes.contains(query)
                    || (!compactQuery.isBlank()
                    && (compactValue.contains(compactQuery)
                    || compactDisplayName.contains(compactQuery)
                    || compactRecipes.contains(compactQuery)));
        }

        Component[] tooltip() {
            return PickerTooltips.nameAndId(displayName, value);
        }

        private static ItemStack[] copyStacks(ItemStack[] stacks) {
            ItemStack[] copy = new ItemStack[stacks.length];
            for (int i = 0; i < stacks.length; i++) {
                ItemStack stack = stacks[i] == null ? ItemStack.EMPTY : stacks[i].copy();
                if (!stack.isEmpty()) {
                    stack.setCount(1);
                }
                copy[i] = stack;
            }
            return copy;
        }
    }
}
