package com.abo47.questsandstuff.gametest;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.network.quest.runtime.C2SManualTaskPacket;
import com.abo47.questsandstuff.network.quest.editor.C2SResetQuestPacket;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.GroupDef;
import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.abo47.questsandstuff.quest.runtime.RuntimeEngine;
import com.abo47.questsandstuff.quest.sync.PerformanceTracker;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.sync.SyncService;
import com.abo47.questsandstuff.quest.runtime.team.TeamProgressProvider;
import com.abo47.questsandstuff.quest.runtime.team.TeamProgressProviders;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

@GameTestHolder(QuestsAndStuffMod.MODID)
public final class QuestRewardAndTeamGameTests {
    private QuestRewardAndTeamGameTests() {
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void rewardClaimsCoverDefaultTypes(GameTestHelper helper) {
        Bundle bundle = null;
        try {
            bundle = createBundle(helper, "reward_claim_types");
            ServerPlayer player = createDetachedServerPlayer(helper);

            Map<String, QuestRewardDefinition> rewards = new LinkedHashMap<>();
            rewards.put("item_reward", reward("item_reward", "item", 2, "minecraft:apple", false, Map.of()));
            rewards.put("xp_reward", reward("xp_reward", "xp", 7, "", false, Map.of()));
            rewards.put("loot_reward", reward("loot_reward", "loot_table", 3, "minecraft:empty", false, Map.of("fallback_item", "minecraft:stick")));
            rewards.put("command_reward", reward("command_reward", "command", 1, "", false, Map.of("command", "gamerule doDaylightCycle false")));

            QuestDefinition definition = quest("test/reward_claim_types", QuestSettings.DEFAULT, Map.of(), rewards, Set.of());
            bundle.store.upsert(definition);
            bundle.engine.rebuildIndex();

            int applesBefore = countItems(player, "minecraft:apple");
            bundle.engine.claimReward(player, definition.id(), "item_reward");
            if (countItems(player, "minecraft:apple") - applesBefore != 2) {
                throw new GameTestAssertException("Item reward did not give expected item count");
            }

            int xpBefore = player.totalExperience;
            bundle.engine.claimReward(player, definition.id(), "xp_reward");
            if (player.totalExperience - xpBefore < 7) {
                throw new GameTestAssertException("XP reward did not give expected points");
            }

            int sticksBefore = countItems(player, "minecraft:stick");
            bundle.engine.claimReward(player, definition.id(), "loot_reward");
            if (countItems(player, "minecraft:stick") != sticksBefore) {
                throw new GameTestAssertException("Empty loot reward should not grant fallback items");
            }
            if (!bundle.progressData.state(player.getUUID()).quest(definition.id()).claimedRewards().contains("loot_reward")) {
                throw new GameTestAssertException("Empty loot reward should still be marked claimed");
            }

            bundle.engine.claimReward(player, definition.id(), "command_reward");
            if (!bundle.progressData.state(player.getUUID()).quest(definition.id()).claimedRewards().contains("command_reward")) {
                throw new GameTestAssertException("Command reward was not marked claimed");
            }

            int itemBeforeDuplicate = countItems(player, "minecraft:apple");
            bundle.engine.claimReward(player, definition.id(), "item_reward");
            if (countItems(player, "minecraft:apple") != itemBeforeDuplicate) {
                throw new GameTestAssertException("Duplicate reward claim should not execute reward twice");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create quest bundle: " + e.getMessage());
        } finally {
            if (bundle != null) {
                bundle.close();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void selectableClaimAlsoClaimsNormalRewards(GameTestHelper helper) {
        Bundle bundle = null;
        try {
            bundle = createBundle(helper, "selectable_claim_normal");
            ServerPlayer player = createDetachedServerPlayer(helper);

            Map<String, QuestRewardDefinition> rewards = new LinkedHashMap<>();
            rewards.put("normal_item", reward("normal_item", "item", 3, "minecraft:apple", false, Map.of()));
            rewards.put("selector_a", reward("selector_a", "selectable", 1, "", true, Map.of(
                    "pick_count", "1",
                    "choices", "sel_a"
            )));
            rewards.put("selector_b", reward("selector_b", "selectable", 1, "", true, Map.of(
                    "pick_count", "1",
                    "choices", "sel_b"
            )));

            QuestDefinition definition = quest("test/selectable_claim_normal", QuestSettings.DEFAULT, Map.of(), rewards, Set.of());
            bundle.store.upsert(definition);
            bundle.engine.rebuildIndex();

            int applesBefore = countItems(player, "minecraft:apple");
            int carrotsBefore = countItems(player, "minecraft:carrot");
            bundle.engine.claimAllRewards(player, definition.id());
            bundle.engine.claimReward(player, definition.id(), "normal_item");
            bundle.engine.claimSelectedRewardAndAvailableRewards(player, definition.id(), "selector_a", List.of("missing"));
            var blockedState = bundle.progressData.state(player.getUUID()).quest(definition.id());
            if (countItems(player, "minecraft:apple") != applesBefore || blockedState.claimedRewards().contains("normal_item")) {
                throw new GameTestAssertException("Normal rewards should wait for a valid selectable reward claim");
            }
            if (countItems(player, "minecraft:carrot") != carrotsBefore || blockedState.claimedRewards().contains("selector_a")) {
                throw new GameTestAssertException("Invalid selectable claims should not execute rewards");
            }

            bundle.engine.claimSelectedRewardAndAvailableRewards(player, definition.id(), "selector_a", List.of("sel_a"));

            var state = bundle.progressData.state(player.getUUID()).quest(definition.id());
            if (countItems(player, "minecraft:apple") - applesBefore != 3) {
                throw new GameTestAssertException("Selectable claim should also grant normal item rewards");
            }
            if (countItems(player, "minecraft:carrot") - carrotsBefore != 1) {
                throw new GameTestAssertException("Selectable claim should grant the selected child reward");
            }
            if (!state.claimedRewards().contains("normal_item") || !state.claimedRewards().contains("selector_a")) {
                throw new GameTestAssertException("Selectable claim should mark normal and selected rewards claimed");
            }
            if (!state.claimedRewards().contains("selector_b")) {
                throw new GameTestAssertException("Unselected one-choice selectable rewards should be marked skipped");
            }
            if (!state.claimedRewards().containsAll(rewards.keySet())) {
                throw new GameTestAssertException("All reward ids should be claimed after one singleton selectable reward is picked");
            }

            QuestDefinition stuckDefinition = quest("test/selectable_claim_stuck", QuestSettings.DEFAULT, Map.of(), rewards, Set.of());
            bundle.store.upsert(stuckDefinition);
            bundle.engine.rebuildIndex();
            var stuckState = bundle.progressData.state(player.getUUID()).quest(stuckDefinition.id());
            stuckState.claimedRewards().add("normal_item");
            stuckState.claimedRewards().add("selector_a");

            int carrotsBeforeRetry = countItems(player, "minecraft:carrot");
            bundle.engine.claimSelectedRewardAndAvailableRewards(player, stuckDefinition.id(), "selector_a", List.of("sel_a"));
            if (countItems(player, "minecraft:carrot") != carrotsBeforeRetry) {
                throw new GameTestAssertException("Retrying a stuck selectable claim should not grant the selected reward twice");
            }
            if (!stuckState.claimedRewards().containsAll(rewards.keySet())) {
                throw new GameTestAssertException("Retrying a stuck selectable claim should mark skipped singleton choices claimed");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create quest bundle: " + e.getMessage());
        } finally {
            if (bundle != null) {
                bundle.close();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void autoClaimWaitsForSelectableRewardChoice(GameTestHelper helper) {
        Bundle bundle = null;
        try {
            bundle = createBundle(helper, "auto_claim_selectable_wait");
            ServerPlayer player = createDetachedServerPlayer(helper);

            QuestSettings autoClaim = new QuestSettings(
                    false,
                    QuestVisibilityMode.PREREQUISITES_VISIBLE,
                    false,
                    true,
                    false,
                    true
            );
            Map<String, QuestRewardDefinition> rewards = new LinkedHashMap<>();
            rewards.put("normal_item", reward("normal_item", "item", 2, "minecraft:apple", false, Map.of()));
            rewards.put("selector", reward("selector", "selectable", 1, "", true, Map.of(
                    "pick_count", "1",
                    "choices", "sel_a"
            )));

            QuestDefinition definition = quest("test/auto_claim_selectable_wait", autoClaim, Map.of(), rewards, Set.of());
            bundle.store.upsert(definition);
            bundle.engine.rebuildIndex();

            int applesBefore = countItems(player, "minecraft:apple");
            bundle.engine.completeQuest(player, definition.id());
            var completedState = bundle.progressData.state(player.getUUID()).quest(definition.id());
            if (countItems(player, "minecraft:apple") != applesBefore || completedState.claimedRewards().contains("normal_item")) {
                throw new GameTestAssertException("Auto-claim should wait for selectable rewards before granting normal rewards");
            }

            bundle.engine.claimSelectedRewardAndAvailableRewards(player, definition.id(), "selector", List.of("sel_a"));
            if (countItems(player, "minecraft:apple") - applesBefore != 2) {
                throw new GameTestAssertException("Normal rewards should grant after the selectable reward choice is claimed");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create quest bundle: " + e.getMessage());
        } finally {
            if (bundle != null) {
                bundle.close();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void tasklessRewardClaimRequiresUnlock(GameTestHelper helper) {
        Bundle bundle = null;
        try {
            bundle = createBundle(helper, "taskless_reward_unlock");
            ServerPlayer player = createDetachedServerPlayer(helper);

            QuestDefinition parent = quest("test/reward_parent", QuestSettings.DEFAULT, Map.of(), Map.of(), Set.of());
            QuestDefinition child = quest(
                    "test/reward_locked_child",
                    QuestSettings.DEFAULT,
                    Map.of(),
                    Map.of("item_reward", reward("item_reward", "item", 1, "minecraft:apple", false, Map.of())),
                    Set.of(parent.id())
            );
            bundle.store.upsert(parent);
            bundle.store.upsert(child);
            bundle.engine.rebuildIndex();

            int applesBefore = countItems(player, "minecraft:apple");
            bundle.engine.claimReward(player, child.id(), "item_reward");
            var lockedState = bundle.progressData.state(player.getUUID()).quest(child.id());
            if (countItems(player, "minecraft:apple") != applesBefore) {
                throw new GameTestAssertException("Locked taskless reward should not grant items");
            }
            if (lockedState.unlocked() || lockedState.claimedRewards().contains("item_reward")) {
                throw new GameTestAssertException("Locked taskless reward should not be marked unlocked or claimed");
            }

            bundle.engine.completeQuest(player, parent.id());
            bundle.engine.claimReward(player, child.id(), "item_reward");
            var unlockedState = bundle.progressData.state(player.getUUID()).quest(child.id());
            if (countItems(player, "minecraft:apple") - applesBefore != 1) {
                throw new GameTestAssertException("Unlocked taskless reward should grant items");
            }
            if (!unlockedState.unlocked() || !unlockedState.claimedRewards().contains("item_reward")) {
                throw new GameTestAssertException("Unlocked taskless reward should be marked claimed");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create quest bundle: " + e.getMessage());
        } finally {
            if (bundle != null) {
                bundle.close();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void manualCheckPacketOnlyCompletesVisibleExactTask(GameTestHelper helper) {
        Bundle bundle = null;
        try {
            bundle = createBundle(helper, "manual_check_packet_scope");
            ServerPlayer player = createDetachedServerPlayer(helper);

            QuestDefinition visible = quest(
                    "test/manual_check_visible",
                    QuestSettings.DEFAULT,
                    Map.of(
                            "clicked", task("clicked", "check", 1, "shared/manual_key", Map.of()),
                            "same_key", task("same_key", "check", 1, "shared/manual_key", Map.of())
                    ),
                    Map.of(),
                    Set.of()
            );
            QuestDefinition hidden = quest(
                    "test/manual_check_hidden",
                    new QuestSettings(false, QuestVisibilityMode.COMPLETED, false, false, false, true),
                    Map.of("hidden", task("hidden", "check", 1, "hidden/manual_key", Map.of())),
                    Map.of(),
                    Set.of()
            );
            bundle.store.upsert(visible);
            bundle.store.upsert(hidden);
            bundle.engine.rebuildIndex();

            new C2SManualTaskPacket(visible.id(), "clicked").handle(immediateContext(player));
            var visibleState = bundle.progressData.state(player.getUUID()).quest(visible.id());
            if (visibleState.getTaskCount("clicked") != 1) {
                throw new GameTestAssertException("Manual check packet should complete the clicked requirement");
            }
            if (visibleState.getTaskCount("same_key") != 0) {
                throw new GameTestAssertException("Manual check packet should not complete another requirement sharing the same target key");
            }

            new C2SManualTaskPacket(hidden.id(), "hidden").handle(immediateContext(player));
            var hiddenState = bundle.progressData.state(player.getUUID()).quest(hidden.id());
            if (hiddenState.getTaskCount("hidden") != 0 || hiddenState.completed()) {
                throw new GameTestAssertException("Manual check packet should not complete hidden non-visible requirements");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create quest bundle: " + e.getMessage());
        } finally {
            if (bundle != null) {
                bundle.close();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void nonEditorResetPacketDoesNotClearClaimedRewards(GameTestHelper helper) {
        Bundle bundle = null;
        try {
            bundle = createBundle(helper, "reset_packet_permission");
            ServerPlayer player = createDetachedServerPlayer(helper);

            QuestDefinition definition = quest(
                    "test/reset_packet_permission",
                    QuestSettings.DEFAULT,
                    Map.of(),
                    Map.of("item_reward", reward("item_reward", "item", 1, "minecraft:apple", false, Map.of())),
                    Set.of()
            );
            bundle.store.upsert(definition);
            bundle.engine.rebuildIndex();

            bundle.engine.claimReward(player, definition.id(), "item_reward");
            var state = bundle.progressData.state(player.getUUID()).quest(definition.id());
            if (!state.claimedRewards().contains("item_reward")) {
                throw new GameTestAssertException("Test setup should start with a claimed reward");
            }

            new C2SResetQuestPacket(definition.id()).handle(immediateContext(player));
            var afterResetAttempt = bundle.progressData.state(player.getUUID()).quest(definition.id());
            if (!afterResetAttempt.claimedRewards().contains("item_reward")) {
                throw new GameTestAssertException("Non-editor reset packet should not clear claimed rewards");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create quest bundle: " + e.getMessage());
        } finally {
            if (bundle != null) {
                bundle.close();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void commandRewardsRespectConfig(GameTestHelper helper) {
        Bundle bundle = null;
        boolean previousCommandRewards = QuestsAndStuffConfig.commandRewardsEnabled();
        try {
            QuestsAndStuffConfig.setCommandRewardsEnabled(false);
            bundle = createBundle(helper, "command_reward_config");
            ServerPlayer player = createDetachedServerPlayer(helper);

            QuestDefinition definition = quest(
                    "test/command_reward_config",
                    QuestSettings.DEFAULT,
                    Map.of(),
                    Map.of("command_reward", reward("command_reward", "command", 1, "", false, Map.of("command", "give @s minecraft:apple 1"))),
                    Set.of()
            );
            bundle.store.upsert(definition);
            bundle.engine.rebuildIndex();

            int applesBefore = countItems(player, "minecraft:apple");
            bundle.engine.claimReward(player, definition.id(), "command_reward");
            if (countItems(player, "minecraft:apple") != applesBefore) {
                throw new GameTestAssertException("Disabled command rewards should not execute commands");
            }
            if (bundle.progressData.state(player.getUUID()).quest(definition.id()).claimedRewards().contains("command_reward")) {
                throw new GameTestAssertException("Disabled command rewards should not be marked claimed");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create quest bundle: " + e.getMessage());
        } finally {
            QuestsAndStuffConfig.setCommandRewardsEnabled(previousCommandRewards);
            if (bundle != null) {
                bundle.close();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void repeatableQuestIgnoresDisabledCommandRewardForReset(GameTestHelper helper) {
        Bundle bundle = null;
        boolean previousCommandRewards = QuestsAndStuffConfig.commandRewardsEnabled();
        try {
            QuestsAndStuffConfig.setCommandRewardsEnabled(false);
            bundle = createBundle(helper, "repeatable_disabled_command");
            ServerPlayer player = createDetachedServerPlayer(helper);

            QuestSettings settings = new QuestSettings(
                    false,
                    QuestVisibilityMode.LOCKED,
                    true,
                    false,
                    false,
                    true
            );
            QuestDefinition definition = quest(
                    "test/repeatable_disabled_command",
                    settings,
                    Map.of("check", task("check", "check", 1, "repeatable/disabled_command", Map.of())),
                    Map.of("command_reward", reward("command_reward", "command", 1, "", false, Map.of("command", "give @s minecraft:apple 1"))),
                    Set.of()
            );
            bundle.store.upsert(definition);
            bundle.engine.rebuildIndex();

            bundle.engine.completeQuest(player, definition.id());
            var completedState = bundle.progressData.state(player.getUUID()).quest(definition.id());
            if (!completedState.completed()) {
                throw new GameTestAssertException("Test setup should complete repeatable quest");
            }

            bundle.engine.claimReward(player, definition.id(), "command_reward");
            var resetState = bundle.progressData.state(player.getUUID()).quest(definition.id());
            if (resetState.completed()) {
                throw new GameTestAssertException("Disabled command reward should not block repeatable reset");
            }
            if (resetState.claimedRewards().contains("command_reward")) {
                throw new GameTestAssertException("Disabled command reward should not be marked claimed during repeatable reset");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create quest bundle: " + e.getMessage());
        } finally {
            QuestsAndStuffConfig.setCommandRewardsEnabled(previousCommandRewards);
            if (bundle != null) {
                bundle.close();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void repeatableAutoClaimResetsQuestState(GameTestHelper helper) {
        Bundle bundle = null;
        try {
            bundle = createBundle(helper, "repeatable_auto_claim");
            ServerPlayer player = createDetachedServerPlayer(helper);

            QuestSettings settings = new QuestSettings(
                    false,
                    QuestVisibilityMode.LOCKED,
                    true,
                    true,
                    false,
                    true
            );

            Map<String, QuestTaskDefinition> tasks = Map.of(
                    "check", task("check", "check", 1, "repeatable/check", Map.of())
            );
            Map<String, QuestRewardDefinition> rewards = Map.of(
                    "xp_reward", reward("xp_reward", "xp", 2, "", false, Map.of())
            );
            QuestDefinition definition = quest("test/repeatable_auto_claim", settings, tasks, rewards, Set.of());
            bundle.store.upsert(definition);
            bundle.engine.rebuildIndex();

            int xpBefore = player.totalExperience;
            bundle.engine.completeQuest(player, definition.id());
            var state = bundle.progressData.state(player.getUUID()).quest(definition.id());

            if (state.completed()) {
                throw new GameTestAssertException("Repeatable auto-claim quest should reset completion state");
            }
            if (!state.unlocked()) {
                throw new GameTestAssertException("Repeatable auto-claim quest should remain unlocked after reset");
            }
            if (!state.claimedRewards().isEmpty()) {
                throw new GameTestAssertException("Repeatable reset should clear claimed reward flags");
            }
            if (state.getTaskCount("check") != 0) {
                throw new GameTestAssertException("Repeatable reset should clear task progress");
            }
            if (player.totalExperience - xpBefore < 2) {
                throw new GameTestAssertException("Auto-claim should execute non-selectable reward exactly once before reset");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create quest bundle: " + e.getMessage());
        } finally {
            if (bundle != null) {
                bundle.close();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void teamProviderHookSyncsSharedProgress(GameTestHelper helper) {
        Bundle bundle = null;
        TestTeamProvider provider = new TestTeamProvider();
        try {
            TeamProgressProviders.bootstrapDefaults();
            TeamProgressProviders.register(provider);

            bundle = createBundle(helper, "team_provider_sync");
            UUID a = UUID.randomUUID();
            UUID b = UUID.randomUUID();
            provider.setMembers(List.of(a, b));

            Map<String, QuestTaskDefinition> tasks = Map.of(
                    "check", task("check", "item", 10, "minecraft:stone", Map.of("collection_mode", "automatic"))
            );
            Map<String, QuestRewardDefinition> rewards = Map.of(
                    "r1", reward("r1", "xp", 1, "", false, Map.of())
            );
            QuestDefinition definition = quest(
                    "test/team_shared",
                    new QuestSettings(false, QuestVisibilityMode.LOCKED, false, false, false, true),
                    tasks,
                    rewards,
                    Set.of()
            );
            bundle.store.upsert(definition);
            bundle.engine.rebuildIndex();

            var stateA = bundle.progressData.state(a).quest(definition.id());
            stateA.setUnlocked(true);
            stateA.setCompleted(true, 20L);
            stateA.claimedRewards().add("r1");
            stateA.addTaskCount("check", 7, Integer.MAX_VALUE);

            var stateB = bundle.progressData.state(b).quest(definition.id());
            stateB.setUnlocked(false);
            stateB.setCompleted(false, 0L);
            stateB.addTaskCount("check", 2, Integer.MAX_VALUE);

            provider.fire(helper.getLevel(), a);

            var mergedA = bundle.progressData.state(a).quest(definition.id());
            var mergedB = bundle.progressData.state(b).quest(definition.id());
            if (!mergedA.unlocked() || !mergedB.unlocked()) {
                throw new GameTestAssertException("Team sync should propagate unlocked state to all members");
            }
            if (!mergedA.completed() || !mergedB.completed()) {
                throw new GameTestAssertException("Team sync should propagate completed state to all members");
            }
            if (!mergedA.claimedRewards().contains("r1") || !mergedB.claimedRewards().contains("r1")) {
                throw new GameTestAssertException("Team sync should propagate claimed rewards to all members");
            }
            if (mergedA.getTaskCount("check") != 7 || mergedB.getTaskCount("check") != 7) {
                throw new GameTestAssertException("Team sync should propagate max task progress to all members");
            }
        } catch (IOException e) {
            throw new GameTestAssertException("Failed to create quest bundle: " + e.getMessage());
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
        return new Bundle(store, progressData, engine);
    }

    private static ServerPlayer createDetachedServerPlayer(GameTestHelper helper) {
        return new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "qas_test_player")
        );
    }

    private static ModPacketContext immediateContext(ServerPlayer player) {
        return new ModPacketContext() {
            @Override
            public ServerPlayer sender() {
                return player;
            }

            @Override
            public void enqueueWork(Runnable work) {
                work.run();
            }
        };
    }

    private static QuestDefinition quest(
            String id,
            QuestSettings settings,
            Map<String, QuestTaskDefinition> tasks,
            Map<String, QuestRewardDefinition> rewards,
            Set<String> prerequisites
    ) {
        return new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                id,
                new QuestDisplay(
                        id,
                        "",
                        List.of(),
                        Map.of("Main", GroupDef.DEFAULT),
                        "minecraft:book",
                        "minecraft:barrier"
                ),
                settings,
                prerequisites,
                tasks,
                rewards
        );
    }

    private static QuestTaskDefinition task(String id, String type, int goal, String target, Map<String, String> args) {
        return QuestGameTestDefinitions.task(id, type, goal, target, args);
    }

    private static QuestRewardDefinition reward(String id, String type, int amount, String payload, boolean selectable, Map<String, String> args) {
        return QuestGameTestDefinitions.reward(id, type, amount, payload, selectable, args);
    }

    private static int countItems(ServerPlayer player, String itemId) {
        ResourceLocation target = ResourceLocation.tryParse(itemId);
        int count = 0;
        for (var stack : player.getInventory().items) {
            if (target != null && target.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private record Bundle(
            QuestDefinitionStore store,
            QuestProgressSavedData progressData,
            RuntimeEngine engine
    ) {
        private void close() {
            store.shutdown();
        }
    }

    private static final class TestTeamProvider implements TeamProgressProvider {
        private final ResourceLocation id = ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "gametest_provider");
        private List<UUID> members = List.of();
        private BiConsumer<net.minecraft.server.level.ServerLevel, UUID> callback;

        @Override
        public ResourceLocation id() {
            return id;
        }

        @Override
        public java.util.Collection<UUID> members(net.minecraft.server.level.ServerLevel level, UUID playerId) {
            if (members.isEmpty()) {
                return List.of(playerId);
            }
            return members;
        }

        @Override
        public void installChangeHook(BiConsumer<net.minecraft.server.level.ServerLevel, UUID> callback) {
            this.callback = callback;
        }

        private void setMembers(List<UUID> members) {
            this.members = members;
        }

        private void fire(net.minecraft.server.level.ServerLevel level, UUID changedPlayer) {
            if (callback != null) {
                callback.accept(level, changedPlayer);
            }
        }
    }
}
