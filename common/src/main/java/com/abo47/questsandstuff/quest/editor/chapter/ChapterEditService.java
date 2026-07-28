package com.abo47.questsandstuff.quest.editor.chapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.quest.model.ChapterDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;

import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withChapters;
import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withSettings;

public final class ChapterEditService {
    private final EditorSessionService owner;

    public ChapterEditService(EditorSessionService owner) {
        this.owner = owner;
    }

    public void createChapter(ServerPlayer player, String chapterName) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (chapter.isBlank()) {
            return;
        }
        if (owner.definitionStore().chapterOrder().contains(chapter)) {
            owner.session(player).currentChapter = chapter;
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.ensureChapterExists(chapter);
        owner.session(player).currentChapter = chapter;
        owner.postMutation(player);
    }

    public void deleteChapter(ServerPlayer player, String chapterName) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (chapter.isBlank() || !owner.definitionStore().chapterOrder().contains(chapter)) {
            return;
        }
        EditorSessionService.EditorSession session = owner.session(player);
        owner.captureUndo(session);

        List<String> chapters = new ArrayList<>(owner.definitionStore().chapterOrder());
        chapters.remove(chapter);

        for (QuestDefinition quest : new ArrayList<>(owner.definitionStore().quests().values())) {
            if (!quest.display().chapters().containsKey(chapter)) {
                continue;
            }
            Map<String, ChapterDef> map = new HashMap<>(quest.display().chapters());
            map.remove(chapter);
            if (map.isEmpty()) {
                QuestsAndStuffMod.debugLog("[QnS:Editor] deleting quest without remaining chapters quest={} removedChapter={}", quest.id(), chapter);
                owner.definitionStore().remove(quest.id());
            } else {
                owner.definitionStore().upsert(withChapters(quest, map));
            }
        }
        owner.definitionStore().setChapterOrder(chapters);
        session.currentChapter = chapters.isEmpty() ? "" : chapters.get(0);
        owner.normalizeQuestSelection(session);
        owner.postMutation(player);
    }

    public void moveChapter(ServerPlayer player, String chapterName, int offset) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (chapter.isBlank() || offset == 0) {
            return;
        }
        List<String> chapters = new ArrayList<>(owner.definitionStore().chapterOrder());
        int index = chapters.indexOf(chapter);
        if (index < 0) {
            return;
        }
        int next = Math.max(0, Math.min(chapters.size() - 1, index + offset));
        if (next == index) {
            return;
        }
        owner.captureUndo(owner.session(player));
        chapters.remove(index);
        chapters.add(next, chapter);
        owner.definitionStore().setChapterOrder(chapters);
        owner.session(player).currentChapter = chapter;
        owner.postMutation(player);
    }

    public void moveChapterToIndex(ServerPlayer player, String chapterName, int targetIndex) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (chapter.isBlank()) {
            return;
        }
        List<String> chapters = new ArrayList<>(owner.definitionStore().chapterOrder());
        int index = chapters.indexOf(chapter);
        if (index < 0) {
            return;
        }
        int next = Math.max(0, Math.min(chapters.size() - 1, targetIndex));
        if (next == index) {
            return;
        }
        owner.captureUndo(owner.session(player));
        chapters.remove(index);
        chapters.add(next, chapter);
        owner.definitionStore().setChapterOrder(chapters);
        owner.session(player).currentChapter = chapter;
        owner.postMutation(player);
    }

    public void renameChapter(ServerPlayer player, String fromName, String toName) {
        String from = EditorSessionService.normalizeChapter(fromName);
        String to = EditorSessionService.normalizeChapter(toName);
        if (from.isBlank() || to.isBlank() || from.equals(to)) {
            return;
        }
        List<String> chapters = new ArrayList<>(owner.definitionStore().chapterOrder());
        int index = chapters.indexOf(from);
        if (index < 0 || chapters.contains(to)) {
            return;
        }

        EditorSessionService.EditorSession session = owner.session(player);
        owner.captureUndo(session);

        owner.definitionStore().renameChapterMetadata(from, to);
        for (QuestDefinition quest : new ArrayList<>(owner.definitionStore().quests().values())) {
            if (!quest.display().chapters().containsKey(from)) {
                continue;
            }
            Map<String, ChapterDef> map = new HashMap<>(quest.display().chapters());
            ChapterDef view = map.remove(from);
            map.put(to, view == null ? ChapterDef.DEFAULT : view);
            owner.definitionStore().upsert(withChapters(quest, map));
        }
        chapters.set(index, to);
        owner.definitionStore().setChapterOrder(chapters);

        if (from.equals(session.currentChapter)) {
            session.currentChapter = to;
        }
        owner.postMutation(player);
    }

    public void setChapterIcon(ServerPlayer player, String chapterName, String iconId) {
        String chapter = validChapter(chapterName);
        String value = iconId == null ? "" : iconId.trim();
        if (chapter.isBlank() || owner.definitionStore().chapterIcon(chapter).equals(value)) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().setChapterIcon(chapter, value);
        owner.postMutation(player);
    }

    public void setChapterBackground(ServerPlayer player, String chapterName, String backgroundId) {
        String chapter = validChapter(chapterName);
        String value = backgroundId == null || backgroundId.isBlank() ? "default" : backgroundId.trim();
        if (chapter.isBlank() || owner.definitionStore().chapterBackground(chapter).equals(value)) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().setChapterBackground(chapter, value);
        owner.postMutation(player);
    }

    public void setChapterCanvasBackground(ServerPlayer player, String chapterName, String backgroundId) {
        String chapter = validChapter(chapterName);
        String value = backgroundId == null || backgroundId.isBlank() ? "default" : backgroundId.trim();
        if (chapter.isBlank() || owner.definitionStore().chapterCanvasBackground(chapter).equals(value)) {
            return;
        }
        owner.captureUndo(owner.session(player));
        QuestsAndStuffMod.debugLog("[QnS:Editor] chapter canvas background chapter={} background={}", chapter, value);
        owner.definitionStore().setChapterCanvasBackground(chapter, value);
        owner.postMutation(player);
    }

    public void setChapterTextAlign(ServerPlayer player, String chapterName, String align) {
        String chapter = validChapter(chapterName);
        String value = normalizeTextAlign(align);
        if (chapter.isBlank() || owner.definitionStore().chapterTextAlign(chapter).equals(value)) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().setChapterTextAlign(chapter, value);
        owner.postMutation(player);
    }

    public void setChapterTextColor(ServerPlayer player, String chapterName, int color) {
        String chapter = validChapter(chapterName);
        if (chapter.isBlank() || owner.definitionStore().chapterTextColor(chapter) == color) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().setChapterTextColor(chapter, color);
        owner.postMutation(player);
    }

    public void setChapterTextStyle(ServerPlayer player, String chapterName, String style) {
        String chapter = validChapter(chapterName);
        String value = normalizeTextStyle(style);
        if (chapter.isBlank() || owner.definitionStore().chapterTextStyle(chapter).equals(value)) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().setChapterTextStyle(chapter, value);
        owner.postMutation(player);
    }

    public void setChapterTextSize(ServerPlayer player, String chapterName, int size) {
        String chapter = validChapter(chapterName);
        int value = CanvasTextLayer.clampFontSize(size);
        if (chapter.isBlank() || owner.definitionStore().chapterTextSize(chapter) == value) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().setChapterTextSize(chapter, value);
        owner.postMutation(player);
    }

    public void setChapterLockUntilUnlocked(ServerPlayer player, String chapterName, boolean lockUntilUnlocked) {
        String chapter = validChapter(chapterName);
        if (chapter.isBlank() || owner.definitionStore().chapterLockUntilUnlocked(chapter) == lockUntilUnlocked) {
            return;
        }
        EditorSessionService.EditorSession session = owner.session(player);
        owner.captureUndo(session);
        owner.definitionStore().setChapterLockUntilUnlocked(chapter, lockUntilUnlocked);

        QuestVisibilityMode mode = lockUntilUnlocked ? QuestVisibilityMode.LOCKED : QuestVisibilityMode.PREREQUISITES_VISIBLE;
        for (QuestDefinition quest : new ArrayList<>(owner.definitionStore().quests().values())) {
            if (!quest.display().chapters().containsKey(chapter) || quest.settings().hiddenMode() == mode) {
                continue;
            }
            QuestSettings old = quest.settings();
            QuestSettings settings = new QuestSettings(
                    old.individualProgress(),
                    mode,
                    old.repeatable(),
                    old.autoClaimRewards(),
                    old.unlockNotification(),
                    old.showPrerequisiteArrow()
            );
            owner.definitionStore().upsert(withSettings(quest, settings));
        }
        owner.postMutation(player);
    }

    public void setChapterHideUntilUnlocked(ServerPlayer player, String chapterName, boolean hideUntilUnlocked) {
        String chapter = validChapter(chapterName);
        if (chapter.isBlank() || owner.definitionStore().chapterHideUntilUnlocked(chapter) == hideUntilUnlocked) {
            return;
        }
        EditorSessionService.EditorSession session = owner.session(player);
        owner.captureUndo(session);
        owner.definitionStore().setChapterHideUntilUnlocked(chapter, hideUntilUnlocked);
        owner.postMutation(player);
    }

    private String validChapter(String chapterName) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        return chapter.isBlank() || !owner.definitionStore().chapterOrder().contains(chapter) ? "" : chapter;
    }

    private static String normalizeTextAlign(String align) {
        String value = align == null ? "" : align.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "left", "right" -> value;
            default -> "center";
        };
    }

    private static String normalizeTextStyle(String style) {
        return CanvasTextLayer.normalizeStyle(style);
    }
}
