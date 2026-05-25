package com.abo47.questsandstuff.client.tablet.icons;


import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeManager;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class UiIconAtlas {
    private static final String BASE = "textures/gui/icons/";
    private static final Map<String, ResourceLocation> ICON_ID_CACHE = new HashMap<>();
    private static final Map<String, ResourceTexture> ICON_TEXTURE_CACHE = new HashMap<>();

    private UiIconAtlas() {
    }

    public static ResourceLocation icon(String fileName) {
        String clean = normalizeFileName(fileName);
        if (clean.isBlank() || !isValidIconPath(clean)) {
            return null;
        }
        ResourceLocation cached = ICON_ID_CACHE.get(clean);
        if (cached != null) {
            return cached;
        }
        var manager = Minecraft.getInstance().getResourceManager();
        for (String candidate : candidates(clean)) {
            ResourceLocation id = ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, BASE + candidate);
            if (manager.getResource(id).isPresent()) {
                ICON_ID_CACHE.put(clean, id);
                return id;
            }
        }
        return null;
    }

    public static ResourceTexture iconTexture(String fileName) {
        String clean = normalizeFileName(fileName);
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

    private static String normalizeFileName(String fileName) {
        return fileName == null ? "" : fileName.trim().toLowerCase(Locale.ROOT);
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

    private static List<String> candidates(String clean) {
        String base = clean.endsWith(".png") ? clean.substring(0, clean.length() - 4) : clean;
        List<String> names = new ArrayList<>();
        add(names, base);

        switch (base) {
            case "context_add" -> add(names, "add");
            case "context_rename" -> add(names, "rename");
            case "context_delete" -> add(names, "delete");
            case "context_icon" -> add(names, "icon");
            case "context_entity" -> add(names, "entity");
            case "context_background" -> add(names, "background");
            case "context_text" -> add(names, "text");
            case "context_style_color" -> add(names, "color");
            case "context_image" -> add(names, "image");
            case "context_grid" -> add(names, "grid");
            case "context_size" -> add(names, "size");
            case "context_reset_zoom" -> add(names, "reset_zoom");
            case "context_reset_quest" -> add(names, "reset_quest");
            case "context_repeat" -> add(names, "repeat");
            case "context_repeat-off", "context_repeat_off" -> add(names, "repeat-off");
            case "context_variant" -> add(names, "variant");
            case "context_motion" -> add(names, "motion");
            case "context_style" -> {
                add(names, "text");
                add(names, "color");
            }
            case "context_move_up" -> add(names, "up");
            case "context_move_down" -> add(names, "down");
            case "context_open", "context_external_open" -> add(names, "open");
            case "context_connect" -> add(names, "connect");
            case "context_copy" -> add(names, "copy");
            case "context_paste" -> add(names, "paste");
            case "context_eye" -> add(names, "eye");
            case "context_eye-off", "context_eye_off" -> add(names, "eye-off");
            case "context_lock_quest" -> add(names, "lock_quest");
            case "context_unlock_quest" -> add(names, "unlock_quest");
            case "context_lock_chapter" -> add(names, "lock_chapter");
            case "context_unlock_chapter" -> add(names, "unlock_chapter");
            case "context_audio-lines", "context_audio_lines" -> add(names, "audio-lines");
            case "context_reset", "reset" -> add(names, "reset_quest");
            case "context_editor", "edit", "toggle_editor" -> add(names, "editor");
            case "auto_claim" -> add(names, "claim_all");
            case "context_inspector", "context_properties" -> add(names, "properties");
            case "context_minimap" -> add(names, "minimap");
            case "mode_items" -> add(names, "icon");
            case "mode_tags" -> add(names, "name_tag");
            case "picker_search" -> add(names, "search");
            case "style_align_left" -> add(names, "align-left");
            case "style_align_center" -> add(names, "align-center");
            case "style_align_right" -> add(names, "align-right");
            case "style_bold" -> add(names, "bold");
            case "style_italic" -> add(names, "italic");
            case "style_color" -> add(names, "color");
            case "palette_add" -> add(names, "add");
            case "palette_remove" -> add(names, "delete");
            default -> {
            }
        }
        if (names.contains("delete")) {
            add(names, "close");
        }

        List<String> withExtensions = new ArrayList<>(names.size());
        for (String name : names) {
            withExtensions.add(name + ".png");
        }
        return withExtensions;
    }

    private static void add(List<String> names, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!names.contains(value)) {
            names.add(value);
        }
    }
}
