package com.abo47.questsandstuff.client.tablet.modal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModalTargetParserTest {
    @Test
    void parsesTypedTaskIconTarget() {
        ModalTargetParser.Target target = ModalTargetParser.parse(ModalTargets.taskIcon("quest", "task"));

        assertTrue(target.isTaskIcon());
        assertEquals("quest", target.questId());
        assertEquals("task", target.entryId());
        assertEquals("icon", target.type());
    }

    @Test
    void parsesAdvancementTaskTarget() {
        ModalTargetParser.Target target = ModalTargetParser.parse(ModalTargets.taskAdvancement("quest", "task", "questsandstuff:advancement"));

        assertTrue(target.isTaskAdvancement());
        assertEquals("quest", target.questId());
        assertEquals("task", target.entryId());
        assertEquals("questsandstuff:advancement", target.type());
    }

    @Test
    void parsesRecipeTaskTarget() {
        ModalTargetParser.Target target = ModalTargetParser.parse(ModalTargets.taskRecipe("quest", "task", "questsandstuff:recipe"));

        assertTrue(target.isTaskRecipe());
        assertEquals("quest", target.questId());
        assertEquals("task", target.entryId());
        assertEquals("questsandstuff:recipe", target.type());
    }

    @Test
    void parsesStructureTaskTarget() {
        ModalTargetParser.Target target = ModalTargetParser.parse(ModalTargets.taskStructure("quest", "task", "questsandstuff:structure"));

        assertTrue(target.isTaskStructure());
        assertEquals("quest", target.questId());
        assertEquals("task", target.entryId());
        assertEquals("questsandstuff:structure", target.type());
    }

    @Test
    void parsesBlockTaskTarget() {
        ModalTargetParser.Target target = ModalTargetParser.parse(ModalTargets.taskBlock("quest", "task", "questsandstuff:block_interact"));

        assertTrue(target.isTaskBlock());
        assertEquals("quest", target.questId());
        assertEquals("task", target.entryId());
        assertEquals("questsandstuff:block_interact", target.type());
    }

    @Test
    void parsesStatTaskTarget() {
        ModalTargetParser.Target target = ModalTargetParser.parse(ModalTargets.taskStat("quest", "task", "questsandstuff:stat"));

        assertTrue(target.isTaskStat());
        assertEquals("quest", target.questId());
        assertEquals("task", target.entryId());
        assertEquals("questsandstuff:stat", target.type());
    }

    @Test
    void targetPartsAreDefensivelyCopied() {
        ModalTargetParser.Target target = ModalTargetParser.parse("reward_icon|quest|reward|icon");
        String[] parts = target.parts();

        parts[0] = "changed";

        assertArrayEquals(new String[]{"reward_icon", "quest", "reward", "icon"}, target.parts());
        assertTrue(target.isRewardIcon());
    }

    @Test
    void nullTargetParsesToSafeEmptyKind() {
        ModalTargetParser.Target target = ModalTargetParser.parse(null);

        assertEquals("", target.kind());
        assertEquals("", target.part(3));
        assertTrue(target.hasAtLeast(1));
        assertFalse(target.isTaskIcon());
    }

    @Test
    void entityPickerPredicatesCoverTypedTargets() {
        assertTrue(ModalTargetParser.parse(ModalTargets.descEntityNew("quest", "image", 4, 5)).isEntityIconPickerTarget());
        assertTrue(ModalTargetParser.parse(ModalTargets.rewardIcon("quest", "reward")).supportsEntityIconSelection());
        assertFalse(ModalTargetParser.parse(ModalTargets.descImage("quest", "image")).isEntityIconPickerTarget());
    }

    @Test
    void inventoryIconPredicateCoversIconTargetsOnly() {
        assertTrue(ModalTargetParser.parse(ModalTargets.questIcon("quest")).supportsInventoryIconSelection());
        assertTrue(ModalTargetParser.parse(ModalTargets.chapterIcon("chapter")).supportsInventoryIconSelection());
        assertTrue(ModalTargetParser.parse(ModalTargets.taskIcon("quest", "task")).supportsInventoryIconSelection());
        assertTrue(ModalTargetParser.parse(ModalTargets.rewardIcon("quest", "reward")).supportsInventoryIconSelection());
        assertFalse(ModalTargetParser.parse(ModalTargets.taskItem("quest", "task", "questsandstuff:item")).supportsInventoryIconSelection());
    }
}
