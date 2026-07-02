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

class RecipeViewerSelectionUtilsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void outputSelectionAcceptsExplicitRecipeIdsAndMatchingOutputs() {
        RecipeViewerSelectionUtils.Selection selection = new RecipeViewerSelectionUtils.Selection(
                "minecraft:diamond",
                Set.of("minecraft:known_recipe"),
                RecipeViewerSelectionMode.OUTPUT
        );

        assertTrue(RecipeViewerSelectionUtils.canPickRecipe(selection, "minecraft:known_recipe", recipe("minecraft:known_recipe", Items.STONE)));
        assertTrue(RecipeViewerSelectionUtils.canPickRecipe(selection, "minecraft:diamond_recipe", recipe("minecraft:diamond_recipe", Items.DIAMOND)));
        assertFalse(RecipeViewerSelectionUtils.canPickRecipe(selection, "minecraft:gold_recipe", recipe("minecraft:gold_recipe", Items.GOLD_INGOT)));
    }

    @Test
    void inputSelectionUsesRecipeOutputAsPickedTarget() {
        RecipeViewerSelectionUtils.Selection selection = new RecipeViewerSelectionUtils.Selection(
                "minecraft:diamond",
                Set.of(),
                RecipeViewerSelectionMode.INPUT
        );
        RecipeView recipe = recipeUsing("minecraft:gold_from_diamond", Items.GOLD_INGOT, Ingredient.of(Items.DIAMOND));

        assertTrue(RecipeViewerSelectionUtils.canPickRecipe(selection, recipe.id(), recipe));
        assertEquals("minecraft:gold_ingot", RecipeViewerSelectionUtils.pickTarget(selection, recipe.id(), recipe, false, ""));
    }

    @Test
    void visibleFallbackUsesOutputTargetRulesPerSelectionMode() {
        RecipeViewerSelectionUtils.Selection outputSelection = new RecipeViewerSelectionUtils.Selection(
                "minecraft:diamond",
                Set.of(),
                RecipeViewerSelectionMode.OUTPUT
        );
        RecipeViewerSelectionUtils.Selection inputSelection = new RecipeViewerSelectionUtils.Selection(
                "minecraft:diamond",
                Set.of(),
                RecipeViewerSelectionMode.INPUT
        );

        assertTrue(RecipeViewerSelectionUtils.canPickVisibleRecipe(outputSelection, "minecraft:external", null, ""));
        assertEquals("minecraft:diamond", RecipeViewerSelectionUtils.pickTarget(outputSelection, "minecraft:external", null, true, ""));
        assertFalse(RecipeViewerSelectionUtils.canPickVisibleRecipe(inputSelection, "minecraft:external", null, ""));
        assertTrue(RecipeViewerSelectionUtils.canPickVisibleRecipe(inputSelection, "minecraft:external", null, "minecraft:gold_ingot"));
        assertEquals("minecraft:gold_ingot", RecipeViewerSelectionUtils.pickTarget(inputSelection, "minecraft:external", null, true, "minecraft:gold_ingot"));
    }

    @Test
    void invalidRecipeIdsAndEmptySelectionsDoNotPick() {
        RecipeViewerSelectionUtils.Selection selection = new RecipeViewerSelectionUtils.Selection(
                "minecraft:diamond",
                Set.of(),
                RecipeViewerSelectionMode.OUTPUT
        );

        assertEquals("", RecipeViewerSelectionUtils.normalizeRecipeId("not a recipe id"));
        assertFalse(RecipeViewerSelectionUtils.canPickRecipe(selection, "not a recipe id", recipe("minecraft:diamond_recipe", Items.DIAMOND)));
        assertEquals("", RecipeViewerSelectionUtils.pickTarget(selection, "not a recipe id", recipe("minecraft:diamond_recipe", Items.DIAMOND), true, "minecraft:diamond"));
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
