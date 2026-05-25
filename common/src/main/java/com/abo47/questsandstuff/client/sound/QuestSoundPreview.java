package com.abo47.questsandstuff.client.sound;

import net.minecraft.client.resources.sounds.SoundInstance;

public final class QuestSoundPreview {
    private static String currentSoundKey = "";
    private static SoundInstance currentSound;

    private QuestSoundPreview() {
    }

    public static boolean toggle(String soundId) {
        return toggle(soundId, 100);
    }

    public static boolean toggle(String soundId, int volumePercent) {
        String key = QuestCompletionSoundPlayer.previewKey(soundId);
        if (key.isBlank()) {
            return false;
        }
        if (key.equals(currentSoundKey) && QuestCompletionSoundPlayer.isActive(currentSound)) {
            stop();
            return false;
        }
        stop();
        currentSound = QuestCompletionSoundPlayer.play(soundId, volumePercent);
        currentSoundKey = currentSound == null ? "" : key;
        return currentSound != null;
    }

    public static void restartIfPlaying(String soundId, int volumePercent) {
        if (!isPlaying(soundId)) {
            return;
        }
        stop();
        currentSound = QuestCompletionSoundPlayer.play(soundId, volumePercent);
        currentSoundKey = currentSound == null ? "" : QuestCompletionSoundPlayer.previewKey(soundId);
    }

    public static void stop() {
        if (currentSound != null) {
            QuestCompletionSoundPlayer.stop(currentSound);
        }
        currentSound = null;
        currentSoundKey = "";
    }

    public static boolean isPlaying(String soundId) {
        String key = QuestCompletionSoundPlayer.previewKey(soundId);
        if (key.isBlank() || !key.equals(currentSoundKey) || !QuestCompletionSoundPlayer.isActive(currentSound)) {
            return false;
        }
        return true;
    }
}
