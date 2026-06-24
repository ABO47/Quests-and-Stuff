package com.abo47.questsandstuff.client.tablet.controls;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchScrollStateTest {
    @Test
    void resetClearsSearchFocusScrollAndDragging() {
        MutableSearchScroll mutable = new MutableSearchScroll();
        mutable.search = "Nether Fortress";
        mutable.focused = true;
        mutable.scroll = 24;
        mutable.dragging = true;

        mutable.state().reset();

        assertEquals("", mutable.search);
        assertFalse(mutable.focused);
        assertEquals(0, mutable.scroll);
        assertFalse(mutable.dragging);
    }

    @Test
    void clearInteractionKeepsSearchAndScrollPosition() {
        MutableSearchScroll mutable = new MutableSearchScroll();
        mutable.search = "minecraft:plains";
        mutable.focused = true;
        mutable.scroll = 18;
        mutable.dragging = true;

        mutable.state().clearInteraction();

        assertEquals("minecraft:plains", mutable.search);
        assertFalse(mutable.focused);
        assertEquals(18, mutable.scroll);
        assertFalse(mutable.dragging);
    }

    @Test
    void searchInputUsesSharedSearchNormalization() {
        MutableSearchScroll mutable = new MutableSearchScroll();

        SearchScrollState state = mutable.state();
        state.setSearch("Nether\nFORTRESS__");

        assertEquals("nether fortress", mutable.search);
        assertEquals("nether fortress", state.normalizedSearch());
        assertEquals("netherfortress", state.normalizedKey());
    }

    @Test
    void nullSupplierValueReadsAsBlank() {
        MutableSearchScroll mutable = new MutableSearchScroll();
        mutable.search = null;

        assertEquals("", mutable.state().search());
    }

    @Test
    void exposesScrollBinding() {
        MutableSearchScroll mutable = new MutableSearchScroll();

        SearchScrollState state = mutable.state();
        state.setScrollValue(12);
        state.setDragging(true);

        assertEquals(12, state.scrollValue());
        assertTrue(state.dragging());
        assertEquals(12, state.scroll().value());
        assertTrue(state.scroll().dragging());
    }

    private static final class MutableSearchScroll {
        private String search = "";
        private boolean focused;
        private int scroll;
        private boolean dragging;

        private SearchScrollState state() {
            return SearchScrollState.bind(
                    () -> search,
                    value -> search = value,
                    () -> focused,
                    value -> focused = value,
                    () -> scroll,
                    value -> scroll = value,
                    () -> dragging,
                    value -> dragging = value
            );
        }
    }
}
