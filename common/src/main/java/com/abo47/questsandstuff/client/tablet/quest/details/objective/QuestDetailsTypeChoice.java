package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.function.Function;

record QuestDetailsTypeChoice(
        String labelKey,
        String type,
        String icon,
        QuestObjectiveEditFlow editFlow,
        Function<String, JsonObject> defaultJsonFactory,
        List<String> requiredJsonFields
) {
    QuestDetailsTypeChoice {
        requiredJsonFields = List.copyOf(requiredJsonFields);
    }

    String label() {
        return QuestVocabulary.text(labelKey);
    }

    String fullType() {
        return QuestObjectiveJsons.MOD + type;
    }

    JsonObject defaultJson(String id) {
        return defaultJsonFactory.apply(id);
    }
}
