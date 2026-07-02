package com.abo47.questsandstuff.client.tablet.contextmenu;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContextActionToneTest {
    @Test
    void factoryActionsCarryExplicitTones() {
        assertTone(ContextActionFactory.add("Add", () -> {
        }), ActionTone.SUCCESS, ModColors.SUCCESS);
        assertTone(ContextActionFactory.copy(() -> {
        }), ActionTone.PRIMARY, ModColors.INTERACTIVE);
        assertTone(ContextActionFactory.rename("Rename", () -> {
        }), ActionTone.PRIMARY, ModColors.INTERACTIVE);
    }

    @Test
    void destructiveFactoriesCarryDestructiveTones() {
        TabletUiState state = new TabletUiState();

        ContextAction delete = ContextActionFactory.delete(state, "quest", "Delete quest", () -> {
        });
        ContextAction warningDelete = ContextActionFactory.warningDelete(state, "quest", "Remove background", () -> {
        });

        assertTone(delete, ActionTone.DANGER, ModColors.ERROR);
        assertTone(warningDelete, ActionTone.WARNING, ModColors.WARNING);
    }

    @Test
    void legacyColorsMapToTonesWithoutLabelOrIconInference() {
        ContextAction neutralDeleteText = ContextActionFactory.action("Delete translated", "delete", ModColors.TEXT_MUTED, () -> {
        });
        ContextAction warningColor = ContextActionFactory.action("Plain label", "plain", ModColors.WARNING, () -> {
        });

        assertTone(neutralDeleteText, ActionTone.NEUTRAL, ModColors.TEXT_MUTED);
        assertTone(warningColor, ActionTone.WARNING, ModColors.WARNING);
    }

    @Test
    void explicitToneWinsOverTextAndIcon() {
        ContextAction successDeleteIcon = new ContextAction("Delete translated", "delete", ActionTone.SUCCESS, () -> {
        });

        assertTone(successDeleteIcon, ActionTone.SUCCESS, ModColors.SUCCESS);
    }

    private static void assertTone(ContextAction action, ActionTone tone, int accentColor) {
        assertEquals(tone, action.tone());
        assertEquals(accentColor, action.accentColor());
    }
}
