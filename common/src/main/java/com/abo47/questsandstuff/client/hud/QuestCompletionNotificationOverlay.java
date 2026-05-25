package com.abo47.questsandstuff.client.hud;

import com.abo47.questsandstuff.client.sound.QuestCompletionSoundPlayer;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayDeque;
import java.util.Deque;

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
        QuestCompletionSoundPlayer.play(quest.getString("completion_sound"), completionSoundVolume(quest));
        NOTIFICATIONS.addLast(new Notification(title == null || title.isBlank() ? questId : title, System.currentTimeMillis()));
        while (NOTIFICATIONS.size() > 3) {
            NOTIFICATIONS.removeFirst();
        }
    }

    public static void render(GuiGraphics graphics) {
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

    private static float easeOut(float value) {
        float t = Math.max(0.0f, Math.min(1.0f, value));
        return 1.0f - (1.0f - t) * (1.0f - t);
    }

    private static String crop(String value, int max) {
        String safe = value == null ? "" : value;
        return safe.length() <= max ? safe : safe.substring(0, Math.max(0, max - 3)) + "...";
    }

    private static int completionSoundVolume(CompoundTag quest) {
        if (quest == null || !quest.contains("completion_sound_volume")) {
            return QuestDisplay.DEFAULT_COMPLETION_SOUND_VOLUME;
        }
        return QuestDisplay.normalizeCompletionSoundVolume(quest.getInt("completion_sound_volume"));
    }

    private record Notification(String title, long startedAtMs) {
    }
}
