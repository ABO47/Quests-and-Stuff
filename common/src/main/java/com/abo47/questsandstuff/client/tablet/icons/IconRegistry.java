package com.abo47.questsandstuff.client.tablet.icons;

import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class IconRegistry {
    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();
    private static final Map<String, String> ALIASES = new LinkedHashMap<>();

    static {
        registerCoreIcons();
        registerContextIcons();
        registerPickerIcons();
        registerStyleIcons();
        registerWindowAndHudIcons();
        registerAliases();
    }

    private IconRegistry() {
    }

    public static List<String> preloadKeys() {
        return ENTRIES.values().stream()
                .filter(Entry::preload)
                .map(Entry::key)
                .toList();
    }

    public static Map<String, String> defaultIconRoles() {
        Map<String, String> roles = new LinkedHashMap<>();
        for (Entry entry : ENTRIES.values()) {
            roles.put(entry.key(), entry.defaultRole());
        }
        for (Map.Entry<String, String> alias : ALIASES.entrySet()) {
            Entry target = ENTRIES.get(alias.getValue());
            if (target != null) {
                roles.put(alias.getKey(), target.defaultRole());
            }
        }
        return roles;
    }

    public static boolean registered(String iconKey) {
        String key = normalizeKey(iconKey);
        return ENTRIES.containsKey(key) || ALIASES.containsKey(key);
    }

    public static List<String> candidateFiles(String iconKey) {
        String key = normalizeKey(iconKey);
        if (key.isBlank()) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        add(names, key);

        Entry entry = entryFor(key);
        if (entry != null) {
            add(names, entry.key());
            for (String candidate : entry.resourceCandidates()) {
                add(names, candidate);
            }
        }
        if (names.contains("delete")) {
            add(names, "close");
        }

        List<String> files = new ArrayList<>(names.size());
        for (String name : names) {
            files.add(name + ".png");
        }
        return files;
    }

    public static String normalizeKey(String iconKey) {
        String clean = iconKey == null ? "" : iconKey.trim().toLowerCase(Locale.ROOT);
        if (clean.endsWith(".png")) {
            clean = clean.substring(0, clean.length() - 4);
        }
        return clean;
    }

    private static Entry entryFor(String key) {
        Entry direct = ENTRIES.get(key);
        if (direct != null) {
            return direct;
        }
        String canonical = ALIASES.get(key);
        return canonical == null ? null : ENTRIES.get(canonical);
    }

    private static void registerCoreIcons() {
        preloaded("tools");
        preloaded("grid");
        preloaded("editor");
        preloaded("scroll");
        preloaded("align-center-horizontal");
        preloaded("align-center-vertical");
        preloaded("objects");
        preloaded("entity");
        preloadedRole("close", UiThemeManager.ROLE_ICON_ERROR);
        preloaded("search");
        preloadedRole("add", UiThemeManager.ROLE_ICON_SUCCESS);
        preloaded("rename");
        preloadedRole("delete", UiThemeManager.ROLE_ICON_ERROR, "close");
        preloaded("copy");
        preloadedRole("paste", UiThemeManager.ROLE_ICON_SUCCESS);
        preloadedRole("connect", UiThemeManager.ROLE_ICON_SUCCESS);
        preloadedRole("share-2", UiThemeManager.ROLE_ICON_INTERACTIVE);
        preloaded("settings-2");
        preloaded("stat");
        preloaded("recipe");
        preloaded("item_use");
        preloaded("item_interact");
        preloaded("icon");
        preloadedRole("image", UiThemeManager.ROLE_ICON_SUCCESS);
        preloaded("background");
        preloaded("style");
        preloaded("up");
        preloaded("down");
        preloaded("back");
        preloaded("chevron-right");
        preloadedRole("open", UiThemeManager.ROLE_ICON_INTERACTIVE);
        preloadedRole("focus", UiThemeManager.ROLE_ICON_INTERACTIVE);
        preloadedRole("backpack", UiThemeManager.ROLE_ICON_INTERACTIVE);
        preloadedRole("selectable", UiThemeManager.ROLE_ICON_INTERACTIVE);
        preloaded("file-up");
        preloaded("file-down");
        preloadedRole("manual_check", UiThemeManager.ROLE_ICON_SUCCESS);
        preloaded("size");
        preloaded("opacity");
        preloaded("magnet");
        preloadedRole("lock", UiThemeManager.ROLE_ICON_WARNING);
        preloadedRole("unlock", UiThemeManager.ROLE_ICON_SUCCESS);
        preloadedRole("lock_canvas", UiThemeManager.ROLE_ICON_WARNING);
        preloadedRole("unlock_canvas", UiThemeManager.ROLE_ICON_SUCCESS);
        preloadedRole("lock_separator", UiThemeManager.ROLE_ICON_WARNING);
        preloadedRole("unlock_separator", UiThemeManager.ROLE_ICON_SUCCESS);
        preloadedRole("lock_quest", UiThemeManager.ROLE_ICON_WARNING);
        preloadedRole("unlock_quest", UiThemeManager.ROLE_ICON_SUCCESS);
        preloadedRole("lock_chapter", UiThemeManager.ROLE_ICON_WARNING);
        preloadedRole("unlock_chapter", UiThemeManager.ROLE_ICON_SUCCESS);
        preloaded("background_opacity");
        preloaded("reset_zoom");
        preloadedRole("reset_quest", UiThemeManager.ROLE_ICON_WARNING);
        preloadedRole("repeat", UiThemeManager.ROLE_ICON_SUCCESS);
        preloadedRole("repeat-off", UiThemeManager.ROLE_ICON_WARNING);
        preloadedRole("variant", UiThemeManager.ROLE_ICON_INTERACTIVE);
        preloadedRole("motion", UiThemeManager.ROLE_ICON_INTERACTIVE);
        preloaded("properties");
        preloaded("minimap");
        preloadedRole("themes", UiThemeManager.ROLE_ICON_INTERACTIVE);
        preloadedRole("auto_claim", UiThemeManager.ROLE_ICON_SUCCESS);
        preloadedRole("claim_all", UiThemeManager.ROLE_ICON_SUCCESS);
        preloadedRole("xp", UiThemeManager.ROLE_ICON_SUCCESS);
        preloadedRole("send-horizontal", UiThemeManager.ROLE_ICON_SUCCESS);
        preloadedRole("eye", UiThemeManager.ROLE_ICON_INTERACTIVE);
        preloadedRole("eye-off", UiThemeManager.ROLE_ICON_WARNING);
        preloadedRole("audio-lines", UiThemeManager.ROLE_ICON_INTERACTIVE);
        preloaded("completion_hud_background");
        preloadedRole("play", UiThemeManager.ROLE_ICON_SUCCESS);
        preloadedRole("pause", UiThemeManager.ROLE_ICON_WARNING);
        preloadedRole("reset", UiThemeManager.ROLE_ICON_WARNING, "reset_quest");
        preloadedRole("window_pin", UiThemeManager.ROLE_ICON_INTERACTIVE);
        preloadedRole("hud_layout", UiThemeManager.ROLE_ICON_INTERACTIVE);
        preloadedRole("mail", UiThemeManager.ROLE_ICON_INTERACTIVE);
        preloadedRole("mail-open", UiThemeManager.ROLE_ICON_SUCCESS);
        preloadedRole("door-open", UiThemeManager.ROLE_ICON_WARNING);
        preloadedRole("user-round-x", UiThemeManager.ROLE_ICON_ERROR);
        preloadedRole("crown", UiThemeManager.ROLE_ICON_INTERACTIVE);
        preloadedRole("arrow-up-down", UiThemeManager.ROLE_ICON_INTERACTIVE);
        preloadedRole("wrench", UiThemeManager.ROLE_ICON_INTERACTIVE);
        preloadedRole("mouse-pointer-click", UiThemeManager.ROLE_ICON_SUCCESS);
        
        registeredDefault("biome");
        registeredDefault("orbit");
        registeredDefault("scissors");
        registeredRole("kill_entity", UiThemeManager.ROLE_ICON_WARNING);
        registeredRole("chapter_notice", UiThemeManager.ROLE_ICON_WARNING);
        registeredDefault("fit_grid");
    }

    private static void registerContextIcons() {
        registeredRole("context_add", UiThemeManager.ROLE_ICON_SUCCESS, "add");
        registeredCandidate("context_rename", "rename");
        registeredRole("context_delete", UiThemeManager.ROLE_ICON_ERROR, "delete");
        registeredRole("context_icon", UiThemeManager.ROLE_ICON_INTERACTIVE, "icon");
        registeredRole("context_entity", UiThemeManager.ROLE_ICON_INTERACTIVE, "entity");
        registeredCandidate("context_background", "background");
        registeredCandidate("context_text", "text");
        registeredRole("context_style_color", UiThemeManager.ROLE_ICON_INTERACTIVE, "color");
        registeredRole("context_image", UiThemeManager.ROLE_ICON_SUCCESS, "image");
        registeredCandidate("context_grid", "grid");
        registeredCandidate("context_fit_grid", "fit_grid");
        preloadedRole("context_focus", UiThemeManager.ROLE_ICON_INTERACTIVE, "focus");
        preloadedRole("context_backpack", UiThemeManager.ROLE_ICON_INTERACTIVE, "backpack");
        preloadedRole("context_selectable", UiThemeManager.ROLE_ICON_INTERACTIVE, "selectable");
        registeredCandidate("context_size", "size");
        registeredCandidate("context_reset_zoom", "reset_zoom");
        registeredRole("context_reset_quest", UiThemeManager.ROLE_ICON_WARNING, "reset_quest");
        registeredRole("context_repeat", UiThemeManager.ROLE_ICON_SUCCESS, "repeat");
        registeredRole("context_repeat-off", UiThemeManager.ROLE_ICON_WARNING, "repeat-off");
        registeredRole("context_variant", UiThemeManager.ROLE_ICON_INTERACTIVE, "variant");
        registeredRole("context_motion", UiThemeManager.ROLE_ICON_INTERACTIVE, "motion");
        registeredRole("context_style", UiThemeManager.ROLE_ICON_INTERACTIVE, "text", "color");
        registeredCandidate("context_move_up", "up");
        registeredCandidate("context_move_down", "down");
        preloadedRole("context_open", UiThemeManager.ROLE_ICON_INTERACTIVE, "open");
        registeredRole("context_external_open", UiThemeManager.ROLE_ICON_INTERACTIVE, "open");
        registeredRole("context_connect", UiThemeManager.ROLE_ICON_SUCCESS, "connect");
        registeredRole("context_disconnect", UiThemeManager.ROLE_ICON_WARNING);
        registeredCandidate("context_copy", "copy");
        registeredRole("context_paste", UiThemeManager.ROLE_ICON_SUCCESS, "paste");
        registeredRole("context_cut", UiThemeManager.ROLE_ICON_WARNING);
        registeredRole("context_eye", UiThemeManager.ROLE_ICON_INTERACTIVE, "eye");
        registeredRole("context_eye-off", UiThemeManager.ROLE_ICON_WARNING, "eye-off");
        registeredRole("context_lock_quest", UiThemeManager.ROLE_ICON_WARNING, "lock_quest");
        registeredRole("context_unlock_quest", UiThemeManager.ROLE_ICON_SUCCESS, "unlock_quest");
        registeredRole("context_lock_chapter", UiThemeManager.ROLE_ICON_WARNING, "lock_chapter");
        registeredRole("context_unlock_chapter", UiThemeManager.ROLE_ICON_SUCCESS, "unlock_chapter");
        registeredRole("context_audio-lines", UiThemeManager.ROLE_ICON_INTERACTIVE, "audio-lines");
        registeredCandidate("context_editor", "editor");
        registeredDefault("context_orbit");
        registeredDefault("context_scissors");
        registeredDefault("context_center");
        registeredDefault("context_snap");
        registeredCandidate("context_inspector", "properties");
        registeredCandidate("context_minimap", "minimap");
        registeredDefault("context_select");
        registeredDefault("context_drag");
        registeredDefault("context_properties");
        registeredRole("context_separator", UiThemeManager.ROLE_ICON_MUTED);
        registeredDefault("context_more");
    }

    private static void registerPickerIcons() {
        registeredCandidate("mode_items", "icon");
        registeredCandidate("mode_tags", "name_tag");
        registeredCandidate("mode_inventory", "backpack");
        registeredCandidate("mode_fluids", "droplet");
        registeredCandidate("picker_search", "search");
        registeredRole("picker_scroll_track", UiThemeManager.ROLE_ICON_SCROLL_TRACK);
        registeredRole("picker_scroll_thumb", UiThemeManager.ROLE_ICON_SCROLL_THUMB);
    }

    private static void registerStyleIcons() {
        preloadedCandidate("style_align_left", "align-left");
        preloadedCandidate("style_align_center", "align-center");
        preloadedCandidate("style_align_right", "align-right");
        preloadedCandidate("style_bold", "bold");
        preloadedCandidate("style_italic", "italic");
        preloadedRole("style_color", UiThemeManager.ROLE_ICON_INTERACTIVE, "color");
        registeredRole("palette_add", UiThemeManager.ROLE_ICON_SUCCESS, "add");
        registeredRole("palette_remove", UiThemeManager.ROLE_ICON_ERROR, "delete");
    }

    private static void registerWindowAndHudIcons() {
        registeredRole("window_drag_handle", UiThemeManager.ROLE_ICON_MUTED);
    }

    private static void registerAliases() {
        alias("context_repeat_off", "context_repeat-off");
        alias("context_eye_off", "context_eye-off");
        alias("context_audio_lines", "context_audio-lines");
        alias("context_reset", "reset");
        alias("edit", "editor");
        alias("toggle_editor", "editor");
        alias("pin", "window_pin");
    }

    private static void preloaded(String key) {
        register(key, UiThemeManager.ROLE_ICON_DEFAULT, true, "general");
    }

    private static void preloadedCandidate(String key, String... candidates) {
        register(key, UiThemeManager.ROLE_ICON_DEFAULT, true, "general", candidates);
    }

    private static void preloadedRole(String key, String role, String... candidates) {
        register(key, role, true, "general", candidates);
    }

    private static void registeredDefault(String key) {
        register(key, UiThemeManager.ROLE_ICON_DEFAULT, false, "general");
    }

    private static void registeredCandidate(String key, String... candidates) {
        register(key, UiThemeManager.ROLE_ICON_DEFAULT, false, "general", candidates);
    }

    private static void registeredRole(String key, String role, String... candidates) {
        register(key, role, false, "general", candidates);
    }

    private static void register(String key, String role, boolean preload, String category, String... candidates) {
        String normalized = normalizeKey(key);
        if (normalized.isBlank()) {
            return;
        }
        ENTRIES.put(normalized, new Entry(
                normalized,
                role == null || role.isBlank() ? UiThemeManager.ROLE_ICON_DEFAULT : role,
                preload,
                category == null || category.isBlank() ? "general" : category,
                List.of(candidates == null ? new String[0] : candidates)
        ));
    }

    private static void alias(String alias, String canonical) {
        String normalizedAlias = normalizeKey(alias);
        String normalizedCanonical = normalizeKey(canonical);
        if (!normalizedAlias.isBlank() && ENTRIES.containsKey(normalizedCanonical)) {
            ALIASES.put(normalizedAlias, normalizedCanonical);
        }
    }

    private static void add(List<String> names, String value) {
        String clean = normalizeKey(value);
        if (clean.isBlank() || names.contains(clean)) {
            return;
        }
        names.add(clean);
    }

    public record Entry(String key, String defaultRole, boolean preload, String category, List<String> resourceCandidates) {
        public Entry {
            resourceCandidates = List.copyOf(resourceCandidates);
        }
    }
}
