package com.abo47.questsandstuff.client.tablet.context;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ContextActionLayoutTest {
    @Test
    void promotedActionsUseToneForDestructiveOrdering() {
        ContextAction destructive = action("Translated destructive action", "plain", ActionTone.DANGER, true);
        ContextAction primary = action("Primary", "rename", ActionTone.PRIMARY, true);

        List<ContextAction> promoted = ContextActionLayout.promotedActions(List.of(destructive, primary));

        assertEquals(2, promoted.size());
        assertSame(primary, promoted.get(0));
        assertSame(destructive, promoted.get(1));
    }

    @Test
    void rowActionsKeepSinglePromotedActionUntilPromotedBarIsUseful() {
        ContextAction promoted = action("Promoted", "copy", ActionTone.PRIMARY, true);
        ContextAction row = action("Row", "style", ActionTone.NEUTRAL, false);

        List<ContextAction> rows = ContextActionLayout.rowActions(List.of(promoted, row));

        assertEquals(2, rows.size());
        assertSame(promoted, rows.get(0));
        assertSame(row, rows.get(1));
    }

    @Test
    void rowActionsExcludePromotedActionsWhenPromotedBarIsVisible() {
        ContextAction promotedOne = action("Promoted one", "copy", ActionTone.PRIMARY, true);
        ContextAction promotedTwo = action("Promoted two", "rename", ActionTone.PRIMARY, true);
        ContextAction row = action("Row", "style", ActionTone.NEUTRAL, false);

        List<ContextAction> rows = ContextActionLayout.rowActions(List.of(promotedOne, row, promotedTwo));

        assertEquals(1, rows.size());
        assertSame(row, rows.get(0));
    }

    @Test
    void visiblePromotedActionsPreserveDestructiveActionWhenSpaceIsLimited() {
        ContextAction first = action("First", "copy", ActionTone.PRIMARY, true);
        ContextAction second = action("Second", "rename", ActionTone.PRIMARY, true);
        ContextAction destructive = action("Translated destructive action", "plain", ActionTone.DANGER, true);
        int twoButtonWidth = ContextActionLayout.OUTER_PAD * 2 + ContextActionLayout.PROMOTED_BUTTON * 2;

        List<ContextAction> visible = ContextActionLayout.visiblePromotedActions(List.of(first, second, destructive), twoButtonWidth);

        assertEquals(2, visible.size());
        assertSame(first, visible.get(0));
        assertSame(destructive, visible.get(1));
    }

    private static ContextAction action(String label, String icon, ActionTone tone, boolean promoted) {
        return new ContextAction(label, icon, tone, true, promoted, () -> {
        });
    }
}
