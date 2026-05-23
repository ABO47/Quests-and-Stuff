package com.abo47.questsandstuff.client.tablet.details.objective;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestObjectiveTypeCatalogTest {
    @Test
    void taskCatalogExposesItemInteractionRequirements() {
        Set<String> types = QuestObjectiveTypeCatalog.taskChoices()
                .stream()
                .map(QuestDetailsTypeChoice::type)
                .collect(Collectors.toSet());

        assertTrue(types.contains("item_interact"));
        assertTrue(types.contains("item_use"));
    }
}
