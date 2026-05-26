package com.abo47.questsandstuff.client.hud;

import com.abo47.questsandstuff.client.sound.QuestCompletionSoundPlayer;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.Deque;

public final class QuestCompletionNotificationOverlay {
    private static final long DISPLAY_MS = 2600L;
    private static final int WIDTH = 128;
    private static final int HEIGHT = 32;
    private static final Deque<Notification> NOTIFICATIONS = new ArrayDeque<>();

    private QuestCompletionNotificationOverlay() {
    }

    public static int width() {
        return WIDTH;
    }

    public static int height() {
        return HEIGHT;
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
        float heightScale = QuestHudLayout.heightScale(QuestHudLayout.Element.COMPLETION);
        QuestHudLayout.HudBox box = QuestHudLayout.completionBox(
                window.getGuiScaledWidth(),
                window.getGuiScaledHeight(),
                QuestHudLayout.scaledSize(QuestHudLayout.Element.COMPLETION, WIDTH),
                QuestHudLayout.scaledHeight(QuestHudLayout.Element.COMPLETION, HEIGHT)
        );
        int x = box.x();
        int y = box.y() - Math.round((1.0f - easeOut(age)) * 12.0f * heightScale);
        int alpha = age > 0.78f ? Math.max(0, Math.round(255.0f * (1.0f - (age - 0.78f) / 0.22f))) : 255;
        drawNotification(graphics, x, y, box.width(), box.height(), notification.title(), alpha, age, false);
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
                255,
                0.85f,
                selected
        );
    }

    private static void drawNotification(GuiGraphics graphics, int x, int y, int width, int height, String titleValue, int alpha, float age, boolean selected) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int safeW = Math.max(1, width);
        int safeH = Math.max(1, height);
        int contentW = Math.max(0, safeW - 14);
        int text = TabletUiFactory.withAlpha(ModColors.TEXT_PRIMARY, alpha);

        QuestHudBackgroundRenderer.draw(graphics, QuestHudLayout.Element.COMPLETION, x, y, safeW, safeH, selected);
        if (safeH >= 10) {
            int barH = safeH < 24 ? 4 : 6;
            QuestHudProgressBar.draw(graphics, x + 4, y + safeH - barH - 3, safeW - 8, barH, Math.min(1.0f, age * 1.8f), ModColors.SUCCESS, alpha);
        }
        if (contentW <= 0 || safeH < 12) {
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

    private static float easeOut(float value) {
        float t = Math.max(0.0f, Math.min(1.0f, value));
        return 1.0f - (1.0f - t) * (1.0f - t);
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

    private record Notification(String title, long startedAtMs) {
    }
}
