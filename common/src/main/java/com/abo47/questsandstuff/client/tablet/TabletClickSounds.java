package com.abo47.questsandstuff.client.tablet;

import com.abo47.questsandstuff.client.quest.sound.QuestCompletionSoundPlayer;

public final class TabletClickSounds {
    public static final String UI_CLICK = "questsandstuff:ui.click";

    private TabletClickSounds() {
    }

    // Plays the default tablet click sound. Used as the default for all tablet clicks and for the
    // quest app splitter. Silent if the player is not available or the sound is not registered yet.
    public static void playClick() {
        QuestCompletionSoundPlayer.play(UI_CLICK);
    }
}
