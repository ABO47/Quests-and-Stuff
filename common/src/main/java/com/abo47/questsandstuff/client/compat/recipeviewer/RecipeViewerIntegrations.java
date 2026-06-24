package com.abo47.questsandstuff.client.compat.recipeviewer;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes.RecipeView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class RecipeViewerIntegrations {
    private static final List<RecipeViewerProvider> PROVIDERS = List.of(
            new JeiRecipeViewerProvider(),
            new EmiRecipeViewerProvider(),
            new ReiRecipeViewerProvider()
    );

    private RecipeViewerIntegrations() {
    }

    public static boolean hasAvailableViewer() {
        return activeProvider() != null;
    }

    public static List<RecipeViewerProviderCapabilities> capabilityMatrix() {
        return PROVIDERS.stream()
                .map(RecipeViewerProvider::capabilities)
                .toList();
    }

    public static List<String> debugProbe() {
        return capabilityMatrix().stream()
                .map(RecipeViewerProviderCapabilities::debugLine)
                .toList();
    }

    public static boolean providerSupports(String providerName, RecipeViewerCapability capability) {
        if (providerName == null || providerName.isBlank() || capability == null) {
            return false;
        }
        for (RecipeViewerProvider provider : PROVIDERS) {
            if (providerName.equalsIgnoreCase(provider.name())) {
                return provider.supports(capability);
            }
        }
        return false;
    }

    public static boolean showRecipesForSelection(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        for (RecipeViewerProvider provider : PROVIDERS) {
            if (provider.supportsNativeRecipeSelection()) {
                return showWithProvider(provider, stack, true);
            }
        }
        return false;
    }

    public static boolean showRecipesForSelection(String target) {
        if (target == null || target.isBlank()) {
            return false;
        }
        for (RecipeViewerProvider provider : PROVIDERS) {
            if (provider.supportsNativeRecipeSelection()) {
                return showWithProvider(provider, target, true);
            }
        }
        return false;
    }

    public static boolean showForSelection(ItemStack stack, SelectionKeybind keybind) {
        if (stack == null || stack.isEmpty() || keybind == null || keybind.providerIndex() < 0 || keybind.providerIndex() >= PROVIDERS.size()) {
            return false;
        }
        RecipeViewerProvider provider = PROVIDERS.get(keybind.providerIndex());
        if (!provider.supportsNativeRecipeSelection()) {
            return false;
        }
        return showWithProvider(provider, stack, keybind.recipes());
    }

    public static boolean showForSelection(String target, SelectionKeybind keybind) {
        if (target == null || target.isBlank() || keybind == null || keybind.providerIndex() < 0 || keybind.providerIndex() >= PROVIDERS.size()) {
            return false;
        }
        RecipeViewerProvider provider = PROVIDERS.get(keybind.providerIndex());
        if (!provider.supportsNativeRecipeSelection()) {
            return false;
        }
        return showWithProvider(provider, target, keybind.recipes());
    }

    public static SelectionKeybind selectionKeybind(int keyCode, int scanCode) {
        for (int i = 0; i < PROVIDERS.size(); i++) {
            RecipeViewerProvider provider = PROVIDERS.get(i);
            if (!provider.supportsNativeRecipeSelection()) {
                continue;
            }
            if (provider.supports(RecipeViewerCapability.RECIPE_KEYBIND) && provider.matchesRecipeKey(keyCode, scanCode)) {
                return new SelectionKeybind(true, i, provider.name());
            }
            if (provider.supports(RecipeViewerCapability.USES_KEYBIND) && provider.matchesUsesKey(keyCode, scanCode)) {
                return new SelectionKeybind(false, i, provider.name());
            }
        }
        return null;
    }

    public static boolean handleKeybind(ItemStack stack, int keyCode, int scanCode) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        for (RecipeViewerProvider provider : PROVIDERS) {
            if (!provider.isAvailable()) {
                continue;
            }
            if (provider.supports(RecipeViewerCapability.RECIPE_KEYBIND) && provider.matchesRecipeKey(keyCode, scanCode)) {
                return showWithProvider(provider, stack, true);
            }
            if (provider.supports(RecipeViewerCapability.USES_KEYBIND) && provider.matchesUsesKey(keyCode, scanCode)) {
                return showWithProvider(provider, stack, false);
            }
        }
        return false;
    }

    public static boolean renderRecipeSnapshot(GuiGraphics graphics, RecipeView recipe, int width, int height, int pivotX, int pivotY) {
        if (recipe == null || recipe.id() == null || recipe.id().isBlank()) {
            return false;
        }
        RecipeViewerProvider first = activeProvider();
        if (first != null
                && first.supports(RecipeViewerCapability.SNAPSHOT_RENDERING)
                && first.renderRecipeSnapshot(graphics, recipe, width, height, pivotX, pivotY)) {
            return true;
        }
        for (RecipeViewerProvider provider : PROVIDERS) {
            if (provider == first || !provider.isAvailable()) {
                continue;
            }
            if (provider.supports(RecipeViewerCapability.SNAPSHOT_RENDERING)
                    && provider.renderRecipeSnapshot(graphics, recipe, width, height, pivotX, pivotY)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> fluidEntries() {
        Set<String> entries = new LinkedHashSet<>();
        for (RecipeViewerProvider provider : PROVIDERS) {
            if (!provider.supports(RecipeViewerCapability.FLUID_ENTRIES)) {
                continue;
            }
            entries.addAll(provider.fluidEntries());
        }
        return new ArrayList<>(entries);
    }

    private static boolean showWithProvider(RecipeViewerProvider provider, ItemStack stack, boolean recipes) {
        RecipeViewerCapability capability = recipes ? RecipeViewerCapability.SHOW_RECIPES : RecipeViewerCapability.SHOW_USES;
        if (!provider.supports(capability)) {
            logCapabilitySkip(provider, capability);
            return false;
        }
        boolean opened = recipes ? provider.showRecipes(stack.copy()) : provider.showUses(stack.copy());
        if (opened) {
            QuestsAndStuffMod.debugLog("[QnS:Compat] opened {} in {}", recipes ? "recipes" : "uses", provider.name());
        }
        return opened;
    }

    private static boolean showWithProvider(RecipeViewerProvider provider, String target, boolean recipes) {
        RecipeViewerCapability capability = recipes ? RecipeViewerCapability.SHOW_RECIPES : RecipeViewerCapability.SHOW_USES;
        if (!provider.supports(capability)) {
            logCapabilitySkip(provider, capability);
            return false;
        }
        boolean opened = recipes ? provider.showRecipes(target) : provider.showUses(target);
        if (opened) {
            QuestsAndStuffMod.debugLog("[QnS:Compat] opened {} target={} in {}", recipes ? "recipes" : "uses", target, provider.name());
        }
        return opened;
    }

    private static void logCapabilitySkip(RecipeViewerProvider provider, RecipeViewerCapability capability) {
        QuestsAndStuffMod.debugLog(
                "[QnS:Compat] skipped {} provider={} reason={}",
                capability.id(),
                provider.name(),
                provider.capabilities().reason(capability)
        );
    }

    private static RecipeViewerProvider activeProvider() {
        for (RecipeViewerProvider provider : PROVIDERS) {
            if (provider.isAvailable()) {
                return provider;
            }
        }
        return null;
    }

    public record SelectionKeybind(boolean recipes, int providerIndex, String providerName) {
    }
}
