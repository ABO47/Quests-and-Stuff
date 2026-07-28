package com.abo47.questsandstuff.client.compat.recipeviewer.rei;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import com.abo47.questsandstuff.client.compat.recipeviewer.RecipePickButtonOverlay;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerSelectionBridge;
import com.abo47.questsandstuff.client.tablet.icons.FluidIconCodec;
import com.abo47.questsandstuff.client.tablet.icons.ItemStackIconCodec;

public final class ReiRecipePickOverlay {
    private static final String REI_DEFAULT_SCREEN = "me.shedaniel.rei.impl.client.gui.screen.DefaultDisplayViewingScreen";
    private static final String REI_COMPOSITE_SCREEN = "me.shedaniel.rei.impl.client.gui.screen.CompositeDisplayViewingScreen";

    private ReiRecipePickOverlay() {
    }

    public static void drawForScreen(Object screen, GuiGraphics graphics, int mouseX, int mouseY) {
        if (!RecipeViewerSelectionBridge.hasPendingSelection() || !isRecipeScreen(screen)) {
            return;
        }
        List<ButtonTarget> targets = targets(screen);
        for (ButtonTarget target : targets) {
            if (target.isNativeRecipeHovered(mouseX, mouseY) && !RecipePickButtonOverlay.contains(target.button(), mouseX, mouseY)) {
                continue;
            }
            RecipePickButtonOverlay.draw(graphics, mouseX, mouseY, target.button());
        }
        for (ButtonTarget target : targets) {
            if (target.isNativeRecipeHovered(mouseX, mouseY)) {
                continue;
            }
            RecipePickButtonOverlay.renderTooltip(graphics, mouseX, mouseY, target.button());
        }
    }

    public static boolean pickFromScreen(Object screen, double mouseX, double mouseY, int mouseButton) {
        if (mouseButton != 0 || !RecipeViewerSelectionBridge.hasPendingSelection() || !isRecipeScreen(screen)) {
            return false;
        }
        for (ButtonTarget target : targets(screen)) {
            if (RecipePickButtonOverlay.contains(target.button(), mouseX, mouseY)) {
                return RecipeViewerSelectionBridge.pickVisibleRecipe(target.recipeId(), "REI", target.viewerTypeId(), target.outputTarget());
            }
        }
        return false;
    }

    private static boolean isRecipeScreen(Object screen) {
        return isDefaultScreen(screen) || isCompositeScreen(screen);
    }

    private static boolean isDefaultScreen(Object screen) {
        return classHierarchyContains(screen, REI_DEFAULT_SCREEN);
    }

    private static boolean isCompositeScreen(Object screen) {
        return classHierarchyContains(screen, REI_COMPOSITE_SCREEN);
    }

    private static boolean classHierarchyContains(Object owner, String className) {
        if (owner == null || className == null) {
            return false;
        }
        Class<?> type = owner.getClass();
        while (type != null) {
            if (className.equals(type.getName())) {
                return true;
            }
            type = type.getSuperclass();
        }
        return false;
    }

    private static List<ButtonTarget> targets(Object screen) {
        if (isCompositeScreen(screen)) {
            return compositeTargets(screen);
        }
        Object recipeBounds = fieldValue(screen, "recipeBounds");
        if (!(recipeBounds instanceof Map<?, ?> map) || map.isEmpty()) {
            return List.of();
        }
        List<ButtonTarget> targets = new ArrayList<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Rect2i bounds = rectangle(entry.getKey());
            Object displaySpec = displaySpec(entry.getValue());
            String outputTarget = outputTarget(displaySpec);
            String recipeId = pickableRecipeId(displaySpec, outputTarget);
            if (bounds == null || recipeId.isBlank()) {
                continue;
            }
            Rect2i defaultReiButton = new Rect2i(bounds.getX() + bounds.getWidth() + 2, bounds.getY() + bounds.getHeight() - 16, 10, 10);
            Rect2i button = RecipePickButtonOverlay.buttonAbove(defaultReiButton);
            if (button != null) {
                targets.add(new ButtonTarget(recipeId, viewerTypeId(displaySpec), outputTarget, bounds, button));
            }
        }
        return targets;
    }

    private static List<ButtonTarget> compositeTargets(Object screen) {
        Object displaySpec = selectedDisplaySpec(screen);
        String outputTarget = outputTarget(displaySpec);
        String recipeId = pickableRecipeId(displaySpec, outputTarget);
        Rect2i screenBounds = rectangle(fieldValue(screen, "bounds"));
        Object display = invoke(displaySpec, "provideInternalDisplay");
        Object category = selectedCategory(screen);
        String viewerTypeId = firstPresent(categoryId(category), viewerTypeId(displaySpec));
        int displayWidth = intInvoke(category, "getDisplayWidth", display);
        int displayHeight = intInvoke(category, "getDisplayHeight");
        if (recipeId.isBlank() || screenBounds == null || displayWidth <= 0 || displayHeight <= 0) {
            return List.of();
        }
        Rect2i recipeBounds = new Rect2i(
                screenBounds.getX() + 100 + (screenBounds.getWidth() - 100) / 2 - displayWidth / 2,
                screenBounds.getY() + screenBounds.getHeight() / 2 - displayHeight / 2,
                displayWidth,
                displayHeight
        );
        Rect2i defaultReiButton = new Rect2i(recipeBounds.getX() + recipeBounds.getWidth() + 2, recipeBounds.getY() + recipeBounds.getHeight() - 16, 10, 10);
        Rect2i button = RecipePickButtonOverlay.buttonAbove(defaultReiButton);
        return button == null ? List.of() : List.of(new ButtonTarget(recipeId, viewerTypeId, outputTarget, recipeBounds, button));
    }

    private static Object selectedDisplaySpec(Object screen) {
        Object category = selectedCategory(screen);
        Object categoryMap = fieldValue(screen, "categoryMap");
        int selectedRecipeIndex = intFieldOrMethod(screen, "selectedRecipeIndex");
        if (category == null || !(categoryMap instanceof Map<?, ?> map)) {
            return null;
        }
        Object displays = map.get(category);
        if (!(displays instanceof List<?> list) || selectedRecipeIndex < 0 || selectedRecipeIndex >= list.size()) {
            return null;
        }
        return list.get(selectedRecipeIndex);
    }

    private static Object selectedCategory(Object screen) {
        Object categories = fieldValue(screen, "categories");
        int selectedCategoryIndex = intFieldOrMethod(screen, "selectedCategoryIndex");
        if (!(categories instanceof List<?> list) || selectedCategoryIndex < 0 || selectedCategoryIndex >= list.size()) {
            return null;
        }
        return list.get(selectedCategoryIndex);
    }

    private static Object displaySpec(Object pair) {
        if (pair == null) {
            return null;
        }
        Object left = invoke(pair, "left");
        if (left != null) {
            return left;
        }
        Object first = invoke(pair, "getFirst");
        if (first != null) {
            return first;
        }
        Object key = invoke(pair, "getKey");
        if (key != null) {
            return key;
        }
        Object field = fieldValue(pair, "first");
        if (field != null) {
            return field;
        }
        return fieldValue(pair, "left");
    }

    private static String pickableRecipeId(Object displaySpec, String outputTarget) {
        if (displaySpec == null) {
            return "";
        }
        Object ids = invoke(displaySpec, "provideInternalDisplayIds");
        if (ids instanceof Collection<?> collection) {
            for (Object id : collection) {
                String recipeId = id == null ? "" : id.toString();
                if (RecipeViewerSelectionBridge.canPickVisibleRecipe(recipeId, outputTarget)) {
                    return recipeId;
                }
            }
        }
        Object display = invoke(displaySpec, "provideInternalDisplay");
        Object optional = invoke(display, "getDisplayLocation");
        if (optional instanceof Optional<?> location && location.isPresent()) {
            String recipeId = location.get().toString();
            if (RecipeViewerSelectionBridge.canPickVisibleRecipe(recipeId, outputTarget)) {
                return recipeId;
            }
        }
        return "";
    }

    private static String outputTarget(Object displaySpec) {
        Object display = invoke(displaySpec, "provideInternalDisplay");
        Object outputs = invoke(display, "getOutputEntries");
        if (!(outputs instanceof List<?> list)) {
            return "";
        }
        for (Object ingredient : list) {
            if (!(ingredient instanceof Iterable<?> entries)) {
                continue;
            }
            for (Object entry : entries) {
                String target = entryTarget(entry);
                if (!target.isBlank()) {
                    return target;
                }
            }
        }
        return "";
    }

    private static String entryTarget(Object entry) {
        Object value = invoke(entry, "getValue");
        String fluidTarget = fluidTarget(value);
        if (!fluidTarget.isBlank()) {
            return fluidTarget;
        }
        ItemStack stack = value instanceof ItemStack itemStack ? itemStack : ItemStack.EMPTY;
        if (stack.isEmpty()) {
            Object cheated = invoke(entry, "cheatsAs");
            value = invoke(cheated, "getValue");
            fluidTarget = fluidTarget(value);
            if (!fluidTarget.isBlank()) {
                return fluidTarget;
            }
            if (!(value instanceof ItemStack cheatedStack) || cheatedStack.isEmpty()) {
                return "";
            }
            stack = cheatedStack;
        }
        ItemStack output = stack.copy();
        output.setCount(1);
        return ItemStackIconCodec.iconFromStack(output);
    }

    private static String fluidTarget(Object value) {
        Object fluid = invoke(value, "getFluid");
        return fluid instanceof Fluid typedFluid ? FluidIconCodec.iconFromFluid(typedFluid) : "";
    }

    private static String viewerTypeId(Object displaySpec) {
        Object display = invoke(displaySpec, "provideInternalDisplay");
        return firstPresent(categoryId(display), categoryId(displaySpec));
    }

    private static String categoryId(Object owner) {
        String fromCategory = resourceId(invoke(owner, "getCategoryIdentifier"));
        if (!fromCategory.isBlank()) {
            return fromCategory;
        }
        return resourceId(invoke(owner, "getIdentifier"));
    }

    private static String resourceId(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof ResourceLocation resourceLocation) {
            return resourceLocation.toString();
        }
        Object identifier = invoke(value, "getIdentifier");
        if (identifier != null && identifier != value) {
            String nested = resourceId(identifier);
            if (!nested.isBlank()) {
                return nested;
            }
        }
        ResourceLocation parsed = ResourceLocation.tryParse(value.toString());
        return parsed == null ? "" : parsed.toString();
    }

    private static String firstPresent(String first, String second) {
        return first == null || first.isBlank() ? second == null ? "" : second : first;
    }

    private static Rect2i rectangle(Object rectangle) {
        if (rectangle == null) {
            return null;
        }
        int x = intFieldOrMethod(rectangle, "x");
        int y = intFieldOrMethod(rectangle, "y");
        int width = intFieldOrMethod(rectangle, "width");
        int height = intFieldOrMethod(rectangle, "height");
        if (width <= 0 || height <= 0) {
            return null;
        }
        return new Rect2i(x, y, width, height);
    }

    private static int intFieldOrMethod(Object owner, String name) {
        Object field = fieldValue(owner, name);
        if (field instanceof Number number) {
            return number.intValue();
        }
        Object value = invoke(owner, name);
        return value instanceof Number number ? number.intValue() : 0;
    }

    private static int intInvoke(Object owner, String name, Object... args) {
        Object value = invoke(owner, name, args);
        return value instanceof Number number ? number.intValue() : 0;
    }

    private static Object fieldValue(Object owner, String name) {
        if (owner == null) {
            return null;
        }
        Class<?> type = owner.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(owner);
            } catch (ReflectiveOperationException ignored) {
                type = type.getSuperclass();
            } catch (LinkageError | RuntimeException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Object invoke(Object owner, String name, Object... args) {
        if (owner == null) {
            return null;
        }
        try {
            Method method = accessibleMethod(owner.getClass(), name, args.length);
            return method.invoke(owner, args);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return null;
        }
    }

    private static Method accessibleMethod(Class<?> owner, String name, int parameterCount) throws NoSuchMethodException {
        Class<?> type = owner;
        while (type != null) {
            for (Method method : type.getDeclaredMethods()) {
                if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                    method.setAccessible(true);
                    return method;
                }
            }
            type = type.getSuperclass();
        }
        for (Method method : owner.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new NoSuchMethodException(owner.getName() + "#" + name + "/" + parameterCount);
    }

    private record ButtonTarget(String recipeId, String viewerTypeId, String outputTarget, Rect2i recipeBounds, Rect2i button) {
        private boolean isNativeRecipeHovered(int mouseX, int mouseY) {
            return RecipePickButtonOverlay.contains(recipeBounds, mouseX, mouseY);
        }
    }
}
