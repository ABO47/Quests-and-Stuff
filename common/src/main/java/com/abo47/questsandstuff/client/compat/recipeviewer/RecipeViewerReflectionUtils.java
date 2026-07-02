package com.abo47.questsandstuff.client.compat.recipeviewer;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class RecipeViewerReflectionUtils {
    private RecipeViewerReflectionUtils() {
    }

    public static boolean classPresent(String className) {
        try {
            Class.forName(className, false, RecipeViewerReflectionUtils.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }

    public static Method firstMethod(Class<?> owner, String name, int parameterCount) throws NoSuchMethodException {
        for (Method method : owner.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new NoSuchMethodException(owner.getName() + "#" + name + "/" + parameterCount);
    }

    public static boolean matchesMinecraftKey(String[] mappingNames, int keyCode, int scanCode) {
        if (mappingNames == null || mappingNames.length == 0) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null || minecraft.options.keyMappings == null) {
            return false;
        }
        for (KeyMapping mapping : minecraft.options.keyMappings) {
            if (mapping == null || !hasName(mapping, mappingNames)) {
                continue;
            }
            if (mapping.matches(keyCode, scanCode)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesPublicStaticBind(String className, String fieldName, int keyCode, int scanCode) {
        try {
            Class<?> configClass = Class.forName(className);
            Field field = configClass.getField(fieldName);
            Object bind = field.get(null);
            return matchesModifierKey(bind, keyCode, scanCode);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return false;
        }
    }

    public static boolean matchesSingletonBind(String className, String accessorName, int keyCode, int scanCode) {
        try {
            Class<?> configClass = Class.forName(className);
            Object config = configClass.getMethod("getInstance").invoke(null);
            if (config == null) {
                return false;
            }
            Object bind = configClass.getMethod(accessorName).invoke(config);
            return matchesModifierKey(bind, keyCode, scanCode);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return false;
        }
    }

    private static boolean matchesModifierKey(Object bind, int keyCode, int scanCode) throws ReflectiveOperationException {
        if (bind == null) {
            return false;
        }
        Object result = firstMethod(bind.getClass(), "matchesKey", 2).invoke(bind, keyCode, scanCode);
        return result instanceof Boolean value && value;
    }

    private static boolean hasName(KeyMapping mapping, String[] names) {
        String mappingName = mapping.getName();
        for (String name : names) {
            if (name != null && name.equals(mappingName)) {
                return true;
            }
        }
        return false;
    }
}
