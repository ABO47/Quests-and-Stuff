package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadKeys;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

final class EditorPrerequisiteCommandHandlers {
    private EditorPrerequisiteCommandHandlers() {
    }

    static void register(EditorCommandRegistrar registrar) {
        registrar.register(EditorCommandType.PREREQUISITE_ADD, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::addPrerequisite);
        registrar.register(EditorCommandType.PREREQUISITE_REMOVE, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::removePrerequisite);
        registrar.register(EditorCommandType.CONNECTION_COLOR, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::connectionColor);
        registrar.register(EditorCommandType.CONNECTION_MODE, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::connectionMode);
        registrar.register(EditorCommandType.CONNECTION_HIDDEN, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::connectionHidden);
    }

    private static void addPrerequisite(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestPrerequisite(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.prerequisite(payload), true);
    }

    private static void removePrerequisite(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestPrerequisite(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.prerequisite(payload), false);
    }

    private static void connectionColor(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setConnectionColor(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.prerequisite(payload),
                EditorCommandPayloads.integer(payload, EditorCommandPayloadKeys.COLOR)
        );
    }

    private static void connectionMode(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setConnectionMode(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.prerequisite(payload),
                EditorCommandPayloads.bool(payload, EditorCommandPayloadKeys.GRID)
        );
    }

    private static void connectionHidden(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setConnectionHidden(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.prerequisite(payload),
                EditorCommandPayloads.bool(payload, EditorCommandPayloadKeys.HIDDEN)
        );
    }
}
