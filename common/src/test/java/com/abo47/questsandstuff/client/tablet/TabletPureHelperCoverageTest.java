package com.abo47.questsandstuff.client.tablet;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TabletPureHelperCoverageTest {
    @Test
    void fastCommonCoverageSuitesStayPresentForTabletRefactors() {
        Map<String, String> coverage = Map.ofEntries(
                entry("modal sessions", "com.abo47.questsandstuff.client.tablet.ui.TabletModalStateTest"),
                entry("picker sessions", "com.abo47.questsandstuff.client.tablet.modal.ModalPickerStatesTest"),
                entry("settings descriptors", "com.abo47.questsandstuff.client.tablet.modal.SettingsTabDescriptorsTest"),
                entry("transform sessions", "com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformSessionsTest"),
                entry("sync keys and chunks", "com.abo47.questsandstuff.quest.sync.QuestSyncPayloadBuilderTest"),
                entry("client sync inbox", "com.abo47.questsandstuff.client.sync.packet.ClientSyncInboxTest"),
                entry("optimistic mutations", "com.abo47.questsandstuff.client.sync.mutation.ClientOptimisticMutationsTest"),
                entry("text field builders", "com.abo47.questsandstuff.client.tablet.controls.StyledTextFieldsTest"),
                entry("selector builders", "com.abo47.questsandstuff.client.tablet.controls.TabletSelectorTest"),
                entry("quest details picker sessions", "com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsPickerSessionTest"),
                entry("recipe viewer capabilities", "com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerCapabilityMatrixTest"),
                entry("recipe viewer selection rules", "com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerSelectionRulesTest"),
                entry("shared smart snap engine", "com.abo47.questsandstuff.client.tablet.quest.canvas.snap.CanvasSnapEngineTest"),
                entry("main canvas smart snap adapter", "com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasSmartSnapperTest"),
                entry("quest card background defaults", "com.abo47.questsandstuff.client.tablet.quest.canvas.render.QuestCardBackgroundRendererTest"),
                entry("mini quest card renderer", "com.abo47.questsandstuff.client.tablet.quest.canvas.render.QuestMiniCardRendererTest"),
                entry("objective defaults", "com.abo47.questsandstuff.client.tablet.quest.details.objective.QuestObjectiveTypeCatalogTest")
        );

        coverage.forEach((area, className) ->
                assertDoesNotThrow(() -> Class.forName(className), area + " coverage class is missing"));
    }
}
