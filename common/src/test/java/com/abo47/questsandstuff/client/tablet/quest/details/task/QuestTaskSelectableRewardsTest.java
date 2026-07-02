package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestTaskSelectableRewardsTest {
    @Test
    void standaloneSelectableChoiceDoesNotUseEditSelection() {
        TabletUiState state = new TabletUiState();

        QuestTaskSelectableRewards.selectChoice(state, "reward_item_0002");

        assertEquals("", state.questDetails.questDetailsSelectedTaskKind);
        assertEquals("", state.questDetails.questDetailsSelectedTaskId);
        assertTrue(QuestTaskSelectableRewards.isSelectedChoice(state, "reward_item_0002"));
    }

    @Test
    void oneStandaloneSelectableRewardChoiceCompletesStandaloneSelection() {
        TabletUiState state = new TabletUiState();
        CompoundTag quest = questWithRewards(
                reward("reward_item_0001", true),
                reward("reward_item_0002", true),
                reward("reward_item_0003", true)
        );

        QuestTaskSelectableRewards.selectChoice(state, "reward_item_0002");

        assertTrue(QuestTaskSelectableRewards.allSelectableRewardsSelected(quest, state));
    }

    @Test
    void standaloneSelectableRewardsActAsOneChoiceGroup() {
        TabletUiState state = new TabletUiState();

        QuestTaskSelectableRewards.selectChoice(state, "reward_item_0001");
        QuestTaskSelectableRewards.selectChoice(state, "reward_item_0002");

        assertTrue(QuestTaskSelectableRewards.isSelectedChoice(state, "reward_item_0002"));
        assertFalse(QuestTaskSelectableRewards.isSelectedChoice(state, "reward_item_0001"));
    }

    @Test
    void wrapperSelectableRewardChoiceUsesGroupChoiceState() {
        TabletUiState state = new TabletUiState();
        CompoundTag quest = questWithRewards(wrapperReward("reward_selectable_0001"));

        QuestTaskSelectableRewards.selectChoice(state, "reward_selectable_0001\u001Fchoice_a");

        assertEquals("", state.questDetails.questDetailsSelectedTaskKind);
        assertEquals("", state.questDetails.questDetailsSelectedTaskId);
        assertTrue(QuestTaskSelectableRewards.isSelectedChoice(state, "reward_selectable_0001\u001Fchoice_a"));
        assertTrue(QuestTaskSelectableRewards.allSelectableRewardsSelected(quest, state));
    }

    @Test
    void claimChoiceEntriesIncludeStandaloneAndDisplayedWrapperChoicesOnly() {
        QuestDetailsTaskEntry standalone = entry("reward_item_0001", reward("reward_item_0001", true));
        QuestDetailsTaskEntry wrapper = entry("reward_selectable_0001", wrapperReward("reward_selectable_0001"));
        QuestDetailsTaskEntry wrapperChoice = entry("reward_selectable_0001\u001Fchoice_a", reward("choice_a", false));
        QuestDetailsTaskEntry normal = entry("reward_item_0002", reward("reward_item_0002", false));

        assertTrue(QuestTaskSelectableRewards.isClaimChoiceEntry(standalone));
        assertTrue(QuestTaskSelectableRewards.isClaimChoiceEntry(wrapperChoice));
        assertFalse(QuestTaskSelectableRewards.isClaimChoiceEntry(wrapper));
        assertFalse(QuestTaskSelectableRewards.isClaimChoiceEntry(normal));
    }

    @Test
    void clearingEditSelectionKeepsSelectableRewardChoice() {
        TabletUiState state = new TabletUiState();
        QuestTaskSelectableRewards.selectChoice(state, "reward_item_0002");
        QuestTaskListInteractions.select(state, "rewards", "reward_item_0002");

        assertTrue(QuestTaskListInteractions.clearSelection(state, "outside_card_click"));

        assertEquals("", state.questDetails.questDetailsSelectedTaskKind);
        assertEquals("", state.questDetails.questDetailsSelectedTaskId);
        assertTrue(QuestTaskSelectableRewards.isSelectedChoice(state, "reward_item_0002"));
    }

    private static CompoundTag questWithRewards(CompoundTag... rewardTags) {
        CompoundTag quest = new CompoundTag();
        CompoundTag rewards = new CompoundTag();
        for (CompoundTag reward : rewardTags) {
            rewards.put(reward.getString("id"), reward);
        }
        quest.put("rewards", rewards);
        return quest;
    }

    private static CompoundTag reward(String id, boolean selectable) {
        String json = """
                {"id":"%s","type":"questsandstuff:item","item":"minecraft:diamond","amount":1,"nbt":"","selectable":%s}
                """.formatted(id, selectable);
        return rewardTag(id, json);
    }

    private static CompoundTag wrapperReward(String id) {
        String json = """
                {"id":"%s","type":"questsandstuff:selectable","amount":1,"rewards":{"choice_a":{"id":"choice_a","type":"questsandstuff:item","item":"minecraft:diamond","amount":1,"nbt":""}}}
                """.formatted(id);
        return rewardTag(id, json);
    }

    private static CompoundTag rewardTag(String id, String json) {
        CompoundTag reward = new CompoundTag();
        reward.putString("id", id);
        reward.putString("json", json);
        return reward;
    }

    private static QuestDetailsTaskEntry entry(String id, CompoundTag tag) {
        return new QuestDetailsTaskEntry(id, tag, TaskJsonFactory.read(tag.getString("json")));
    }
}
