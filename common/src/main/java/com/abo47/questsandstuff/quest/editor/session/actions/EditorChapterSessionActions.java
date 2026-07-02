package com.abo47.questsandstuff.quest.editor.session.actions;

import com.abo47.questsandstuff.quest.editor.chapter.ChapterEditService;
import net.minecraft.server.level.ServerPlayer;

public final class EditorChapterSessionActions {
    private final ChapterEditService chapterEdits;

    public EditorChapterSessionActions(ChapterEditService chapterEdits) {
        this.chapterEdits = chapterEdits;
    }

    public void createChapter(ServerPlayer player, String chapterName) {
        chapterEdits.createChapter(player, chapterName);
    }

    public void deleteChapter(ServerPlayer player, String chapterName) {
        chapterEdits.deleteChapter(player, chapterName);
    }

    public void moveChapter(ServerPlayer player, String chapterName, int offset) {
        chapterEdits.moveChapter(player, chapterName, offset);
    }

    public void moveChapterToIndex(ServerPlayer player, String chapterName, int targetIndex) {
        chapterEdits.moveChapterToIndex(player, chapterName, targetIndex);
    }

    public void renameChapter(ServerPlayer player, String fromName, String toName) {
        chapterEdits.renameChapter(player, fromName, toName);
    }

    public void setChapterIcon(ServerPlayer player, String chapterName, String iconId) {
        chapterEdits.setChapterIcon(player, chapterName, iconId);
    }

    public void setChapterBackground(ServerPlayer player, String chapterName, String backgroundId) {
        chapterEdits.setChapterBackground(player, chapterName, backgroundId);
    }

    public void setChapterCanvasBackground(ServerPlayer player, String chapterName, String backgroundId) {
        chapterEdits.setChapterCanvasBackground(player, chapterName, backgroundId);
    }

    public void setChapterTextAlign(ServerPlayer player, String chapterName, String align) {
        chapterEdits.setChapterTextAlign(player, chapterName, align);
    }

    public void setChapterTextColor(ServerPlayer player, String chapterName, int color) {
        chapterEdits.setChapterTextColor(player, chapterName, color);
    }

    public void setChapterTextStyle(ServerPlayer player, String chapterName, String style) {
        chapterEdits.setChapterTextStyle(player, chapterName, style);
    }

    public void setChapterTextSize(ServerPlayer player, String chapterName, int size) {
        chapterEdits.setChapterTextSize(player, chapterName, size);
    }

    public void setChapterLockUntilUnlocked(ServerPlayer player, String chapterName, boolean lockUntilUnlocked) {
        chapterEdits.setChapterLockUntilUnlocked(player, chapterName, lockUntilUnlocked);
    }

    public void setChapterHideUntilUnlocked(ServerPlayer player, String chapterName, boolean hideUntilUnlocked) {
        chapterEdits.setChapterHideUntilUnlocked(player, chapterName, hideUntilUnlocked);
    }
}
