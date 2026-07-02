package com.abo47.questsandstuff.client.quest.sound;

import com.mojang.blaze3d.audio.OggAudioStream;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.ConstantFloat;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class FileSoundInstance extends AbstractSoundInstance implements TickableSoundInstance, FadeableQuestSound {
    private final Path path;
    private final String extension;
    private final WeighedSoundEvents event;
    private final SoundFadeState fade = new SoundFadeState();

    public FileSoundInstance(ResourceLocation id, Path path) {
        this(id, path, 1.0f);
    }

    public FileSoundInstance(ResourceLocation id, Path path, float volume) {
        super(id, SoundSource.MASTER, SoundInstance.createUnseededRandom());
        this.path = path;
        this.extension = extension(path);
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        this.pitch = 1.0f;
        this.relative = true;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.sound = new Sound(
                id.toString(),
                ConstantFloat.of(1.0f),
                ConstantFloat.of(1.0f),
                1,
                Sound.Type.FILE,
                true,
                false,
                16
        );
        this.event = new WeighedSoundEvents(id, null);
        this.event.addSound(this.sound);
    }

    @Override
    public WeighedSoundEvents resolve(SoundManager manager) {
        return event;
    }

    public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary library, ResourceLocation location, boolean looping) {
        return openAudioStream();
    }

    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary library, Sound sound, boolean looping) {
        return openAudioStream();
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

    private CompletableFuture<AudioStream> openAudioStream() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if ("ogg".equals(extension)) {
                    return new OggAudioStream(Files.newInputStream(path));
                }
                if ("wav".equals(extension)) {
                    return new WavAudioStream(Files.newInputStream(path));
                }
                throw new IOException("Unsupported sound format: " + extension);
            } catch (Exception e) {
                throw new AssetSoundException(path, e);
            }
        });
    }

    public static boolean canPlay(Path path) {
        String ext = extension(path);
        return "ogg".equals(ext) || "wav".equals(ext);
    }

    private static String extension(Path path) {
        String name = path.getFileName() == null ? "" : path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot + 1 >= name.length()) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static final class WavAudioStream implements AudioStream {
        private final AudioInputStream stream;
        private final AudioFormat format;

        private WavAudioStream(InputStream input) throws Exception {
            AudioInputStream raw = AudioSystem.getAudioInputStream(input);
            AudioFormat rawFormat = raw.getFormat();
            this.format = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    rawFormat.getSampleRate(),
                    16,
                    rawFormat.getChannels(),
                    rawFormat.getChannels() * 2,
                    rawFormat.getSampleRate(),
                    false
            );
            this.stream = AudioSystem.getAudioInputStream(format, raw);
        }

        @Override
        public AudioFormat getFormat() {
            return format;
        }

        @Override
        public ByteBuffer read(int size) throws IOException {
            byte[] bytes = new byte[Math.max(1, size)];
            int read = stream.read(bytes);
            if (read <= 0) {
                return ByteBuffer.allocate(0);
            }
            ByteBuffer buffer = ByteBuffer.allocateDirect(read);
            buffer.put(bytes, 0, read);
            buffer.flip();
            return buffer;
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }
    }

    private static final class AssetSoundException extends RuntimeException {
        private AssetSoundException(Path path, Throwable cause) {
            super("Failed to play asset sound " + path, cause);
        }
    }
}
