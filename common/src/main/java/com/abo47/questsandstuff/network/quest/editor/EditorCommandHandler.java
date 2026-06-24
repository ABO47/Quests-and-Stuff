package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
interface EditorCommandHandler {
    void apply(ServerPlayer player, EditorSessionService editor, CompoundTag payload);
}
