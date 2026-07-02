package com.abo47.questsandstuff.client.quest.hud;

import static com.abo47.questsandstuff.client.tablet.theme.render.Surfaces.withAlpha;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.animation.ProgressAnimations;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestObjectiveHudDisplay;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PinnedQuestHudOverlay {
    private static final int WIDTH = 168;
    private static final int HEADER_HEIGHT = 25;
    private static final int ROW_HEIGHT = 12;
    private static final int PAD = 6;
    private static final int STACK_GAP = 4;
    private static final int MAX_QUESTS = 3;
    private static final int MAX_REQUIREMENT_ROWS = 4;

    private PinnedQuestHudOverlay() {
    }

    public static int width() {
        return WIDTH;
    }

    public static int currentStackHeight() {
        List<CompoundTag> quests = pinnedQuestTags();
        if (quests.isEmpty()) {
            return previewHeight();
        }
        return stackHeight(quests);
    }

    public static int previewHeight() {
        return HEADER_HEIGHT + PAD + ROW_HEIGHT * 2;
    }

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        List<CompoundTag> quests = pinnedQuestTags();
        if (quests.isEmpty()) {
            return;
        }

        Window window = minecraft.getWindow();
        int stackHeight = stackHeight(quests);
        QuestHudLayout.HudBox box = QuestHudLayout.pinnedBox(
                window.getGuiScaledWidth(),
                window.getGuiScaledHeight(),
                QuestHudLayout.scaledSize(QuestHudLayout.Element.PINNED, WIDTH),
                QuestHudLayout.scaledHeight(QuestHudLayout.Element.PINNED, stackHeight)
        );
        drawPinnedStack(graphics, quests, box.x(), box.y(), box.width(), box.height(), false);
    }

    public static void renderPreview(GuiGraphics graphics, int x, int y, boolean selected) {
        renderPreview(
                graphics,
                x,
                y,
                QuestHudLayout.scaledSize(QuestHudLayout.Element.PINNED, WIDTH),
                QuestHudLayout.scaledHeight(QuestHudLayout.Element.PINNED, currentStackHeight()),
                selected
        );
    }

    public static void renderPreview(GuiGraphics graphics, int x, int y, int width, int height, boolean selected) {
        List<CompoundTag> quests = pinnedQuestTags();
        if (quests.isEmpty()) {
            drawEmptyPreview(graphics, x, y, width, height, selected);
            return;
        }
        drawPinnedStack(graphics, quests, x, y, width, height, selected);
    }

    private static void drawPinnedStack(GuiGraphics graphics, List<CompoundTag> quests, int x, int y, int width, int height, boolean selected) {
        int safeH = Math.max(1, height);
        int count = Math.max(1, quests.size());
        int gap = count > 1 ? Math.min(STACK_GAP, Math.max(0, (safeH - count) / Math.max(1, count - 1))) : 0;
        int itemSpace = Math.max(count, safeH - gap * (count - 1));
        int baseItemSpace = 0;
        for (CompoundTag quest : quests) {
            baseItemSpace += heightForQuest(quest);
        }

        int rowY = y;
        int usedItemSpace = 0;
        for (int i = 0; i < quests.size(); i++) {
            CompoundTag quest = quests.get(i);
            int questHeight;
            if (i == quests.size() - 1) {
                questHeight = Math.max(1, itemSpace - usedItemSpace);
            } else {
                questHeight = Math.max(1, Math.round(heightForQuest(quest) * itemSpace / (float) Math.max(1, baseItemSpace)));
                usedItemSpace += questHeight;
            }
            drawPinnedQuest(graphics, quest, x, rowY, width, questHeight, selected);
            rowY += questHeight + gap;
        }
    }

    private static void drawPinnedQuest(GuiGraphics graphics, CompoundTag quest, int x, int y, int width, int height, boolean selected) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int safeW = Math.max(1, width);
        int safeH = Math.max(1, height);
        int titleColor = withAlpha(ModColors.TEXT_PRIMARY, 235);
        int secondary = withAlpha(ModColors.TEXT_SECONDARY, 202);
        int muted = withAlpha(ModColors.TEXT_MUTED, 154);

        QuestHudBackgroundRenderer.draw(graphics, QuestHudLayout.Element.PINNED, x, y, safeW, safeH, selected);
        if (safeW < PAD * 2 + 8 || safeH < 12) {
            return;
        }

        float progressValue = Math.max(0.0f, Math.min(1.0f, quest.getFloat("progress")));
        int progressPercent = Math.max(0, Math.min(100, Math.round(progressValue * 100.0f)));
        String percent = progressPercent + "%";
        int percentW = font.width(percent);
        int contentW = safeW - PAD * 2;
        boolean showPercent = contentW > percentW + 16;
        String title = cropToWidth(font, questTitle(quest), contentW - (showPercent ? percentW + 7 : 0));
        graphics.drawString(font, title, x + PAD, y + 5, titleColor, false);
        if (showPercent) {
            graphics.drawString(font, percent, x + safeW - PAD - percentW, y + 5, secondary, false);
        }

        int barX = x + PAD;
        int barY = y + Math.min(HEADER_HEIGHT - 8, Math.max(10, safeH - 9));
        int barW = safeW - PAD * 2;
        if (safeH >= 22) {
            String progressKey = ProgressAnimations.key("pinned_hud", quest.getString("_hud_id"));
            QuestHudProgressBar.draw(graphics, barX, barY, barW, 6, ProgressAnimations.value(progressKey, progressValue), ModColors.SUCCESS, 230);
        }

        List<RequirementLine> lines = requirementLines(quest);
        int maxRowsByHeight = Math.max(0, (y + safeH - 2 - (y + HEADER_HEIGHT + 2)) / ROW_HEIGHT);
        int shown = Math.min(Math.min(MAX_REQUIREMENT_ROWS, lines.size()), maxRowsByHeight);
        int more = Math.max(0, lines.size() - shown);
        int lineY = y + HEADER_HEIGHT + 2;
        if (lines.isEmpty() && maxRowsByHeight > 0) {
            graphics.drawString(font, Component.translatable("ui.questsandstuff.hud.no_requirements").getString(), x + PAD, lineY, muted, false);
            return;
        }
        for (int i = 0; i < shown; i++) {
            RequirementLine line = lines.get(i);
            String progress = line.progress();
            int progressW = progress.isBlank() ? 0 : font.width(progress);
            int titleW = safeW - PAD * 2 - 13 - progressW - 5;
            int color = line.complete() ? muted : secondary;
            QuestHudIconRenderer.draw(graphics, line.icon(), x + PAD, lineY - 1, 9, line.complete() ? 132 : 220);
            graphics.drawString(font, cropToWidth(font, line.title(), titleW), x + PAD + 13, lineY, color, false);
            if (!progress.isBlank()) {
                graphics.drawString(font, progress, x + safeW - PAD - progressW, lineY, color, false);
            }
            lineY += ROW_HEIGHT;
        }
        if (more > 0 && lineY + 8 <= y + safeH - 2) {
            String moreText = Component.translatable("ui.questsandstuff.hud.more_requirements", more).getString();
            graphics.drawString(font, cropToWidth(font, moreText, contentW), x + PAD, lineY, muted, false);
        }
    }

    private static void drawEmptyPreview(GuiGraphics graphics, int x, int y, int width, int height, boolean selected) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int safeW = Math.max(1, width);
        int safeH = Math.max(1, height);
        int contentW = Math.max(0, safeW - PAD * 2);
        QuestHudBackgroundRenderer.draw(graphics, QuestHudLayout.Element.PINNED, x, y, safeW, safeH, selected);
        if (contentW <= 0 || safeH < 12) {
            return;
        }
        graphics.drawString(font, cropToWidth(font, Component.translatable("ui.questsandstuff.hud.pinned_preview").getString(), contentW), x + PAD, y + 5, withAlpha(ModColors.TEXT_PRIMARY, 230), false);
        if (safeH >= 28) {
            graphics.drawString(font, cropToWidth(font, Component.translatable("ui.questsandstuff.hud.no_pinned_quest").getString(), contentW), x + PAD, y + HEADER_HEIGHT + 2, withAlpha(ModColors.TEXT_MUTED, 170), false);
        }
    }

    private static List<CompoundTag> pinnedQuestTags() {
        List<CompoundTag> quests = new ArrayList<>();
        for (String questId : ClientQuestCache.pinned()) {
            if (quests.size() >= MAX_QUESTS) {
                break;
            }
            CompoundTag quest = ClientQuestCache.quest(questId);
            if (!quest.isEmpty()) {
                quest.putString("_hud_id", questId);
                quests.add(quest);
            }
        }
        return quests;
    }

    private static int stackHeight(List<CompoundTag> quests) {
        int total = 0;
        for (CompoundTag quest : quests) {
            if (total > 0) {
                total += STACK_GAP;
            }
            total += heightForQuest(quest);
        }
        return Math.max(previewHeight(), total);
    }

    private static int heightForQuest(CompoundTag quest) {
        List<RequirementLine> lines = requirementLines(quest);
        int rows = Math.max(1, Math.min(MAX_REQUIREMENT_ROWS, lines.size()));
        if (lines.size() > rows) {
            rows++;
        }
        return HEADER_HEIGHT + PAD + rows * ROW_HEIGHT + 1;
    }

    private static List<RequirementLine> requirementLines(CompoundTag quest) {
        List<RequirementLine> lines = new ArrayList<>();
        if (quest == null) {
            return lines;
        }
        CompoundTag tasks = quest.getCompound("tasks");
        ListTag order = quest.getList("tasks_order", Tag.TAG_STRING);
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < order.size(); i++) {
            addRequirementLine(lines, seen, tasks, order.getString(i));
        }
        List<String> unordered = new ArrayList<>(tasks.getAllKeys());
        unordered.sort(String::compareTo);
        for (String id : unordered) {
            addRequirementLine(lines, seen, tasks, id);
        }
        return lines;
    }

    private static void addRequirementLine(List<RequirementLine> lines, Set<String> seen, CompoundTag tasks, String id) {
        if (id == null || id.isBlank() || !seen.add(id)) {
            return;
        }
        CompoundTag taskTag = tasks.getCompound(id);
        if (taskTag.isEmpty()) {
            return;
        }
        String title = QuestObjectiveHudDisplay.title(taskTag);
        lines.add(new RequirementLine(
                title == null || title.isBlank() ? id : title,
                QuestObjectiveHudDisplay.progressText(taskTag),
                taskTag.getBoolean("complete"),
                QuestObjectiveHudDisplay.icon(taskTag)
        ));
    }

    private static String questTitle(CompoundTag quest) {
        String title = quest.getString("title");
        if (title != null && !title.isBlank()) {
            return title;
        }
        String questId = quest.getString("_hud_id");
        return questId == null || questId.isBlank() ? Component.translatable("ui.questsandstuff.hud.pinned_preview").getString() : TabletUiFactory.shortQuestId(questId);
    }

    private static String cropToWidth(Font font, String value, int width) {
        String safe = value == null ? "" : value;
        if (width <= 0 || safe.isBlank()) {
            return "";
        }
        if (font.width(safe) <= width) {
            return safe;
        }
        String cropped = safe;
        while (!cropped.isEmpty() && font.width(cropped + "...") > width) {
            cropped = cropped.substring(0, cropped.length() - 1);
        }
        return cropped.isEmpty() ? "..." : cropped + "...";
    }

    private record RequirementLine(String title, String progress, boolean complete, String icon) {
    }
}
