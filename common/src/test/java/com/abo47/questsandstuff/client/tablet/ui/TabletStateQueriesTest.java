package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.util.QuestIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabletStateQueriesTest {
    @Test
    void rootDimensionsUseLayoutDefaultsUntilStateCarriesARealSize() {
        TabletUiState state = new TabletUiState();

        assertEquals(TabletLayout.ROOT_W, TabletStateQueries.rootWidth(state));
        assertEquals(TabletLayout.ROOT_H, TabletStateQueries.rootHeight(state));

        state.tabletRootWidth = 640;
        state.tabletRootHeight = 360;

        assertEquals(640, TabletStateQueries.rootWidth(state));
        assertEquals(360, TabletStateQueries.rootHeight(state));
    }

    @Test
    void selectedGroupNameUsesSanitizedEditableSelection() {
        TabletUiState state = new TabletUiState();
        state.canEdit = true;
        state.selectedGroup = "  main\nchapter with a very very very very very very long suffix  ";

        assertEquals(QuestIdentity.uiGroupName(state.selectedGroup), TabletStateQueries.sanitizeGroupName(state.selectedGroup));
        assertEquals("main chapter with a very very very very ", TabletStateQueries.selectedGroupName(state));
    }

    @Test
    void selectedQuestHelpersExposeSingleAndSnapshotSelection() {
        TabletUiState state = new TabletUiState();

        assertEquals("", TabletStateQueries.singleSelectedQuestId(state));
        assertFalse(TabletStateQueries.hasSelectedQuests(state));

        state.canvasSelection.questIds().add("quest_a");
        assertEquals("quest_a", TabletStateQueries.singleSelectedQuestId(state));
        assertTrue(TabletStateQueries.hasSelectedQuests(state));

        List<String> snapshot = TabletStateQueries.selectedQuestIdSnapshot(state);
        state.canvasSelection.questIds().add("quest_b");

        assertEquals(List.of("quest_a"), snapshot);
        assertEquals("", TabletStateQueries.singleSelectedQuestId(state));
    }
}
