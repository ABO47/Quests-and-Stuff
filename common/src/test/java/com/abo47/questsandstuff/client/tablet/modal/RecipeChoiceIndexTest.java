package com.abo47.questsandstuff.client.tablet.modal;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class RecipeChoiceIndexTest {
    @Test
    void outputSearchMatchesValueDisplayNameAndRecipeIds() {
        RecipeChoiceIndex.RecipeChoice iron = choice("minecraft:iron_ingot", "Iron Ingot", "minecraft:smelting/iron_ingot", false);
        RecipeChoiceIndex.RecipeChoice bread = choice("minecraft:bread", "Bread", "minecraft:crafting/bread", false);
        RecipeChoiceIndex.RecipeChoices choices = new RecipeChoiceIndex.RecipeChoices(List.of(iron, bread), List.of());

        assertSame(iron, RecipeChoiceIndex.filterChoices(choices, "iron", false).get(0));
        assertSame(iron, RecipeChoiceIndex.filterChoices(choices, "smelting", false).get(0));
        assertSame(bread, RecipeChoiceIndex.filterChoices(choices, "minecraft:bread", false).get(0));
    }

    @Test
    void tagSearchUsesTagsForTagModeOrHashPrefix() {
        RecipeChoiceIndex.RecipeChoice output = choice("minecraft:iron_ingot", "Iron Ingot", "minecraft:smelting/iron_ingot", false);
        RecipeChoiceIndex.RecipeChoice tag = choice("#forge:ingots/iron", "Iron Ingots", "", true);
        RecipeChoiceIndex.RecipeChoices choices = new RecipeChoiceIndex.RecipeChoices(List.of(output), List.of(tag));

        assertSame(tag, RecipeChoiceIndex.filterChoices(choices, "iron", true).get(0));
        assertSame(tag, RecipeChoiceIndex.filterChoices(choices, "#forge:ingots", false).get(0));
        assertEquals(List.of(output), RecipeChoiceIndex.filterChoices(choices, "", false));
    }

    @Test
    void nullPreviewsBecomeEmptyArray() {
        RecipeChoiceIndex.RecipeChoice choice = RecipeChoiceIndex.RecipeChoice.of("minecraft:diamond", "Diamond", null, List.of(), false);

        assertEquals(0, choice.previews().length);
    }

    private static RecipeChoiceIndex.RecipeChoice choice(String value, String displayName, String recipeId, boolean tag) {
        List<String> recipes = recipeId == null || recipeId.isBlank() ? List.of() : List.of(recipeId);
        return RecipeChoiceIndex.RecipeChoice.of(value, displayName, null, recipes, tag);
    }
}
