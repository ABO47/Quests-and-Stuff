package com.abo47.questsandstuff.gametest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.network.QuestNetwork;
import com.abo47.questsandstuff.network.sync.S2CFullSyncPacket;
import com.abo47.questsandstuff.quest.model.ChapterDefinition;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.runtime.QuestRuntimeEngine;
import com.abo47.questsandstuff.quest.sync.QuestPerformanceTracker;
import com.abo47.questsandstuff.quest.sync.QuestSyncService;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@GameTestHolder(QuestsAndStuffMod.MODID)
public final class QuestPrerequisiteRealtimeGameTests {
    private QuestPrerequisiteRealtimeGameTests() {
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void prerequisiteEditRelocksQuestOnServerAndClient(GameTestHelper helper) {
        QuestDefinitionStore store = null;
        try {
            ClientQuestCache.resetStateForTests();
            Path root = Files.createTempDirectory("qas_prerequisite_relock_");
            store = new QuestDefinitionStore(root);
            String rootQuest = "editor/root";
            String childQuest = "editor/child";
            store.upsert(incompleteQuest(rootQuest, "Main", 32, 32, Set.of()));
            store.upsert(incompleteQuest(childQuest, "Main", 64, 32, Set.of()));

            QuestProgressSavedData progressData = QuestProgressSavedData.get(helper.getLevel().getServer());
            QuestPerformanceTracker perf = new QuestPerformanceTracker();
            QuestSyncService sync = new QuestSyncService(store, progressData, perf);
            QuestRuntimeEngine engine = new QuestRuntimeEngine(store, progressData, sync, perf);
            sync.setVisibilityFilter(engine::isVisibleFor);
            sync.setEditorVisibilityPredicate(ignored -> true);

            ServerPlayer player = detachedPlayer(helper);
            List<Object> packets = Collections.synchronizedList(new ArrayList<>());
            QuestNetwork.setTestPacketSink((target, packet) -> {
                if (target != null && target.getUUID().equals(player.getUUID())) {
                    packets.add(packet);
                }
            });

            engine.preparePlayerForFullSync(player);
            sync.syncFull(player);
            applyFullSyncPackets(packets);
            assertClientQuestUnlocked(childQuest, true, "Child quest should start unlocked before prerequisites are added");

            ClientQuestCache.setQuestPrerequisiteLocal(childQuest, rootQuest, true);
            assertClientQuestUnlocked(childQuest, false, "Local prerequisite edit should immediately relock the child quest");

            store.upsert(incompleteQuest(childQuest, "Main", 64, 32, Set.of(rootQuest)));
            engine.rebuildIndex();
            packets.clear();
            engine.preparePlayerForFullSync(player);
            sync.syncFull(player);
            applyFullSyncPackets(packets);
            assertClientQuestUnlocked(childQuest, false, "Server sync should relock the child quest after prerequisite edit");

            store.upsert(incompleteQuest(childQuest, "Main", 64, 32, Set.of()));
            engine.rebuildIndex();
            packets.clear();
            engine.preparePlayerForFullSync(player);
            sync.syncFull(player);
            applyFullSyncPackets(packets);
            assertClientQuestUnlocked(childQuest, true, "Removing the prerequisite should unlock the child quest again");
        } catch (IOException e) {
            throw new GameTestAssertException("Prerequisite relock test setup failed: " + e.getMessage());
        } finally {
            QuestNetwork.clearTestPacketSink();
            if (store != null) {
                store.shutdown();
            }
        }
        helper.succeed();
    }

    private static QuestDefinition incompleteQuest(String id, String chapter, int x, int y, Set<String> prerequisites) {
        QuestTaskDefinition task = QuestGameTestDefinitions.task("check", "check", 1, id, Map.of());
        return new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                id,
                new QuestDisplay(
                        id,
                        "prerequisite relock",
                        List.of("sync"),
                        Map.of(chapter, new ChapterDefinition(true, x, y, 1.0f)),
                        "minecraft:book",
                        "minecraft:barrier"
                ),
                new QuestSettings(false, QuestVisibilityMode.LOCKED, false, false, false, true),
                prerequisites,
                Map.of(),
                Map.of(),
                Set.of(),
                Map.of(task.id(), task),
                Map.of()
        );
    }

    private static ServerPlayer detachedPlayer(GameTestHelper helper) {
        return new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "qas_test_player")
        );
    }

    private static void applyFullSyncPackets(List<Object> packets) {
        for (S2CFullSyncPacket packet : packetsOf(packets, S2CFullSyncPacket.class)) {
            ClientQuestCache.acceptFullChunk(packet.sequence(), packet.chunkIndex(), packet.chunkCount(), packet.payload());
        }
    }

    private static void assertClientQuestUnlocked(String questId, boolean expected, String message) {
        CompoundTag quest = ClientQuestCache.quests().get(questId);
        if (quest == null) {
            throw new GameTestAssertException(message + ": quest missing from client cache");
        }
        if (quest.getBoolean("unlocked") != expected) {
            throw new GameTestAssertException(message + ": expected unlocked=" + expected + ", got " + quest.getBoolean("unlocked"));
        }
    }

    private static <T> List<T> packetsOf(List<?> packets, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Object packet : packets) {
            if (type.isInstance(packet)) {
                result.add(type.cast(packet));
            }
        }
        return result;
    }
}
