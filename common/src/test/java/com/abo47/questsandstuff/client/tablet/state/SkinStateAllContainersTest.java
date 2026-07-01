package com.abo47.questsandstuff.client.tablet.state;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkinStateAllContainersTest {

    private static final List<String> ALL_CONTAINER_TARGETS = List.of(
            "quests_chapter_list",
            "quests_chapter",
            "quests_splitter",
            "quests_canvas",
            "quests_canvas_background",
            "quest_details_splitter",
            "quest_details_modal",
            "quest_details_description_canvas",
            "quest_details_canvas_panel",
            "quest_details_canvas_background",
            "quest_details_objectives",
            "teams_member_list",
            "home_inner",
            "root"
    );

    private static final List<String> ALL_APPS = List.of("quests", "teams", "home");

    private static final String SAMPLE_TEXTURE = "stretch|assets/test/texture.png";

    @Test
    void everyContainerCanBeSetAndCleared() {
        TabletUiState state = new TabletUiState();
        for (String target : ALL_CONTAINER_TARGETS) {
            state.root.skinFillOverrides.put(target, SAMPLE_TEXTURE);
            assertEquals(SAMPLE_TEXTURE, state.root.skinFillOverrides.get(target),
                    "should contain override for " + target);
            assertFalse(state.root.skinFillOverrides.isEmpty());

            state.root.skinFillOverrides.remove(target);
            assertFalse(state.root.skinFillOverrides.containsKey(target),
                    "should be cleared for " + target);
        }
        assertTrue(state.root.skinFillOverrides.isEmpty());
    }

    @Test
    void everyContainerCanBeSetAndClearedWithAppPrefix() {
        TabletUiState state = new TabletUiState();
        for (String app : ALL_APPS) {
            for (String target : ALL_CONTAINER_TARGETS) {
                String prefixedKey = app + ":" + target;
                state.root.skinFillOverrides.put(prefixedKey, SAMPLE_TEXTURE);
                assertEquals(SAMPLE_TEXTURE, state.root.skinFillOverrides.get(prefixedKey),
                        "should contain override for " + prefixedKey);

                state.root.skinFillOverrides.remove(prefixedKey);
                assertFalse(state.root.skinFillOverrides.containsKey(prefixedKey),
                        "should be cleared for " + prefixedKey);
            }
        }
        assertTrue(state.root.skinFillOverrides.isEmpty());
    }

    @Test
    void allContainersCanBeSetTogetherThenClearedTogether() {
        TabletUiState state = new TabletUiState();

        for (String target : ALL_CONTAINER_TARGETS) {
            state.root.skinFillOverrides.put(target, SAMPLE_TEXTURE);
        }
        assertEquals(ALL_CONTAINER_TARGETS.size(), state.root.skinFillOverrides.size());

        for (String target : ALL_CONTAINER_TARGETS) {
            assertEquals(SAMPLE_TEXTURE, state.root.skinFillOverrides.get(target));
        }

        state.root.skinFillOverrides.clear();
        assertTrue(state.root.skinFillOverrides.isEmpty());
    }

    @Test
    void allContainersWithAppPrefixCanBeSetTogetherThenCleared() {
        TabletUiState state = new TabletUiState();
        int expected = ALL_CONTAINER_TARGETS.size() * ALL_APPS.size();

        for (String app : ALL_APPS) {
            for (String target : ALL_CONTAINER_TARGETS) {
                state.root.skinFillOverrides.put(app + ":" + target, SAMPLE_TEXTURE);
            }
        }
        assertEquals(expected, state.root.skinFillOverrides.size());

        for (String app : ALL_APPS) {
            for (String target : ALL_CONTAINER_TARGETS) {
                assertEquals(SAMPLE_TEXTURE, state.root.skinFillOverrides.get(app + ":" + target));
            }
        }

        state.root.skinFillOverrides.clear();
        assertTrue(state.root.skinFillOverrides.isEmpty());
    }

    @Test
    void questsAppContainersDoNotLeakIntoTeamsAppViaAppPrefixFiltering() {
        TabletUiState state = new TabletUiState();
        state.root.currentApp = "quests";
        String appPrefix = "quests:";

        state.root.skinFillOverrides.put("quests:quests_canvas", SAMPLE_TEXTURE);
        state.root.skinFillOverrides.put("teams:teams_member_list", SAMPLE_TEXTURE);
        state.root.skinFillOverrides.put("home:home_inner", SAMPLE_TEXTURE);
        state.root.skinFillOverrides.put("root", SAMPLE_TEXTURE);

        for (Map.Entry<String, String> entry : state.root.skinFillOverrides.entrySet()) {
            String key = entry.getKey();
            if (key.contains(":") && !key.startsWith(appPrefix)) {
                assertFalse(appliesToCurrentApp(key, state.root.currentApp),
                        key + " should not apply to quests app");
            }
        }
    }

    @Test
    void sameContainerTargetInDifferentAppsAreIndependent() {
        TabletUiState state = new TabletUiState();

        state.root.skinFillOverrides.put("quests:quests_canvas", "stretch|quests_texture.png");
        state.root.skinFillOverrides.put("teams:quests_canvas", "tile|teams_texture.png");

        assertEquals("stretch|quests_texture.png", state.root.skinFillOverrides.get("quests:quests_canvas"));
        assertEquals("tile|teams_texture.png", state.root.skinFillOverrides.get("teams:quests_canvas"));

        state.root.skinFillOverrides.remove("quests:quests_canvas");
        assertFalse(state.root.skinFillOverrides.containsKey("quests:quests_canvas"));
        assertEquals("tile|teams_texture.png", state.root.skinFillOverrides.get("teams:quests_canvas"),
                "teams override should survive after quests override removed");
    }

    private static boolean appliesToCurrentApp(String key, String currentApp) {
        if (!key.contains(":")) return true;
        return key.startsWith(currentApp + ":");
    }
}
