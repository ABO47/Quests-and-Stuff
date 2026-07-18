package com.abo47.questsandstuff.network.quest.editor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;

record EditorCommandDescriptor(EditorCommandType type, EditorCommandFamily family, EditorCommandHandler handler) {
    EditorCommandDescriptor {
        if (type == null || type == EditorCommandType.UNKNOWN) {
            throw new IllegalArgumentException("Editor command descriptor needs a concrete type");
        }
        if (family == null) {
            throw new IllegalArgumentException("Editor command descriptor needs a family");
        }
        if (handler == null) {
            throw new IllegalArgumentException("Editor command descriptor needs a handler");
        }
    }

    void apply(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        handler.apply(player, editor, payload);
    }
}
