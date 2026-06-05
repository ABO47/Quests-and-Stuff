package com.abo47.questsandstuff.client.quest.hud;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.quest.sound.QuestCompletionSoundPlayer;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.Deque;

public final class QuestCompletionNotificationOverlay {
    private static final int WIDTH = 128;
    private static final int HEIGHT = 32;
    private static final int MAX_NOTIFICATIONS = 3;
    private static final int MIN_FONT_ALPHA = 4;
    private static final Deque<PendingNotification> PENDING = new ArrayDeque<>();
    private static ActiveNotification activeNotification;
    private static SoundInstance activeSound;

    private QuestCompletionNotificationOverlay() {
    }

    public static int width() {
        return WIDTH;
    }

    public static int height() {
        return HEIGHT;
    }

    public static void push(String questId) {
        if (!QuestsAndStuffConfig.completionHudEnabled() || questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestCache.quest(questId);
        String title = quest.getString("title");
        String background = quest.getString("completion_hud_background");
        PENDING.addLast(new PendingNotification(
                title == null || title.isBlank() ? questId : title,
                background,
                quest.getString("completion_sound"),
                completionSoundVolume(quest)
        ));
        trimPendingNotifications();
    }

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            clear();
            return;
        }
        if (!QuestsAndStuffConfig.completionHudEnabled()) {
            clear();
            return;
        }
        if (!QuestsAndStuffConfig.completionHudSoundEnabled()) {
            fadeOutActiveSound();
        }
        long now = System.currentTimeMillis();
        long displayMs = QuestsAndStuffConfig.completionHudDurationMs();
        updateActiveNotification(now, displayMs);
        ActiveNotification notification = activeNotification;
        if (notification == null) {
            return;
        }

        Window window = minecraft.getWindow();
        long elapsedMs = Math.max(0L, now - notification.startedAtMs());
        float age = progress(elapsedMs, displayMs);
        float heightScale = QuestHudLayout.heightScale(QuestHudLayout.Element.COMPLETION);
        QuestHudLayout.HudBox box = QuestHudLayout.completionBox(
                window.getGuiScaledWidth(),
                window.getGuiScaledHeight(),
                QuestHudLayout.scaledSize(QuestHudLayout.Element.COMPLETION, WIDTH),
                QuestHudLayout.scaledHeight(QuestHudLayout.Element.COMPLETION, HEIGHT)
        );
        int x = box.x();
        float slideDistance = 12.0f * heightScale;
        int y = notificationY(box.y(), slideDistance, age);
        int alpha = notificationAlpha(age);
        drawNotification(graphics, x, y, box.width(), box.height(), notification.title(), notification.background(), alpha, age, false);
    }

    public static void onHudHidden() {
        finishActiveNotification();
    }

    public static void renderPreview(GuiGraphics graphics, int x, int y, boolean selected) {
        renderPreview(
                graphics,
                x,
                y,
                QuestHudLayout.scaledSize(QuestHudLayout.Element.COMPLETION, WIDTH),
                QuestHudLayout.scaledHeight(QuestHudLayout.Element.COMPLETION, HEIGHT),
                selected
        );
    }

    public static void renderPreview(GuiGraphics graphics, int x, int y, int width, int height, boolean selected) {
        drawNotification(
                graphics,
                x,
                y,
                width,
                height,
                Component.translatable("ui.questsandstuff.hud.completion_preview").getString(),
                "",
                255,
                0.85f,
                selected
        );
    }

    private static void drawNotification(GuiGraphics graphics, int x, int y, int width, int height, String titleValue, String background, int alpha, float age, boolean selected) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int safeW = Math.max(1, width);
        int safeH = Math.max(1, height);
        int contentW = Math.max(0, safeW - 14);
        int text = TabletUiFactory.withAlpha(ModColors.TEXT_PRIMARY, alpha);

        QuestHudBackgroundRenderer.draw(graphics, QuestHudLayout.Element.COMPLETION, x, y, safeW, safeH, selected, background, alpha);
        if (safeH >= 10) {
            int barH = safeH < 24 ? 4 : 6;
            QuestHudProgressBar.draw(graphics, x + 4, y + safeH - barH - 3, safeW - 8, barH, age, ModColors.SUCCESS, alpha);
        }
        if (contentW <= 0 || safeH < 12) {
            return;
        }
        if (alpha < MIN_FONT_ALPHA) {
            return;
        }

        String completed = cropToWidth(font, Component.translatable("ui.questsandstuff.hud.quest_completed").getString(), contentW);
        if (safeH >= 27) {
            graphics.drawString(font, completed, x + 7, y + 4, text, false);
            String title = cropToWidth(font, titleValue, contentW);
            graphics.drawString(font, title, x + 7, y + 14, TabletUiFactory.withAlpha(ModColors.TEXT_SECONDARY, alpha), false);
            return;
        }
        String title = cropToWidth(font, titleValue == null || titleValue.isBlank() ? completed : titleValue, contentW);
        graphics.drawString(font, title, x + 7, y + Math.max(2, (safeH - 8) / 2), text, false);
    }

    private static float smoothStep(float value) {
        float t = Math.max(0.0f, Math.min(1.0f, value));
        return t * t * (3.0f - 2.0f * t);
    }

    private static float progress(long elapsedMs, long durationMs) {
        if (durationMs <= 0L) {
            return 1.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, elapsedMs / (float) durationMs));
    }

    private static int notificationY(int restingY, float slideDistance, float age) {
        return restingY + Math.round((-slideDistance) + smoothStep(age) * slideDistance * 2.0f);
    }

    private static int notificationAlpha(float age) {
        float alpha = age < 0.5f
                ? smoothStep(age * 2.0f)
                : 1.0f - smoothStep((age - 0.5f) * 2.0f);
        return Math.max(0, Math.min(255, Math.round(255.0f * alpha)));
    }

    private static String cropToWidth(Font font, String value, int width) {
        String safe = value == null ? "" : value;
        if (width <= 0 || safe.isBlank()) {
            return "";
        }
        if (font.width(safe) <= width) {
            return safe;
        }
        String ellipsis = "...";
        if (font.width(ellipsis) > width) {
            String cropped = safe;
            while (!cropped.isEmpty() && font.width(cropped) > width) {
                cropped = cropped.substring(0, cropped.length() - 1);
            }
            return cropped;
        }
        String cropped = safe;
        while (!cropped.isEmpty() && font.width(cropped + ellipsis) > width) {
            cropped = cropped.substring(0, cropped.length() - 1);
        }
        return cropped.isEmpty() ? ellipsis : cropped + ellipsis;
    }

    private static int completionSoundVolume(CompoundTag quest) {
        if (quest == null || !quest.contains("completion_sound_volume")) {
            return QuestDisplay.DEFAULT_COMPLETION_SOUND_VOLUME;
        }
        return QuestDisplay.normalizeCompletionSoundVolume(quest.getInt("completion_sound_volume"));
    }

    private static void updateActiveNotification(long now, long displayMs) {
        if (activeNotification != null && now - activeNotification.startedAtMs() > displayMs) {
            finishActiveNotification();
        }
        if (activeNotification == null) {
            startNextNotification(now);
        }
    }

    private static void startNextNotification(long now) {
        PendingNotification pending = PENDING.pollFirst();
        if (pending == null) {
            return;
        }
        activeSound = QuestsAndStuffConfig.completionHudSoundEnabled()
                ? QuestCompletionSoundPlayer.play(pending.soundId(), pending.soundVolume())
                : null;
        activeNotification = new ActiveNotification(pending.title(), pending.background(), now);
    }

    private static void finishActiveNotification() {
        fadeOutActiveSound();
        activeNotification = null;
    }

    private static void fadeOutActiveSound() {
        QuestCompletionSoundPlayer.fadeOut(activeSound);
        activeSound = null;
    }

    private static void clear() {
        PENDING.clear();
        finishActiveNotification();
    }

    private static void trimPendingNotifications() {
        while (PENDING.size() + (activeNotification == null ? 0 : 1) > MAX_NOTIFICATIONS) {
            PENDING.removeFirst();
        }
    }

    private record PendingNotification(String title, String background, String soundId, int soundVolume) {
    }

    private record ActiveNotification(String title, String background, long startedAtMs) {
    }
}
