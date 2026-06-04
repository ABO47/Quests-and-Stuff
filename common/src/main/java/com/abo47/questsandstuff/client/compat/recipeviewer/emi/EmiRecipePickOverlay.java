package com.abo47.questsandstuff.client.compat.recipeviewer.emi;

import com.abo47.questsandstuff.client.compat.recipeviewer.RecipePickButtonOverlay;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerSelectionBridge;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class EmiRecipePickOverlay {
    private static final String EMI_RECIPE_SCREEN = "dev.emi.emi.screen.RecipeScreen";

    private EmiRecipePickOverlay() {
    }

    public static void drawForScreen(Object screen, GuiGraphics graphics, int mouseX, int mouseY) {
        if (!RecipeViewerSelectionBridge.hasPendingSelection() || !isRecipeScreen(screen)) {
            return;
        }
        List<ButtonTarget> targets = targets(screen);
        boolean nativeTooltipActive = nativeTooltipActive(screen);
        for (ButtonTarget target : targets) {
            if (nativeTooltipActive && !RecipePickButtonOverlay.contains(target.button(), mouseX, mouseY)) {
                continue;
            }
            RecipePickButtonOverlay.draw(graphics, mouseX, mouseY, target.button());
        }
        for (ButtonTarget target : targets) {
            if (nativeTooltipActive) {
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
                return RecipeViewerSelectionBridge.pickVisibleRecipe(target.recipeId(), "EMI", target.viewerTypeId());
            }
        }
        return false;
    }

    private static boolean isRecipeScreen(Object screen) {
        return screen != null && EMI_RECIPE_SCREEN.equals(screen.getClass().getName());
    }

    private static boolean nativeTooltipActive(Object screen) {
        return fieldValue(screen, "hoveredWidget") != null;
    }

    private static List<ButtonTarget> targets(Object screen) {
        Object currentPage = fieldValue(screen, "currentPage");
        if (!(currentPage instanceof List<?> groups)) {
            return List.of();
        }
        List<ButtonTarget> targets = new ArrayList<>();
        for (Object group : groups) {
            Object recipe = fieldValue(group, "recipe");
            String recipeId = recipeId(recipe);
            if (recipeId.isBlank() || !RecipeViewerSelectionBridge.canPickVisibleRecipe(recipeId)) {
                continue;
            }
            Rect2i recipeBounds = groupBounds(group);
            if (recipeBounds == null) {
                continue;
            }
            Rect2i button = RecipePickButtonOverlay.pickButtonAboveRightStack(recipeBounds, widgetBlockers(group, recipeBounds));
            if (button != null) {
                targets.add(new ButtonTarget(recipeId, recipeTypeId(recipe), button));
            }
        }
        return targets;
    }

    private static Rect2i groupBounds(Object group) {
        int x = intMethod(group, "x");
        int y = intMethod(group, "y");
        int width = intMethod(group, "getWidth");
        int height = intMethod(group, "getHeight");
        if (width <= 0 || height <= 0) {
            return null;
        }
        return new Rect2i(x, y, width, height);
    }

    private static List<Rect2i> widgetBlockers(Object group, Rect2i groupBounds) {
        Object widgets = fieldValue(group, "widgets");
        if (!(widgets instanceof List<?> widgetList)) {
            return List.of(groupBounds);
        }
        int groupX = groupBounds.getX();
        int groupY = groupBounds.getY();
        List<Rect2i> blockers = new ArrayList<>();
        for (Object widget : widgetList) {
            Object bounds = invoke(widget, "getBounds");
            Rect2i rect = emiBounds(bounds, groupX, groupY);
            if (rect != null) {
                blockers.add(rect);
            }
        }
        return blockers;
    }

    private static Rect2i emiBounds(Object bounds, int offsetX, int offsetY) {
        if (bounds == null) {
            return null;
        }
        int x = intMethod(bounds, "x");
        int y = intMethod(bounds, "y");
        int width = intMethod(bounds, "width");
        int height = intMethod(bounds, "height");
        if (width <= 0 || height <= 0) {
            return null;
        }
        return new Rect2i(offsetX + x, offsetY + y, width, height);
    }

    private static String recipeId(Object recipe) {
        Object originalId = fieldValue(recipe, "originalId");
        if (originalId != null) {
            return originalId.toString();
        }
        Object id = invoke(recipe, "getId");
        return id == null ? "" : id.toString();
    }

    private static String recipeTypeId(Object recipe) {
        Object category = invoke(recipe, "getCategory");
        Object id = invoke(category, "getId");
        return id == null ? "" : id.toString();
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

    private static Object invoke(Object owner, String name) {
        if (owner == null) {
            return null;
        }
        try {
            Method method = accessibleMethod(owner.getClass(), name);
            return method.invoke(owner);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return null;
        }
    }

    private static int intMethod(Object owner, String name) {
        Object value = invoke(owner, name);
        return value instanceof Number number ? number.intValue() : 0;
    }

    private static Method accessibleMethod(Class<?> owner, String name) throws NoSuchMethodException {
        Class<?> type = owner;
        while (type != null) {
            try {
                Method method = type.getDeclaredMethod(name);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                type = type.getSuperclass();
            }
        }
        Method method = owner.getMethod(name);
        method.setAccessible(true);
        return method;
    }

    private record ButtonTarget(String recipeId, String viewerTypeId, Rect2i button) {
    }
}
