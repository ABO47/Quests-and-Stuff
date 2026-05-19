package com.abo47.questsandstuff;

import net.minecraftforge.common.ForgeConfigSpec;

public final class QuestsAndStuffConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec.BooleanValue DEBUG_LOGGING;
    public static final ForgeConfigSpec.BooleanValue UI_ANIMATIONS;
    public static final ForgeConfigSpec.BooleanValue CONTEXT_MENU_ANIMATIONS;
    public static final ForgeConfigSpec.BooleanValue TOOLS_MENU_ANIMATIONS;
    public static final ForgeConfigSpec.BooleanValue MINIMAP_ANIMATIONS;
    public static final ForgeConfigSpec.BooleanValue QUEST_WINDOW_ANIMATIONS;
    public static final ForgeConfigSpec.BooleanValue POPUP_WINDOW_ANIMATIONS;
    public static final ForgeConfigSpec.BooleanValue CONNECTION_ANIMATIONS;
    public static final ForgeConfigSpec.BooleanValue CHAPTER_SWITCH_ANIMATIONS;

    static {
        ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();

        commonBuilder.push("debug");
        DEBUG_LOGGING = commonBuilder
                .comment("Enable verbose Quests and Stuff debug logging and debug files.")
                .define("debugLogging", false);
        commonBuilder.pop();

        COMMON_SPEC = commonBuilder.build();

        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();

        clientBuilder.push("animations");
        UI_ANIMATIONS = clientBuilder
                .comment("Enable Quests and Stuff UI animations globally. When false, all animation-specific toggles below are ignored.")
                .define("uiAnimations", true);
        CONTEXT_MENU_ANIMATIONS = clientBuilder
                .comment("Enable context menu reveal animations.")
                .define("contextMenuAnimations", true);
        TOOLS_MENU_ANIMATIONS = clientBuilder
                .comment("Enable tools menu sheet reveal animations.")
                .define("toolsMenuAnimations", true);
        MINIMAP_ANIMATIONS = clientBuilder
                .comment("Enable minimap expand and collapse animations.")
                .define("minimapAnimations", true);
        QUEST_WINDOW_ANIMATIONS = clientBuilder
                .comment("Enable quest details window source-origin open animations.")
                .define("questWindowAnimations", true);
        POPUP_WINDOW_ANIMATIONS = clientBuilder
                .comment("Enable picker, settings, and other popup window open and close animations.")
                .define("popupWindowAnimations", true);
        CONNECTION_ANIMATIONS = clientBuilder
                .comment("Enable prerequisite connection draw animations.")
                .define("connectionAnimations", true);
        CHAPTER_SWITCH_ANIMATIONS = clientBuilder
                .comment("Enable canvas slide and fade animations when switching chapters.")
                .define("chapterSwitchAnimations", true);
        clientBuilder.pop();

        CLIENT_SPEC = clientBuilder.build();
    }

    private QuestsAndStuffConfig() {
    }

    public static boolean debugLoggingEnabled() {
        return DEBUG_LOGGING.get();
    }

    public static void setDebugLoggingEnabled(boolean enabled) {
        setCommonBoolean(DEBUG_LOGGING, enabled);
    }

    public static boolean uiAnimationsEnabled() {
        return UI_ANIMATIONS.get();
    }

    public static void setUiAnimationsEnabled(boolean enabled) {
        setClientBoolean(UI_ANIMATIONS, enabled);
    }

    public static boolean contextMenuAnimationSettingEnabled() {
        return CONTEXT_MENU_ANIMATIONS.get();
    }

    public static boolean contextMenuAnimationsEnabled() {
        return UI_ANIMATIONS.get() && CONTEXT_MENU_ANIMATIONS.get();
    }

    public static void setContextMenuAnimationsEnabled(boolean enabled) {
        setClientBoolean(CONTEXT_MENU_ANIMATIONS, enabled);
    }

    public static boolean toolsMenuAnimationSettingEnabled() {
        return TOOLS_MENU_ANIMATIONS.get();
    }

    public static boolean toolsMenuAnimationsEnabled() {
        return UI_ANIMATIONS.get() && TOOLS_MENU_ANIMATIONS.get();
    }

    public static void setToolsMenuAnimationsEnabled(boolean enabled) {
        setClientBoolean(TOOLS_MENU_ANIMATIONS, enabled);
    }

    public static boolean minimapAnimationSettingEnabled() {
        return MINIMAP_ANIMATIONS.get();
    }

    public static boolean minimapAnimationsEnabled() {
        return UI_ANIMATIONS.get() && MINIMAP_ANIMATIONS.get();
    }

    public static void setMinimapAnimationsEnabled(boolean enabled) {
        setClientBoolean(MINIMAP_ANIMATIONS, enabled);
    }

    public static boolean questWindowAnimationSettingEnabled() {
        return QUEST_WINDOW_ANIMATIONS.get();
    }

    public static boolean questWindowAnimationsEnabled() {
        return UI_ANIMATIONS.get() && QUEST_WINDOW_ANIMATIONS.get();
    }

    public static void setQuestWindowAnimationsEnabled(boolean enabled) {
        setClientBoolean(QUEST_WINDOW_ANIMATIONS, enabled);
    }

    public static boolean popupWindowAnimationSettingEnabled() {
        return POPUP_WINDOW_ANIMATIONS.get();
    }

    public static boolean popupWindowAnimationsEnabled() {
        return UI_ANIMATIONS.get() && POPUP_WINDOW_ANIMATIONS.get();
    }

    public static void setPopupWindowAnimationsEnabled(boolean enabled) {
        setClientBoolean(POPUP_WINDOW_ANIMATIONS, enabled);
    }

    public static boolean connectionAnimationSettingEnabled() {
        return CONNECTION_ANIMATIONS.get();
    }

    public static boolean connectionAnimationsEnabled() {
        return UI_ANIMATIONS.get() && CONNECTION_ANIMATIONS.get();
    }

    public static void setConnectionAnimationsEnabled(boolean enabled) {
        setClientBoolean(CONNECTION_ANIMATIONS, enabled);
    }

    public static boolean chapterSwitchAnimationSettingEnabled() {
        return CHAPTER_SWITCH_ANIMATIONS.get();
    }

    public static boolean chapterSwitchAnimationsEnabled() {
        return UI_ANIMATIONS.get() && CHAPTER_SWITCH_ANIMATIONS.get();
    }

    public static void setChapterSwitchAnimationsEnabled(boolean enabled) {
        setClientBoolean(CHAPTER_SWITCH_ANIMATIONS, enabled);
    }

    private static void setClientBoolean(ForgeConfigSpec.BooleanValue value, boolean enabled) {
        if (value.get() == enabled) {
            return;
        }
        value.set(enabled);
        CLIENT_SPEC.save();
    }

    private static void setCommonBoolean(ForgeConfigSpec.BooleanValue value, boolean enabled) {
        if (value.get() == enabled) {
            return;
        }
        value.set(enabled);
        COMMON_SPEC.save();
    }
}
