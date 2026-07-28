package com.abo47.questsandstuff.client.tablet.theme.skin;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

public final class SkinAnchorRegistry {
    private static final Map<String, Widget> KEY_TO_WIDGET = new HashMap<>();
    private static final IdentityHashMap<Widget, String> WIDGET_TO_KEY = new IdentityHashMap<>();

    private SkinAnchorRegistry() {
    }

    public static void clear() {
        KEY_TO_WIDGET.clear();
        WIDGET_TO_KEY.clear();
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

    public static Widget findByKey(String key) {
        if (key == null || key.isBlank()) return null;
        return KEY_TO_WIDGET.get(key);
    }

    public static String keyFor(Widget widget) {
        if (widget == null) return null;
        return WIDGET_TO_KEY.get(widget);
    }
}
