package com.abo47.questsandstuff.client.tablet.theme.skin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

public final class SkinAnchorRegistry {
    private static final Map<String, Widget> KEY_TO_WIDGET = new HashMap<>();
    private static final IdentityHashMap<Widget, String> WIDGET_TO_KEY = new IdentityHashMap<>();
    private static final Map<String, Set<Widget>> SHARED_KEY_TO_WIDGETS = new HashMap<>();
    private static final IdentityHashMap<Widget, String> WIDGET_TO_SHARED_KEY = new IdentityHashMap<>();

    private SkinAnchorRegistry() {
    }

    public static void clear() {
        KEY_TO_WIDGET.clear();
        WIDGET_TO_KEY.clear();
        SHARED_KEY_TO_WIDGETS.clear();
        WIDGET_TO_SHARED_KEY.clear();
    }

    public static void register(String key, Widget widget) {
        if (key == null || key.isBlank() || widget == null) return;
        Widget old = KEY_TO_WIDGET.put(key, widget);
        if (old != null && old != widget) {
            WIDGET_TO_KEY.remove(old);
        }
        WIDGET_TO_KEY.put(widget, key);
    }

    public static void unregister(String key) {
        if (key == null || key.isBlank()) return;
        Widget old = KEY_TO_WIDGET.remove(key);
        if (old != null) {
            WIDGET_TO_KEY.remove(old);
        }
    }

    public static void registerShared(String key, Widget widget) {
        if (key == null || key.isBlank() || widget == null) return;
        SHARED_KEY_TO_WIDGETS.computeIfAbsent(key, k -> new HashSet<>()).add(widget);
        WIDGET_TO_SHARED_KEY.put(widget, key);
    }

    public static void unregisterShared(String key, Widget widget) {
        if (key == null || key.isBlank() || widget == null) return;
        Set<Widget> set = SHARED_KEY_TO_WIDGETS.get(key);
        if (set != null) {
            set.remove(widget);
            if (set.isEmpty()) {
                SHARED_KEY_TO_WIDGETS.remove(key);
            }
        }
        if (key.equals(WIDGET_TO_SHARED_KEY.get(widget))) {
            WIDGET_TO_SHARED_KEY.remove(widget);
        }
    }

    public static void clearShared(String key) {
        if (key == null || key.isBlank()) return;
        Set<Widget> set = SHARED_KEY_TO_WIDGETS.remove(key);
        if (set != null) {
            for (Widget w : set) {
                if (key.equals(WIDGET_TO_SHARED_KEY.get(w))) {
                    WIDGET_TO_SHARED_KEY.remove(w);
                }
            }
        }
    }

    public static Widget findByKey(String key) {
        if (key == null || key.isBlank()) return null;
        return KEY_TO_WIDGET.get(key);
    }

    public static Set<Widget> sharedWidgetsFor(String key) {
        if (key == null || key.isBlank()) return Set.of();
        Set<Widget> set = SHARED_KEY_TO_WIDGETS.get(key);
        return set == null ? Set.of() : set;
    }

    public static String keyFor(Widget widget) {
        if (widget == null) return null;
        String key = WIDGET_TO_KEY.get(widget);
        if (key != null) return key;
        return WIDGET_TO_SHARED_KEY.get(widget);
    }
}
