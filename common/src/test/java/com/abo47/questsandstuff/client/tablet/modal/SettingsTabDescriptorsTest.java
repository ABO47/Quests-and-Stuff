package com.abo47.questsandstuff.client.tablet.modal;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsTabDescriptorsTest {
    @Test
    void tabRegistryOwnsSettingsSectionsInOrder() {
        assertIterableEquals(
                List.of("themes", "canvas", "hud", "animations", "debug", "skin", "chunkClaims"),
                SettingsTabDescriptors.all().stream()
                        .map(SettingsTabDescriptor::logName)
                        .toList()
        );
    }

    @Test
    void invalidTabFallsBackToThemes() {
        assertEquals(SettingsTabDescriptors.THEMES, SettingsTabDescriptors.activeTab(-99));
        assertTrue(SettingsTabDescriptors.descriptor(SettingsTabDescriptors.THEMES).themePicker());
    }

    @Test
    void canvasHudAnimationAndDebugTabsExposeOptionDescriptors() {
        TabletUiState state = new TabletUiState();

        assertIterableEquals(
                List.of(
                        "fullScreenMode",
                        "minimap",
                        "visualMinimap",
                        "readOnlyCanvasFocus",
                        "questEffectIcons",
                        "canvasMiniNotifications"
                ),
                optionIds(SettingsTabDescriptors.descriptor(SettingsTabDescriptors.CANVAS).options(state))
        );
        assertIterableEquals(
                List.of("editHudLayout", "completionHud", "completionHudSound", "completionHudDurationMs"),
                optionIds(SettingsTabDescriptors.descriptor(SettingsTabDescriptors.HUD).options(state))
        );
        assertIterableEquals(
                List.of(
                        "uiAnimations",
                        "contextMenuAnimations",
                        "toolsMenuAnimations",
                        "minimapAnimations",
                        "questWindowAnimations",
                        "popupWindowAnimations",
                        "connectionAnimations",
                        "chapterSwitchAnimations"
                ),
                optionIds(SettingsTabDescriptors.descriptor(SettingsTabDescriptors.ANIMATIONS).options(state))
        );
        assertIterableEquals(
                List.of("debugLogging"),
                optionIds(SettingsTabDescriptors.descriptor(SettingsTabDescriptors.DEBUG).options(state))
        );
    }

    @Test
    void chunkClaimsTabExposesProtectionAndCapOptions() {
        TabletUiState state = new TabletUiState();

        assertIterableEquals(
                List.of(
                        "protectBreakPlace",
                        "protectInteraction",
                        "protectExplosions",
                        "protectMobGriefing",
                        "protectPvp",
                        "protectFire",
                        "maxClaimedChunks",
                        "maxForceLoadedChunks"
                ),
                optionIds(SettingsTabDescriptors.descriptor(SettingsTabDescriptors.CHUNK_CLAIMS).options(state))
        );

        SettingsOptionDescriptor maxClaimed = option(SettingsTabDescriptors.CHUNK_CLAIMS, state, "maxClaimedChunks");
        assertTrue(maxClaimed.number());
        assertEquals(QuestsAndStuffConfig.minChunkClaimCap(), maxClaimed.min());
        assertEquals(QuestsAndStuffConfig.maxChunkClaimCap(), maxClaimed.max());

        SettingsOptionDescriptor protect = option(SettingsTabDescriptors.CHUNK_CLAIMS, state, "protectBreakPlace");
        assertEquals(SettingsOptionKind.TOGGLE, protect.kind());
    }

    @Test
    void descriptorsCarryRowKindAndValueMetadata() {
        TabletUiState state = new TabletUiState();

        SettingsOptionDescriptor hudAction = option(SettingsTabDescriptors.HUD, state, "editHudLayout");
        assertTrue(hudAction.isAction());

        SettingsOptionDescriptor duration = option(SettingsTabDescriptors.HUD, state, "completionHudDurationMs");
        assertTrue(duration.number());
        assertEquals(QuestsAndStuffConfig.MIN_COMPLETION_HUD_DURATION_MS, duration.min());
        assertEquals(QuestsAndStuffConfig.MAX_COMPLETION_HUD_DURATION_MS, duration.max());
        assertEquals(5, duration.maxLength());

        SettingsOptionDescriptor contextMenuAnimation = option(SettingsTabDescriptors.ANIMATIONS, state, "contextMenuAnimations");
        assertFalse(contextMenuAnimation.restartRequired());
        assertTrue(contextMenuAnimation.requiresGlobalAnimation());

        SettingsOptionDescriptor minimap = option(SettingsTabDescriptors.CANVAS, state, "minimap");
        assertEquals(SettingsOptionKind.TOGGLE, minimap.kind());
        assertFalse(minimap.requiresGlobalAnimation());
    }

    private static List<String> optionIds(List<SettingsOptionDescriptor> options) {
        return options.stream()
                .map(SettingsOptionDescriptor::id)
                .toList();
    }

    private static SettingsOptionDescriptor option(int tab, TabletUiState state, String id) {
        return SettingsTabDescriptors.descriptor(tab).options(state).stream()
                .filter(option -> option.id().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
