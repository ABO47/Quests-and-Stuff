package com.abo47.questsandstuff.quest.editor.session.actions;

import com.abo47.questsandstuff.quest.editor.chapter.ChapterEditService;
import net.minecraft.server.level.ServerPlayer;

public final class EditorChapterSessionActions {
    private final ChapterEditService chapterEdits;

    public EditorChapterSessionActions(ChapterEditService chapterEdits) {
        this.chapterEdits = chapterEdits;
    }

    public void createGroup(ServerPlayer player, String groupName) {
        chapterEdits.createGroup(player, groupName);
    }

    public void deleteGroup(ServerPlayer player, String groupName) {
        chapterEdits.deleteGroup(player, groupName);
    }

    public void moveGroup(ServerPlayer player, String groupName, int offset) {
        chapterEdits.moveGroup(player, groupName, offset);
    }

    public void moveGroupToIndex(ServerPlayer player, String groupName, int targetIndex) {
        chapterEdits.moveGroupToIndex(player, groupName, targetIndex);
    }

    public void renameGroup(ServerPlayer player, String fromName, String toName) {
        chapterEdits.renameGroup(player, fromName, toName);
    }

    public void setGroupIcon(ServerPlayer player, String groupName, String iconId) {
        chapterEdits.setGroupIcon(player, groupName, iconId);
    }

    public void setGroupBackground(ServerPlayer player, String groupName, String backgroundId) {
        chapterEdits.setGroupBackground(player, groupName, backgroundId);
    }

    public void setGroupCanvasBackground(ServerPlayer player, String groupName, String backgroundId) {
        chapterEdits.setGroupCanvasBackground(player, groupName, backgroundId);
    }

    public void setGroupTextAlign(ServerPlayer player, String groupName, String align) {
        chapterEdits.setGroupTextAlign(player, groupName, align);
    }

    public void setGroupTextColor(ServerPlayer player, String groupName, int color) {
        chapterEdits.setGroupTextColor(player, groupName, color);
    }

    public void setGroupTextStyle(ServerPlayer player, String groupName, String style) {
        chapterEdits.setGroupTextStyle(player, groupName, style);
    }

    public void setGroupTextSize(ServerPlayer player, String groupName, int size) {
        chapterEdits.setGroupTextSize(player, groupName, size);
    }

    public void setGroupLockUntilUnlocked(ServerPlayer player, String groupName, boolean lockUntilUnlocked) {
        chapterEdits.setGroupLockUntilUnlocked(player, groupName, lockUntilUnlocked);
    }
}
