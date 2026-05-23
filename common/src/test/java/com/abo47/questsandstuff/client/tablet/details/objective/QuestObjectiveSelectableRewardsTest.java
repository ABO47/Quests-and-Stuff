package com.abo47.questsandstuff.client.tablet.details.objective;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestObjectiveSelectableRewardsTest {
    @Test
    void standaloneSelectableChoiceDoesNotUseEditSelection() {
        TabletUiState state = new TabletUiState();

        QuestObjectiveSelectableRewards.selectChoice(state, "reward_item_0002");

        assertEquals("", state.questDetailsSelectedObjectiveKind);
        assertEquals("", state.questDetailsSelectedObjectiveId);
        assertTrue(QuestObjectiveSelectableRewards.isSelectedChoice(state, "reward_item_0002"));
    }

    @Test
    void oneStandaloneSelectableRewardChoiceCompletesStandaloneSelection() {
        TabletUiState state = new TabletUiState();
        CompoundTag quest = questWithRewards(
                reward("reward_item_0001", true),
                reward("reward_item_0002", true),
                reward("reward_item_0003", true)
        );

        QuestObjectiveSelectableRewards.selectChoice(state, "reward_item_0002");

        assertTrue(QuestObjectiveSelectableRewards.allSelectableRewardsSelected(quest, state));
    }

    @Test
    void standaloneSelectableRewardsActAsOneChoiceGroup() {
        TabletUiState state = new TabletUiState();

        QuestObjectiveSelectableRewards.selectChoice(state, "reward_item_0001");
        QuestObjectiveSelectableRewards.selectChoice(state, "reward_item_0002");

        assertTrue(QuestObjectiveSelectableRewards.isSelectedChoice(state, "reward_item_0002"));
        assertFalse(QuestObjectiveSelectableRewards.isSelectedChoice(state, "reward_item_0001"));
    }

    @Test
    void wrapperSelectableRewardChoiceUsesGroupChoiceState() {
        TabletUiState state = new TabletUiState();
        CompoundTag quest = questWithRewards(wrapperReward("reward_selectable_0001"));

        QuestObjectiveSelectableRewards.selectChoice(state, "reward_selectable_0001\u001Fchoice_a");

        assertEquals("", state.questDetailsSelectedObjectiveKind);
        assertEquals("", state.questDetailsSelectedObjectiveId);
        assertTrue(QuestObjectiveSelectableRewards.isSelectedChoice(state, "reward_selectable_0001\u001Fchoice_a"));
        assertTrue(QuestObjectiveSelectableRewards.allSelectableRewardsSelected(quest, state));
    }

    @Test
    void claimChoiceEntriesIncludeStandaloneAndDisplayedWrapperChoicesOnly() {
        QuestDetailsObjectiveEntry standalone = entry("reward_item_0001", reward("reward_item_0001", true));
        QuestDetailsObjectiveEntry wrapper = entry("reward_selectable_0001", wrapperReward("reward_selectable_0001"));
        QuestDetailsObjectiveEntry wrapperChoice = entry("reward_selectable_0001\u001Fchoice_a", reward("choice_a", false));
        QuestDetailsObjectiveEntry normal = entry("reward_item_0002", reward("reward_item_0002", false));

        assertTrue(QuestObjectiveSelectableRewards.isClaimChoiceEntry(standalone));
        assertTrue(QuestObjectiveSelectableRewards.isClaimChoiceEntry(wrapperChoice));
        assertFalse(QuestObjectiveSelectableRewards.isClaimChoiceEntry(wrapper));
        assertFalse(QuestObjectiveSelectableRewards.isClaimChoiceEntry(normal));
    }

    @Test
    void clearingEditSelectionKeepsSelectableRewardChoice() {
        TabletUiState state = new TabletUiState();
        QuestObjectiveSelectableRewards.selectChoice(state, "reward_item_0002");
        QuestObjectiveListInteractions.select(state, "rewards", "reward_item_0002");

        assertTrue(QuestObjectiveListInteractions.clearSelection(state, "outside_card_click"));

        assertEquals("", state.questDetailsSelectedObjectiveKind);
        assertEquals("", state.questDetailsSelectedObjectiveId);
        assertTrue(QuestObjectiveSelectableRewards.isSelectedChoice(state, "reward_item_0002"));
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

    private static QuestDetailsObjectiveEntry entry(String id, CompoundTag tag) {
        return new QuestDetailsObjectiveEntry(id, tag, QuestObjectiveJsons.read(tag.getString("json")));
    }
}
