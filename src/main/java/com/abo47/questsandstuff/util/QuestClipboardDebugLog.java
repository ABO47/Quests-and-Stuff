package com.abo47.questsandstuff.util;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.QuestsAndStuffConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

public final class QuestClipboardDebugLog {
    public static final String FILE_NAME = "copy_paste_debug.log";

    private QuestClipboardDebugLog() {
    }

    public static synchronized void append(Path clipboardDir, String message) {
        if (!QuestsAndStuffConfig.debugLoggingEnabled()) {
            return;
        }
        if (clipboardDir == null || message == null || message.isBlank()) {
            return;
        }
        try {
            Files.createDirectories(clipboardDir);
            Path target = clipboardDir.resolve(FILE_NAME);
            String line = "[" + Instant.now() + "] " + message + System.lineSeparator();
            Files.writeString(
                    target,
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:ClipboardDebug] failed writing {}", FILE_NAME, e);
        }
    }
}
