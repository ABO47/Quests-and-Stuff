package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestDetailsDescriptionInteractionStateTest {
    @Test
    void beginPanningCapturesPointerAndCurrentScroll() {
        TabletUiState state = new TabletUiState();
        state.questDetails.questDetailsDescScroll = 42;

        QuestDetailsDescriptionInteractionState.beginPanning(state, 10.4D, 25.6D);

        assertTrue(state.questDetails.questDetailsPanning);
        assertEquals(10, state.questDetails.questDetailsPanStartX);
        assertEquals(26, state.questDetails.questDetailsPanStartY);
        assertEquals(42, state.questDetails.questDetailsPanStartScroll);
    }

    @Test
    void beginBoxSelectionOwnsDragStateAndOptionalClear() {
        TabletUiState state = new TabletUiState();
        AtomicInteger clears = new AtomicInteger();

        QuestDetailsDescriptionInteractionState.beginBoxSelection(state, 12, 34, false, clears::incrementAndGet);

        assertTrue(state.questDetails.questDetailsBoxSelecting);
        assertFalse(state.questDetails.questDetailsBoxAdditive);
        assertEquals(12, state.questDetails.questDetailsBoxStartX);
        assertEquals(34, state.questDetails.questDetailsBoxStartY);
        assertEquals(12, state.questDetails.questDetailsBoxCurrentX);
        assertEquals(34, state.questDetails.questDetailsBoxCurrentY);
        assertEquals(1, clears.get());

        QuestDetailsDescriptionInteractionState.updateBoxSelection(state, 30, 40);

        assertEquals(30, state.questDetails.questDetailsBoxCurrentX);
        assertEquals(40, state.questDetails.questDetailsBoxCurrentY);
    }

    @Test
    void additiveBoxSelectionDoesNotClearExistingSelection() {
        TabletUiState state = new TabletUiState();
        AtomicInteger clears = new AtomicInteger();

        QuestDetailsDescriptionInteractionState.beginBoxSelection(state, 1, 2, true, clears::incrementAndGet);

        assertTrue(state.questDetails.questDetailsBoxAdditive);
        assertEquals(0, clears.get());
    }

    @Test
    void recordTextClickDetectsDoubleClickWithinThreshold() {
        TabletUiState state = new TabletUiState();

        assertFalse(QuestDetailsDescriptionInteractionState.recordTextClick(state, "text:a", 1_000L, 350L));
        assertTrue(QuestDetailsDescriptionInteractionState.recordTextClick(state, "text:a", 1_250L, 350L));
        assertFalse(QuestDetailsDescriptionInteractionState.recordTextClick(state, "text:b", 1_300L, 350L));
        assertFalse(QuestDetailsDescriptionInteractionState.recordTextClick(state, "text:b", 2_000L, 350L));
    }

    @Test
    void wheelScrollUsesDescriptionClamp() {
        TabletUiState state = new TabletUiState();
        state.root.editorAvailable = true;
        state.questDetails.questDetailsEditMode = true;
        QuestDetailsDescriptionModel model = new QuestDetailsDescriptionModel();
        model.putText(new CanvasTextLayer("text", "Tall", 20, 20, 80, 220, 0, "left", "normal", 0xFFFFFF));

        boolean changed = QuestDetailsDescriptionInteractionState.scrollByWheel(state, model, 100, -1.0D);

        assertTrue(changed);
        assertEquals(18, state.questDetails.questDetailsDescScroll);

        changed = QuestDetailsDescriptionInteractionState.scrollByWheel(state, model, 100, 1.0D);

        assertTrue(changed);
        assertEquals(0, state.questDetails.questDetailsDescScroll);
    }
}
