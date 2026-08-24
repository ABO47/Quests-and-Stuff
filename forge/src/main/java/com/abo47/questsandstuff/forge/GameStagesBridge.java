package com.abo47.questsandstuff.forge;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.platform.Services;
import com.abo47.questsandstuff.quest.runtime.lock.StageBridge;

public final class GameStagesBridge implements StageBridge.GrantHook {
    private static final String HELPER_CLASS = "net.darkhax.gamestages.GameStageHelper";
    private static final Gson GSON = new Gson();

    private static boolean probed;
    private static Method addStageMethod;
    private static Method removeStageMethod;
    private static Map<String, List<String>> questStages = Map.of();
    private static boolean warnedMissing;

    private GameStagesBridge() {
    }

    public static void install() {
        if (!probe()) {
            return;
        }
        loadConfig();
        StageBridge.setHook(new GameStagesBridge());
        if (questStages.isEmpty()) {
            QuestsAndStuffMod.LOGGER.info(
                    "[QnS:Lock] GameStages present but no quest->stage mappings in {}",
                    configFile().getFileName());
        } else {
            QuestsAndStuffMod.LOGGER.info(
                    "[QnS:Lock] GameStages bridge active with {} mapped quest(s)", questStages.size());
        }
    }

    @Override
    public void onQuestCompleted(ServerPlayer player, String questId) {
        apply(player, questStages.get(questId), true);
    }

    @Override
    public void onQuestRevoked(ServerPlayer player, String questId) {
        apply(player, questStages.get(questId), false);
    }

    private static void apply(ServerPlayer player, List<String> stages, boolean grant) {
        if (stages == null || stages.isEmpty() || addStageMethod == null || removeStageMethod == null) {
            return;
        }
        Object[] args = {player, stages.toArray(new String[0])};
        try {
            if (grant) {
                addStageMethod.invoke(null, args);
            } else {
                removeStageMethod.invoke(null, args);
            }
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.warn(
                    "[QnS:Lock] GameStages {} failed for quest {}", grant ? "grant" : "revoke", questIdOf(stages),
                    error);
        }
    }

    private static String questIdOf(List<String> stages) {
        return stages == null ? "" : String.join(",", stages);
    }

    private static boolean probe() {
        if (probed) {
            return addStageMethod != null;
        }
        probed = true;
        try {
            Class<?> helper = Class.forName(HELPER_CLASS);
            addStageMethod = helper.getMethod("addStage", ServerPlayer.class, String[].class);
            removeStageMethod = helper.getMethod("removeStage", ServerPlayer.class, String[].class);
            return true;
        } catch (ClassNotFoundException notInstalled) {
            return false;
        } catch (Exception error) {
            if (!warnedMissing) {
                warnedMissing = true;
                QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] GameStages probe failed", error);
            }
            return false;
        }
    }

    private static void loadConfig() {
        Path file = configFile();
        try {
            if (!Files.exists(file)) {
                writeDefaultConfig(file);
                return;
            }
            JsonObject root = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), JsonObject.class);
            if (root == null) {
                return;
            }
            Map<String, List<String>> parsed = GSON.fromJson(
                    root.getAsJsonObject("questStages"),
                    new TypeToken<Map<String, List<String>>>() {}.getType());
            if (parsed != null) {
                Map<String, List<String>> normalized = new HashMap<>();
                parsed.forEach((questId, stages) -> {
                    if (questId != null && !questId.isBlank() && stages != null && !stages.isEmpty()) {
                        normalized.put(questId.toLowerCase(), List.copyOf(stages));
                    }
                });
                questStages = Map.copyOf(normalized);
            }
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.warn(
                    "[QnS:Lock] failed reading GameStages bridge config {}", file, error);
        }
    }

    private static void writeDefaultConfig(Path file) {
        try {
            JsonObject root = new JsonObject();
            JsonObject mapping = new JsonObject();
            com.google.gson.JsonArray example = new com.google.gson.JsonArray();
            example.add("stage_one");
            mapping.add("example_quest_id", example);
            root.add("questStages", mapping);
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
            QuestsAndStuffMod.LOGGER.info("[QnS:Lock] wrote default GameStages bridge config {}", file);
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] failed writing GameStages bridge config {}", file, error);
        }
    }

    private static Path configFile() {
        return Services.platform().configDir()
                .resolve(QuestsAndStuffMod.MODID)
                .resolve("gamestages-bridge.json");
    }

    static Set<String> mappedQuestsForTests() {
        return questStages.keySet();
    }
}
