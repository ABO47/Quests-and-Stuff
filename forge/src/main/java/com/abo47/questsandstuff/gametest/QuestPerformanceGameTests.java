package com.abo47.questsandstuff.gametest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.ChapterDef;
import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.abo47.questsandstuff.quest.runtime.RuntimeEngine;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignal;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;
import com.abo47.questsandstuff.quest.sync.PerformanceTracker;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.sync.SyncService;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@GameTestHolder(QuestsAndStuffMod.MODID)
public final class QuestPerformanceGameTests {
    private QuestPerformanceGameTests() {
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void benchmarkLargeQuestPackAndMultiplayerSignals(GameTestHelper helper) {
        Bundle bundle = null;
        try {
            bundle = createBundle(helper, "performance_benchmark");

            int questCount = 320;
            int actorCount = 6;
            int roundsPerActor = 90;
            int targetVariants = 10;

            for (int i = 0; i < questCount; i++) {
                String questId = "bench/q_" + String.format("%03d", i);
                String target = "minecraft:item_" + (i % targetVariants);
                bundle.store.upsert(quest(questId, target));
            }
            bundle.engine.rebuildIndex();

            List<ServerPlayer> actors = new ArrayList<>();
            for (int i = 0; i < actorCount; i++) {
                actors.add(detachedPlayer(helper, "qas_bench_" + i));
            }

            long startNanos = System.nanoTime();
            for (int round = 0; round < roundsPerActor; round++) {
                for (ServerPlayer actor : actors) {
                    String key = "minecraft:item_" + (round % targetVariants);
                    bundle.engine.onSignal(QuestSignal.of(
                            QuestSignalType.ITEM_COLLECTED,
                            actor,
                            key,
                            1,
                            actor.blockPosition()
                    ));
                }
            }
            long elapsedNanos = System.nanoTime() - startNanos;

            var perf = bundle.perf.snapshotTag();
            long expectedSignals = (long) actorCount * roundsPerActor;
            long signalCount = perf.getLong("signals");
            long visitedBindings = perf.getLong("bindings");
            long questUpdates = perf.getLong("quest_updates");
            long avgMicros = signalCount == 0L ? 0L : (perf.getLong("signal_nanos") / signalCount) / 1000L;

            if (signalCount != expectedSignals) {
                throw new GameTestAssertException("Benchmark signal count mismatch. Expected " + expectedSignals + " but got " + signalCount);
            }
            if (visitedBindings < expectedSignals * questCount) {
                throw new GameTestAssertException("Benchmark visited bindings should reflect indexed signal fan-out");
            }
            if (questUpdates <= 0L) {
                throw new GameTestAssertException("Benchmark should produce quest progress updates under multiplayer load");
            }
            if (avgMicros > 50_000L) {
                throw new GameTestAssertException("Average signal processing exceeded benchmark budget: " + avgMicros + "us");
            }
            if (elapsedNanos > 45_000_000_000L) {
                throw new GameTestAssertException("Benchmark wall time exceeded budget: " + (elapsedNanos / 1_000_000L) + "ms");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create performance benchmark bundle: " + e.getMessage());
        } finally {
            if (bundle != null) {
                bundle.close();
            }
        }
        helper.succeed();
    }

    private static Bundle createBundle(GameTestHelper helper, String rootName) throws IOException {
        Path root = Files.createTempDirectory("qas_" + rootName + "_");
        QuestDefinitionStore store = new QuestDefinitionStore(root);
        QuestProgressSavedData progressData = QuestProgressSavedData.get(helper.getLevel().getServer());
        PerformanceTracker perf = new PerformanceTracker();
        SyncService sync = new SyncService(store, progressData, perf);
        RuntimeEngine engine = new RuntimeEngine(store, progressData, sync, perf);
        sync.setVisibilityFilter(engine::isVisibleFor);
        return new Bundle(store, engine, perf);
    }

    private static QuestDefinition quest(String id, String targetItem) {
        Map<String, QuestTaskDefinition> tasks = Map.of(
                "collect", QuestGameTestDefinitions.task("collect", "item", 12, targetItem, Map.of("collection_mode", "automatic"))
        );
        Map<String, QuestRewardDefinition> rewards = Map.of(
                "reward", QuestGameTestDefinitions.reward("reward", "xp", 1, "", false, Map.of())
        );
        return new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                id,
                new QuestDisplay(
                        id,
                        "performance",
                        List.of(),
                        Map.of("Main", ChapterDef.DEFAULT),
                        "minecraft:book",
                        "minecraft:barrier"
                ),
                new QuestSettings(true, QuestVisibilityMode.LOCKED, false, false, false, true),
                Set.of(),
                tasks,
                rewards
        );
    }

    private static ServerPlayer detachedPlayer(GameTestHelper helper, String name) {
        return new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), name)
        );
    }

    private record Bundle(
            QuestDefinitionStore store,
            RuntimeEngine engine,
            PerformanceTracker perf
    ) {
        private void close() {
            store.shutdown();
        }
    }
}
