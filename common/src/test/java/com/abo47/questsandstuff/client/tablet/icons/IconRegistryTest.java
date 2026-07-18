package com.abo47.questsandstuff.client.tablet.icons;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IconRegistryTest {
    @Test
    void prewarmKeysComeFromRegistryEntries() {
        List<String> preloads = IconRegistry.preloadKeys();

        assertTrue(preloads.contains("tools"));
        assertTrue(preloads.contains("context_open"));
        assertTrue(preloads.contains("context_focus"));
        assertTrue(preloads.contains("hud_layout"));
        assertFalse(preloads.contains("context_repeat_off"));
        assertTrue(IconRegistry.registered("mode_tags"));
    }

    @Test
    void aliasesResolveToFallbackResourceCandidates() {
        assertEquals(
                List.of("mode_tags.png", "name_tag.png"),
                IconRegistry.candidateFiles("mode_tags")
        );
        assertTrue(IconRegistry.candidateFiles("context_repeat_off").contains("repeat-off.png"));
        assertTrue(IconRegistry.candidateFiles("pin").contains("window_pin.png"));
    }

    @Test
    void defaultRolesUseRegistryKeysAndAliases() {
        Map<String, String> roles = IconRegistry.defaultIconRoles();

        assertEquals(UiThemeManager.ROLE_ICON_SUCCESS, roles.get("send-horizontal"));
        assertEquals(UiThemeManager.ROLE_ICON_WARNING, roles.get("context_repeat-off"));
        assertEquals(UiThemeManager.ROLE_ICON_WARNING, roles.get("context_repeat_off"));
        assertEquals(UiThemeManager.ROLE_ICON_SCROLL_TRACK, roles.get("picker_scroll_track"));
    }
}
