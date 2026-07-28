package com.abo47.questsandstuff.client.tablet.quest.details.task;

import java.util.List;
import java.util.function.Function;

import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;

import com.google.gson.JsonObject;

record QuestDetailsTypeChoice(
        String labelKey,
        String type,
        String icon,
        QuestTaskEditFlow editFlow,
        Function<String, JsonObject> defaultJsonFactory,
        List<String> requiredJsonFields
) {
    QuestDetailsTypeChoice {
        requiredJsonFields = List.copyOf(requiredJsonFields);
    }

    String label() {
        return QuestTranslationKeys.text(labelKey);
    }

    String fullType() {
        return TaskJsonFactory.MOD + type;
    }

    JsonObject defaultJson(String id) {
        return defaultJsonFactory.apply(id);
    }
}
