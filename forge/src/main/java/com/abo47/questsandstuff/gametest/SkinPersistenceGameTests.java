package com.abo47.questsandstuff.gametest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiStatePersistence;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@GameTestHolder(QuestsAndStuffMod.MODID)
public final class SkinPersistenceGameTests {
    private static final Path SKIN_STATE_FILE = Path.of("config", "questsandstuff", "skin_state.json");

    private SkinPersistenceGameTests() {
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void emptyStatePersistenceRoundTrip(GameTestHelper helper) {
        try {
            TabletUiState original = new TabletUiState();
            TabletUiStatePersistence.writeSkinState(original);

            TabletUiState loaded = new TabletUiState();
            loaded.root.skinEditMode = true;
            loaded.root.skinEditSelectedTarget = "previous_selection";
            loaded.root.skinFillOverrides.put("dummy", "stretch|old.png");
            TabletUiStatePersistence.readSkinState(loaded);

            assertEqual(false, loaded.root.skinEditMode, "skinEditMode should default to false");
            assertEqual("", loaded.root.skinEditSelectedTarget, "skinEditSelectedTarget should default to empty");
            assertEqual(true, loaded.root.skinFillOverrides.isEmpty(), "skinFillOverrides should be empty");
        } catch (Exception e) {
            throw new GameTestAssertException("emptyStatePersistenceRoundTrip failed: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void fullStatePersistenceRoundTrip(GameTestHelper helper) {
        try {
            TabletUiState original = new TabletUiState();
            original.root.skinEditMode = true;
            original.root.skinEditSelectedTarget = "quests_canvas";
            original.root.skinFillOverrides.put("home_inner", "stretch|assets/bg.png");
            original.root.skinFillOverrides.put("quests:quest_details_modal", "tile|assets/tile.png");
            TabletUiStatePersistence.writeSkinState(original);

            TabletUiState loaded = new TabletUiState();
            TabletUiStatePersistence.readSkinState(loaded);

            assertEqual(true, loaded.root.skinEditMode, "skinEditMode");
            assertEqual("quests_canvas", loaded.root.skinEditSelectedTarget, "skinEditSelectedTarget");
            assertEqual(2, loaded.root.skinFillOverrides.size(), "skinFillOverrides count");
            assertEqual("stretch|assets/bg.png", loaded.root.skinFillOverrides.get("home_inner"), "non-prefixed key");
            assertEqual("tile|assets/tile.png", loaded.root.skinFillOverrides.get("quests:quest_details_modal"), "prefixed key");
        } catch (Exception e) {
            throw new GameTestAssertException("fullStatePersistenceRoundTrip failed: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void skinEditModeToggleSurvivesRoundTrip(GameTestHelper helper) {
        try {
            TabletUiState original = new TabletUiState();
            original.root.skinEditMode = true;
            TabletUiStatePersistence.writeSkinState(original);

            TabletUiState loaded = new TabletUiState();
            TabletUiStatePersistence.readSkinState(loaded);
            assertEqual(true, loaded.root.skinEditMode, "skinEditMode=true");

            original.root.skinEditMode = false;
            TabletUiStatePersistence.writeSkinState(original);

            TabletUiState reloaded = new TabletUiState();
            TabletUiStatePersistence.readSkinState(reloaded);
            assertEqual(false, reloaded.root.skinEditMode, "skinEditMode=false");
        } catch (Exception e) {
            throw new GameTestAssertException("skinEditModeToggleSurvivesRoundTrip failed: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void malformedFileFallsBackToDefaults(GameTestHelper helper) {
        try {
            Files.createDirectories(SKIN_STATE_FILE.getParent());
            Files.writeString(SKIN_STATE_FILE, "{not valid json", StandardCharsets.UTF_8);

            TabletUiState state = new TabletUiState();
            state.root.skinEditMode = true;
            TabletUiStatePersistence.readSkinState(state);

            assertEqual(false, state.root.skinEditMode, "skinEditMode should fall back to default");
            assertEqual("", state.root.skinEditSelectedTarget, "skinEditSelectedTarget should fall back to empty");
            assertEqual(true, state.root.skinFillOverrides.isEmpty(), "skinFillOverrides should fall back to empty");
        } catch (Exception e) {
            throw new GameTestAssertException("malformedFileFallsBackToDefaults failed: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void overrideInvalidEntriesAreSkippedOnRead(GameTestHelper helper) {
        try {
            Files.createDirectories(SKIN_STATE_FILE.getParent());
            String json = """
                    {
                      "skin_edit_mode": true,
                      "skin_edit_selected_target": "root",
                      "skin_fill_overrides": {
                        "valid_key": "stretch|real.png",
                        "empty_value_key": "",
                        "pipe_only_key": "|",
                        "blank_path_key": "stretch|"
                      }
                    }
                    """;
            Files.writeString(SKIN_STATE_FILE, json, StandardCharsets.UTF_8);

            TabletUiState state = new TabletUiState();
            TabletUiStatePersistence.readSkinState(state);

            assertEqual(true, state.root.skinEditMode, "skinEditMode read from json");
            assertEqual("root", state.root.skinEditSelectedTarget, "skinEditSelectedTarget read from json");
            assertEqual(1, state.root.skinFillOverrides.size(), "only one valid override should be loaded");
            assertEqual("stretch|real.png", state.root.skinFillOverrides.get("valid_key"), "valid key preserved");
        } catch (Exception e) {
            throw new GameTestAssertException("overrideInvalidEntriesAreSkippedOnRead failed: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void nullFieldsDoNotCorruptWrittenFile(GameTestHelper helper) {
        try {
            TabletUiState state = new TabletUiState();
            state.root.skinEditSelectedTarget = null;
            state.root.skinFillOverrides.put("some_key", null);
            TabletUiStatePersistence.writeSkinState(state);

            String content = Files.readString(SKIN_STATE_FILE, StandardCharsets.UTF_8);
            if (!content.contains("\"skin_edit_selected_target\": \"\"")) {
                throw new GameTestAssertException("null skinEditSelectedTarget should be written as empty string");
            }

            TabletUiState loaded = new TabletUiState();
            TabletUiStatePersistence.readSkinState(loaded);
            assertEqual("", loaded.root.skinEditSelectedTarget, "null target becomes empty string");
            assertEqual(true, loaded.root.skinFillOverrides.isEmpty(), "null override value should be skipped");
        } catch (Exception e) {
            throw new GameTestAssertException("nullFieldsDoNotCorruptWrittenFile failed: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void missingFileDoesNotOverrideExistingState(GameTestHelper helper) {
        try {
            cleanup();
            TabletUiState state = new TabletUiState();
            state.root.skinEditMode = true;
            state.root.skinEditSelectedTarget = "root";
            state.root.skinFillOverrides.put("home_inner", "stretch|bg.png");
            TabletUiStatePersistence.readSkinState(state);

            assertEqual(true, state.root.skinEditMode, "skinEditMode should not be changed by missing file");
            assertEqual("root", state.root.skinEditSelectedTarget, "skinEditSelectedTarget should not be changed by missing file");
            assertEqual("stretch|bg.png", state.root.skinFillOverrides.get("home_inner"), "overrides should not be cleared by missing file");
        } catch (Exception e) {
            throw new GameTestAssertException("missingFileDoesNotOverrideExistingState failed: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

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

    private static final String SAMPLE_TEX = "stretch|assets/test/bg.png";

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void allContainerKeysSetThenClearedSurvivesRoundTrip(GameTestHelper helper) {
        try {
            TabletUiState original = new TabletUiState();
            for (String target : ALL_CONTAINER_TARGETS) {
                original.root.skinFillOverrides.put(target, SAMPLE_TEX);
            }
            assertEqual(ALL_CONTAINER_TARGETS.size(), original.root.skinFillOverrides.size(), "all keys set before write");
            TabletUiStatePersistence.writeSkinState(original);

            TabletUiState loaded = new TabletUiState();
            TabletUiStatePersistence.readSkinState(loaded);
            assertEqual(ALL_CONTAINER_TARGETS.size(), loaded.root.skinFillOverrides.size(), "all keys survived read");
            for (String target : ALL_CONTAINER_TARGETS) {
                String val = loaded.root.skinFillOverrides.get(target);
                if (!SAMPLE_TEX.equals(val)) {
                    throw new GameTestAssertException("key " + target + " had value " + val);
                }
            }

            loaded.root.skinFillOverrides.clear();
            assertEqual(0, loaded.root.skinFillOverrides.size(), "all keys cleared");
            TabletUiStatePersistence.writeSkinState(loaded);

            TabletUiState reloaded = new TabletUiState();
            TabletUiStatePersistence.readSkinState(reloaded);
            assertEqual(true, reloaded.root.skinFillOverrides.isEmpty(), "all keys remain cleared after reload");
        } catch (Exception e) {
            throw new GameTestAssertException("allContainerKeysSetThenClearedSurvivesRoundTrip failed: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void allContainerKeysWithAppPrefixSurviveRoundTrip(GameTestHelper helper) {
        try {
            List<String> apps = List.of("quests", "teams", "home");
            TabletUiState original = new TabletUiState();
            int expected = ALL_CONTAINER_TARGETS.size() * apps.size();

            for (String app : apps) {
                for (String target : ALL_CONTAINER_TARGETS) {
                    original.root.skinFillOverrides.put(app + ":" + target, SAMPLE_TEX);
                }
            }
            assertEqual(expected, original.root.skinFillOverrides.size(), "all prefixed keys set");
            TabletUiStatePersistence.writeSkinState(original);

            TabletUiState loaded = new TabletUiState();
            TabletUiStatePersistence.readSkinState(loaded);
            assertEqual(expected, loaded.root.skinFillOverrides.size(), "all prefixed keys survived read");

            for (String app : apps) {
                for (String target : ALL_CONTAINER_TARGETS) {
                    String key = app + ":" + target;
                    String val = loaded.root.skinFillOverrides.get(key);
                    if (!SAMPLE_TEX.equals(val)) {
                        throw new GameTestAssertException("key " + key + " had value " + val);
                    }
                }
            }

            loaded.root.skinFillOverrides.clear();
            TabletUiStatePersistence.writeSkinState(loaded);

            TabletUiState reloaded = new TabletUiState();
            TabletUiStatePersistence.readSkinState(reloaded);
            assertEqual(true, reloaded.root.skinFillOverrides.isEmpty(), "prefixed keys remain cleared");
        } catch (Exception e) {
            throw new GameTestAssertException("allContainerKeysWithAppPrefixSurviveRoundTrip failed: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void questsAndTeamsOverridesAreIndependentAfterRoundTrip(GameTestHelper helper) {
        try {
            TabletUiState original = new TabletUiState();
            original.root.skinFillOverrides.put("quests:quests_canvas", "stretch|quests_bg.png");
            original.root.skinFillOverrides.put("quests:quests_chapter", "tile|quests_chapter.png");
            original.root.skinFillOverrides.put("quests:quest_details_modal", "stretch|details.png");
            original.root.skinFillOverrides.put("teams:teams_member_list", "tile|team_members.png");
            original.root.skinFillOverrides.put("teams:quests_canvas", "stretch|team_canvas.png");
            original.root.skinFillOverrides.put("home:home_inner", "stretch|home_bg.png");
            TabletUiStatePersistence.writeSkinState(original);

            TabletUiState loaded = new TabletUiState();
            TabletUiStatePersistence.readSkinState(loaded);

            assertEqual(6, loaded.root.skinFillOverrides.size(), "all 6 app-prefixed keys");

            assertEqual("stretch|quests_bg.png", loaded.root.skinFillOverrides.get("quests:quests_canvas"), "quests_canvas in quests app");
            assertEqual("tile|quests_chapter.png", loaded.root.skinFillOverrides.get("quests:quests_chapter"), "quests_chapter in quests app");
            assertEqual("stretch|details.png", loaded.root.skinFillOverrides.get("quests:quest_details_modal"), "quest_details_modal in quests app");
            assertEqual("tile|team_members.png", loaded.root.skinFillOverrides.get("teams:teams_member_list"), "teams_member_list in teams app");
            assertEqual("stretch|team_canvas.png", loaded.root.skinFillOverrides.get("teams:quests_canvas"), "quests_canvas in teams app");
            assertEqual("stretch|home_bg.png", loaded.root.skinFillOverrides.get("home:home_inner"), "home_inner in home app");

            loaded.root.skinFillOverrides.remove("quests:quests_canvas");
            loaded.root.skinFillOverrides.remove("teams:teams_member_list");
            TabletUiStatePersistence.writeSkinState(loaded);

            TabletUiState reloaded = new TabletUiState();
            TabletUiStatePersistence.readSkinState(reloaded);
            assertEqual(4, reloaded.root.skinFillOverrides.size(), "two removed, four remain");
            assertEqual(null, reloaded.root.skinFillOverrides.get("quests:quests_canvas"), "quests_canvas was removed");
            assertEqual(null, reloaded.root.skinFillOverrides.get("teams:teams_member_list"), "teams_member_list was removed");
            assertEqual("stretch|details.png", reloaded.root.skinFillOverrides.get("quests:quest_details_modal"), "quests details survives");
            assertEqual("stretch|team_canvas.png", reloaded.root.skinFillOverrides.get("teams:quests_canvas"), "teams canvas survives");
        } catch (Exception e) {
            throw new GameTestAssertException("questsAndTeamsOverridesAreIndependentAfterRoundTrip failed: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void eachContainerSetAndClearIndividuallySurvivesRoundTrip(GameTestHelper helper) {
        try {
            for (String target : ALL_CONTAINER_TARGETS) {
                TabletUiState original = new TabletUiState();
                original.root.skinFillOverrides.put(target, SAMPLE_TEX);
                TabletUiStatePersistence.writeSkinState(original);

                TabletUiState loaded = new TabletUiState();
                TabletUiStatePersistence.readSkinState(loaded);
                assertEqual(1, loaded.root.skinFillOverrides.size(), target + " set count");
                assertEqual(SAMPLE_TEX, loaded.root.skinFillOverrides.get(target), target + " value survived");

                loaded.root.skinFillOverrides.clear();
                TabletUiStatePersistence.writeSkinState(loaded);

                TabletUiState reloaded = new TabletUiState();
                TabletUiStatePersistence.readSkinState(reloaded);
                assertEqual(true, reloaded.root.skinFillOverrides.isEmpty(), target + " cleared");
            }
        } catch (Exception e) {
            throw new GameTestAssertException("eachContainerSetAndClearIndividuallySurvivesRoundTrip failed: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private static void assertEqual(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new GameTestAssertException(message + " (expected=" + expected + " actual=" + actual + ")");
        }
    }

    private static void cleanup() {
        try {
            Files.deleteIfExists(SKIN_STATE_FILE);
            Path parent = SKIN_STATE_FILE.getParent();
            if (parent != null && Files.isDirectory(parent)) {
                try (var stream = Files.list(parent)) {
                    if (stream.findAny().isEmpty()) {
                        Files.deleteIfExists(parent);
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }
}
