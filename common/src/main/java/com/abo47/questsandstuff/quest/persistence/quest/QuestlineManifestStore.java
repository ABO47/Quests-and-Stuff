package com.abo47.questsandstuff.quest.persistence.quest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import com.abo47.questsandstuff.platform.Services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.ZoneOffset;

final class QuestlineManifestStore {
    static final int CURRENT_SCHEMA = 1;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String DEFAULT_TITLE = "Quests and Stuff Questline";
    private static final String DEFAULT_AUTHOR = "Abo47";
    private static final String DEFAULT_DESCRIPTION = "A questline pack for Quests and Stuff.";
    private static final String UNKNOWN_VERSION = "unknown";

    private final Path manifestFile;

    QuestlineManifestStore(Path root) {
        this.manifestFile = root.resolve("manifest.json");
    }

    void ensureExists() {
        if (Files.isRegularFile(manifestFile)) {
            return;
        }
        save();
    }

    void save() {
        JsonObject existing = readExisting();
        JsonObject manifest = buildManifest(existing);
        write(manifest);
    }

    private JsonObject buildManifest(JsonObject existing) {
        JsonObject manifest = new JsonObject();
        manifest.addProperty("schema_version", CURRENT_SCHEMA);

        JsonObject mod = new JsonObject();
        mod.addProperty("id", QuestsAndStuffMod.MODID);
        mod.addProperty("version", modVersion(QuestsAndStuffMod.MODID));
        manifest.add("mod", mod);

        manifest.addProperty("quest_schema_version", QuestDefinition.CURRENT_SCHEMA);
        manifest.add("pack", pack(object(existing, "pack")));
        manifest.add("targets", targets());
        manifest.add("required_mods", requiredMods());
        manifest.add("optional_mods", optionalMods(existing));
        return manifest;
    }

    private static JsonObject pack(JsonObject existing) {
        JsonObject pack = new JsonObject();
        pack.addProperty("title", readString(existing, "title", DEFAULT_TITLE));
        pack.addProperty("author", readString(existing, "author", DEFAULT_AUTHOR));
        pack.addProperty("description", readString(existing, "description", DEFAULT_DESCRIPTION));
        pack.addProperty("created_date", readString(existing, "created_date", LocalDate.now(ZoneOffset.UTC).toString()));
        return pack;
    }

    private static JsonObject targets() {
        JsonObject targets = new JsonObject();
        targets.addProperty("minecraft", minecraftVersion());
        targets.addProperty(Services.platform().loaderName(), loaderVersion());
        return targets;
    }

    private static JsonArray requiredMods() {
        JsonArray required = new JsonArray();
        required.add(modRequirement("minecraft", minecraftVersion()));
        required.add(modRequirement(Services.platform().loaderName(), loaderVersion()));
        required.add(modRequirement("ldlib", modVersion("ldlib")));
        required.add(modRequirement(QuestsAndStuffMod.MODID, modVersion(QuestsAndStuffMod.MODID)));
        return required;
    }

    private static JsonArray optionalMods(JsonObject existing) {
        if (existing != null && existing.has("optional_mods") && existing.get("optional_mods").isJsonArray()) {
            return existing.getAsJsonArray("optional_mods").deepCopy();
        }
        return new JsonArray();
    }

    private static JsonObject object(JsonObject root, String key) {
        if (root != null && root.has(key) && root.get(key).isJsonObject()) {
            return root.getAsJsonObject(key);
        }
        return null;
    }

    private static JsonObject modRequirement(String id, String version) {
        JsonObject mod = new JsonObject();
        mod.addProperty("id", id);
        mod.addProperty("version", version == null || version.isBlank() ? UNKNOWN_VERSION : version);
        return mod;
    }

    private JsonObject readExisting() {
        if (!Files.isRegularFile(manifestFile)) {
            return null;
        }
        try {
            JsonElement parsed = JsonParser.parseString(Files.readString(manifestFile, StandardCharsets.UTF_8));
            return parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed reading questline manifest {}, rewriting defaults", manifestFile, e);
            return null;
        }
    }

    private void write(JsonObject manifest) {
        try {
            Path parent = manifestFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temp = manifestFile.resolveSibling(manifestFile.getFileName().toString() + ".tmp");
            Files.writeString(temp, GSON.toJson(manifest), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(temp, manifestFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temp, manifestFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            QuestsAndStuffMod.LOGGER.warn("Failed writing questline manifest {}", manifestFile, e);
        }
    }

    private static String readString(JsonObject root, String key, String fallback) {
        if (root != null && root.has(key) && root.get(key).isJsonPrimitive()) {
            String value = root.get(key).getAsString();
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return fallback;
    }

    private static String modVersion(String modId) {
        String version = Services.platform().modVersion(modId);
        return version == null || version.isBlank() ? UNKNOWN_VERSION : version;
    }

    private static String minecraftVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }

    private static String loaderVersion() {
        String version = Services.platform().loaderVersion();
        return version == null || version.isBlank() ? UNKNOWN_VERSION : version;
    }
}
