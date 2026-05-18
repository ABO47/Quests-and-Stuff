package com.abo47.questsandstuff.client.tablet.entity.motion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EntityMotionTargetsTest {

    @Test
    void parseDraftFallsBackForBlankOrInvalidInput() {
        assertEquals(12, EntityMotionTargets.parseDraft("", 12, 100));
        assertEquals(12, EntityMotionTargets.parseDraft("nope", 12, 100));
    }

    @Test
    void parseDraftClampsToRange() {
        assertEquals(0, EntityMotionTargets.parseDraft("-50", 12, 100));
        assertEquals(100, EntityMotionTargets.parseDraft("500", 12, 100));
    }

    @Test
    void parseDraftAcceptsValidNumber() {
        assertEquals(42, EntityMotionTargets.parseDraft("42", 12, 100));
    }
}
