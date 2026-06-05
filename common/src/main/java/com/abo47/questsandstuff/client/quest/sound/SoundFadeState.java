package com.abo47.questsandstuff.client.quest.sound;

final class SoundFadeState {
    private boolean stopped;
    private int fadeTicks;
    private int fadeTicksRemaining;
    private float fadeStartVolume;

    void fadeOut(float currentVolume, int ticks) {
        if (stopped) {
            return;
        }
        fadeStartVolume = Math.max(0.0f, currentVolume);
        fadeTicks = Math.max(1, ticks);
        fadeTicksRemaining = fadeTicks;
    }

    float tick(float currentVolume) {
        if (fadeTicksRemaining <= 0) {
            return currentVolume;
        }
        fadeTicksRemaining--;
        if (fadeTicksRemaining <= 0) {
            stopped = true;
            return 0.0f;
        }
        return fadeStartVolume * (fadeTicksRemaining / (float) fadeTicks);
    }

    void stopImmediately() {
        stopped = true;
        fadeTicksRemaining = 0;
    }

    boolean stopped() {
        return stopped;
    }
}
