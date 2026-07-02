package com.abo47.questsandstuff.client.compat.recipeviewer;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardAsset;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes.RecipeView;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;

public final class RecipeViewerSelectionBridge {
    private static PendingSelection pendingSelection;

    private RecipeViewerSelectionBridge() {
    }

    public static boolean begin(Player player, TabletUiState state, String target, List<RecipeView> recipes, ItemStack stack, Runnable refresh) {
        return begin(player, state, target, recipes, stack, refresh, RecipeViewerSelectionMode.OUTPUT, null);
    }

    public static boolean begin(Player player, TabletUiState state, String target, List<RecipeView> recipes, Runnable refresh) {
        return begin(player, state, target, recipes, ItemStack.EMPTY, refresh, RecipeViewerSelectionMode.OUTPUT, null);
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
        return begin(player, state, target, recipes, stack, refresh, keybind.recipes() ? RecipeViewerSelectionMode.OUTPUT : RecipeViewerSelectionMode.INPUT, keybind);
    }

    public static boolean beginFromKeybind(
            Player player,
            TabletUiState state,
            String target,
            List<RecipeView> recipes,
            Runnable refresh,
            RecipeViewerIntegrations.SelectionKeybind keybind
    ) {
        if (keybind == null) {
            return false;
        }
        return begin(player, state, target, recipes, ItemStack.EMPTY, refresh, keybind.recipes() ? RecipeViewerSelectionMode.OUTPUT : RecipeViewerSelectionMode.INPUT, keybind);
    }

    private static boolean begin(
            Player player,
            TabletUiState state,
            String target,
            List<RecipeView> recipes,
            ItemStack stack,
            Runnable refresh,
            RecipeViewerSelectionMode mode,
            RecipeViewerIntegrations.SelectionKeybind keybind
    ) {
        if (player == null || state == null || target == null || target.isBlank()) {
            return false;
        }
        RecipeViewerSelectionUtils.Selection selection = new RecipeViewerSelectionUtils.Selection(
                target,
                RecipeViewerSelectionUtils.recipeIds(recipes),
                mode
        );
        boolean targetOnlySelection = stack == null || stack.isEmpty();
        if (selection.recipeIds().isEmpty() && !targetOnlySelection) {
            return false;
        }
        Screen parent = Minecraft.getInstance().screen;
        pendingSelection = new PendingSelection(player, state, selection, refresh, parent);
        boolean opened;
        if (targetOnlySelection) {
            opened = keybind == null
                    ? RecipeViewerIntegrations.showRecipesForSelection(target)
                    : RecipeViewerIntegrations.showForSelection(target, keybind);
        } else {
            opened = keybind == null
                    ? RecipeViewerIntegrations.showRecipesForSelection(stack.copy())
                    : RecipeViewerIntegrations.showForSelection(stack.copy(), keybind);
        }
        if (opened) {
            QuestsAndStuffMod.debugLog("[QnS:Compat] started native {} selection target={} recipes={}", mode.logName(), target, selection.recipeIds().size());
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
        RecipeView recipe = CanvasRecipeCardRecipes.recipeById(normalized);
        return RecipeViewerSelectionUtils.canPickRecipe(pending.selection(), normalized, recipe);
    }

    public static boolean canPickVisibleRecipe(String recipeId) {
        return canPickVisibleRecipe(recipeId, "");
    }

    public static boolean canPickVisibleRecipe(String recipeId, String visibleOutputTarget) {
        PendingSelection pending = pendingSelection;
        if (pending == null) {
            return false;
        }
        String normalized = normalizeRecipeId(recipeId);
        if (normalized.isBlank()) {
            return false;
        }
        RecipeView recipe = CanvasRecipeCardRecipes.recipeById(normalized);
        if (RecipeViewerSelectionUtils.canPickVisibleRecipe(pending.selection(), normalized, recipe, visibleOutputTarget)) {
            return true;
        }
        return false;
    }

    public static boolean matchesPendingTarget(String target) {
        PendingSelection pending = pendingSelection;
        if (pending == null) {
            return false;
        }
        String normalizedTarget = CanvasRecipeCardAsset.target(CanvasRecipeCardAsset.assetForPick(target));
        String normalizedPending = CanvasRecipeCardAsset.target(CanvasRecipeCardAsset.assetForPick(pending.selection().target()));
        return !normalizedTarget.isBlank() && normalizedTarget.equals(normalizedPending);
    }

    public static boolean pickRecipe(String recipeId, String providerName) {
        return pickRecipe(recipeId, providerName, "", false);
    }

    public static boolean pickVisibleRecipe(String recipeId, String providerName, String viewerTypeId) {
        return pickVisibleRecipe(recipeId, providerName, viewerTypeId, "");
    }

    public static boolean pickVisibleRecipe(String recipeId, String providerName, String viewerTypeId, String visibleOutputTarget) {
        return pickRecipe(recipeId, providerName, viewerTypeId, true, visibleOutputTarget);
    }

    private static boolean pickRecipe(String recipeId, String providerName, String viewerTypeId, boolean allowVisibleFallback) {
        return pickRecipe(recipeId, providerName, viewerTypeId, allowVisibleFallback, "");
    }

    private static boolean pickRecipe(String recipeId, String providerName, String viewerTypeId, boolean allowVisibleFallback, String visibleOutputTarget) {
        PendingSelection pending = pendingSelection;
        String normalized = normalizeRecipeId(recipeId);
        if (pending == null || normalized.isBlank()) {
            return false;
        }
        RecipeView recipe = CanvasRecipeCardRecipes.recipeById(normalized);
        String target = RecipeViewerSelectionUtils.pickTarget(pending.selection(), normalized, recipe, allowVisibleFallback, visibleOutputTarget);
        if (target.isBlank()) {
            return false;
        }
        String asset = CanvasRecipeCardAsset.assetForRecipe(target, normalized, viewerTypeId);
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
        QuestsAndStuffMod.debugLog("[QnS:Compat] picked {} via {} recipe={} target={}", pending.selection().mode().logName(), providerName, normalized, target);
        return true;
    }

    private static String normalizeRecipeId(String recipeId) {
        return RecipeViewerSelectionUtils.normalizeRecipeId(recipeId);
    }

    private record PendingSelection(
            Player player,
            TabletUiState state,
            RecipeViewerSelectionUtils.Selection selection,
            Runnable refresh,
            Screen parentScreen
    ) {
    }
}
