package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerIntegrations;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerSelectionBridge;
import com.abo47.questsandstuff.client.tablet.icons.FluidIconCodec;
import com.abo47.questsandstuff.client.tablet.icons.ItemStackIconCodec;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardAsset;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes.RecipeView;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot.QUEST_DETAILS_PICK;

final class RecipePickerApplyActions {
    private RecipePickerApplyActions() {
    }

    static void applyRecipePick(Player player, TabletUiState state, String pick, Runnable refresh) {
        String value = pick == null ? "" : pick.trim();
        if (value.isBlank()) {
            return;
        }
        if (isRecipeCardTarget(state)) {
            String asset = CanvasRecipeCardAsset.assetForPick(value);
            String target = recipeTargetForPick(value, CanvasRecipeCardAsset.outputStack(asset));
            if (!target.isBlank()) {
                List<RecipeView> recipes = CanvasRecipeCardRecipes.recipesForTarget(target);
                if (!recipes.isEmpty() && CanvasRecipeCardAsset.recipeId(value).isBlank()) {
                    if (FluidIconCodec.isFluidIcon(target)) {
                        if (RecipeViewerSelectionBridge.begin(player, state, target, recipes, refresh)) {
                            return;
                        }
                        QuestsAndStuffMod.debugLog("[QnS:UI] recipe fluid pick ignored target={} reason=viewer_unavailable", target);
                        return;
                    }
                    if (recipes.size() > 1 && RecipeViewerSelectionBridge.begin(player, state, target, recipes, pickerStack(asset, recipes), refresh)) {
                        return;
                    }
                    value = CanvasRecipeCardAsset.assetForRecipe(target, recipes.get(0).id());
                } else if (recipes.isEmpty() && FluidIconCodec.isFluidIcon(target) && CanvasRecipeCardAsset.recipeId(value).isBlank()) {
                    if (RecipeViewerSelectionBridge.begin(player, state, target, recipes, refresh)) {
                        return;
                    }
                    QuestsAndStuffMod.debugLog("[QnS:UI] recipe fluid pick ignored target={} reason=viewer_unavailable", target);
                    return;
                }
            }
        }
        QuestDetailsWindow.applyRecipePick(player, state, value);
        closeAll(state);
        refresh.run();
    }

    static void addRecipeViewerKeyHandler(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh) {
        state.pickers.recipeHoveredPick = "";
        modal.addWidget(new WidgetGroup(0, 0, 0, 0) {
            @Override
            public void drawInBackground(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                state.pickers.recipeHoveredPick = "";
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                return handleRecipeViewerSelectionShortcut(player, state, refresh, keyCode, scanCode);
            }
        });
    }

    static void trackRecipeHover(TabletUiState state, String pick) {
        String value = pick == null ? "" : pick.trim();
        if (!value.isBlank()) {
            state.pickers.recipeHoveredPick = value;
        }
    }

    private static ItemStack pickerStack(String asset, List<RecipeView> recipes) {
        ItemStack stack = CanvasRecipeCardAsset.outputStack(asset);
        if (!stack.isEmpty()) {
            return stack;
        }
        if (recipes == null || recipes.isEmpty() || recipes.get(0).output().isEmpty()) {
            return ItemStack.EMPTY;
        }
        return recipes.get(0).output().copy();
    }

    private static boolean handleRecipeViewerSelectionShortcut(Player player, TabletUiState state, Runnable refresh, int keyCode, int scanCode) {
        if (state.pickers.recipeSearchFocused || !isRecipeCardTarget(state)) {
            return false;
        }
        RecipeViewerIntegrations.SelectionKeybind keybind = RecipeViewerIntegrations.selectionKeybind(keyCode, scanCode);
        if (keybind == null) {
            return false;
        }
        String pick = state.pickers.recipeHoveredPick == null ? "" : state.pickers.recipeHoveredPick.trim();
        if (pick.isBlank()) {
            return false;
        }
        ItemStack stack = viewerStackForPick(pick);
        String target = recipeTargetForPick(pick, stack);
        if (target.isBlank()) {
            return false;
        }
        if (FluidIconCodec.isFluidIcon(target)) {
            List<RecipeView> recipes = keybind.recipes()
                    ? CanvasRecipeCardRecipes.recipesForTarget(target)
                    : CanvasRecipeCardRecipes.usesForTarget(target);
            boolean opened = RecipeViewerSelectionBridge.beginFromKeybind(player, state, target, recipes, refresh, keybind);
            if (opened) {
                QuestsAndStuffMod.debugLog("[QnS:UI] recipe picker viewer key mode={} target={} provider={}", keybind.recipes() ? "recipes" : "uses", target, keybind.providerName());
            }
            return opened;
        }
        if (stack.isEmpty()) {
            return false;
        }
        List<RecipeView> recipes = keybind.recipes()
                ? CanvasRecipeCardRecipes.recipesForTarget(target)
                : CanvasRecipeCardRecipes.usesForTarget(target);
        if (recipes.isEmpty()) {
            return false;
        }
        boolean opened = RecipeViewerSelectionBridge.beginFromKeybind(player, state, target, recipes, stack, refresh, keybind);
        if (opened) {
            QuestsAndStuffMod.debugLog("[QnS:UI] recipe picker viewer key mode={} target={} provider={}", keybind.recipes() ? "recipes" : "uses", target, keybind.providerName());
        }
        return opened;
    }

    private static ItemStack viewerStackForPick(String pick) {
        ItemStack stack = CanvasRecipeCardAsset.outputStack(CanvasRecipeCardAsset.assetForPick(pick));
        if (!stack.isEmpty()) {
            stack.setCount(1);
        }
        return stack;
    }

    private static String recipeTargetForPick(String pick, ItemStack stack) {
        String target = CanvasRecipeCardAsset.target(CanvasRecipeCardAsset.assetForPick(pick));
        if (target.isBlank()) {
            return "";
        }
        if (FluidIconCodec.isFluidIcon(target)) {
            return target;
        }
        if (ItemStackIconCodec.isStackIcon(target) && stack != null && !stack.isEmpty()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return id == null ? "" : id.toString();
        }
        return target;
    }

    private static boolean isRecipeCardTarget(TabletUiState state) {
        ModalTargetParser.Target parsed = ModalTargetState.parsedTarget(state, QUEST_DETAILS_PICK, state.questDetails.questDetailsPickTarget);
        return parsed.isCanvasRecipeNew() || parsed.isCanvasRecipeChange() || parsed.isDescRecipe() || parsed.isDescRecipeNew();
    }
}
