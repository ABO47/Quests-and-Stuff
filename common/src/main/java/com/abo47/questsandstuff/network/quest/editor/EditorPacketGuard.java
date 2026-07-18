package com.abo47.questsandstuff.network.quest.editor;

import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.editor.QuestEditorPermissions;

final class EditorPacketGuard {
    private EditorPacketGuard() {
    }

    static boolean canEdit(ServerPlayer player) {
        return QuestEditorPermissions.canEdit(player);
    }
}
