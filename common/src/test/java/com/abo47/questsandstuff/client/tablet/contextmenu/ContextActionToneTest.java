package com.abo47.questsandstuff.client.tablet.contextmenu;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContextActionToneTest {
    @Test
    void factoryActionsCarryExplicitTones() {
        assertTone(ContextActionFactory.add("Add", () -> {
        }), ActionTone.SUCCESS, TabletColors.SUCCESS);
        assertTone(ContextActionFactory.copy(() -> {
        }), ActionTone.PRIMARY, TabletColors.INTERACTIVE);
        assertTone(ContextActionFactory.rename("Rename", () -> {
        }), ActionTone.PRIMARY, TabletColors.INTERACTIVE);
    }

    @Test
    void destructiveFactoriesCarryDestructiveTones() {
        TabletUiState state = new TabletUiState();

        ContextAction delete = ContextActionFactory.delete(state, "quest", "Delete quest", () -> {
        });
        ContextAction warningDelete = ContextActionFactory.warningDelete(state, "quest", "Remove background", () -> {
        });

        assertTone(delete, ActionTone.DANGER, TabletColors.ERROR);
        assertTone(warningDelete, ActionTone.WARNING, TabletColors.WARNING);
    }

    @Test
    void legacyColorsMapToTonesWithoutLabelOrIconInference() {
        ContextAction neutralDeleteText = ContextActionFactory.action("Delete translated", "delete", TabletColors.TEXT_MUTED, () -> {
        });
        ContextAction warningColor = ContextActionFactory.action("Plain label", "plain", TabletColors.WARNING, () -> {
        });

        assertTone(neutralDeleteText, ActionTone.NEUTRAL, TabletColors.TEXT_MUTED);
        assertTone(warningColor, ActionTone.WARNING, TabletColors.WARNING);
    }

    @Test
    void explicitToneWinsOverTextAndIcon() {
        ContextAction successDeleteIcon = new ContextAction("Delete translated", "delete", ActionTone.SUCCESS, () -> {
        });

        assertTone(successDeleteIcon, ActionTone.SUCCESS, TabletColors.SUCCESS);
    }

    private static void assertTone(ContextAction action, ActionTone tone, int accentColor) {
        assertEquals(tone, action.tone());
        assertEquals(accentColor, action.accentColor());
    }
}
