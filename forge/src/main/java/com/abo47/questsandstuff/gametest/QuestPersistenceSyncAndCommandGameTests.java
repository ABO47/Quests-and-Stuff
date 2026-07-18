package com.abo47.questsandstuff.gametest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionModel;
import com.abo47.questsandstuff.command.QuestCommands;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.sync.S2CDeltaSyncPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CDescriptionSyncPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CEditorMutationPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CFullSyncPacket;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.quest.model.ChapterDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbtCodec;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.runtime.RuntimeEngine;
import com.abo47.questsandstuff.quest.sync.PerformanceTracker;
import com.abo47.questsandstuff.quest.sync.SyncService;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(QuestsAndStuffMod.MODID)
public final class QuestPersistenceSyncAndCommandGameTests {
    private QuestPersistenceSyncAndCommandGameTests() {
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void persistenceHandlesMalformedAndAtomicWrite(GameTestHelper helper) {
        QuestDefinitionStore store = null;
        try {
            Path root = Files.createTempDirectory("qas_persist_");
            Path questsDir = root.resolve("quests");
            Files.createDirectories(questsDir);

            store = new QuestDefinitionStore(root);
            store.upsert(quest("tests/valid"));
            store.saveAll();

            Path malformed = questsDir.resolve("broken.json");
            Files.writeString(malformed, "{not valid json", StandardCharsets.UTF_8);
            Path stale = questsDir.resolve("unused.json");
            Files.writeString(stale, "{}", StandardCharsets.UTF_8);

            store.load();

            if (Files.exists(malformed) || Files.exists(stale)) {
                throw new GameTestAssertException("Malformed/stale quest files should be cleaned after load");
            }
            if (store.quests().isEmpty()) {
                throw new GameTestAssertException("Store should keep at least one valid in-memory quest after load");
            }

            QuestDefinition custom = quest("tests/atomic_write");
            store.upsert(custom);
            store.saveAll();

            Path customFile = questsDir.resolve("main").resolve("tests_atomic_write.json");
            if (!Files.exists(customFile)) {
                throw new GameTestAssertException("Expected persisted quest file for atomic write test");
            }
            try (var files = Files.walk(root)) {
                boolean hasTmp = files.anyMatch(path -> path.getFileName().toString().endsWith(".tmp"));
                if (hasTmp) {
                    throw new GameTestAssertException("No lingering .tmp files expected after saveAll");
                }
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Persistence test setup failed: " + e.getMessage());
        } finally {
            if (store != null) {
                store.shutdown();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void chapterMetadataWritesCurrentSchema(GameTestHelper helper) {
        QuestDefinitionStore store = null;
        try {
            Path root = Files.createTempDirectory("qas_chapter_schema_");
            Path chaptersDir = root.resolve("chapters");
            Files.createDirectories(chaptersDir);
            Path chapterFile = chaptersDir.resolve("main.json");
            Files.writeString(chapterFile, """
                    {
                      "name": "Main",
                      "order": 0,
                      "icon": "minecraft:book",
                      "background": "default",
                      "canvas_background": "default",
                      "text_align": "center",
                      "text_color": -1,
                      "text_style": "normal",
                      "text_size": 17,
                      "canvas_images": [
                        {
                          "id": "img",
                          "asset": "entity:minecraft:cat",
                          "x": 12,
                          "y": 16,
                          "w": 64,
                          "h": 64,
                          "rotation": 0
                        }
                      ],
                      "canvas_texts": [],
                      "canvas_layer_order": [
                        "image:img"
                      ]
                    }
                    """, StandardCharsets.UTF_8);

            store = new QuestDefinitionStore(root);
            store.load();

            if (!store.chapterOrder().contains("Main")) {
                throw new GameTestAssertException("Chapter metadata should load chapter order");
            }
            if (store.chapterTextSize("Main") != 17) {
                throw new GameTestAssertException("Chapter metadata should load text_size");
            }
            if (store.canvasImages("Main").size() != 1 || store.canvasLayerOrder("Main").size() != 1) {
                throw new GameTestAssertException("Chapter canvas metadata should load");
            }

            JsonObject saved = JsonParser.parseString(Files.readString(chapterFile, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!saved.has("schema_version") || saved.get("schema_version").getAsInt() != 1) {
                throw new GameTestAssertException("Chapter metadata should write schema_version 1");
            }
            if (!saved.has("text_size") || saved.get("text_size").getAsInt() != 17) {
                throw new GameTestAssertException("Chapter metadata should persist text_size");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Chapter metadata migration test setup failed: " + e.getMessage());
        } finally {
            if (store != null) {
                store.shutdown();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void questlineManifestIsSavedWithShareMetadata(GameTestHelper helper) {
        QuestDefinitionStore store = null;
        try {
            Path root = Files.createTempDirectory("qas_manifest_");
            Path manifestFile = root.resolve("manifest.json");
            Files.writeString(manifestFile, """
                    {
                      "schema_version": 1,
                      "pack": {
                        "title": "My Pack",
                        "author": "Tester",
                        "description": "Shared quests",
                        "created_date": "2026-05-18"
                      },
                      "optional_mods": [
                        {"id": "example", "version": "1.0.0"}
                      ]
                    }
                    """, StandardCharsets.UTF_8);

            store = new QuestDefinitionStore(root);
            store.upsert(quest("tests/manifest"));
            store.saveAll();

            JsonObject manifest = JsonParser.parseString(Files.readString(manifestFile, StandardCharsets.UTF_8)).getAsJsonObject();
            if (manifest.get("schema_version").getAsInt() != 1) {
                throw new GameTestAssertException("Manifest should include its schema version");
            }
            if (manifest.get("quest_schema_version").getAsInt() != QuestDefinition.CURRENT_SCHEMA) {
                throw new GameTestAssertException("Manifest should include current quest schema version");
            }
            JsonObject pack = manifest.getAsJsonObject("pack");
            if (!"My Pack".equals(pack.get("title").getAsString()) || !"2026-05-18".equals(pack.get("created_date").getAsString())) {
                throw new GameTestAssertException("Manifest should preserve editable pack metadata");
            }
            if (!manifest.has("mod") || !manifest.has("targets") || !manifest.has("required_mods") || !manifest.has("optional_mods")) {
                throw new GameTestAssertException("Manifest should include mod, targets, required mods, and optional mods");
            }
            if (manifest.getAsJsonArray("required_mods").size() < 4 || manifest.getAsJsonArray("optional_mods").size() != 1) {
                throw new GameTestAssertException("Manifest mod tasks should be written");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Manifest test setup failed: " + e.getMessage());
        } finally {
            if (store != null) {
                store.shutdown();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void questDetailsDescriptionWritesJsonImmediately(GameTestHelper helper) {
        QuestDefinitionStore store = null;
        try {
            Path root = Files.createTempDirectory("qas_desc_save_");
            String questId = "details/persist";
            store = new QuestDefinitionStore(root);
            store.upsert(quest(questId));

            QuestProgressSavedData progressData = QuestProgressSavedData.get(helper.getLevel().getServer());
            PerformanceTracker perf = new PerformanceTracker();
            SyncService sync = new SyncService(store, progressData, perf);
            RuntimeEngine engine = new RuntimeEngine(store, progressData, sync, perf);
            EditorSessionService editor = new EditorSessionService(store, engine, sync);
            ServerPlayer player = detachedPlayer(helper);

            String meta = "@qas_desc_meta:{background:\"default\",bg_opacity:60,canvas_locked:0b,center_x:1b,center_y:1b,grid:1b,grid_opacity:50,object_snap:0b,snap:1b}";
            String text = "@qas_desc_text:{align:\"left\",color:-1443841,font_size:9,h:32,id:\"txt_0001\",rotation:0,spans:[],style:\"normal\",text:\"Saved detail\",w:112,x:96,y:0}";
            editor.updateQuestDescription(player, questId, List.of(meta, text));

            Path saved = root.resolve("quests").resolve("main").resolve("details_persist.json");
            if (!Files.exists(saved)) {
                throw new GameTestAssertException("Quest details edit should write its quest JSON immediately");
            }
            String raw = Files.readString(saved, StandardCharsets.UTF_8);
            if (!raw.contains("@qas_desc_meta") || !raw.contains("Saved detail")) {
                throw new GameTestAssertException("Quest details description should be stored in the quest JSON");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Quest details save test setup failed: " + e.getMessage());
        } finally {
            if (store != null) {
                store.shutdown();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void questDetailsDescriptionKeepsMultilineTextFormatting(GameTestHelper helper) {
        String poem = "First line\nSecond line\nThird line";
        CanvasTextLayer layer = new CanvasTextLayer("poem", poem, 12, 16, 112, 32, 0, "center", "bold", -1);
        CompoundTag quest = new CompoundTag();
        ListTag description = new ListTag();
        description.add(StringTag.valueOf("@qas_desc_text:" + CanvasLayerNbtCodec.textToTag(layer)));
        quest.put("description", description);

        QuestDetailsDescriptionModel decoded = QuestDetailsDescriptionModel.decode(quest);
        List<String> encoded = QuestDetailsDescriptionModel.encode(decoded);
        CompoundTag roundTripQuest = new CompoundTag();
        ListTag roundTripDescription = new ListTag();
        for (String line : encoded) {
            roundTripDescription.add(StringTag.valueOf(line));
        }
        roundTripQuest.put("description", roundTripDescription);

        CanvasTextLayer roundTrip = QuestDetailsDescriptionModel.decode(roundTripQuest).text("poem");
        if (roundTrip == null) {
            throw new GameTestAssertException("Quest details text should survive description encode/decode");
        }
        if (!poem.equals(roundTrip.text())) {
            throw new GameTestAssertException("Quest details multiline text should keep newline characters");
        }
        if (!"center".equals(roundTrip.align()) || !"bold".equals(roundTrip.style())) {
            throw new GameTestAssertException("Quest details text formatting should survive description encode/decode");
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void editorMutationIncludesQuestDetailsDescription(GameTestHelper helper) {
        QuestDefinitionStore store = null;
        try {
            Path root = Files.createTempDirectory("qas_desc_sync_");
            store = new QuestDefinitionStore(root);
            QuestDefinition source = quest("details/sync");
            String detailLine = "@qas_desc_text:{align:\"left\",color:-1443841,font_size:9,h:32,id:\"txt_0001\",rotation:0,spans:[],style:\"normal\",text:\"Synced detail\",w:112,x:96,y:0}";
            QuestDefinition withDescription = new QuestDefinition(
                    source.schema(),
                    source.id(),
                    new QuestDisplay(
                            source.display().title(),
                            source.display().subtitle(),
                            List.of(detailLine),
                            source.display().chapters(),
                            source.display().icon(),
                            source.display().iconBackground(),
                            source.display().completionSound(),
                            source.display().completionSoundVolume(),
                            source.display().visualHidden()
                    ),
                    source.settings(),
                    source.prerequisites(),
                    source.connectionColors(),
                    source.connectionModes(),
                    source.hiddenConnections(),
                    source.tasks(),
                    source.rewards()
            );
            store.upsert(withDescription);

            QuestProgressSavedData progressData = QuestProgressSavedData.get(helper.getLevel().getServer());
            PerformanceTracker perf = new PerformanceTracker();
            SyncService sync = new SyncService(store, progressData, perf);
            sync.setEditorVisibilityPredicate(ignored -> true);
            ServerPlayer player = detachedPlayer(helper);
            List<Object> packets = Collections.synchronizedList(new ArrayList<>());
            ModNetwork.setTestPacketSink((target, packet) -> {
                if (target != null && target.getUUID().equals(player.getUUID())) {
                    packets.add(packet);
                }
            });

            sync.broadcastEditorMutation(List.of(player), "update", withDescription);

            List<S2CEditorMutationPacket> mutations = packetsOf(packets, S2CEditorMutationPacket.class);
            if (mutations.size() != 1) {
                throw new GameTestAssertException("Expected one editor mutation packet with quest details");
            }
            ListTag lines = mutations.get(0).questTag().getList("description", net.minecraft.nbt.Tag.TAG_STRING);
            if (lines.isEmpty() || !detailLine.equals(lines.getString(0))) {
                throw new GameTestAssertException("Editor mutation should carry quest details description lines");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Quest details mutation test setup failed: " + e.getMessage());
        } finally {
            ModNetwork.clearTestPacketSink();
            if (store != null) {
                store.shutdown();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void syncSupportsChunkingDeltaAndReconnect(GameTestHelper helper) {
        QuestDefinitionStore store = null;
        try {
            ClientQuestStateFacade.resetStateForTests();
            Path root = Files.createTempDirectory("qas_sync_");
            store = new QuestDefinitionStore(root);

            int totalQuests = 270;
            for (int i = 0; i < totalQuests; i++) {
                String id = "bulk/q_" + String.format("%03d", i);
                store.upsert(quest(id));
            }

            QuestProgressSavedData progressData = QuestProgressSavedData.get(helper.getLevel().getServer());
            PerformanceTracker perf = new PerformanceTracker();
            SyncService sync = new SyncService(store, progressData, perf);
            sync.setVisibilityFilter((state, definition) -> true);

            ServerPlayer player = detachedPlayer(helper);
            List<Object> packets = Collections.synchronizedList(new ArrayList<>());
            ModNetwork.setTestPacketSink((target, packet) -> {
                if (target != null && target.getUUID().equals(player.getUUID())) {
                    packets.add(packet);
                }
            });

            sync.syncFull(player);
            List<S2CFullSyncPacket> fullPackets = packetsOf(packets, S2CFullSyncPacket.class);
            List<S2CDescriptionSyncPacket> fullDescriptions = packetsOf(packets, S2CDescriptionSyncPacket.class);
            if (fullPackets.size() != 3) {
                throw new GameTestAssertException("Expected 3 full sync chunks, got " + fullPackets.size());
            }
            if (fullDescriptions.size() != 5) {
                throw new GameTestAssertException("Expected 5 description chunks, got " + fullDescriptions.size());
            }

            ClientQuestStateFacade.applyFullSync(emptyFullPayload());
            List<S2CFullSyncPacket> reversedFull = new ArrayList<>(fullPackets);
            Collections.reverse(reversedFull);
            for (S2CFullSyncPacket packet : reversedFull) {
                ClientQuestStateFacade.acceptFullChunk(packet.sequence(), packet.chunkIndex(), packet.chunkCount(), packet.payload());
            }
            if (ClientQuestStateFacade.totalCount() != totalQuests) {
                throw new GameTestAssertException("Client full sync reconstruction mismatch");
            }

            Set<String> changed = new LinkedHashSet<>(store.quests().keySet());
            changed.add("ghost/missing");
            int beforeDeltaPacketCount = packets.size();
            sync.syncDelta(player, changed);
            List<Object> deltaBatch = packets.subList(beforeDeltaPacketCount, packets.size());
            List<S2CDeltaSyncPacket> deltaPackets = packetsOf(deltaBatch, S2CDeltaSyncPacket.class);
            List<S2CDescriptionSyncPacket> deltaDescriptions = packetsOf(deltaBatch, S2CDescriptionSyncPacket.class);
            if (deltaPackets.size() != 3) {
                throw new GameTestAssertException("Expected 3 delta chunks, got " + deltaPackets.size());
            }
            if (deltaDescriptions.size() != 5) {
                throw new GameTestAssertException("Expected 5 delta description chunks, got " + deltaDescriptions.size());
            }

            List<S2CDeltaSyncPacket> reversedDelta = new ArrayList<>(deltaPackets);
            Collections.reverse(reversedDelta);
            for (S2CDeltaSyncPacket packet : reversedDelta) {
                ClientQuestStateFacade.acceptDeltaChunk(packet.sequence(), packet.chunkIndex(), packet.chunkCount(), packet.payload());
            }
            if (ClientQuestStateFacade.totalCount() != totalQuests) {
                throw new GameTestAssertException("Delta merge should preserve quest count for non-existing removals");
            }

            String removedQuest = "bulk/q_005";
            store.remove(removedQuest);
            int beforeReconnect = packets.size();
            sync.syncFull(player);
            List<S2CFullSyncPacket> reconnectFull = packetsOf(packets.subList(beforeReconnect, packets.size()), S2CFullSyncPacket.class);
            for (S2CFullSyncPacket packet : reconnectFull) {
                ClientQuestStateFacade.acceptFullChunk(packet.sequence(), packet.chunkIndex(), packet.chunkCount(), packet.payload());
            }
            if (ClientQuestStateFacade.totalCount() != totalQuests - 1) {
                throw new GameTestAssertException("Reconnect full sync should reflect server-side quest removal");
            }

            for (S2CDeltaSyncPacket packet : deltaPackets) {
                ClientQuestStateFacade.acceptDeltaChunk(packet.sequence(), packet.chunkIndex(), packet.chunkCount(), packet.payload());
            }
            if (ClientQuestStateFacade.totalCount() != totalQuests - 1) {
                throw new GameTestAssertException("Client should ignore stale delta packets after newer full sync");
            }

            CompoundTag perfTag = perf.snapshotTag();
            if (perfTag.getLong("full_chunks") < 3 || perfTag.getLong("delta_chunks") < 3) {
                throw new GameTestAssertException("Performance tracker should record chunked sync counts");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Sync test setup failed: " + e.getMessage());
        } finally {
            ModNetwork.clearTestPacketSink();
            if (store != null) {
                store.shutdown();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void preparedFullSyncIncludesFreshNoPrerequisiteQuests(GameTestHelper helper) {
        QuestDefinitionStore store = null;
        try {
            ClientQuestStateFacade.resetStateForTests();
            Path root = Files.createTempDirectory("qas_sync_unlock_");
            store = new QuestDefinitionStore(root);
            String questId = "editor/fresh_visible";
            store.upsert(quest(questId));

            QuestProgressSavedData progressData = QuestProgressSavedData.get(helper.getLevel().getServer());
            PerformanceTracker perf = new PerformanceTracker();
            SyncService sync = new SyncService(store, progressData, perf);
            RuntimeEngine engine = new RuntimeEngine(store, progressData, sync, perf);
            sync.setVisibilityFilter(engine::isVisibleFor);

            ServerPlayer player = detachedPlayer(helper);
            List<Object> packets = Collections.synchronizedList(new ArrayList<>());
            ModNetwork.setTestPacketSink((target, packet) -> {
                if (target != null && target.getUUID().equals(player.getUUID())) {
                    packets.add(packet);
                }
            });

            sync.syncFull(player);
            for (S2CFullSyncPacket packet : packetsOf(packets, S2CFullSyncPacket.class)) {
                ClientQuestStateFacade.acceptFullChunk(packet.sequence(), packet.chunkIndex(), packet.chunkCount(), packet.payload());
            }
            CompoundTag lockedPreview = ClientQuestStateFacade.quests().get(questId);
            if (lockedPreview == null) {
                throw new GameTestAssertException("Fresh locked quest should sync as a locked preview before unlock preparation");
            }
            if (lockedPreview.getBoolean("unlocked")) {
                throw new GameTestAssertException("Fresh locked quest preview should stay locked before unlock preparation");
            }

            packets.clear();
            ClientQuestStateFacade.resetStateForTests();
            engine.preparePlayerForFullSync(player);
            sync.syncFull(player);
            for (S2CFullSyncPacket packet : packetsOf(packets, S2CFullSyncPacket.class)) {
                ClientQuestStateFacade.acceptFullChunk(packet.sequence(), packet.chunkIndex(), packet.chunkCount(), packet.payload());
            }

            CompoundTag quest = ClientQuestStateFacade.quests().get(questId);
            if (quest == null) {
                throw new GameTestAssertException("Prepared full sync should include newly unlockable quests");
            }
            if (!quest.getBoolean("unlocked")) {
                throw new GameTestAssertException("Prepared full sync should send fresh quest as unlocked");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Prepared full sync test setup failed: " + e.getMessage());
        } finally {
            ModNetwork.clearTestPacketSink();
            if (store != null) {
                store.shutdown();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void editorFullSyncIncludesLockedPrerequisiteChain(GameTestHelper helper) {
        QuestDefinitionStore store = null;
        try {
            ClientQuestStateFacade.resetStateForTests();
            Path root = Files.createTempDirectory("qas_editor_sync_");
            store = new QuestDefinitionStore(root);
            store.upsert(quest("editor/root", "Main", 32, 32, Set.of(), Map.of(), Map.of(), Set.of()));
            store.upsert(quest("editor/child", "Main", 64, 32, Set.of("editor/root"), Map.of(), Map.of(), Set.of()));

            QuestProgressSavedData progressData = QuestProgressSavedData.get(helper.getLevel().getServer());
            PerformanceTracker perf = new PerformanceTracker();
            SyncService sync = new SyncService(store, progressData, perf);
            RuntimeEngine engine = new RuntimeEngine(store, progressData, sync, perf);
            sync.setVisibilityFilter(engine::isVisibleFor);
            sync.setEditorVisibilityPredicate(ignored -> true);

            ServerPlayer player = detachedPlayer(helper);
            List<Object> packets = Collections.synchronizedList(new ArrayList<>());
            ModNetwork.setTestPacketSink((target, packet) -> {
                if (target != null && target.getUUID().equals(player.getUUID())) {
                    packets.add(packet);
                }
            });

            sync.syncFull(player);
            for (S2CFullSyncPacket packet : packetsOf(packets, S2CFullSyncPacket.class)) {
                ClientQuestStateFacade.acceptFullChunk(packet.sequence(), packet.chunkIndex(), packet.chunkCount(), packet.payload());
            }
            if (!ClientQuestStateFacade.quests().containsKey("editor/root") || !ClientQuestStateFacade.quests().containsKey("editor/child")) {
                throw new GameTestAssertException("Editor full sync should include locked prerequisite chains for canvas editing");
            }
            CompoundTag child = ClientQuestStateFacade.quests().get("editor/child");
            if (child == null || !child.getList(QuestDefinition.PREREQUISITES_FIELD, net.minecraft.nbt.Tag.TAG_STRING).contains(net.minecraft.nbt.StringTag.valueOf("editor/root"))) {
                throw new GameTestAssertException("Editor full sync should preserve prerequisite edges for locked pasted-style quests");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Editor sync test setup failed: " + e.getMessage());
        } finally {
            ModNetwork.clearTestPacketSink();
            if (store != null) {
                store.shutdown();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void editorCreatedQuestStartsEmpty(GameTestHelper helper) {
        QuestDefinitionStore store = null;
        try {
            ClientQuestStateFacade.resetStateForTests();
            Path root = Files.createTempDirectory("qas_empty_editor_");
            store = new QuestDefinitionStore(root);
            QuestProgressSavedData progressData = QuestProgressSavedData.get(helper.getLevel().getServer());
            PerformanceTracker perf = new PerformanceTracker();
            SyncService sync = new SyncService(store, progressData, perf);
            RuntimeEngine engine = new RuntimeEngine(store, progressData, sync, perf);
            sync.setVisibilityFilter(engine::isVisibleFor);
            EditorSessionService editor = new EditorSessionService(store, engine, sync);
            ServerPlayer player = detachedPlayer(helper);
            String questId = "quest_0001_Main";
            progressData.state(player.getUUID()).quest(questId).setCompleted(true, 1L);

            List<Object> packets = Collections.synchronizedList(new ArrayList<>());
            ModNetwork.setTestPacketSink((target, packet) -> {
                if (target != null && target.getUUID().equals(player.getUUID())) {
                    packets.add(packet);
                }
            });

            editor.addQuest(player, "Main", questId, 32, 48, "");

            QuestDefinition created = store.quests().get(questId);
            if (created == null) {
                throw new GameTestAssertException("Expected editor-created quest");
            }
            if (!created.display().title().isBlank()) {
                throw new GameTestAssertException("Editor-created quest title should be blank when no title was entered");
            }
            if (!created.display().subtitle().isBlank()) {
                throw new GameTestAssertException("Editor-created quest subtitle should be blank");
            }
            if (!created.display().description().isEmpty()) {
                throw new GameTestAssertException("Editor-created quest description should start empty");
            }
            if (!created.tasks().isEmpty()) {
                throw new GameTestAssertException("Editor-created quest should not spawn default tasks");
            }
            if (!created.rewards().isEmpty()) {
                throw new GameTestAssertException("Editor-created quest should not spawn default rewards");
            }
            if (progressData.state(player.getUUID()).quest(created.id()).completed()) {
                throw new GameTestAssertException("Editor-created empty quest should not auto-complete");
            }

            engine.preparePlayerForFullSync(player);
            sync.syncFull(player);
            sync.broadcastEditorMutation(List.of(player), "add", created);
            for (S2CFullSyncPacket packet : packetsOf(packets, S2CFullSyncPacket.class)) {
                ClientQuestStateFacade.acceptFullChunk(packet.sequence(), packet.chunkIndex(), packet.chunkCount(), packet.payload());
            }
            for (S2CEditorMutationPacket packet : packetsOf(packets, S2CEditorMutationPacket.class)) {
                ClientQuestStateFacade.applyEditorMutation(packet.sequence(), packet.action(), packet.questId(), packet.questTag());
            }
            CompoundTag synced = ClientQuestStateFacade.quests().get(created.id());
            if (synced == null) {
                throw new GameTestAssertException("Editor-created quest should sync to the client");
            }
            if (!synced.getBoolean("unlocked")) {
                throw new GameTestAssertException("Editor-created empty quest should sync as unlocked/blue");
            }
            if (synced.getBoolean("completed") || synced.getBoolean("claimed")) {
                throw new GameTestAssertException("Editor-created empty quest should not sync as completed/claimed");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Editor empty quest test setup failed: " + e.getMessage());
        } finally {
            ModNetwork.clearTestPacketSink();
            if (store != null) {
                store.shutdown();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void clipboardPasteIntoChapterCreatedAfterCopyKeepsInternalEdges(GameTestHelper helper) {
        QuestDefinitionStore store = null;
        try {
            Path root = Files.createTempDirectory("qas_clipboard_");
            store = new QuestDefinitionStore(root);
            store.setChapterOrder(List.of("Original"));
            store.upsert(quest("source/external", "Original", 96, 32, Set.of(), Map.of(), Map.of(), Set.of()));
            store.upsert(quest("source/a", "Original", 32, 32, Set.of("source/external"), Map.of("source/external", 0xFF00FF), Map.of("source/external", "grid"), Set.of("source/external")));
            store.upsert(quest(
                    "source/b",
                    "Original",
                    64,
                    32,
                    Set.of("source/a", "source/external"),
                    Map.of("source/a", 0x00FF00, "source/external", 0xFF0000),
                    Map.of("source/a", "grid", "source/external", "grid"),
                    Set.of("source/a", "source/external")
            ));

            QuestProgressSavedData progressData = QuestProgressSavedData.get(helper.getLevel().getServer());
            PerformanceTracker perf = new PerformanceTracker();
            SyncService sync = new SyncService(store, progressData, perf);
            RuntimeEngine engine = new RuntimeEngine(store, progressData, sync, perf);
            sync.setVisibilityFilter(engine::isVisibleFor);
            EditorSessionService editor = new EditorSessionService(store, engine, sync);
            ServerPlayer player = detachedPlayer(helper);

            editor.copyQuestsToClipboard(player, "Original", new LinkedHashSet<>(List.of("source/a", "source/b")));
            store.remove("source/a");
            store.remove("source/b");
            editor.createChapter(player, "After Copy");
            editor.pasteClipboardInChapter(player, "After Copy", 200, 300);

            List<QuestDefinition> firstPaste = pastedByTitle(store, "After Copy");
            if (firstPaste.size() != 2) {
                throw new GameTestAssertException("Expected two pasted quests in new chapter, got " + firstPaste.size());
            }
            for (QuestDefinition pasted : firstPaste) {
                if (Set.of("source/a", "source/b").contains(pasted.id())) {
                    throw new GameTestAssertException("Paste must not reuse deleted source id " + pasted.id());
                }
            }
            assertClipboardGraph(firstPaste);

            editor.undo(player);
            if (!pastedByTitle(store, "After Copy").isEmpty()) {
                throw new GameTestAssertException("Undo after paste should remove pasted quests");
            }

            editor.redo(player);
            List<QuestDefinition> redoPaste = pastedByTitle(store, "After Copy");
            if (redoPaste.size() != 2) {
                throw new GameTestAssertException("Redo after paste should restore two pasted quests, got " + redoPaste.size());
            }
            assertClipboardGraph(redoPaste);

            editor.pasteClipboardInChapter(player, "After Copy", 240, 340);
            List<QuestDefinition> secondPaste = pastedByTitle(store, "After Copy");
            if (secondPaste.size() != 4) {
                throw new GameTestAssertException("Repeated paste should create unique quests; expected 4, got " + secondPaste.size());
            }
            if (!store.chapterOrder().contains("After Copy")) {
                throw new GameTestAssertException("Paste target chapter should exist after copy-created chapter paste");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Clipboard graph test setup failed: " + e.getMessage());
        } finally {
            if (store != null) {
                store.shutdown();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void editorMutationAddsMissingClientChapter(GameTestHelper helper) {
        ClientQuestStateFacade.resetStateForTests();
        ClientQuestStateFacade.applyFullSync(emptyFullPayload());

        CompoundTag groupView = new CompoundTag();
        groupView.putBoolean("visible", true);
        groupView.putInt("x", 200);
        groupView.putInt("y", 300);
        groupView.putFloat("scale", 1.0f);
        CompoundTag groups = new CompoundTag();
        groups.put("After Copy", groupView);

        CompoundTag questTag = new CompoundTag();
        questTag.putString("title", "source/a");
        questTag.put("chapters", groups);

        ClientQuestStateFacade.applyEditorMutation(1L, "add", "after_copy/source_a_copy", questTag);
        if (!ClientQuestStateFacade.chapterOrder().contains("After Copy")) {
            throw new GameTestAssertException("Editor add mutation should create missing client chapter metadata");
        }
        if (!ClientQuestStateFacade.quests().containsKey("after_copy/source_a_copy")) {
            throw new GameTestAssertException("Editor add mutation should add pasted quest to client cache");
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void commandPermissionsAndPinBoundaries(GameTestHelper helper) {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        QuestCommands.register(dispatcher);

        var server = helper.getLevel().getServer();
        if (server == null) {
            throw new GameTestAssertException("Server is unavailable for command test");
        }

        CommandSourceStack lowPerm = server.createCommandSourceStack().withPermission(0);
        expectCommandRejected(dispatcher, lowPerm, "questsandstuff reload");
        expectCommandRejected(dispatcher, lowPerm, "questsandstuff resetall");
        expectCommandRejected(dispatcher, lowPerm, "questsandstuff manual any_key");

        ServerPlayer player = detachedPlayer(helper);
        CommandSourceStack playerSource = player.createCommandSourceStack().withPermission(0);

        String questId = QuestServiceRegistry.engine(server).questIds().stream().findFirst().orElse("starter/logging_basics");
        String quotedQuestId = quoteArg(questId);
        try {
            int result = dispatcher.execute("questsandstuff pin " + quotedQuestId, playerSource);
            if (result <= 0) {
                throw new GameTestAssertException("Pin command should succeed for player source");
            }
        } catch (CommandSyntaxException e) {
            throw new GameTestAssertException("Pin command unexpectedly failed: " + e.getMessage());
        }

        QuestProgressSavedData progressData = QuestProgressSavedData.get(server);
        if (!progressData.state(player.getUUID()).pinnedQuests().contains(questId)) {
            throw new GameTestAssertException("Pin command should toggle pinned quest on");
        }

        try {
            dispatcher.execute("questsandstuff pin " + quotedQuestId, playerSource);
        } catch (CommandSyntaxException e) {
            throw new GameTestAssertException("Pin toggle-off command unexpectedly failed: " + e.getMessage());
        }
        if (progressData.state(player.getUUID()).pinnedQuests().contains(questId)) {
            throw new GameTestAssertException("Second pin command should toggle pinned quest off");
        }

        helper.succeed();
    }

    private static void expectCommandRejected(CommandDispatcher<CommandSourceStack> dispatcher, CommandSourceStack source, String command) {
        try {
            dispatcher.execute(command, source);
            throw new GameTestAssertException("Command should be rejected by permission boundary: " + command);
        } catch (CommandSyntaxException ignored) {
        }
    }

    private static ServerPlayer detachedPlayer(GameTestHelper helper) {
        return new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "qas_test_player")
        );
    }

    private static QuestDefinition quest(String id) {
        return quest(id, "Main", 0, 0, Set.of(), Map.of(), Map.of(), Set.of());
    }

    private static QuestDefinition quest(
            String id,
            String chapter,
            int x,
            int y,
            Set<String> prerequisites,
            Map<String, Integer> connectionColors,
            Map<String, String> connectionModes,
            Set<String> hiddenConnections
    ) {
        return new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                id,
                new QuestDisplay(
                        id,
                        "bulk",
                        List.of("sync"),
                        Map.of(chapter, new ChapterDef(true, x, y, 1.0f)),
                        "minecraft:book",
                        "minecraft:barrier"
                ),
                new QuestSettings(false, QuestVisibilityMode.LOCKED, false, false, false, true),
                prerequisites,
                connectionColors,
                connectionModes,
                hiddenConnections,
                Map.of(),
                Map.of()
        );
    }

    private static List<QuestDefinition> pastedByTitle(QuestDefinitionStore store, String chapter) {
        return store.quests().values().stream()
                .filter(quest -> quest.display().chapters().containsKey(chapter))
                .filter(quest -> "source/a".equals(quest.display().title()) || "source/b".equals(quest.display().title()))
                .toList();
    }

    private static void assertClipboardGraph(List<QuestDefinition> pasted) {
        QuestDefinition pastedA = pasted.stream()
                .filter(quest -> "source/a".equals(quest.display().title()))
                .findFirst()
                .orElseThrow(() -> new GameTestAssertException("Missing pasted source/a"));
        QuestDefinition pastedB = pasted.stream()
                .filter(quest -> "source/b".equals(quest.display().title()))
                .findFirst()
                .orElseThrow(() -> new GameTestAssertException("Missing pasted source/b"));

        if (!pastedA.prerequisites().isEmpty()) {
            throw new GameTestAssertException("External prerequisite on source/a should be dropped");
        }
        if (!pastedB.prerequisites().equals(Set.of(pastedA.id()))) {
            throw new GameTestAssertException("Pasted source/b should use only pasted source/a as a prerequisite, got " + pastedB.prerequisites());
        }
        if (!pastedB.connectionColors().keySet().equals(Set.of(pastedA.id()))) {
            throw new GameTestAssertException("Connection colors should only keep the remapped internal connection");
        }
        if (!pastedB.connectionModes().equals(Map.of(pastedA.id(), "grid"))) {
            throw new GameTestAssertException("Connection mode should follow the remapped internal connection");
        }
        if (!pastedB.hiddenConnections().equals(Set.of(pastedA.id()))) {
            throw new GameTestAssertException("Hidden connection should follow the remapped internal connection");
        }
    }

    private static CompoundTag emptyFullPayload() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema", QuestDefinition.CURRENT_SCHEMA);
        tag.put("chapters", new ListTag());
        tag.put("quests", new CompoundTag());
        return tag;
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

    private static String quoteArg(String value) {
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }
}
