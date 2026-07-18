package com.abo47.questsandstuff.quest.persistence.quest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.cloneDefinition;

final class QuestDefinitionSaveQueue {
    private final Path questsDir;
    private final Gson gson;
    private final ScheduledExecutorService saveExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, ScheduledFuture<?>> delayedSaves = new HashMap<>();

    QuestDefinitionSaveQueue(Path questsDir, Gson gson) {
        this.questsDir = questsDir;
        this.gson = gson;
    }

    void shutdown() {
        saveExecutor.shutdownNow();
    }

    void cancel(String questId) {
        ScheduledFuture<?> pending = delayedSaves.remove(questId);
        if (pending != null) {
            pending.cancel(true);
        }
    }

    void markDirty(String questId, QuestDefinition definition) {
        cancel(questId);
        if (definition == null) {
            return;
        }
        QuestDefinition snapshot = cloneDefinition(definition);
        delayedSaves.put(questId, saveExecutor.schedule(() -> saveSnapshot(questId, snapshot), 600, TimeUnit.MILLISECONDS));
    }

    void saveNow(String questId, QuestDefinition definition) {
        cancel(questId);
        if (definition == null) {
            return;
        }
        saveSnapshot(questId, cloneDefinition(definition));
    }

    void saveAll(Map<String, QuestDefinition> snapshot) {
        for (String questId : snapshot.keySet()) {
            cancel(questId);
        }
        for (Map.Entry<String, QuestDefinition> entry : snapshot.entrySet()) {
            saveSnapshot(entry.getKey(), entry.getValue());
        }
    }

    private void saveSnapshot(String questId, QuestDefinition definition) {
        if (definition == null) {
            return;
        }
        try {
            Files.createDirectories(questsDir);
            DataResult<JsonElement> encoded = QuestDefinition.CODEC.encodeStart(JsonOps.INSTANCE, definition);
            JsonElement json = encoded.getOrThrow(false, QuestsAndStuffMod.LOGGER::error);
            if (json.isJsonObject()) {
                JsonObject object = json.getAsJsonObject();
                object.addProperty("schema_version", QuestDefinition.CURRENT_SCHEMA);
            }

            Path target = QuestDefinitionPaths.questPath(questsDir, definition);
            QuestDefinitionFiles.writeAtomic(target, gson.toJson(json));
            delayedSaves.remove(questId);
        } catch (IOException e) {
            QuestsAndStuffMod.LOGGER.error("Failed to save quest {}", questId, e);
        }
    }
}
