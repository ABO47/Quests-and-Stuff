package com.abo47.questsandstuff.client.tablet.text;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestTranslationVocabularyTest {
    private static final Path EN_US = Path.of("src/main/resources/assets/questsandstuff/lang/en_us.json");
    private static final String OLD_UNLOCK_EDGE = "dependenc";
    private static final String OLD_MEDIA_NODE = "pictur";
    private static final String OLD_EDGE_VERB = "lin";
    private static final Set<String> RETIRED_VISIBLE_TERMS = Set.of(
            "requirement",
            "requirements",
            OLD_UNLOCK_EDGE + "y",
            OLD_UNLOCK_EDGE + "ies",
            OLD_MEDIA_NODE + "e",
            OLD_MEDIA_NODE + "es",
            OLD_EDGE_VERB + "k",
            OLD_EDGE_VERB + "ks",
            "un" + OLD_EDGE_VERB + "k",
            "un" + OLD_EDGE_VERB + "ks"
    );

    @Test
    void englishTranslationsUseCanonicalQuestTranslationKeys() throws Exception {
        JsonObject translations = JsonParser.parseString(Files.readString(EN_US)).getAsJsonObject();
        Set<String> drift = new LinkedHashSet<>();
        for (Map.Entry<String, JsonElement> entry : translations.entrySet()) {
            checkText(entry.getKey(), "key", entry.getKey(), drift);
            if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString()) {
                checkText(entry.getKey(), "value", entry.getValue().getAsString(), drift);
            }
        }
        assertTrue(
                drift.isEmpty(),
                "Use Task, Reward, Prerequisite, Image, and Connect/Connection in visible translations: " + drift
        );
    }

    private static void checkText(String key, String part, String text, Set<String> drift) {
        String[] tokens = text.toLowerCase(Locale.ROOT).split("[^a-z]+");
        for (String token : tokens) {
            if (RETIRED_VISIBLE_TERMS.contains(token)) {
                drift.add(key + " " + part + " uses '" + token + "'");
            }
        }
    }
}
