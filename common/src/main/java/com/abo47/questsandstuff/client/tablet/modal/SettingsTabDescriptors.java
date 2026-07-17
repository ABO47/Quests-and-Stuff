package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.quest.hud.QuestHudLayoutEditScreen;
import com.abo47.questsandstuff.client.tablet.bootstrap.TabletScreenManager;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.text.ChunkClaimTranslationKeys;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.chunkclaim.C2SChunkClaimConfigPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

import java.util.ArrayList;
import java.util.List;

public final class SettingsTabDescriptors {
    static final int THEMES = 0;
    static final int CANVAS = 1;
    static final int HUD = 2;
    static final int ANIMATIONS = 3;
    static final int DEBUG = 4;
    static final int SKIN = 5;
    static final int CHUNK_CLAIMS = 6;

    private static final List<SettingsTabDescriptor> TABS = List.of(
            new SettingsTabDescriptor(THEMES, "themes", "ui.questsandstuff.settings.tab_themes", true, state -> List.of()),
            new SettingsTabDescriptor(CANVAS, "canvas", "ui.questsandstuff.settings.tab_canvas", false, SettingsTabDescriptors::canvasOptions),
            new SettingsTabDescriptor(HUD, "hud", "ui.questsandstuff.settings.tab_hud", false, state -> hudOptions()),
            new SettingsTabDescriptor(ANIMATIONS, "animations", "ui.questsandstuff.settings.tab_animations", false, state -> animationOptions()),
            new SettingsTabDescriptor(DEBUG, "debug", "ui.questsandstuff.settings.tab_debug", false, state -> debugOptions()),
            new SettingsTabDescriptor(SKIN, "skin", "ui.questsandstuff.settings.tab_skin", false, SettingsTabDescriptors::skinOptions),
            new SettingsTabDescriptor(CHUNK_CLAIMS, "chunkClaims", ChunkClaimTranslationKeys.SETTINGS_TAB, false, SettingsTabDescriptors::chunkClaimOptions)
    );

    private SettingsTabDescriptors() {
    }

    public static List<SettingsTabDescriptor> all() {
        return TABS;
    }

    public static int activeTab(int tab) {
        return descriptor(tab).id();
    }

    static SettingsTabDescriptor active(TabletUiState state) {
        return descriptor(state.settings.currentTab);
    }

    public static SettingsTabDescriptor descriptor(int tab) {
        for (SettingsTabDescriptor descriptor : TABS) {
            if (descriptor.id() == tab) {
                return descriptor;
            }
        }
        return TABS.get(0);
    }

    public static List<SettingsOptionDescriptor> search(TabletUiState state, String query) {
        String q = SearchFilter.normalizeUserInput(query);
        List<SettingsOptionDescriptor> matches = new ArrayList<>();
        for (SettingsTabDescriptor tab : TABS) {
            if (tab.themePicker()) {
                continue;
            }
            for (SettingsOptionDescriptor option : tab.options(state)) {
                if (optionMatches(option, q)) {
                    matches.add(option);
                }
            }
        }
        return matches;
    }

    private static boolean optionMatches(SettingsOptionDescriptor option, String q) {
        if (q.isBlank()) {
            return true;
        }
        String label = SearchFilter.normalizeUserInput(I18n.get(option.labelKey()));
        if (label.contains(q)) {
            return true;
        }
        String description = SearchFilter.normalizeUserInput(I18n.get(option.descriptionKey()));
        return description.contains(q);
    }

    private static List<SettingsOptionDescriptor> canvasOptions(TabletUiState state) {
        return List.of(
                new SettingsOptionDescriptor(
                        "fullScreenMode",
                        "ui.questsandstuff.settings.full_screen_mode",
                        "ui.questsandstuff.settings.full_screen_mode_desc",
                        QuestsAndStuffConfig::fullScreenModeEnabled,
                        enabled -> setFullScreenMode(state, enabled),
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "minimap",
                        "ui.questsandstuff.settings.minimap",
                        "ui.questsandstuff.settings.minimap_desc",
                        QuestsAndStuffConfig::minimapEnabled,
                        QuestsAndStuffConfig::setMinimapEnabled,
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "visualMinimap",
                        "ui.questsandstuff.settings.visual_minimap",
                        "ui.questsandstuff.settings.visual_minimap_desc",
                        QuestsAndStuffConfig::visualMinimapEnabled,
                        QuestsAndStuffConfig::setVisualMinimapEnabled,
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "readOnlyCanvasFocus",
                        "ui.questsandstuff.settings.read_only_canvas_focus",
                        "ui.questsandstuff.settings.read_only_canvas_focus_desc",
                        QuestsAndStuffConfig::readOnlyCanvasFocusEnabled,
                        QuestsAndStuffConfig::setReadOnlyCanvasFocusEnabled,
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "questEffectIcons",
                        "ui.questsandstuff.settings.quest_effect_icons",
                        "ui.questsandstuff.settings.quest_effect_icons_desc",
                        QuestsAndStuffConfig::questEffectIconsEnabled,
                        QuestsAndStuffConfig::setQuestEffectIconsEnabled,
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "canvasMiniNotifications",
                        "ui.questsandstuff.settings.canvas_mini_notifications",
                        "ui.questsandstuff.settings.canvas_mini_notifications_desc",
                        QuestsAndStuffConfig::canvasMiniNotificationsEnabled,
                        QuestsAndStuffConfig::setCanvasMiniNotificationsEnabled,
                        false,
                        false
                )
        );
    }

    private static List<SettingsOptionDescriptor> hudOptions() {
        return List.of(
                new SettingsOptionDescriptor(
                        "editHudLayout",
                        "ui.questsandstuff.hud.layout.button",
                        "ui.questsandstuff.hud.layout.button_tooltip",
                        () -> {
                            Minecraft.getInstance().setScreen(new QuestHudLayoutEditScreen());
                            QuestsAndStuffMod.debugLog("[QnS:UI] hud layout editor opened from settings");
                        }
                ),
                new SettingsOptionDescriptor(
                        "completionHud",
                        "ui.questsandstuff.settings.completion_hud",
                        "ui.questsandstuff.settings.completion_hud_desc",
                        QuestsAndStuffConfig::completionHudEnabled,
                        QuestsAndStuffConfig::setCompletionHudEnabled,
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "completionHudSound",
                        "ui.questsandstuff.settings.completion_hud_sound",
                        "ui.questsandstuff.settings.completion_hud_sound_desc",
                        QuestsAndStuffConfig::completionHudSoundEnabled,
                        QuestsAndStuffConfig::setCompletionHudSoundEnabled,
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "completionHudDurationMs",
                        "ui.questsandstuff.settings.completion_hud_duration",
                        "ui.questsandstuff.settings.completion_hud_duration_desc",
                        QuestsAndStuffConfig::completionHudDurationMs,
                        QuestsAndStuffConfig::setCompletionHudDurationMs,
                        QuestsAndStuffConfig.MIN_COMPLETION_HUD_DURATION_MS,
                        QuestsAndStuffConfig.MAX_COMPLETION_HUD_DURATION_MS,
                        5,
                        false,
                        "ui.questsandstuff.settings.duration_unit_ms"
                )
        );
    }

    private static void setFullScreenMode(TabletUiState state, boolean enabled) {
        QuestsAndStuffConfig.setFullScreenModeEnabled(enabled);
        TabletScreenManager.applyTabletLayoutMode(state);
    }

    private static List<SettingsOptionDescriptor> animationOptions() {
        return List.of(
                new SettingsOptionDescriptor(
                        "uiAnimations",
                        "ui.questsandstuff.settings.ui_animations",
                        "ui.questsandstuff.settings.ui_animations_desc",
                        QuestsAndStuffConfig::uiAnimationsEnabled,
                        QuestsAndStuffConfig::setUiAnimationsEnabled,
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "contextMenuAnimations",
                        "ui.questsandstuff.settings.context_menu_animations",
                        "ui.questsandstuff.settings.context_menu_animations_desc",
                        QuestsAndStuffConfig::contextMenuAnimationSettingEnabled,
                        QuestsAndStuffConfig::setContextMenuAnimationsEnabled,
                        false,
                        true
                ),
                new SettingsOptionDescriptor(
                        "toolsMenuAnimations",
                        "ui.questsandstuff.settings.tools_menu_animations",
                        "ui.questsandstuff.settings.tools_menu_animations_desc",
                        QuestsAndStuffConfig::toolsMenuAnimationSettingEnabled,
                        QuestsAndStuffConfig::setToolsMenuAnimationsEnabled,
                        false,
                        true
                ),
                new SettingsOptionDescriptor(
                        "minimapAnimations",
                        "ui.questsandstuff.settings.minimap_animations",
                        "ui.questsandstuff.settings.minimap_animations_desc",
                        QuestsAndStuffConfig::minimapAnimationSettingEnabled,
                        QuestsAndStuffConfig::setMinimapAnimationsEnabled,
                        false,
                        true
                ),
                new SettingsOptionDescriptor(
                        "questWindowAnimations",
                        "ui.questsandstuff.settings.quest_window_animations",
                        "ui.questsandstuff.settings.quest_window_animations_desc",
                        QuestsAndStuffConfig::questWindowAnimationSettingEnabled,
                        QuestsAndStuffConfig::setQuestWindowAnimationsEnabled,
                        false,
                        true
                ),
                new SettingsOptionDescriptor(
                        "popupWindowAnimations",
                        "ui.questsandstuff.settings.popup_window_animations",
                        "ui.questsandstuff.settings.popup_window_animations_desc",
                        QuestsAndStuffConfig::popupWindowAnimationSettingEnabled,
                        QuestsAndStuffConfig::setPopupWindowAnimationsEnabled,
                        false,
                        true
                ),
                new SettingsOptionDescriptor(
                        "connectionAnimations",
                        "ui.questsandstuff.settings.connection_animations",
                        "ui.questsandstuff.settings.connection_animations_desc",
                        QuestsAndStuffConfig::connectionAnimationSettingEnabled,
                        QuestsAndStuffConfig::setConnectionAnimationsEnabled,
                        false,
                        true
                ),
                new SettingsOptionDescriptor(
                        "chapterSwitchAnimations",
                        "ui.questsandstuff.settings.chapter_switch_animations",
                        "ui.questsandstuff.settings.chapter_switch_animations_desc",
                        QuestsAndStuffConfig::chapterSwitchAnimationSettingEnabled,
                        QuestsAndStuffConfig::setChapterSwitchAnimationsEnabled,
                        false,
                        true
                )
        );
    }

    private static List<SettingsOptionDescriptor> debugOptions() {
        return List.of(
                new SettingsOptionDescriptor(
                        "debugLogging",
                        "ui.questsandstuff.settings.debug_logging",
                        "ui.questsandstuff.settings.debug_logging_desc",
                        QuestsAndStuffConfig::debugLoggingEnabled,
                        QuestsAndStuffConfig::setDebugLoggingEnabled,
                        false,
                        false
                )
        );
    }

    private static List<SettingsOptionDescriptor> skinOptions(TabletUiState state) {
        return List.of();
    }

    private static List<SettingsOptionDescriptor> chunkClaimOptions(TabletUiState state) {
        int capMin = QuestsAndStuffConfig.minChunkClaimCap();
        int capMax = QuestsAndStuffConfig.maxChunkClaimCap();
        return List.of(
                new SettingsOptionDescriptor(
                        "protectBreakPlace",
                        ChunkClaimTranslationKeys.SETTING_PROTECT_BREAK_PLACE,
                        ChunkClaimTranslationKeys.SETTING_PROTECT_BREAK_PLACE + "_desc",
                        QuestsAndStuffConfig::chunkClaimProtectBreakPlace,
                        enabled -> {
                            QuestsAndStuffConfig.setChunkClaimProtectBreakPlace(enabled);
                            sendChunkClaimsConfig();
                        },
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "protectInteraction",
                        ChunkClaimTranslationKeys.SETTING_PROTECT_INTERACTION,
                        ChunkClaimTranslationKeys.SETTING_PROTECT_INTERACTION + "_desc",
                        QuestsAndStuffConfig::chunkClaimProtectInteraction,
                        enabled -> {
                            QuestsAndStuffConfig.setChunkClaimProtectInteraction(enabled);
                            sendChunkClaimsConfig();
                        },
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "protectExplosions",
                        ChunkClaimTranslationKeys.SETTING_PROTECT_EXPLOSIONS,
                        ChunkClaimTranslationKeys.SETTING_PROTECT_EXPLOSIONS + "_desc",
                        QuestsAndStuffConfig::chunkClaimProtectExplosions,
                        enabled -> {
                            QuestsAndStuffConfig.setChunkClaimProtectExplosions(enabled);
                            sendChunkClaimsConfig();
                        },
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "protectMobGriefing",
                        ChunkClaimTranslationKeys.SETTING_PROTECT_MOB_GRIEFING,
                        ChunkClaimTranslationKeys.SETTING_PROTECT_MOB_GRIEFING + "_desc",
                        QuestsAndStuffConfig::chunkClaimProtectMobGriefing,
                        enabled -> {
                            QuestsAndStuffConfig.setChunkClaimProtectMobGriefing(enabled);
                            sendChunkClaimsConfig();
                        },
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "protectPvp",
                        ChunkClaimTranslationKeys.SETTING_PROTECT_PVP,
                        ChunkClaimTranslationKeys.SETTING_PROTECT_PVP + "_desc",
                        QuestsAndStuffConfig::chunkClaimProtectPvp,
                        enabled -> {
                            QuestsAndStuffConfig.setChunkClaimProtectPvp(enabled);
                            sendChunkClaimsConfig();
                        },
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "protectFire",
                        ChunkClaimTranslationKeys.SETTING_PROTECT_FIRE,
                        ChunkClaimTranslationKeys.SETTING_PROTECT_FIRE + "_desc",
                        QuestsAndStuffConfig::chunkClaimProtectFire,
                        enabled -> {
                            QuestsAndStuffConfig.setChunkClaimProtectFire(enabled);
                            sendChunkClaimsConfig();
                        },
                        false,
                        false
                ),
                new SettingsOptionDescriptor(
                        "maxClaimedChunks",
                        ChunkClaimTranslationKeys.SETTING_MAX_CLAIMED,
                        ChunkClaimTranslationKeys.SETTING_MAX_CLAIMED + "_desc",
                        QuestsAndStuffConfig::chunkClaimMaxClaimedChunks,
                        value -> {
                            QuestsAndStuffConfig.setChunkClaimMaxClaimedChunks(value);
                            sendChunkClaimsConfig();
                        },
                        capMin,
                        capMax,
                        10,
                        false,
                        "ui.questsandstuff.settings.chunk_unit"
                ),
                new SettingsOptionDescriptor(
                        "maxForceLoadedChunks",
                        ChunkClaimTranslationKeys.SETTING_MAX_FORCE_LOADED,
                        ChunkClaimTranslationKeys.SETTING_MAX_FORCE_LOADED + "_desc",
                        QuestsAndStuffConfig::chunkClaimMaxForceLoadedChunks,
                        value -> {
                            QuestsAndStuffConfig.setChunkClaimMaxForceLoadedChunks(value);
                            sendChunkClaimsConfig();
                        },
                        capMin,
                        capMax,
                        10,
                        false,
                        "ui.questsandstuff.settings.chunk_unit"
                )
        );
    }

    private static void sendChunkClaimsConfig() {
        ModNetwork.sendToServer(new C2SChunkClaimConfigPacket(
                QuestsAndStuffConfig.chunkClaimProtectBreakPlace(),
                QuestsAndStuffConfig.chunkClaimProtectInteraction(),
                QuestsAndStuffConfig.chunkClaimProtectExplosions(),
                QuestsAndStuffConfig.chunkClaimProtectMobGriefing(),
                QuestsAndStuffConfig.chunkClaimProtectPvp(),
                QuestsAndStuffConfig.chunkClaimProtectFire(),
                QuestsAndStuffConfig.chunkClaimMaxClaimedChunks(),
                QuestsAndStuffConfig.chunkClaimMaxForceLoadedChunks()
        ));
    }
}
