package com.abo47.questsandstuff.quest.persistence.quest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.persistence.QuestSchemaMigrator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.normalizeQuestId;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.withId;

final class QuestDefinitionLoader {
    private QuestDefinitionLoader() {
    }

    static Map<String, QuestDefinition> load(Path questsDir) throws Exception {
        Files.createDirectories(questsDir);
        Map<String, QuestDefinition> loaded = new HashMap<>();
        for (Path path : QuestDefinitionFiles.jsonFiles(questsDir)) {
            readQuestFile(path, loaded);
        }
        return loaded;
    }

    private static void readQuestFile(Path path, Map<String, QuestDefinition> loaded) {
        try {
            String raw = Files.readString(path);
            JsonElement json = JsonParser.parseString(raw);
            JsonObject migrated = QuestSchemaMigrator.migrate(json.getAsJsonObject());
            DataResult<QuestDefinition> decoded = QuestDefinition.CODEC.parse(JsonOps.INSTANCE, migrated);
            QuestDefinition definition = decoded.getOrThrow(false, QuestsAndStuffMod.LOGGER::error);
            String canonicalId = normalizeQuestId(definition.id());
            if (loaded.containsKey(canonicalId)) {
                QuestsAndStuffMod.LOGGER.warn("Duplicate quest id {} while loading {}, keeping latest", canonicalId, path);
            }
            loaded.put(canonicalId, withId(definition, canonicalId));
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.error("Failed reading quest file {}", path, e);
        }
    }
}
