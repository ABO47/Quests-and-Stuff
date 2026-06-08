package com.abo47.questsandstuff.client.tablet;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TabletPureHelperCoverageTest {
    @Test
    void fastCommonCoverageSuitesStayPresentForTabletRefactors() {
        Map<String, String> coverage = Map.of(
                "modal sessions", "com.abo47.questsandstuff.client.tablet.ui.TabletModalStateTest",
                "picker sessions", "com.abo47.questsandstuff.client.tablet.modal.ModalPickerStatesTest",
                "transform sessions", "com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformSessionsTest",
                "sync keys and chunks", "com.abo47.questsandstuff.quest.sync.QuestSyncPayloadBuilderTest",
                "client sync inbox", "com.abo47.questsandstuff.client.sync.packet.ClientSyncInboxTest",
                "optimistic mutations", "com.abo47.questsandstuff.client.sync.mutation.ClientOptimisticMutationsTest",
                "text field builders", "com.abo47.questsandstuff.client.tablet.controls.StyledTextFieldsTest",
                "selector builders", "com.abo47.questsandstuff.client.tablet.controls.TabletSelectorTest",
                "quest details picker sessions", "com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsPickerSessionTest",
                "objective defaults", "com.abo47.questsandstuff.client.tablet.quest.details.objective.QuestObjectiveTypeCatalogTest"
        );

        coverage.forEach((area, className) ->
                assertDoesNotThrow(() -> Class.forName(className), area + " coverage class is missing"));
    }
}
