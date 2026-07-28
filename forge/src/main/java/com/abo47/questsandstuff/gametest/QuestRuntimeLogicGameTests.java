package com.abo47.questsandstuff.gametest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.ChapterDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.runtime.RuntimeEngine;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;

import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(QuestsAndStuffMod.MODID)
public final class QuestRuntimeLogicGameTests {
    private QuestRuntimeLogicGameTests() {
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void hiddenModeVisibilityBehavior(GameTestHelper helper) {
        QuestDefinitionStore store = null;
        try {
            store = new QuestDefinitionStore(tempRoot("hidden_modes"));
            QuestDefinition locked = quest("test/locked", QuestVisibilityMode.LOCKED, Set.of());
            QuestDefinition inProgress = quest("test/in_progress", QuestVisibilityMode.IN_PROGRESS, Set.of());
            QuestDefinition completed = quest("test/completed", QuestVisibilityMode.COMPLETED, Set.of());

            store.upsert(locked);
            store.upsert(inProgress);
            store.upsert(completed);
            RuntimeEngine engine = new RuntimeEngine(store, null, null, null);
            PlayerQuestState state = new PlayerQuestState();

            if (engine.isVisibleFor(state, locked)) {
                throw new GameTestAssertException("LOCKED mode should hide quest before unlock");
            }
            if (engine.isVisibleFor(state, inProgress)) {
                throw new GameTestAssertException("IN_PROGRESS mode should hide quest before unlock");
            }
            if (engine.isVisibleFor(state, completed)) {
                throw new GameTestAssertException("COMPLETED mode should hide quest before completion");
            }

            state.quest(locked.id()).setUnlocked(true);
            state.quest(inProgress.id()).setUnlocked(true);
            if (!engine.isVisibleFor(state, locked) || !engine.isVisibleFor(state, inProgress)) {
                throw new GameTestAssertException("LOCKED/IN_PROGRESS modes should show after unlock");
            }
            if (engine.isVisibleFor(state, completed)) {
                throw new GameTestAssertException("COMPLETED mode should stay hidden until complete");
            }

            state.quest(completed.id()).setCompleted(true, 5L);
            if (!engine.isVisibleFor(state, completed)) {
                throw new GameTestAssertException("COMPLETED mode should show after completion");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Failed creating temp quest test root: " + e.getMessage());
        } finally {
            if (store != null) {
                store.shutdown();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void prerequisitesVisibleModeUsesPrerequisiteProgress(GameTestHelper helper) {
        QuestDefinitionStore store = null;
        try {
            store = new QuestDefinitionStore(tempRoot("prerequisite_visible"));
            QuestDefinition prerequisite = quest("test/prerequisite", QuestVisibilityMode.LOCKED, Set.of());
            QuestDefinition gated = quest("test/gated", QuestVisibilityMode.PREREQUISITES_VISIBLE, Set.of(prerequisite.id()));
            QuestDefinition chained = quest("test/chained", QuestVisibilityMode.PREREQUISITES_VISIBLE, Set.of(gated.id()));

            store.upsert(prerequisite);
            store.upsert(gated);
            store.upsert(chained);
            RuntimeEngine engine = new RuntimeEngine(store, null, null, null);
            PlayerQuestState state = new PlayerQuestState();

            if (engine.isVisibleFor(state, gated)) {
                throw new GameTestAssertException("PREREQUISITES_VISIBLE should hide when no prerequisite progress exists");
            }
            if (engine.isVisibleFor(state, chained)) {
                throw new GameTestAssertException("Chained PREREQUISITES_VISIBLE should hide when its visible root is hidden");
            }

            state.quest(prerequisite.id()).setUnlocked(true);
            if (!engine.isVisibleFor(state, gated)) {
                throw new GameTestAssertException("PREREQUISITES_VISIBLE should show when prerequisite is unlocked");
            }
            if (!engine.isVisibleFor(state, chained)) {
                throw new GameTestAssertException("Chained PREREQUISITES_VISIBLE should show when prerequisite is visible");
            }

            state.quest(prerequisite.id()).setUnlocked(false);
            state.quest(prerequisite.id()).setCompleted(true, 12L);
            if (!engine.isVisibleFor(state, gated)) {
                throw new GameTestAssertException("PREREQUISITES_VISIBLE should show when prerequisite is completed");
            }
            if (!engine.isVisibleFor(state, chained)) {
                throw new GameTestAssertException("Chained PREREQUISITES_VISIBLE should show when prerequisite is visible through completion");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Failed creating temp quest test root: " + e.getMessage());
        } finally {
            if (store != null) {
                store.shutdown();
            }
        }
        helper.succeed();
    }

    private static QuestDefinition quest(String id, QuestVisibilityMode mode, Set<String> prerequisites) {
        return new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                id,
                new QuestDisplay(
                        id,
                        "",
                        List.of(),
                        Map.of("Main", ChapterDef.DEFAULT),
                        "minecraft:book",
                        "minecraft:barrier"
                ),
                new QuestSettings(false, mode, false, false, false, true),
                prerequisites,
                Map.of(),
                Map.of()
        );
    }

    private static Path tempRoot(String testName) throws IOException {
        return Files.createTempDirectory("qas_" + testName + "_");
    }
}
