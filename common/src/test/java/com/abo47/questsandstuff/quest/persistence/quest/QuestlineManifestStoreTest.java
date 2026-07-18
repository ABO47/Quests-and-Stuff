package com.abo47.questsandstuff.quest.persistence.quest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestlineManifestStoreTest {
    @TempDir
    Path root;

    @Test
    void missingManifestWritesDefaultPolicy() throws Exception {
        new QuestlineManifestStore(root).ensureExists();

        JsonObject manifest = readManifest();
        assertEquals(QuestlineManifestStore.CURRENT_SCHEMA, manifest.get("schema_version").getAsInt());
        assertEquals(QuestDefinition.CURRENT_SCHEMA, manifest.get("quest_schema_version").getAsInt());
        assertEquals(QuestsAndStuffMod.MODID, manifest.getAsJsonObject("mod").get("id").getAsString());
        assertEquals("Quests and Stuff Questline", pack(manifest).get("title").getAsString());
        assertEquals("Abo47", pack(manifest).get("author").getAsString());
        assertEquals(0, manifest.getAsJsonArray("optional_mods").size());
        assertTrue(manifest.getAsJsonArray("required_mods").size() >= 4);
        assertTrue(manifest.getAsJsonObject("targets").has("minecraft"));
    }

    @Test
    void partialManifestPreservesRecoverableEditableFieldsAndRegeneratesGeneratedFields() throws Exception {
        Files.writeString(root.resolve("manifest.json"), """
                {
                  "schema_version": -1,
                  "quest_schema_version": -1,
                  "mod": {"id": "wrong", "version": "old"},
                  "pack": {
                    "title": "  ",
                    "author": "Tester",
                    "description": "Shared quests"
                  },
                  "optional_mods": [
                    {"id": "example", "version": "1.0.0"}
                  ],
                  "required_mods": []
                }
                """, StandardCharsets.UTF_8);

        new QuestlineManifestStore(root).save();

        JsonObject manifest = readManifest();
        JsonObject pack = pack(manifest);
        assertEquals(QuestlineManifestStore.CURRENT_SCHEMA, manifest.get("schema_version").getAsInt());
        assertEquals(QuestDefinition.CURRENT_SCHEMA, manifest.get("quest_schema_version").getAsInt());
        assertEquals(QuestsAndStuffMod.MODID, manifest.getAsJsonObject("mod").get("id").getAsString());
        assertEquals("Quests and Stuff Questline", pack.get("title").getAsString());
        assertEquals("Tester", pack.get("author").getAsString());
        assertEquals("Shared quests", pack.get("description").getAsString());
        assertEquals(1, manifest.getAsJsonArray("optional_mods").size());
        assertTrue(manifest.getAsJsonArray("required_mods").size() >= 4);
    }

    @Test
    void malformedManifestRewritesDefaults() throws Exception {
        Files.writeString(root.resolve("manifest.json"), "{not valid json", StandardCharsets.UTF_8);

        new QuestlineManifestStore(root).save();

        JsonObject manifest = readManifest();
        assertEquals("Quests and Stuff Questline", pack(manifest).get("title").getAsString());
        assertEquals("Abo47", pack(manifest).get("author").getAsString());
        assertEquals(0, manifest.getAsJsonArray("optional_mods").size());
        assertEquals(QuestlineManifestStore.CURRENT_SCHEMA, manifest.get("schema_version").getAsInt());
    }

    @Test
    void nonObjectManifestRewritesDefaults() throws Exception {
        Files.writeString(root.resolve("manifest.json"), "[]", StandardCharsets.UTF_8);

        new QuestlineManifestStore(root).save();

        JsonObject manifest = readManifest();
        assertEquals("Quests and Stuff Questline", pack(manifest).get("title").getAsString());
        assertEquals("Abo47", pack(manifest).get("author").getAsString());
        assertEquals(0, manifest.getAsJsonArray("optional_mods").size());
    }

    private JsonObject readManifest() throws Exception {
        return JsonParser.parseString(Files.readString(root.resolve("manifest.json"), StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static JsonObject pack(JsonObject manifest) {
        return manifest.getAsJsonObject("pack");
    }
}
