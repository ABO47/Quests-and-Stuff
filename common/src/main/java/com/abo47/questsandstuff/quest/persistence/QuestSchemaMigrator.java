package com.abo47.questsandstuff.quest.persistence;

import com.abo47.questsandstuff.quest.model.QuestDefinition;

import com.google.gson.JsonObject;

public final class QuestSchemaMigrator {
    private QuestSchemaMigrator() {
    }

    public static JsonObject migrate(JsonObject input) {
        JsonObject working = input.deepCopy();
        writeSchema(working);
        return working;
    }

    private static void writeSchema(JsonObject root) {
        root.addProperty("schema", QuestDefinition.CURRENT_SCHEMA);
        root.addProperty("schema_version", QuestDefinition.CURRENT_SCHEMA);
    }
}
