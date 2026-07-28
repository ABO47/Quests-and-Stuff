package com.abo47.questsandstuff.client.tablet.app;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TabletAppRegistry {
    private static final LinkedHashMap<String, AppDescriptor> APPS = new LinkedHashMap<>();

    private TabletAppRegistry() {
    }

    public static void register(AppDescriptor app) {
        APPS.put(app.id(), app);
    }

    public static AppDescriptor get(String id) {
        return APPS.get(id);
    }

    public static Map<String, AppDescriptor> all() {
        return Map.copyOf(APPS);
    }
}
