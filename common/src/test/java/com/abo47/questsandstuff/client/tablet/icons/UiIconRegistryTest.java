package com.abo47.questsandstuff.client.tablet.icons;

import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiIconRegistryTest {
    @Test
    void prewarmKeysComeFromRegistryEntries() {
        List<String> preloads = UiIconRegistry.preloadKeys();

        assertTrue(preloads.contains("tools"));
        assertTrue(preloads.contains("context_open"));
        assertTrue(preloads.contains("context_focus"));
        assertTrue(preloads.contains("hud_layout"));
        assertFalse(preloads.contains("context_repeat_off"));
        assertTrue(UiIconRegistry.registered("mode_tags"));
    }

    @Test
    void aliasesResolveToFallbackResourceCandidates() {
        assertEquals(
                List.of("mode_tags.png", "name_tag.png"),
                UiIconRegistry.candidateFiles("mode_tags")
        );
        assertTrue(UiIconRegistry.candidateFiles("context_repeat_off").contains("repeat-off.png"));
        assertTrue(UiIconRegistry.candidateFiles("pin").contains("window_pin.png"));
    }

    @Test
    void defaultRolesUseRegistryKeysAndAliases() {
        Map<String, String> roles = UiIconRegistry.defaultIconRoles();

        assertEquals(UiThemeManager.ROLE_ICON_SUCCESS, roles.get("send-horizontal"));
        assertEquals(UiThemeManager.ROLE_ICON_WARNING, roles.get("context_repeat-off"));
        assertEquals(UiThemeManager.ROLE_ICON_WARNING, roles.get("context_repeat_off"));
        assertEquals(UiThemeManager.ROLE_ICON_SCROLL_TRACK, roles.get("picker_scroll_track"));
    }
}
