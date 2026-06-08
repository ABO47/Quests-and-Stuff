package com.abo47.questsandstuff.client.tablet.context;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContextActionToneTest {
    @Test
    void factoryActionsCarryExplicitTones() {
        assertTone(ContextActions.add("Add", () -> {
        }), ActionTone.SUCCESS, ModColors.SUCCESS);
        assertTone(ContextActions.copy(() -> {
        }), ActionTone.PRIMARY, ModColors.INTERACTIVE);
        assertTone(ContextActions.rename("Rename", () -> {
        }), ActionTone.PRIMARY, ModColors.INTERACTIVE);
    }

    @Test
    void destructiveFactoriesCarryDestructiveTones() {
        TabletUiState state = new TabletUiState();

        ContextAction delete = ContextActions.delete(state, "quest", "Delete quest", () -> {
        });
        ContextAction warningDelete = ContextActions.warningDelete(state, "quest", "Remove background", () -> {
        });

        assertTone(delete, ActionTone.DANGER, ModColors.ERROR);
        assertTone(warningDelete, ActionTone.WARNING, ModColors.WARNING);
    }

    @Test
    void legacyColorsMapToTonesWithoutLabelOrIconInference() {
        ContextAction neutralDeleteText = ContextActions.action("Delete translated", "delete", ModColors.TEXT_MUTED, () -> {
        });
        ContextAction warningColor = ContextActions.action("Plain label", "plain", ModColors.WARNING, () -> {
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
