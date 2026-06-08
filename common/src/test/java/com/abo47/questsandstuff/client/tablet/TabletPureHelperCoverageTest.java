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
                entry("recipe choice index", "com.abo47.questsandstuff.client.tablet.modal.RecipeChoiceIndexTest"),
                entry("recipe picker mode controller", "com.abo47.questsandstuff.client.tablet.modal.RecipePickerModeControllerTest"),
                entry("recipe viewer capabilities", "com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerCapabilityMatrixTest"),
                entry("recipe viewer selection rules", "com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerSelectionRulesTest"),
                entry("prerequisite connection model", "com.abo47.questsandstuff.client.tablet.quest.prerequisite.PrerequisiteConnectionModelTest"),
                entry("shared smart snap engine", "com.abo47.questsandstuff.client.tablet.quest.canvas.snap.CanvasSnapEngineTest"),
                entry("main canvas smart snap adapter", "com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasSmartSnapperTest"),
                entry("quest card background defaults", "com.abo47.questsandstuff.client.tablet.quest.canvas.render.QuestCardBackgroundRendererTest"),
                entry("mini quest card renderer", "com.abo47.questsandstuff.client.tablet.quest.canvas.render.QuestMiniCardRendererTest"),
                entry("context action tones", "com.abo47.questsandstuff.client.tablet.context.ContextActionToneTest"),
                entry("context action layout", "com.abo47.questsandstuff.client.tablet.context.ContextActionLayoutTest"),
                entry("objective defaults", "com.abo47.questsandstuff.client.tablet.quest.details.objective.QuestObjectiveTypeCatalogTest")
        );

        coverage.forEach((area, className) ->
                assertDoesNotThrow(() -> Class.forName(className), area + " coverage class is missing"));
    }
}
