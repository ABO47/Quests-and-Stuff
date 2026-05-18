package com.abo47.questsandstuff.client.hud;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sound.AssetSoundInstance;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = QuestsAndStuffMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class QuestCompletionNotificationOverlay {
    private static final long DISPLAY_MS = 2600L;
    private static final int WIDTH = 128;
    private static final int HEIGHT = 32;
    private static final Deque<Notification> NOTIFICATIONS = new ArrayDeque<>();

    private QuestCompletionNotificationOverlay() {
    }

    public static void push(String questId) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestCache.quest(questId);
        String title = quest.getString("title");
        playSound(quest.getString("completion_sound"));
        NOTIFICATIONS.addLast(new Notification(title == null || title.isBlank() ? questId : title, System.currentTimeMillis()));
        while (NOTIFICATIONS.size() > 3) {
            NOTIFICATIONS.removeFirst();
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || NOTIFICATIONS.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        while (!NOTIFICATIONS.isEmpty() && now - NOTIFICATIONS.peekFirst().startedAtMs() > DISPLAY_MS) {
            NOTIFICATIONS.removeFirst();
        }
        Notification notification = NOTIFICATIONS.peekFirst();
        if (notification == null) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        Window window = minecraft.getWindow();
        float age = Math.max(0.0f, Math.min(1.0f, (now - notification.startedAtMs()) / (float) DISPLAY_MS));
        int x = window.getGuiScaledWidth() / 2 - WIDTH / 2;
        int y = window.getGuiScaledHeight() - 76 - Math.round((1.0f - easeOut(age)) * 12.0f);
        int alpha = age > 0.78f ? Math.max(0, Math.round(255.0f * (1.0f - (age - 0.78f) / 0.22f))) : 255;
        int panel = TabletUiFactory.withAlpha(ModColors.SURFACE_PANEL, Math.min(235, alpha));
        int border = TabletUiFactory.withAlpha(ModColors.BORDER_ACCENT, alpha);
        int text = TabletUiFactory.withAlpha(ModColors.TEXT_PRIMARY, alpha);
        int success = TabletUiFactory.withAlpha(ModColors.SUCCESS, alpha);

        graphics.fill(x, y, x + WIDTH, y + HEIGHT, panel);
        graphics.renderOutline(x, y, WIDTH, HEIGHT, border);
        int barW = Math.max(1, Math.min(WIDTH - 8, Math.round((WIDTH - 8) * Math.min(1.0f, age * 1.8f))));
        graphics.fill(x + 4, y + HEIGHT - 8, x + 4 + barW, y + HEIGHT - 3, success);
        graphics.drawString(minecraft.font, "Quest completed", x + 7, y + 4, text, false);
        String title = crop(notification.title(), 22);
        graphics.drawString(minecraft.font, title, x + 7, y + 14, TabletUiFactory.withAlpha(ModColors.TEXT_SECONDARY, alpha), false);
    }

    private static void playSound(String soundId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        boolean assetSoundValue = !normalizeAssetSound(soundId).isBlank();
        Optional<Path> assetSound = resolveAssetSound(soundId);
        if (assetSound.isPresent()) {
            Path path = assetSound.get();
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    QuestsAndStuffMod.MODID,
                    "completion_asset/" + Integer.toHexString(path.toAbsolutePath().normalize().toString().hashCode())
            );
            minecraft.getSoundManager().play(new AssetSoundInstance(id, path));
            return;
        }
        if (assetSoundValue) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f));
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(soundId == null || soundId.isBlank()
                ? "minecraft:ui.toast.challenge_complete"
                : soundId);
        SoundEvent event = id == null || "minecraft:ui.toast.challenge_complete".equals(id.toString())
                ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE
                : SoundEvent.createVariableRangeEvent(id);
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(event, 1.0f));
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

    private static float easeOut(float value) {
        float t = Math.max(0.0f, Math.min(1.0f, value));
        return 1.0f - (1.0f - t) * (1.0f - t);
    }

    private static String crop(String value, int max) {
        String safe = value == null ? "" : value;
        return safe.length() <= max ? safe : safe.substring(0, Math.max(0, max - 3)) + "...";
    }

    private record Notification(String title, long startedAtMs) {
    }
}
