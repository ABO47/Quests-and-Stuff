package com.abo47.questsandstuff.gametest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.chunkclaim.ChunkClaimPacketHelper;
import com.abo47.questsandstuff.chunkclaim.ChunkClaimService;
import com.abo47.questsandstuff.network.chunkclaim.C2SChunkClaimActionPacket;
import com.abo47.questsandstuff.network.chunkclaim.C2SChunkClaimConfigPacket;
import com.abo47.questsandstuff.network.chunkclaim.S2CChunkClaimSyncPacket;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.runtime.RuntimeEngine;
import com.abo47.questsandstuff.quest.sync.PerformanceTracker;
import com.abo47.questsandstuff.quest.sync.SyncService;
import com.abo47.questsandstuff.team.TeamManager;
import com.abo47.questsandstuff.team.model.TeamData;

import io.netty.buffer.Unpooled;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(QuestsAndStuffMod.MODID)
public final class ChunkClaimGameTests {
    private ChunkClaimGameTests() {
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void claimUnclaimAndOwnershipTracksTeam(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "cc1")) {
            ServerPlayer player = ctx.player("claimer");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);
            TeamData team = manager.createTeam(player);
            ResourceLocation dim = helper.getLevel().dimension().location();
            ChunkClaimService service = new ChunkClaimService(helper.getLevel().getServer());

            assertEqual(ChunkClaimService.ClaimResult.OK,
                    service.claim(team.teamId(), "claimer", dim, 4, 7), "First claim should succeed");
            assertTrue(service.isClaimed(team.teamId(), dim, 4, 7), "Chunk should be claimed");
            assertEqual(team.teamId(), service.ownerTeamIdOf(dim, 4, 7), "Owning team should match");
            assertEqual(ChunkClaimService.ClaimResult.ALREADY_CLAIMED,
                    service.claim(team.teamId(), "claimer", dim, 4, 7), "Duplicate claim should be rejected");
            assertEqual(ChunkClaimService.ClaimResult.OK,
                    service.unclaim(team.teamId(), dim, 4, 7), "Unclaim should succeed");
            assertEqual(ChunkClaimService.ClaimResult.NOT_CLAIMED,
                    service.unclaim(team.teamId(), dim, 4, 7), "Unclaim of free chunk should fail");
            assertNull(service.ownerTeamIdOf(dim, 4, 7), "Freed chunk should have no owner");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void forceLoadAppliesChunkForcing(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "cc2")) {
            ServerPlayer player = ctx.player("forcer");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);
            TeamData team = manager.createTeam(player);
            ResourceLocation dim = helper.getLevel().dimension().location();
            ChunkClaimService service = new ChunkClaimService(helper.getLevel().getServer());
            ChunkPos pos = new ChunkPos(2, 3);

            service.claim(team.teamId(), "forcer", dim, pos.x, pos.z);
            assertEqual(ChunkClaimService.ClaimResult.OK,
                    service.setForceLoaded(team.teamId(), dim, pos.x, pos.z, true),
                    "Arming force load should succeed");
            assertTrue(isForced(helper, pos), "Chunk should be force loaded");
            assertTrue(service.isForceLoaded(team.teamId(), dim, pos.x, pos.z), "Service should report force loaded");

            assertEqual(ChunkClaimService.ClaimResult.OK,
                    service.setForceLoaded(team.teamId(), dim, pos.x, pos.z, false),
                    "Disarming force load should succeed");
            assertTrue(!isForced(helper, pos), "Chunk should no longer be force loaded");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void claimCapLimitsTeamTotal(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "cc3")) {
            ServerPlayer player = ctx.player("cap");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);
            TeamData team = manager.createTeam(player);
            ResourceLocation dim = helper.getLevel().dimension().location();
            ChunkClaimService service = new ChunkClaimService(helper.getLevel().getServer());

            int previous = QuestsAndStuffConfig.chunkClaimMaxClaimedChunks();
            QuestsAndStuffConfig.setChunkClaimMaxClaimedChunks(2);
            try {
                assertEqual(ChunkClaimService.ClaimResult.OK, service.claim(team.teamId(), "cap", dim, 0, 0), "First claim ok");
                assertEqual(ChunkClaimService.ClaimResult.OK, service.claim(team.teamId(), "cap", dim, 0, 1), "Second claim ok");
                assertEqual(ChunkClaimService.ClaimResult.LIMIT_REACHED,
                        service.claim(team.teamId(), "cap", dim, 0, 2), "Third claim should hit cap");
                assertEqual(2, service.countClaimed(team.teamId()), "Only two chunks claimed");
            } finally {
                QuestsAndStuffConfig.setChunkClaimMaxClaimedChunks(previous);
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void actionAndConfigPacketsRoundtrip(GameTestHelper helper) {
        ResourceLocation dim = helper.getLevel().dimension().location();
        C2SChunkClaimActionPacket action = new C2SChunkClaimActionPacket(
                C2SChunkClaimActionPacket.Action.TOGGLE_FORCE, dim, 9, -4);
        C2SChunkClaimActionPacket decodedAction = roundtrip(action);
        assertEqual(C2SChunkClaimActionPacket.Action.TOGGLE_FORCE, decodedAction.action(), "Action enum mismatch");
        assertEqual(dim, decodedAction.dimension(), "Action dimension mismatch");
        assertEqual(9, decodedAction.x(), "Action x mismatch");
        assertEqual(-4, decodedAction.z(), "Action z mismatch");

        C2SChunkClaimConfigPacket config = new C2SChunkClaimConfigPacket(
                true, false, true, false, true, false, 64, 8);
        C2SChunkClaimConfigPacket decodedConfig = roundtrip(config);
        assertTrue(decodedConfig.protectBreakPlace(), "config protectBreakPlace mismatch");
        assertTrue(!decodedConfig.protectInteraction(), "config protectInteraction mismatch");
        assertTrue(decodedConfig.protectExplosions(), "config protectExplosions mismatch");
        assertTrue(!decodedConfig.protectMobGriefing(), "config protectMobGriefing mismatch");
        assertTrue(decodedConfig.protectPvp(), "config protectPvp mismatch");
        assertTrue(!decodedConfig.protectFire(), "config protectFire mismatch");
        assertEqual(64, decodedConfig.maxClaimedChunks(), "config maxClaimed mismatch");
        assertEqual(8, decodedConfig.maxForceLoadedChunks(), "config maxForceLoaded mismatch");

        ChunkClaimService emptyService = new ChunkClaimService(helper.getLevel().getServer());
        CompoundTag payload = ChunkClaimPacketHelper.encodeAll(emptyService);
        S2CChunkClaimSyncPacket sync = new S2CChunkClaimSyncPacket(payload);
        S2CChunkClaimSyncPacket decodedSync = roundtrip(sync);
        assertEqual(payload, decodedSync.payload(), "sync payload mismatch");

        helper.succeed();
    }

    private static boolean isForced(GameTestHelper helper, ChunkPos pos) {
        return helper.getLevel().getForcedChunks().contains(ChunkPos.asLong(pos.x, pos.z));
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundtrip(T packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        if (packet instanceof C2SChunkClaimActionPacket action) {
            action.encode(buf);
            return (T) C2SChunkClaimActionPacket.decode(buf);
        }
        if (packet instanceof C2SChunkClaimConfigPacket config) {
            config.encode(buf);
            return (T) C2SChunkClaimConfigPacket.decode(buf);
        }
        if (packet instanceof S2CChunkClaimSyncPacket sync) {
            sync.encode(buf);
            return (T) S2CChunkClaimSyncPacket.decode(buf);
        }
        throw new GameTestAssertException("Unsupported packet type");
    }

    private static TestBundle createBundle(GameTestHelper helper, String rootName) throws IOException {
        Path root = Files.createTempDirectory("qas_chunkclaim_" + rootName + "_");
        QuestDefinitionStore store = new QuestDefinitionStore(root);
        QuestProgressSavedData progressData = QuestProgressSavedData.get(helper.getLevel().getServer());
        PerformanceTracker perf = new PerformanceTracker();
        SyncService sync = new SyncService(store, progressData, perf);
        RuntimeEngine engine = new RuntimeEngine(store, progressData, sync, perf);
        sync.setVisibilityFilter(engine::isVisibleFor);
        return new TestBundle(engine, store, helper);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }

    private static void assertNull(Object obj, String message) {
        if (obj != null) throw new GameTestAssertException(message);
    }

    private static void assertEqual(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new GameTestAssertException(message + " (expected=" + expected + " actual=" + actual + ")");
        }
    }

    private static final class TestBundle implements AutoCloseable {
        final RuntimeEngine engine;
        private final GameTestHelper helper;
        private final QuestDefinitionStore store;

        TestBundle(RuntimeEngine engine, QuestDefinitionStore store, GameTestHelper helper) {
            this.engine = engine;
            this.store = store;
            this.helper = helper;
        }

        ServerPlayer player(String name) {
            return new ServerPlayer(
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    new GameProfile(UUID.randomUUID(), name)
            );
        }

        @Override
        public void close() {
            store.shutdown();
        }
    }
}
