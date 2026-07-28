package com.abo47.questsandstuff.network.quest.editor;

import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;

final class EditorPrerequisiteCommandHandlers {
    private EditorPrerequisiteCommandHandlers() {
    }

    static void register(Consumer<EditorCommandDescriptor> registrar) {
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.PREREQUISITE_ADD, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::addPrerequisite));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.PREREQUISITE_REMOVE, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::removePrerequisite));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.CONNECTION_COLOR, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::connectionColor));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.CONNECTION_MODE, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::connectionMode));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.CONNECTION_HIDDEN, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::connectionHidden));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.CONNECTION_TEXTURE, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::connectionTexture));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.CONNECTION_TEXTURE_MANY, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::connectionTextures));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.CONNECTION_TEXTURE_SPACING, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::connectionTextureSpacing));
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
                EditorCommandPayloads.integer(payload, EditorCommandPayloads.COLOR)
        );
    }

    private static void connectionMode(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setConnectionMode(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.prerequisite(payload),
                EditorCommandPayloads.bool(payload, EditorCommandPayloads.GRID)
        );
    }

    private static void connectionHidden(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setConnectionHidden(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.prerequisite(payload),
                EditorCommandPayloads.bool(payload, EditorCommandPayloads.HIDDEN)
        );
    }

    private static void connectionTexture(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setConnectionTexture(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.prerequisite(payload),
                EditorCommandPayloads.string(payload, EditorCommandPayloads.TEXTURE)
        );
    }

    private static void connectionTextures(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        Map<String, Map<String, String>> questTextures = EditorCommandPayloads.connectionTextureMap(payload);
        editor.setConnectionTextures(player, questTextures);
    }

    private static void connectionTextureSpacing(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setConnectionTextureSpacing(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.prerequisite(payload),
                EditorCommandPayloads.integer(payload, EditorCommandPayloads.SPACING)
        );
    }
}
