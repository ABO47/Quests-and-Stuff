package com.abo47.questsandstuff.quest.persistence.group;

import com.google.gson.JsonObject;

final class GroupMetadataMigrator {
    static final int CURRENT_SCHEMA = 1;

    private GroupMetadataMigrator() {
    }

    static boolean needsMigration(JsonObject input) {
        return input == null || !input.has("schema_version");
    }

    static JsonObject migrate(JsonObject input) {
        JsonObject working = input == null ? new JsonObject() : input.deepCopy();
        working.addProperty("schema_version", CURRENT_SCHEMA);
        return working;
    }
}
