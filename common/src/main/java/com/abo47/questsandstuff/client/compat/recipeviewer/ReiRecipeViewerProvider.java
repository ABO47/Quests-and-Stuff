package com.abo47.questsandstuff.client.compat.recipeviewer;

import com.abo47.questsandstuff.client.canvas.recipe.CanvasRecipeCardRecipes.RecipeView;
import com.abo47.questsandstuff.client.tablet.icons.FluidIconCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

final class ReiRecipeViewerProvider implements RecipeViewerProvider {
    private static final String ENTRY_STACK = "me.shedaniel.rei.api.common.entry.EntryStack";
    private static final String ENTRY_STACKS = "me.shedaniel.rei.api.common.util.EntryStacks";
    private static final String CONFIG_OBJECT = "me.shedaniel.rei.api.client.config.ConfigObject";
    private static final String CATEGORY_REGISTRY = "me.shedaniel.rei.api.client.registry.category.CategoryRegistry";
    private static final String DISPLAY_REGISTRY = "me.shedaniel.rei.api.client.registry.display.DisplayRegistry";
    private static final String DISPLAY_SPEC = "me.shedaniel.rei.impl.display.DisplaySpec";
    private static final String DISPLAY_TOOLTIP_COMPONENT = "me.shedaniel.rei.impl.client.gui.widget.DisplayTooltipComponent";
    private static final String ENTRY_REGISTRY = "me.shedaniel.rei.api.client.registry.entry.EntryRegistry";
    private static final String RECTANGLE = "me.shedaniel.math.Rectangle";
    private static final String VIEW_SEARCH_BUILDER = "me.shedaniel.rei.api.client.view.ViewSearchBuilder";

    @Override
    public String name() {
        return "REI";
    }

    @Override
    public boolean isAvailable() {
        return RecipeViewerReflection.classPresent(ENTRY_STACK)
                && RecipeViewerReflection.classPresent(ENTRY_STACKS)
                && RecipeViewerReflection.classPresent(VIEW_SEARCH_BUILDER);
    }

    @Override
    public boolean showRecipes(ItemStack stack) {
        return show(stack, "addRecipesFor");
    }

    @Override
    public boolean showUses(ItemStack stack) {
        return show(stack, "addUsagesFor");
    }

    @Override
    public boolean showRecipes(String target) {
        return show(target, "addRecipesFor");
    }

    @Override
    public boolean showUses(String target) {
        return show(target, "addUsagesFor");
    }

    @Override
    public boolean supportsNativeRecipeSelection() {
        return true;
    }

    @Override
    public boolean matchesRecipeKey(int keyCode, int scanCode) {
        return RecipeViewerReflection.matchesSingletonBind(CONFIG_OBJECT, "getRecipeKeybind", keyCode, scanCode);
    }

    @Override
    public boolean matchesUsesKey(int keyCode, int scanCode) {
        return RecipeViewerReflection.matchesSingletonBind(CONFIG_OBJECT, "getUsageKeybind", keyCode, scanCode);
    }

    @Override
    public boolean renderRecipeSnapshot(GuiGraphics graphics, RecipeView recipe, int width, int height, int pivotX, int pivotY) {
        if (recipe == null || recipe.id().isBlank()) {
            return false;
        }
        String key = "rei-live:" + recipe.typeId() + ":" + recipe.id();
        return RecipeViewerSnapshotRenderer.renderLive(graphics, key, () -> createSnapshotPlan(recipe), width, height, pivotX, pivotY);
    }

    @Override
    public List<String> fluidEntries() {
        if (!isAvailable() || !RecipeViewerReflection.classPresent(ENTRY_REGISTRY)) {
            return List.of();
        }
        try {
            Object registry = Class.forName(ENTRY_REGISTRY).getMethod("getInstance").invoke(null);
            Object value = RecipeViewerReflection.firstMethod(registry.getClass(), "getEntryStacks", 0).invoke(registry);
            List<String> entries = new ArrayList<>();
            if (value instanceof Stream<?> stream) {
                try (stream) {
                    stream.forEach(entry -> addFluidEntry(entries, fluidFromEntry(entry)));
                }
            } else if (value instanceof Iterable<?> iterable) {
                for (Object entry : iterable) {
                    addFluidEntry(entries, fluidFromEntry(entry));
                }
            }
            return entries;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return List.of();
        }
    }

    private boolean show(ItemStack stack, String methodName) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        try {
            Class<?> entryStackClass = Class.forName(ENTRY_STACK);
            Object entry = Class.forName(ENTRY_STACKS).getMethod("of", ItemStack.class).invoke(null, stack.copy());
            Class<?> builderClass = Class.forName(VIEW_SEARCH_BUILDER);
            Object builder = builderClass.getMethod("builder").invoke(null);
            builderClass.getMethod(methodName, entryStackClass).invoke(builder, entry);
            Object opened = builderClass.getMethod("open").invoke(builder);
            return !(opened instanceof Boolean value) || value;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return false;
        }
    }

    private boolean show(String target, String methodName) {
        if (!FluidIconCodec.isFluidIcon(target)) {
            return "addRecipesFor".equals(methodName) ? RecipeViewerProvider.super.showRecipes(target) : RecipeViewerProvider.super.showUses(target);
        }
        Fluid fluid = FluidIconCodec.fluidFromIcon(target);
        if (fluid == Fluids.EMPTY) {
            return false;
        }
        try {
            Class<?> entryStackClass = Class.forName(ENTRY_STACK);
            Object entry = Class.forName(ENTRY_STACKS).getMethod("of", Fluid.class).invoke(null, fluid);
            Class<?> builderClass = Class.forName(VIEW_SEARCH_BUILDER);
            Object builder = builderClass.getMethod("builder").invoke(null);
            builderClass.getMethod(methodName, entryStackClass).invoke(builder, entry);
            Object opened = builderClass.getMethod("open").invoke(builder);
            return !(opened instanceof Boolean value) || value;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return false;
        }
    }

    private RecipeViewerSnapshotRenderer.SnapshotPlan createSnapshotPlan(RecipeView view) throws ReflectiveOperationException {
        ResourceLocation recipeId = ResourceLocation.tryParse(view.id());
        if (recipeId == null) {
            return null;
        }
        ResourceLocation preferredCategoryId = ResourceLocation.tryParse(view.typeId() == null ? "" : view.typeId().trim());
        Object displayRegistry = Class.forName(DISPLAY_REGISTRY).getMethod("getInstance").invoke(null);
        DisplayMatch match = findDisplay(displayRegistry, recipeId, preferredCategoryId);
        if (match == null) {
            return null;
        }
        try {
            RecipeViewerSnapshotRenderer.SnapshotPlan tooltipPlan = createTooltipPlan(match.display());
            if (tooltipPlan != null) {
                return tooltipPlan;
            }
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
        }
        Object categoryRegistry = Class.forName(CATEGORY_REGISTRY).getMethod("getInstance").invoke(null);
        Object config = RecipeViewerReflection.firstMethod(categoryRegistry.getClass(), "get", 1).invoke(categoryRegistry, match.categoryId());
        Object category = RecipeViewerReflection.firstMethod(config.getClass(), "getCategory", 0).invoke(config);
        Object viewObject = RecipeViewerReflection.firstMethod(config.getClass(), "getView", 1).invoke(config, match.display());
        int snapshotWidth = intValue(RecipeViewerReflection.firstMethod(category.getClass(), "getDisplayWidth", 1).invoke(category, match.display()));
        int snapshotHeight = intValue(RecipeViewerReflection.firstMethod(category.getClass(), "getDisplayHeight", 0).invoke(category));
        if (snapshotWidth <= 0 || snapshotHeight <= 0) {
            return null;
        }
        Object bounds = Class.forName(RECTANGLE)
                .getConstructor(int.class, int.class, int.class, int.class)
                .newInstance(0, 0, snapshotWidth, snapshotHeight);
        Object widgets = RecipeViewerReflection.firstMethod(viewObject.getClass(), "setupDisplay", 2).invoke(viewObject, match.display(), bounds);
        if (!(widgets instanceof List<?> widgetList) || widgetList.isEmpty()) {
            return null;
        }
        return new RecipeViewerSnapshotRenderer.SnapshotPlan(snapshotWidth, snapshotHeight, snapshotGraphics -> renderWidgets(widgetList, snapshotGraphics));
    }

    private static RecipeViewerSnapshotRenderer.SnapshotPlan createTooltipPlan(Object display) throws ReflectiveOperationException {
        if (display == null) {
            return null;
        }
        Class<?> componentClass = Class.forName(DISPLAY_TOOLTIP_COMPONENT);
        Class<?> displaySpecClass = Class.forName(DISPLAY_SPEC);
        Object component = componentClass.getConstructor(displaySpecClass).newInstance(display);
        Font font = Minecraft.getInstance().font;
        int snapshotWidth = intValue(RecipeViewerReflection.firstMethod(componentClass, "getWidth", 1).invoke(component, font));
        int snapshotHeight = intValue(RecipeViewerReflection.firstMethod(componentClass, "getHeight", 0).invoke(component));
        if (snapshotWidth <= 0 || snapshotHeight <= 0) {
            return null;
        }
        Method renderImage = RecipeViewerReflection.firstMethod(componentClass, "renderImage", 4);
        return new RecipeViewerSnapshotRenderer.SnapshotPlan(snapshotWidth, snapshotHeight, snapshotGraphics -> {
            try {
                renderImage.invoke(component, font, 0, 0, snapshotGraphics);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(exception);
            }
        });
    }

    private static DisplayMatch findDisplay(Object displayRegistry, ResourceLocation recipeId, ResourceLocation preferredCategoryId) throws ReflectiveOperationException {
        Object all = RecipeViewerReflection.firstMethod(displayRegistry.getClass(), "getAll", 0).invoke(displayRegistry);
        if (!(all instanceof Map<?, ?> map)) {
            return null;
        }
        if (preferredCategoryId != null) {
            DisplayMatch match = findDisplayInCategory(map, recipeId, preferredCategoryId);
            if (match != null) {
                return match;
            }
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getValue() instanceof List<?> displays)) {
                continue;
            }
            for (Object display : displays) {
                Optional<?> location = displayLocation(display);
                if (location.isPresent() && recipeId.equals(location.get())) {
                    return new DisplayMatch(entry.getKey(), display);
                }
            }
        }
        return null;
    }

    private static DisplayMatch findDisplayInCategory(Map<?, ?> map, ResourceLocation recipeId, ResourceLocation categoryId) throws ReflectiveOperationException {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!categoryMatches(entry.getKey(), categoryId) || !(entry.getValue() instanceof List<?> displays)) {
                continue;
            }
            for (Object display : displays) {
                Optional<?> location = displayLocation(display);
                if (location.isPresent() && recipeId.equals(location.get())) {
                    return new DisplayMatch(entry.getKey(), display);
                }
            }
        }
        return null;
    }

    private static boolean categoryMatches(Object category, ResourceLocation expected) {
        if (category == null || expected == null) {
            return false;
        }
        ResourceLocation id = ResourceLocation.tryParse(category.toString());
        return expected.equals(id);
    }

    private static Optional<?> displayLocation(Object display) throws ReflectiveOperationException {
        Object value = RecipeViewerReflection.firstMethod(display.getClass(), "getDisplayLocation", 0).invoke(display);
        return value instanceof Optional<?> optional ? optional : Optional.empty();
    }

    private static void renderWidgets(List<?> widgets, GuiGraphics graphics) {
        float delta = Minecraft.getInstance().getDeltaFrameTime();
        for (Object widget : widgets) {
            try {
                Method render = RecipeViewerReflection.firstMethod(widget.getClass(), "render", 4);
                render.invoke(widget, graphics, -10_000, -10_000, delta);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(exception);
            }
        }
    }

    private static int intValue(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private static Fluid fluidFromEntry(Object entry) {
        Object value = invoke(entry, "getValue");
        Object fluid = invoke(value, "getFluid");
        return fluid instanceof Fluid typedFluid ? typedFluid : null;
    }

    private static Object invoke(Object owner, String name) {
        if (owner == null) {
            return null;
        }
        try {
            Method method = RecipeViewerReflection.firstMethod(owner.getClass(), name, 0);
            return method.invoke(owner);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return null;
        }
    }

    private static void addFluidEntry(List<String> entries, Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return;
        }
        String icon = FluidIconCodec.iconFromFluid(fluid);
        if (!icon.isBlank() && !entries.contains(icon)) {
            entries.add(icon);
        }
    }

    private record DisplayMatch(Object categoryId, Object display) {
    }
}
