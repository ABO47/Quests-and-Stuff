package com.abo47.questsandstuff.client.tablet.modal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModalTargetsTest {
    @Test
    void buildersTrimPartsAndKeepEmptySlots() {
        assertEquals("task_item|quest|task|item", ModalTargets.taskItem(" quest ", " task ", " item "));
        assertEquals("task_advancement|quest|task|advancement", ModalTargets.taskAdvancement(" quest ", " task ", " advancement "));
        assertEquals("task_recipe|quest|task|recipe", ModalTargets.taskRecipe(" quest ", " task ", " recipe "));
        assertEquals("task_structure|quest|task|structure", ModalTargets.taskStructure(" quest ", " task ", " structure "));
        assertEquals("task_block|quest|task|block_interact", ModalTargets.taskBlock(" quest ", " task ", " block_interact "));
        assertEquals("task_stat|quest|task|stat", ModalTargets.taskStat(" quest ", " task ", " stat "));
        assertEquals("kind|a||c", ModalTargets.of(" kind ", " a ", null, " c "));
    }

    @Test
    void descriptionAndConnectionTargetsUseStableKinds() {
        assertEquals("desc_image_new|quest|image|12|34", ModalTargets.descImageNew("quest", "image", 12, 34));
        assertEquals("connection_selection|chapter", ModalTargets.connectionSelection(" chapter "));
    }
}
