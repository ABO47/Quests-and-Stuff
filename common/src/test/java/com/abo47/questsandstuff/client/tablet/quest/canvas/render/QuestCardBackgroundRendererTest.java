package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static com.abo47.questsandstuff.client.tablet.theme.render.Surfaces.withAlpha;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestCardBackgroundRendererTest {
    @Test
    void normalizesDefaultBackgroundInOnePlace() {
        assertTrue(QuestCardBackgroundRenderer.usesDefaultBackground(null));
        assertTrue(QuestCardBackgroundRenderer.usesDefaultBackground(""));
        assertTrue(QuestCardBackgroundRenderer.usesDefaultBackground(QuestDisplay.DEFAULT_QUEST_BACKGROUND));
        assertFalse(QuestCardBackgroundRenderer.usesDefaultBackground("backgrounds/stone.png"));
    }

    @Test
    void defaultTintMatchesQuestState() {
        CompoundTag unlocked = tag();
        unlocked.putBoolean(QuestSyncKeys.Quest.UNLOCKED, true);
        assertEquals(withAlpha(ModColors.INTERACTIVE, 255), QuestCardBackgroundRenderer.defaultTint(unlocked, 255));

        CompoundTag completed = tag();
        completed.putBoolean(QuestSyncKeys.Quest.COMPLETED, true);
        assertEquals(withAlpha(ModColors.SUCCESS, 200), QuestCardBackgroundRenderer.defaultTint(completed, 200));

        CompoundTag claimed = tag();
        claimed.putBoolean(QuestSyncKeys.Quest.CLAIMED, true);
        assertEquals(withAlpha(ModColors.WARNING, 180), QuestCardBackgroundRenderer.defaultTint(claimed, 180));

        CompoundTag locked = tag();
        locked.putString(QuestSyncKeys.Quest.HIDDEN_MODE, "locked");
        assertEquals(withAlpha(ModColors.TEXT_SECONDARY, 255), QuestCardBackgroundRenderer.defaultTint(locked, 255));
    }

    @Test
    void statusFilterScalesForMiniatures() {
        CompoundTag claimed = tag();
        claimed.putBoolean(QuestSyncKeys.Quest.CLAIMED, true);

        assertEquals(withAlpha(ModColors.WARNING, 47), QuestCardBackgroundRenderer.statusFilter(claimed, 128));
    }

    @Test
    void progressRulesAreSharedByFullCardsAndPreviews() {
        CompoundTag tag = tag();
        tag.putBoolean(QuestSyncKeys.Quest.UNLOCKED, true);
        tag.putFloat(QuestSyncKeys.Quest.PROGRESS, 0.5f);

        assertEquals(0.5f, QuestCardBackgroundRenderer.questProgress(tag), 0.0001f);
        assertEquals(50, QuestCardBackgroundRenderer.progressPercent(tag));
        assertTrue(QuestCardBackgroundRenderer.shouldShowProgressFill(tag, 0.5f));
        assertEquals(8, QuestCardBackgroundRenderer.progressFillWidth(16, 0.5f));

        tag.putBoolean(QuestSyncKeys.Quest.COMPLETED, true);
        assertFalse(QuestCardBackgroundRenderer.shouldShowProgressFill(tag, 0.5f));
    }

    private static CompoundTag tag() {
        CompoundTag tag = new CompoundTag();
        tag.putString(QuestSyncKeys.Quest.QUEST_BACKGROUND, QuestDisplay.DEFAULT_QUEST_BACKGROUND);
        return tag;
    }
}
