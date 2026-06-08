package com.abo47.questsandstuff.client.tablet.controls;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TabletSelectorTest {
    @Test
    void exposesCandidateLabelsInOrder() {
        List<TabletSelector.Option<Integer>> options = List.of(
                TabletSelector.option(1, "Themes"),
                TabletSelector.option(2, "Canvas"),
                TabletSelector.option(3, "HUD")
        );

        assertEquals(List.of("Themes", "Canvas", "HUD"), TabletSelector.candidateLabels(options));
    }

    @Test
    void selectedLabelFallsBackToFirstOption() {
        List<TabletSelector.Option<Integer>> options = List.of(
                TabletSelector.option(1, "Themes"),
                TabletSelector.option(2, "Canvas")
        );

        assertEquals("Canvas", TabletSelector.selectedLabel(options, 2));
        assertEquals("Themes", TabletSelector.selectedLabel(options, 99));
        assertEquals("", TabletSelector.selectedLabel(List.of(), 99));
    }

    @Test
    void mapsChangedLabelBackToOptionValue() {
        List<TabletSelector.Option<Integer>> options = List.of(
                TabletSelector.option(1, "Themes"),
                TabletSelector.option(2, "Canvas")
        );

        assertEquals(2, TabletSelector.optionForLabel(options, "Canvas").value());
        assertNull(TabletSelector.optionForLabel(options, "Unknown"));
    }
}
