package com.abo47.questsandstuff.client.quest.sound;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class QuestCompletionSoundPlayer {
    public static final String DEFAULT_SOUND = "minecraft:ui.toast.challenge_complete";
    private static final int FADE_OUT_TICKS = 12;

    private QuestCompletionSoundPlayer() {
    }

    public static SoundInstance play(String soundId) {
        return play(soundId, 100);
    }

    public static SoundInstance play(String soundId, int volumePercent) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return null;
        }
        SoundInstance sound = create(soundId, volumePercent);
        if (sound != null) {
            minecraft.getSoundManager().play(sound);
        }
        return sound;
    }

    public static void stop(SoundInstance sound) {
        if (sound != null) {
            if (sound instanceof FadeableQuestSound fadeable) {
                fadeable.stopImmediately();
            }
            Minecraft.getInstance().getSoundManager().stop(sound);
        }
    }

    public static void fadeOut(SoundInstance sound) {
        if (sound == null) {
            return;
        }
        if (sound instanceof FadeableQuestSound fadeable) {
            fadeable.fadeOut(FADE_OUT_TICKS);
            return;
        }
        stop(sound);
    }

    public static boolean isActive(SoundInstance sound) {
        return sound != null && Minecraft.getInstance().getSoundManager().isActive(sound);
    }

    public static boolean isAssetSoundId(String soundId) {
        return !normalizeAssetSound(soundId).isBlank();
    }

    public static String previewKey(String soundId) {
        String normalized = normalizeAssetSound(soundId);
        if (!normalized.isBlank()) {
            return normalized;
        }
        String safe = soundId == null ? "" : soundId.trim();
        return safe.isBlank() ? DEFAULT_SOUND : safe;
    }

    private static SoundInstance create(String soundId, int volumePercent) {
        float volume = volume(volumePercent);
        boolean assetSoundValue = !normalizeAssetSound(soundId).isBlank();
        Optional<Path> assetSound = resolveAssetSound(soundId);
        if (assetSound.isPresent()) {
            Path path = assetSound.get();
            ResourceLocation id = ResourceLocation.tryBuild(
                    QuestsAndStuffMod.MODID,
                    "completion_asset/" + Integer.toHexString(path.toAbsolutePath().normalize().toString().hashCode())
            );
            return id == null ? null : new AssetSoundInstance(id, path, volume);
        }
        if (assetSoundValue) {
            return new FadeableSimpleSoundInstance(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, volume);
        }
        ResourceLocation id = ResourceLocation.tryParse(soundId == null || soundId.isBlank()
                ? DEFAULT_SOUND
                : soundId);
        SoundEvent event = id == null || DEFAULT_SOUND.equals(id.toString())
                ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE
                : SoundEvent.createVariableRangeEvent(id);
        return new FadeableSimpleSoundInstance(event, 1.0f, volume);
    }

    private static float volume(int volumePercent) {
        return Math.max(0.0f, Math.min(1.0f, volumePercent / 100.0f));
    }

    private static Optional<Path> resolveAssetSound(String soundId) {
        String relative = normalizeAssetSound(soundId);
        if (relative.isBlank()) {
            return Optional.empty();
        }
        Path root = TabletUiFactory.ASSETS_ROOT_DIR.toAbsolutePath().normalize();
        Path path = root.resolve(relative).normalize();
        if (!path.startsWith(root) || !Files.isRegularFile(path) || !AssetSoundInstance.canPlay(path)) {
            return Optional.empty();
        }
        return Optional.of(path);
    }

    private static String normalizeAssetSound(String soundId) {
        if (soundId == null) {
            return "";
        }
        String normalized = soundId.replace('\\', '/').trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (!normalized.startsWith("sounds/") || normalized.contains("..")) {
            return "";
        }
        return normalized;
    }
}
