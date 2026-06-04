package com.abo47.questsandstuff.client.compat.recipeviewer;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.recipe.CanvasRecipeCardAsset;
import com.abo47.questsandstuff.client.canvas.recipe.CanvasRecipeCardRecipes;
import com.abo47.questsandstuff.client.canvas.recipe.CanvasRecipeCardRecipes.RecipeView;
import com.abo47.questsandstuff.client.tablet.icons.ItemStackIconCodec;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;

public final class RecipeViewerSelectionBridge {
    private static PendingSelection pendingSelection;

    private RecipeViewerSelectionBridge() {
    }

    public static boolean begin(Player player, TabletUiState state, String target, List<RecipeView> recipes, ItemStack stack, Runnable refresh) {
        return begin(player, state, target, recipes, stack, refresh, SelectionMode.OUTPUT, null);
    }

    public static boolean beginFromKeybind(
            Player player,
            TabletUiState state,
            String target,
            List<RecipeView> recipes,
            ItemStack stack,
            Runnable refresh,
            RecipeViewerIntegrations.SelectionKeybind keybind
    ) {
        if (keybind == null) {
            return false;
        }
        return begin(player, state, target, recipes, stack, refresh, keybind.recipes() ? SelectionMode.OUTPUT : SelectionMode.INPUT, keybind);
    }

    private static boolean begin(
            Player player,
            TabletUiState state,
            String target,
            List<RecipeView> recipes,
            ItemStack stack,
            Runnable refresh,
            SelectionMode mode,
            RecipeViewerIntegrations.SelectionKeybind keybind
    ) {
        if (player == null || state == null || target == null || target.isBlank() || stack == null || stack.isEmpty()) {
            return false;
        }
        Set<String> recipeIds = recipeIds(recipes);
        if (recipeIds.isEmpty()) {
            return false;
        }
        Screen parent = Minecraft.getInstance().screen;
        pendingSelection = new PendingSelection(player, state, target, recipeIds, refresh, parent, mode);
        boolean opened = keybind == null
                ? RecipeViewerIntegrations.showRecipesForSelection(stack.copy())
                : RecipeViewerIntegrations.showForSelection(stack.copy(), keybind);
        if (opened) {
            QuestsAndStuffMod.debugLog("[QnS:Compat] started native {} selection target={} recipes={}", mode.logName(), target, recipeIds.size());
            return true;
        }
        pendingSelection = null;
        return false;
    }

    public static boolean hasPendingSelection() {
        return pendingSelection != null;
    }

    public static boolean canPickRecipe(String recipeId) {
        PendingSelection pending = pendingSelection;
        if (pending == null) {
            return false;
        }
        String normalized = normalizeRecipeId(recipeId);
        if (normalized.isBlank()) {
            return false;
        }
        if (pending.recipeIds().contains(normalized)) {
            return true;
        }
        RecipeView recipe = CanvasRecipeCardRecipes.recipeById(normalized);
        return recipe != null && pending.mode().matches(pending.target(), recipe);
    }

    public static boolean pickRecipe(String recipeId, String providerName) {
        PendingSelection pending = pendingSelection;
        String normalized = normalizeRecipeId(recipeId);
        if (pending == null || normalized.isBlank()) {
            return false;
        }
        RecipeView recipe = CanvasRecipeCardRecipes.recipeById(normalized);
        if (recipe == null || !canPickRecipe(pending, normalized, recipe)) {
            return false;
        }
        String target = pending.mode().assetTarget(pending.target(), recipe);
        String asset = CanvasRecipeCardAsset.assetForRecipe(target, normalized);
        if (asset.isBlank()) {
            return false;
        }
        pendingSelection = null;
        QuestDetailsWindow.applyRecipePick(pending.player(), pending.state(), asset);
        closeAll(pending.state());
        if (pending.refresh() != null) {
            pending.refresh().run();
        }
        Screen parent = pending.parentScreen();
        if (parent != null) {
            Minecraft.getInstance().setScreen(parent);
        }
        QuestsAndStuffMod.debugLog("[QnS:Compat] picked {} via {} recipe={} target={}", pending.mode().logName(), providerName, normalized, target);
        return true;
    }

    private static boolean canPickRecipe(PendingSelection pending, String normalized, RecipeView recipe) {
        if (pending.recipeIds().contains(normalized)) {
            return true;
        }
        return pending.mode().matches(pending.target(), recipe);
    }

    private static String outputTarget(RecipeView recipe) {
        if (recipe == null || recipe.output() == null || recipe.output().isEmpty()) {
            return "";
        }
        ItemStack output = recipe.output().copy();
        output.setCount(1);
        return ItemStackIconCodec.iconFromStack(output);
    }

    private static Set<String> recipeIds(List<RecipeView> recipes) {
        Set<String> ids = new LinkedHashSet<>();
        if (recipes == null) {
            return ids;
        }
        for (RecipeView recipe : recipes) {
            String id = normalizeRecipeId(recipe == null ? "" : recipe.id());
            if (!id.isBlank()) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static String normalizeRecipeId(String recipeId) {
        ResourceLocation id = ResourceLocation.tryParse(recipeId == null ? "" : recipeId.trim());
        return id == null ? "" : id.toString();
    }

    private record PendingSelection(
            Player player,
            TabletUiState state,
            String target,
            Set<String> recipeIds,
            Runnable refresh,
            Screen parentScreen,
            SelectionMode mode
    ) {
    }

    private enum SelectionMode {
        OUTPUT {
            @Override
            boolean matches(String target, RecipeView recipe) {
                return CanvasRecipeCardAsset.matchesOutput(target, recipe.output());
            }

            @Override
            String assetTarget(String target, RecipeView recipe) {
                return target;
            }

            @Override
            String logName() {
                return "recipe";
            }
        },
        INPUT {
            @Override
            boolean matches(String target, RecipeView recipe) {
                return CanvasRecipeCardRecipes.recipeUsesTarget(target, recipe);
            }

            @Override
            String assetTarget(String target, RecipeView recipe) {
                return outputTarget(recipe);
            }

            @Override
            String logName() {
                return "uses";
            }
        };

        abstract boolean matches(String target, RecipeView recipe);

        abstract String assetTarget(String target, RecipeView recipe);

        abstract String logName();
    }
}
