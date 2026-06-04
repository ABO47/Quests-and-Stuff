package com.abo47.questsandstuff.client.compat.recipeviewer.jei;

import com.abo47.questsandstuff.client.compat.recipeviewer.RecipePickButtonOverlay;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerSelectionBridge;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public final class JeiRecipePickOverlay {
    private static final String JEI_RECIPES_GUI = "mezz.jei.gui.recipes.RecipesGui";

    private JeiRecipePickOverlay() {
    }

    public static void drawForScreen(Object screen, GuiGraphics graphics, int mouseX, int mouseY) {
        Object layouts = layoutsFromScreen(screen);
        if (layouts == null) {
            return;
        }
        draw(layouts, graphics, mouseX, mouseY);
        drawTooltip(layouts, graphics, mouseX, mouseY);
    }

    public static void draw(Object recipeGuiLayouts, GuiGraphics graphics, int mouseX, int mouseY) {
        drawLayouts(layouts(recipeGuiLayouts), graphics, mouseX, mouseY);
    }

    private static void drawLayouts(List<?> recipeLayoutsWithButtons, GuiGraphics graphics, int mouseX, int mouseY) {
        if (!RecipeViewerSelectionBridge.hasPendingSelection()) {
            return;
        }
        for (Object layoutWithButtons : recipeLayoutsWithButtons) {
            Object layout = recipeLayout(layoutWithButtons);
            String recipeId = recipeId(layout);
            if (layout == null || recipeId.isBlank() || !RecipeViewerSelectionBridge.canPickVisibleRecipe(recipeId)) {
                continue;
            }
            Rect2i button = buttonRect(layoutWithButtons, layout);
            if (button == null) {
                continue;
            }
            if (isNativeSlotHovered(layout, mouseX, mouseY) && !RecipePickButtonOverlay.contains(button, mouseX, mouseY)) {
                continue;
            }
            RecipePickButtonOverlay.draw(graphics, mouseX, mouseY, button);
        }
    }

    public static void drawTooltip(Object recipeGuiLayouts, GuiGraphics graphics, int mouseX, int mouseY) {
        drawLayoutTooltips(layouts(recipeGuiLayouts), graphics, mouseX, mouseY);
    }

    private static void drawLayoutTooltips(List<?> recipeLayoutsWithButtons, GuiGraphics graphics, int mouseX, int mouseY) {
        if (!RecipeViewerSelectionBridge.hasPendingSelection()) {
            return;
        }
        for (Object layoutWithButtons : recipeLayoutsWithButtons) {
            Object layout = recipeLayout(layoutWithButtons);
            String recipeId = recipeId(layout);
            Rect2i button = recipeId.isBlank() || !RecipeViewerSelectionBridge.canPickVisibleRecipe(recipeId) ? null : buttonRect(layoutWithButtons, layout);
            if (button != null
                    && RecipePickButtonOverlay.contains(button, mouseX, mouseY)
                    && !isNativeSlotHovered(layout, mouseX, mouseY)) {
                RecipePickButtonOverlay.renderTooltip(graphics, mouseX, mouseY, button);
                return;
            }
        }
    }

    public static boolean pick(Object recipeGuiLayouts, double mouseX, double mouseY, int mouseButton) {
        if (mouseButton != 0 || !RecipeViewerSelectionBridge.hasPendingSelection()) {
            return false;
        }
        List<?> layouts = layouts(recipeGuiLayouts);
        if (layouts.isEmpty()) {
            return false;
        }
        for (Object layoutWithButtons : layouts) {
            Object layout = recipeLayout(layoutWithButtons);
            String recipeId = recipeId(layout);
            Rect2i button = recipeId.isBlank() || !RecipeViewerSelectionBridge.canPickVisibleRecipe(recipeId) ? null : buttonRect(layoutWithButtons, layout);
            if (button != null && RecipePickButtonOverlay.contains(button, mouseX, mouseY)) {
                return RecipeViewerSelectionBridge.pickVisibleRecipe(recipeId, "JEI", recipeTypeId(layout));
            }
        }
        return false;
    }

    public static boolean pickFromScreen(Object screen, double mouseX, double mouseY, int mouseButton) {
        Object layouts = layoutsFromScreen(screen);
        return layouts != null && pick(layouts, mouseX, mouseY, mouseButton);
    }

    private static boolean isNativeSlotHovered(Object layout, int mouseX, int mouseY) {
        if (layout == null) {
            return false;
        }
        try {
            Object slot = accessibleMethod(layout.getClass(), "getSlotUnderMouse", double.class, double.class)
                    .invoke(layout, (double) mouseX, (double) mouseY);
            return slot instanceof java.util.Optional<?> optional && optional.isPresent();
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return false;
        }
    }

    private static Object layoutsFromScreen(Object screen) {
        if (screen == null || !JEI_RECIPES_GUI.equals(screen.getClass().getName())) {
            return null;
        }
        try {
            Field field = screen.getClass().getDeclaredField("layouts");
            field.setAccessible(true);
            return field.get(screen);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return null;
        }
    }

    private static List<?> layouts(Object recipeGuiLayouts) {
        if (recipeGuiLayouts == null) {
            return List.of();
        }
        try {
            Field field = recipeGuiLayouts.getClass().getDeclaredField("recipeLayoutsWithButtons");
            field.setAccessible(true);
            Object value = field.get(recipeGuiLayouts);
            return value instanceof List<?> list ? list : List.of();
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return List.of();
        }
    }

    private static Object recipeLayout(Object layoutWithButtons) {
        if (layoutWithButtons == null) {
            return null;
        }
        try {
            Method method = accessibleMethod(layoutWithButtons.getClass(), "recipeLayout");
            return method.invoke(layoutWithButtons);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return null;
        }
    }

    private static String recipeId(Object layout) {
        if (layout == null) {
            return "";
        }
        try {
            Object category = layout.getClass().getMethod("getRecipeCategory").invoke(layout);
            Object recipe = layout.getClass().getMethod("getRecipe").invoke(layout);
            Object id = firstMethod(category.getClass(), "getRegistryName", 1).invoke(category, recipe);
            String registryName = resourceId(id);
            if (!registryName.isBlank()) {
                return registryName;
            }
            return firstPresent(
                    resourceId(methodValue(recipe, "getId")),
                    resourceId(fieldValueOrNull(recipe, "id")),
                    resourceId(fieldValueOrNull(recipe, "originalId"))
            );
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return "";
        }
    }

    private static String recipeTypeId(Object layout) {
        if (layout == null) {
            return "";
        }
        try {
            Object category = accessibleMethod(layout.getClass(), "getRecipeCategory").invoke(layout);
            Object recipeType = accessibleMethod(category.getClass(), "getRecipeType").invoke(category);
            Object uid = accessibleMethod(recipeType.getClass(), "getUid").invoke(recipeType);
            return resourceId(uid);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return "";
        }
    }

    private static Rect2i buttonRect(Object layoutWithButtons, Object layout) {
        if (layoutWithButtons == null) {
            return null;
        }
        try {
            Object bookmarkButton = accessibleMethod(layoutWithButtons.getClass(), "bookmarkButton").invoke(layoutWithButtons);
            if (bookmarkButton != null && isVisible(bookmarkButton)) {
                Rect2i bookmark = buttonArea(bookmarkButton);
                if (bookmark.getWidth() > 0 && bookmark.getHeight() > 0) {
                    return RecipePickButtonOverlay.buttonAbove(bookmark);
                }
            }
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
        }
        Rect2i bookmark = layoutBookmarkArea(layout);
        return bookmark == null ? null : RecipePickButtonOverlay.buttonAbove(bookmark);
    }

    private static Rect2i layoutBookmarkArea(Object layout) {
        if (layout == null) {
            return null;
        }
        try {
            Rect2i layoutArea = (Rect2i) accessibleMethod(layout.getClass(), "getRect").invoke(layout);
            Rect2i bookmark = (Rect2i) accessibleMethod(layout.getClass(), "getRecipeBookmarkButtonArea").invoke(layout);
            if (layoutArea == null || bookmark == null || bookmark.getWidth() <= 0 || bookmark.getHeight() <= 0) {
                return null;
            }
            return new Rect2i(
                    layoutArea.getX() + bookmark.getX(),
                    layoutArea.getY() + bookmark.getY(),
                    bookmark.getWidth(),
                    bookmark.getHeight()
            );
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return null;
        }
    }

    private static boolean isVisible(Object button) throws ReflectiveOperationException {
        Object value = accessibleMethod(button.getClass(), "isVisible").invoke(button);
        return value instanceof Boolean visible && visible;
    }

    private static Rect2i buttonArea(Object bookmarkButton) throws ReflectiveOperationException {
        Object area = fieldValue(bookmarkButton, "area");
        if (area == null) {
            return new Rect2i(0, 0, 0, 0);
        }
        int x = ((Number) area.getClass().getMethod("getX").invoke(area)).intValue();
        int y = ((Number) area.getClass().getMethod("getY").invoke(area)).intValue();
        int width = ((Number) area.getClass().getMethod("getWidth").invoke(area)).intValue();
        int height = ((Number) area.getClass().getMethod("getHeight").invoke(area)).intValue();
        return new Rect2i(x, y, width, height);
    }

    private static Object fieldValue(Object target, String name) throws ReflectiveOperationException {
        Field field = fieldInHierarchy(target.getClass(), name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static Object fieldValueOrNull(Object target, String name) {
        try {
            return target == null ? null : fieldValue(target, name);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return null;
        }
    }

    private static Object methodValue(Object target, String name) {
        if (target == null) {
            return null;
        }
        try {
            return accessibleMethod(target.getClass(), name).invoke(target);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return null;
        }
    }

    private static String resourceId(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof ResourceLocation resourceLocation) {
            return resourceLocation.toString();
        }
        ResourceLocation parsed = ResourceLocation.tryParse(value.toString());
        return parsed == null ? "" : parsed.toString();
    }

    private static String firstPresent(String first, String second, String third) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return third == null ? "" : third;
    }

    private static Field fieldInHierarchy(Class<?> owner, String name) throws NoSuchFieldException {
        Class<?> current = owner;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(owner.getName() + "#" + name);
    }

    private static Method firstMethod(Class<?> owner, String name, int parameterCount) throws NoSuchMethodException {
        for (Method method : owner.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                return method;
            }
        }
        throw new NoSuchMethodException(owner.getName() + "#" + name + "/" + parameterCount);
    }

    private static Method accessibleMethod(Class<?> owner, String name) throws NoSuchMethodException {
        try {
            return owner.getMethod(name);
        } catch (NoSuchMethodException ignored) {
            Method method = owner.getDeclaredMethod(name);
            method.setAccessible(true);
            return method;
        }
    }

    private static Method accessibleMethod(Class<?> owner, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        try {
            return owner.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException ignored) {
            Method method = owner.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        }
    }
}
