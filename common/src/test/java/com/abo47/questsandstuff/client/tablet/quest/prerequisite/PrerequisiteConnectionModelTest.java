package com.abo47.questsandstuff.client.tablet.quest.prerequisite;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;

import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrerequisiteConnectionModelTest {
    @BeforeEach
    void resetClientState() {
        ClientQuestStateFacade.resetStateForTests();
        ClientQuestStateFacade.createEditorQuestLocal("quest/parent", "main", 10, 20, "Parent");
        ClientQuestStateFacade.createEditorQuestLocal("quest/focus", "main", 30, 40, "Focus");
        ClientQuestStateFacade.createEditorQuestLocal("quest/child", "main", 50, 60, "Child");
        ClientQuestStateFacade.createEditorQuestLocal("quest/external", "other", 70, 80, "External");
        ClientQuestStateFacade.createEditorQuestLocal("quest/external_child", "other", 90, 100, "External Child");
        ClientQuestStateFacade.setQuestPrerequisiteLocal("quest/focus", "quest/parent", true);
        ClientQuestStateFacade.setQuestPrerequisiteLocal("quest/child", "quest/focus", true);
        ClientQuestStateFacade.setQuestPrerequisiteLocal("quest/focus", "quest/external", true);
        ClientQuestStateFacade.setQuestPrerequisiteLocal("quest/external_child", "quest/focus", true);
    }

    @Test
    void modelBuildsIncomingAndOutgoingRows() {
        PrerequisiteConnectionModel model = model(false, "");

        assertEquals(4, model.allRows().size());
        assertIterableEquals(
                java.util.List.of("quest/parent->quest/focus", "quest/focus->quest/child"),
                model.rows().stream().map(PrerequisiteConnectionRow::key).toList()
        );
        assertEquals(PrerequisiteConnectionKind.INCOMING, model.rows().get(0).kind());
        assertEquals(PrerequisiteConnectionKind.OUTGOING, model.rows().get(1).kind());
        assertEquals("Focus", model.targetTitle());
    }

    @Test
    void externalModeShowsOnlyCrossGroupConnections() {
        PrerequisiteConnectionModel model = model(true, "");

        assertIterableEquals(
                java.util.List.of("quest/external->quest/focus", "quest/focus->quest/external_child"),
                model.rows().stream().map(PrerequisiteConnectionRow::key).toList()
        );
    }

    @Test
    void searchFiltersWithinTheActiveModeOnly() {
        PrerequisiteConnectionModel local = model(false, "child");
        PrerequisiteConnectionModel external = model(true, "external");

        assertIterableEquals(
                java.util.List.of("quest/focus->quest/child"),
                local.rows().stream().map(PrerequisiteConnectionRow::key).toList()
        );
        assertIterableEquals(
                java.util.List.of("quest/external->quest/focus", "quest/focus->quest/external_child"),
                external.rows().stream().map(PrerequisiteConnectionRow::key).toList()
        );
    }

    @Test
    void selectedRowUsesTheFullSnapshot() {
        PrerequisiteConnectionModel local = model(false, "");

        assertEquals("quest/focus->quest/external_child", local.selectedRow("quest/focus->quest/external_child").key());
        assertNull(local.selectedRow(""));
        assertNull(local.selectedRow("missing"));
    }

    @Test
    void highlightsPreferHoverAndMapBackToQuestIds() {
        PrerequisiteConnectionModel local = model(false, "");

        Set<String> highlighted = PrerequisiteConnectionModel.highlightedConnections(
                "quest/focus->quest/child",
                "quest/parent->quest/focus"
        );

        assertEquals(Set.of("quest/focus->quest/child"), highlighted);
        assertEquals(Set.of("quest/focus", "quest/child"), local.highlightedQuests(highlighted));
    }

    @Test
    void previewUsesActualPositionsForLocalConnections() {
        CanvasBlueprint blueprint = PrerequisitePreviewBuilder.build("main", model(false, ""), false);
        Map<String, CanvasBlueprint.QuestEntry> entries = entriesById(blueprint);

        assertEquals(3, entries.size());
        assertEquals(10, entries.get("quest/parent").sourceX());
        assertEquals(30, entries.get("quest/focus").sourceX());
        assertEquals(50, entries.get("quest/child").sourceX());
        assertIterableEquals(
                java.util.List.of(
                        CanvasLayerOrdering.questKey("quest/parent"),
                        CanvasLayerOrdering.questKey("quest/child"),
                        CanvasLayerOrdering.questKey("quest/focus")
                ),
                blueprint.layerOrder()
        );
    }

    @Test
    void previewUsesCompactColumnsForExternalConnections() {
        CanvasBlueprint blueprint = PrerequisitePreviewBuilder.build("main", model(true, ""), true);
        Map<String, CanvasBlueprint.QuestEntry> entries = entriesById(blueprint);

        assertEquals(3, entries.size());
        assertEquals(-128, entries.get("quest/external").sourceX());
        assertEquals(0, entries.get("quest/focus").sourceX());
        assertEquals(128, entries.get("quest/external_child").sourceX());
    }

    @Test
    void removeCleanupClearsOnlyMatchingSelectionState() {
        TabletUiState state = new TabletUiState();
        PrerequisiteConnectionRow row = new PrerequisiteConnectionRow(
                "quest/parent",
                "quest/focus",
                "Parent",
                "Focus",
                "Parent",
                "",
                PrerequisiteConnectionKind.INCOMING,
                false
        );
        state.modal.prerequisitesManagerSelectedConnectionKey = row.key();
        state.modal.prerequisitesManagerHoveredConnectionKey = row.key();
        state.modal.prerequisitesManagerContextOpen = true;
        state.modal.prerequisitesManagerContextPrerequisiteId = row.sourceId();
        ContextMenuController.confirmDeleteClick(state, "connection:remove:" + row.key());

        assertTrue(PrerequisiteConnectionRemover.canRemove(row));
        assertFalse(PrerequisiteConnectionRemover.canRemove(new PrerequisiteConnectionRow("", "quest/focus", "", "", "", "", PrerequisiteConnectionKind.INCOMING, false)));

        PrerequisiteConnectionRemover.clearAfterRemove(state, row);

        assertEquals("", state.modal.prerequisitesManagerSelectedConnectionKey);
        assertEquals("", state.modal.prerequisitesManagerHoveredConnectionKey);
        assertFalse(state.modal.prerequisitesManagerContextOpen);
        assertEquals("", state.modal.prerequisitesManagerContextPrerequisiteId);
        assertEquals("", state.contextMenu.contextDeleteConfirmKey);
    }

    private static PrerequisiteConnectionModel model(boolean externalMode, String query) {
        return PrerequisiteConnectionModel.build(
                "quest/focus",
                ClientQuestStateFacade.quest("quest/focus"),
                "main",
                query,
                externalMode
        );
    }

    private static Map<String, CanvasBlueprint.QuestEntry> entriesById(CanvasBlueprint blueprint) {
        return blueprint.quests().stream()
                .collect(toMap(CanvasBlueprint.QuestEntry::sourceId, entry -> entry));
    }
}
