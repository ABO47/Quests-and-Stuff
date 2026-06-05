package com.abo47.questsandstuff.client.quest.sound;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

final class FadeableSimpleSoundInstance extends SimpleSoundInstance implements TickableSoundInstance, FadeableQuestSound {
    private final SoundFadeState fade = new SoundFadeState();

    FadeableSimpleSoundInstance(SoundEvent sound, float pitch, float volume) {
        super(
                sound.getLocation(),
                SoundSource.MASTER,
                Math.max(0.0f, Math.min(1.0f, volume)),
                pitch,
                SoundInstance.createUnseededRandom(),
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0.0,
                0.0,
                0.0,
                true
        );
    }

    @Override
    public void tick() {
        volume = fade.tick(volume);
    }

    @Override
    public boolean isStopped() {
        return fade.stopped();
    }

    @Override
    public void fadeOut(int ticks) {
        fade.fadeOut(volume, ticks);
    }

    @Override
    public void stopImmediately() {
        fade.stopImmediately();
        volume = 0.0f;
    }
}
