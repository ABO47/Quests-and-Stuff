package com.abo47.questsandstuff.gametest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.team.TeamData;
import com.abo47.questsandstuff.quest.model.team.TeamMember;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.runtime.QuestRuntimeEngine;
import com.abo47.questsandstuff.quest.sync.QuestPerformanceTracker;
import com.abo47.questsandstuff.quest.sync.QuestSyncService;
import com.abo47.questsandstuff.quest.team.TeamManager;
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
import java.util.UUID;

@GameTestHolder(QuestsAndStuffMod.MODID)
public final class TeamManagerGameTests {
    private TeamManagerGameTests() {
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void createTeamReturnsNewTeam(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "ct1")) {
            ServerPlayer player = ctx.player("creator");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            TeamData team = manager.createTeam(player);
            assertNotNull(team, "createTeam should return a non-null team");
            assertEqual(1, team.members().size(), "New team should have exactly one member");
            assertTrue(team.isOwner(player.getUUID()), "Creator should be the owner");
            assertTrue(team.inviteCode().isBlank(), "New team should have a blank invite code");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void createTeamReturnsExistingIfAlreadyInTeam(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "ct2")) {
            ServerPlayer player = ctx.player("dup_check");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            TeamData first = manager.createTeam(player);
            TeamData second = manager.createTeam(player);
            assertNotNull(second, "Second createTeam should return the existing team");
            assertTrue(first.teamId().equals(second.teamId()), "Second createTeam should return the same team ID");
            assertEqual(1, second.members().size(), "Existing team should still have exactly one member");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void joinTeamWithValidInviteCode(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "jt1")) {
            ServerPlayer owner = ctx.player("owner_join");
            ServerPlayer joiner = ctx.player("joiner");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            manager.createTeam(owner);
            String code = manager.generateInviteCode(owner);
            assertFalse(code.isBlank(), "Owner should be able to generate an invite code");

            TeamData joined = manager.joinTeam(joiner, code);
            assertNotNull(joined, "joinTeam with valid code should return the team");
            assertEqual(2, joined.members().size(), "Team should have 2 members after join");
            assertTrue(joined.isMember(joiner.getUUID()), "Joiner should be a member after joining");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void joinTeamReturnsExistingIfAlreadyInTeam(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "jt2")) {
            ServerPlayer player = ctx.player("already_in");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            manager.createTeam(player);
            String code = manager.generateInviteCode(player);
            TeamData joined = manager.joinTeam(player, code);
            assertNotNull(joined, "joinTeam on own team should return existing team");
            assertEqual(1, joined.members().size(), "Team should still have 1 member");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void joinTeamWithInvalidCodeReturnsNull(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "jt3")) {
            ServerPlayer player = ctx.player("invalid_join");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            TeamData result = manager.joinTeam(player, "INVALID99");
            assertNull(result, "joinTeam with wrong code should return null");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void ownerCannotLeaveTeam(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "ol1")) {
            ServerPlayer owner = ctx.player("owner_leave");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            manager.createTeam(owner);
            boolean left = manager.leaveTeam(owner);
            assertFalse(left, "Owner should not be able to leave the team");
            TeamData team = manager.getTeam(owner);
            assertNotNull(team, "Owner should still have a team after failed leave");
            assertEqual(1, team.members().size(), "Team should still have 1 member after failed owner leave");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void memberCanLeaveTeam(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "ml1")) {
            ServerPlayer owner = ctx.player("owner_leave_mem");
            ServerPlayer member = ctx.player("leaving_member");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            manager.createTeam(owner);
            String code = manager.generateInviteCode(owner);
            manager.joinTeam(member, code);

            assertEqual(2, manager.getTeam(member).members().size(), "Setup: team should have 2 members before leave");

            boolean left = manager.leaveTeam(member);
            assertTrue(left, "Member should be able to leave the team");

            TeamData teamAfter = manager.getTeam(owner);
            assertNotNull(teamAfter, "Owner's team should still exist after member leaves");
            assertEqual(1, teamAfter.members().size(), "Team should have 1 member after member leaves");
            assertNull(manager.getTeam(member), "Leaving member should no longer have a team");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void ownerCanKickMember(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "ok1")) {
            ServerPlayer owner = ctx.player("owner_kick");
            ServerPlayer member = ctx.player("kicked_member");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            manager.createTeam(owner);
            String code = manager.generateInviteCode(owner);
            manager.joinTeam(member, code);

            boolean kicked = manager.kickMember(owner, member.getUUID());
            assertTrue(kicked, "Owner should be able to kick a member");

            TeamData teamAfter = manager.getTeam(owner);
            assertEqual(1, teamAfter.members().size(), "Team should have 1 member after kick");
            assertNull(manager.getTeam(member), "Kicked member should no longer have a team");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void nonOwnerCannotKick(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "nk1")) {
            ServerPlayer owner = ctx.player("owner_nokick");
            ServerPlayer memberA = ctx.player("member_a");
            ServerPlayer memberB = ctx.player("member_b");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            manager.createTeam(owner);
            String code = manager.generateInviteCode(owner);
            manager.joinTeam(memberA, code);
            code = manager.generateInviteCode(owner);
            manager.joinTeam(memberB, code);

            boolean kicked = manager.kickMember(memberA, memberB.getUUID());
            assertFalse(kicked, "Non-owner should not be able to kick other members");
            assertEqual(3, manager.getTeam(owner).members().size(), "Team should still have 3 members after failed kick");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void ownerCannotKickSelf(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "os1")) {
            ServerPlayer owner = ctx.player("self_kick");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            manager.createTeam(owner);
            boolean kicked = manager.kickMember(owner, owner.getUUID());
            assertFalse(kicked, "Owner should not be able to kick themselves");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void transferOwnershipChangesOwner(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "to1")) {
            ServerPlayer owner = ctx.player("old_owner");
            ServerPlayer newOwner = ctx.player("new_owner");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            manager.createTeam(owner);
            String code = manager.generateInviteCode(owner);
            manager.joinTeam(newOwner, code);

            boolean transferred = manager.transferOwnership(owner, newOwner.getUUID());
            assertTrue(transferred, "Transfer ownership should succeed");

            TeamData teamAfter = manager.getTeam(newOwner);
            assertNotNull(teamAfter, "New owner should have a team after transfer");
            assertTrue(teamAfter.isOwner(newOwner.getUUID()), "New owner should be the owner after transfer");
            assertFalse(teamAfter.isOwner(owner.getUUID()), "Old owner should no longer be the owner");

            TeamMember oldMember = teamAfter.findMember(owner.getUUID());
            assertNotNull(oldMember, "Old owner should still be a member");
            assertEqual(TeamMember.Role.MEMBER, oldMember.role(), "Old owner should become a regular member");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void nonOwnerCannotTransferOwnership(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "nt1")) {
            ServerPlayer owner = ctx.player("owner_transfer_x");
            ServerPlayer member = ctx.player("member_transfer_x");
            ServerPlayer target = ctx.player("target_transfer_x");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            manager.createTeam(owner);
            String code = manager.generateInviteCode(owner);
            manager.joinTeam(member, code);
            code = manager.generateInviteCode(owner);
            manager.joinTeam(target, code);

            boolean transferred = manager.transferOwnership(member, target.getUUID());
            assertFalse(transferred, "Non-owner should not be able to transfer ownership");
            assertTrue(owner.getUUID().equals(manager.getTeam(owner).owner()), "Owner should remain unchanged after failed transfer");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void generateInviteCodeReturnsValidCode(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "gc1")) {
            ServerPlayer owner = ctx.player("code_gen_owner");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            manager.createTeam(owner);
            String code = manager.generateInviteCode(owner);
            assertFalse(code.isBlank(), "generateInviteCode should return a non-blank code");
            assertEqual(8, code.length(), "Invite code should be 8 characters");
            String validChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
            for (char c : code.toCharArray()) {
                assertTrue(validChars.indexOf(c) >= 0, "Invite code character '" + c + "' is not in the valid set");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void nonOwnerCannotGenerateInviteCode(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "ng1")) {
            ServerPlayer owner = ctx.player("owner_no_gen");
            ServerPlayer member = ctx.player("member_no_gen");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            manager.createTeam(owner);
            String code = manager.generateInviteCode(owner);
            manager.joinTeam(member, code);

            String memberCode = manager.generateInviteCode(member);
            assertTrue(memberCode.isBlank(), "Non-owner should not be able to generate invite codes");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void inviteCodeExpiry(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "ie1")) {
            ServerPlayer owner = ctx.player("expiry_owner");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            manager.createTeam(owner);
            String code = manager.generateInviteCode(owner);
            assertFalse(code.isBlank(), "Invite code should be generated");

            TeamData team = manager.getTeam(owner);
            assertTrue(team.inviteExpiryMs() > System.currentTimeMillis(), "Invite code should have a future expiry time");
            assertTrue(team.inviteExpiryMs() <= System.currentTimeMillis() + 3600_000L + 1000L, "Invite expiry should be within 1 hour + 1 second of now");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void playerWithoutTeamReturnsNull(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "pt1")) {
            ServerPlayer player = ctx.player("no_team_player");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            TeamData team = manager.getTeam(player);
            assertNull(team, "Player not in a team should have null team");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void getTeamByIdReturnsCorrectTeam(GameTestHelper helper) {
        try (TestBundle ctx = createBundle(helper, "ti1")) {
            ServerPlayer owner = ctx.player("id_owner");
            TeamManager manager = new TeamManager(helper.getLevel(), ctx.engine);

            TeamData team = manager.createTeam(owner);
            TeamData found = manager.getTeamById(team.teamId());
            assertNotNull(found, "getTeamById should return the team");
            assertTrue(team.teamId().equals(found.teamId()), "Team IDs should match");
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create context: " + e.getMessage());
        }
        helper.succeed();
    }

    private static TestBundle createBundle(GameTestHelper helper, String rootName) throws IOException {
        Path root = Files.createTempDirectory("qas_teams_" + rootName + "_");
        QuestDefinitionStore store = new QuestDefinitionStore(root);
        QuestProgressSavedData progressData = QuestProgressSavedData.get(helper.getLevel().getServer());
        QuestPerformanceTracker perf = new QuestPerformanceTracker();
        QuestSyncService sync = new QuestSyncService(store, progressData, perf);
        QuestRuntimeEngine engine = new QuestRuntimeEngine(store, progressData, sync, perf);
        sync.setVisibilityFilter(engine::isVisibleFor);
        return new TestBundle(engine, store, helper);
    }

    private static ServerPlayer createPlayer(GameTestHelper helper, String name) {
        return new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), name)
        );
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) throw new GameTestAssertException(message);
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) throw new GameTestAssertException(message);
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
        final QuestRuntimeEngine engine;
        private final GameTestHelper helper;
        private final QuestDefinitionStore store;

        TestBundle(QuestRuntimeEngine engine, QuestDefinitionStore store, GameTestHelper helper) {
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
