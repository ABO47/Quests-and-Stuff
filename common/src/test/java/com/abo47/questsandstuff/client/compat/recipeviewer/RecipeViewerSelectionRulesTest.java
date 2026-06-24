package com.abo47.questsandstuff.client.compat.recipeviewer;

import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes.RecipeView;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeViewerSelectionRulesTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void outputSelectionAcceptsExplicitRecipeIdsAndMatchingOutputs() {
        RecipeViewerSelectionRules.Selection selection = new RecipeViewerSelectionRules.Selection(
                "minecraft:diamond",
                Set.of("minecraft:known_recipe"),
                RecipeViewerSelectionMode.OUTPUT
        );

        assertTrue(RecipeViewerSelectionRules.canPickRecipe(selection, "minecraft:known_recipe", recipe("minecraft:known_recipe", Items.STONE)));
        assertTrue(RecipeViewerSelectionRules.canPickRecipe(selection, "minecraft:diamond_recipe", recipe("minecraft:diamond_recipe", Items.DIAMOND)));
        assertFalse(RecipeViewerSelectionRules.canPickRecipe(selection, "minecraft:gold_recipe", recipe("minecraft:gold_recipe", Items.GOLD_INGOT)));
    }

    @Test
    void inputSelectionUsesRecipeOutputAsPickedTarget() {
        RecipeViewerSelectionRules.Selection selection = new RecipeViewerSelectionRules.Selection(
                "minecraft:diamond",
                Set.of(),
                RecipeViewerSelectionMode.INPUT
        );
        RecipeView recipe = recipeUsing("minecraft:gold_from_diamond", Items.GOLD_INGOT, Ingredient.of(Items.DIAMOND));

        assertTrue(RecipeViewerSelectionRules.canPickRecipe(selection, recipe.id(), recipe));
        assertEquals("minecraft:gold_ingot", RecipeViewerSelectionRules.pickTarget(selection, recipe.id(), recipe, false, ""));
    }

    @Test
    void visibleFallbackUsesOutputTargetRulesPerSelectionMode() {
        RecipeViewerSelectionRules.Selection outputSelection = new RecipeViewerSelectionRules.Selection(
                "minecraft:diamond",
                Set.of(),
                RecipeViewerSelectionMode.OUTPUT
        );
        RecipeViewerSelectionRules.Selection inputSelection = new RecipeViewerSelectionRules.Selection(
                "minecraft:diamond",
                Set.of(),
                RecipeViewerSelectionMode.INPUT
        );

        assertTrue(RecipeViewerSelectionRules.canPickVisibleRecipe(outputSelection, "minecraft:external", null, ""));
        assertEquals("minecraft:diamond", RecipeViewerSelectionRules.pickTarget(outputSelection, "minecraft:external", null, true, ""));
        assertFalse(RecipeViewerSelectionRules.canPickVisibleRecipe(inputSelection, "minecraft:external", null, ""));
        assertTrue(RecipeViewerSelectionRules.canPickVisibleRecipe(inputSelection, "minecraft:external", null, "minecraft:gold_ingot"));
        assertEquals("minecraft:gold_ingot", RecipeViewerSelectionRules.pickTarget(inputSelection, "minecraft:external", null, true, "minecraft:gold_ingot"));
    }

    @Test
    void invalidRecipeIdsAndEmptySelectionsDoNotPick() {
        RecipeViewerSelectionRules.Selection selection = new RecipeViewerSelectionRules.Selection(
                "minecraft:diamond",
                Set.of(),
                RecipeViewerSelectionMode.OUTPUT
        );

        assertEquals("", RecipeViewerSelectionRules.normalizeRecipeId("not a recipe id"));
        assertFalse(RecipeViewerSelectionRules.canPickRecipe(selection, "not a recipe id", recipe("minecraft:diamond_recipe", Items.DIAMOND)));
        assertEquals("", RecipeViewerSelectionRules.pickTarget(selection, "not a recipe id", recipe("minecraft:diamond_recipe", Items.DIAMOND), true, "minecraft:diamond"));
    }

    private static RecipeView recipe(String id, net.minecraft.world.item.Item output) {
        return recipeUsing(id, output, Ingredient.EMPTY);
    }

    private static RecipeView recipeUsing(String id, net.minecraft.world.item.Item output, Ingredient ingredient) {
        return new RecipeView(
                id,
                "minecraft:crafting",
                "Crafting",
                new ItemStack(output),
                ingredient == null || ingredient.isEmpty() ? List.of() : List.of(ingredient),
                false,
                1,
                1,
                CanvasRecipeCardRecipes.LayoutKind.GENERIC,
                new ItemStack(Items.CRAFTING_TABLE)
        );
    }
}
