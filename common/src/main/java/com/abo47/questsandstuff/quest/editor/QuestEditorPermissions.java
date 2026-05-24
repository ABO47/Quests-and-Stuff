package com.abo47.questsandstuff.quest.editor;

import net.minecraft.server.level.ServerPlayer;

public final class QuestEditorPermissions {
    private QuestEditorPermissions() {
    }

    public static boolean canEdit(ServerPlayer player) {
        return player != null && player.hasPermissions(2);
    }
}
