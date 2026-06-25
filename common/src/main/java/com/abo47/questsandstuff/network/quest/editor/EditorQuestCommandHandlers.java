package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadKeys;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadReader;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

final class EditorQuestCommandHandlers {
    private EditorQuestCommandHandlers() {
    }

    static void register(EditorCommandRegistrar registrar) {
        registrar.register(EditorCommandType.QUEST_ICON, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::questIcon);
        registrar.register(EditorCommandType.QUEST_REPEATABLE, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::questRepeatable);
        registrar.register(EditorCommandType.QUEST_HIDDEN_MODE, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::questHiddenMode);
        registrar.register(EditorCommandType.QUEST_VISUAL_HIDDEN, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::questVisualHidden);
        registrar.register(EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::completionSound);
        registrar.register(EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND_MANY, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::completionSoundMany);
        registrar.register(EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND_VOLUME, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::completionSoundVolume);
        registrar.register(EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND_VOLUME_MANY, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::completionSoundVolumeMany);
        registrar.register(EditorCommandType.QUEST_COMPLETION_HUD_BACKGROUND, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::completionHudBackground);
        registrar.register(EditorCommandType.QUEST_COMPLETION_HUD_BACKGROUND_MANY, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::completionHudBackgroundMany);
        registrar.register(EditorCommandType.QUEST_BACKGROUND, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::questBackground);
        registrar.register(EditorCommandType.QUEST_BACKGROUND_MANY, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::questBackgroundMany);
    }

    private static void questIcon(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestIcon(
                player,
                EditorCommandPayloadReader.quest(payload),
                EditorCommandPayloadReader.string(payload, EditorCommandPayloadKeys.ICON)
        );
    }

    private static void questRepeatable(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestRepeatable(
                player,
                EditorCommandPayloadReader.quest(payload),
                EditorCommandPayloadReader.bool(payload, EditorCommandPayloadKeys.ENABLED)
        );
    }

    private static void questHiddenMode(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestHiddenMode(
                player,
                EditorCommandPayloadReader.quest(payload),
                EditorCommandPayloadReader.string(payload, EditorCommandPayloadKeys.MODE)
        );
    }

    private static void questVisualHidden(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestVisualHidden(
                player,
                EditorCommandPayloadReader.quest(payload),
                EditorCommandPayloadReader.bool(payload, EditorCommandPayloadKeys.HIDDEN)
        );
    }

    private static void completionSound(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestCompletionSound(
                player,
                EditorCommandPayloadReader.quest(payload),
                EditorCommandPayloadReader.string(payload, EditorCommandPayloadKeys.SOUND)
        );
    }

    private static void completionSoundMany(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestCompletionSound(
                player,
                EditorCommandPayloadReader.questIds(payload),
                EditorCommandPayloadReader.string(payload, EditorCommandPayloadKeys.SOUND)
        );
    }

    private static void completionSoundVolume(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestCompletionSoundVolume(
                player,
                EditorCommandPayloadReader.quest(payload),
                EditorCommandPayloadReader.integer(payload, EditorCommandPayloadKeys.VOLUME)
        );
    }

    private static void completionSoundVolumeMany(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestCompletionSoundVolume(
                player,
                EditorCommandPayloadReader.questIds(payload),
                EditorCommandPayloadReader.integer(payload, EditorCommandPayloadKeys.VOLUME)
        );
    }

    private static void completionHudBackground(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestCompletionHudBackground(
                player,
                EditorCommandPayloadReader.quest(payload),
                EditorCommandPayloadReader.string(payload, EditorCommandPayloadKeys.BACKGROUND)
        );
    }

    private static void completionHudBackgroundMany(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestCompletionHudBackground(
                player,
                EditorCommandPayloadReader.questIds(payload),
                EditorCommandPayloadReader.string(payload, EditorCommandPayloadKeys.BACKGROUND)
        );
    }

    private static void questBackground(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestBackground(
                player,
                EditorCommandPayloadReader.quest(payload),
                EditorCommandPayloadReader.string(payload, EditorCommandPayloadKeys.BACKGROUND),
                EditorCommandPayloadReader.bool(payload, EditorCommandPayloadKeys.GRAYSCALE)
        );
    }

    private static void questBackgroundMany(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestBackground(
                player,
                EditorCommandPayloadReader.questIds(payload),
                EditorCommandPayloadReader.string(payload, EditorCommandPayloadKeys.BACKGROUND),
                EditorCommandPayloadReader.bool(payload, EditorCommandPayloadKeys.GRAYSCALE)
        );
    }
}
