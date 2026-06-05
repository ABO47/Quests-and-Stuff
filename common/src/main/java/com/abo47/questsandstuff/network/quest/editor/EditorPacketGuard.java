package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.quest.editor.QuestEditorPermissions;
import net.minecraft.server.level.ServerPlayer;

final class EditorPacketGuard {
    private EditorPacketGuard() {
    }

    static boolean canEdit(ServerPlayer player) {
        return QuestEditorPermissions.canEdit(player);
    }
}
