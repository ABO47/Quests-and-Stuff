package com.abo47.questsandstuff.client.tablet.theme;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.HashMap;
import java.util.Map;

public final class SkinAnchorRegistry {
    private static final Map<String, Widget> ANCHORS = new HashMap<>();

    private SkinAnchorRegistry() {
    }

    public static void clear() {
        ANCHORS.clear();
    }

    public static void register(String key, Widget widget) {
        if (key == null || key.isBlank()) return;
        ANCHORS.put(key, widget);
    }

    public static Widget findByKey(String key) {
        if (key == null || key.isBlank()) return null;
        return ANCHORS.get(key);
    }

    public static String keyFor(Widget widget) {
        if (widget == null) return null;
        for (Map.Entry<String, Widget> entry : ANCHORS.entrySet()) {
            if (entry.getValue() == widget) return entry.getKey();
        }
        return null;
    }
}
