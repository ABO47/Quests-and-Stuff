package com.abo47.questsandstuff.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SafeNamesTest {
    @Test
    void identifierKeepsAsciiWordsAndCollapsesSeparators() {
        assertEquals("my_chapter_01", SafeNames.identifier(" My Chapter!! 01 ", "fallback"));
        assertEquals("already_clean", SafeNames.identifier("__already__clean__", "fallback"));
    }

    @Test
    void identifierFallsBackWhenNothingUsableRemains() {
        assertEquals("fallback", SafeNames.identifier("!!!", "fallback"));
        assertEquals("fallback", SafeNames.identifier(null, "fallback"));
    }

    @Test
    void fileStemAllowsDashes() {
        assertEquals("dark-ui_theme", SafeNames.fileStem("Dark-UI Theme", "default"));
    }
}
