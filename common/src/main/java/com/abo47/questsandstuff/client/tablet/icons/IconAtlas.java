package com.abo47.questsandstuff.client.tablet.icons;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;

public final class IconAtlas {
    private static final String BASE = "textures/gui/icons/";
    private static final Map<String, ResourceLocation> ICON_ID_CACHE = new HashMap<>();
    private static final Map<String, ResourceTexture> ICON_TEXTURE_CACHE = new HashMap<>();
    private static final Set<String> MISSING_REGISTERED_ICONS = new HashSet<>();

    private IconAtlas() {
    }

    public static ResourceLocation icon(String fileName) {
        String clean = IconRegistry.normalizeKey(fileName);
        if (clean.isBlank() || !isValidIconPath(clean)) {
            return null;
        }
        ResourceLocation cached = ICON_ID_CACHE.get(clean);
        if (cached != null) {
            return cached;
        }
        var manager = Minecraft.getInstance().getResourceManager();
        for (String candidate : IconRegistry.candidateFiles(clean)) {
            ResourceLocation id = ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, BASE + candidate);
            if (manager.getResource(id).isPresent()) {
                ICON_ID_CACHE.put(clean, id);
                return id;
            }
        }
        logMissingRegisteredIcon(clean);
        return null;
    }

    public static ResourceTexture iconTexture(String fileName) {
        String clean = IconRegistry.normalizeKey(fileName);
        if (clean.isBlank() || !isValidIconPath(clean)) {
            return null;
        }
        ResourceTexture cached = ICON_TEXTURE_CACHE.get(clean);
        if (cached != null) {
            return cached;
        }
        ResourceLocation id = icon(clean);
        if (id == null) {
            return null;
        }
        ResourceTexture texture = new SmoothResourceTexture(id).setDynamicColor(() -> UiThemeManager.iconColor(clean));
        ICON_TEXTURE_CACHE.put(clean, texture);
        return texture;
    }

    public static void prewarm(String... names) {
        if (names == null || names.length == 0) {
            return;
        }
        for (String name : names) {
            iconTexture(name);
        }
    }

    public static void prewarm(Iterable<String> names) {
        if (names == null) {
            return;
        }
        for (String name : names) {
            iconTexture(name);
        }
    }

    private static boolean isValidIconPath(String value) {
        String clean = value.endsWith(".png") ? value.substring(0, value.length() - 4) : value;
        if (clean.isBlank()) {
            return false;
        }
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            if ((c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == '/'
                    || c == '.'
                    || c == '_'
                    || c == '-') {
                continue;
            }
            return false;
        }
        return true;
    }

    private static void logMissingRegisteredIcon(String clean) {
        if (!IconRegistry.registered(clean) || !MISSING_REGISTERED_ICONS.add(clean)) {
            return;
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] registered icon missing key={} candidates={}", clean, IconRegistry.candidateFiles(clean));
    }
}
