package com.abo47.questsandstuff.client.tablet.contextmenu;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TranslationKeyCompletenessTest {
    private static final String RESOURCE = "/assets/questsandstuff/lang/en_us.json";
    private static final Pattern KEY_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*:");

    @Test
    void allReferencedTranslationKeysExist() throws Exception {
        Set<String> keys = loadKeys();
        for (String value : constantValues(QuestTranslationKeys.class)) {
            assertTrue(keys.contains(value), "Missing en_us key for QuestTranslationKeys value: " + value);
        }
        for (String value : constantValues(TabletTranslationKeys.class)) {
            assertTrue(keys.contains(value), "Missing en_us key for TabletTranslationKeys value: " + value);
        }
        for (ContextMenuSection section : ContextMenuSection.values()) {
            assertTrue(keys.contains(section.titleKey()), "Missing en_us key for section title: " + section.titleKey());
        }
    }

    private static Set<String> loadKeys() throws Exception {
        Set<String> keys = new HashSet<>();
        try (InputStream in = TranslationKeyCompletenessTest.class.getResourceAsStream(RESOURCE)) {
            assertTrue(in != null, "Could not load " + RESOURCE);
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            Matcher matcher = KEY_PATTERN.matcher(sb);
            while (matcher.find()) {
                keys.add(matcher.group(1));
            }
        }
        return keys;
    }

    private static Set<String> constantValues(Class<?> holder) {
        Set<String> values = new HashSet<>();
        for (Field field : holder.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                try {
                    Object value = field.get(null);
                    if (value instanceof String s && s.startsWith("ui.questsandstuff")) {
                        values.add(s);
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        return values;
    }
}
