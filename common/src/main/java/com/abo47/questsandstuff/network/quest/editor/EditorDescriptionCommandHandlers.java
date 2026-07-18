package com.abo47.questsandstuff.network.quest.editor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;

final class EditorDescriptionCommandHandlers {
    private EditorDescriptionCommandHandlers() {
    }

    static void register(EditorCommandRegistrar registrar) {
        registrar.register(EditorCommandType.DESCRIPTION_PUT, EditorCommandFamily.DESCRIPTION, EditorDescriptionCommandHandlers::descriptionPut);
    }

    private static void descriptionPut(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        ListTag description = EditorCommandPayloads.description(payload);
        if (EditorCommandPayloads.exceedsLimit(description, EditorCommandPayloads.MAX_DESCRIPTION_LINES)) {
            return;
        }
        editor.updateQuestDescription(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.stringsFrom(description));
    }
}
