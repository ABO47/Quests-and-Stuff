package com.abo47.questsandstuff.client.tablet.contextmenu;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextMenuSectionsTest {
    @Test
    void loneSectionSuppressesHeader() {
        ContextMenuSections sections = new ContextMenuSections();
        sections.add(ContextMenuSection.PRIMARY, row("Open"));
        sections.add(ContextMenuSection.PRIMARY, row("Connect"));

        List<ContextAction> built = sections.build();

        assertEquals(2, built.size());
        assertFalse(built.get(0).isSection());
    }

    @Test
    void multipleSectionsEmitHeadersInEnumOrder() {
        ContextMenuSections sections = new ContextMenuSections();
        sections.add(ContextMenuSection.DANGER, row("Remove"));
        sections.add(ContextMenuSection.PRIMARY, row("Open"));
        sections.add(ContextMenuSection.APPEARANCE, row("Texture"));

        List<ContextAction> built = sections.build();

        assertEquals(ContextMenuSection.PRIMARY, built.get(0).section());
        assertEquals(ContextMenuSection.APPEARANCE, built.get(2).section());
        assertEquals(ContextMenuSection.DANGER, built.get(4).section());
        assertEquals("Open", built.get(1).label());
        assertEquals("Texture", built.get(3).label());
        assertEquals("Remove", built.get(5).label());
    }

    @Test
    void emptySectionsAreSkippedButHeadersStillOrdered() {
        ContextMenuSections sections = new ContextMenuSections();
        sections.add(ContextMenuSection.PRIMARY, row("Open"));
        sections.add(ContextMenuSection.DANGER, row("Remove"));

        List<ContextAction> built = sections.build();

        assertEquals(ContextMenuSection.PRIMARY, built.get(0).section());
        assertEquals(ContextMenuSection.DANGER, built.get(2).section());
        assertEquals(4, built.size());
    }

    @Test
    void sectionActionsAreNeverPromoted() {
        ContextMenuSections sections = new ContextMenuSections();
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.promoted("Open", "open", ActionTone.PRIMARY, () -> {
        }));
        sections.add(ContextMenuSection.DANGER, row("Remove"));

        List<ContextAction> built = sections.build();
        assertEquals(3, built.size());
        assertFalse(built.get(0).isSection());
        assertTrue(built.get(1).isSection());
        assertFalse(built.get(1).promoted());
        assertEquals(ContextMenuSection.DANGER, built.get(1).section());
    }

    @Test
    void allPromotedSectionSuppressesHeader() {
        ContextMenuSections sections = new ContextMenuSections();
        sections.add(ContextMenuSection.CLIPBOARD, ContextActionFactory.promoted("Copy", "copy", ActionTone.PRIMARY, () -> {
        }));
        sections.add(ContextMenuSection.DANGER, row("Delete"));

        List<ContextAction> built = sections.build();
        // CLIPBOARD all-promoted → header suppressed. DANGER non-promoted → header shown.
        assertEquals(3, built.size());
        assertFalse(built.get(0).isSection());
        assertTrue(built.get(1).isSection());
        assertEquals(ContextMenuSection.DANGER, built.get(1).section());
    }

    private static ContextAction row(String label) {
        return new ContextAction(label, "style", ActionTone.NEUTRAL, () -> {
        });
    }
}
