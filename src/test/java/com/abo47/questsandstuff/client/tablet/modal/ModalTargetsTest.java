package com.abo47.questsandstuff.client.tablet.modal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModalTargetsTest {
    @Test
    void buildersTrimPartsAndKeepEmptySlots() {
        assertEquals("task_item|quest|task|item", ModalTargets.taskItem(" quest ", " task ", " item "));
        assertEquals("kind|a||c", ModalTargets.of(" kind ", " a ", null, " c "));
    }

    @Test
    void descriptionAndConnectionTargetsUseStableKinds() {
        assertEquals("desc_image_new|quest|image|12|34", ModalTargets.descImageNew("quest", "image", 12, 34));
        assertEquals("connection_selection|chapter", ModalTargets.connectionSelection(" chapter "));
    }
}
